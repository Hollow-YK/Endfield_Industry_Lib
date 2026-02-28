package endfieldindustrylib.EFcontents;

import endfieldindustrylib.EFworld.blocks.AICBasicFacility.*;
import endfieldindustrylib.EFworld.blocks.AICPower.*;
import endfieldindustrylib.EFworld.blocks.AICTransport.*;

public class EFblocks {
    public static void load() {
        // 注册矩形多块工厂所需的子方块
        RectGenericAICBasicFacility.registerChildBlock();
        // 物流运输
        new TransportBelt("transport-belt").load();           // 传送带
        new ItemControlPort("item-control-port").load();      // 物品准入口
        new Splitter("splitter").load();                      // 分流器
        new BeltBridge("belt-bridge").load();                 // 物流桥
        new Converger("converger").load();                    // 汇流器
        // 基础生产
        new SeedPickingUnit("seed-picking-unit").load();      //采种机
        new PlantingUnit("planting-unit").load();             //种植机
        new RefiningUnit("refining-unit").load();             //精炼炉
        new ShreddingUnit("shredding-unit").load();           //粉碎机
        new FittingUnit("fitting-unit").load();               //配件机
        new MouldingUnit("moulding-unit").load();             //塑形机
        // 合成制造
        //new GearingUnit("gearing-unit").load();                    //装备原件机
        //new FillingUnit("filling-unit").load();                    //灌装机
        new PackagingUnit("packaging-unit", 3, 5).load();           //封装机
        //new GrindingUnit("grinding-unit").load();                  //研磨机
        //new SeparatingUnit("separating-unit").load();              //拆解机
        //new ReactorCrucible("reactor-crucible").load();            //反应池
        //new ForgeoftheSky("forge-of-the-sky").load();              //天有洪炉
        //电力供应
        //new RelayTower("relay-tower").load();                      //中继器
        //new ElectricPylon("electric-pylon").load();                //供电桩
        new ThermalBank("thermal-bank").load();               //热能池
        //new XiraniteRelay("xiranite-relay").load();                //息壤中继器
        //new XiranitePylon("xiranite-pylon").load();                //息壤供电桩
    }
}