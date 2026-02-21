package endfieldindustrylib.Items;

import arc.graphics.Color;
import mindustry.type.Item;

public class Plants {
    public static Item buckflower, firebuckle, buckflowerSeed,
            citrome, umbraline, citromeSeed,
            aketine, aketineSeed,
            sandleaf, sandleafSeed,
            tartpepper, tartpepperSeed,
            reedRye, reedRyeSeed,
            jincao, fluffedJincao, jincaoSeed,
            yazhen, thornyYazhen, yazhenSeed,
            redjadeGinseng, redjadeGinsengSeed,
            amberRice, amberRiceSeed;

    public static void load() {
        // 荞花系列
        buckflower = new Item("buckflower") {{
            color = Color.valueOf("d4a5a5"); // 淡粉色
            hardness = 1;
            cost = 1;
        }};
        firebuckle = new Item("firebuckle") {{
            color = Color.valueOf("e67e22"); // 橙色
            hardness = 1;
            cost = 1;
        }};
        buckflowerSeed = new Item("buckflower-seed") {{
            color = Color.valueOf("b87333"); // 铜色
            hardness = 0;
            cost = 1;
        }};

        // 柑实系列
        citrome = new Item("citrome") {{
            color = Color.valueOf("f1c40f"); // 黄色
            hardness = 1;
            cost = 1;
        }};
        umbraline = new Item("umbraline") {{
            color = Color.valueOf("2c3e50"); // 深蓝灰
            hardness = 1;
            cost = 1;
        }};
        citromeSeed = new Item("citrome-seed") {{
            color = Color.valueOf("7d3c98"); // 紫色
            hardness = 0;
            cost = 1;
        }};

        // 酮化灌木系列
        aketine = new Item("aketine") {{
            color = Color.valueOf("27ae60"); // 绿色
            hardness = 1;
            cost = 1;
        }};
        aketineSeed = new Item("aketine-seed") {{
            color = Color.valueOf("1e8449"); // 深绿
            hardness = 0;
            cost = 1;
        }};

        // 砂叶系列
        sandleaf = new Item("sandleaf") {{
            color = Color.valueOf("f4d03f"); // 沙黄色
            hardness = 1;
            cost = 1;
        }};
        sandleafSeed = new Item("sandleaf-seed") {{
            color = Color.valueOf("b7950b"); // 暗黄
            hardness = 0;
            cost = 1;
        }};

        // 苦叶椒系列
        tartpepper = new Item("tartpepper") {{
            color = Color.valueOf("e74c3c"); // 红色
            hardness = 1;
            cost = 1;
        }};
        tartpepperSeed = new Item("tartpepper-seed") {{
            color = Color.valueOf("c0392b"); // 暗红
            hardness = 0;
            cost = 1;
        }};

        // 灰芦麦系列
        reedRye = new Item("reed-rye") {{
            color = Color.valueOf("95a5a6"); // 灰色
            hardness = 1;
            cost = 1;
        }};
        reedRyeSeed = new Item("reed-rye-seed") {{
            color = Color.valueOf("7f8c8d"); // 暗灰
            hardness = 0;
            cost = 1;
        }};

        // 锦草系列
        jincao = new Item("jincao") {{
            color = Color.valueOf("2ecc71"); // 亮绿
            hardness = 1;
            cost = 1;
        }};
        fluffedJincao = new Item("fluffed-jincao") {{
            color = Color.valueOf("f39c12"); // 橙色
            hardness = 1;
            cost = 1;
        }};
        jincaoSeed = new Item("jincao-seed") {{
            color = Color.valueOf("d35400"); // 橙红
            hardness = 0;
            cost = 1;
        }};

        // 芽针系列
        yazhen = new Item("yazhen") {{
            color = Color.valueOf("1abc9c"); // 青绿
            hardness = 1;
            cost = 1;
        }};
        thornyYazhen = new Item("thorny-yazhen") {{
            color = Color.valueOf("16a085"); // 深青
            hardness = 1;
            cost = 1;
        }};
        yazhenSeed = new Item("yazhen-seed") {{
            color = Color.valueOf("0e6251"); // 墨绿
            hardness = 0;
            cost = 1;
        }};

        // 琼叶参系列
        redjadeGinseng = new Item("redjade-ginseng") {{
            color = Color.valueOf("c44569"); // 粉红
            hardness = 2;
            cost = 1;
        }};
        redjadeGinsengSeed = new Item("redjade-ginseng-seed") {{
            color = Color.valueOf("b5345e"); // 深粉
            hardness = 0;
            cost = 1;
        }};

        // 金石稻系列
        amberRice = new Item("amber-rice") {{
            color = Color.valueOf("f6e58d"); // 浅黄
            hardness = 1;
            cost = 1;
        }};
        amberRiceSeed = new Item("amber-rice-seed") {{
            color = Color.valueOf("f1c40f"); // 金黄
            hardness = 0;
            cost = 1;
        }};
    }
}