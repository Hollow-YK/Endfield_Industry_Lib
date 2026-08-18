package endfieldindustrylib.EFworld.unit.Landbreakers;

import arc.graphics.Color;
import endfieldindustrylib.EFworld.ai.AmbusherAI;
import mindustry.content.Fx;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.MechUnit;
import mindustry.type.UnitType;
import mindustry.type.Weapon;

/**
 * 伏击者（Ambusher）—— Landbreakers 人形族的 T1 远程单位。
 * <p>
 * 人形（{@link MechUnit}）弩手：行动敏捷、血量偏脆，以弩箭在远距离持续输出。
 * 拥有"极其智慧"的战术 AI（见 {@link AmbusherAI}）：
 * <ul>
 *   <li><b>换弹时寻找掩体</b>：武器装填期间躲到能遮挡敌人视线的墙/建筑后，避免暴露挨打</li>
 *   <li><b>被近身后逃离</b>：敌人贴身时立即向远离方向撤退，边退边架弩还击</li>
 * </ul>
 * 走路线 A（引擎原生 Mech 人形机甲）：两腿交替迈步 + 身体随步伐摆动，无需多帧腿部动画。
 */
public class Ambusher extends UnitType{
  
    public Ambusher(String name){
        super(name);
        // —— 基础属性：敏捷脆皮远程（T1） ——
        health = 180f;
        speed = 0.5f;
        hitSize = 8f;
        armor = 0f;
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
        targetAir = true;               // 弩箭可对空

        // —— 人形机甲（Mech）参数 ——
        constructor = MechUnit::create;
        mechSideSway = 0.5f;            // 行走左右摆动
        mechFrontSway = 0.3f;           // 行走前后起伏
        baseRotateSpeed = 9f;           // 身体转向较快（人形灵敏）；mechStride 由引擎按 hitSize 自动计算

        // —— 武器：弩（远程，装填较慢 → 触发"换弹找掩体"战术） ——
        weapons.add(new Weapon("endfield-industry-lib-ambusher-crossbow"){{
            mirror = false;            // 单个弩，不镜像成对
            top = true;
            layerOffset = 0.02f;       // 绘制在身体之上
            rotate = false;             // 独立旋转瞄准（人形举弩）
            x = 3f; y = 3f;            // 手持位置（身体右前方）
            shootY = 6f;               // 弩口
            reload = 180f;             // 装填较慢 → AI 装填时找掩体
            recoil = 2.5f;             // 弩后坐
            shake = 1f;
            shootCone = 6f;            // 射击锥角收窄：rotate=false 弹道=准星方向+身体偏角，锥角大→移动中乱射偏 25° 打不中；收窄后先对准再射
            bullet = new BasicBulletType(5f, 25f){{
                lifetime = 27f;        // 射程 ≈ 5*27 = 135（约 16.9 格），略大于 AI 的 16 格交战距离（确保 16 格处命中）
                width = 3f;            // 更细
                height = 12f;          // 更长 → 细长箭矢
                drag = 0f;
                keepVelocity = false;  // 不继承本体移动动量：走位/闪避时开火弹道不被带偏（打得更准）
                collides = true;
                collidesAir = true;    // 可对空
                collidesGround = true;
                hittable = true;
                pierce = false;        // 单目标弩箭
                hitEffect = Fx.hitBulletSmall;
                shootEffect = Fx.shootSmall;
                smokeEffect = Fx.shootSmallSmoke;
                knockback = 0.5f;
                trailWidth = 2f;       // 箭矢拖尾
                trailLength = 8;       // 箭矢拖尾长度（帧）
                // —— 泛红配色：子弹/拖尾呈红渐变，非刺眼纯红 ——
                frontColor = Color.red;             // 箭矢头部亮红
                backColor = Color.scarlet;          // 箭矢尾部深红 → 渐变泛红
                trailColor = Color.scarlet.cpy().a(0.5f); // 拖尾泛红（半透明）
            }};
        }});

        // —— 战术 AI：换弹找掩体 + 被近身逃离 ——
        aiController = () -> new AmbusherAI();

        // ⚠️ 临时调试开关：开启地图悬浮文本（状态/目标/寻路路径）+ 终端计划日志（调试完删除此行）
        AmbusherAI.debug = true;
    }
}
