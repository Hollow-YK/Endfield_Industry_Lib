package endfieldindustrylib.EFworld.ai;

import endfieldindustrylib.EFworld.unit.Aggeloi.Heavyram;
import mindustry.ai.types.CommandAI;
import mindustry.gen.Teamc;

import static mindustry.Vars.tilesize;

/**
 * 拉姆 / 重装拉姆 专属指挥 AI — 玩家指挥（右键攻击 / 勾选追击）时替换 {@link CommandAI}，修复近战单位问题：
 * <ul>
 *   <li><b>追击找不到敌人</b>：{@link CommandAI} 用 {@code unit.range()}（近战仅约 4）索敌，
 *       此处改用较大的近战索敌范围，使「追击」能找到远处敌人并冲向它。</li>
 *   <li><b>推着敌人走</b>：配合在 {@code Ram} 上设置的 {@code range}（使 CommandAI 的
 *       {@code engageRange = range - 10} 为正），被指挥攻击时在敌人面前停下而非直冲敌人中心。</li>
 *   <li><b>重装冲锋</b>：重装拉姆被指挥攻击/追击时，<strong>走向敌人途中自动触发</strong>
 *       {@link HeavyramCharge} 冲锋（蓄力 → 疾驰冲脸），与自动 AI 模式行为一致。</li>
 * </ul>
 * <p>
 * 由 {@code Ram} 构造器把 {@code controller} 覆盖为「玩家/被指挥单位使用本 AI」来实现接入。
 */
public class RamCommandAI extends CommandAI {
    /** 近战追击索敌范围（世界单位）：指挥/追击时用此范围找敌人（覆盖 unit.range() 过小的问题） */
    private static final float chaseRange = 18f * tilesize;

    /** 重装拉姆冲锋技能逻辑（仅对重装拉姆生效） */
    private final HeavyramCharge charge = new HeavyramCharge();

    @Override
    public Teamc findMainTarget(float x, float y, float range, boolean air, boolean ground){
        // 近战单位：unit.range() 过小（约 4），自动索敌/追击找不到远处敌人；改用较大的索敌范围
        return super.findMainTarget(x, y, chaseRange, air, ground);
    }

    @Override
    public void updateUnit(){
        // 先执行标准指挥逻辑（右键攻击、追击、编队移动等）
        super.updateUnit();

        // 重装拉姆：仅在“指挥攻击/追击”时触发冲锋（走向敌人途中自动蓄力 → 疾驰冲脸）。
        // 只认 attackTarget（右键攻击/追击都会设置它）；纯移动命令（右键地面）attackTarget 为 null，
        // 不触发冲锋，避免自动索敌目标（target）把单位带偏；此时调用的 stop 也不会清零速度（见 HeavyramCharge.stop）。
        if(unit.type instanceof Heavyram){
            if(attackTarget != null){
                charge.update(unit, attackTarget);
            }else{
                charge.stop(unit);
            }
        }
    }
}
