package endfieldindustrylib.AICTransport;

public class AICTransport {
    public static void load() {
        new TransportBelt("transport-belt").load();
        new ItemControlPort("item-control-port").load();
        new Splitter("splitter").load();
        new BeltBridge("belt-bridge").load();
        new Converger("converger").load();
    }
}