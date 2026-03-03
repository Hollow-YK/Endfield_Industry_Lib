package endfieldindustrylib.EFworld.blocks.AICPower;

import endfieldindustrylib.EFcontents.EFitems;
import mindustry.type.*;

public class RelayTower extends EFPowerNode {

    public RelayTower(String name) {
        super(name);
        laserRange = 0;
        squarelaserRange = 3;
        size = 3;
        requirements(Category.power, ItemStack.with(EFitems.origocrust, 20));
        configurable = true;
    }
}