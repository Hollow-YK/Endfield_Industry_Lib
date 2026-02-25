package endfieldindustrylib.EFworld.blocks.AICTransport;

import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.blocks.distribution.Conveyor;
import mindustry.world.meta.BlockGroup;
import mindustry.type.Item;
import mindustry.gen.Building;
import arc.util.io.*;
import mindustry.Vars;

public class ItemControlPort extends Conveyor {
    public ItemControlPort(String name) {
        super(name);
        speed = 0.008f;
        configurable = true;
        health = 60;
        size = 1;
        itemCapacity = 1;
        noSideBlend = true; // 禁止侧面输入
        group = BlockGroup.transportation;
        category = Category.distribution;
        requirements(Category.distribution, ItemStack.with());
    }

    @Override
    public void init() {
        super.init();
        config(Item.class, (ItemControlPortBuild tile, Item item) -> tile.allowedItem = item);
    }

    public class ItemControlPortBuild extends ConveyorBuild {
        public Item allowedItem;

        @Override
        public boolean acceptItem(Building source, Item item) {
            if (allowedItem != null && item != allowedItem) return false;
            return super.acceptItem(source, item);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.s(allowedItem == null ? -1 : allowedItem.id);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            int id = read.s();
            allowedItem = id == -1 ? null : Vars.content.item(id);
        }
    }
}