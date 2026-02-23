package endfieldindustrylib.Items;

import arc.graphics.Color;
import mindustry.type.Item;

public class Ore {
    public static Item originiumOre, amethystOre, ferriumOre;

    public static void load() {
        originiumOre = new Item("originium-ore") {{
            color = Color.valueOf("c66322");
            hardness = 1;
            cost = 1;
        }};

        amethystOre = new Item("amethyst-ore") {{
            color = Color.valueOf("a55fc4");
            hardness = 2;
            cost = 1;
        }};

        ferriumOre = new Item("ferrium-ore") {{
            color = Color.valueOf("4f7ebf");
            hardness = 3;
            cost = 1;
        }};
    }
}