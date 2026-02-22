package endfieldindustrylib;

import endfieldindustrylib.AICBasicFacility.AICBasicFacility;
import endfieldindustrylib.AICTransport.*;
import endfieldindustrylib.Items.EFItems;

public class Lib extends mindustry.mod.Mod {
    @Override
    public void loadContent() {
        
        // item
        EFItems.load();

        // block
        AICTransport.load();// 物流
        AICBasicFacility.load();// 工厂
    }
}