package endfieldindustrylib.EFworld;

import arc.Events;
import static mindustry.Vars.state;
import mindustry.game.EventType.WorldLoadEvent;

/**
 * 塔卫二战役事件处理器。
 * <p>
 * 所有战役规则/目标定义已迁移至 .msav 地图文件中，
 * 通过 Mindustry 地图编辑器配置 MapObjectives。
 * 此类仅保留全局基础设施。
 */
public class CampaignHandler {

    // ======================== 工具方法 ========================

    /** 是否为塔卫二战役模式 */
    public static boolean isCampaign() {
        return state.rules.sector != null
            && state.rules.sector.planet == endfieldindustrylib.EFcontents.EFplanets.taelosII;
    }

    /** 重置当前关卡状态 */
    private static void resetState() {
        // 预留：需要关卡级重置的状态在此添加
    }

    // ===================================================================
    //  事件监听器 — 在 Mod.init() 中注册
    // ===================================================================

    /** 注册全局战役事件监听 */
    public static void init() {
        // ── 加载关卡时重置状态 ──
        Events.on(WorldLoadEvent.class, event -> {
            resetState();
        });
    }
}
