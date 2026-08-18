package endfieldindustrylib.EFworld.unit.Aggeloi;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import endfieldindustrylib.EFworld.ai.StingAI;
import mindustry.ai.ControlPathfinder;
import mindustry.ai.Pathfinder;
import mindustry.content.Fx;
import mindustry.entities.Leg;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.LegsUnit;
import mindustry.gen.Legsc;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Weapon;

/**
 * 刺蝎（Sting）—— 蝎子形四足远程单位。
 * <p>
 * <ul>
 *   <li>四条腿：前腿（index 0、3）与后腿（index 1、2）使用不同贴图，引擎默认对角交替步态（蝎子式爬行）</li>
 *   <li>武器：蝎尾炮台（位于尾部，独立旋转的远程炮台，可对空/对地）</li>
 *   <li>cell：极光光环，与 Ram 一致（继承 {@link AuroraUnit}）</li>
 *   <li>AI：远程索敌走位，射程外推进、贴脸后退、射程内原地射击（见 {@link StingAI}）</li>
 * </ul>
 */
public class Sting extends AuroraUnit{

    // —— 前/后腿贴图（前腿 index 0、3；后腿 index 1、2） ——
    private TextureRegion legFront, legBack, footFront, footBack;
    /** 腿基座临时向量（父类 UnitType 的私有 static 不可访问，此处自备一个） */
    private static final Vec2 legOffset = new Vec2();

    public Sting(String name){
        super(name);

        // —— 基础属性 ——
        health = 300f;
        speed = 1.1f;
        hitSize = 10f;
        armor = 2f;
        drag = 0.4f;
        accel = 0.25f;
        rotateSpeed = 3f;
        // —— 地面单位 ——
        flying = false;
        physics = true;                 // 启动物理碰撞
        hovering = false;               // 不悬浮，受地面影响
        canDrown = true;                // 可在深水中淹死
        canBoost = false;               // 不能起飞
        omniMovement = true;            // 可原地转向
        isEnemy = true;
        drawBody = true;
        drawCell = true;
        drawItems = true;
        targetAir = true;               // 蝎尾炮台可对空
        allowLegStep = false;
        alwaysCreateOutline = false;
        outlines = false;               // 不自动生成描边：武器（蝎尾炮台）与身体都不绘制描边区域
        // —— 足部：四足，引擎默认对角交替步态（蝎子式爬行，无需像 Ram 那样镜像成奔跑） ——
        legCount = 4;
        legGroupSize = 2;
        legLength = 5f;
        legBaseOffset = 3f;
        legExtension = -1f;
        legMoveSpace = 1.5f;
        legForwardScl = 1.2f;
        legSpeed = 0.15f;
        legLengthScl = 1f;
        legPairOffset = 0.05f;

        // —— 武器：蝎尾炮台（位于尾部，独立旋转远程攻击） ——
        weapons.add(new Weapon("endfield-industry-lib-sting-tail"){{
            mirror = false;            // 单个炮台，不镜像成对
            top = true;               // 绘制在主体之上

            layerOffset = 0.02f;
            rotate = true;             // 独立旋转的炮台，可朝任意方向射击
            rotateSpeed = 6f;          // 炮塔旋转速度（较慢，默认 20）
            x = 0f; y = -6f;          // 位于尾部（负 y = 正后方）
            shootY = 8f;               // 炮口（毒针）长度
            recoil = -1f;               // 开火后坐
            shake = 1f;
            reload = 45f;              // 射击间隔（帧）
            shootCone = 30f;           // 瞄准锥形
            bullet = new BasicBulletType(4f, 22f){{   // BasicBulletType：带子弹贴图（默认 "bullet"，修复子弹贴图丢失）
                lifetime = 24f;        // 射程 = 4*24 = 96（12 格），与 AI 的 12 格交战距离匹配
                drag = 0f;
                collides = true;
                collidesAir = true;    // 可对空
                collidesGround = true;
                hittable = true;
                hitEffect = Fx.hitBulletSmall;
                shootEffect = Fx.shootSmall;
                smokeEffect = Fx.shootSmallSmoke;
                knockback = 0.8f;
            }};
        }});

        constructor = LegsUnit::create;  // 标准四足实体（对角交替步态）
        aiController = () -> new StingAI();
    }

    /** 前/后腿贴图：缺失时回退到引擎默认单套贴图，保证即使未画图也不崩 */
    @Override
    public void load(){
        super.load();
        legFront = Core.atlas.find(name + "-leg-front", legRegion);
        legBack = Core.atlas.find(name + "-leg-back", legRegion);
        // 无 foot-front/back 变体：回退到单一 footRegion（sting-foot）
        footFront = Core.atlas.find(name + "-foot-front", footRegion);
        footBack = Core.atlas.find(name + "-foot-back", footRegion);
    }

    /** 前/后腿使用不同贴图（前腿 index 0、最后一只；其余为后腿），其余绘制逻辑与引擎默认一致 */
    @Override
    public <T extends Unit & Legsc> void drawLegs(T unit){
        applyColor(unit);
        Tmp.c3.set(Draw.getMixColor());

        Leg[] legs = unit.legs();
        float rotation = unit.baseRotation();
        float invDrown = 1f - unit.drownTime;

        // 阴影先全部绘制（足影画在缩短后的半长终点）
        for(int j = legs.length - 1; j >= 0; j--){
            int i = (j % 2 == 0 ? j/2 : legs.length - 1 - j/2);
            Leg leg = legs[i];
            TextureRegion foot = isFront(i, legs.length) ? footFront : footBack;
            if(foot.found()){
                Vec2 position = unit.legOffset(legOffset, i).add(unit);
                Vec2 end = Tmp.v1.set(leg.base).sub(position).scl(0.5f).add(position);
                Drawf.shadow(end.x, end.y, foot.width * foot.scl() * 1.5f, invDrown);
            }
        }

        //legs are drawn front first
        for(int j = legs.length - 1; j >= 0; j--){
            int i = (j % 2 == 0 ? j/2 : legs.length - 1 - j/2);
            Leg leg = legs[i];
            boolean flip = i >= legs.length/2f;
            int flips = Mathf.sign(flip);

            // 前/后腿贴图区分
            boolean front = isFront(i, legs.length);
            TextureRegion foot = front ? footFront : footBack;
            TextureRegion legTex = front ? legFront : legBack;

            Vec2 position = unit.legOffset(legOffset, i).add(unit);

            // 单关节腿：髋→足中点为终点（腿缩短一倍、去掉膝关节）
            Vec2 end = Tmp.v1.set(leg.base).sub(position).scl(0.5f).add(position);

            if(foot.found() && leg.moving && shadowElevation > 0){
                float scl = shadowElevation * invDrown;
                float elev = Mathf.slope(1f - leg.stage) * scl;
                Draw.color(Pal.shadow);
                Draw.rect(foot, end.x + shadowTX * elev, end.y + shadowTY * elev, position.angleTo(leg.base));
                Draw.color();
            }

            Draw.mixcol(Tmp.c3, Tmp.c3.a);

            // 腿：单段（髋→end）
            Lines.stroke(legTex.height * legTex.scl() * flips);
            Lines.line(legTex, position.x, position.y, end.x, end.y, false);

            // 足部画在腿之上（体现蝎子反关节）
            if(foot.found()){
                Draw.rect(foot, end.x, end.y, position.angleTo(leg.base));
            }
        }

        //base joints are drawn after everything else
        if(baseJointRegion.found()){
            for(int j = legs.length - 1; j >= 0; j--){
                Vec2 position = unit.legOffset(legOffset, (j % 2 == 0 ? j/2 : legs.length - 1 - j/2)).add(unit);
                Draw.rect(baseJointRegion, position.x, position.y, rotation);
            }
        }

        // base 贴图实为前肢肩胛骨 → 在两条前腿髋部各画一块（左右镜像），
        // 并随前肢前后摆动微微转动（lean = 前肢相对身体夹角 × 35%）
        if(baseRegion.found()){
            for(int j = legs.length - 1; j >= 0; j--){
                int i = (j % 2 == 0 ? j/2 : legs.length - 1 - j/2);
                if(!isFront(i, legs.length)) continue;
                Leg leg = legs[i];
                Vec2 position = unit.legOffset(legOffset, i).add(unit);
                boolean flip = i >= legs.length/2f;
                float flips = Mathf.sign(flip);

                // 前肢上段相对身体朝向的摆角（有符号 [-180,180)），取一小部分做"微微"跟随
                float legAngle = position.angleTo(leg.joint);
                float lean = (legAngle - rotation + 540f) % 360f - 180f;
                float prev = Draw.xscl;
                Draw.xscl = flips;   // 左右镜像
                Draw.rect(baseRegion, position.x, position.y, rotation - 90 + lean * 0.35f);
                Draw.xscl = prev;
            }
        }

        Draw.reset();
    }

    /** 前腿判定：index 0 与最后一只（3）为前腿，其余（1、2）为后腿 */
    private boolean isFront(int i, int count){
        return i == 0 || i == count - 1;
    }

    /**
     * 撤销 UnitType.init() 的自动设置：constructor 为 LegsUnit 时 init() 会强制
     * allowLegStep=true，使碰撞使用 legSolid（可翻越/穿过玩家建造的墙），导致穿墙。
     * 此处恢复为普通地面碰撞，并同步寻路成本（与 Ram 相同）。
     */
    @Override
    public void init(){
        super.init();

        // 腿部单位碰撞使用 legSolid（对玩家建造的墙返回可通行），改回普通地面碰撞（solid）
        allowLegStep = false;
        // 同步寻路成本为普通地面：StingAI 基于 GroundAI 使用 costGround 流场寻路绕墙
        flowfieldPathType = Pathfinder.costGround;
        pathCost = ControlPathfinder.costGround;
        pathCostId = ControlPathfinder.costTypes.indexOf(pathCost);
        if(pathCostId == -1) pathCostId = 0;
    }
    @Override
    public void drawCell(Unit unit){
        float t = Time.time;
        // 血量因子（1=满血，0=残血）：决定光带明暗/强弱
        float hf = Mathf.clamp(unit.healthf());
        // 每单位固定随机相位（弧度），避免所有个体完全同步
        float ph = Mathf.randomSeed(unit.id, 360f) * Mathf.degRad;

        // 阵营色（血量通过透明度/呼吸体现，保留血量与阵营指示）
        applyColor(unit);
        Color tc = cellColorBright(unit);   // 提亮后的队色：暗色（尤其红色）更醒目

        // 光带中心：整体朝单位朝向（向前）偏移 2 像素，并绕该点微微飘动
        float bx = unit.x + Angles.trnsx(unit.rotation, 1.5f) + Mathf.sin(t, 40f, 0.1f);
        float by = unit.y + Angles.trnsy(unit.rotation, 1.5f) + Mathf.absin(t, 55f, 0.1f);
        float R = 2.8f;
        float dmg = 1f - hf;
        float alpha = hf+(0.5f*dmg)+Mathf.sin(t, 30f*hf, 0.1f+0.5f*dmg);
        // —— 泛光底晕（略大于光带，不喧宾夺主） ——
        Tmp.c2.set(tc).a(alpha * 0.06f);
        Fill.light(bx, by, 24, R * 1.3f, Tmp.c2, Color.clear);  

        // —— 极光光带：单条随角度波动、缓缓流转的发光带（外层柔光 + 内层亮芯，加法混合泛光） ——
        Draw.blend(Blending.additive);
        // 外层柔光
        Draw.color(tc.r, tc.g, tc.b, alpha * 0.18f);
        Lines.stroke(2.2f+Mathf.sin(t,30f,0.6f));
        drawBand(bx, by, R, t, ph, unit.rotation);
        // 中层
        Draw.color(tc.r, tc.g, tc.b, alpha * 0.34f);
        Lines.stroke(1.5f);
        drawBand(bx, by, R, t, ph, unit.rotation);
        // 内层亮芯
        Draw.color(tc.r, tc.g, tc.b, alpha * 0.55f);
        Lines.stroke(0.5f);
        drawBand(bx, by, R, t, ph, unit.rotation);
        Draw.blend();
        Draw.reset();
    }
}
