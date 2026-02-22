package endfieldindustrylib.AICBasicFacility;

import mindustry.type.*;
import endfieldindustrylib.Items.EFItems;

public class RefiningUnit extends GenericAICBasicFacility {

    public RefiningUnit(String name){
        super(name);

        size = 3;
        requirements(Category.crafting, ItemStack.with(EFItems.originiumOre, 5));

        rotate = true;
        inputFacingMask = 1 << 2;
        outputFacingMask = 1 << 0;

        inputSlotDefs = new SlotDef[]{ new SlotDef(null) };
        outputSlotDefs = new SlotDef[]{ new SlotDef(null) };

        recipes = new Recipe[] {
            // 基础配方
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.ferriumOre, 1) },
                new ItemStack[] { new ItemStack(EFItems.ferrium, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.amethystOre, 1) },
                new ItemStack[] { new ItemStack(EFItems.amethystFiber, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.originiumOre, 1) },
                new ItemStack[] { new ItemStack(EFItems.origocrust, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.denseOrigocrustPowder, 1) },
                new ItemStack[] { new ItemStack(EFItems.packedOrigocrust, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.denseFerriumPowder, 1) },
                new ItemStack[] { new ItemStack(EFItems.steel, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.crystonPowder, 1) },
                new ItemStack[] { new ItemStack(EFItems.crystonFiber, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.denseCarbonPowder, 1) },
                new ItemStack[] { new ItemStack(EFItems.stabilizedCarbon, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.denseOriginiumPowder, 1) },
                new ItemStack[] { new ItemStack(EFItems.denseOrigocrustPowder, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.buckflower, 1) },
                new ItemStack[] { new ItemStack(EFItems.carbon, 1) },
                2f
            ),

            // 隐藏配方（需先加工一次解锁）
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.ferriumPowder, 1) },
                new ItemStack[] { new ItemStack(EFItems.ferrium, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.amethystPowder, 1) },
                new ItemStack[] { new ItemStack(EFItems.amethystFiber, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.origocrustPowder, 1) },
                new ItemStack[] { new ItemStack(EFItems.origocrust, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.originiumPowder, 1) },
                new ItemStack[] { new ItemStack(EFItems.origocrustPowder, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.citrome, 1) },
                new ItemStack[] { new ItemStack(EFItems.carbon, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.sandleaf, 1) },
                new ItemStack[] { new ItemStack(EFItems.carbon, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.jincao, 1) },
                new ItemStack[] { new ItemStack(EFItems.carbon, 2) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.yazhen, 1) },
                new ItemStack[] { new ItemStack(EFItems.carbon, 2) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.wood, 1) },
                new ItemStack[] { new ItemStack(EFItems.carbon, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.buckflowerPowder, 1) },
                new ItemStack[] { new ItemStack(EFItems.carbonPowder, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.citromePowder, 1) },
                new ItemStack[] { new ItemStack(EFItems.carbonPowder, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.sandleafPowder, 3) },
                new ItemStack[] { new ItemStack(EFItems.carbonPowder, 2) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.jincaoPowder, 1) },
                new ItemStack[] { new ItemStack(EFItems.carbonPowder, 2) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.yazhenPowder, 1) },
                new ItemStack[] { new ItemStack(EFItems.carbonPowder, 2) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.groundBuckflowerPowder, 1) },
                new ItemStack[] { new ItemStack(EFItems.denseCarbonPowder, 1) },
                2f
            ),
            new Recipe(
                new ItemStack[] { new ItemStack(EFItems.groundCitromePowder, 1) },
                new ItemStack[] { new ItemStack(EFItems.denseCarbonPowder, 1) },
                2f
            )
        };
    }
    
}
