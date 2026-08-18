package endfieldindustrylib.EFworld.unit.Landbreakers;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import endfieldindustrylib.EFworld.ai.InfiltratorAI;
import static mindustry.Vars.headless;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import mindustry.ai.types.CommandAI;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.entities.Effect;
import mindustry.entities.Fires;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Building;
import mindustry.gen.EntityMapping;
import mindustry.gen.Groups;
import mindustry.gen.Healthc;
import mindustry.gen.MechUnit;
import mindustry.gen.Unit;
import mindustry.type.StatusEffect;
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
 *       <li>附近有敌方子弹/电弧/火焰接近时横跳闪避（{@link #dodge}，带冷却；脚下有火时朝远离火区方向跳）</li>
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
    /** 攻击前摇时长（帧）：约 0.93 秒；加上突刺窗口后攻击间隔 ≈ 1 秒（攻速） */
    public static final float WINDUP_TIME = 60f;
    /** 单体突刺锁定半径（世界单位）：以 AI 瞄准点为中心寻找目标 */
    public static final float STRIKE_RANGE = 12f;
    /** 单体单次打击伤害（二连击共两次） */
    public static final float STRIKE_DAMAGE = 14f;
    /** 二连击第二击延迟（帧）：0.1 秒 */
    public static final float SECOND_HIT_DELAY = 12f;
    /** 两道划痕的随机角度差（度）：第二道 ≈ 第一道相反方向 ± 此范围 */
    public static final float SLASH_ANGLE_RANGE = 40f;
    /** 红色划痕特效：沿 e.rotation 方向在目标身上快速划过一道红色短线（快速闪过随即淡出） */
    public static final Effect slashFx = new Effect(8f, e -> {
        float a = e.rotation;
        Draw.color(Color.red, e.fout());
        Lines.stroke(2.2f * e.fout());
        Lines.line(e.x - Angles.trnsx(a, 9f), e.y - Angles.trnsy(a, 9f),
                   e.x + Angles.trnsx(a, 9f), e.y + Angles.trnsy(a, 9f));
        Draw.reset();
    });

    /** 潜行受击横跳冷却计时（帧） */
    private float dodgeTimer = 0f;
    /** 潜行受击检测半径（世界单位）：3 格 */
    private static final float DETECT_RADIUS = 3f * 8f;
    /** 判定"子弹正朝本单位飞来"的夹角阈值（度）：弹道方向与"子弹→本单位"方向夹角小于此值才算威胁，避免对路过/远处子弹误闪避 */
    private static final float DODGE_ANGLE = 60f;
    /** 横跳突发速度（世界单位/帧）：阻尼滑行 ≈ 3.2/0.4 = 8 单位 = 1 格（比原 2.5 格减少 1.5 格） */
    private static final float DODGE_VEL = 3.2f;
    /** 横跳冷却（帧）：0.5 秒 */
    private static final float DODGE_COOLDOWN = 10f;
    /** 受击检测中间标志（在遍历 lambda 内写入） */
    private boolean threatNear = false;
    @Override
    public void damage(float amount){
        // 潜行：所有伤害无效（普通子弹因 checkTarget=false 已穿透不命中；此处兜底范围爆炸/电弧/火焰等）
        if(stealth) return;
        super.damage(amount);
    }

    /** 潜行：免疫异常状态（燃烧/触电等会绕过 damage() 拦截，由火焰/电弧/特殊弹附带，需单独拦下） */
    @Override
    public void apply(StatusEffect effect, float duration){
        if(stealth) return;
        super.apply(effect, duration);
    }

    @Override
    public void update(){
        super.update();

        // 玩家控制/指挥：解除潜行（隐身仅供 AI 潜行；玩家接管即显形、可被击杀）
        if(isPlayer() || controller() instanceof CommandAI){
            stealth = false;
            windup = 0f;
        }

        // 潜行受击闪避：附近有威胁（敌方子弹/电弧/火焰）→ 横跳闪避（有冷却）
        if(stealth && (dodgeTimer -= Time.delta) <= 0f && hasThreat()){
            dodgeTimer = DODGE_COOLDOWN;
            dodge();
        }
    }

    /** 附近是否有威胁需要闪避：敌方子弹朝本单位飞来，或附近有敌方电弧/闪电，或脚下附近有火焰
     *  （电弧/火焰无子弹直接伤害，且可能附带异常状态） */
    private boolean hasThreat(){
        threatNear = false;
        // 1. 敌方子弹：普通可命中且朝本单位飞来，或电弧/闪电类（即使不可被子弹碰撞也会直接伤害）
        Groups.bullet.intersect(x - DETECT_RADIUS, y - DETECT_RADIUS, DETECT_RADIUS * 2f, DETECT_RADIUS * 2f, b -> {
            if(!threatNear && b.team != team && b.within(x, y, DETECT_RADIUS)
                && ((b.type.hittable && Angles.angleDist(b.rotation(), b.angleTo(x, y)) < DODGE_ANGLE) || b.type.lightning > 0)){
                threatNear = true;
            }
        });
        // 2. 附近火焰（无子弹直接灼烧）
        if(!threatNear && fireNear()){
            threatNear = true;
        }
        return threatNear;
    }

    /** 附近（约 2 格内）是否有火焰（无子弹直接灼烧，可能附带燃烧状态） */
    private boolean fireNear(){
        int tx = World.toTile(x), ty = World.toTile(y);
        for(int dx = -2; dx <= 2; dx++){
            for(int dy = -2; dy <= 2; dy++){
                if(Fires.has(tx + dx, ty + dy)) return true;
            }
        }
        return false;
    }

    /** 附近最近火焰的世界坐标 {x,y}（用于朝远离火焰方向跳出火区），无则 null */
    private float[] nearestFire(){
        float[] best = null;
        float bestD = Float.MAX_VALUE;
        int tx = World.toTile(x), ty = World.toTile(y);
        for(int dx = -2; dx <= 2; dx++){
            for(int dy = -2; dy <= 2; dy++){
                if(Fires.has(tx + dx, ty + dy)){
                    float wx = (tx + dx) * tilesize + tilesize / 2f;
                    float wy = (ty + dy) * tilesize + tilesize / 2f;
                    float d = Mathf.dst(wx, wy, x, y);
                    if(d < bestD){
                        bestD = d;
                        best = new float[]{wx, wy};
                    }
                }
            }
        }
        return best;
    }

    /** 闪避横跳：脚下有火焰→朝远离火焰方向跳出火区；否则向左右随机一边横跳一步 */
    private void dodge(){
        float dir;
        float[] f = nearestFire();
        if(f != null){
            dir = Mathf.atan2(y - f[1], x - f[0]);   // 远离火焰
        }else{
            dir = baseRotation() + (Mathf.chance(0.5f) ? 90f : -90f);
        }
        vel.add(Angles.trnsx(dir, DODGE_VEL), Angles.trnsy(dir, DODGE_VEL));
        if(!headless){
            Fx.unitLandSmall.at(x, y, hitSize / 8f, team.color);
        }
    }

    public static Infiltrator create(){
        return new Infiltrator();
    }

    /** 单体二连击（由 InfiltratorAI 突刺窗口触发）：锁定最近的敌地面单位或建筑，
     *  两次单体打击，并在目标身上打出两道相反方向、随机角度差的红色划痕 */
    public void strike(float aimX, float aimY){
        // 优先以瞄准点为中心找最近的敌地面单位（单体，非范围）
        Unit target = Units.closestEnemy(team, aimX, aimY, STRIKE_RANGE, u -> u.checkTarget(false, true) && !u.dead);
        if(target == null){
            // 兜底：以自身为中心再找一次（避免瞄准点偏离导致锁定失败）
            target = Units.closestEnemy(team, x, y, STRIKE_RANGE, u -> u.checkTarget(false, true) && !u.dead);
        }
        if(target != null){
            // 近战触达校验（兜底，防止后跳/击退把本体弹开后仍在远处"远程突刺"）：
            // 目标必须在本体 + 目标碰撞半径 + 余量的可触及范围内才结算伤害
            if(within(target, hitSize + target.hitSize + 4f)){
                strikeTarget(target.x(), target.y(), target);
            }
            return;
        }
        // 兜底：命中瞄准点所在格的敌方可攻击建筑（同样校验建筑占地面积内的触达距离）
        Building b = world.buildWorld(aimX, aimY);
        if(b != null && b.team != team && b.block.targetable && b.isValid()
            && b.dst(this) < b.block.size * tilesize / 2f + hitSize + 4f){
            strikeTarget(b.x(), b.y(), b);
        }
    }

    /** 对目标执行二连击（两次单体伤害，第二击略延迟）+ 两道相反方向红色划痕 */
    private void strikeTarget(float tx, float ty, Healthc target){
        // 第一击
        if(!target.dead()) target.damage(STRIKE_DAMAGE);

        // 第二击（略延迟，形成连击感）
        Time.run(SECOND_HIT_DELAY, () -> {
            if(target.isValid() && !target.dead()){
                target.damage(STRIKE_DAMAGE);
            }
        });

        // 划痕特效：第一道随机方向，第二道与之相反并带随机角度差（与第二击同步）
        if(!headless){
            float a1 = Mathf.random(360f);
            float a2 = a1 + 180f + Mathf.range(SLASH_ANGLE_RANGE);
            slashFx.at(tx, ty, a1, team.color);
            Time.run(SECOND_HIT_DELAY, () -> slashFx.at(tx, ty, a2, team.color));
        }
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

        // —— 武器：短刃（纯视觉，不实际开火）。攻击由 InfiltratorAI 突刺窗口直接结算（Infiltrator.strike） ——
        weapons.add(new Weapon("endfield-industry-lib-infiltrator-dagger"){{
            mirror = false;            // 单个短刃，不镜像成对
            top = true;
            layerOffset = 0.02f;       // 绘制在身体之上
            x = 0f; y = 6f;            // 握刃位置（正前方）
            shootY = 6f;
            rotate = false;            // 固定朝前：Mech 身体（baseRotation）朝敌人即命中
            controllable = false;      // 不参与武器开火：突刺由 AI 直接结算，避免自动/时序开火绕过前摇
            autoTarget = false;
            reload = 25f;              // 攻速快（刺客）
            shootCone = 50f;
            recoil = -1f;
            shake = 0f;
            bullet = new BulletType(0f, 0f){{
                speed = 0f;
                lifetime = 1f;
                instantDisappear = true;   // 立即突刺
                // 单体二连击：伤害与特效由 Infiltrator.strike 结算，此处不创建子弹
                splashDamage = 0f;
                splashDamageRadius = 0f;
                collidesAir = false;       // 仅攻击地面目标
                collidesGround = true;
                hittable = false;
                rangeOverride = 8f;        // 攻击/发现距离（与 AI 的贴近距离匹配，避免远处空挥）
            }};
        }});

        // —— 刺客 AI：潜行接近 + 友军保持距离 + 攻击模式 ——
        aiController = () -> new InfiltratorAI();
    }

    /** 攻击模式下首次进入半血 → 闪烁并直接进入潜行（隐身撤退）。
     *  放在 UnitType.update 保证对每个该类型单位必然执行（与实体类无关）。
     *  仅 AI 潜行单位触发；玩家控制/指挥时不触发 AI 隐身逃跑。 */
    @Override
    public void update(Unit unit){
        super.update(unit);

        if(unit instanceof Infiltrator ent && !ent.stealth && !ent.halfHpJumped && unit.healthf() <= 0.5f
            && !unit.isPlayer() && !(unit.controller() instanceof CommandAI)){
            ent.halfHpJumped = true;
            blinkOut(unit, ent);
        }
    }

    /** 半血逃跑：向后猛跳 8 格（远离最近敌人）+ 闪烁火花 + 直接进入潜行（无需等 2 秒） */
    private void blinkOut(Unit unit, Infiltrator ent){
        // 向后 = 远离最近敌人；无敌人则朝当前朝向反方向
        Unit enemy = Units.closestEnemy(unit.team, unit.x, unit.y, 30f * 8f, u -> true);
        float dir = enemy != null ? unit.angleTo(enemy) + 180f : unit.rotation + 180f;

        // 猛跳 8 格（vel 直驱爆发，阻尼滑行 ≈ jumpVel/drag）
        unit.vel.add(Angles.trnsx(dir, JUMP_VEL), Angles.trnsy(dir, JUMP_VEL));

        // 闪烁效果：彩色火花（半血瞬间闪烁后进入潜行）
        if(!headless){
            Fx.colorSpark.at(unit.x, unit.y);
            Fx.colorSpark.at(unit.x + Mathf.range(4f), unit.y + Mathf.range(4f), Color.sky);
        }

        // 直接进入潜行（隐身撤退），取消进行中的前摇
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
