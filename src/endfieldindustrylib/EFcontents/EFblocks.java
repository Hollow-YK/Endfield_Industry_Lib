package endfieldindustrylib.EFcontents;

import arc.graphics.Color;
import endfieldindustrylib.EFcontents.EFenv.HarvestablePlant;
import endfieldindustrylib.EFcontents.EFenv.OreStone;
import endfieldindustrylib.EFworld.blocks.AICBasicFacility.*;
import endfieldindustrylib.EFworld.blocks.AICDepotAccess.ProtocolStash;
import endfieldindustrylib.EFworld.blocks.AICErosion.ActiveBlight;
import endfieldindustrylib.EFworld.blocks.AICErosion.BlightCore;
import endfieldindustrylib.EFworld.blocks.AICPower.ElectricPylon;
import endfieldindustrylib.EFworld.blocks.AICPower.RelayTower;
import endfieldindustrylib.EFworld.blocks.AICPower.ThermalBank;
import endfieldindustrylib.EFworld.blocks.AICTransport.*;
import endfieldindustrylib.EFworld.blocks.AICTurret.*;
import endfieldindustrylib.EFworld.blocks.AICExtraction.VentDrill;
import mindustry.content.Blocks;
import mindustry.type.Planet;
import mindustry.world.blocks.environment.SteamVent;
import mindustry.world.meta.Attribute;

public class EFblocks {
    // ===== 自定义环境属性（矿物喷口用） =====
    public static Attribute originiumAttribute;
    public static Attribute amethystAttribute;
    public static Attribute ferriumAttribute;

    // ===== 物流运输 =====
    public static TransportBelt transportBelt;
    public static ItemControlPort itemControlPort;
    public static Splitter splitter;
    public static BeltBridge beltBridge;
    public static Converger converger;
    public static ProtocolStash protocolStash;
    // ===== 基础生产 =====
    public static SeedPickingUnit seedPickingUnit;
    public static PlantingUnit plantingUnit;
    public static RefiningUnit refiningUnit;
    public static ShreddingUnit shreddingUnit;
    public static FittingUnit fittingUnit;
    public static MouldingUnit mouldingUnit;
    // ===== 合成制造 =====
    public static PackagingUnit packagingUnit;
    public static GrindingUnit grindingUnit;
    //public static GearingUnit gearingUnit;        //装备原件机
    //public static FillingUnit fillingUnit;        //灌装机
    //public static SeparatingUnit separatingUnit;        //拆解机
    //public static ReactorCrucible reactorCrucible;        //反应池
    //public static ForgeoftheSky forgeoftheSky;        //天有洪炉
    // ===== 电力供应 =====
    public static RelayTower relayTower;
    public static ElectricPylon electricPylon;
    public static ThermalBank thermalBank;
    //public static XiraniteRelay xiraniteRelay;        //息壤中继器
    //public static XiranitePylon xiranitePylon;        //息壤供电桩
    // ===== 战斗辅助 =====
    public static GunTower gunTower;
    public static GrenadeTower grenadeTower;
    public static LNTower lnTower;
    public static HeavyGunTower heavyGunTower;
    public static OmnidirectionalSonicTower omnidirectionalSonicTower;
    public static BeamTower beamTower;
    public static SurgeTower surgeTower;
    public static SentryTower sentryTower;
    public static SteamVent originiumSpot;
    public static SteamVent amethystSpot;
    public static SteamVent ferriumSpot;
    // ===== 侵蚀结构（战役专属） =====
    public static BlightCore blightCore;
    public static ActiveBlight activeBlight;
    // ===== 矿石 =====
    public static OreStone originiumStone;
    public static OreStone amethystStone;
    public static OreStone ferriumStone;
    // ===== 可采集植株 =====
    public static HarvestablePlant aketinePlant;
    public static HarvestablePlant sandleafPlant;
    // ===== 喷口矿机 =====
    public static VentDrill originiumVentDrill;
    public static VentDrill amethystVentDrill;
    public static VentDrill ferriumVentDrill;

    public static void load() {
        // 注册矩形多块工厂所需的子方块
        RectGenericAICBasicFacility.registerChildBlock();
        // 物流运输
        transportBelt = new TransportBelt("transport-belt");              // 传送带
        transportBelt.load();
        itemControlPort = new ItemControlPort("item-control-port");       // 物品准入口
        itemControlPort.load();
        splitter = new Splitter("splitter");                              // 分流器
        splitter.load();
        beltBridge = new BeltBridge("belt-bridge");                       // 物流桥
        beltBridge.load();
        converger = new Converger("converger");                           // 汇流器
        converger.load();
        protocolStash = new ProtocolStash("protocol-stash");              // 协议储存箱
        protocolStash.load();
        // 基础生产
        seedPickingUnit = new SeedPickingUnit("seed-picking-unit");       // 采种机
        seedPickingUnit.load();
        plantingUnit = new PlantingUnit("planting-unit");                 // 种植机
        plantingUnit.load();
        refiningUnit = new RefiningUnit("refining-unit");                 // 精炼炉
        refiningUnit.load();
        shreddingUnit = new ShreddingUnit("shredding-unit");              // 粉碎机
        shreddingUnit.load();
        fittingUnit = new FittingUnit("fitting-unit");                    // 配件机
        fittingUnit.load();
        mouldingUnit = new MouldingUnit("moulding-unit");                 // 塑形机
        mouldingUnit.load();
        // 合成制造
        //gearingUnit = new GearingUnit("gearing-unit", 4, 6);        //装备原件机
        //gearingUnit.load();
        //fillingUnit = new FillingUnit("filling-unit", 4, 6);        //灌装机
        //fillingUnit.load();
        packagingUnit = new PackagingUnit("packaging-unit", 4, 6);        // 封装机
        packagingUnit.load();
        grindingUnit = new GrindingUnit("grinding-unit", 4, 6);           // 研磨机
        grindingUnit.load();
        //separatingUnit = new SeparatingUnit("separating-unit", 4, 6);        //拆解机
        //separatingUnit.load();
        //reactorCrucible = new ReactorCrucible("reactor-crucible", 4, 6);        //反应池
        //reactorCrucible.load();
        //forgeoftheSky = new ForgeoftheSky("forge-of-the-sky");        //天有洪炉
        //forgeoftheSky.load();
        // 电力供应
        relayTower = new RelayTower("relay-tower");                       // 中继器
        relayTower.load();
        electricPylon = new ElectricPylon("electric-pylon");              // 供电桩
        electricPylon.load();
        thermalBank = new ThermalBank("thermal-bank");                    // 热能池
        thermalBank.load();
        //xiraniteRelay = new XiraniteRelay("xiranite-relay");        //息壤中继器
        //xiraniteRelay.load();
        //xiranitePylon = new XiranitePylon("xiranite-pylon");        //息壤供电桩
        //xiranitePylon.load(); 
        // 战斗辅助
        gunTower = new GunTower("gun-tower");                              //铳械塔
        gunTower.load();
        grenadeTower = new GrenadeTower("grenade-tower");                  //榴弹塔
        grenadeTower.load();
        lnTower = new LNTower("ln-tower");                                 //液氮塔
        lnTower.load();
        heavyGunTower = new HeavyGunTower("heavy-gun-tower");              //扩装铳械塔
        heavyGunTower.load();
        omnidirectionalSonicTower = new OmnidirectionalSonicTower("omnidirectional-sonic-tower"); //全向声波塔
        omnidirectionalSonicTower.load();
        beamTower = new BeamTower("beam-tower");                           //射线塔
        beamTower.load();
        surgeTower = new SurgeTower("surge-tower");                        //电涌塔
        surgeTower.load();
        sentryTower = new SentryTower("sentry-tower");                     //哨戒塔
        sentryTower.load();
         originiumAttribute = Attribute.add("originium");
        amethystAttribute = Attribute.add("amethyst");
        ferriumAttribute = Attribute.add("ferrium");
        // 矿物喷口
        originiumSpot = new SteamVent("originium-spot"){{
            parent = blendGroup = Blocks.stone;
            attributes.set(originiumAttribute, 1f);
            effectSpacing = Float.MAX_VALUE; // 关闭蒸汽粒子
            emitLight = true;
            lightRadius = 35f;
            lightColor = Color.valueOf("ffe066"); // 黄光
        }};
        amethystSpot = new SteamVent("amethyst-spot"){{
            parent = blendGroup = Blocks.stone;
            attributes.set(amethystAttribute, 1f);
            effectSpacing = Float.MAX_VALUE;
            emitLight = true;
            lightRadius = 35f;
            lightColor = Color.valueOf("c084fc"); // 紫光
        }};
        ferriumSpot = new SteamVent("ferrium-spot"){{
            parent = blendGroup = Blocks.stone;
            attributes.set(ferriumAttribute, 1f);
            effectSpacing = Float.MAX_VALUE;
            emitLight = true;
            lightRadius = 35f;
            lightColor = Color.valueOf("7cb9f0"); // 蓝光
        }};
        // 注册矿物喷口类型（attribute → item → tier），供 VentDrill 等级系统使用
        // 等级 1: 源矿喷口 | 等级 2: 紫水晶喷口 | 等级 3: 铁锭喷口
        VentDrill.registerVentType(originiumAttribute, EFitems.originiumOre, 1);
        VentDrill.registerVentType(amethystAttribute, EFitems.amethystOre, 2);
        VentDrill.registerVentType(ferriumAttribute, EFitems.ferriumOre, 3);
        // 侵蚀结构（战役专属）
        blightCore = new BlightCore("blight-core");
        blightCore.load();
        activeBlight = new ActiveBlight("active-blight");
        activeBlight.load();
        // 矿化岩石
        originiumStone = new OreStone("originium-stone", EFitems.originiumOre);
        amethystStone = new OreStone("amethyst-stone", EFitems.amethystOre);
        ferriumStone = new OreStone("ferrium-stone", EFitems.ferriumOre);
        // 可采集植株
        aketinePlant = new HarvestablePlant("aketine-plant", EFitems.aketine);
        sandleafPlant = new HarvestablePlant("sandleaf-plant", EFitems.sandleaf);
        // 喷口矿机（等级 1: 仅源矿 | 等级 2: 源矿+紫水晶 | 等级 3: 全部）
        originiumVentDrill = new VentDrill("originium-vent-drill") {{
            mineLevel = 1;
            size = 2;
        }};
        originiumVentDrill.load();
        amethystVentDrill = new VentDrill("amethyst-vent-drill") {{
            mineLevel = 2;
            size = 2;
        }};
        amethystVentDrill.load();
        ferriumVentDrill = new VentDrill("ferrium-vent-drill") {{
            mineLevel = 3;
            size = 2;
        }};
        ferriumVentDrill.load();
    }

    /** 将所有方块注册到指定星球 */
    public static void registerToPlanet(Planet planet) {
        transportBelt.shownPlanets.add(planet);
        itemControlPort.shownPlanets.add(planet);
        splitter.shownPlanets.add(planet);
        beltBridge.shownPlanets.add(planet);
        converger.shownPlanets.add(planet);
        protocolStash.shownPlanets.add(planet);
        seedPickingUnit.shownPlanets.add(planet);
        plantingUnit.shownPlanets.add(planet);
        refiningUnit.shownPlanets.add(planet);
        shreddingUnit.shownPlanets.add(planet);
        fittingUnit.shownPlanets.add(planet);
        mouldingUnit.shownPlanets.add(planet);
        packagingUnit.shownPlanets.add(planet);
        grindingUnit.shownPlanets.add(planet);
        electricPylon.shownPlanets.add(planet);
        relayTower.shownPlanets.add(planet);
        thermalBank.shownPlanets.add(planet);
        gunTower.shownPlanets.add(planet);
        grenadeTower.shownPlanets.add(planet);
        lnTower.shownPlanets.add(planet);
        heavyGunTower.shownPlanets.add(planet);
        omnidirectionalSonicTower.shownPlanets.add(planet);
        beamTower.shownPlanets.add(planet);
        surgeTower.shownPlanets.add(planet);
        sentryTower.shownPlanets.add(planet);
        originiumSpot.shownPlanets.add(planet);
        amethystSpot.shownPlanets.add(planet);
        ferriumSpot.shownPlanets.add(planet);
        originiumStone.shownPlanets.add(planet);
        amethystStone.shownPlanets.add(planet);
        ferriumStone.shownPlanets.add(planet);
        aketinePlant.shownPlanets.add(planet);
        sandleafPlant.shownPlanets.add(planet);
        originiumVentDrill.shownPlanets.add(planet);
        amethystVentDrill.shownPlanets.add(planet);
        ferriumVentDrill.shownPlanets.add(planet);
    }
}