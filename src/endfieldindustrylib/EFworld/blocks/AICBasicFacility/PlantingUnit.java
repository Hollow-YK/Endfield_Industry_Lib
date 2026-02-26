package endfieldindustrylib.EFworld.blocks.AICBasicFacility;

import endfieldindustrylib.EFcontents.EFitems;
import mindustry.type.*;

public class PlantingUnit extends GenericAICBasicFacility {

    public PlantingUnit(String name) {
        super(name);

        size = 5;
        requirements(Category.production, ItemStack.with(EFitems.amethystPart, 20, EFitems.carbon, 10));

        rotate = true;
        inputFacingMask = 1 << 2;
        outputFacingMask = 1 << 0;

        inputSlotDefs = new SlotDef[]{ new SlotDef(null) };
        outputSlotDefs = new SlotDef[]{ new SlotDef(null) };

        recipes = new Recipe[]{
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.buckflowerSeed, 1) },
                new ItemStack[]{ new ItemStack(EFitems.buckflower, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.citromeSeed, 1) },
                new ItemStack[]{ new ItemStack(EFitems.citrome, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.sandleafSeed, 1) },
                new ItemStack[]{ new ItemStack(EFitems.sandleaf, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.aketineSeed, 1) },
                new ItemStack[]{ new ItemStack(EFitems.aketine, 1) },
                120f
            )
        };

    }
    
}
