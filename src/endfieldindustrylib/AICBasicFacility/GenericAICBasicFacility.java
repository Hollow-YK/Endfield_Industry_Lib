package endfieldindustrylib.AICBasicFacility;

import arc.Core;
import arc.graphics.Color;
import arc.math.*;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Label;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.Vars;
import endfieldindustrylib.ui.GridItemsDisplay;

// 导入自定义物流方块
import endfieldindustrylib.AICTransport.TransportBelt;
import endfieldindustrylib.AICTransport.ItemControlPort;
import endfieldindustrylib.AICTransport.Splitter;
import endfieldindustrylib.AICTransport.BeltBridge;
import endfieldindustrylib.AICTransport.Converger;

import java.util.function.Consumer;

/**
 * 通用多槽位工厂基类。
 * 支持：
 * - 分别设置输入/输出允许的方向（相对于建筑朝向）
 * - 固定物品类型的输入/输出槽位，每个槽位最多堆叠50个
 * - 通用槽位（itemType = null）可接受任意物品，但每种物品仍只占一个槽位
 * - 多个配方，自动匹配第一个可用的配方进行生产
 * - 点击建筑显示所有槽位（输入左，输出右，中间箭头）
 * - 输入槽位中，一种物品仅允许占一个槽位
 * - 输出并发：同一输出槽可同时向多个方向输出
 * - 仅与自定义传送带系列方块交互（TransportBelt, ItemControlPort, Splitter, BeltBridge, Converger）
 */
public class GenericAICBasicFacility extends Block {
    public int inputFacingMask = 0b1111;
    public int outputFacingMask = 0b1111;
    public SlotDef[] inputSlotDefs = {};
    public SlotDef[] outputSlotDefs = {};
    public Recipe[] recipes = {};

    // 静态回调，用于显示自定义配置面板（由 Mod 主类注册）
    public static Consumer<GenericAICBasicFacilityBuild> showConfigHandler;

    public static class SlotDef {
        public Item item; // null 表示通用槽位
        public SlotDef(Item item) { this.item = item; }
    }

    public static class Recipe {
        public ItemStack[] input;
        public ItemStack[] output;
        public float craftTime;
        public Recipe(ItemStack[] input, ItemStack[] output, float craftTime) {
            this.input = input; this.output = output; this.craftTime = craftTime;
        }
    }

    public GenericAICBasicFacility(String name) {
        super(name);
        update = true; 
        solid = true; 
        hasItems = true;
        hasLiquids = false;
        configurable = true; 
        ambientSound = Sounds.loopMachine;
        sync = true;
        drawArrow = false;
        flags = EnumSet.of(BlockFlag.factory);
        rotate = true;
    }

    @Override
    public void setStats() {
        super.setStats();
        if (recipes.length > 0) {
            // 使用 productionTime 作为标题，内部构建一个表格展示所有配方
            stats.add(Stat.productionTime, table -> {
                table.table(recipesTable -> {
                    for (int i = 0; i < recipes.length; i++) {
                        Recipe r = recipes[i];
                        if (i > 0) recipesTable.row(); // 配方之间添加分隔

                        recipesTable.table(line -> {
                            // 输入物品：将 r.input 转换为 GridItemsDisplay.Slot 数组
                            GridItemsDisplay.Slot[] inputSlots = new GridItemsDisplay.Slot[r.input.length];
                            for (int j = 0; j < r.input.length; j++) {
                                ItemStack stack = r.input[j];
                                inputSlots[j] = new GridItemsDisplay.Slot(stack.item, stack.amount);
                            }
                            GridItemsDisplay inputDisplay = GridItemsDisplay.withFixedColumns(2);
                            inputDisplay.setSlots(inputSlots);
                            line.add(inputDisplay).left();

                            line.add(" -> "); // 箭头表示转化

                            // 输出物品
                            GridItemsDisplay.Slot[] outputSlots = new GridItemsDisplay.Slot[r.output.length];
                            for (int j = 0; j < r.output.length; j++) {
                                ItemStack stack = r.output[j];
                                outputSlots[j] = new GridItemsDisplay.Slot(stack.item, stack.amount);
                            }
                            GridItemsDisplay outputDisplay = GridItemsDisplay.withFixedColumns(2);
                            outputDisplay.setSlots(outputSlots);
                            line.add(outputDisplay).left();

                            // 制造时间（以秒为单位）
                            line.add("  " + (r.craftTime / 60f) + " " + StatUnit.seconds.localized());
                        }).pad(4);
                    }
                }).pad(4).left();
            });
        }
    }

    @Override public void setBars() {
        super.setBars(); removeBar("liquid");
        addBar("progress", (GenericAICBasicFacilityBuild e) -> new Bar("bar.progress", Pal.ammo, e::progress));
    }

    @Override public void init() {
        if (recipes != null) {
            for (Recipe recipe : recipes) {
                if (recipe.input.length > inputSlotDefs.length)
                    throw new IllegalArgumentException("Recipe input count exceeds input slots");
                for (int i = 0; i < recipe.input.length; i++) {
                    ItemStack stack = recipe.input[i];
                    if (stack.amount > 50) throw new IllegalArgumentException("Input amount >50 at slot " + i);
                    if (inputSlotDefs[i].item != null && inputSlotDefs[i].item != stack.item)
                        throw new IllegalArgumentException("Input item mismatch at slot " + i);
                }
                if (recipe.output.length > outputSlotDefs.length)
                    throw new IllegalArgumentException("Recipe output count exceeds output slots");
                for (int i = 0; i < recipe.output.length; i++) {
                    ItemStack stack = recipe.output[i];
                    if (stack.amount > 50) throw new IllegalArgumentException("Output amount >50 at slot " + i);
                    if (outputSlotDefs[i].item != null && outputSlotDefs[i].item != stack.item)
                        throw new IllegalArgumentException("Output item mismatch at slot " + i);
                }
            }
        }
        super.init();
    }

    @Override public boolean outputsItems() { return true; }

    public class GenericAICBasicFacilityBuild extends Building {
        public Slot[] inputSlots, outputSlots;
        public float progress, warmup;
        public Recipe currentRecipe;
        public int currentRecipeIndex = -1;

        // 轮询索引：每个输出槽上次输出到的相邻建筑在proximity中的位置
        private int[] lastOutputIndex;

        public GenericAICBasicFacilityBuild() {
            inputSlots = new Slot[inputSlotDefs.length];
            for (int i = 0; i < inputSlotDefs.length; i++) inputSlots[i] = new Slot(inputSlotDefs[i].item);
            outputSlots = new Slot[outputSlotDefs.length];
            for (int i = 0; i < outputSlotDefs.length; i++) outputSlots[i] = new Slot(outputSlotDefs[i].item);
            lastOutputIndex = new int[outputSlotDefs.length];
        }

        public class Slot {
            public final Item fixedType; // null = 通用
            public Item currentItem;      // 当前存放的物品类型（仅当fixedType==null且非空时有效）
            public int amount;
            public final int maxAmount = 50;

            public Slot(Item fixedType) {
                this.fixedType = fixedType;
                this.currentItem = null;
            }

            public boolean accepts(Item item) {
                if (fixedType != null) return item == fixedType && amount < maxAmount;
                // 通用槽位：为空可接受任何物品；非空只能接受与当前物品相同的物品
                return (amount == 0 || currentItem == item) && amount < maxAmount;
            }

            public int add(Item item, int count) {
                int added = Math.min(count, maxAmount - amount);
                if (added > 0) {
                    if (amount == 0) currentItem = item;
                    amount += added;
                }
                return added;
            }

            public int remove(int count) {
                int removed = Math.min(count, amount);
                amount -= removed;
                if (amount == 0) currentItem = null;
                return removed;
            }

            public boolean has(Item item, int count) {
                if (fixedType != null) return fixedType == item && amount >= count;
                return currentItem == item && amount >= count;
            }
        }

        /** 检查相邻建筑是否为允许交互的自定义物流方块 */
        private boolean isAllowedTransport(Building other) {
            return other instanceof TransportBelt.TransportBeltBuild ||
                   other instanceof ItemControlPort.ItemControlPortBuild ||
                   other instanceof Splitter.SplitterBuild ||
                   other instanceof BeltBridge.BeltBridgeBuild ||
                   other instanceof Converger.ConvergerBuild;
        }

        @Override public void updateTile() {
            if (currentRecipe == null || !canUseRecipe(currentRecipe)) findRecipe();
            if (currentRecipe != null && canUseRecipe(currentRecipe)) {
                float inc = getProgressIncrease(currentRecipe.craftTime);
                progress += inc;
                warmup = Mathf.approachDelta(warmup, 1f, 0.019f);
                if (progress >= 1f) craft();
            } else {
                warmup = Mathf.approachDelta(warmup, 0f, 0.019f);
            }
            dumpOutputs();
        }

        public boolean canUseRecipe(Recipe recipe) {
            for (int i = 0; i < recipe.input.length; i++) {
                ItemStack req = recipe.input[i];
                Slot slot = inputSlots[i];
                if (!slot.has(req.item, req.amount)) return false;
            }
            for (int i = 0; i < recipe.output.length; i++) {
                ItemStack prod = recipe.output[i];
                Slot slot = outputSlots[i];
                if (slot.fixedType != null && slot.fixedType != prod.item) return false;
                if (slot.amount + prod.amount > slot.maxAmount) return false;
            }
            return true;
        }

        public void findRecipe() {
            if (recipes == null) return;
            for (int i = 0; i < recipes.length; i++) {
                if (canUseRecipe(recipes[i])) {
                    currentRecipe = recipes[i];
                    currentRecipeIndex = i;
                    progress = 0f;
                    return;
                }
            }
            currentRecipe = null; currentRecipeIndex = -1;
        }

        public void craft() {
            if (currentRecipe == null) return;
            for (int i = 0; i < currentRecipe.input.length; i++) {
                ItemStack req = currentRecipe.input[i];
                inputSlots[i].remove(req.amount);
            }
            for (int i = 0; i < currentRecipe.output.length; i++) {
                ItemStack prod = currentRecipe.output[i];
                outputSlots[i].add(prod.item, prod.amount);
            }
            progress %= 1f;
        }

        public float getProgressIncrease(float baseTime) {
            return edelta() * efficiency / baseTime;
        }

        /**
         * 公平轮询输出：每个输出槽在计时器允许时，尽可能将槽内物品分发给相邻可接受建筑。
         * 使用 lastOutputIndex 记录每个槽上次输出的建筑索引，实现轮询公平性。
         */
        public void dumpOutputs() {
            if (!timer(timerDump, dumpTime / timeScale)) return;

            // 确保 lastOutputIndex 长度与当前输出槽一致（防止配置变更）
            if (lastOutputIndex.length != outputSlots.length) {
                lastOutputIndex = new int[outputSlots.length];
            }

            // 获取相邻建筑列表并转为数组（便于按索引访问）
            Building[] neighbors = proximity.toArray(Building.class);
            int n = neighbors.length;
            if (n == 0) return;

            for (int slotIdx = 0; slotIdx < outputSlots.length; slotIdx++) {
                Slot slot = outputSlots[slotIdx];
                if (slot.amount <= 0 || slot.currentItem == null) continue;
                Item item = slot.fixedType != null ? slot.fixedType : slot.currentItem;

                int startIdx = lastOutputIndex[slotIdx] % n; // 从上一次的位置开始

                // 当前 tick 内尽可能输出该槽位的所有物品
                while (slot.amount > 0) {
                    boolean found = false;
                    for (int i = 0; i < n; i++) {
                        int idx = (startIdx + i) % n;
                        Building other = neighbors[idx];
                        if (other == null || other.team != team) continue;

                        // 方向检查（相对于建筑朝向）
                        int worldDir = relativeTo(other);
                        int localDir = (worldDir - rotation + 4) % 4;
                        if ((outputFacingMask & (1 << localDir)) == 0) continue;

                        if (!isAllowedTransport(other)) continue;

                        if (other.acceptItem(this, item)) {
                            other.handleItem(this, item);
                            slot.remove(1);
                            // 更新下次起始位置为当前建筑的下一个
                            lastOutputIndex[slotIdx] = (idx + 1) % n;
                            startIdx = lastOutputIndex[slotIdx]; // 更新 startIdx 以便继续
                            found = true;
                            break; // 输出成功，继续尝试下一个物品
                        }
                    }
                    if (!found) break; // 没有建筑可接受，退出循环
                }
            }
        }

        private int findAcceptableInputSlot(Item item) {
            IntSeq candidates = new IntSeq();
            for (int i = 0; i < inputSlots.length; i++) {
                if (inputSlots[i].accepts(item)) candidates.add(i);
            }
            if (candidates.size == 0) return -1;
            IntSeq occupied = new IntSeq();
            for (int i = 0; i < candidates.size; i++) {
                int idx = candidates.get(i);
                Slot s = inputSlots[idx];
                if (s.amount > 0 && (s.fixedType != null ? s.fixedType == item : s.currentItem == item)) {
                    occupied.add(idx);
                }
            }
            if (occupied.size > 1) return -1;
            if (occupied.size == 1) {
                int idx = occupied.get(0);
                return inputSlots[idx].amount < inputSlots[idx].maxAmount ? idx : -1;
            } else {
                return candidates.get(0);
            }
        }

        @Override public void handleItem(Building source, Item item) {
            int idx = findAcceptableInputSlot(item);
            if (idx != -1) inputSlots[idx].add(item, 1);
        }

        @Override public boolean acceptItem(Building source, Item item) {
            if (source == null || !isAllowedTransport(source)) return false;
            int worldDir = relativeTo(source); // 世界方向：0上,1右,2下,3左
            // 转换为相对于建筑的方向：localDir = (worldDir - rotation + 4) % 4
            int localDir = (worldDir - rotation + 4) % 4;
            if ((inputFacingMask & (1 << localDir)) == 0) return false;
            return findAcceptableInputSlot(item) != -1;
        }

        // 浮动面板
        @Override public void tapped() {
            if (showConfigHandler != null) {
                showConfigHandler.accept(this);
            } else {
                super.tapped();
            }
        }

        // 浮动面板
        @Override public void buildConfiguration(Table table) {
            // 创建内部面板，用于统一设置背景
            Table panel = new Table();
            panel.setBackground(new TextureRegionDrawable(Core.atlas.white()).tint(new Color(0, 0, 0, 0.5f)));

            // 主内容表格（输入、箭头、输出）
            panel.table(main -> {
                // 左侧输入槽位
                Table inTable = new Table();
                main.add(inTable).left();

                // 间隔
                //main.add().width(20);

                // 箭头
                Label arrowLabel = new Label("->");
                arrowLabel.setColor(Pal.accent);
                main.add(arrowLabel).pad(10);

                // 右侧输出槽位
                Table outTable = new Table();
                main.add(outTable).right();

                // 创建动态更新的 GridItemsDisplay（输入）
                int columns = 2;
                GridItemsDisplay inputDisplay = GridItemsDisplay.withMaxColumns(columns, () -> {
                    GridItemsDisplay.Slot[] result = new GridItemsDisplay.Slot[inputSlots.length];
                    for (int i = 0; i < inputSlots.length; i++) {
                        Slot slot = inputSlots[i];
                        Item item = slot.fixedType != null ? slot.fixedType : slot.currentItem;
                        result[i] = new GridItemsDisplay.Slot(item, slot.amount);
                    }
                    return result;
                });

                // 创建动态更新的 GridItemsDisplay（输出）
                GridItemsDisplay outputDisplay = GridItemsDisplay.withMaxColumns(columns, () -> {
                    GridItemsDisplay.Slot[] result = new GridItemsDisplay.Slot[outputSlots.length];
                    for (int i = 0; i < outputSlots.length; i++) {
                        Slot slot = outputSlots[i];
                        Item item = slot.fixedType != null ? slot.fixedType : slot.currentItem;
                        result[i] = new GridItemsDisplay.Slot(item, slot.amount);
                    }
                    return result;
                });

                // 让输入/输出显示组件填充剩余宽度，避免溢出
                inTable.add(inputDisplay).growX().left();
                outTable.add(outputDisplay).growX().right();
            }).pad(10).growX();  // 主表格内边距并水平扩展

            panel.row();

            // 进度条：使用 growX() 使其宽度与面板一致，避免右侧空白
            panel.add(new Bar("bar.progress", Pal.ammo, () -> progress))
                .width(200f)
                .height(18f)
                .pad(4)
                .center();

            // 将面板添加到外部 table 并使其填充整个可用区域
            table.add(panel).grow();
        }

        @Override public boolean shouldConsume() { return currentRecipe != null && canUseRecipe(currentRecipe); }
        @Override public float warmup() { return warmup; }
        @Override public float progress() { return progress; }
        @Override public float totalProgress() { return progress; }

        @Override public byte version() {
            return 1; // 增加版本号以支持 lastOutputIndex
        }

        @Override public void write(Writes write) {
            super.write(write);
            write.f(progress); write.f(warmup); write.i(currentRecipeIndex);
            for (Slot s : inputSlots) write.i(s.amount);
            for (Slot s : outputSlots) write.i(s.amount);
            for (Slot s : inputSlots) {
                if (s.fixedType == null) {
                    write.s(s.currentItem == null ? -1 : s.currentItem.id);
                }
            }
            for (Slot s : outputSlots) {
                if (s.fixedType == null) {
                    write.s(s.currentItem == null ? -1 : s.currentItem.id);
                }
            }
            // 写入 lastOutputIndex
            write.i(lastOutputIndex.length);
            for (int idx : lastOutputIndex) {
                write.i(idx);
            }
        }

        @Override public void read(Reads read, byte revision) {
            super.read(read, revision);
            progress = read.f(); warmup = read.f(); currentRecipeIndex = read.i();
            currentRecipe = (currentRecipeIndex >= 0 && currentRecipeIndex < recipes.length) ? recipes[currentRecipeIndex] : null;
            for (Slot s : inputSlots) s.amount = read.i();
            for (Slot s : outputSlots) s.amount = read.i();
            for (Slot s : inputSlots) {
                if (s.fixedType == null) {
                    int id = read.s();
                    s.currentItem = id == -1 ? null : Vars.content.item(id);
                }
            }
            for (Slot s : outputSlots) {
                if (s.fixedType == null) {
                    int id = read.s();
                    s.currentItem = id == -1 ? null : Vars.content.item(id);
                }
            }
            if (revision >= 1) {
                int len = read.i();
                lastOutputIndex = new int[len];
                for (int i = 0; i < len; i++) {
                    lastOutputIndex[i] = read.i();
                }
            } else {
                // 旧版本存档，初始化为新数组
                lastOutputIndex = new int[outputSlots.length];
            }
        }
    }
}