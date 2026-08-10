package endfieldindustrylib.EFworld.blocks.AICBasicFacility;

import arc.math.geom.Point2;
import endfieldindustrylib.EFcontents.EFitems;
import mindustry.type.Category;
import mindustry.type.ItemStack;

public class SeedPickingUnit extends GenericAICBasicFacility {

    public SeedPickingUnit(String name) {
        super(name);

        size = 5;
        powerUsage = 0.16667f;
        requirements(Category.crafting, ItemStack.with(EFitems.amethystPart, 20));

        rotate = true;
        inputOffsets = new Point2[]{ new Point2(-3, -2), new Point2(-3, -1), new Point2(-3, 0), new Point2(-3, 1), new Point2(-3, 2) };  // 背面输入
        outputOffsets = new Point2[]{ new Point2(3, -2), new Point2(3, -1), new Point2(3, 0), new Point2(3, 1), new Point2(3, 2) };    // 正面输出

        inputSlotDefs = new SlotDef[]{ new SlotDef(null) };
        outputSlotDefs = new SlotDef[]{ new SlotDef(null) };

        recipes = new Recipe[]{
            //new Recipe(
            //    new ItemStack[]{ new ItemStack(EFitems.buckflower, 1) },
            //    new ItemStack[]{ new ItemStack(EFitems.buckflowerSeed, 2) },
            //    120f
            //),
            //new Recipe(
            //    new ItemStack[]{ new ItemStack(EFitems.citrome, 1) },
            //    new ItemStack[]{ new ItemStack(EFitems.citromeSeed, 2) },
            //    120f
            //),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.aketine, 1) },
                new ItemStack[]{ new ItemStack(EFitems.aketineSeed, 2) },
                120f
            ),
            new Recipe(
                new ItemStack[]{ new ItemStack(EFitems.sandleaf, 1) },
                new ItemStack[]{ new ItemStack(EFitems.sandleafSeed, 2) },
                120f
            ),
            //new Recipe(
            //    new ItemStack[]{ new ItemStack(EFitems.tartpepper, 1) },
            //    new ItemStack[]{ new ItemStack(EFitems.tartpepperSeed, 2) },
            //    120f
            //),
            //new Recipe(
            //    new ItemStack[]{ new ItemStack(EFitems.reedRye, 1) },
            //    new ItemStack[]{ new ItemStack(EFitems.reedRyeSeed, 2) },
            //    120f
            //),
            //new Recipe(
            //    new ItemStack[]{ new ItemStack(EFitems.redjadeGinseng, 1) },
            //    new ItemStack[]{ new ItemStack(EFitems.redjadeGinsengSeed, 2) },
            //    120f
            //),
            //new Recipe(
            //    new ItemStack[]{ new ItemStack(EFitems.amberRice, 1) },
            //    new ItemStack[]{ new ItemStack(EFitems.amberRiceSeed, 2) },
            //    120f
            //)
        };
    }
}