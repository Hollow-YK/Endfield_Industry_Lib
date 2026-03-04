package endfieldindustrylib;

import endfieldindustrylib.EFcontents.EFblocks;
import endfieldindustrylib.EFcontents.EFitems;

public class Lib extends mindustry.mod.Mod {

    @Override
    public void loadContent() {
        // item
        EFitems.load();

        // block
        EFblocks.load();
    }
}