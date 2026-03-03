package endfieldindustrylib.EFworld.blocks.AICBasicFacility;

import endfieldindustrylib.EFcontents.EFitems;
import mindustry.type.*;

public class MouldingUnit extends GenericAICBasicFacility {

    public MouldingUnit(String name) {
        super(name);

        size = 3;
        powerUsage = 0.16667f;
        requirements(Category.crafting, ItemStack.with(EFitems.originiumOre, 10));

        rotate = true;
        inputFacingMask = 1 << 2;
        outputFacingMask = 1 << 0;

        inputSlotDefs = new SlotDef[]{ new SlotDef(null) };
        outputSlotDefs = new SlotDef[]{ new SlotDef(null) };

        recipes = new Recipe[]{
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.ferrium, 2) },
                new ItemStack[]{ new ItemStack(EFitems.ferriumBottle, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.amethystFiber, 2) },
                new ItemStack[]{ new ItemStack(EFitems.amethystBottle, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.steel, 2) },
                new ItemStack[]{ new ItemStack(EFitems.steelBottle , 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.crystonFiber, 2) },
                new ItemStack[]{ new ItemStack(EFitems.crystonBottle, 1) },
                120f
            )
        };

    }
    
}
