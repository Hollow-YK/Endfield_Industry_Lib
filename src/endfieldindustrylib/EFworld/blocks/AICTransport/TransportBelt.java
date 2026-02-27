package endfieldindustrylib.EFworld.blocks.AICTransport;

import arc.math.Mathf;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.gen.Teamc;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.Conveyor;
import mindustry.world.meta.BlockGroup;
import mindustry.core.UI;
import arc.Core;
import arc.util.Align;
import arc.util.Scaling;
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
        configurable = true;
        group = BlockGroup.transportation;
        category = Category.distribution;
        requirements(Category.distribution, ItemStack.with());
    }

    @Override
    public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock) {
        // 如果没有相邻方块，不混合
        if (otherblock == null) return false;

        // 获取当前方块的建筑实例，以获取 inputDir
        TransportBeltBuild build = tile.build instanceof TransportBeltBuild ? (TransportBeltBuild) tile.build : null;
        if (build == null) return false;

        int inputDir = build.inputDir;
        // 计算相邻方块相对于当前方块的方向
        int dir = tile.relativeTo(otherx, othery);

        // 仅当方向等于输入方向或输出方向时才考虑混合
        if (dir != inputDir && dir != rotation) return false;

        // 对方方块应该是运输组（传送带、路由器等）或者可以输出物品
        // 原版传送带属于 transportation 组
        return otherblock.group == BlockGroup.transportation || otherblock.outputsItems();
    }

    public class TransportBeltBuild extends ConveyorBuild {
        private static final float itemSpace = 0.4f; // 从父类复制，用于位置计算

        // 允许的输入方向（0=右,1=上,2=左,3=下），默认后方
        public int inputDir;

        @Override
        public void created() {
            super.created();
            // 建造时默认输入方向为后方
            inputDir = (rotation + 2) % 4;
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            // 检查来源方向是否与设定的输入方向一致
            if (source == null) return false;
            int sourceDir = tile.relativeTo(source.tile);
            if (sourceDir != inputDir) return false;

            // 只有当前没有物品时才接受
            return len == 0;
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
            } else { // 从侧面进入（可能发生，因为方向允许侧面）
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
        public void buildConfiguration(Table table) {
            // 清除原有配置
            table.clear();

            // 获取三个允许的输入方向（排除输出方向）
            int[] validDirs = new int[]{
                (rotation + 1) % 4,
                (rotation + 2) % 4,
                (rotation + 3) % 4
            };

            for (int dir : validDirs) {
                // 计算箭头旋转角度
                float angle = 0;
                switch (dir) {
                    case 0: angle = 90; break;  // 右
                    case 1: angle = 180; break;   // 上
                    case 2: angle = 270; break; // 左
                    case 3: angle = 0; break; // 下
                }

                // 创建箭头按钮
                ImageButton button = new ImageButton(Core.atlas.find("endfield-industry-lib-arrow"));
                button.getImage().setRotation(angle);
                button.getImage().setScaling(Scaling.fit);
                button.getImage().setOrigin(Align.center);
                button.getImageCell().center();
                button.clicked(() -> {
                    inputDir = dir;
                    deselect(); // 关闭配置菜单
                });

                // 如果当前方向已被选中，高亮按钮
                button.setChecked(inputDir == dir);
                // 设置按钮颜色（可选）
                button.getImage().setColor(Pal.accent);

                // 添加到表格
                table.add(button).size(40).pad(2);
            }
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