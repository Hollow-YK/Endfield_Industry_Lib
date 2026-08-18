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
import mindustry.gen.Unit;
import mindustry.type.UnitType;

/**
 * 极光光环 cell 基类：所有带"极光光环"的单位共用同一套 drawCell 绘制逻辑。
 * <p>
 * Ram 与 Sting 都继承本类，保证光环视觉完全一致（血量/阵营指示 + 四瓣极光光带）。
 * 若需调整光环效果，只需改动本类一处。
 */
public abstract class AuroraUnit extends UnitType{

    public AuroraUnit(String name){
        super(name);
    }

    /**
     * 适度提亮的 cell 阵营色：原队色（尤其红色等暗色）数值偏暗，叠加加法泛光后不明显。
     * 这里仅轻微放大 RGB 并截断到 [0,1]：既让暗色队色看得清，又避免过饱和成白色、看不清队色。
     */
    protected Color cellColorBright(Unit unit){
        return Tmp.c1.set(unit.team.color).mul(1.25f, 1.25f, 1.25f, 1f).clamp();
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
        Color tc = cellColorBright(unit);   // 提亮后的队色：暗色（尤其红色）更醒目

        // 光带中心：整体朝单位朝向（向前）偏移 2 像素，并绕该点微微飘动
        float bx = unit.x + Angles.trnsx(unit.rotation, 2.3f) + Mathf.sin(t, 40f, 0.1f);
        float by = unit.y + Angles.trnsy(unit.rotation, 2.3f) + Mathf.absin(t, 55f, 0.1f);
        float R = 3f;
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

    /** 绘制一条四瓣波动的闭合极光光带，随单位朝向旋转，相位周期左右扭动 */
    protected void drawBand(float cx, float cy, float R, float t, float ph, float rot){
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
