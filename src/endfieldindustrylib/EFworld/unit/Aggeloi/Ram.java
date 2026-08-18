package endfieldindustrylib.EFworld.unit.Aggeloi;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import endfieldindustrylib.EFworld.ai.RamAI;
import endfieldindustrylib.EFworld.ai.RamCommandAI;
import mindustry.ai.ControlPathfinder;
import mindustry.ai.Pathfinder;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Unit;
import mindustry.type.Weapon;

public class Ram extends AuroraUnit{
    // —— 玩家操控自动追击（勾选设置里的 autotarget 时，自动向敌人移动） ——
    /** 玩家自动追击的索敌半径（世界单位） */
    public float autoChaseRange = 10f * 8f;
    /** 玩家自动追击的停止距离（世界单位，中心距：进入武器射程即停下交给武器自动索敌） */
    public float autoChaseStopRange = 27f;
    /** 玩家自动追击的速度倍率（相对普通移速） */
    public float autoChaseSpeedMul = 1f;
    /** 玩家自动追击的加速度（{@code vel.approachDelta} 速率） */
    public float autoChaseAccel = 2f;

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

        // —— 近战射程：指挥模式（CommandAI）的 engageRange = range - 10，需要为正数才会在
        //    敌人面前停下而非直冲中心（修复被指挥攻击时推着敌人走的问题） ——
        range = 28f;

        // —— 玩家/被指挥单位使用 RamCommandAI（修复追击索敌 + 触发重装冲锋）；
        //    AI 单位仍用 aiController（RamAI / HeavyramAI） ——
        controller = u -> !playerControllable || (u.team.isAI() && !u.team.rules().rtsAi) ? aiController.get() : new RamCommandAI();
    }

    /**
     * 玩家操控自动追击增强：勾选「自动追击」（设置里的 autotarget）且在自动射击时，
     * 自动向最近的敌人移动（近战追身），直到进入武器射程停下，交给武器 autoTarget 自动索敌挥击。
     * <p>
     * 仅影响玩家操控的 Ram / Heavyram；AI 单位（非玩家）不受影响。
     */
    @Override
    public void update(Unit unit){
        super.update(unit);

        if(unit.isPlayer() && unit.isShooting() && Core.settings.getBool("autotarget")){
            Unit enemy = Units.closestEnemy(unit.team, unit.x, unit.y, autoChaseRange, u -> u.checkTarget(false, true));
            if(enemy != null && !unit.within(enemy, autoChaseStopRange + enemy.hitSize * 0.5f)){
                // 向敌人追身移动（速度取单位普通移速；进入射程后由武器自动索敌开火）
                Tmp.v1.set(enemy).sub(unit).setLength(unit.speed() * autoChaseSpeedMul);
                unit.vel.approachDelta(Tmp.v1, autoChaseAccel);
                unit.lookAt(enemy);
            }
        }
    }

    // ==================== 三段式尾巴（B2：分节贴图） ====================
    /**
     * 尾巴三段贴图 region，对应文件：
     * assets/sprites/units/ram-tail-1.png（根部）、ram-tail-2.png（中段）、ram-tail-3.png（末梢）。
     * 贴图均朝上（+y）绘制，根部在图片下缘、末梢在上缘，与 ram.png 朝向约定一致。
     */
    private TextureRegion tail1, tail2, tail3;
    /** 尾巴各节长度（世界单位，根部→末梢；贴图两端有留白，各减 3f 补偿）。非 static：子类（重装拉姆）可各自覆写而不影响 Ram 本体 */
    protected float[] tailLens = {7f, 5f, 2f};
    /** 每一节在上一节基础上再偏转的角度（度），逐节累计形成一致弯曲。正/负即偏转方向，若弯向相反翻转此符号 */
    protected float tailBend = 4f;
    @Override
    public void load(){
        super.load();
        tail1 = Core.atlas.find(name + "-tail-1");
        tail2 = Core.atlas.find(name + "-tail-2");
        tail3 = Core.atlas.find(name + "-tail-3");
    }

    /** 身体下层先画三段式尾巴，再由身体贴图盖住尾根 */
    @Override
    public void drawBody(Unit unit){
        drawTail(unit);
        super.drawBody(unit);
    }

    /** 三段式尾巴：根部朝后，逐节累计弯曲 + 随时间自然摆动（不依赖速度/动能） */
    private void drawTail(Unit unit){
        float t = Time.time;
        float baseRot = unit.rotation + 180f;              // 正后方（度）

        // 根部整体轻微左右摆动（纯随时间）
        float baseAng = baseRot + Mathf.sin(t, 42f, 5f);

        // 尾根锚点：正后方，距中心 = 自身碰撞半径 + 2
        float tailBase = unit.type.hitSize * 0.5f + 1f;
        float px = unit.x + Angles.trnsx(baseRot, tailBase);
        float py = unit.y + Angles.trnsy(baseRot, tailBase);

        applyColor(unit);
        TextureRegion[] segs = {tail1, tail2, tail3};
        float ang = baseAng;
        for(int i = 0; i < segs.length; i++){
            TextureRegion r = segs[i];
            if(r == null || !Core.atlas.isFound(r)) continue;

            float segLen = tailLens[i];
            // 1) 静态累计弯曲：每一节在上一节基础上再偏转 tailBend 度，形成一致弯曲
            ang += tailBend * Mathf.degRad;
            // 2) 随时间自然摆动：各节同周期、相位逐节滞后 → 波浪自根部向末梢正确传播（逐节累计偏转）
            float amp = 0.12f + 3f * i;                 // 摆幅向末梢大幅放大（重点增强后两节）
            ang += Mathf.sin(t - i * 9f, 30f, amp);
            // 3) 轻微高频颤动：让末梢更灵动
            ang += Mathf.sin(t, 12f + 4f * i, amp * 2f);

            // 该节中点
            float mx = px + Angles.trnsx(ang, segLen * 0.5f);
            float my = py + Angles.trnsy(ang, segLen * 0.5f);

            // 高度按贴图宽高比缩放，保持贴图比例
            float h = segLen * (r.height / (float)Math.max(r.width, 1));
            Draw.rect(r, mx, my, segLen, h, ang - 90f);

            // 推进到该节末端，作为下一节根部
            px += Angles.trnsx(ang, segLen)*0.8f;
            py += Angles.trnsy(ang, segLen)*0.8f;
        }
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
}