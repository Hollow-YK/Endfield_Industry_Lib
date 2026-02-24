package endfieldindustrylib.AICBasicFacility;

import mindustry.type.*;
import endfieldindustrylib.Items.*;

public class ShreddingUnit extends GenericAICBasicFacility {
    public ShreddingUnit(String name) {
        super(name);

        size = 3;
        requirements(Category.crafting, ItemStack.with(EFItems.origocrust, 5));

        rotate = true;
        inputFacingMask = 1 << 2;
        outputFacingMask = 1 << 0;

        inputSlotDefs = new SlotDef[]{ new SlotDef(null) };
        outputSlotDefs = new SlotDef[]{ new SlotDef(null) };

        recipes = new Recipe[]{
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.ferrium, 1) },
                new ItemStack[]{ new ItemStack(EFItems.ferriumPowder, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.amethystFiber, 1) },
                new ItemStack[]{ new ItemStack(EFItems.amethystPowder, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.originiumOre, 1) },
                new ItemStack[]{ new ItemStack(EFItems.originiumPowder, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.carbon, 1) },
                new ItemStack[]{ new ItemStack(EFItems.carbonPowder, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.origocrust, 1) },
                new ItemStack[]{ new ItemStack(EFItems.origocrustPowder, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.buckflower, 1) },
                new ItemStack[]{ new ItemStack(EFItems.buckflowerPowder, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.citrome, 1) },
                new ItemStack[]{ new ItemStack(EFItems.citromePowder, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.sandleaf, 1) },
                new ItemStack[]{ new ItemStack(EFItems.sandleafPowder, 3) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.aketine, 1) },
                new ItemStack[]{ new ItemStack(EFItems.aketinePowder, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.jincao, 1) },
                new ItemStack[]{ new ItemStack(EFItems.jincaoPowder, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.yazhen, 1) },
                new ItemStack[]{ new ItemStack(EFItems.yazhenPowder, 2) },
                120f
            )
        };

    }
    
}
