package endfieldindustrylib.AICBasicFacility;

import mindustry.type.*;
import endfieldindustrylib.Items.EFItems;

public class MouldingUnit extends GenericAICBasicFacility {
    public MouldingUnit(String name) {
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
                new ItemStack[]{ new ItemStack(EFItems.ferrium, 2) },
                new ItemStack[]{ new ItemStack(EFItems.ferriumBottle, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.amethystFiber, 2) },
                new ItemStack[]{ new ItemStack(EFItems.amethystBottle, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.steel, 2) },
                new ItemStack[]{ new ItemStack(EFItems.steelBottle , 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFItems.crystonFiber, 2) },
                new ItemStack[]{ new ItemStack(EFItems.crystonBottle, 1) },
                120f
            )
        };

    }
    
}
