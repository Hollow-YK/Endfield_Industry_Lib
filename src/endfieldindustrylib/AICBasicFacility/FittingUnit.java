package endfieldindustrylib.AICBasicFacility;

import mindustry.type.*;
import endfieldindustrylib.Items.EFItems;

public class FittingUnit extends GenericAICBasicFacility {
    public FittingUnit(String name) {
        super(name);

        size = 3;
        requirements(Category.crafting, ItemStack.with(EFItems.originiumOre, 10));

        rotate = true;
        inputFacingMask = 1 << 2;
        outputFacingMask = 1 << 0;

        inputSlotDefs = new SlotDef[]{ new SlotDef(null) };
        outputSlotDefs = new SlotDef[]{ new SlotDef(null) };

        recipes = new Recipe[]{
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.ferrium, 1) },
                new ItemStack[]{ new ItemStack(EFItems.ferriumPart, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.amethystFiber, 1) },
                new ItemStack[]{ new ItemStack(EFItems.amethystPart, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.steel, 1) },
                new ItemStack[]{ new ItemStack(EFItems.steelPart, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.crystonFiber, 1) },
                new ItemStack[]{ new ItemStack(EFItems.crystonPart, 1) },
                120f
            )
        };

    }
    
}
