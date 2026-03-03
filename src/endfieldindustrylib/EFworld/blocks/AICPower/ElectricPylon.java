package endfieldindustrylib.EFworld.blocks.AICPower;

import mindustry.type.*;
import endfieldindustrylib.EFcontents.EFitems;

public class ElectricPylon extends EFPowerNode {
    public ElectricPylon(String name) {
        super(name);
        laserRange = 0;
        squarelaserRange = 6;
        size = 2;
        requirements(Category.power, ItemStack.with(EFitems.originiumOre, 5));
    }
}