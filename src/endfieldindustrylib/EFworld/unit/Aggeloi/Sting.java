package endfieldindustrylib.EFworld.unit.Aggeloi;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import endfieldindustrylib.EFworld.ai.StingAI;
import mindustry.ai.ControlPathfinder;
import mindustry.ai.Pathfinder;
import mindustry.content.Fx;
import mindustry.entities.Leg;
import mindustry.entities.bullet.BulletType;
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
    private TextureRegion legFront, legBack, legBaseFront, legBaseBack, footFront, footBack;
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
            top = true;                // 绘制在主体之上
            layerOffset = 0.02f;
            rotate = true;             // 独立旋转的炮台，可朝任意方向射击
            x = 0f; y = -11f;          // 位于尾部（负 y = 正后方）
            shootY = 8f;               // 炮口（毒针）长度
            recoil = 2f;               // 开火后坐
            shake = 1f;
            reload = 45f;              // 射击间隔（帧）
            shootCone = 30f;           // 瞄准锥形
            bullet = new BulletType(4.5f, 22f){{
                lifetime = 34f;        // 射程 ≈ 4.5*34 ≈ 153（约 19 格），与 AI 的 15 格交战距离匹配
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
        legBaseFront = Core.atlas.find(name + "-leg-base-front", legBaseRegion);
        legBaseBack = Core.atlas.find(name + "-leg-base-back", legBaseRegion);
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

        // 阴影先全部绘制
        for(int j = legs.length - 1; j >= 0; j--){
            int i = (j % 2 == 0 ? j/2 : legs.length - 1 - j/2);
            Leg leg = legs[i];
            TextureRegion foot = isFront(i, legs.length) ? footFront : footBack;
            if(foot.found()){
                Drawf.shadow(leg.base.x, leg.base.y, foot.width * foot.scl() * 1.5f, invDrown);
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
            TextureRegion legBaseTex = front ? legBaseFront : legBaseBack;

            Vec2 position = unit.legOffset(legOffset, i).add(unit);

            Tmp.v1.set(leg.base).sub(leg.joint).inv().setLength(legExtension);

            if(foot.found() && leg.moving && shadowElevation > 0){
                float scl = shadowElevation * invDrown;
                float elev = Mathf.slope(1f - leg.stage) * scl;
                Draw.color(Pal.shadow);
                Draw.rect(foot, leg.base.x + shadowTX * elev, leg.base.y + shadowTY * elev, position.angleTo(leg.base));
                Draw.color();
            }

            Draw.mixcol(Tmp.c3, Tmp.c3.a);

            if(foot.found()){
                Draw.rect(foot, leg.base.x, leg.base.y, position.angleTo(leg.base));
            }

            if(legBaseUnder){
                Lines.stroke(legBaseTex.height * legTex.scl() * flips);
                Lines.line(legBaseTex, leg.joint.x + Tmp.v1.x, leg.joint.y + Tmp.v1.y, leg.base.x, leg.base.y, false);

                Lines.stroke(legTex.height * legTex.scl() * flips);
                Lines.line(legTex, position.x, position.y, leg.joint.x, leg.joint.y, false);
            }else{
                Lines.stroke(legTex.height * legTex.scl() * flips);
                Lines.line(legTex, position.x, position.y, leg.joint.x, leg.joint.y, false);

                Lines.stroke(legBaseTex.height * legTex.scl() * flips);
                Lines.line(legBaseTex, leg.joint.x + Tmp.v1.x, leg.joint.y + Tmp.v1.y, leg.base.x, leg.base.y, false);
            }

            if(jointRegion.found()){
                Draw.rect(jointRegion, leg.joint.x, leg.joint.y);
            }
        }

        //base joints are drawn after everything else
        if(baseJointRegion.found()){
            for(int j = legs.length - 1; j >= 0; j--){
                Vec2 position = unit.legOffset(legOffset, (j % 2 == 0 ? j/2 : legs.length - 1 - j/2)).add(unit);
                Draw.rect(baseJointRegion, position.x, position.y, rotation);
            }
        }

        if(baseRegion.found()){
            Draw.rect(baseRegion, unit.x, unit.y, rotation - 90);
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
}
