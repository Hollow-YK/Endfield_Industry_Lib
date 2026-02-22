package endfieldindustrylib.AICBasicFacility;

import arc.Core;
import arc.math.*;
import arc.math.geom.Geometry;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.Log;
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

    @Override public void setStats() {
        super.setStats();
        if (recipes.length > 0) stats.add(Stat.output, StatValues.items(recipes[0].output));
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

        public GenericAICBasicFacilityBuild() {
            inputSlots = new Slot[inputSlotDefs.length];
            for (int i = 0; i < inputSlotDefs.length; i++) inputSlots[i] = new Slot(inputSlotDefs[i].item);
            outputSlots = new Slot[outputSlotDefs.length];
            for (int i = 0; i < outputSlotDefs.length; i++) outputSlots[i] = new Slot(outputSlotDefs[i].item);
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
            Log.info("other class: " + other.getClass().getName());
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

public void dumpOutputs() {
    if (!timer(timerDump, dumpTime / timeScale)) return;
    Log.info("dumpOutputs start, progress=" + progress);
    
    int size = block.size;
    int offset = size / 2; // 中心到边界的距离
    int centerX = tileX();   // 中心格 X
    int centerY = tileY();   // 中心格 Y
    int minX = centerX - offset;
    int maxX = centerX + offset;
    int minY = centerY - offset;
    int maxY = centerY + offset;
    
    // 旋转映射：rotation=0(右) -> 世界方向1(东), 1(上) -> 0(北), 2(左) -> 3(西), 3(下) -> 2(南)
    int[] rotToWorld = {1, 0, 3, 2};
    
    for (Slot slot : outputSlots) {
        if (slot.amount <= 0 || slot.currentItem == null) continue;
        Item item = slot.fixedType != null ? slot.fixedType : slot.currentItem;
        Log.info(" attempting to output item: " + item + " amount=" + slot.amount);
        
        for (int dir = 0; dir < 4; dir++) {
            if ((outputFacingMask & (1 << dir)) == 0) continue;
            
            int faceWorld = rotToWorld[rotation];
            int worldDir = (faceWorld + dir) % 4;
            
            // 根据世界方向确定要检查的外部格子范围
            int checkStartX, checkStartY, checkEndX, checkEndY, stepX, stepY;
            if (worldDir == 0) { // 北
                checkStartX = minX; checkEndX = maxX; stepX = 1;
                checkStartY = maxY + 1; checkEndY = maxY + 1; stepY = 0;
            } else if (worldDir == 1) { // 东
                checkStartX = maxX + 1; checkEndX = maxX + 1; stepX = 0;
                checkStartY = minY; checkEndY = maxY; stepY = 1;
            } else if (worldDir == 2) { // 南
                checkStartX = minX; checkEndX = maxX; stepX = 1;
                checkStartY = minY - 1; checkEndY = minY - 1; stepY = 0;
            } else { // 西
                checkStartX = minX - 1; checkEndX = minX - 1; stepX = 0;
                checkStartY = minY; checkEndY = maxY; stepY = 1;
            }
            
            for (int x = checkStartX; x <= checkEndX; x += stepX) {
                for (int y = checkStartY; y <= checkEndY; y += stepY) {
                    Log.info("    checking target (" + x + "," + y + ")");
                    Tile targetTile = Vars.world.tile(x, y);
                    if (targetTile == null) {
                        Log.info("        tile out of bounds");
                        continue;
                    }
                    Building other = targetTile.build;
                    if (other == null) {
                        Log.info("        no building");
                        continue;
                    }
                    Log.info("        other tile: (" + other.tileX() + "," + other.tileY() + "), block=" + other.block);
                    if (!isAllowedTransport(other)) {
                        Log.info("        other not allowed (temporarily allowing all)");
                        continue; // 恢复限制后应跳过不允许的方块
                    }
                    boolean canAccept = other.acceptItem(this, item);
                    Log.info("        acceptItem=" + canAccept);
                    if (canAccept) {
                        other.handleItem(this, item);
                        slot.remove(1);
                        Log.info("    transferred 1 " + item + " to " + other + " at (" + x + "," + y + ")");
                        return; // 每次只输出一个物品
                    }
                }
            }
        }
    }
    Log.info("dumpOutputs end");
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

        @Override public void buildConfiguration(Table table) {
            table.table(main -> {
                main.table(inTable -> {
                    GridItemsDisplay.Slot[] inSlots = new GridItemsDisplay.Slot[inputSlots.length];
                    for (int i = 0; i < inputSlots.length; i++) {
                        Item displayItem = inputSlots[i].fixedType != null ? inputSlots[i].fixedType : inputSlots[i].currentItem;
                        inSlots[i] = new GridItemsDisplay.Slot(displayItem, inputSlots[i].amount);
                    }
                    GridItemsDisplay inputDisplay = new GridItemsDisplay(2);
                    inputDisplay.rebuild(inSlots);
                    inTable.add(inputDisplay).left();
                }).left();
                main.add().width(20);
                main.image(Core.atlas.white()).color(Pal.accent).size(20, 4).pad(10);
                main.table(outTable -> {
                    GridItemsDisplay.Slot[] outSlots = new GridItemsDisplay.Slot[outputSlots.length];
                    for (int i = 0; i < outputSlots.length; i++) {
                        Item displayItem = outputSlots[i].fixedType != null ? outputSlots[i].fixedType : outputSlots[i].currentItem;
                        outSlots[i] = new GridItemsDisplay.Slot(displayItem, outputSlots[i].amount);
                    }
                    GridItemsDisplay outputDisplay = new GridItemsDisplay(2);
                    outputDisplay.rebuild(outSlots);
                    outTable.add(outputDisplay).right();
                }).right();
            }).pad(10);
            table.row();
            table.add(new Bar("bar.progress", Pal.ammo, () -> progress)).size(200f, 18f).pad(4);
        }

        @Override public boolean shouldConsume() { return currentRecipe != null && canUseRecipe(currentRecipe); }
        @Override public float warmup() { return warmup; }
        @Override public float progress() { return progress; }
        @Override public float totalProgress() { return progress; }

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
        }
    }
}