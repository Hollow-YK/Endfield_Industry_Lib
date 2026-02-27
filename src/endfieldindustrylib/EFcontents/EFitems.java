package endfieldindustrylib.EFcontents;

import arc.graphics.Color;
import mindustry.type.Item;

public class EFitems {
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
        originiumOre = new Item("originium-ore") {{color = Color.valueOf("c66322");hardness = 1;cost = 1;}};
        amethystOre = new Item("amethyst-ore") {{color = Color.valueOf("a55fc4");hardness = 2;cost = 1;}};
        ferriumOre = new Item("ferrium-ore") {{color = Color.valueOf("4f7ebf");hardness = 3;cost = 1;}};

        // 植物
        wood = new Item("wood") {{cost = 1;}};
        buckflower = new Item("buckflower") {{color = Color.valueOf("d4a5a5");cost = 1;}};
        firebuckle = new Item("firebuckle") {{color = Color.valueOf("e67e22");cost = 1;}};
        buckflowerSeed = new Item("buckflower-seed") {{color = Color.valueOf("b87333");cost = 1;}};
        citrome = new Item("citrome") {{color = Color.valueOf("f1c40f");cost = 1;}};
        umbraline = new Item("umbraline") {{color = Color.valueOf("2c3e50");cost = 1;}};
        citromeSeed = new Item("citrome-seed") {{color = Color.valueOf("7d3c98");cost = 1;}};
        aketine = new Item("aketine") {{color = Color.valueOf("27ae60");cost = 1;}};
        aketineSeed = new Item("aketine-seed") {{color = Color.valueOf("1e8449");cost = 1;}};
        sandleaf = new Item("sandleaf") {{color = Color.valueOf("f4d03f");cost = 1;}};
        sandleafSeed = new Item("sandleaf-seed") {{color = Color.valueOf("b7950b");cost = 1;}};
        tartpepper = new Item("tartpepper") {{color = Color.valueOf("e74c3c");cost = 1;}};
        tartpepperSeed = new Item("tartpepper-seed") {{color = Color.valueOf("c0392b");cost = 1;}};
        reedRye = new Item("reed-rye") {{color = Color.valueOf("95a5a6");cost = 1;}};
        reedRyeSeed = new Item("reed-rye-seed") {{color = Color.valueOf("7f8c8d");cost = 1;}};
        jincao = new Item("jincao") {{color = Color.valueOf("2ecc71");cost = 1;}};
        fluffedJincao = new Item("fluffed-jincao") {{color = Color.valueOf("f39c12");cost = 1;}};
        jincaoSeed = new Item("jincao-seed") {{color = Color.valueOf("d35400");cost = 1;}};
        yazhen = new Item("yazhen") {{color = Color.valueOf("1abc9c");cost = 1;}};
        thornyYazhen = new Item("thorny-yazhen") {{color = Color.valueOf("16a085");cost = 1;}};
        yazhenSeed = new Item("yazhen-seed") {{color = Color.valueOf("0e6251");cost = 1;}};
        redjadeGinseng = new Item("redjade-ginseng") {{color = Color.valueOf("c44569");cost = 1;}};
        redjadeGinsengSeed = new Item("redjade-ginseng-seed") {{color = Color.valueOf("b5345e");cost = 1;}};
        amberRice = new Item("amber-rice") {{color = Color.valueOf("f6e58d");cost = 1;}};
        amberRiceSeed = new Item("amber-rice-seed") {{color = Color.valueOf("f1c40f");cost = 1;}};

        // 粉末
        buckflowerPowder = new Item("buckflower-powder") {{color = Color.valueOf("e8c4c4");cost = 1;}};
        firebucklePowder = new Item("firebuckle-powder") {{color = Color.valueOf("f5b041");cost = 1;}};
        groundBuckflowerPowder = new Item("ground-buckflower-powder") {{color = Color.valueOf("d7b19d");cost = 1;}};
        citromePowder = new Item("citrome-powder") {{color = Color.valueOf("f7dc6f");cost = 1;}};
        citromix = new Item("citromix") {{color = Color.valueOf("f39c12");cost = 1;}};
        groundCitromePowder = new Item("ground-citrome-powder") {{color = Color.valueOf("d4ac0d");cost = 1;}};
        aketinePowder = new Item("aketine-powder") {{color = Color.valueOf("82e0aa");cost = 1;}};
        sandleafPowder = new Item("sandleaf-powder") {{color = Color.valueOf("f7dc6f");cost = 1;}};
        jincaoPowder = new Item("jincao-powder") {{color = Color.valueOf("a9dfbf");cost = 1;}};
        fluffedJincaoPowder = new Item("fluffed-jincao-powder") {{color = Color.valueOf("f8c471");cost = 1;}};
        yazhenPowder = new Item("yazhen-powder") {{color = Color.valueOf("a3e4d7");cost = 1;}};
        thornyYazhenPowder = new Item("thorny-yazhen-powder") {{color = Color.valueOf("76d7c4");cost = 1;}};
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
        lcValleyBattery = new Item("lc-valley-battery") {{color = Color.valueOf("7dcea0");cost = 1;}};
        scValleyBattery = new Item("sc-valley-battery") {{color = Color.valueOf("52be80");cost = 1;}};
        hcValleyBattery = new Item("hc-valley-battery") {{color = Color.valueOf("27ae60");cost = 1;}};
        lcWulingBattery = new Item("lc-wuling-battery") {{color = Color.valueOf("85c1e2");cost = 1;}};
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