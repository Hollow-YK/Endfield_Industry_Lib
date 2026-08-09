package endfieldindustrylib.EFworld.unit;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import endfieldindustrylib.EFworld.ai.RamAI;
import mindustry.ai.ControlPathfinder;
import mindustry.ai.Pathfinder;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.type.Weapon;

public class Ram extends UnitType{
    public Ram(String name) {
        super(name);
        // —— 基础属性 ——
        health = 240f;
        speed = 1.5f;
        hitSize = 8f;
        armor = 0f;
        drag = 0.4f;
        accel = 0.3f;
        rotateSpeed = 3f;
        // —— 地面单位 ——
        flying = false;
        physics = true;                 // 启动物理碰撞
        hovering = false;               // 不悬浮，受地面影响
        canDrown = true;                // 可在深水中淹死
        canBoost = false;               // 不能起飞
        omniMovement = true;             // 可原地转向：无需前进即可转头对准目标
        isEnemy = true;
        drawBody = true;
        drawCell = true;
        drawItems = true;
        targetAir = false;
        allowLegStep = false;
        alwaysCreateOutline = false;
        // —— 足部：四足奔跑步态（无普通步态，始终奔跑；Ram 为小型单位，腿短贴体、不外延） ——
        legCount = 4;                   // 四足
        legGroupSize = 2;               // 对角腿一组（类四足动物奔跑）
        legLength = 4f;                 // 腿短，贴体不向外延展
        legBaseOffset = 2f;
        legExtension = -1f;
        legMoveSpace = 8f;              // 步幅（奔跑档）
        legForwardScl = 1.3f;           // 腿部向前伸展更充分
        legSpeed = 0.2f;                // 腿部到位更快（奔跑滞空短）
        legLengthScl = 1f;
        legPairOffset = 0.05f;             // 前后腿组由 RamLegsUnit 镜像配对（两前腿/两后腿各一组），引擎不再按腿索引错开相位

        // —— 武器：雷兽式前颚近战（位于正前方） ——
        weapons.add(new Weapon("endfield-industry-lib-ram-mandible"){{
            mirror = false;            // 单个武器，不镜像成对
            top = true;          
            layerOffset = -0.02f;      // 负 z 偏移：把武器贴图压到主体贴图之下（仅 top=false 只影响轮廓，
               // 本体贴图仍画在身体之上，必须用负 layerOffset 才能真正置于主体下方）
            x = 0f; y = 7f;            // 向前偏移 5 像素，避免被身体贴图盖住
            shootY = 7f;               // 挥击点位于单位正前方
            rotate = false;            // 固定朝前，不独立转向
            controllable = false;      // 不可手动瞄准
            autoTarget = true;         // 自动索敌
            reload = 60f;              // 挥击频率（帧）
            shootCone = 60f;           // 前方锥形触发范围
            recoil = -1f;
            shake = 0f;
            bullet = new BulletType(0f, 0f){{
                speed = 0f;
                lifetime = 1f;
                instantDisappear = true;   // 立即挥击
                splashDamage = 24f;        // 前颚挥击伤害
                splashDamageRadius = 28f;  // 挥击范围
                collidesAir = false;       // 仅攻击地面目标
                collidesGround = true;
                hittable = false;
                rangeOverride = 8f;       // 攻击/发现距离（与 AI 的 15 格索敌一致，避免远处空挥）
                //hitEffect = Fx.hitBulletSmall;
                //shootEffect = Fx.shootSmall;
            }};
        }});

        constructor = RamLegsUnit::create;   // 始终奔跑步态（两前腿一起迈出）

        // —— 始终奔跑的数值移动模式（见 EFworld.ai.RamAI） ——
        aiController = () -> new RamAI();
    }

    /**
     * 撤销 UnitType.init() 的自动设置：因 constructor 为 LegsUnit，init() 会强制
     * allowLegStep=true，使碰撞使用 legSolid（腿部单位可翻越/穿过玩家建造的墙），
     * 导致 Ram 穿墙。此处恢复为普通地面碰撞，并同步寻路成本，使 Ram 与所有墙体碰撞。
     */
    @Override
    public void init(){
        super.init();

        // 腿部单位碰撞使用 legSolid（对玩家建造的墙返回可通行），改回普通地面碰撞（solid）
        allowLegStep = false;
        // 同步寻路成本为普通地面：RamAI 基于 GroundAI 使用 costGround 流场寻路绕墙，
        // 若保留 costLegs 会认为墙可通行而规划"穿墙"路径
        flowfieldPathType = Pathfinder.costGround;
        pathCost = ControlPathfinder.costGround;
        pathCostId = ControlPathfinder.costTypes.indexOf(pathCost);
        if(pathCostId == -1) pathCostId = 0;
    }

    /** 极光光环 cell：一条微微飘动的泛光极光光带，同时保留血量与阵营显示 */
    @Override
    public void drawCell(Unit unit){
        float t = Time.time;
        // 血量因子（1=满血，0=残血）：决定光带明暗/强弱
        float hf = Mathf.clamp(unit.healthf());
        // 每单位固定随机相位（弧度），避免所有个体完全同步
        float ph = Mathf.randomSeed(unit.id, 360f) * Mathf.degRad;

        // 阵营色（血量通过透明度/呼吸体现，保留血量与阵营指示）
        applyColor(unit);
        Color tc = unit.team.color;

        // 光带中心：整体朝单位朝向（向前）偏移 2 像素，并绕该点微微飘动
        float bx = unit.x + Angles.trnsx(unit.rotation, 2.3f) + Mathf.sin(t, 40f, 0.1f);
        float by = unit.y + Angles.trnsy(unit.rotation, 2.3f) + Mathf.absin(t, 55f, 0.1f);
        float R = 3f;
        float dmg = 1f - hf;
        float alpha = hf+(0.5f*dmg)+Mathf.sin(t, 30f*hf, 0.1f+0.5f*dmg);
        // —— 泛光底晕（略大于光带，不喧宾夺主） ——
        Tmp.c2.set(tc).a(alpha * 0.12f);
        Fill.light(bx, by, 24, R * 1.3f, Tmp.c2, Color.clear);

        // —— 极光光带：单条随角度波动、缓缓流转的发光带（外层柔光 + 内层亮芯，加法混合泛光） ——
        Draw.blend(Blending.additive);
        // 外层柔光
        Draw.color(tc.r, tc.g, tc.b, alpha * 0.12f);
        Lines.stroke(2.2f+Mathf.sin(t,30f,0.6f));
        drawBand(bx, by, R, t, ph, unit.rotation);
        // 中层
        Draw.color(tc.r, tc.g, tc.b, alpha * 0.60f);
        Lines.stroke(1.5f);
        drawBand(bx, by, R, t, ph, unit.rotation);
        // 内层亮芯
        Draw.color(tc.r, tc.g, tc.b, alpha * 0.95f);
        Lines.stroke(0.5f);
        drawBand(bx, by, R, t, ph, unit.rotation);
        Draw.blend();
        Draw.reset();
    }

    /** 绘制一条四瓣波动的闭合极光光带，随单位朝向旋转，相位周期左右扭动 */
    private void drawBand(float cx, float cy, float R, float t, float ph, float rot){
        Lines.beginLine();
        int n = 36;
        // 四瓣波纹；相位做周期性左右摆动（不持续旋转）
        float wiggle = ph + Mathf.sin(t, 90f, 0.9f);
        for(int i = 0; i <= n; i++){
            float a = i / (float)n * Mathf.PI2;
            // 半径沿角度呈 4 段波纹（四瓣花）；整体绕圆心随单位朝向旋转，随时间左右扭动
            float rr = R * (1f + 0.16f * Mathf.sin(a * 4f + wiggle));
            float ang = rot + a * Mathf.radDeg;
            Lines.linePoint(cx + Angles.trnsx(ang, rr), cy + Angles.trnsy(ang, rr));
        }
        Lines.endLine();
    }
}