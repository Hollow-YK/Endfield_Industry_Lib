package endfieldindustrylib.EFworld.blocks.AICPower;

import arc.struct.ObjectMap;
import endfieldindustrylib.EFcontents.EFitems;
import mindustry.gen.Building;
import mindustry.type.*;
import mindustry.world.blocks.power.ConsumeGenerator;
import mindustry.world.consumers.ConsumeItemCharged;

public class ThermalBank extends ConsumeGenerator {
    // 物品 → 每秒发电量（用于计算每 tick 功率）
    public final ObjectMap<Item, Float> powerPerSecond = new ObjectMap<>();
    // 物品 → 燃烧时长倍数（基础时长 40 秒）
    public final ObjectMap<Item, Float> timeMultiplierMap = new ObjectMap<>();

    public ThermalBank(String name) {
        super(name);
        size = 2;               // 2x2
        rotate = true;           // 可转向
        itemCapacity = 50;       // 物品容量 50
        hasItems = true;         // 启用物品存储
        hasLiquids = false;      // 不涉及液体
        powerProduction = 16.66667f;     // 基础功率设为 0，完全由 getPowerProduction 控制
        itemDuration = 2400f;    // 基础燃烧时间 40 秒（2400 ticks）
        itemDurationMultipliers.put(EFitems.originiumOre,0.2f);
        // 建造需求（请确保物品已定义）
        requirements(Category.power, ItemStack.with(EFitems.origocrust, 5, EFitems.amethystPart, 5));
        consume(new ConsumeItemCharged(0.05f));
    }

    public class ThermalBankBuild extends ConsumeGeneratorBuild {

        @Override
        public boolean acceptItem(Building source, Item item) {
            // 1. 必须从背面输入
            int relative = source.relativeTo(tile);
            if (relative != ((rotation + 2) % 4)) return false;

            // 2. 容量检查
            if (items.total() >= itemCapacity) return false;

            // 3. 类型一致性检查
            if (items.total() == 0) {
                // 空仓：可接受任何物品（包括不可燃物品，但不可燃物品无法发电）
                return true;
            } else {
                // 非空仓：必须与现有物品类型相同
                Item existing = items.first();
                return item == existing;
            }
        }
    }
}