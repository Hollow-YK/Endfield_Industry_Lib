package endfieldindustrylib.EFworld.unit.Aggeloi;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import static mindustry.Vars.headless;
import mindustry.content.Fx;
import mindustry.entities.Leg;
import mindustry.gen.EntityMapping;
import mindustry.gen.LegsUnit;
import mindustry.world.blocks.environment.Floor;

/**
 * 重装拉姆（Heavyram）专用足部实体 — 支持「普通走路 + 冲锋疾驰 + 蓄力磨地」三种足部状态。
 * <p>
 * 引擎默认足类步态为对角步态（腿按对角成对移动）。{@link RamLegsUnit} 通过每帧在引擎计算后
 * 将左右腿绕单位朝向轴镜像同步，形成「始终疾驰（gallop）」的步态；本实体在 {@link #sprinting}
 * 为 {@code true}（由 {@code HeavyramAI} 冲锋时置位）才启用该镜像，形成冲锋奔跑步态；
 * 在 {@link #pawing} 为 {@code true}（由 {@code HeavyramAI} 蓄力时置位）时播放公牛式
 * 单腿摩擦地面的蓄力动作；否则保持引擎默认的普通走路步态。
 */
public class HeavyramLegsUnit extends LegsUnit {

    /**
     * 实体唯一注册 id：通过 {@link EntityMapping#register} 注册本类到实体映射，
     * 使存档/网络按此 id 反序列化回 {@code HeavyramLegsUnit} 而非基类 {@code LegsUnit}。
     * （参见 {@link RamLegsUnit#ENTITY_ID} 说明）
     */
    public static final int ENTITY_ID = EntityMapping.register("endfield-industry-lib-heavyram-legs-unit", HeavyramLegsUnit::new);

    /** 冲锋（奔跑步态）标志：由 HeavyramAI 在冲锋期间置 true，结束后置 false */
    public boolean sprinting = false;
    /** 蓄力（公牛式磨地）标志：由 HeavyramAI 在蓄力期间置 true，冲锋/结束后置 false */
    public boolean pawing = false;

    /** 两前脚迈出的最大随机间距（世界单位），每步重新随机 */
    private static final float MAX_FRONT_GAP = 1.0f;
    /** 蓄力磨地的刮蹭摆动周期（帧） */
    private static final float pawPeriod = 26f;
    /** 蓄力磨地的刮蹭幅度（世界单位）：较小幅度，避免摩擦行程过长显得夸张 */
    private static final float pawAmplitude = 1.5f;

    /** 当前两前脚间距（沿运动方向偏移，世界单位；负=靠后） */
    private float frontGap = 0f;
    /** 上一帧前腿相位，用于检测新的一步 */
    private float lastStage = 0f;
    /** 蓄力磨地扬尘计时（帧） */
    private float pawDustTimer = 0f;

    @Override
    public int classId(){
        return ENTITY_ID;
    }

    @Override
    public void update(){
        super.update();

        if(legs().length != 4) return;

        if(sprinting){
            // —— 冲锋：疾驰（gallop）步态 ——
            // 后腿：严格同相位镜像（一起迈出）
            mirrorLeg(1, 2);
            // 前腿：镜像配对后，在两前脚之间加入很小的随机间距（每步重掷）
            mirrorLeg(0, 3);

            if(moving()){
                // 检测前腿进入新一步（phase 回绕）→ 重新随机两前脚间距，使步态略微凌乱
                if(legs()[0].stage < lastStage){
                    frontGap = Mathf.random(-MAX_FRONT_GAP, MAX_FRONT_GAP);
                }
                lastStage = legs()[0].stage;

                // 沿运动方向把左前腿（3）略微前/后移，形成轻微错开
                if(frontGap != 0f){
                    float dir = baseRotation();
                    float dx = Mathf.cosDeg(dir) * frontGap, dy = Mathf.sinDeg(dir) * frontGap;
                    legs()[3].base.add(dx, dy);
                    legs()[3].joint.add(dx, dy);
                }
            }
        }else if(pawing && !moving()){
            // —— 蓄力：公牛式单腿摩擦地面 ——
            pawGround();
        }else{
            // —— 普通走路：保持引擎默认对角步态 ——
            // 同步相位，避免切入冲锋时因相位跳变出现"滑步"
            lastStage = legs()[0].stage;
        }
    }

    /**
     * 蓄力时的公牛式单腿摩擦地面：前脚（0）沿身体朝向轴来回刮蹭，并间歇扬起尘土，
     * 营造出公牛冲锋前刨蹄蓄力的观感。
     */
    private void pawGround(){
        float t = Time.time;
        // 沿身体朝向轴的刮蹭位移（来回摆动）
        float s = Mathf.sin(t, pawPeriod, pawAmplitude);
        float dir = rotation;
        float dx = Mathf.cosDeg(dir) * s, dy = Mathf.sinDeg(dir) * s;

        Leg leg = legs()[0];
        leg.base.add(dx, dy);
        leg.joint.add(dx * 0.5f, dy * 0.5f);

        // 摩擦扬尘（约每 0.1 秒一团）
        if(!headless && (pawDustTimer += Time.delta) >= 6f){
            pawDustTimer = 0f;
            Floor floor = floorOn();
            float scale = type.hitSize / 8f;
            if(floor != null){
                floor.walkEffect.at(leg.base.x, leg.base.y, scale, floor.mapColor);
            }else{
                Fx.unitLandSmall.at(leg.base.x, leg.base.y, scale, team.color);
            }
        }
    }

    /** 把 to 腿（左侧）镜像为 from 腿（右侧）绕单位朝向轴的对称位姿，并同步相位 */
    private void mirrorLeg(int from, int to){
        Leg[] legArr = legs();
        float t = 2f * baseRotation() * Mathf.degRad;
        float cos = Mathf.cos(t), sin = Mathf.sin(t);

        mirrorVec(legArr[from].base, legArr[to].base, cos, sin);
        mirrorVec(legArr[from].joint, legArr[to].joint, cos, sin);

        legArr[to].moving = legArr[from].moving;
        legArr[to].stage = legArr[from].stage;
        legArr[to].group = legArr[from].group;
    }

    /** 将 src 相对单位中心的偏移绕朝向轴反射，写入 dst */
    private void mirrorVec(Vec2 src, Vec2 dst, float cos, float sin){
        float dx = src.x - x, dy = src.y - y;
        dst.set(x + dx * cos + dy * sin, y + dx * sin - dy * cos);
    }

    public static HeavyramLegsUnit create(){
        return new HeavyramLegsUnit();
    }
}
