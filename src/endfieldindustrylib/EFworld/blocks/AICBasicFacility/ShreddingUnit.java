package endfieldindustrylib.EFworld.blocks.AICBasicFacility;

import endfieldindustrylib.EFcontents.EFitems;
import mindustry.type.*;

public class ShreddingUnit extends GenericAICBasicFacility {

    public ShreddingUnit(String name) {
        super(name);

        size = 3;
        requirements(Category.crafting, ItemStack.with(EFitems.origocrust, 5));

        rotate = true;
        inputFacingMask = 1 << 2;
        outputFacingMask = 1 << 0;

        inputSlotDefs = new SlotDef[]{ new SlotDef(null) };
        outputSlotDefs = new SlotDef[]{ new SlotDef(null) };

        recipes = new Recipe[]{
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.ferrium, 1) },
                new ItemStack[]{ new ItemStack(EFitems.ferriumPowder, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.amethystFiber, 1) },
                new ItemStack[]{ new ItemStack(EFitems.amethystPowder, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.originiumOre, 1) },
                new ItemStack[]{ new ItemStack(EFitems.originiumPowder, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.carbon, 1) },
                new ItemStack[]{ new ItemStack(EFitems.carbonPowder, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.origocrust, 1) },
                new ItemStack[]{ new ItemStack(EFitems.origocrustPowder, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.buckflower, 1) },
                new ItemStack[]{ new ItemStack(EFitems.buckflowerPowder, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.citrome, 1) },
                new ItemStack[]{ new ItemStack(EFitems.citromePowder, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.sandleaf, 1) },
                new ItemStack[]{ new ItemStack(EFitems.sandleafPowder, 3) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.aketine, 1) },
                new ItemStack[]{ new ItemStack(EFitems.aketinePowder, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.jincao, 1) },
                new ItemStack[]{ new ItemStack(EFitems.jincaoPowder, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.yazhen, 1) },
                new ItemStack[]{ new ItemStack(EFitems.yazhenPowder, 2) },
                120f
            )
        };

    }
    
}
