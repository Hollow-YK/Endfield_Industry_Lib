package endfieldindustrylib.AICBasicFacility;

import arc.struct.ObjectMap;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.type.*;
import mindustry.world.blocks.power.ConsumeGenerator;
import mindustry.world.consumers.ConsumeItemEfficiency;
import endfieldindustrylib.Items.EFItems;

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
        powerProduction = 0f;     // 基础功率设为 0，完全由 getPowerProduction 控制
        itemDuration = 2400f;    // 基础燃烧时间 40 秒（2400 ticks）
        // 建造需求（请确保物品已定义）
        requirements(Category.power, ItemStack.with(EFItems.origocrust, 5, EFItems.amethystPart, 5));
    }

    @Override
    public void init() {
        // 填充物品每秒发电量
        powerPerSecond.put(EFItems.originiumOre, 50f);
        powerPerSecond.put(EFItems.lcValleyBattery, 220f);
        powerPerSecond.put(EFItems.scValleyBattery, 420f);
        powerPerSecond.put(EFItems.hcValleyBattery, 1100f);
        powerPerSecond.put(EFItems.lcWulingBattery, 1600f);

        // 填充物品燃烧时长倍数（实际秒数 / 40）
        timeMultiplierMap.put(EFItems.originiumOre, 0.2f);  // 8 / 40
        timeMultiplierMap.put(EFItems.lcValleyBattery, 1f); // 40 / 40
        timeMultiplierMap.put(EFItems.scValleyBattery, 1f);
        timeMultiplierMap.put(EFItems.hcValleyBattery, 1f);
        timeMultiplierMap.put(EFItems.lcWulingBattery, 1f);

        // 设置燃烧时长乘数（影响 generateTime 的递减速度）
        itemDurationMultipliers.clear();
        for (Item item : timeMultiplierMap.keys()) {
            itemDurationMultipliers.put(item, timeMultiplierMap.get(item));
        }

        // 添加物品消耗器：仅用于过滤可燃烧物品（不提供效率乘数）
        consume(new ConsumeItemEfficiency(item -> powerPerSecond.containsKey(item)));

        super.init();
    }

    public class ThermalBankBuild extends ConsumeGeneratorBuild {
        private Item currentFuel = null; // 当前燃烧的物品

        @Override
        public void updateTile() {
            float oldGen = generateTime;
            // 记录消耗前的物品栏第一个物品（即将被消耗的可能物品）
            Item possibleFuel = items.total() > 0 ? items.first() : null;

            // 调用父类更新，它会执行消耗逻辑并更新 generateTime
            super.updateTile();

            // 如果刚刚消耗了一个物品（generateTime 从 <=0 变为 >0），设置当前燃料
            if (oldGen <= 0f && generateTime > 0f && possibleFuel != null) {
                currentFuel = possibleFuel;
            }

            // 如果燃烧结束，清除燃料记录
            if (generateTime <= 0f) {
                currentFuel = null;
            }
        }

        @Override
        public float getPowerProduction() {
            // 返回每 tick 的发电量 = 每秒功率 / 60
            if (generateTime > 0f && currentFuel != null) {
                Float powerSec = powerPerSecond.get(currentFuel);
                if (powerSec != null) {
                    return powerSec / 60f;
                }
            }
            return 0f;
        }

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

        @Override
        public void write(Writes write) {
            super.write(write);
            write.s(currentFuel == null ? -1 : currentFuel.id);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            int id = read.s();
            currentFuel = id == -1 ? null : Vars.content.item(id);
        }
    }
}