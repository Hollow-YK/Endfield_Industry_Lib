package endfieldindustrylib;

import endfieldindustrylib.EFcontents.*;
import endfieldindustrylib.EFworld.CampaignHandler;

public class Lib extends mindustry.mod.Mod {

    @Override
    public void loadContent() {
        // item
        EFitems.load();

        // planet (塔卫二)
        EFplanets.loadContents();

        // sector presets (战役关卡)
        EFsectorPresets.load();

        // unit (塔塔)
        EFunits.load();

        // status effects
        EFstatusEffects.load();

        // block
        EFblocks.load();

        // tech tree: 初始化节点（需在 blocks 之后）
        EFTechTree.initNodes();

        // tech tree: 构建显示树
        EFTechTree.load(EFplanets.taelosII);
    }

    @Override
    public void init() {
        // 注册战役自定义事件监听器
        CampaignHandler.init();
    }
}
