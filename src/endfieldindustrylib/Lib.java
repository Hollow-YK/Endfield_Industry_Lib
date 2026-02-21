package endfieldindustrylib;

import endfieldindustrylib.AICTransport.*;
import endfieldindustrylib.Items.Ore;
import endfieldindustrylib.Items.Plants;
import endfieldindustrylib.Items.Powder;
import endfieldindustrylib.Items.Industrial;
import endfieldindustrylib.Items.Battery;
import endfieldindustrylib.Items.Parts;
import endfieldindustrylib.Items.Components;
import endfieldindustrylib.Items.Bottle;

public class Lib extends mindustry.mod.Mod {
    @Override
    public void loadContent() {

        // block
        AICTransport.load();// 物流
        
        // item
        Ore.load();// 矿物
        Plants.load();// 植物
        Powder.load();// 粉末
        Industrial.load();// 工业产物
        Battery.load();// 电池
        Parts.load();// 零件
        Components.load();  // 装备原件
        Bottle.load();// 瓶子
    }
}