package endfieldindustrylib.EFworld.blocks.AICBasicFacility;

import endfieldindustrylib.EFcontents.EFitems;
import mindustry.type.*;

public class FittingUnit extends GenericAICBasicFacility {
    public FittingUnit(String name) {
        super(name);

        size = 3;
        requirements(Category.crafting, ItemStack.with(EFitems.originiumOre, 10));

        rotate = true;
        inputFacingMask = 1 << 2;
        outputFacingMask = 1 << 0;

        inputSlotDefs = new SlotDef[]{ new SlotDef(null) };
        outputSlotDefs = new SlotDef[]{ new SlotDef(null) };

        recipes = new Recipe[]{
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.ferrium, 1) },
                new ItemStack[]{ new ItemStack(EFitems.ferriumPart, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.amethystFiber, 1) },
                new ItemStack[]{ new ItemStack(EFitems.amethystPart, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.steel, 1) },
                new ItemStack[]{ new ItemStack(EFitems.steelPart, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.crystonFiber, 1) },
                new ItemStack[]{ new ItemStack(EFitems.crystonPart, 1) },
                120f
            )
        };

    }
    
}
