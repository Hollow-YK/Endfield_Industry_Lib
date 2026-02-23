package endfieldindustrylib.Items;

import arc.graphics.Color;
import mindustry.type.Item;

public class Battery {
    public static Item
            lcValleyBattery,
            scValleyBattery,
            hcValleyBattery,
            lcWulingBattery;
            // scWulingBattery,
            // hcWulingBattery;

    public static void load() {
        // 谷地系列电池
        lcValleyBattery = new Item("lc-valley-battery") {{
            color = Color.valueOf("7dcea0"); // 浅绿
            hardness = 0;
            cost = 1;
        }};

        scValleyBattery = new Item("sc-valley-battery") {{
            color = Color.valueOf("52be80"); // 中绿
            hardness = 0;
            cost = 1;
        }};

        hcValleyBattery = new Item("hc-valley-battery") {{
            color = Color.valueOf("27ae60"); // 深绿
            hardness = 0;
            cost = 1;
        }};

        // 武陵系列电池
        lcWulingBattery = new Item("lc-wuling-battery") {{
            color = Color.valueOf("85c1e2"); // 浅蓝
            hardness = 0;
            cost = 1;
        }};

        // scWulingBattery = new Item("sc-wuling-battery") {{
        //     color = Color.valueOf("5dade2"); // 中蓝
        //     hardness = 0;
        //     cost = 1;
        // }};

        // hcWulingBattery = new Item("hc-wuling-battery") {{
        //     color = Color.valueOf("3498db"); // 深蓝
        //     hardness = 0;
        //     cost = 1;
        // }};
    }
}