package endfieldindustrylib.EFworld.unit.Aggeloi;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import endfieldindustrylib.EFworld.ai.HeavyramAI;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.part.RegionPart;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.type.Weapon;

/**
 * 重装拉姆（Heavyram）——拉姆（Ram）的 T2 重装升级版。
 * <p>
 * 继承 {@link Ram} 的全部渲染与行为逻辑（三段式尾巴、极光 cell、init 中的地面碰撞修复等），
 * 仅在其基础上加强数值，并重构移动 / 攻击方式：
 * <ul>
 *   <li>血量 / 护甲大幅提升、体型更大 → 标准重装定位</li>
 *   <li>移速 / 转身 / 加速度更慢 → 重装应有的迟钝感</li>
 *   <li><b>普通走路步态</b>：改用 {@link HeavyramLegsUnit}（默认走路对角步态），四足依次迈步</li>
 *   <li><b>冲锋技能</b>：{@link HeavyramAI} 周期性触发，切换奔跑步态（gallop）高速冲到敌人面前</li>
 *   <li>前颚挥击更重：伤害、范围更大，但挥击频率更低</li>
 *   <li><b>可旋转武器</b>：射界 150°（{@code rotationLimit} 相对基座 ±75°），无需转向即可侧击</li>
 *   <li>尾巴加长，与更大体型相称</li>
 * </ul>
 */
public class Heavyram extends Ram{

    public Heavyram(String name){
        // 先按 Ram（T1）初始化全部基础逻辑（含三段式尾巴、极光 cell、RamLegsUnit 足部、RamAI）
        super(name);

        // —— 基础属性：重装加强 ——
        health = 520f;              // T1 240 → 重装坦度（约 2.2 倍）
        speed = 1.2f;              // T1 1.5  → 更慢
        hitSize = 16f;              // T1 8    → 体型更大
        armor = 8f;                 // T1 0    → 重装甲
        drag = 0.5f;                // T1 0.4  → 更重
        accel = 0.2f;               // T1 0.3  → 加速更慢
        rotateSpeed = 2.4f;         // T1 3    → 转身更慢

        // —— 足部：体型更大 → 腿更长、更外展，默认普通走路步态 ——
        legLength = 7f;             // T1 4    → 腿更长，足部更靠外
        legBaseOffset = 5f;         // T1 2    → 髋部基座更靠外，四肢向外展开
        legExtension = -1.5f;       // T1 -1
        legMoveSpace = 2f;          // 走路步幅（较小；Ram 的 8f 为奔跑档）
        legForwardScl = 1.05f;      // 走路前伸幅度适中
        legSpeed = 0.12f;           // 步伐沉稳（重装迟钝感）
        legGroupSize = 1;           // 四足依次迈步 → 普通走路步态（区别于 Ram 的奔跑）
        legLengthScl = 1f;
        legPairOffset = 0.05f;

        // —— 尾巴：与更大体型相称的加长尾 ——
        tailLens[0] = 7f;           // T1 7    → 尾根加长
        tailLens[1] = 5f;           // T1 5    → 中段加长
        tailLens[2] = 2f;           // T1 2    → 末梢加长
        tailBend = 3f;

        // —— 武器：替换为重装版前颚（更大更重，挥击更慢，可旋转 150° 射界） ——
        // 注意：Ram 构造器已加入 T1 前颚，此处先清空再挂载重装版，避免继承 T1 弱武器
        weapons.clear();
        weapons.add(new Weapon("endfield-industry-lib-heavyram-mandible"){{
            mirror = false;            // 单个武器，不镜像成对
            top = true;
            layerOffset = 0.05f;       // 正 z 偏移：绘制在主体贴图之上
            x = 0f; y = 7f;            // 前颚稍后移：让极光 cell 能盖住武器（避免过于靠前）
            shootY = 7f;
            rotate = true;             // 可旋转武器：独立于本体朝目标旋转瞄准
            rotateSpeed = 5f;          // 武器旋转速度（度/帧）：重装武器转动较慢
            rotationLimit = 150f;      // 射界：武器相对基座可旋转 ±75°，总计 150° 的射击扇区
            shootCone = 75f;           // 全射界内皆可开火（半角 75° = 总 150°）
            controllable = false;
            autoTarget = true;
            reload = 75f;              // T1 60  → 挥击频率更低（更重的挥击）
            recoil = -1f;
            shake = 0f;
            // 底座贴图（heavyram-mandible-base.png）＝下颚：默认 Weapon 不会加载 "-base" 贴图，
            // 因此用 RegionPart 加载。下颚强制沉到建筑/敌人之下，与上颚（武器主体）一上一下夹住目标，形成上下颚咬合效果
            parts.add(new RegionPart("-base"){{
                under = true;          // 绘制在武器主体之下
                outline = false;       // 底座无独立轮廓贴图
                // 强制沉底：层位定在建筑层（Layer.block=30）之下、地板之上，
                // 使下颚绘制在大部分建筑与地面单位（Layer.groundUnit=60）之下；上颚仍在其上 → 咬住敌人
                layer = Layer.block - 0.1f;
                // 后坐力驱动：开火时底座沿武器朝向额外后拉，形成前颚前伸、底座后坐的对比
                progress = PartProgress.recoil;
                moveY = 4f;
            }});
            bullet = new BulletType(0f, 0f){{
                speed = 0f;
                lifetime = 1f;
                instantDisappear = true;
                splashDamage = 42f;        // T1 24 → 挥击伤害更高
                splashDamageRadius = 38f;  // T1 28 → 挥击范围更大
                collidesAir = false;
                collidesGround = true;
                hittable = false;
                rangeOverride = 9f;        // T1 8  → 攻击/发现距离稍远
            }};
        }});

        // cell 由下方覆写的 draw() 在武器之后手动补画（默认引擎先画 cell 再画武器，武器会盖住 cell）
        drawCell = false;

        constructor = HeavyramLegsUnit::create;   // 默认走路步态；冲锋时由 HeavyramAI 切换为奔跑步态

        // —— 冲锋技能 AI（走路 + 周期性冲锋奔跑到敌人面前） ——
        aiController = () -> new HeavyramAI();
    }

    /**
     * 自定义绘制顺序：默认引擎顺序为 body → cell → weapons（武器会盖住极光 cell）。
     * 此处改为 body → weapons → cell，让极光光环覆盖在前颚武器之上，并削弱武器的突兀感。
     */
    @Override
    public void draw(Unit unit){
        // 引擎完整流程（body、legs、weapons；因 drawCell=false 自动跳过 cell）
        super.draw(unit);
        // 在武器层（主体层 + layerOffset 0.05）之上补画极光 cell
        Draw.z(groundLayer + Mathf.clamp(hitSize / 4000f, 0, 0.01f) + 0.1f);
        drawCell(unit);
    }

    /**
     * 更大的极光光环 cell：光带半径较 Ram（3f）放大，并在中心叠加一个
     * 同心正圆光环（平滑正圆，缓缓呼吸），血量/阵营指示逻辑与 Ram 一致。
     */
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

        // 光带中心：整体朝单位朝向偏移，并绕该点微微飘动
        float bx = unit.x + Angles.trnsx(unit.rotation, 6.8f) + Mathf.sin(t, 40f, 0.12f);
        float by = unit.y + Angles.trnsy(unit.rotation, 6.8f) + Mathf.absin(t, 55f, 0.12f);
        float R = 5.5f;                 // 比 Ram（3f）更大的光带半径
        float dmg = 1f - hf;
        float alpha = hf+(0.5f*dmg)+Mathf.sin(t, 30f*hf, 0.1f+0.5f*dmg);
        // —— 泛光底晕（随体型更大） ——
        Tmp.c2.set(tc).a(alpha * 0.06f);
        Fill.light(bx, by, 28, R * 1.4f, Tmp.c2, Color.clear);

        // —— 外层四瓣极光光带（更大，三层泛光） ——
        Draw.blend(Blending.additive);
        // 外层柔光
        Draw.color(tc.r, tc.g, tc.b, alpha * 0.18f);
        Lines.stroke(2.6f+Mathf.sin(t,30f,0.6f));
        drawBand(bx, by, R, t, ph, unit.rotation);
        // 中层
        Draw.color(tc.r, tc.g, tc.b, alpha * 0.34f);
        Lines.stroke(1.7f);
        drawBand(bx, by, R, t, ph, unit.rotation);
        // 内层亮芯
        Draw.color(tc.r, tc.g, tc.b, alpha * 0.55f);
        Lines.stroke(0.6f);
        drawBand(bx, by, R, t, ph, unit.rotation);

        // —— 同心正圆光环：居中，平滑正圆（不随四瓣波动），缓缓呼吸 ——
        float cr = R * 0.35f;           // 位于光带内部中心区域
        // 外柔光
        Draw.color(tc.r, tc.g, tc.b, alpha * 0.10f);
        Lines.stroke(1.7f + Mathf.sin(t, 40f, 0.4f));
        Lines.circle(bx, by, cr);
        // 内亮线 
        Draw.color(tc.r, tc.g, tc.b, alpha * 0.55f);
        Lines.stroke(0.5f);
        Lines.circle(bx, by, cr * 0.85f);

        Draw.blend();
        Draw.reset();
    }
}
