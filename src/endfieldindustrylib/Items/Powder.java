package endfieldindustrylib.Items;

import arc.graphics.Color;
import mindustry.type.Item;

public class Powder {
    public static Item
            buckflowerPowder, firebucklePowder, groundBuckflowerPowder,
            citromePowder, citromix, groundCitromePowder,
            aketinePowder,
            sandleafPowder,
            jincaoPowder, fluffedJincaoPowder,
            yazhenPowder, thornyYazhenPowder,
            carbonPowder, denseCarbonPowder,
            originiumPowder, origocrustPowder, denseOriginiumPowder, denseOrigocrustPowder,
            amethystPowder, crystonPowder,
            ferriumPowder, denseFerriumPowder;

    public static void load() {
        // 荞花系列粉末
        buckflowerPowder = new Item("buckflower-powder") {{
            color = Color.valueOf("e8c4c4"); // 淡粉
            hardness = 0;
            cost = 1;
        }};
        firebucklePowder = new Item("firebuckle-powder") {{
            color = Color.valueOf("f5b041"); // 橙黄
            hardness = 0;
            cost = 1;
        }};
        groundBuckflowerPowder = new Item("ground-buckflower-powder") {{
            color = Color.valueOf("d7b19d"); // 米色
            hardness = 1;
            cost = 1;
        }};

        // 柑实系列粉末
        citromePowder = new Item("citrome-powder") {{
            color = Color.valueOf("f7dc6f"); // 淡黄
            hardness = 0;
            cost = 1;
        }};
        citromix = new Item("citromix") {{
            color = Color.valueOf("f39c12"); // 橙色冲剂
            hardness = 1;
            cost = 1;
        }};
        groundCitromePowder = new Item("ground-citrome-powder") {{
            color = Color.valueOf("d4ac0d"); // 金黄
            hardness = 1;
            cost = 1;
        }};

        // 酮化灌木粉末
        aketinePowder = new Item("aketine-powder") {{
            color = Color.valueOf("82e0aa"); // 浅绿
            hardness = 0;
            cost = 1;
        }};

        // 砂叶粉末
        sandleafPowder = new Item("sandleaf-powder") {{
            color = Color.valueOf("f7dc6f"); // 沙黄
            hardness = 0;
            cost = 1;
        }};

        // 锦草系列粉末
        jincaoPowder = new Item("jincao-powder") {{
            color = Color.valueOf("a9dfbf"); // 浅绿
            hardness = 0;
            cost = 1;
        }};
        fluffedJincaoPowder = new Item("fluffed-jincao-powder") {{
            color = Color.valueOf("f8c471"); // 橙黄
            hardness = 1;
            cost = 1;
        }};

        // 芽针系列粉末
        yazhenPowder = new Item("yazhen-powder") {{
            color = Color.valueOf("a3e4d7"); // 浅青
            hardness = 0;
            cost = 1;
        }};
        thornyYazhenPowder = new Item("thorny-yazhen-powder") {{
            color = Color.valueOf("76d7c4"); // 青绿
            hardness = 1;
            cost = 1;
        }};

        // 碳粉末
        carbonPowder = new Item("carbon-powder") {{
            color = Color.valueOf("5d6d7e"); // 灰蓝
            hardness = 0;
            cost = 1;
        }};
        denseCarbonPowder = new Item("dense-carbon-powder") {{
            color = Color.valueOf("2e4053"); // 深灰
            hardness = 1;
            cost = 1;
        }};

        // 源石系列粉末
        originiumPowder = new Item("originium-powder") {{
            color = Color.valueOf("b03a2e"); // 暗红
            hardness = 0;
            cost = 1;
        }};
        origocrustPowder = new Item("origocrust-powder") {{
            color = Color.valueOf("784212"); // 棕褐
            hardness = 0;
            cost = 1;
        }};
        denseOriginiumPowder = new Item("dense-originium-powder") {{
            color = Color.valueOf("943126"); // 深红
            hardness = 1;
            cost = 1;
        }};
        denseOrigocrustPowder = new Item("dense-origocrust-powder") {{
            color = Color.valueOf("5d3a1a"); // 深棕
            hardness = 1;
            cost = 1;
        }};

        // 紫晶系列粉末
        amethystPowder = new Item("amethyst-powder") {{
            color = Color.valueOf("c39bd3"); // 浅紫
            hardness = 0;
            cost = 1;
        }};
        crystonPowder = new Item("cryston-powder") {{
            color = Color.valueOf("a569bd"); // 紫罗兰
            hardness = 1;
            cost = 1;
        }};

        // 蓝铁系列粉末
        ferriumPowder = new Item("ferrium-powder") {{
            color = Color.valueOf("85c1e2"); // 浅蓝
            hardness = 0;
            cost = 1;
        }};
        denseFerriumPowder = new Item("dense-ferrium-powder") {{
            color = Color.valueOf("3498db"); // 亮蓝
            hardness = 1;
            cost = 1;
        }};
    }
}