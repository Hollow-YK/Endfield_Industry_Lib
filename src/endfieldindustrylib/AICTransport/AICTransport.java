package endfieldindustrylib.AICTransport;

public class AICTransport {
    public static void load() {
        new TransportBelt("endfield-transport-belt").load();
        new ItemControlPort("endfield-item-control-port").load();
        new Splitter("endfield-splitter").load();
        new BeltBridge("endfield-belt-bridge").load();
        new Converger("endfield-converger").load();
    }
}