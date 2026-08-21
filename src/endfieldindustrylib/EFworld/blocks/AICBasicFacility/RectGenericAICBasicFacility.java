package endfieldindustrylib.EFworld.blocks.AICBasicFacility;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.geom.Point2;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BuildVisibility;

/**
 * 可自定义尺寸的矩形多块工厂。
 * 主方块位于建筑正中间，尺寸与短边一致；其余区域由 1×1 子方块填充。
 * 所有子方块将物品、液体、伤害等操作转发给主方块。
 * 支持旋转，旋转时宽高互换。
 */
public class RectGenericAICBasicFacility extends GenericAICBasicFacility {
    public final int rectWidth, rectHeight; // 原始未旋转时的宽、高（格数）

    /**
     * 构造一个矩形多块工厂。
     * 
     * @param name   方块ID
     * @param width  原始宽度（格）
     * @param height 原始高度（格）
     */
    public RectGenericAICBasicFacility(String name, int width, int height) {
        super(name);
        this.rectWidth = width;
        this.rectHeight = height;
    }

    // -------------------------------------------------------------------------
    // 子方块类型定义
    // -------------------------------------------------------------------------
    public static class RectChildBlock extends Block {
        public RectChildBlock(String name) {
            super(name);
            update = true;
            solid = true;
            configurable = false; // 子方块不可单独配置
            buildVisibility = BuildVisibility.hidden; // 隐藏，防止手动放置
            ambientSound = Sounds.none; // 子方块不发声
            buildType = RectChildBuild::new;
            placeablePlayer = false;
            health = -1;
        }

        @Override
        public void load() {
            super.load();
            // 使用透明纹理，使其不可见
            region = Core.atlas.find("clear");
        }

        public static class RectChildBuild extends Building {
            public Building master; // 指向主方块

            @Override
            public void update() {
                super.update();
                // 主方块不存在或已被移除，销毁自己
                if (master == null) {
                    tile.setBlock(Blocks.air);
                }else if (!master.isAdded()) {
                    tile.setBlock(Blocks.air);
                }else if (Vars.world.tile(master.pos()) != master.tile) {
                    tile.setBlock(Blocks.air);
                }
            }

            public void setMaster(Building master) {
                this.master = master;
            }

            public boolean shouldHide() {
                return true; // 隐藏自身
            }

            @Override
            public void drawSelect() {
                master.drawSelect();
            }

            @Override
            public void draw() {
                // 不绘制任何内容
            }

            // ---------- 转发所有关键方法 ----------
            @Override
            public boolean acceptItem(Building source, Item item) {
                return master != null && master.acceptItem(source, item);
            }

            @Override
            public void handleItem(Building source, Item item) {
                if (master != null)
                    master.handleItem(source, item);
            }

            @Override
            public boolean acceptLiquid(Building source, Liquid liquid) {
                return master != null && master.acceptLiquid(source, liquid);
            }

            @Override
            public void handleLiquid(Building source, Liquid liquid, float amount) {
                if (master != null)
                    master.handleLiquid(source, liquid, amount);
            }

            @Override
            public float handleDamage(float amount) {
                return master != null ? master.handleDamage(amount) : amount;
            }

            @Override
            public void damage(float damage) {
                if (master != null)
                    master.damage(damage);
            }

            @Override
            public void tapped() {
                if (master != null) {
                    if (Vars.control.input.config.getSelected() == master) {
                        Vars.control.input.config.hideConfig();
                    } else {
                        Vars.control.input.config.showConfig(master);
                    }
                }
            }

            @Override
            public void buildConfiguration(Table table) {
                if (master != null)
                    master.buildConfiguration(table);
            }

            // ---------- 保存/加载 ----------
            @Override
            public void write(Writes write) {
                System.out.println("Writing child block. Master pos: " + (master == null ? -1 : master.pos()));
                super.write(write);
                write.i(master == null ? -1 : master.pos());
            }

            @Override
            public void read(Reads read, byte revision) {
                super.read(read, revision);
                int pos = read.i();
                System.out.println("Reading child block. Master pos: " + pos);
                if (pos != -1) {
                    master = world.build(pos);
                }
            }
        }
    }

    public static RectChildBlock rectChildBlock;

    /**
     * 在 Mod 初始化时调用，用于注册子方块。
     * 重命名 load() 为 registerChildBlock 以避免与 Block.load() 冲突
     */
    public static void registerChildBlock() {
        rectChildBlock = new RectChildBlock("rect-child-block");
        rectChildBlock.load();
    }

    /**
     * 生成矩形机器“朝右”（旋转0）的输入偏移（背面/左侧），相对主方块位置。
     * 注意：RectBuild 重写了 acceptItem/dumpOutputs，这些列表当前不被消费，仅为完整性填充。
     */
    public static Point2[] makeInputOffsets(int width, int height) {
        int minX = -(width % 2 == 0 ? width / 2 - 1 : width / 2);
        int minY = -(height % 2 == 0 ? height / 2 - 1 : height / 2);
        int maxY = (height % 2 == 0 ? height / 2 : height / 2);
        int count = maxY - minY + 1;
        Point2[] result = new Point2[count];
        for (int i = 0; i < count; i++) {
            result[i] = new Point2(minX - 1, minY + i);
        }
        return result;
    }

    /** 生成矩形机器“朝右”（旋转0）的输出偏移（正面/右侧），相对主方块位置。 */
    public static Point2[] makeOutputOffsets(int width, int height) {
        int maxX = (width % 2 == 0 ? width / 2 : width / 2);
        int minY = -(height % 2 == 0 ? height / 2 - 1 : height / 2);
        int maxY = (height % 2 == 0 ? height / 2 : height / 2);
        int count = maxY - minY + 1;
        Point2[] result = new Point2[count];
        for (int i = 0; i < count; i++) {
            result[i] = new Point2(maxX + 1, minY + i);
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // 主方块构建类
    // -------------------------------------------------------------------------
    public class RectBuild extends GenericAICBasicFacilityBuild {
        public Seq<Building> children = new Seq<>(); // 所有子方块
        private float childCheckTimer = 0f;
        private static final float CHECK_INTERVAL = 60f; // 每60 tick检查一次，约1秒
        private int[] lastOutputIndex;

        public RectBuild() {
            inputSlots = new Slot[inputSlotDefs.length];
            for (int i = 0; i < inputSlotDefs.length; i++)
                inputSlots[i] = new Slot(inputSlotDefs[i].item);
            outputSlots = new Slot[outputSlotDefs.length];
            for (int i = 0; i < outputSlotDefs.length; i++)
                outputSlots[i] = new Slot(outputSlotDefs[i].item);
            lastOutputIndex = new int[outputSlotDefs.length];
        }
       
        @Override
        public void update() {
            super.update(); // 保持父类逻辑
            childCheckTimer += delta();
            if (childCheckTimer >= CHECK_INTERVAL) {
                childCheckTimer = 0f;
                ensureChildren();
            }
        }

        @Override
        public void placed() {
            super.placed();
            // 在放置完成后创建所有子方块
            createChildren();
        }

        // private int expandX = rotation%2 == 0 ? 0 : Math.abs(rectHeight - rectWidth);
        // // 单侧X轴扩展
        // private int expandY = rotation%2 == 0 ? Math.abs(rectHeight - rectWidth) : 0;
        // // 单侧Y轴扩展

        /** 根据主方块的位置和旋转，创建所有子方块 */
        public void createChildren() {
            // 根据旋转计算实际宽高
            int w = rotation % 2 == 0 ? rectWidth : rectHeight;
            int h = rotation % 2 == 0 ? rectHeight : rectWidth;
            int minX = tileX() - (w % 2 == 0 ? w / 2 -1 : w / 2);
            int maxX = tileX() + (w % 2 == 0 ? w / 2  : w / 2);
            int minY = tileY() - (h % 2 == 0 ? h / 2 -1 : h / 2);
            int maxY = tileY() + (h % 2 == 0 ? h / 2  : h / 2);            // 遍历整个矩形区域
            for(int x= minX;x<=maxX;x++){
                for(int y = minY; y<=maxY;y++){
                    // 跳过主方块占据的区域
                    if (this==world.tile(x,y).build) continue;
                    
                    Tile childTile = world.tile(x,y);
                    if (childTile == null)
                        continue;
                    // 放置子方块
                    childTile.setBlock(rectChildBlock, team, rotation);
                    System.out.println("Placed child block at: " + x + ", " + y);
                    // 确保建筑类型正确，防止存档加载时的类型不匹配
                    if (childTile.build instanceof RectChildBlock.RectChildBuild) {
                        RectChildBlock.RectChildBuild childBuild = (RectChildBlock.RectChildBuild) childTile.build;
                        childBuild.setMaster(this);
                        children.add(childBuild);
                    } else {
                        // 如果类型不正确，可能是旧存档的建筑，强制替换
                        if (childTile.build != null) {

                            childTile.setBlock(Blocks.air);
                        }
                        childTile.setBlock(rectChildBlock, team, rotation);
                        if (childTile.build instanceof RectChildBlock.RectChildBuild) {
                            RectChildBlock.RectChildBuild childBuild = (RectChildBlock.RectChildBuild) childTile.build;
                            childBuild.setMaster(this);
                            children.add(childBuild);
                        } else {
                            // 如果仍然不是正确类型，记录错误但继续
                            // 这不应该发生
                        }
                    }
                }
            }
        }

        @Override
        public void onRemoved() {

            for (Building child : children) {
                if (child.isAdded())
                    child.tile.setBlock(Blocks.air);
            }
            super.onRemoved();
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            if (source == null || !isAllowedTransport(source))
                return false;

            // 如果之前已经接受过该来源，直接允许（可能用于连续传输）
            if (acceptList.contains(source))
                return findAcceptableInputSlot(item) != -1;

            // 来源必须位于 caninputtile 中的某个格子
            for (Point2 pos : caninputtile) {
                Tile t = worldTileFor(pos);
                if (t != null && source == t.build) {
                    acceptList.add(source);
                    return findAcceptableInputSlot(item) != -1;
                }
            }
            return false;
        }

        /**
         * 将“朝右”本地偏移（相对主方块）旋转到当前朝向，并换算为世界格子。
         * 以矩形真实几何中心为旋转中心：偶数尺寸时中心位于半格（tileX+0.5, tileY+0.5），
         * 旋转后再加回主方块坐标，可消除旋转后的半格偏差。
         */
        @Override
        public Tile worldTileFor(Point2 local) {
            int ox = local.x, oy = local.y;
            // 旋转中心相对主方块的偏移（偶数尺寸为 +0.5 格，奇数尺寸为 0）
            float cx = (rectWidth % 2 == 0) ? 0.5f : 0f;
            float cy = (rectHeight % 2 == 0) ? 0.5f : 0f;
            // 偏移相对旋转中心
            float vx = ox - cx, vy = oy - cy;

            float rx, ry;
            switch (rotation) {
                case 1 -> { rx = -vy; ry = vx; }  // 90°：朝下
                case 2 -> { rx = -vx; ry = -vy; } // 180°：朝左
                case 3 -> { rx = vy; ry = -vx; }  // 270°：朝上
                // 默认 0°：朝右
                default -> { rx = vx; ry = vy; }
            }
            // 世界坐标 = 旋转中心 + 旋转后的偏移（偶数尺寸时结果恰为整数格）
            return Vars.world.tile(Math.round(tileX() + cx + rx), Math.round(tileY() + cy + ry));
        }

        @Override
        public void dumpOutputs() {
            if (!timer(timerDump, dumpTime / timeScale))
                return;

            // 确保 lastOutputIndex 长度与当前输出槽一致（防止配置变更）
            if (lastOutputIndex.length != outputSlots.length) {
                lastOutputIndex = new int[outputSlots.length];
            }

            int n = canoutputtile.size();
            if (n == 0)
                return;

            for (int slotIdx = 0; slotIdx < outputSlots.length; slotIdx++) {
                Slot slot = outputSlots[slotIdx];
                if (slot.amount <= 0 || slot.currentItem == null)
                    continue;
                Item item = slot.fixedType != null ? slot.fixedType : slot.currentItem;

                int startIdx = lastOutputIndex[slotIdx] % n; // 从上一次的位置开始

                // 当前 tick 内尽可能输出该槽位的所有物品
                while (slot.amount > 0) {
                    boolean found = false;
                    for (int i = 0; i < n; i++) {
                        int idx = (startIdx + i) % n;
                        Point2 pos = canoutputtile.get(idx);
                        Tile t = worldTileFor(pos);
                        if (t == null)
                            continue;
                        Building other = t.build;
                        if (other == null || other.team != team)
                            continue;

                        if (!isAllowedTransport(other))
                            continue;

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
                    if (!found)
                        break; // 没有建筑可接受，退出循环
                }
            }
        }

        private void ensureChildren() {
            // 根据旋转计算实际占用的宽高
            boolean rotated = rotation % 2 != 0;
            int w = rotated ? rectHeight : rectWidth;
            int h = rotated ? rectWidth : rectHeight;

            // 计算矩形边界（与 createChildren 中一致）
            int minX = tileX() - (w % 2 == 0 ? w / 2 - 1 : w / 2);
            int maxX = tileX() + (w % 2 == 0 ? w / 2 : w / 2);
            int minY = tileY() - (h % 2 == 0 ? h / 2 - 1 : h / 2);
            int maxY = tileY() + (h % 2 == 0 ? h / 2 : h / 2);

            Seq<Building> newChildren = new Seq<>();

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    // 跳过主方块自身
                    if (x == tileX() && y == tileY()) continue;

                    Tile t = world.tile(x, y);
                    if (t == null) continue;

                    Building b = t.build;
                    // 如果当前建筑已经是正确的子方块，直接保留
                    if (b instanceof RectChildBlock.RectChildBuild && ((RectChildBlock.RectChildBuild) b).master == this) {
                        newChildren.add(b);
                    } else {
                        createChildren(); // 重新创建子方块
                        break; // 放置后跳出内层循环，避免重复放置
                    }
                }
            }

            // 更新 children 列表为新的有效列表
            children = newChildren;
        }

        // 可选：在绘制时添加整个矩形的边框效果
        @Override
        public void drawConfigure() {
            int w = rotation % 2 == 0 ? rectWidth : rectHeight;
            int h = rotation % 2 == 0 ? rectHeight : rectWidth;
            float offX = w * tilesize / 2f;
            float offY = h * tilesize / 2f;
            Draw.color(Pal.accent);
            Lines.stroke(1.0F);
            Lines.rect(x - offX, y - offY, w * tilesize, h * tilesize);
            Draw.reset();
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            Draw.color(Pal.accent);
            Lines.stroke(1.0F);
            int w = rotation % 2 == 0 ? rectWidth : rectHeight;
            int h = rotation % 2 == 0 ? rectHeight : rectWidth;
            float offX = w * tilesize / 2f;
            float offY = h * tilesize / 2f;
            Lines.rect(x - offX, y - offY, w * tilesize, h * tilesize);
        }
        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            createChildren(); // 修复子块引用
        }
    }

    // -------------------------------------------------------------------------
    // 覆盖原 Block 方法，实现矩形放置检查与预览
    // -------------------------------------------------------------------------
    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        
        int w = rotation % 2 == 0 ? rectWidth  : rectHeight ;
        int h = rotation % 2 == 0 ? rectHeight  : rectWidth ;
            int minX = tile.x - (w % 2 == 0 ? w / 2 -1 : w / 2);
            int maxX = tile.x + (w % 2 == 0 ? w / 2  : w / 2);
            int minY = tile.y - (h % 2 == 0 ? h / 2 -1 : h / 2);
            int maxY = tile.y + (h % 2 == 0 ? h / 2  : h / 2);
        for (int x =minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                Tile other = world.tile(x, y);
                if (other == null
                        || other.block().solid /* || !other.team().data().canPlace(other.x, other.y, team) */ ) {
                    return false;
                }
            }
        }
        return super.canPlaceOn(tile, team, rotation);

    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        int w = rotation % 2 == 0 ? rectWidth : rectHeight;
        int h = rotation % 2 == 0 ? rectHeight : rectWidth;
        float wx = size%2==0?x * tilesize+4:x*tilesize;
        float wy = size%2==0?y * tilesize+4:x*tilesize;
        float offX = w * tilesize / 2f;
        float offY = h * tilesize / 2f;
        Drawf.dashRect(valid ? Pal.accent : Pal.remove, wx - offX, wy - offY, w * tilesize,
                h * tilesize);
    }
}
