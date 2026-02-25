package endfieldindustrylib;

import arc.Core;
import endfieldindustrylib.EFcontents.EFblocks;
import endfieldindustrylib.EFcontents.EFitems;
import endfieldindustrylib.EFworld.blocks.AICBasicFacility.GenericAICBasicFacility;
import endfieldindustrylib.ui.fragments.AICFacilityConfigFragment;

public class Lib extends mindustry.mod.Mod {
    private AICFacilityConfigFragment facilityConfigFragment;

    @Override
    public void loadContent() {
        // item
        EFitems.load();

        // block
        EFblocks.load();
    }

    @Override
    public void init() {
        // 创建并构建配置面板
        facilityConfigFragment = new AICFacilityConfigFragment();
        facilityConfigFragment.build(Core.scene.root);

        // 为所有通用工厂方块设置显示处理器的回调
        GenericAICBasicFacility.showConfigHandler = build -> facilityConfigFragment.showConfig(build);
    }
}