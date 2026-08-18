package endfieldindustrylib.EFworld.unit.Landbreakers;

import endfieldindustrylib.EFworld.ai.RaiderAI;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.MechUnit;
import mindustry.type.UnitType;
import mindustry.type.Weapon;

/**
 * 突袭者（Raider）—— Landbreakers 人形族的 T1 基础近战单位。
 * <p>
 * 人形（{@link MechUnit}）战士：血量/护甲比同族伏击者（Ambusher）更硬、移速更快，
 * 手持一把砍刀<b>横扫</b>前方扇形区域（较宽的挥击锥角 + 较大范围），贴身作战。
 * <p>
 * 基础近战 AI（见 {@link RaiderAI}）：索敌后贴近到横扫命中距离，贴身由武器自动挥砍；
 * 无目标时朝核心寻路。
 * <p>
 * 走路线 A（引擎原生 Mech 人形机甲）：两腿交替迈步 + 身体随步伐摆动，无需多帧腿部动画。
 */
public class Raider extends UnitType{

    public Raider(String name){
        super(name);

        // —— 基础属性：近战战士（T1，比伏击者更硬更快） ——
        health = 240f;
        speed = 0.5f;
        hitSize = 9f;
        armor = 2f;
        drag = 0.4f;
        accel = 0.3f;
        rotateSpeed = 4f;

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
        constructor = MechUnit::create;
        mechSideSway = 0.5f;            // 行走左右摆动
        mechFrontSway = 0.35f;          // 行走前后起伏（近战步幅更沉）
        baseRotateSpeed = 9f;           // 身体转向较快（人形灵敏）；mechStride 由引擎按 hitSize 自动计算

        // —— 武器：砍刀横扫（前方扇形近战挥击） ——
        weapons.add(new Weapon("endfield-industry-lib-raider-machete"){{
            mirror = false;            // 单个砍刀，不镜像成对
            top = true;
            layerOffset = 0.02f;       // 绘制在身体之上
            x = 0f; y = 7f;            // 握刀位置（正前方）
            shootY = 7f;               // 挥击点位于单位正前方
            rotate = false;            // 固定朝前：Mech 身体（baseRotation）朝敌人即命中
            controllable = false;
            autoTarget = true;         // 自动索敌挥砍
            reload = 100f;              // 挥砍频率（帧）
            shootCone = 70f;           // 宽锥角 → 扇形横扫
            recoil = -1f;
            shake = 0f;
            bullet = new BulletType(0f, 0f){{
                speed = 0f;
                lifetime = 1f;
                instantDisappear = true;   // 立即横扫
                splashDamage = 32f;        // 砍刀挥击伤害
                splashDamageRadius = 30f;  // 横扫范围（较大扇形）
                collidesAir = false;       // 仅攻击地面目标
                collidesGround = true;
                hittable = false;
                rangeOverride = 10f;       // 攻击/发现距离（与 AI 的贴近距离匹配，避免远处空挥）
                //hitEffect = Fx.hitBulletSmall;
                //shootEffect = Fx.shootSmall;
            }};
        }});

        // —— 基础近战 AI：索敌贴近 + 砍刀横扫 ——
        aiController = () -> new RaiderAI();
    }
}
