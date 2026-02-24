package endfieldindustrylib.AICBasicFacility;

import mindustry.type.*;
import endfieldindustrylib.Items.*;

public class SeedPickingUnit extends GenericAICBasicFacility {

    public SeedPickingUnit(String name) {
        super(name);

        size = 5;
        requirements(Category.production, ItemStack.with(EFItems.amethystPart, 20));

        rotate = true;
        inputFacingMask = 1 << 2; // 背面输入
        outputFacingMask = 1 << 0; // 正面输出

        inputSlotDefs = new SlotDef[]{ new SlotDef(null) };
        outputSlotDefs = new SlotDef[]{ new SlotDef(null) };

        recipes = new Recipe[]{
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.buckflower, 1) },
                new ItemStack[]{ new ItemStack(EFItems.buckflowerSeed, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.citrome, 1) },
                new ItemStack[]{ new ItemStack(EFItems.citromeSeed, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.aketine, 1) },
                new ItemStack[]{ new ItemStack(EFItems.aketineSeed, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.sandleaf, 1) },
                new ItemStack[]{ new ItemStack(EFItems.sandleafSeed, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.tartpepper, 1) },
                new ItemStack[]{ new ItemStack(EFItems.tartpepperSeed, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.reedRye, 1) },
                new ItemStack[]{ new ItemStack(EFItems.reedRyeSeed, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.redjadeGinseng, 1) },
                new ItemStack[]{ new ItemStack(EFItems.redjadeGinsengSeed, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.amberRice, 1) },
                new ItemStack[]{ new ItemStack(EFItems.amberRiceSeed, 2) },
                120f
            )
        };
    }
}