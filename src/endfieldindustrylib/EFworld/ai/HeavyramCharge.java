package endfieldindustrylib.EFworld.ai;

import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.util.Time;
import endfieldindustrylib.EFworld.unit.Aggeloi.HeavyramLegsUnit;
import static mindustry.Vars.headless;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.world.Tile;

/**
 * （Heavyram）冲锋技能逻辑 — 供 {@link HeavyramAI}（自动 AI）与
 * {@link RamCommandAI}（玩家指挥）共用，保证两套控制方式下冲锋行为一致。
 * <p>
 * 状态机：普通走路/攻击 → 锁定敌人原地停顿蓄力（公牛式单腿磨地）→ 疾驰奔跑到敌人面前 →
 * 回走路攻击。蓄力阶段敌人靠近则直接取消蓄力转入普通攻击；两次冲锋内置冷却 20 秒。
 * <p>
 * {@link #update(Unit, Position)} 每帧调用：返回 {@code true} 表示本帧处于蓄力/冲锋状态
 * （已接管移动），调用方应跳过自己的移动逻辑；返回 {@code false} 表示按普通逻辑移动。
 */
public class HeavyramCharge {
    /** 冲锋冷却（帧）：两次冲锋之间的内置冷却（20 秒） */
    public static final float chargeCooldown = 60f * 20f;
    /** 冲锋最大持续时间（帧）：防止目标一直逃跑导致无限冲锋的安全上限（正常靠近敌人提前结束） */
    public static final float chargeDuration = 60f * 5f;
    /** 原地蓄力停顿（帧）：锁定敌人后站定 2 秒再冲锋 */
    public static final float chargeWindup = 60f * 2f;
    /** 冲锋目标速度倍率（相对普通移动速度） */
    public static final float chargeSpeedMul = 2f;
    /** 冲锋加速度（{@code vel.approachDelta} 速率）：越大起步越猛 */
    public static final float chargeAccel = 3f;
    /** 触发冲锋的最小距离（世界单位）：太近无需冲，直接攻击 */
    public static final float minChargeRange = 7f * tilesize;
    /** 触发冲锋的最大距离（世界单位）：太远先走路接近 */
    public static final float maxChargeRange = 17f * tilesize;
    /** 前颚挥击命中距离基数（世界单位）：冲锋停止/取消与贴近攻击的距离基准；
     *  与 RamAI.meleeRange 保持一致（2026-08-12 由 8 微调至 7，攻击距离略微缩小） */
    private static final float meleeRange = 7f;

    /** 复用向量，避免每帧分配 */
    private final Vec2 tmp = new Vec2();

    /** 距离下次可冲锋的剩余时间（帧） */
    private float chargeTimer = 0f;
    /** 原地蓄力停顿剩余时间（帧）：>0 表示锁定敌人、正在原地停顿蓄力 */
    private float windup = 0f;
    /** 冲锋剩余持续时间（帧）：>0 表示冲锋中（直到靠近敌人） */
    private float charging = 0f;

    /** 当前是否处于冲锋中（用于扬尘等表现） */
    public boolean charging(){
        return charging > 0f;
    }

    /**
     * 每帧调用。返回 {@code true} 表示本帧处于蓄力/冲锋状态（接管移动），
     * 返回 {@code false} 表示冲锋未激活，调用方应按普通移动/攻击逻辑处理。
     */
    public boolean update(Unit unit, Position target){
        if(target == null){
            stop(unit);
            return false;
        }

        if(charging > 0f){
            // 冲锋中：持续高速冲向目标，直到靠近敌人
            charging -= Time.delta;
            // 冲锋仍在进行但计时已耗尽（安全上限、始终未能命中目标，如卡在墙边）→ 取消并返还冷却
            if(doCharge(unit, target) && charging <= 0f){
                cancel(unit);
            }
            return charging > 0f;
        }else if(windup > 0f){
            // 蓄力停顿：原地站定面向锁定目标，停顿 2 秒后开始冲锋
            windup -= Time.delta;
            // 敌人靠得太近（已进入命中距离、或近到无需再冲锋）：取消蓄力并返还冷却，转入普通走路攻击
            if(unit.within(target, hitRangeFor(unit, target)) || unit.within(target, minChargeRange)){
                cancel(unit);
                return false;
            }
            chargeWindupStand(unit, target);
            if(windup <= 0f){
                startCharge(unit);
            }
            return true;
        }else{
            // 普通状态：冷却计时；满足条件则锁定目标进入蓄力
            chargeTimer -= Time.delta;
            if(chargeTimer <= 0f && canCharge(unit, target)){
                beginWindup(unit);
                return true;
            }
            return false;
        }
    }

    /** 结束冲锋/蓄力：清除奔跑步态/磨地标志、清零计时；仅在确实处于冲锋/蓄力时刹停（清零速度），
     *  否则不清零——避免普通移动命令（非冲锋状态）每帧被清零速度而无法移动 */
    public void stop(Unit unit){
        boolean active = charging > 0f || windup > 0f;
        if(charging > 0f) charging = 0f;
        windup = 0f;
        setSprint(unit, false);
        setPawing(unit, false);
        if(active){
            unit.vel.setZero();   // 冲锋/蓄力中结束才刹停，避免以冲锋速度继续滑行/顶推敌人
        }
    }

    /** 取消冲锋/蓄力：清理状态并返还冷却（冲锋未执行/未命中即被取消时调用，可尽快再次冲锋） */
    public void cancel(Unit unit){
        stop(unit);
        chargeTimer = 0f;     // 返还冷却：取消的冲锋不计入 20 秒内置冷却
    }

    /** 是否满足冲锋条件：距离适中且未贴脸（已在挥击命中距离内直接攻击，不冲锋）+ 直线路径未被墙挡住 */
    private boolean canCharge(Unit unit, Position target){
        float d = unit.dst(target);
        if(d < minChargeRange || d > maxChargeRange) return false;
        // 已在挥击命中距离内：直接攻击即可，无需冲锋（避免贴脸仍触发冲锋→随即取消的闪烁）
        if(unit.within(target, hitRangeFor(unit, target))) return false;
        return !lineBlocked(unit, target);
    }

    /** 锁定敌人：进入蓄力停顿，同时启动 20 秒内置冷却 */
    private void beginWindup(Unit unit){
        windup = chargeWindup;
        chargeTimer = chargeCooldown;
        setSprint(unit, false);
        setPawing(unit, true);       // 蓄力：公牛式单腿摩擦地面
        unit.vel.setZero();

        if(!headless){
            Fx.unitLandSmall.at(unit.x, unit.y, unit.hitSize / 8f, unit.team.color);
        }
    }

    /** 蓄力停顿：原地站定、锁定朝向目标（不移动） */
    private void chargeWindupStand(Unit unit, Position target){
        unit.vel.setZero();
        unit.lookAt(target);
        setSprint(unit, false);
    }

    /** 蓄力结束：切换奔跑步态并开始冲锋（持续到靠近敌人） */
    private void startCharge(Unit unit){
        charging = chargeDuration;
        setSprint(unit, true);
        setPawing(unit, false);      // 停止磨地，开始冲锋

        if(!headless){
            Fx.unitLandSmall.at(unit.x, unit.y, unit.hitSize / 8f, unit.team.color);
        }
    }

    /** 冲锋过程：高速直线冲向目标直到靠近敌人；撞墙或路径被挡即结束（返还冷却）。返回 true=仍在冲锋，false=已结束 */
    private boolean doCharge(Unit unit, Position target){
        float hitRange = hitRangeFor(unit, target);

        // 已冲到敌人面前（命中距离内）：冲锋成功结束，转入普通攻击（正常命中不返还冷却）
        if(unit.within(target, hitRange)){
            stop(unit);
            return false;
        }

        // 撞上墙体或直线被挡：冲锋被中断、未命中目标 → 取消并返还冷却
        if(unit.onSolid() || lineBlocked(unit, target)){
            cancel(unit);
            return false;
        }

        setSprint(unit, true);

        // 直接驱动速度朝目标高速冲刺（爆发技能，短时绕过标准移动链路；lookAt 负责面向目标）
        float speed = unit.speed() * chargeSpeedMul;
        tmp.set(target).sub(unit).setLength(speed);
        unit.vel.approachDelta(tmp, chargeAccel);
        unit.lookAt(target);
        return true;
    }

    /** 前颚挥击命中距离：挥击半径 + 自身碰撞半径 + 目标命中体积的一半 + 缓冲 */
    private float hitRangeFor(Unit unit, Position aim){
        float targetHit = aim instanceof Unit u ? u.hitSize : (aim instanceof Building b ? b.hitSize() : 0f);
        return meleeRange + unit.type.hitSize * 0.5f + targetHit * 0.5f + 1f;
    }

    /** 切换冲锋奔跑步态（由 HeavyramLegsUnit 渲染疾驰步态） */
    private void setSprint(Unit unit, boolean sprint){
        if(unit instanceof HeavyramLegsUnit legs){
            legs.sprinting = sprint;
        }
    }

    /** 切换蓄力磨地标志（由 HeavyramLegsUnit 渲染公牛式单腿摩擦地面） */
    private void setPawing(Unit unit, boolean paw){
        if(unit instanceof HeavyramLegsUnit legs){
            legs.pawing = paw;
        }
    }

    /** 单位与目标之间是否有实心障碍（目标所在格不算障碍，避免贴脸误判） */
    private boolean lineBlocked(Unit unit, Position target){
        int tx = World.toTile(target.getX()), ty = World.toTile(target.getY());
        return World.raycast(unit.tileX(), unit.tileY(), tx, ty, (x, y) -> {
            Tile tile = world.tile(x, y);
            if(tile != null && tile.build == target) return false;
            return tile == null || tile.solid();
        });
    }
}
