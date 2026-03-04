package endfieldindustrylib.EFworld.blocks.AICPower;

import static mindustry.Vars.tilesize;
import java.util.ArrayList;
import java.util.List;
import arc.util.Time;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.scene.ui.layout.Table;
import arc.scene.ui.Label;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.world.blocks.power.PowerNode;
import endfieldindustrylib.EFworld.blocks.AICBasicFacility.RectGenericAICBasicFacility;
import endfieldindustrylib.ui.Powerline;
import mindustry.core.Renderer;

public class EFPowerNode extends PowerNode {

    public int squarelaserRange = 0;

    public EFPowerNode(String name) {
        super(name);
        autolink = false;
        drawRange = false;
        maxNodes = 99;
    }

    // 判断是否为电力建筑
    protected boolean isSpecial(Building building) {
        if (building == null)
            return false;
        return building.block instanceof ThermalBank ||
                building.block instanceof RelayTower ||
                building.block instanceof ElectricPylon;
    }
    @Override
    public void drawPlace(int tx, int ty, int rotation, boolean valid) {
        float centerX = (tx + size/2f) * tilesize; // 或 tx * tilesize + size * 4
        float centerY = (ty + size/2f) * tilesize;
        float width = (squarelaserRange * 2 + 1) * tilesize;
        Draw.color(185f, 205f, 0f, 1f);
        Lines.rect(centerX - squarelaserRange * tilesize - 4, centerY - squarelaserRange * tilesize - 4, width, width);
        Draw.color(185f, 205f, 0f, 0.25f);
        Fill.rect(centerX, centerY, width, width);
    // ... 后续绘制建筑
        List<Building> drawlist = new ArrayList<>();
        for (int x = (int) Math.floor(tx - this.squarelaserRange); x <= Math.ceil(tx + this.squarelaserRange); x++) {
            for (int y = (int) Math.floor(ty - this.squarelaserRange); y <= Math.ceil(ty + this.squarelaserRange); y++) {
                if (Vars.world.tile(x, y).build != null) {
                    Building canlink = Vars.world.tile(x, y).build;
                    if (!isSpecial(canlink) && !drawlist.contains(canlink)) {
                        if (canlink.block instanceof RectGenericAICBasicFacility) {
                            // 什么都不做
                        } else {
                            Draw.color(185f, 205f, 0f, 0.3f);
                            Fill.rect(canlink.x(), canlink.y(), canlink.block.size * tilesize, canlink.block.size * tilesize);
                        }
                        drawlist.add(canlink);
                    }
                }
            }
        }
        super.drawPlace(tx, ty, rotation, valid);
    }
    @Override
    public boolean linkValid(Building tile, Building link, boolean checkMaxNodes) {
        if (tile == link || link == null || !link.block.hasPower || !link.block.connectedPower || tile.team != link.team
                || (sameBlockConnection && tile.block != link.block))
            return false;

        if (isSpecial(link)) {
            // 电力建筑：通过中心距离检查（手动连接)
            Powerline canpowerline = new Powerline();
            int a = canpowerline.canpowerline(tile, link);
            if (a != -1 && a <= 60) {
                if (checkMaxNodes && link.block instanceof PowerNode node) {
                    return link.power.links.size < node.maxNodes || link.power.links.contains(tile.pos());
                }
                return true;
            }
            return false;
        } else {
            // 普通建筑：必须位于方形范围内
            if (squareOverlaps(tile, link, squarelaserRange)) {
                if (checkMaxNodes && link.block instanceof PowerNode node) {
                    return link.power.links.size < node.maxNodes || link.power.links.contains(tile.pos());
                }
                return true;
            }
            return false;
        }
    }

    // 检查两个建筑是否重叠（以src为中心，边长为squareSize格的正方形与other的碰撞箱重叠）
    protected boolean squareOverlaps(Building src, Building other, int range) {
        for (int x = (int) Math.floor(src.tileX() - range); x <= Math.ceil(src.tileX() + range); x++) {
            for (int y = (int) Math.floor(src.tileY() - range); y <= Math.ceil(src.tileY() + range); y++) {
                if (Vars.world.tile(x, y).build == other) {
                    return true;
                }
            }
        }
        return false;
    }

    public class EFPowerNodeBuild extends PowerNodeBuild {

        @Override
        public void placed() {
            Seq<Point2> targets = new Seq<>();
            for (int x = (int) Math.floor(this.tileX() - squarelaserRange); x <= Math
                    .ceil(this.tileX() + squarelaserRange); x++) {
                for (int y = (int) Math.floor(this.tileY() - squarelaserRange); y <= Math
                        .ceil(this.tileY() + squarelaserRange); y++) {
                    if (Vars.world.tile(x, y).build != null) {
                        Point2 xy = new Point2(Vars.world.tile(x, y).build.tileX() - this.tileX(), Vars.world.tile(x, y).build.tileY() - this.tileY());
                        if (!isSpecial(Vars.world.tile(x, y).build) && Vars.world.tile(x, y).build.block.hasPower
                                && !targets.contains(xy)) {
                            targets.add(new Point2(xy));
                        }
                    }
                }
            }
            if (targets.size > 0) {
                // 转换为 Point2[] 并调用配置
                Point2[] array = targets.toArray(Point2.class);
                configure(array);
            }
        }
        private Label label= new Label("");
        private int lastDistance = -1;
        Powerline powerline = new Powerline();
        @Override
        public void buildConfiguration(Table table) {
            powerline.showText(label);
        }
        @Override
        public void onConfigureClosed() {
            powerline.removeText();
        }
        private void updateLabelText() {
            if (label == null) return;

            if (lastDistance != -1) {
                label.setText(lastDistance + "m");
            } else {
                label.setText("距离过长");
            }
        }

        @Override
        public void drawConfigure() {
            // 计算距离并保存，供 update 使用
            
            lastDistance = powerline.drawpowerline(this); // 此方法既绘制又返回距离，因此先绘制并获取返回值
            powerline.fallowMouse();
            updateLabelText();
            // 绘制圆形高亮
            Drawf.circles(x, y, tile.block().size * tilesize / 2f + 1f + Mathf.absin(Time.time, 4f, 1f));
            // 绘制选择范围
            drawSelect();
        }
        @Override
        public void drawSelect() {
            // 原有 drawSelect 逻辑不变
            super.drawSelect();
            float width = (squarelaserRange * 2 + 1) * tilesize;
            Draw.color(185f, 205f, 0f, 1f);
            Lines.rect(this.x() - squarelaserRange * tilesize - 4, this.y() - squarelaserRange * tilesize - 4, width, width);
            Draw.color(185f, 205f, 0f, 0.25f);
            Fill.rect(this.x(), this.y(), width, width);

            List<Building> drawlist = new ArrayList<>();
            for (int x = (int) Math.floor(this.tileX() - squarelaserRange); x <= Math.ceil(this.tileX() + squarelaserRange); x++) {
                for (int y = (int) Math.floor(this.tileY() - squarelaserRange); y <= Math.ceil(this.tileY() + squarelaserRange); y++) {
                    if (Vars.world.tile(x, y).build != null) {
                        Building canlink = Vars.world.tile(x, y).build;
                        if (!isSpecial(canlink) && !drawlist.contains(canlink)) {
                            for (int i = 0; i < power.links.size; i++) {
                                if (canlink == Vars.world.build(power.links.get(i))) {
                                    if (canlink.block instanceof RectGenericAICBasicFacility) {
                                        // 什么都不做
                                    } else {
                                        Draw.color(185f, 205f, 0f, 0.5f);
                                        Fill.rect(canlink.x(), canlink.y(), canlink.block.size * tilesize, canlink.block.size * tilesize);
                                    }
                                    drawlist.add(canlink);
                                }
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void draw() {
            super.draw();

            if (Mathf.zero(Renderer.laserOpacity) || isPayload() || team == Team.derelict) return;

            Draw.z(powerLayer);
            setupColor(power.graph.getSatisfaction());

            for (int i = 0; i < power.links.size; i++) {
                Building link = Vars.world.build(power.links.get(i));
                // 如果链接无效，可以跳过，但原代码注释掉了判断
                // if(!linkValid(this, link)) continue;

                if (link.block instanceof PowerNode && link.id >= id) continue;

                drawLaser(x, y, link.x, link.y, size, link.block.size);
            }

            Draw.reset();
        }

        protected boolean linked(Building other) {
            return power.links.contains(other.pos());
        }

        @Override
        public Point2[] config() {
            Point2[] out = new Point2[power.links.size];
            for (int i = 0; i < out.length; i++) {
                out[i] = Point2.unpack(power.links.get(i)).sub(tile.x, tile.y);
            }
            return out;
        }

        /*
         * @Override
         * 
         * super.onProximityUpdate();
         * updateConnections();
         * }
         * 
         * private void updateConnections() {
         * if (squarelaserRange <= 0) return;
         * 
         * float worldHalf = squarelaserRange * tilesize / 2f;
         * Seq<Building> buildingsInRect = EFBuildings.getBuildingsInRect(
         * (int)(x - worldHalf), (int)(y - worldHalf),
         * (int)(x + worldHalf), (int)(y + worldHalf)
         * );
         * 
         * // 收集当前仍然有效的连接（使用普通 for 循环遍历 power.links）
         * 
         * // 收集正方形区域内有效的新目标（去重），排除特殊建筑
         * Seq<Point2> newTargets = new Seq<>();
         * for (Building other : buildingsInRect) {
         * if (other == this) continue;
         * // 排除电力建筑
         * if (EFPowerNode.this.isSpecial(other)) continue;
         * if (EFPowerNode.this.linkValid(this, other, false)) {
         * int dx = other.tileX() - tileX();
         * int dy = other.tileY() - tileY();
         * Point2 p = new Point2(dx, dy);
         * if (!currentLinks.contains(p)) {
         * newTargets.add(p);
         * }
         * }
         * }
         * 
         * // 合并现有有效连接与新目标
         * Seq<Point2> allTargets = new Seq<>();
         * allTargets.addAll(currentLinks);
         * allTargets.addAll(newTargets);
         * 
         * configure(allTargets.toArray(Point2.class));
         * }
         */
    }
}