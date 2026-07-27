package endfieldindustrylib.EFcontents;

import arc.graphics.Color;
import mindustry.type.StatusEffect;

public class EFstatusEffects {
    /** 液氮 - 移动速度下降75% */
    public static StatusEffect lnTowerEffect;

    public static void load() {
        lnTowerEffect = new StatusEffect("ln-tower-effect") {{
            color = Color.valueOf("a8d8ff");   // 冰蓝色
            speedMultiplier = 0.25f;           // 移动速度下降75%
            damageMultiplier = 1f;
            healthMultiplier = 1f;
            reloadMultiplier = 1f;

            // 仅出现在塔卫二的核心数据库标签页
            allDatabaseTabs = false;
            databaseTabs.add(EFplanets.taelosII);
        }};
    }
}
