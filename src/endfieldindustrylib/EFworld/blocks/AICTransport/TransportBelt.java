package endfieldindustrylib.EFworld.blocks.AICTransport;

import arc.math.Mathf;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Teamc;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.Conveyor;
import mindustry.world.meta.BlockGroup;
import arc.util.io.Reads;
import arc.util.io.Writes;

public class TransportBelt extends Conveyor {
    public TransportBelt(String name) {
        super(name);
        speed = 0.008f;
        health = 1024;
        size = 1;
        itemCapacity = 1;          // 容量为1，但内部数组仍为3，需通过逻辑限制
        noSideBlend = true;         // 禁止侧面输入（视觉上，逻辑由方向控制）
        group = BlockGroup.transportation;
        category = Category.distribution;
        requirements(Category.distribution, ItemStack.with());
    }

    @Override
    public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock) {
        TransportBeltBuild build = tile.build instanceof TransportBeltBuild ? (TransportBeltBuild) tile.build : null;
        if (build == null) return false;
        Tile otherTile = Vars.world.tile(otherx, othery);
        if (otherTile == null || otherTile.build == null) return false;
        Tile facing = Edges.getFacingEdge(otherTile.build.tile, tile);
        if (facing == null) return false;
        int dir = (facing.relativeTo(tile.x, tile.y) + 2) % 4;// facing.relativeTo 返回左0下1右2上3，转换为右0上1左2下3
        if (dir == build.inputDir) return true;
        if (dir == rotation) {
            return lookingAt(tile, rotation, otherx, othery, otherblock);
        }
        return false;
    }

    public class TransportBeltBuild extends ConveyorBuild {
        private static final float itemSpace = 0.4f; // 从父类复制，用于位置计算

        // 允许的输入方向（绝对方向：右0上1左2下3），由放置时自动确定
        public int inputDir;

        @Override
        public void created() {
            super.created();
            // 放置时自动确定输入方向：优先后方，其次右侧，再次左侧，默认后方
            int back = (rotation + 2) % 4; // 后方
            int right = (rotation + 1) % 4; // 右侧
            int left = (rotation + 3) % 4; // 左侧

            Tile backTile = tile.nearby(back);
            if (backTile != null && backTile.build != null) {
                inputDir = back;
            }
            else {
                Tile rightTile = tile.nearby(right);
                if (rightTile != null && rightTile.build != null) {
                    inputDir = right;
                }
                else {
                    Tile leftTile = tile.nearby(left);
                    if (leftTile != null && leftTile.build != null) {
                        inputDir = left;
                    } else {
                        inputDir = back;
                    }
                }
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            if (source == null) {
                return false;
            }

            // 获取接触边
            Tile facing = Edges.getFacingEdge(source.tile, tile);
            if (facing == null) {
                return false;
            }

            // 计算来源绝
            int sourceDir = (facing.relativeTo(tile.x, tile.y) + 2 ) % 4 ;// facing.relativeTo 左0下1右2上3，加2转换为右0上1左2下3
            boolean dirOk = (sourceDir == inputDir);

            if (!dirOk) {
                return false;
            }

            // 容量检查：只能有一个物品
            if (len != 0) {
                return false;
            }

            return true;
        }
        @Override
        public int acceptStack(Item item, int amount, Teamc source) {
            // 检查来源方向（如果是建筑）
            if (source instanceof Building) {
                Building src = (Building) source;
                int sourceDir = tile.relativeTo(src.tile);
                if (sourceDir != inputDir) return 0;
            }
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
            } else { // 从侧面进入
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

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(inputDir);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            inputDir = read.i();
        }
    }
}