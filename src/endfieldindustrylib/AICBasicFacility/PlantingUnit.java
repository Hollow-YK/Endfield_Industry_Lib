package endfieldindustrylib.AICBasicFacility;

import mindustry.type.*;
import endfieldindustrylib.Items.*;

public class PlantingUnit extends GenericAICBasicFacility {
    public PlantingUnit(String name) {
        super(name);

        size = 5;
        requirements(Category.production, ItemStack.with(EFItems.amethystPart, 20, EFItems.carbon, 10));

        rotate = true;
        inputFacingMask = 1 << 2;
        outputFacingMask = 1 << 0;

        inputSlotDefs = new SlotDef[]{ new SlotDef(null) };
        outputSlotDefs = new SlotDef[]{ new SlotDef(null) };

        recipes = new Recipe[]{
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.buckflowerSeed, 1) },
                new ItemStack[]{ new ItemStack(EFItems.buckflower, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.citromeSeed, 1) },
                new ItemStack[]{ new ItemStack(EFItems.citrome, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.sandleafSeed, 1) },
                new ItemStack[]{ new ItemStack(EFItems.sandleaf, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.aketineSeed, 1) },
                new ItemStack[]{ new ItemStack(EFItems.aketine, 1) },
                120f
            )
        };

    }
    
}
