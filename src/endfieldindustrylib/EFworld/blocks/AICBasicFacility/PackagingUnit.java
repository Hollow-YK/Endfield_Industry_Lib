package endfieldindustrylib.EFworld.blocks.AICBasicFacility;

import endfieldindustrylib.EFcontents.EFitems;
import arc.math.geom.Point2;
import mindustry.type.*;

public class PackagingUnit extends RectGenericAICBasicFacility {

    public PackagingUnit(String name, int width, int height) {
        super(name, width, height);

        size = 4;
        powerUsage = 0.333335f;
        requirements(Category.crafting, ItemStack.with(EFitems.amethystPart, 20));

        rotate = true;
        // 背面输入 / 正面输出（当前 4×6 矩形的偏移）
        inputOffsets = new Point2[]{ new Point2(-2, -2), new Point2(-2, -1), new Point2(-2, 0), new Point2(-2, 1), new Point2(-2, 2), new Point2(-2, 3) };
        outputOffsets = new Point2[]{ new Point2(3, -2), new Point2(3, -1), new Point2(3, 0), new Point2(3, 1), new Point2(3, 2), new Point2(3, 3) };

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
