package endfieldindustrylib.EFworld.blocks.AICExtraction;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.EnumSet;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawBlurSpin;
import mindustry.world.draw.DrawDefault;
import mindustry.world.draw.DrawMulti;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
public class VentDrill extends Block {

    /** 该矿机的挖掘等级（1‑3），可挖掘 ≤ mineLevel 的所有喷口 */
    public int mineLevel = 1;
    public float minEfficiency = 0f;
    public float craftTime = 360f; // 6 秒（60 ticks/s × 6）
    public Effect drillEffect = Fx.mine;
    public float effectChance = 0.04f;

    /** 绘制器 — 与 ThermalGenerator 一致的底座 + 旋转部件 */
    public DrawBlock drawer = new DrawMulti(new DrawDefault(), new DrawBlurSpin("-rotator", 0.6f * 9f) {{
        blurThresh = 0.01f;
    }});

    // ======================== 矿物喷口注册表 ========================

    /** 描述一种可被挖掘的喷口类型 */
    public static class VentType {
        public final Attribute attribute;
        public final Item item;
        public final int tier;

        public VentType(Attribute attribute, Item item, int tier) {
            this.attribute = attribute;
            this.item = item;
            this.tier = tier;
        }
    }

    /** 全局矿物喷口注册表 — 在 EFblocks.load() 中填充 */
    public static final Seq<VentType> ventTypes = new Seq<>();

    /** 注册一种矿物喷口类型 */
    public static void registerVentType(Attribute attr, Item item, int tier) {
        ventTypes.add(new VentType(attr, item, tier));
    }

    /** 根据 attribute 查找对应的 VentType */
    public static @Nullable VentType findByAttribute(Attribute attr) {
        for (var vt : ventTypes) {
            if (vt.attribute == attr) return vt;
        }
        return null;
    }

    // ============================================================

    public VentDrill(String name) {
        super(name);
        update = true;
        solid = true;
        hasItems = true;
        hasPower = false;
        hasLiquids = false;
        sync = true;
        itemCapacity = 10;
        ambientSound = Sounds.loopDrill;
        ambientSoundVolume = 0.04f;
        flags = EnumSet.of(BlockFlag.factory);
        drawArrow = true;
        noUpdateDisabled = true;
    }

    @Override
    public void load() {
        super.load();
        drawer.load(this);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.timePeriod = craftTime;
        stats.add(Stat.productionTime, craftTime / 60f, StatUnit.seconds);

        // 统计该矿机可挖掘的所有矿物
        StringBuilder sb = new StringBuilder();
        for (var vt : ventTypes) {
            if (vt.tier <= mineLevel) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(vt.item.emoji()).append(" ").append(vt.item.localizedName);
            }
        }
        if (sb.length() > 0) {
            stats.add(Stat.output, sb.toString());
        }

        stats.add(Stat.tiles, Core.bundle.format("bar.minelevel", mineLevel));
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        drawer.drawPlan(this, plan, list);
    }

    @Override
    public TextureRegion[] icons() {
        return drawer.finalIcons(this);
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out) {
        drawer.getRegionsToOutline(this, out);
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        for (var vt : ventTypes) {
            if (vt.tier <= mineLevel) {
                float sum = tile.getLinkedTilesAs(this, tempTiles).sumf(other -> other.floor().attributes.get(vt.attribute));
                if (sum > minEfficiency) return true;
            }
        }
        return false;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);

        // 显示当前可挖掘的最高等级
        int highest = 0;
        for (var vt : ventTypes) {
            if (vt.tier <= mineLevel) {
                float s = sumAttribute(vt.attribute, x, y);
                if (s > minEfficiency && vt.tier > highest) highest = vt.tier;
            }
        }
        if (highest > 0) {
            drawPlaceText(Core.bundle.format("bar.minelevel", highest), x, y, valid);
        }
    }

    // ======================== Building ========================

    public class VentDrillBuild extends Building {
        public float progress;
        public float totalProgress;
        public float warmup;

        /** 当前选中的喷口类型（缓存，由 recalcVent 刷新） */
        public @Nullable VentType currentVent;

        @Override
        public void draw() {
            drawer.draw(this);
        }

        @Override
        public void drawLight() {
            super.drawLight();
            drawer.drawLight(this);
        }

        @Override
        public void onProximityAdded() {
            super.onProximityAdded();
            recalcVent();
        }

        /** 扫描脚下所有喷口，选择 tier 最高且 ≤ mineLevel 的喷口 */
        public void recalcVent() {
            currentVent = null;
            int bestTier = -1;
            for (var vt : ventTypes) {
                if (vt.tier <= mineLevel && vt.tier > bestTier) {
                    float sum = sumAttribute(vt.attribute, tile.x, tile.y);
                    if (sum > minEfficiency) {
                        bestTier = vt.tier;
                        currentVent = vt;
                    }
                }
            }
        }

        @Override
        public void updateTile() {
            // 如果没有选中喷口，尝试重新扫描（可能是地图加载后重建）
            if (currentVent == null) {
                recalcVent();
            }

            if (currentVent == null) {
                warmup = Mathf.lerpDelta(warmup, 0f, 0.02f);
                return;
            }

            float sum = sumAttribute(currentVent.attribute, tile.x, tile.y);
            float eff = sum + currentVent.attribute.env();

            if (eff > 0.01f && items.get(currentVent.item) < itemCapacity) {
                progress += eff * getProgressIncrease(craftTime);
                totalProgress += eff * getProgressIncrease(craftTime);
                warmup = Mathf.lerpDelta(warmup, 1f, 0.02f);

                if (Mathf.chanceDelta(effectChance)) {
                    drillEffect.at(x + Mathf.range(4f), y + Mathf.range(4f));
                }

                if (progress >= 1f) {
                    progress = 0f;
                    items.add(currentVent.item, 1);
                }
            } else {
                warmup = Mathf.lerpDelta(warmup, 0f, 0.02f);
            }

            dump(currentVent.item);
        }

        @Override
        public boolean shouldConsume() {
            return currentVent != null && items.get(currentVent.item) < itemCapacity;
        }

        @Override
        public float totalProgress() {
            return totalProgress;
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(progress);
            write.f(warmup);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            progress = read.f();
            warmup = read.f();
            // currentVent 将在下一帧的 updateTile 中通过 recalcVent() 恢复
        }
    }
}
