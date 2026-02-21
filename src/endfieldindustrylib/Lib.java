package endfieldindustrylib;

import endfieldindustrylib.AICTransport.*;
import endfieldindustrylib.Items.Ore;
import endfieldindustrylib.Items.Plants;

public class Lib extends mindustry.mod.Mod {
    @Override
    public void loadContent() {

        // block
        // 加载物流方块
        AICTransport.load();
        
        // item
        // 加载矿物
        Ore.load();
        // 加载植物
        Plants.load();
    }
}