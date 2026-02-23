package endfieldindustrylib.Items;

import arc.graphics.Color;
import mindustry.type.Item;

public class Industrial {
    public static Item
            carbonBlock, stabilizedCarbon,
            origocrust, packedOrigocrust,
            amethystFiber, crystonFiber,
            ferriumBlock, steelBlock;

    public static void load() {
        // 碳块
        carbonBlock = new Item("carbon") {{
            color = Color.valueOf("2c3e50"); // 深灰
            hardness = 2;
            cost = 1;
        }};

        // 稳定炭块
        stabilizedCarbon = new Item("stabilized-carbon") {{
            color = Color.valueOf("1e2b37"); // 更深灰
            hardness = 3;
            cost = 1;
        }};

        // 晶体外壳
        origocrust = new Item("origocrust") {{
            color = Color.valueOf("bdc3c7"); // 银灰
            hardness = 2;
            cost = 1;
        }};

        // 密制晶体
        packedOrigocrust = new Item("packed-origocrust") {{
            color = Color.valueOf("ecf0f1"); // 亮银
            hardness = 3;
            cost = 1;
        }};

        // 紫晶纤维
        amethystFiber = new Item("amethyst-fiber") {{
            color = Color.valueOf("9b59b6"); // 紫
            hardness = 1;
            cost = 1;
        }};

        // 高晶纤维
        crystonFiber = new Item("cryston-fiber") {{
            color = Color.valueOf("8e44ad"); // 亮紫
            hardness = 2;
            cost = 1;
        }};

        // 蓝铁块
        ferriumBlock = new Item("ferrium") {{
            color = Color.valueOf("3498db"); // 蓝
            hardness = 3;
            cost = 1;
        }};

        // 钢块
        steelBlock = new Item("steel") {{
            color = Color.valueOf("7f8c8d"); // 金属灰
            hardness = 4;
            cost = 1;
        }};
    }
}