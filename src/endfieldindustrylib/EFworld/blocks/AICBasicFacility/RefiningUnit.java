package endfieldindustrylib.EFworld.blocks.AICBasicFacility;

import endfieldindustrylib.EFcontents.EFitems;
import mindustry.type.*;

public class RefiningUnit extends GenericAICBasicFacility {

    public RefiningUnit(String name){
        super(name);

        size = 3;
        requirements(Category.crafting, ItemStack.with(EFitems.originiumOre, 5));

        rotate = true;
        inputFacingMask = 1 << 2;
        outputFacingMask = 1 << 0;

        inputSlotDefs = new SlotDef[]{ new SlotDef(null) };
        outputSlotDefs = new SlotDef[]{ new SlotDef(null) };

        recipes = new Recipe[] {
            // 基础配方
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.ferriumOre, 1) },
                new ItemStack[] { new ItemStack(EFitems.ferrium, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.amethystOre, 1) },
                new ItemStack[] { new ItemStack(EFitems.amethystFiber, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.originiumOre, 1) },
                new ItemStack[] { new ItemStack(EFitems.origocrust, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.denseOrigocrustPowder, 1) },
                new ItemStack[] { new ItemStack(EFitems.packedOrigocrust, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.denseFerriumPowder, 1) },
                new ItemStack[] { new ItemStack(EFitems.steel, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.crystonPowder, 1) },
                new ItemStack[] { new ItemStack(EFitems.crystonFiber, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.denseCarbonPowder, 1) },
                new ItemStack[] { new ItemStack(EFitems.stabilizedCarbon, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.denseOriginiumPowder, 1) },
                new ItemStack[] { new ItemStack(EFitems.denseOrigocrustPowder, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.buckflower, 1) },
                new ItemStack[] { new ItemStack(EFitems.carbon, 1) },
                2f
            ),

            // 隐藏配方（需先加工一次解锁）
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.ferriumPowder, 1) },
                new ItemStack[] { new ItemStack(EFitems.ferrium, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.amethystPowder, 1) },
                new ItemStack[] { new ItemStack(EFitems.amethystFiber, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.origocrustPowder, 1) },
                new ItemStack[] { new ItemStack(EFitems.origocrust, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.originiumPowder, 1) },
                new ItemStack[] { new ItemStack(EFitems.origocrustPowder, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.citrome, 1) },
                new ItemStack[] { new ItemStack(EFitems.carbon, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.sandleaf, 1) },
                new ItemStack[] { new ItemStack(EFitems.carbon, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.jincao, 1) },
                new ItemStack[] { new ItemStack(EFitems.carbon, 2) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.yazhen, 1) },
                new ItemStack[] { new ItemStack(EFitems.carbon, 2) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.wood, 1) },
                new ItemStack[] { new ItemStack(EFitems.carbon, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.buckflowerPowder, 1) },
                new ItemStack[] { new ItemStack(EFitems.carbonPowder, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.citromePowder, 1) },
                new ItemStack[] { new ItemStack(EFitems.carbonPowder, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.sandleafPowder, 3) },
                new ItemStack[] { new ItemStack(EFitems.carbonPowder, 2) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.jincaoPowder, 1) },
                new ItemStack[] { new ItemStack(EFitems.carbonPowder, 2) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.yazhenPowder, 1) },
                new ItemStack[] { new ItemStack(EFitems.carbonPowder, 2) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.groundBuckflowerPowder, 1) },
                new ItemStack[] { new ItemStack(EFitems.denseCarbonPowder, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFitems.groundCitromePowder, 1) },
                new ItemStack[] { new ItemStack(EFitems.denseCarbonPowder, 1) },
                2f
            )
        };
    }
    
}
