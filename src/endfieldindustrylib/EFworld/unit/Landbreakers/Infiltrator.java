package endfieldindustrylib.EFworld.unit.Landbreakers;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import endfieldindustrylib.EFworld.ai.InfiltratorAI;
import static mindustry.Vars.headless;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.EntityMapping;
import mindustry.gen.Groups;
import mindustry.gen.MechUnit;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.type.Weapon;

/**
 * 潜行者（Infiltrator）专用实体 — 实现潜行/攻击双模式的战斗内机制。
 * <p>
 * <ul>
 *   <li><b>潜行模式（{@link #stealth}=true，默认）</b>：
 *     <ul>
 *       <li>{@link #checkTarget} 返回 false → 敌方 AI 无法索敌（极低仇恨值），
 *           子弹碰撞也跳过它 → <b>子弹穿透、不消耗</b>（依旧向后飞）</li>
 *       <li>{@link #damage} 拦截 → 所有伤害无效（兜底范围爆炸等）</li>
 *       <li>附近有敌方子弹接近时向左右横跳一步（{@link #dodge}，带冷却）</li>
 *     </ul>
 *   </li>
 *   <li><b>攻击前摇（{@link #stealth}=false）</b>：进入突刺距离后解除潜行（暴露、可被锁定），
 *       蓄力 {@link #WINDUP_TIME} 后突刺一次；潜行中不会攻击</li>
 * </ul>
 */
public class Infiltrator extends MechUnit{

    /**
     * 实体唯一注册 id：通过 {@link EntityMapping#register} 注册本类到实体映射，
     * 使存档/网络按此 id 反序列化回 {@code InfiltratorUnit} 而非基类 {@code MechUnit}。
     */
    public static final int ENTITY_ID = EntityMapping.register("endfield-industry-lib-infiltrator-unit", Infiltrator::new);

    @Override
    public int classId(){
        return ENTITY_ID;
    }

    /** 潜行模式标志（由 InfiltratorAI / Infiltrator 半血猛跳切换） */
    public boolean stealth = true;
    /** 攻击模式是否已触发过半血猛跳（仅首次） */
    public boolean halfHpJumped = false;
    /** 攻击前摇剩余时间（帧），>0 表示正在前摇（已解除潜行、暴露可被锁定） */
    public float windup = 0f;
    /** 攻击前摇时长（帧）：0.67 秒（≥ 武器装填 25 帧，保证前摇结束即可突刺） */
    public static final float WINDUP_TIME = 40f;

    /** 潜行受击横跳冷却计时（帧） */
    private float dodgeTimer = 0f;
    /** 潜行受击检测半径（世界单位）：3 格 */
    private static final float DETECT_RADIUS = 3f * 8f;
    /** 横跳突发速度（世界单位/帧）：阻尼滑行后约 1~2 格（DODGE_VEL / drag） */
    private static final float DODGE_VEL = 8f;
    /** 横跳冷却（帧）：0.5 秒 */
    private static final float DODGE_COOLDOWN = 30f;
    /** 受击检测中间标志（在遍历 lambda 内写入） */
    private boolean threatNear = false;

    @Override
    public boolean checkTarget(boolean targetAir, boolean targetGround){
        // 潜行：完全不可索敌（低仇恨）+ 子弹穿透（敌方子弹碰撞跳过它，不消耗、依旧向后飞）
        if(stealth) return false;
        return super.checkTarget(targetAir, targetGround);
    }

    @Override
    public void damage(float amount){
        // 潜行：所有伤害无效（普通子弹因 checkTarget=false 已穿透不命中；此处兜底范围爆炸等）
        if(stealth) return;
        super.damage(amount);
    }

    @Override
    public void update(){
        super.update();

        // 潜行受击闪避：附近有敌方子弹接近 → 向左右横跳一步（有冷却）
        if(stealth && (dodgeTimer -= Time.delta) <= 0f && enemyBulletNear()){
            dodgeTimer = DODGE_COOLDOWN;
            dodge();
        }
    }

    /** 附近是否有敌方子弹正在接近（碰撞检测半径内） */
    private boolean enemyBulletNear(){
        threatNear = false;
        Groups.bullet.intersect(x - DETECT_RADIUS, y - DETECT_RADIUS, DETECT_RADIUS * 2f, DETECT_RADIUS * 2f, b -> {
            if(b.team != team && b.type.hittable && b.within(x, y, DETECT_RADIUS)){
                threatNear = true;
            }
        });
        return threatNear;
    }

    /** 向左右随机一边横跳一步 */
    private void dodge(){
        float dir = baseRotation() + (Mathf.chance(0.5f) ? 90f : -90f);
        vel.add(Angles.trnsx(dir, DODGE_VEL), Angles.trnsy(dir, DODGE_VEL));
        if(!headless){
            Fx.unitLandSmall.at(x, y, hitSize / 8f, team.color);
        }
    }

    public static Infiltrator create(){
        return new Infiltrator();
    }
    public static class InfiltratorType extends UnitType{
    /** 半血猛跳突发速度（世界单位/帧）：阻尼滑行 ≈ 26/0.4 = 65 单位 ≈ 8 格 */
    private static final float JUMP_VEL = 26f;

    public InfiltratorType(String name){
        super(name);

        // —— 基础属性：高速脆皮刺客（T1） ——
        health = 240f;
        speed = 0.8f;
        hitSize = 8f;
        armor = 0f;
        drag = 0.4f;
        accel = 0.4f;
        rotateSpeed = 5f;

        // —— 地面单位 ——
        flying = false;
        physics = true;                 // 启动物理碰撞
        hovering = false;               // 不悬浮，受地面影响
        canDrown = true;                // 可在深水中淹死
        canBoost = false;               // 不能起飞
        omniMovement = true;            // 可原地转向：朝按键/目标方向直行，身体转向移动方向（人形）
        isEnemy = true;
        drawBody = true;
        drawCell = false;
        drawItems = true;
        drawSoftShadow = false;
        targetAir = false;              // 近战仅对地面

        // —— 人形机甲（Mech）参数 ——
        constructor = Infiltrator::create;
        mechSideSway = 0.4f;            // 行走左右摆动（刺客步幅更轻）
        mechFrontSway = 0.25f;
        baseRotateSpeed = 10f;          // 身体转向快（高速刺客）；mechStride 由引擎按 hitSize 自动计算

        // —— 武器：短刃突刺（近战，攻速快） ——
        weapons.add(new Weapon("endfield-industry-lib-infiltrator-dagger"){{
            mirror = false;            // 单个短刃，不镜像成对
            top = true;
            layerOffset = 0.02f;       // 绘制在身体之上
            x = 0f; y = 6f;            // 握刃位置（正前方）
            shootY = 6f;
            rotate = false;            // 固定朝前：Mech 身体（baseRotation）朝敌人即命中
            controllable = true;       // 由 AI 控制开火时机：前摇结束才突刺（不做自动索敌）
            autoTarget = false;
            reload = 25f;              // 攻速快（刺客）
            shootCone = 50f;
            recoil = -1f;
            shake = 0f;
            bullet = new BulletType(0f, 0f){{
                speed = 0f;
                lifetime = 1f;
                instantDisappear = true;   // 立即突刺
                splashDamage = 22f;        // 突刺伤害（刺客：低单发、高攻速）
                splashDamageRadius = 22f;
                collidesAir = false;       // 仅攻击地面目标
                collidesGround = true;
                hittable = false;
                rangeOverride = 8f;        // 攻击/发现距离（与 AI 的贴近距离匹配，避免远处空挥）
            }};
        }});

        // —— 刺客 AI：潜行接近 + 友军保持距离 + 攻击模式 ——
        aiController = () -> new InfiltratorAI();
    }

    /** 攻击模式下首次进入半血 → 向后猛跳 8 格 + 烟雾弹粒子 + 切回潜行（隐身撤退）。
     *  放在 UnitType.update 保证对每个该类型单位必然执行（与实体类无关）。 */
    @Override
    public void update(Unit unit){
        super.update(unit);

        if(unit instanceof Infiltrator ent && !ent.stealth && !ent.halfHpJumped && unit.healthf() <= 0.5f){
            ent.halfHpJumped = true;
            jumpBack(unit, ent);
        }
    }

    /** 向后猛跳 8 格（远离最近敌人）并产生烟雾弹粒子效果，随后切回潜行 */
    private void jumpBack(Unit unit, Infiltrator ent){
        // 向后 = 远离最近敌人；无敌人则朝当前朝向反方向
        Unit enemy = Units.closestEnemy(unit.team, unit.x, unit.y, 30f * 8f, u -> true);
        float dir = enemy != null ? unit.angleTo(enemy) + 180f : unit.rotation + 180f;

        // 猛跳 8 格（vel 直驱爆发，阻尼滑行 ≈ jumpVel/drag）
        unit.vel.add(Angles.trnsx(dir, JUMP_VEL), Angles.trnsy(dir, JUMP_VEL));

        // 烟雾弹一样的粒子：中心烟团 + 周围多团扩散烟
        if(!headless){
            Fx.smoke.at(unit.x, unit.y);
            for(int i = 0; i < 6; i++){
                Fx.smoke.at(unit.x + Mathf.range(6f), unit.y + Mathf.range(6f), Mathf.random(360f), Color.gray);
            }
        }

        // 切回潜行（隐身撤退），取消进行中的前摇
        ent.stealth = true;
        ent.windup = 0f;
    }

    /** 视觉指示：潜行画淡青蓝"潜行环"；攻击前摇画红色"蓄力警示环"（暴露、即将突刺） */
    @Override
    public void drawCell(Unit unit){
        if(unit instanceof Infiltrator e){
            // 攻击前摇：红色警示环随时间扩散，提示即将突刺
            if(e.windup > 0f){
                float p = 1f - e.windup / WINDUP_TIME;   // 前摇进度 0→1
                Draw.color(Color.red, 0.5f);
                Lines.stroke(1.6f);
                Lines.circle(unit.x, unit.y, unit.hitSize * (0.55f + p * 0.65f));
                Draw.reset();
                return;
            }
            // 潜行：淡青蓝"潜行环"
            if(e.stealth){
                Draw.color(Color.sky, 0.35f);
                Lines.stroke(1.2f);
                Lines.circle(unit.x, unit.y, unit.hitSize * 0.75f);
                Draw.reset();
                return;
            }
        }
        super.drawCell(unit);
    }
}

}
