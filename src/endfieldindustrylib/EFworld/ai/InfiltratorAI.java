package endfieldindustrylib.EFworld.ai;

import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.util.Time;
import static mindustry.Vars.tilesize;
import endfieldindustrylib.EFworld.unit.Landbreakers.Infiltrator;
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
 *   <li><b>攻击前摇</b>：进入突刺距离后解除潜行（暴露、可被锁定），边贴近边蓄力
 *       {@code WINDUP_TIME}（0.67 秒）；前摇结束瞬间武器突刺一次。</li>
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
    /** 近战命中距离基数（世界单位） */
    protected static final float meleeRange = 6f;
    /** 突刺开火窗口（帧）：前摇结束后保持射击开启的帧数，保证武器确实开火一次 */
    protected static final float strikeWindowLen = 3f;
    /** 与友军保持的最小距离（世界单位）：5 格 */
    public static final float friendlyRange = 5f * tilesize;
    /** 友军排斥强度（叠加到移动向量的最大分量） */
    protected static final float separationForce = 2f;
    /** 贴近减速步长（数值越大越早减速） */
    protected static final float attackSmooth = 40f;

    /** 复用向量 */
    private final Vec2 tmp = new Vec2();
    /** 友军分离向量 */
    private final Vec2 separation = new Vec2();
    /** 突刺开火窗口剩余时间（帧），>0 表示正在执行突刺 */
    protected float strikeWindow = 0f;

    @Override
    public void updateMovement(){
        Infiltrator ent = unit instanceof Infiltrator e ? e : null;
        Position nextTarget = findTarget();

        if(nextTarget == null){
            // 无目标：潜行待机，朝核心寻路（标准 GroundAI）
            if(ent != null){ ent.stealth = true; ent.windup = 0f; }
            strikeWindow = 0f;
            unit.controlWeapons(true, false);
            super.updateMovement();
            return;
        }

        float dst = unit.dst(nextTarget);

        // 潜行模式：接近到突刺距离 → 开始攻击前摇（解除潜行、暴露可被锁定）
        if(ent != null && ent.stealth){
            strikeWindow = 0f;
            ent.windup = 0f;
            unit.controlWeapons(true, false);
            if(dst <= hitRangeFor(nextTarget)){
                ent.stealth = false;
                ent.windup = Infiltrator.WINDUP_TIME;
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

        // 突刺窗口：短暂开火瞬间（武器突刺一次），随后停止
        if(strikeWindow > 0f){
            strikeWindow -= Time.delta;
            unit.controlWeapons(true, true);
            unit.aim(nextTarget);
            unit.lookAt(nextTarget);
            if(strikeWindow <= 0f){
                unit.controlWeapons(true, false);
            }
            return;
        }

        // 攻击模式：突刺完成 → 立即进入下一次前摇（前摇时长≥装填时间，保证每次突刺都带前摇）
        if(ent != null) ent.windup = Infiltrator.WINDUP_TIME;
        unit.controlWeapons(true, false);
        attackMove(nextTarget);
    }

    /** 攻击模式：贴近到命中距离挥砍 */
    protected void attackMove(Position aim){
        moveWithSeparation(aim, hitRangeFor(aim));
        unit.lookAt(aim);
    }

    /** 带友军分离的移动：向目标贴近（circleLength 减速）+ 远离 5 格内友军的排斥向量 */
    protected void moveWithSeparation(Position aim, float circleLength){
        tmp.set(aim).sub(unit);
        float length = circleLength <= 0.001f ? 1f : Mathf.clamp((unit.dst(aim) - circleLength) / attackSmooth, 0f, 1f);
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

    /** 突刺命中距离：近战基数 + 自身碰撞半径 + 目标命中体积一半 + 缓冲 */
    protected float hitRangeFor(Position aim){
        float targetHit = aim instanceof Unit u ? u.hitSize : (aim instanceof Building b ? b.hitSize() : 0f);
        return meleeRange + unit.type.hitSize * 0.5f + targetHit * 0.5f + 1f;
    }

    /** 索敌：最近地面单位（刺客目标为敌方单位/人） */
    protected Position findTarget(){
        return Units.closestEnemy(unit.team, unit.x, unit.y, detectRange, u -> u.checkTarget(false, true));
    }
}
