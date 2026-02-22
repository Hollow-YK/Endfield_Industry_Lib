package endfieldindustrylib.AICBasicFacility;

public class AICBasicFacility {
    public static void load() {
        new SeedPickingUnit("seed-picking-unit").load();
        new PlantingUnit("planting-unit").load();
        new RefiningUnit("refining-unit").load();
        new ShreddingUnit("shredding-unit").load();
        new FittingUnit("fitting-unit").load();
        new MouldingUnit("moulding-unit").load();
    }
}