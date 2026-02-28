package endfieldindustrylib.EFworld.blocks.AICBasicFacility;

import endfieldindustrylib.EFcontents.EFitems;
import mindustry.type.*;

public class PackagingUnit extends GenericAICBasicFacility {

    public PackagingUnit(String name) {
        super(name);

        size = 3;
        powerUsage = 0.333335f;
        requirements(Category.crafting, ItemStack.with(EFitems.amethystPart, 20));

        rotate = true;
        inputFacingMask = 1 << 2;
        outputFacingMask = 1 << 0;

        inputSlotDefs = new SlotDef[]{ new SlotDef(null) , new SlotDef(null) };
        outputSlotDefs = new SlotDef[]{ new SlotDef(null) };

        recipes = new Recipe[]{
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.amethystPart, 5), new ItemStack(EFitems.aketinePowder, 1) },
                new ItemStack[]{ new ItemStack(EFitems.industrialExplosive, 1) },
                600f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.amethystPart, 5), new ItemStack(EFitems.originiumPowder, 10) },
                new ItemStack[]{ new ItemStack(EFitems.lcValleyBattery, 1) },
                600f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.ferriumPart, 10), new ItemStack(EFitems.originiumPowder, 15) },
                new ItemStack[]{ new ItemStack(EFitems.scValleyBattery, 1) },
                600f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.steelPart, 10), new ItemStack(EFitems.denseOriginiumPowder, 15) },
                new ItemStack[]{ new ItemStack(EFitems.hcValleyBattery, 1) },
                600f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.xiranite, 5), new ItemStack(EFitems.denseOriginiumPowder, 15) },
                new ItemStack[]{ new ItemStack(EFitems.lcWulingBattery, 1) },
                600f
            )
        };
    }
}
