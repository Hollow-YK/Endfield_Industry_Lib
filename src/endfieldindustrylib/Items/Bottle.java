package endfieldindustrylib.Items;

import arc.graphics.Color;
import mindustry.type.Item;

public class Bottle {
    public static Item
            amethystBottle,
            ferriumBottle,
            crystonBottle;

    public static void load() {
        // 紫晶质瓶
        amethystBottle = new Item("amethyst-bottle") {{
            color = Color.valueOf("c39bd3"); // 浅紫晶色
            hardness = 0;
            cost = 1;
        }};

        // 蓝铁瓶
        ferriumBottle = new Item("ferrium-bottle") {{
            color = Color.valueOf("85c1e2"); // 浅蓝铁色
            hardness = 0;
            cost = 1;
        }};

        // 高晶质瓶
        crystonBottle = new Item("cryston-bottle") {{
            color = Color.valueOf("a569bd"); // 浅高晶紫色
            hardness = 0;
            cost = 1;
        }};
    }
}