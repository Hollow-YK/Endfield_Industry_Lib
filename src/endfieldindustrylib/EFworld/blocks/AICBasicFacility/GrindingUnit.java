package endfieldindustrylib.EFworld.blocks.AICBasicFacility;

import endfieldindustrylib.EFcontents.EFitems;
import mindustry.type.*;

public class GrindingUnit extends RectGenericAICBasicFacility {
    public GrindingUnit(String name, int width, int height) {
        super(name, width, height);

        size = 4;
        powerUsage = 0.833334f;
        requirements(Category.crafting, ItemStack.with(EFitems.ferriumPart, 10));

        rotate = true;
        inputFacingMask = 1 << 2;
        outputFacingMask = 1 << 0;

        inputSlotDefs = new SlotDef[]{ new SlotDef(null) , new SlotDef(null) };
        outputSlotDefs = new SlotDef[]{ new SlotDef(null) };

        recipes = new Recipe[]{
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.ferriumPowder, 2), new ItemStack(EFitems.sandleafPowder, 1) },
                new ItemStack[]{ new ItemStack(EFitems.denseFerriumPowder, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.amethystPowder, 2), new ItemStack(EFitems.sandleafPowder, 1) },
                new ItemStack[]{ new ItemStack(EFitems.crystonPowder, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.originiumPowder, 2), new ItemStack(EFitems.sandleafPowder, 1) },
                new ItemStack[]{ new ItemStack(EFitems.denseOriginiumPowder, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.carbonPowder, 2), new ItemStack(EFitems.sandleafPowder, 1) },
                new ItemStack[]{ new ItemStack(EFitems.denseCarbonPowder, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.origocrustPowder, 2), new ItemStack(EFitems.sandleafPowder, 1) },
                new ItemStack[]{ new ItemStack(EFitems.denseOrigocrustPowder, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.buckflowerPowder, 2), new ItemStack(EFitems.sandleafPowder, 1) },
                new ItemStack[]{ new ItemStack(EFitems.groundBuckflowerPowder, 1) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.citromePowder, 2), new ItemStack(EFitems.sandleafPowder, 1) },
                new ItemStack[]{ new ItemStack(EFitems.groundCitromePowder, 1) },
                120f
            )
        };

    }

}