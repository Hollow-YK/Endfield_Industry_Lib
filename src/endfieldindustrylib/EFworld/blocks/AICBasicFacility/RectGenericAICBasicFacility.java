package endfieldindustrylib.EFworld.blocks.AICBasicFacility;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
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
import mindustry.Vars;
import mindustry.content.Blocks;
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

            if (acceptList.contains(source))
                return findAcceptableInputSlot(item) != -1;

            int dx = 0, dy = 0;
            switch (rotation) {
                case 1:
                    dy = -1;
                    break; // 下
                case 2:
                    dx = 1;
                    break; // 右
                case 3:
                    dy = 1;
                    break; // 上
                case 0:
                    dx = -1;
                    break; // 左
            }

            // 根据旋转计算当前宽度和高度
            boolean rotated = rotation % 2 != 0;
            int w = rotated ? rectHeight : rectWidth;
            int h = rotated ? rectWidth : rectHeight;

            int minX = tileX() - (w % 2 == 0 ? w / 2 -1 : w / 2);
            int maxX = tileX() + (w % 2 == 0 ? w / 2  : w / 2);
            int minY = tileY() - (h % 2 == 0 ? h / 2 -1 : h / 2);
            int maxY = tileY() + (h % 2 == 0 ? h / 2  : h / 2);
            System.out.println("minX: " + minX + ", maxX: " + maxX + ", minY: " + minY + ", maxY: " + maxY);

            // 后方一排的坐标
            int checkX = (dx <= 0) ? minX + dx : maxX + dx;
            int checkY = (dy <= 0) ? minY + dy : maxY + dy;

            if (rotation % 2 == 0) {
                // 偶数旋转：遍历 Y 方向（竖直一排）
                for (int y = minY; y <= maxY; y++) {
                    if (source == Vars.world.tile(checkX, y).build) {
                        acceptList.add(source);
                        return findAcceptableInputSlot(item) != -1;
                    }
                }
            } else {
                // 奇数旋转：遍历 X 方向（水平一排）
                for (int x = minX; x <= maxX; x++) {
                    if (source == Vars.world.tile(x, checkY).build) {
                        acceptList.add(source);
                        return findAcceptableInputSlot(item) != -1;
                    }
                }
            }
            return false;
        }

        @Override
        public void dumpOutputs() {
            if (!timer(timerDump, dumpTime / timeScale))
                return;

            // 确保输出方向掩码非零（若父类未设置，此处强制允许所有方向）
            if (outputFacingMask == 0) {
                outputFacingMask = 0b1111; // 允许左、下、右、上
            }

            // 根据旋转计算实际尺寸和边界（与 createChildren 保持一致）
            boolean rotated = rotation % 2 != 0;
            int w = rotated ? rectHeight : rectWidth;
            int h = rotated ? rectWidth : rectHeight;

            int minX = tileX() - (w % 2 == 0 ? w / 2 - 1 : w / 2);
            int maxX = tileX() + (w % 2 == 0 ? w / 2 : w / 2);
            int minY = tileY() - (h % 2 == 0 ? h / 2 - 1 : h / 2);
            int maxY = tileY() + (h % 2 == 0 ? h / 2 : h / 2);
            System.out.println("dumpOutputs: minX=" + minX + ", maxX=" + maxX + ", minY=" + minY + ", maxY=" + maxY);

            // 输出方向向量
            int dx = 0, dy = 0;
            switch (rotation) {
                case 0: dx = 1; break; // 右
                case 1: dy = 1; break; // 上
                case 2: dx = -1; break; // 左
                case 3: dy = -1; break; // 下
            }

            // 正确的输出排坐标（紧贴建筑外部）
            int checkX = dx >= 0 ? maxX + dx : minX + dx;
            int checkY = dy <= 0 ? minY + dy : maxY + dy;
            System.out.println("dumpOutputs: checkX=" + checkX + ", checkY=" + checkY);

            // 收集输出排上的所有同队建筑
            Seq<Building> outputCandidates = new Seq<>();
            if (rotation % 2 == 0) { // 纵向输出排（列）
                for (int y = minY; y <= maxY; y++) {
                    Tile t = world.tile(checkX, y);
                    if (t != null && t.build != null && t.build.team == team) {
                        outputCandidates.add(t.build);
                    }
                }
            } else { // 横向输出排（行）
                for (int x = minX; x <= maxX; x++) {
                    Tile t = world.tile(x, checkY);
                    if (t != null && t.build != null && t.build.team == team) {
                        outputCandidates.add(t.build);
                    }
                }
            }

            // 调试：打印输出排信息
            System.out.println("dumpOutputs: rotation=" + rotation + ", checkX=" + checkX + ", checkY=" + checkY);
            System.out.println("outputCandidates size=" + outputCandidates.size); // 修正：size 是字段

            if (outputCandidates.isEmpty()) return;

            // 确保轮询索引数组长度正确
            if (lastOutputIndex.length != outputSlots.length) {
                lastOutputIndex = new int[outputSlots.length];
            }

            for (int slotIdx = 0; slotIdx < outputSlots.length; slotIdx++) {
                Slot slot = outputSlots[slotIdx];
                if (slot.amount <= 0 || slot.currentItem == null) continue;
                Item item = slot.fixedType != null ? slot.fixedType : slot.currentItem;

                int n = outputCandidates.size; // 修正：使用字段 size
                int startIdx = lastOutputIndex[slotIdx] % n;

                while (slot.amount > 0) {
                    boolean found = false;
                    for (int i = 0; i < n; i++) {
                        int idx = (startIdx + i) % n;
                        Building other = outputCandidates.get(idx);
                        if (!isAllowedTransport(other)) continue;
                        System.out.println("other: (x=" + other.tileX() + ", y=" + other.tileY() + "),acceptItem=" + other.acceptItem(this, item));

                        if (other.acceptItem(this, item)) {
                            other.handleItem(this, item);
                            slot.remove(1);
                            lastOutputIndex[slotIdx] = (idx + 1) % n;
                            startIdx = lastOutputIndex[slotIdx];
                            found = true;
                            break;
                        }
                    }
                    if (!found) break;
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
