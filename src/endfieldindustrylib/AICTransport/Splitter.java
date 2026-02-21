package endfieldindustrylib.AICTransport;

import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.blocks.distribution.Router;
import mindustry.world.meta.BlockGroup;
import mindustry.type.Item;
import mindustry.gen.Building;

public class Splitter extends Router {
    public Splitter(String name) {
        super(name);
        rotate = true;
        health = 40;
        size = 1;
        group = BlockGroup.transportation;
        category = Category.distribution;
        requirements(Category.distribution, ItemStack.with());
    }

    public class SplitterBuild extends RouterBuild {
        private int nextDir = 0;

        @Override
        public void updateTile() {
            if (items.total() > 0) {
                int direction = rotation;
                int inputDir = (direction + 2) % 4;
                int[] outputs = new int[3];
                int idx = 0;
                for (int i = 0; i < 4; i++) {
                    if (i != inputDir) outputs[idx++] = i;
                }

                for (int i = 0; i < 3; i++) {
                    int tryIdx = (nextDir + i) % 3;
                    int dir = outputs[tryIdx];
                    Building other = nearby(dir);
                    if (other != null && other.acceptItem(this, items.first())) {
                        other.handleItem(this, items.take());
                        nextDir = (tryIdx + 1) % 3;
                        break;
                    }
                }
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            int direction = rotation;
            int inputDir = (direction + 2) % 4;
            return source.tile != null &&
                   source.tile.relativeTo(tile) == inputDir &&
                   items.total() == 0;
        }
    }
}