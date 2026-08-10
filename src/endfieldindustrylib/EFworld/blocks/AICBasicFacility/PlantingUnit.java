package endfieldindustrylib.EFworld.blocks.AICBasicFacility;

import arc.math.geom.Point2;
import endfieldindustrylib.EFcontents.EFitems;
import mindustry.type.Category;
import mindustry.type.ItemStack;

public class PlantingUnit extends GenericAICBasicFacility {

    public PlantingUnit(String name) {
        super(name);

        size = 5;
        powerUsage = 0.333335f;
        requirements(Category.crafting, ItemStack.with(EFitems.amethystPart, 20, EFitems.carbon, 10));

        rotate = true;
        inputOffsets = new Point2[]{ new Point2(-3, -2), new Point2(-3, -1), new Point2(-3, 0), new Point2(-3, 1), new Point2(-3, 2) };  // 背面输入
        outputOffsets = new Point2[]{ new Point2(3, -2), new Point2(3, -1), new Point2(3, 0), new Point2(3, 1), new Point2(3, 2) };    // 正面输出

        inputSlotDefs = new SlotDef[]{ new SlotDef(null) };
        outputSlotDefs = new SlotDef[]{ new SlotDef(null) };

        recipes = new Recipe[]{
            //new Recipe(
            //    new ItemStack[]{ new ItemStack(EFitems.buckflowerSeed, 1) },
            //    new ItemStack[]{ new ItemStack(EFitems.buckflower, 1) },
            //    120f
            //),
            //new Recipe(
            //    new ItemStack[]{ new ItemStack(EFitems.citromeSeed, 1) },
            //    new ItemStack[]{ new ItemStack(EFitems.citrome, 1) },
            //    120f
            //),
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
