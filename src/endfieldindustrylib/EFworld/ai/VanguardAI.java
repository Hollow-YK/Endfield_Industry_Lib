package endfieldindustrylib.EFworld.ai;

import arc.math.geom.Position;
import mindustry.gen.Building;
import mindustry.gen.Unit;

/**
 * 先锋（Vanguard）专属 AI — 盾戳单位，继承 {@link RaiderAI} 的近战贴近逻辑。
 * <p>
 * 长矛戳刺距离比砍刀稍远（{@link #spearRange}）；盾牌（正面伤害吸收）由
 * {@code VanguardUnit} 实体独立驱动，AI 无需管理。
 * <p>
 * TODO: 预留给计划新建的"物理效果"异常——受到其影响时调用
 * {@code VanguardUnit.dropShield()} 让手中的盾牌脱落消失。
 */
public class VanguardAI extends RaiderAI{
    /** 长矛戳刺命中距离基数（世界单位）：比砍刀（Raider 的 7）更远 */
    private static final float spearRange = 9f;

    @Override
    protected float hitRangeFor(Position aim){
        float targetHit = aim instanceof Unit u ? u.hitSize : (aim instanceof Building b ? b.hitSize() : 0f);
        return spearRange + unit.type.hitSize * 0.5f + targetHit * 0.5f + 1f;
    }
}
