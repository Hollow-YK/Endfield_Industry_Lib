package endfieldindustrylib.EFworld.ai;

import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.util.Time;
import endfieldindustrylib.EFworld.unit.Landbreakers.Infiltrator;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.unitCollisionRadiusScale;
import mindustry.ai.types.GroundAI;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Unit;

/**
 * 潜行者（Infiltrator）专属 AI —— Landbreakers 人形族的 T1 高速刺客 AI。
 * <p>
 * 状态机：
 * <ul>
 *   <li><b>潜行接近</b>（默认，{@code Infiltrator.stealth}=true）：低仇恨接近敌人
 *       （潜行中不可索敌、子弹穿透、受击横跳），直到进入突刺距离 {@link #hitRangeFor}。</li>
 *   <li><b>攻击前摇</b>：进入突刺距离后蓄力 {@code WINDUP_TIME}（0.67 秒），蓄力期间保持潜行；
 *       前摇结束瞬间解除潜行并突刺（单体二连击）。</li>
 *   <li><b>攻击循环</b>：每次突刺后立即进入下一次前摇（前摇时长≥装填时间，
 *       保证每次攻击都带前摇）；首次被打到半血时由 {@code Infiltrator} 触发
 *       向后猛跳 8 格 + 烟雾弹并切回潜行（隐身撤退）。</li>
 * </ul>
 * <b>始终与友军保持距离</b>：移动向量叠加 {@link #friendlyRange}（5 格）内友军的排斥力，
 * 避免刺客扎堆。
 */
public class InfiltratorAI extends GroundAI{
    /** 索敌半径（世界单位）：22.5 格 */
    public static final float detectRange = 15f * tilesize * 1.5f;
    /** 突刺开火窗口（帧）：前摇结束后保持射击开启的帧数，保证武器确实开火一次 */
    protected static final float strikeWindowLen = 3f;
    /** 与友军保持的最小距离（世界单位）：5 格 */
    public static final float friendlyRange = 5f * tilesize;
    /** 友军排斥强度（叠加到移动向量的最大分量） */
    protected static final float separationForce = 2f;

    /** 复用向量 */
    private final Vec2 tmp = new Vec2();
    /** 友军分离向量 */
    private final Vec2 separation = new Vec2();
    /** 突刺开火窗口剩余时间（帧），>0 表示正在执行突刺 */
    protected float strikeWindow = 0f;
    /** 未攻击时重新潜行所需的空闲计时（帧） */
    protected float reStealthTimer = 0f;
    /** 未攻击满 1.5 秒后重新潜行（90 帧）；首次攻击解除潜行、半血时立刻进入潜行 */
    protected static final float RESTEALTH_GRACE = 90f;

    /**
     * 潜行中强制锁死武器：AIController.updateWeapons 会自行索敌并置 mount.shoot，
     * 可能绕过潜行限制让武器开火；此处保证"能造成伤害 ⟺ 已解除隐身（可被击杀）"。
     */
    @Override
    public void updateWeapons(){
        Infiltrator ent = unit instanceof Infiltrator e ? e : null;
        if(ent != null && ent.stealth){
            unit.controlWeapons(true, false);
            return;
        }
        super.updateWeapons();
    }

    @Override
    public void updateMovement(){
        Infiltrator ent = unit instanceof Infiltrator e ? e : null;
        Position nextTarget = findTarget();

        if(nextTarget == null){
            // 无目标：未攻击满 1.5 秒后进入潜行（期间保持显形、可被击杀）
            if(ent != null && !ent.stealth && (reStealthTimer += Time.delta) > RESTEALTH_GRACE){
                ent.stealth = true;
                ent.windup = 0f;
            }
            strikeWindow = 0f;
            unit.controlWeapons(true, false);
            super.updateMovement();
            return;
        }

        // 有目标：重置重新潜行计时
        reStealthTimer = 0f;

        float dst = unit.dst(nextTarget);

        // 潜行接近：仅在未进入蓄力/突刺循环时生效（避免每帧重置前摇导致永不攻击）
        if(ent != null && ent.stealth && ent.windup <= 0f && strikeWindow <= 0f){
            unit.controlWeapons(true, false);
            if(dst <= hitRangeFor(nextTarget)){
                ent.windup = Infiltrator.WINDUP_TIME;   // 进入突刺距离：开始蓄力（仍保持潜行，突刺瞬间才解除）
            }else{
                moveWithSeparation(nextTarget, hitRangeFor(nextTarget));
                unit.lookAt(nextTarget);
                return;
            }
        }

        // 攻击前摇：边贴近边蓄力（前摇期间不射击），倒计时结束进入突刺窗口
        if(ent != null && ent.windup > 0f){
            ent.windup -= Time.delta;
            if(ent.windup <= 0f){
                ent.windup = 0f;
                strikeWindow = strikeWindowLen;
            }
            unit.controlWeapons(true, false);
            moveWithSeparation(nextTarget, hitRangeFor(nextTarget));
            unit.lookAt(nextTarget);
            return;
        }

        // 突刺窗口：首帧解除潜行并直接执行单体二连击（由 AI 结算，不依赖武器开火时序）
        if(strikeWindow > 0f){
            boolean first = strikeWindow >= strikeWindowLen;   // 刚进入突刺窗口的那一帧
            strikeWindow -= Time.delta;
            if(ent != null){
                // 仅在仍处于近战触达距离内才开火：半血后跳会把单位弹开 8 格，
                // 若不校验就会在远处"远程突刺"（strike 无距离限制，锁定即命中）
                boolean inReach = unit.within(nextTarget, hitRangeFor(nextTarget) + 4f);
                if(inReach){
                    ent.stealth = false;
                    ent.windup = 0f;
                    if(first){
                        ent.strike(nextTarget.getX(), nextTarget.getY());
                    }
                }else{
                    // 已被弹开/远离：放弃本次突刺并保持潜行，重新贴近再打（不暴露也不远程白嫖）
                    strikeWindow = 0f;
                }
            }
            unit.controlWeapons(true, false);
            unit.lookAt(nextTarget);
            if(strikeWindow <= 0f){
                unit.controlWeapons(true, false);
            }
            return;
        }

        // 攻击模式：突刺完成 → 立即进入下一次前摇（攻击循环，保持解除潜行）
        if(ent != null) ent.windup = Infiltrator.WINDUP_TIME;
        unit.controlWeapons(true, false);
        attackMove(nextTarget);
    }

    /** 攻击模式：贴近到命中距离挥砍 */
    protected void attackMove(Position aim){
        moveWithSeparation(aim, hitRangeFor(aim));
        unit.lookAt(aim);
    }

    /** 带友军分离的移动：无减速——超出停留距离全速推进、进入即停；再叠加远离 5 格内友军的排斥向量 */
    protected void moveWithSeparation(Position aim, float circleLength){
        tmp.set(aim).sub(unit);
        // 删除靠近减速：直接全速推进，进入停留距离立即停下
        float length = unit.dst(aim) > circleLength ? 1f : 0f;
        tmp.setLength(prefSpeed() * length);

        // 友军分离：移动时尝试和其他友军保持 5 格距离
        updateSeparation();
        tmp.add(separation);

        // 走标准移动链路（movePref → rotateMove），Mech 身体朝移动方向
        unit.movePref(tmp);
    }

    /** 计算 5 格内友军的排斥合力（远离扎堆） */
    protected void updateSeparation(){
        separation.setZero();
        Units.nearby(unit.team, unit.x, unit.y, friendlyRange, u -> {
            if(u == unit) return;
            float d = unit.dst(u);
            if(d < friendlyRange && d > 0.001f){
                float push = (1f - d / friendlyRange) * separationForce;
                separation.add((unit.x - u.x) / d * push, (unit.y - u.y) / d * push);
            }
        });
    }

    /** 突刺停止距离：停在敌人/建筑身前（不顶入物理碰撞区），确保前摇能触发、突刺能命中 */
    protected float hitRangeFor(Position aim){
        float own = unit.hitSize;
        if(aim instanceof Unit u){
            // 单位：双方物理碰撞半径之和 + 缓冲（停在敌人身前，不顶着跑）
            return (own + u.hitSize) * unitCollisionRadiusScale + 2f;
        }else if(aim instanceof Building b){
            // 建筑：建筑半格占地 + 自身碰撞半径 + 缓冲
            return b.block.size * tilesize / 2f + own * unitCollisionRadiusScale + 2f;
        }
        return own + 4f;
    }

    /** 索敌：敌方地面单位优先；无单位时兜底建筑（炮台>建筑>墙体），
     *  保证始终有目标可攻击 → 能解除潜行（可被击杀），不会因无单位目标而永远隐身无敌 */
    protected Position findTarget(){
        // 1. 敌方单位优先（地面目标）
        Unit enemy = Units.closestEnemy(unit.team, unit.x, unit.y, detectRange, u -> u.checkTarget(false, true));
        if(enemy != null) return enemy;

        // 2. 建筑兜底（炮台 > 建筑 > 墙体，同类取最近）
        BuildingTarget t = new BuildingTarget();
        Units.nearbyBuildings(unit.x, unit.y, detectRange, b -> {
            if(b.team == unit.team || !b.block.targetable || !b.isValid() || !b.isDiscovered(unit.team)) return;
            int cat = b.block.attacks ? 2 : (b.block.solid ? 0 : 1);
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

    /** 是否低矮建筑（传送带类）：单格、非实心的小型物流/节点方块，不值得攻击 */
    private boolean isLowBuilding(Building b){
        return !b.block.solid && b.block.size == 1;
    }

    /** 建筑目标候选聚合器：在 lambda 内累计最高类别且最近的目标 */
    private static class BuildingTarget{
        Building best;
        int bestCat = -1;
        float bestDist;
    }
}
