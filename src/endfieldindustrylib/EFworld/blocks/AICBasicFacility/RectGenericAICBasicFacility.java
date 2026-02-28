package endfieldindustrylib.EFworld.blocks.AICBasicFacility;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BuildVisibility;
import mindustry.graphics.Drawf;
import mindustry.game.Team;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

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
            configurable = false;          // 子方块不可单独配置
            buildVisibility = BuildVisibility.hidden; // 隐藏，防止手动放置
            ambientSound = Sounds.none;     // 子方块不发声
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

            public void setMaster(Building master) {
                this.master = master;
            }

            public boolean shouldHide() {
                return true; // 隐藏自身
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
                if (master != null) master.handleItem(source, item);
            }

            @Override
            public boolean acceptLiquid(Building source, Liquid liquid) {
                return master != null && master.acceptLiquid(source, liquid);
            }

            @Override
            public void handleLiquid(Building source, Liquid liquid, float amount) {
                if (master != null) master.handleLiquid(source, liquid, amount);
            }

            @Override
            public float handleDamage(float amount) {
                return master != null ? master.handleDamage(amount) : amount;
            }

            @Override
            public void damage(float damage) {
                if (master != null) master.damage(damage);
            }

            @Override
            public void updateTile() {
                // 子方块不需要独立更新逻辑
            }

            @Override
            public void tapped() {
            }

            @Override
            public void buildConfiguration(Table table) {
                if (master != null) master.buildConfiguration(table);
            }

            // ---------- 保存/加载 ----------
            @Override
            public void write(Writes write) {
                super.write(write);
                write.i(master == null ? -1 : master.pos());
            }

            @Override
            public void read(Reads read, byte revision) {
                super.read(read, revision);
                int pos = read.i();
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

    // -------------------------------------------------------------------------
    // 主方块构建类
    // -------------------------------------------------------------------------
    public class RectBuild extends GenericAICBasicFacilityBuild {
        public Seq<Building> children = new Seq<>(); // 所有子方块

        @Override
        public void created() {
            super.created();
            // 在放置完成后创建所有子方块
            createChildren();
        }

        private int expandX = rotation%2 == 0 ? 0 : Math.abs(rectHeight - rectWidth); // 单侧X轴扩展
        private int expandY = rotation%2 == 0 ? Math.abs(rectHeight - rectWidth) : 0; // 单侧Y轴扩展

        /** 根据主方块的位置和旋转，创建所有子方块 */
        private void createChildren() {
            // 根据旋转计算实际宽高
            int w = rotation % 2 == 0 ? rectWidth : rectHeight;
            int h = rotation % 2 == 0 ? rectHeight : rectWidth;
            int mainSize = Math.min(rectWidth, rectHeight); // 主方块边长 = 短边
            //System.out.println("MainBlock Center: " + tileX() + ", " + tileY() + ", Size: " + mainSize);

            // 计算矩形左下角格坐标（主方块在矩形正中心）
            int centerX = tileX();
            int centerY = tileY();
            int startX = centerX - (w - 1) / 2;
            int startY = centerY - (h - 1) / 2;
            //System.out.println("Rect Start: " + startX + ", " + startY);

            // 遍历整个矩形区域
            for (int dx = 0; dx < w; dx++) {
                for (int dy = 0; dy < h; dy++) {
                    int gx = startX + dx;
                    int gy = startY + dy;
                    // 跳过主方块占据的区域
                    if (gx >= tileX() && gx < tileX() + (mainSize % 2) &&
                        gy >= tileY() && gy < tileY() + (mainSize % 2)) {
                        continue;
                    }
                    Tile childTile = world.tile(gx, gy);
                    if (childTile == null) continue;
                    // 放置子方块
                    childTile.setBlock(rectChildBlock, team, rotation);
                    //System.out.println("Placed child block at: " + gx + ", " + gy);
                    // 确保建筑类型正确，防止存档加载时的类型不匹配
                    if (childTile.build instanceof RectChildBlock.RectChildBuild) {
                        RectChildBlock.RectChildBuild childBuild = (RectChildBlock.RectChildBuild) childTile.build;
                        childBuild.setMaster(this);
                        children.add(childBuild);
                    } else {
                        // 如果类型不正确，可能是旧存档的建筑，强制替换
                        if (childTile.build != null) {
                            childTile.build.kill();
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
            // 移除所有子方块
            for (Building child : children) {
                if (child.isAdded()) child.remove();
            }
            super.onRemoved();
        }

        // 可选：在绘制时添加整个矩形的边框效果
        @Override
        public void drawSelect() {
            super.drawSelect();
            int w = rotation % 2 == 0 ? rectWidth : rectHeight;
            int h = rotation % 2 == 0 ? rectHeight : rectWidth;
            float offX = w * tilesize / 2f;
            float offY = h * tilesize / 2f;
            Drawf.dashRect(Pal.accent, x - offX, y - offY, w * tilesize, h * tilesize);
        }
    }

    // -------------------------------------------------------------------------
    // 覆盖原 Block 方法，实现矩形放置检查与预览
    // -------------------------------------------------------------------------
    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        int w = rotation % 2 == 0 ? rectWidth : rectHeight;
        int h = rotation % 2 == 0 ? rectHeight : rectWidth;
        int offX = -w / 2 + (w % 2 == 0 ? 0 : 1);
        int offY = -h / 2 + (h % 2 == 0 ? 0 : 1);
        for (int dx = 0; dx < w; dx++) {
            for (int dy = 0; dy < h; dy++) {
                Tile other = world.tile(tile.x + offX + dx, tile.y + offY + dy);
                if (other == null || !other.block().replaceable /*|| !other.team().data().canPlace(other.x, other.y, team)*/ ) {
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
        float offX = (w - 1) * tilesize / 2f;
        float offY = (h - 1) * tilesize / 2f;
        Drawf.dashRect(valid ? Pal.accent : Pal.remove, x * tilesize - offX, y * tilesize - offY, w * tilesize, h * tilesize);
    }
}
