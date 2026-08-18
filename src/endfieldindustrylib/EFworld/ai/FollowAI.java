package endfieldindustrylib.EFworld.ai;

import mindustry.ai.types.GroundAI;
import mindustry.gen.Groups;
import mindustry.gen.Player;

/**
 * 跟随玩家 AI — 用于塔塔的"跟随"命令。
 * <p>
 * 持续追踪最近的非死亡队友玩家，保持适当的跟随距离。
 */
public class FollowAI extends GroundAI {

    @Override
    public void updateMovement() {
        // 查找最近的同队存活玩家
        Player targetPlayer = null;
        float bestDist = Float.MAX_VALUE;
        for (Player p : Groups.player) {
            if (!p.dead() && p.team() == unit.team) {
                float d = unit.dst2(p);
                if (d < bestDist) {
                    bestDist = d;
                    targetPlayer = p;
                }
            }
        }

        if (targetPlayer != null) {
            var pu = targetPlayer.unit();
            if (pu == null) {
                super.updateMovement();
                return;
            }
            // 面向玩家
            unit.lookAt(targetPlayer);

            // 保持一定距离跟随（玩家单位体积 + 自身体积 + 间距）
            float followDst = unit.type.hitSize + pu.hitSize + 50f;
            moveTo(targetPlayer, followDst, 20f);
        } else {
            // 没有可跟随的玩家，退化为普通地面单位行为
            super.updateMovement();
        }
    }
}
