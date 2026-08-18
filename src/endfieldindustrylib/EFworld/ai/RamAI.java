package endfieldindustrylib.EFworld.ai;

import arc.graphics.Color;
import arc.math.geom.Position;
import arc.util.Time;
import static mindustry.Vars.headless;
import static mindustry.Vars.tilesize;
import mindustry.ai.types.GroundAI;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.world.blocks.environment.Floor;

/**
 * 拉姆（Ram）专属 AI — 基于 GroundAI 完全重写。
 * <p>
 * 行为：22.5 格（{@link #detectRange}）内按仇恨优先级索敌——
 * 单位 > 炮台 > 建筑 > 墙体（普通建筑排除低矮/传送带类方块），
 * 发现目标则转为攻击模式（面向目标并贴近到前颚挥击命中距离，贴身由武器自动挥击），
 * 否则交由 {@link GroundAI} 标准逻辑朝核心寻路奔跑。
 * 所有移动均走标准 AI 移动链路（{@code pathfind}/{@code moveTo} → {@code movePref} → {@code rotateMove}），
 * 因此继承 GroundAI 的寻路绕障、卡住检测与面向逻辑，不再自行驱动 {@code vel}。
 */
public class RamAI extends GroundAI {
    /** 索敌半径（世界单位）：22.5 格（原 15 格 × 150%） */
    private static final float detectRange = 15f * tilesize * 1.5f;
    /** 前颚挥击命中距离基数（世界单位）：用于计算贴近敌人的停止距离；
     *  越小攻击距离越近（2026-08-12 由 8 微调至 7） */
    protected static final float meleeRange = 6f;
    /** 近战贴近的减速距离（世界单位）：越大越早减速，越小冲撞越猛 */
    protected static final float attackSmooth = 40f;
    /** 奔跑扬尘计时（帧） */
    protected float dustTimer = 0f;

    @Override
    public void updateMovement(){
        // —— 22.5 格索敌：按仇恨优先级取目标，否则朝核心寻路（标准 GroundAI） ——
        Position nextTarget = findTarget();

        if(nextTarget != null){
            attackMove(nextTarget);
        }else{
            walkToCore();
        }

        kickDust();
    }

    /** 无目标时的标准朝核心寻路（GroundAI 逻辑），供子类复用 */
    protected void walkToCore(){
        super.updateMovement();
    }

    /** 建筑目标候选聚合器：在 lambda 内累计炮台/建筑/墙体中最高类别且最近的目标 */
    private static class BuildingTarget{
        Building best;
        int bestCat = -1;
        float bestDist;
    }

    /**
     * 仇恨优先级索敌：单位 > 炮台 > 建筑 > 墙体。
     * 同类别内取最近目标，更高类别无视距离优先；普通建筑排除低矮（传送带类）方块。
     */
    protected Position findTarget(){
        // 1. 单位优先（仅地面目标，与前颚挥击只命中地面一致）
        Unit enemy = Units.closestEnemy(unit.team, unit.x, unit.y, detectRange, u -> u.checkTarget(false, true));
        if(enemy != null) return enemy;

        // 2-4. 炮台 > 建筑 > 墙体：一次遍历，同类取最近、高类别无视距离优先
        BuildingTarget t = new BuildingTarget();
        Units.nearbyBuildings(unit.x, unit.y, detectRange, b -> {
            if(b.team == unit.team || !b.block.targetable || !b.isValid() || !b.isDiscovered(unit.team)) return;

            // 类别：炮台(2) > 普通建筑(1) > 墙体(0)
            int cat = b.block.attacks ? 2 : (b.block.solid ? 0 : 1);
            // 普通建筑：排除低矮（传送带类）方块；炮台/墙体始终可索敌
            if(cat == 1 && isLowBuilding(b)) return;

            float dist = b.dst(unit.x, unit.y);
            if(cat > t.bestCat || (cat == t.bestCat && (t.best == null || dist < t.bestDist))){
                t.bestCat = cat;
                t.bestDist = dist;
                t.best = b;
            }
        });
        return t.best;
    }

    /** 是否低矮建筑（传送带类）：单格、非实心的小型物流/节点方块，不值得冲撞 */
    private boolean isLowBuilding(Building b){
        return !b.block.solid && b.block.size == 1;
    }

    /** 挥击命中距离：前颚挥击半径 + 自身碰撞半径 + 目标命中体积的一半 + 缓冲。
     *  计入自身碰撞半径后，停止点与目标表面保持间隙，杜绝 physics 碰撞把目标顶走（"推着敌人走"）。 */
    protected float hitRangeFor(Position aim){
        float targetHit = aim instanceof Unit u ? u.hitSize : (aim instanceof Building b ? b.hitSize() : 0f);
        return meleeRange + unit.type.hitSize * 0.5f + targetHit * 0.5f + 1f; // +1f 额外缓冲，避免贴身时顶推目标
    }

    /** 攻击模式：面向目标并贴近到前颚挥击命中距离（含自身碰撞半径，避免顶推目标），贴身由武器自动挥击 */
    protected void attackMove(Position aim){
        // 贴近到命中距离停下（moveTo 内部走 rotateMove 转向移动；贴身自动停住）
        moveTo(aim, hitRangeFor(aim), attackSmooth);

        // 面向目标：保证前颚朝向目标，跑姿与攻击朝向一致
        unit.lookAt(aim);
    }

    /** 扬起沙尘（约每 0.12 秒一团） */
    protected void kickDust(){
        if((dustTimer += Time.delta) >= 7f){
            dustTimer = 0f;
            if(!headless){
                Floor floor = unit.floorOn();
                float dustScale = unit.type.hitSize / 8f;
                if(floor != null){
                    floor.walkEffect.at(unit.x, unit.y, dustScale, floor.mapColor);
                }else{
                    Fx.unitLandSmall.at(unit.x, unit.y, dustScale, Color.white);
                }
            }
        }
    }

}
