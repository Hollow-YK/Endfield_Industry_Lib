package endfieldindustrylib.EFworld.unit.Aggeloi;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import mindustry.entities.Leg;
import mindustry.gen.EntityMapping;
import mindustry.gen.LegsUnit;

/**
 * Ram 专用足部实体 — 始终使用奔跑步态（两前腿一起迈出，步态略微凌乱）。
 * <p>
 * 引擎默认足类步态把腿按对角成对（前右+后左、前左+后右）移动，无法通过参数改成
 * “同侧前腿同组”。本实体在每帧引擎完成腿部计算后，把左右腿**绕单位朝向轴镜像同步**：
 * <ul>
 *   <li>前腿（3）镜像到前腿（0）→ 两前腿对称且同相位，一起迈出</li>
 *   <li>后腿（2）镜像到后腿（1）→ 两后腿一起迈出</li>
 * </ul>
 * 从而形成奔跑/疾驰（gallop）步态（无普通步态，始终奔跑）。
 * 每迈出一步时，两前脚之间会加入一个很小的随机间距（{@link #MAX_FRONT_GAP}），让步态更自然。
 */
public class RamLegsUnit extends LegsUnit {

    /**
     * 实体唯一注册 id：通过 {@link EntityMapping#register} 注册本类到实体映射，
     * 使存档/网络按此 id 反序列化回 {@code RamLegsUnit} 而非基类 {@code LegsUnit}。
     * 若不注册并覆盖 {@link #classId()}，重启存档后单位退化为普通 {@code LegsUnit}，
     * 失去左右镜像同相位步态，恢复引擎默认的“左右脚错开”对角步态。
     */
    public static final int ENTITY_ID = EntityMapping.register("endfield-industry-lib-ram-legs-unit", RamLegsUnit::new);

    @Override
    public int classId(){
        return ENTITY_ID;
    }

    /** 两前脚迈出的最大随机间距（世界单位），每步重新随机 */
    private static final float MAX_FRONT_GAP = 1.0f;

    /** 当前两前脚间距（沿运动方向偏移，世界单位；负=靠后） */
    private float frontGap = 0f;
    /** 上一帧前腿相位，用于检测新的一步 */
    private float lastStage = 0f;

    @Override
    public void update(){
        super.update();

        if(legs().length == 4){
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

    public static RamLegsUnit create(){
        return new RamLegsUnit();
    }
}
