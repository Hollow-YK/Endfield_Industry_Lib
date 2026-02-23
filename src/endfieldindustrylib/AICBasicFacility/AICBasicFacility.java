package endfieldindustrylib.AICBasicFacility;

public class AICBasicFacility {
    public static void load() {
        // 基础生产
        new SeedPickingUnit("seed-picking-unit").load(); //采种机
        new PlantingUnit("planting-unit").load();        //种植机
        new RefiningUnit("refining-unit").load();        //精炼炉
        new ShreddingUnit("shredding-unit").load();      //粉碎机
        new FittingUnit("fitting-unit").load();          //配件机
        new MouldingUnit("moulding-unit").load();        //塑形机
        // 合成制造
        //new GearingUnit("gearing-unit").load();               //装备原件机
        //new FillingUnit("filling-unit").load();               //灌装机
        //new PackagingUnit("packaging-unit").load();           //封装机
        //new GrindingUnit("grinding-unit").load();             //研磨机
        //new SeparatingUnit("separating-unit").load();         //拆解机
        //new ReactorCrucible("reactor-crucible").load();       //反应池
        //new ForgeoftheSky("forge-of-the-sky").load();         //天有洪炉
    }
}