package endfieldindustrylib.EFcontents;

import arc.graphics.Color;
import mindustry.type.Item;

public class EFitems {
    public static Item
            // 矿物
            originiumOre, amethystOre,
            // 植物
            aketine, aketineSeed,
            sandleaf, sandleafSeed,
            // 粉末
            aketinePowder,
            sandleafPowder,
            carbonPowder, denseCarbonPowder,
            originiumPowder, origocrustPowder, denseOriginiumPowder, denseOrigocrustPowder,
            amethystPowder, crystonPowder,
            ferriumPowder, denseFerriumPowder,
            // 工业产物
            carbon, stabilizedCarbon,
            origocrust, packedOrigocrust,
            amethystFiber, crystonFiber,
            ferrium, steel, xiranite,
            // 电池
            lcValleyBattery, scValleyBattery, hcValleyBattery, lcWulingBattery,// scWulingBattery, hcWulingBattery,
            // 零件
            amethystPart, ferriumPart, steelPart, crystonPart,
            // 装备原件
            amethystComponent, ferriumComponent, crystonComponent, xiraniteComponent,
            // 瓶子
            amethystBottle, crystonBottle, ferriumBottle, steelBottle,
            // 消耗品
            industrialExplosive;

    public static void load() {
        // 矿物
        originiumOre = new Item("originium-ore") {{color = Color.valueOf("c66322");hardness = 1;cost = 1;charge = 0.05f;}};
        amethystOre = new Item("amethyst-ore") {{color = Color.valueOf("a55fc4");hardness = 2;cost = 1;}};

        // 植物
        aketine = new Item("aketine") {{color = Color.valueOf("27ae60");cost = 1;}};
        aketineSeed = new Item("aketine-seed") {{color = Color.valueOf("1e8449");cost = 1;}};
        sandleaf = new Item("sandleaf") {{color = Color.valueOf("f4d03f");cost = 1;}};
        sandleafSeed = new Item("sandleaf-seed") {{color = Color.valueOf("b7950b");cost = 1;}};

        // 粉末
        aketinePowder = new Item("aketine-powder") {{color = Color.valueOf("82e0aa");cost = 1;}};
        sandleafPowder = new Item("sandleaf-powder") {{color = Color.valueOf("f7dc6f");cost = 1;}};
        carbonPowder = new Item("carbon-powder") {{color = Color.valueOf("5d6d7e");cost = 1;}};
        denseCarbonPowder = new Item("dense-carbon-powder") {{color = Color.valueOf("2e4053");cost = 1;}};
        originiumPowder = new Item("originium-powder") {{color = Color.valueOf("b03a2e");cost = 1;}};
        origocrustPowder = new Item("origocrust-powder") {{color = Color.valueOf("784212");cost = 1;}};
        denseOriginiumPowder = new Item("dense-originium-powder") {{color = Color.valueOf("943126");cost = 1;}};
        denseOrigocrustPowder = new Item("dense-origocrust-powder") {{color = Color.valueOf("5d3a1a");cost = 1;}};
        amethystPowder = new Item("amethyst-powder") {{color = Color.valueOf("c39bd3");cost = 1;}};
        crystonPowder = new Item("cryston-powder") {{color = Color.valueOf("a569bd");cost = 1;}};
        ferriumPowder = new Item("ferrium-powder") {{color = Color.valueOf("85c1e2");cost = 1;}};
        denseFerriumPowder = new Item("dense-ferrium-powder") {{color = Color.valueOf("3498db");cost = 1;}};

        // 工业产物
        carbon = new Item("carbon") {{color = Color.valueOf("2c3e50");cost = 1;}};
        stabilizedCarbon = new Item("stabilized-carbon") {{color = Color.valueOf("1e2b37");cost = 1;}};
        origocrust = new Item("origocrust") {{color = Color.valueOf("bdc3c7");cost = 1;}};
        packedOrigocrust = new Item("packed-origocrust") {{color = Color.valueOf("ecf0f1");cost = 1;}};
        amethystFiber = new Item("amethyst-fiber") {{color = Color.valueOf("9b59b6");cost = 1;}};
        crystonFiber = new Item("cryston-fiber") {{color = Color.valueOf("8e44ad");cost = 1;}};
        ferrium = new Item("ferrium") {{color = Color.valueOf("3498db");cost = 1;}};
        steel = new Item("steel") {{color = Color.valueOf("7f8c8d");cost = 1;}};
        xiranite = new Item("xiranite") {{color = Color.valueOf("d4ac0d");cost = 1;}};

        // 电池
        lcValleyBattery = new Item("lc-valley-battery") {{color = Color.valueOf("7dcea0");cost = 1;charge = 0.22f;}};
        scValleyBattery = new Item("sc-valley-battery") {{color = Color.valueOf("52be80");cost = 1;charge = 0.42f;}};
        hcValleyBattery = new Item("hc-valley-battery") {{color = Color.valueOf("27ae60");cost = 1;charge = 1.1f;}};
        lcWulingBattery = new Item("lc-wuling-battery") {{color = Color.valueOf("85c1e2");cost = 1;charge = 1.6f;}};
        //scWulingBattery = new Item("lc-wuling-battery") {{color = Color.valueOf("85c1e2");cost = 1;}};
        //hcWulingBattery = new Item("lc-wuling-battery") {{color = Color.valueOf("85c1e2");cost = 1;}};

        // 零件
        amethystPart = new Item("amethyst-part") {{color = Color.valueOf("9b59b6");cost = 1;}};
        ferriumPart = new Item("ferrium-part") {{color = Color.valueOf("3498db");cost = 1;}};
        steelPart = new Item("steel-part") {{color = Color.valueOf("7f8c8d");cost = 1;}};
        crystonPart = new Item("cryston-part") {{color = Color.valueOf("8e44ad");cost = 1;}};

        // 装备原件
        amethystComponent = new Item("amethyst-component") {{color = Color.valueOf("9b59b6");cost = 1;}};
        ferriumComponent = new Item("ferrium-component") {{color = Color.valueOf("3498db");cost = 1;}};
        crystonComponent = new Item("cryston-component") {{color = Color.valueOf("8e44ad");cost = 1;}};
        xiraniteComponent = new Item("xiranite-component") {{color = Color.valueOf("d4ac0d");cost = 1;}};

        // 瓶子
        amethystBottle = new Item("amethyst-bottle") {{color = Color.valueOf("c39bd3");cost = 1;}};
        crystonBottle = new Item("cryston-bottle") {{color = Color.valueOf("a569bd");cost = 1;}};
        ferriumBottle = new Item("ferrium-bottle") {{color = Color.valueOf("85c1e2");cost = 1;}};
        steelBottle = new Item("steel-bottle") {{color = Color.valueOf("85c1e2");cost = 1;}};

        // 消耗品
        industrialExplosive = new Item("industrial-explosive") {{color = Color.valueOf("ff9300");cost = 1;}};
    }
}