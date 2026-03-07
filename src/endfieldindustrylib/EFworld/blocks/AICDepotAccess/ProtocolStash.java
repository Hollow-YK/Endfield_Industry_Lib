package endfieldindustrylib.EFworld.blocks.AICDepotAccess;

import arc.Core;
import arc.graphics.Color;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Category;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import endfieldindustrylib.EFcontents.EFitems;
import endfieldindustrylib.EFworld.blocks.AICTransport.*;
import endfieldindustrylib.ui.GridItemsDisplay;

/**
 * 协议储存箱 (Protocol Stash)
 * 6个通用槽位，仅后方输入、前方输出，仅与模组传送带交互。
 * 提供“仓储模式”切换：仓储模式下物品仅储存；非仓储模式下每6秒向核心传输物品（核心满则保留）。
 */
public class ProtocolStash extends Block {
    float powerUsage = 0.5f;

    public ProtocolStash(String name) {
        super(name);
        // 基础属性
        solid = true;
        update = true;
        hasItems = true;
        configurable = true;
        rotate = true;
        size = 3;
        itemCapacity = 6 * 50; // 总容量 300，但实际由槽位管理
        group = BlockGroup.transportation;
        requirements(Category.distribution, ItemStack.with(EFitems.origocrust, 20));
    }

    @Override
    public void init() {
        consumePower(powerUsage);
        super.init();
    }

    public class ProtocolStashBuild extends Building {
        // 槽位定义
        public class Slot {
            public Item currentItem; // 当前存放的物品类型
            public int amount;
            public final int maxAmount = 50;

            public boolean accepts(Item item) {
                // 通用槽位：为空可接受任何物品；非空只能接受相同物品
                return (amount == 0 || currentItem == item) && amount < maxAmount;
            }

            public int add(Item item, int count) {
                int added = Math.min(count, maxAmount - amount);
                if (added > 0) {
                    if (amount == 0)
                        currentItem = item;
                    amount += added;
                }
                return added;
            }

            public int remove(int count) {
                int removed = Math.min(count, amount);
                amount -= removed;
                if (amount == 0)
                    currentItem = null;
                return removed;
            }

            public boolean has(Item item, int count) {
                return currentItem == item && amount >= count;
            }
        }

        public Slot[] slots = new Slot[6];
        public boolean storageMode = true; // true = 仓储模式（只储存），false = 非仓储模式（向核心传输）
        private float coreTimer = 0f; // 向核心传输计时器
        private static final float CORE_TRANSMIT_TIME = 360f; // 6秒 (60 ticks * 6)

        // 轮询输出索引（用于公平输出）
        private int lastOutputIndex = 0;

        public ProtocolStashBuild() {
            for (int i = 0; i < slots.length; i++) {
                slots[i] = new Slot();
            }
        }

        /** 检查相邻建筑是否为允许交互的自定义物流方块 */
        public boolean isAllowedTransport(Building other) {
            return other instanceof TransportBelt.TransportBeltBuild ||
                    other instanceof ItemControlPort.ItemControlPortBuild ||
                    other instanceof Splitter.SplitterBuild ||
                    other instanceof BeltBridge.BeltBridgeBuild ||
                    other instanceof Converger.ConvergerBuild;
        }

        @Override
        public void updateTile() {
            // 1. 向核心传输（非仓储模式）
            if (!storageMode) {
                coreTimer += edelta();
                while (coreTimer >= CORE_TRANSMIT_TIME) {
                    coreTimer -= CORE_TRANSMIT_TIME;
                    transmitToCore();
                }
            }

            // 2. 向输出方向输出物品（始终进行）
            dumpOutputs();
        }

        /** 向核心传输所有可能物品 */
        private void transmitToCore() {
            var core = team.core();
            if (core == null || !core.isValid())
                return;

            for (Slot slot : slots) {
                if (slot.amount <= 0 || slot.currentItem == null)
                    continue;
                Item item = slot.currentItem;
                int maxTransfer = slot.amount;
                int coreFree = core.block.itemCapacity - core.items.get(item);
                if (coreFree <= 0)
                    continue; // 核心无空间，跳过

                int transfer = Math.min(maxTransfer, coreFree);
                if (transfer > 0) {
                    // 向核心添加物品
                    core.items.add(item, transfer);
                    slot.remove(transfer);
                }
            }
        }

        /** 向输出面（前方）输出物品 */
        public void dumpOutputs() {
            // 收集前方一排的建筑（最多3个）
            Building[] frontBuildings = new Building[3];
            int frontCount = 0;

            if (rotation % 2 == 0) { // 水平方向：左(0)或右(2)
                int baseX = tileX() + 2 * (1 - rotation);
                for (int i = -1; i <= 1; i++) {
                    int y = tileY() + i;
                    Building b = Vars.world.tile(baseX, y) == null ? null : Vars.world.tile(baseX, y).build;
                    if (b != null && b.team == team && isAllowedTransport(b)) {
                        frontBuildings[frontCount++] = b;
                    }
                }
            } else { // 垂直方向：下(1)或上(3)
                int baseY = tileY() + 2 * (2 - rotation);
                for (int i = -1; i <= 1; i++) {
                    int x = tileX() + i;
                    Building b = Vars.world.tile(x, baseY) == null ? null : Vars.world.tile(x, baseY).build;
                    if (b != null && b.team == team && isAllowedTransport(b)) {
                        frontBuildings[frontCount++] = b;
                    }
                }
            }

            if (frontCount == 0)
                return;

            // 找到第一个非空槽位
            Slot targetSlot = null;
            Item targetItem = null;
            for (int slotIdx = 0; slotIdx < slots.length; slotIdx++) {
                Slot slot = slots[slotIdx];
                if (slot.amount > 0 && slot.currentItem != null) {
                    targetSlot = slot;
                    targetItem = slot.currentItem;
                    break;
                }
            }
            if (targetSlot == null)
                return; // 无物品可输出

            // 对该槽位的物品进行轮询输出（lastOutputIndex 作为轮询指针）
            int startIdx = lastOutputIndex % frontCount;
            while (targetSlot.amount > 0) {
                boolean found = false;
                for (int i = 0; i < frontCount; i++) {
                    int idx = (startIdx + i) % frontCount;
                    Building other = frontBuildings[idx];
                    if (other.acceptItem(this, targetItem)) {
                        other.handleItem(this, targetItem);
                        targetSlot.remove(1);
                        lastOutputIndex = (idx + 1) % frontCount;
                        startIdx = lastOutputIndex;
                        found = true;
                        break;
                    }
                }
                if (!found)
                    break;
            }
        }

        /** 寻找可接受指定物品的输入槽位（用于输入） */
        public int findAcceptableInputSlot(Item item) {
            // 1. 优先找已有同种物品且未满的槽位
            for (int i = 0; i < slots.length; i++) {
                Slot s = slots[i];
                if (s.accepts(item) && s.amount > 0 && s.currentItem == item) {
                    return i; // 存在已有同种物品的空位，直接使用
                }
            }
            // 2. 如果没有，找空槽位（amount == 0）
            for (int i = 0; i < slots.length; i++) {
                Slot s = slots[i];
                if (s.accepts(item) && s.amount == 0) {
                    return i; // 使用第一个空槽位
                }
            }
            // 3. 没有合适的槽位
            return -1;
        }

        @Override
        public void handleItem(Building source, Item item) {
            int idx = findAcceptableInputSlot(item);
            if (idx != -1) {
                slots[idx].add(item, 1);
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            if (source == null || !isAllowedTransport(source))
                return false;

            // 检查方向：必须来自建筑后方
            int worldDir = relativeTo(source);
            int inputDir = (rotation + 2) % 4; // 后方
            if (worldDir != inputDir)
                return false;

            return findAcceptableInputSlot(item) != -1;
        }

        // ========== UI 配置面板 ==========
        @Override
        public void buildConfiguration(Table table) {
            Table panel = new Table();
            panel.setBackground(new TextureRegionDrawable(Core.atlas.white()).tint(new Color(0, 0, 0, 0.5f)));

            // 槽位显示
            int columns = 3; // 6个槽位，2行3列
            GridItemsDisplay slotDisplay = GridItemsDisplay.withFixedColumns(columns, () -> {
                GridItemsDisplay.Slot[] result = new GridItemsDisplay.Slot[slots.length];
                for (int i = 0; i < slots.length; i++) {
                    Slot s = slots[i];
                    result[i] = new GridItemsDisplay.Slot(s.currentItem, s.amount);
                }
                return result;
            });
            panel.add(slotDisplay).pad(10).growX().center();

            panel.row();

            // 仓储模式切换按钮
            TextButton modeButton = new TextButton(storageMode ? "仓储模式" : "无线传输模式", Styles.defaultt);
            modeButton.clicked(() -> {
                storageMode = !storageMode;
                modeButton.setText(storageMode ? "仓储模式" : "无线传输模式");
            });
            panel.add(modeButton).width(150).height(40).pad(4).center();

            table.add(panel).grow();
        }

        // ========== 数据持久化 ==========
        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(storageMode);
            write.f(coreTimer);
            // 写入槽位数量
            write.i(slots.length);
            for (Slot s : slots) {
                write.i(s.amount);
                write.s(s.currentItem == null ? -1 : s.currentItem.id);
            }
            write.i(lastOutputIndex);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            storageMode = read.bool();
            coreTimer = read.f();
            int len = read.i();
            if (len == slots.length) {
                for (int i = 0; i < len; i++) {
                    slots[i].amount = read.i();
                    int id = read.s();
                    slots[i].currentItem = id == -1 ? null : Vars.content.item(id);
                }
            } else {
                // 存档版本不匹配，跳过
                for (int i = 0; i < len; i++) {
                    read.i(); // 跳过 amount
                    read.s(); // 跳过 id
                }
            }
            lastOutputIndex = read.i();
        }

        @Override
        public byte version() {
            return 1;
        }
    }
}