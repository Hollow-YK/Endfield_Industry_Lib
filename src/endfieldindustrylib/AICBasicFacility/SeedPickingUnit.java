package endfieldindustrylib.AICBasicFacility;

import mindustry.type.*;
import endfieldindustrylib.Items.*;

public class SeedPickingUnit extends GenericAICBasicFacility {

    public SeedPickingUnit(String name) {
        super(name);

        size = 5;
        requirements(Category.production, ItemStack.with(Parts.amethystPart, 20));

        rotate = true;
        inputFacingMask = 1 << 2; // 背面输入
        outputFacingMask = 1 << 0; // 正面输出

        inputSlotDefs = new SlotDef[]{ new SlotDef(null) };
        outputSlotDefs = new SlotDef[]{ new SlotDef(null) };

        recipes = new Recipe[]{
            new Recipe(
                new ItemStack[]{ new ItemStack(Plants.buckflower, 1) },
                new ItemStack[]{ new ItemStack(Plants.buckflowerSeed, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(Plants.citrome, 1) },
                new ItemStack[]{ new ItemStack(Plants.citromeSeed, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(Plants.aketine, 1) },
                new ItemStack[]{ new ItemStack(Plants.aketineSeed, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(Plants.sandleaf, 1) },
                new ItemStack[]{ new ItemStack(Plants.sandleafSeed, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(Plants.tartpepper, 1) },
                new ItemStack[]{ new ItemStack(Plants.tartpepperSeed, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(Plants.reedRye, 1) },
                new ItemStack[]{ new ItemStack(Plants.reedRyeSeed, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(Plants.redjadeGinseng, 1) },
                new ItemStack[]{ new ItemStack(Plants.redjadeGinsengSeed, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(Plants.amberRice, 1) },
                new ItemStack[]{ new ItemStack(Plants.amberRiceSeed, 2) },
                120f
            )
        };
    }
}