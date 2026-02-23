package endfieldindustrylib.Items;

import arc.graphics.Color;
import mindustry.type.Item;

public class Components {
    public static Item
            amethystComponent,
            ferriumComponent,
            crystonComponent,
            xiraniteComponent;

    public static void load() {
        // 紫晶装备原件
        amethystComponent = new Item("amethyst-component") {{
            color = Color.valueOf("9b59b6"); // 紫晶色
            hardness = 2;
            cost = 1;
        }};

        // 蓝铁装备原件
        ferriumComponent = new Item("ferrium-component") {{
            color = Color.valueOf("3498db"); // 蓝铁色
            hardness = 3;
            cost = 1;
        }};

        // 高晶装备原件
        crystonComponent = new Item("cryston-component") {{
            color = Color.valueOf("8e44ad"); // 亮紫色
            hardness = 3;
            cost = 1;
        }};

        // 息壤装备原件
        xiraniteComponent = new Item("xiranite-component") {{
            color = Color.valueOf("d4ac0d"); // 暗金色/土黄
            hardness = 4;
            cost = 1;
        }};
    }
}