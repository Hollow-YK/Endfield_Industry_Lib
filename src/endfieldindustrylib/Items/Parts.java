package endfieldindustrylib.Items;

import arc.graphics.Color;
import mindustry.type.Item;

public class Parts {
    public static Item
            amethystPart,
            ferriumPart,
            steelPart,
            crystonPart;

    public static void load() {
        // 紫晶零件
        amethystPart = new Item("amethyst-part") {{
            color = Color.valueOf("9b59b6"); // 紫晶色
            hardness = 2;
            cost = 1;
        }};

        // 铁质零件
        ferriumPart = new Item("ferrium-part") {{
            color = Color.valueOf("3498db"); // 蓝铁色
            hardness = 3;
            cost = 1;
        }};

        // 钢质零件
        steelPart = new Item("steel-part") {{
            color = Color.valueOf("7f8c8d"); // 钢灰色
            hardness = 4;
            cost = 1;
        }};

        // 高晶零件
        crystonPart = new Item("cryston-part") {{
            color = Color.valueOf("8e44ad"); // 亮紫色
            hardness = 3;
            cost = 1;
        }};
    }
}