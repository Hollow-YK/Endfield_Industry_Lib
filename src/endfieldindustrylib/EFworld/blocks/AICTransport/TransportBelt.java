package endfieldindustrylib.EFworld.blocks.AICTransport;

import mindustry.gen.Building;
import mindustry.gen.Teamc;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.Conveyor;
import mindustry.world.meta.BlockGroup;
import arc.math.Mathf;

public class TransportBelt extends Conveyor {
    public TransportBelt(String name) {
        super(name);
        speed = 0.008f;
        health = 1024;
        size = 1;
        itemCapacity = 1;          // 容量为1，但内部数组仍为3，需通过逻辑限制
        noSideBlend = true;         // 禁止侧面输入
        group = BlockGroup.transportation;
        category = Category.distribution;
        requirements(Category.distribution, ItemStack.with());
    }

    public class TransportBeltBuild extends ConveyorBuild {
        private static final float itemSpace = 0.4f; // 从父类复制，用于位置计算

        @Override
        public boolean acceptItem(Building source, Item item) {
            // 只有当前没有物品时才接受，同时保留父类的基本方向检查
            return len == 0 && super.acceptItem(source, item);
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source) {
            // 最多接受1个物品
            return len == 0 ? Math.min(1, amount) : 0;
        }

        @Override
        public void handleStack(Item item, int amount, Teamc source) {
            // 只处理一个物品
            amount = Math.min(amount, 1);
            if (amount > 0 && len == 0) {
                add(0);                 // 插入到索引0
                xs[0] = 0;
                ys[0] = 0;
                ids[0] = item;
                items.add(item, 1);
                noSleep();
            }
        }

        @Override
        public void handleItem(Building source, Item item) {
            // 确保当前没有物品（由 acceptItem 保证）
            if (len != 0) return;

            int r = rotation;
            Tile facing = Edges.getFacingEdge(source.tile, tile);
            int ang = ((facing.relativeTo(tile.x, tile.y) - r));
            float x = (ang == -1 || ang == 3) ? 1 : (ang == 1 || ang == -3) ? -1 : 0;

            noSleep();
            items.add(item, 1);

            // 根据输入方向决定物品初始横向偏移
            if (Math.abs(facing.relativeTo(tile.x, tile.y) - r) == 0) { // 从后面进入
                add(0);
                xs[0] = x;
                ys[0] = 0;
                ids[0] = item;
            } else { // 从侧面进入（noSideBlend=true时不会发生，但保留逻辑）
                add(0);
                xs[0] = x;
                ys[0] = 0.5f;
                ids[0] = item;
            }
        }

        @Override
        public void updateTile() {
            // 没有物品时进入休眠
            if (len == 0) {
                clogHeat = 0f;
                sleep();
                return;
            }

            float moved = speed * edelta(); // 本次移动距离

            // 计算最大允许前进位置：如果前方是对齐的传送带，需考虑对方已占用的空间
            float nextMax = 1f;
            if (aligned && nextc != null) {
                nextMax = 1f - Math.max(itemSpace - nextc.minitem, 0);
            }

            // 移动物品（索引0为唯一物品）
            ys[0] += moved;
            if (ys[0] > nextMax) ys[0] = nextMax;
            xs[0] = Mathf.approach(xs[0], 0, moved * 2);

            // 物品到达终点，尝试传递给下一个方块
            if (ys[0] >= 1f && pass(ids[0])) {
                items.remove(ids[0], 1);
                remove(0); // 移除物品，len减1
            }

            // 更新 minitem（用于父类的一些判断，但已重写 acceptItem，仅作保持）
            if (len > 0) {
                minitem = ys[0];
            } else {
                minitem = 1f;
            }

            // 计算堵塞热度（物品卡住时视觉效果）
            if (len > 0 && minitem < itemSpace) {
                clogHeat = Mathf.approachDelta(clogHeat, 1f, 1f / 60f);
            } else {
                clogHeat = 0f;
            }

            noSleep();
        }
    }
}