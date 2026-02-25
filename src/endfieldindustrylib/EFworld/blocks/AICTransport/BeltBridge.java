package endfieldindustrylib.EFworld.blocks.AICTransport;

import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.blocks.distribution.Junction;
import mindustry.world.meta.BlockGroup;

public class BeltBridge extends Junction {
    public BeltBridge(String name) {
        super(name);
        speed = 0.008f;
        capacity = 1;
        health = 60;
        size = 1;
        group = BlockGroup.transportation;
        category = Category.distribution;
        requirements(Category.distribution, ItemStack.with());
    }

    public class BeltBridgeBuild extends JunctionBuild {}
}