package endfieldindustrylib;

import arc.Core;
import endfieldindustrylib.AICBasicFacility.AICBasicFacility;
import endfieldindustrylib.AICBasicFacility.GenericAICBasicFacility;
import endfieldindustrylib.AICTransport.AICTransport;
import endfieldindustrylib.Items.EFItems;
import endfieldindustrylib.ui.fragments.AICFacilityConfigFragment;

public class Lib extends mindustry.mod.Mod {
    private AICFacilityConfigFragment facilityConfigFragment;

    @Override
    public void loadContent() {
        // item
        EFItems.load();

        // block
        AICTransport.load();// 物流
        AICBasicFacility.load();// 工厂
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