package endfieldindustrylib.Items;

import arc.graphics.Color;
import mindustry.type.Item;

public class EFItems {
    public static Item
            // 矿物
            originiumOre, amethystOre, ferriumOre,
            // 植物
            wood,
            buckflower, firebuckle, buckflowerSeed,
            citrome, umbraline, citromeSeed,
            aketine, aketineSeed,
            sandleaf, sandleafSeed,
            tartpepper, tartpepperSeed,
            reedRye, reedRyeSeed,
            jincao, fluffedJincao, jincaoSeed,
            yazhen, thornyYazhen, yazhenSeed,
            redjadeGinseng, redjadeGinsengSeed,
            amberRice, amberRiceSeed,
            // 粉末
            buckflowerPowder, firebucklePowder, groundBuckflowerPowder,
            citromePowder, citromix, groundCitromePowder,
            aketinePowder,
            sandleafPowder,
            jincaoPowder, fluffedJincaoPowder,
            yazhenPowder, thornyYazhenPowder,
            carbonPowder, denseCarbonPowder,
            originiumPowder, origocrustPowder, denseOriginiumPowder, denseOrigocrustPowder,
            amethystPowder, crystonPowder,
            ferriumPowder, denseFerriumPowder,
            // 工业产物
            carbon, stabilizedCarbon,
            origocrust, packedOrigocrust,
            amethystFiber, crystonFiber,
            ferrium, steel,
            // 电池
            lcValleyBattery, scValleyBattery, hcValleyBattery, lcWulingBattery,// scWulingBattery, hcWulingBattery,
            // 零件
            amethystPart, ferriumPart, steelPart, crystonPart,
            // 装备原件
            amethystComponent, ferriumComponent, crystonComponent, xiraniteComponent,
            // 瓶子
            amethystBottle, crystonBottle, ferriumBottle, steelBottle;

    public static void load() {
        // 矿物
        originiumOre = new Item("originium-ore") {{color = Color.valueOf("c66322");hardness = 1;cost = 1;}};
        amethystOre = new Item("amethyst-ore") {{color = Color.valueOf("a55fc4");hardness = 2;cost = 1;}};
        ferriumOre = new Item("ferrium-ore") {{color = Color.valueOf("4f7ebf");hardness = 3;cost = 1;}};

        // 植物
        wood = new Item("wood") {{hardness = 1;cost = 1;}};
        buckflower = new Item("buckflower") {{color = Color.valueOf("d4a5a5");hardness = 1;cost = 1;}};
        firebuckle = new Item("firebuckle") {{color = Color.valueOf("e67e22");hardness = 1;cost = 1;}};
        buckflowerSeed = new Item("buckflower-seed") {{color = Color.valueOf("b87333");hardness = 0;cost = 1;}};
        citrome = new Item("citrome") {{color = Color.valueOf("f1c40f");hardness = 1;cost = 1;}};
        umbraline = new Item("umbraline") {{color = Color.valueOf("2c3e50");hardness = 1;cost = 1;}};
        citromeSeed = new Item("citrome-seed") {{color = Color.valueOf("7d3c98");hardness = 0;cost = 1;}};
        aketine = new Item("aketine") {{color = Color.valueOf("27ae60");hardness = 1;cost = 1;}};
        aketineSeed = new Item("aketine-seed") {{color = Color.valueOf("1e8449");hardness = 0;cost = 1;}};
        sandleaf = new Item("sandleaf") {{color = Color.valueOf("f4d03f");hardness = 1;cost = 1;}};
        sandleafSeed = new Item("sandleaf-seed") {{color = Color.valueOf("b7950b");hardness = 0;cost = 1;}};
        tartpepper = new Item("tartpepper") {{color = Color.valueOf("e74c3c");hardness = 1;cost = 1;}};
        tartpepperSeed = new Item("tartpepper-seed") {{color = Color.valueOf("c0392b");hardness = 0;cost = 1;}};
        reedRye = new Item("reed-rye") {{color = Color.valueOf("95a5a6");hardness = 1;cost = 1;}};
        reedRyeSeed = new Item("reed-rye-seed") {{color = Color.valueOf("7f8c8d");hardness = 0;cost = 1;}};
        jincao = new Item("jincao") {{color = Color.valueOf("2ecc71");hardness = 1;cost = 1;}};
        fluffedJincao = new Item("fluffed-jincao") {{color = Color.valueOf("f39c12");hardness = 1;cost = 1;}};
        jincaoSeed = new Item("jincao-seed") {{color = Color.valueOf("d35400");hardness = 0;cost = 1;}};
        yazhen = new Item("yazhen") {{color = Color.valueOf("1abc9c");hardness = 1;cost = 1;}};
        thornyYazhen = new Item("thorny-yazhen") {{color = Color.valueOf("16a085");hardness = 1;cost = 1;}};
        yazhenSeed = new Item("yazhen-seed") {{color = Color.valueOf("0e6251");hardness = 0;cost = 1;}};
        redjadeGinseng = new Item("redjade-ginseng") {{color = Color.valueOf("c44569");hardness = 2;cost = 1;}};
        redjadeGinsengSeed = new Item("redjade-ginseng-seed") {{color = Color.valueOf("b5345e");hardness = 0;cost = 1;}};
        amberRice = new Item("amber-rice") {{color = Color.valueOf("f6e58d");hardness = 1;cost = 1;}};
        amberRiceSeed = new Item("amber-rice-seed") {{color = Color.valueOf("f1c40f");hardness = 0;cost = 1;}};

        // 粉末
        buckflowerPowder = new Item("buckflower-powder") {{color = Color.valueOf("e8c4c4");hardness = 0;cost = 1;}};
        firebucklePowder = new Item("firebuckle-powder") {{color = Color.valueOf("f5b041");hardness = 0;cost = 1;}};
        groundBuckflowerPowder = new Item("ground-buckflower-powder") {{color = Color.valueOf("d7b19d");hardness = 1;cost = 1;}};
        citromePowder = new Item("citrome-powder") {{color = Color.valueOf("f7dc6f");hardness = 0;cost = 1;}};
        citromix = new Item("citromix") {{color = Color.valueOf("f39c12");hardness = 1;cost = 1;}};
        groundCitromePowder = new Item("ground-citrome-powder") {{color = Color.valueOf("d4ac0d");hardness = 1;cost = 1;}};
        aketinePowder = new Item("aketine-powder") {{color = Color.valueOf("82e0aa");hardness = 0;cost = 1;}};
        sandleafPowder = new Item("sandleaf-powder") {{color = Color.valueOf("f7dc6f");hardness = 0;cost = 1;}};
        jincaoPowder = new Item("jincao-powder") {{color = Color.valueOf("a9dfbf");hardness = 0;cost = 1;}};
        fluffedJincaoPowder = new Item("fluffed-jincao-powder") {{color = Color.valueOf("f8c471");hardness = 1;cost = 1;}};
        yazhenPowder = new Item("yazhen-powder") {{color = Color.valueOf("a3e4d7");hardness = 0;cost = 1;}};
        thornyYazhenPowder = new Item("thorny-yazhen-powder") {{color = Color.valueOf("76d7c4");hardness = 1;cost = 1;}};
        carbonPowder = new Item("carbon-powder") {{color = Color.valueOf("5d6d7e");hardness = 0;cost = 1;}};
        denseCarbonPowder = new Item("dense-carbon-powder") {{color = Color.valueOf("2e4053");hardness = 1;cost = 1;}};
        originiumPowder = new Item("originium-powder") {{color = Color.valueOf("b03a2e");hardness = 0;cost = 1;}};
        origocrustPowder = new Item("origocrust-powder") {{color = Color.valueOf("784212");hardness = 0;cost = 1;}};
        denseOriginiumPowder = new Item("dense-originium-powder") {{color = Color.valueOf("943126");hardness = 1;cost = 1;}};
        denseOrigocrustPowder = new Item("dense-origocrust-powder") {{color = Color.valueOf("5d3a1a");hardness = 1;cost = 1;}};
        amethystPowder = new Item("amethyst-powder") {{color = Color.valueOf("c39bd3");hardness = 0;cost = 1;}};
        crystonPowder = new Item("cryston-powder") {{color = Color.valueOf("a569bd");hardness = 1;cost = 1;}};
        ferriumPowder = new Item("ferrium-powder") {{color = Color.valueOf("85c1e2");hardness = 0;cost = 1;}};
        denseFerriumPowder = new Item("dense-ferrium-powder") {{color = Color.valueOf("3498db");hardness = 1;cost = 1;}};

        // 工业产物
        carbon = new Item("carbon") {{color = Color.valueOf("2c3e50");hardness = 2;cost = 1;}};
        stabilizedCarbon = new Item("stabilized-carbon") {{color = Color.valueOf("1e2b37");hardness = 3;cost = 1;}};
        origocrust = new Item("origocrust") {{color = Color.valueOf("bdc3c7");hardness = 2;cost = 1;}};
        packedOrigocrust = new Item("packed-origocrust") {{color = Color.valueOf("ecf0f1");hardness = 3;cost = 1;}};
        amethystFiber = new Item("amethyst-fiber") {{color = Color.valueOf("9b59b6");hardness = 1;cost = 1;}};
        crystonFiber = new Item("cryston-fiber") {{color = Color.valueOf("8e44ad");hardness = 2;cost = 1;}};
        ferrium = new Item("ferrium") {{color = Color.valueOf("3498db");hardness = 3;cost = 1;}};
        steel = new Item("steel") {{color = Color.valueOf("7f8c8d");hardness = 4;cost = 1;}};

        // 电池
        lcValleyBattery = new Item("lc-valley-battery") {{color = Color.valueOf("7dcea0");hardness = 0;cost = 1;}};
        scValleyBattery = new Item("sc-valley-battery") {{color = Color.valueOf("52be80");hardness = 0;cost = 1;}};
        hcValleyBattery = new Item("hc-valley-battery") {{color = Color.valueOf("27ae60");hardness = 0;cost = 1;}};
        lcWulingBattery = new Item("lc-wuling-battery") {{color = Color.valueOf("85c1e2");hardness = 0;cost = 1;}};
        //scWulingBattery = new Item("lc-wuling-battery") {{color = Color.valueOf("85c1e2");hardness = 0;cost = 1;}};
        //hcWulingBattery = new Item("lc-wuling-battery") {{color = Color.valueOf("85c1e2");hardness = 0;cost = 1;}};

        // 零件
        amethystPart = new Item("amethyst-part") {{color = Color.valueOf("9b59b6");hardness = 2;cost = 1;}};
        ferriumPart = new Item("ferrium-part") {{color = Color.valueOf("3498db");hardness = 3;cost = 1;}};
        steelPart = new Item("steel-part") {{color = Color.valueOf("7f8c8d");hardness = 4;cost = 1;}};
        crystonPart = new Item("cryston-part") {{color = Color.valueOf("8e44ad");hardness = 3;cost = 1;}};

        // 装备原件
        amethystComponent = new Item("amethyst-component") {{color = Color.valueOf("9b59b6");hardness = 2;cost = 1;}};
        ferriumComponent = new Item("ferrium-component") {{color = Color.valueOf("3498db");hardness = 3;cost = 1;}};
        crystonComponent = new Item("cryston-component") {{color = Color.valueOf("8e44ad");hardness = 3;cost = 1;}};
        xiraniteComponent = new Item("xiranite-component") {{color = Color.valueOf("d4ac0d");hardness = 4;cost = 1;}};

        // 瓶子
        amethystBottle = new Item("amethyst-bottle") {{color = Color.valueOf("c39bd3");hardness = 0;cost = 1;}};
        crystonBottle = new Item("cryston-bottle") {{color = Color.valueOf("a569bd");hardness = 0;cost = 1;}};
        ferriumBottle = new Item("ferrium-bottle") {{color = Color.valueOf("85c1e2");hardness = 0;cost = 1;}};
        steelBottle = new Item("steel-bottle") {{color = Color.valueOf("85c1e2");hardness = 0;cost = 1;}};
    }
}