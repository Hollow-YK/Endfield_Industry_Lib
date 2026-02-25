package endfieldindustrylib.EFworld.blocks.AICTransport;

import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.gen.Building;
import mindustry.world.Tile;
import mindustry.world.Edges;
import arc.util.io.*;
import mindustry.Vars;

public class Converger extends Block {
    public Converger(String name) {
        super(name);
        rotate = true;
        solid = true;
        destructible = true;
        hasItems = true;
        itemCapacity = 1;
        health = 40;
        size = 1;
        group = BlockGroup.transportation;
        category = Category.distribution;
        requirements(Category.distribution, ItemStack.with());
        
        update = true;
        buildType = ConvergerBuild::new;
    }

    public class ConvergerBuild extends Building {
        private int nextInputDir = 0;
        private Item storedItem = null;
        private int timer = 0;
        private boolean canAccept = true;  // 是否允许接受物品
        private static final int TICK_INTERVAL = 120;  // 2 秒 = 120 帧 (60fps)

        @Override
        public boolean acceptItem(Building source, Item item) {
            // 已有物品，拒绝所有输入
            if (storedItem != null) return false;
            
            // 检查是否允许接受物品
            if (!canAccept) return false;
            
            // 确定来源方向（转换为绝对方向）
            if (source.tile == null) return false;
            Tile facing = Edges.getFacingEdge(source.tile, tile);
            if (facing == null) return false;
            
            int sourceDir = facing.relativeTo(tile.x, tile.y);
            
            // 如果输入方向是输出方向，直接拒绝
            /*不管了，反正输出方向不会有输入，就算有也输出不出去
            if (absoluteSourceDir == rotation) {
                return false;
            }*/
            
            // 仅当输入方向等于当前轮询方向时才接受
            return sourceDir == nextInputDir;
        }

        @Override
        public void handleItem(Building source, Item item) {
            storedItem = item;
            items.add(item, 1);
            
            // 设置下一个轮询方向（跳过输出方向）
            do {
                nextInputDir = (nextInputDir + 1) % 4;
            // 修改点：将 rotation 加2再取余4后用于判断跳过输出方向
            } while (nextInputDir == ((rotation + 2) % 4));
            
            canAccept = false;  // 接受后禁止再接受
            timer = 0;
            noSleep();
        }

        @Override
        public void updateTile() {
            // 有物品时：等待TICK_INTERVAL帧后尝试输出
            if (storedItem != null) {
                if (timer >= TICK_INTERVAL) {
                    int outputDir = rotation ;
                    Building out = nearby(outputDir);
                    
                    if (out != null && out.acceptItem(this, storedItem)) {
                        out.handleItem(this, storedItem);
                        storedItem = null;
                        items.clear();
                        canAccept = true;  // 输出后允许接受新物品
                        timer = 0;
                    }
                } else {
                    timer++;
                }
            } 
            // 没有物品时：检查当前轮询方向是否有输入
            else {
                // 检查当前轮询方向是否有输入（通过建筑是否在尝试输入）
                Building currentDirBuilding = nearby(nextInputDir);
                boolean hasInput = currentDirBuilding != null && 
                                  currentDirBuilding.acceptItem(this, null); // 伪检查
                
                if (nextInputDir == ((rotation + 2) % 4) || !hasInput) {
                    // 确保跳过输出方向
                    do {
                        nextInputDir = (nextInputDir + 1) % 4;
                    } while (nextInputDir == ((rotation + 2) % 4));
                    
                    canAccept = true;  // 允许接受新物品
                    timer = 0;         // 重置计时器（不等待）
                } 
                // 如果当前方向有输入，等待TICK_INTERVAL帧
                else {
                    timer++;
                    if (timer >= TICK_INTERVAL) {
                        timer = 0;
                        canAccept = false; // 禁止接受，等待输入
                    }
                }
            }
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(nextInputDir);
            write.s(storedItem == null ? -1 : storedItem.id);
            write.i(timer);
            write.bool(canAccept);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            nextInputDir = read.i();
            int id = read.s();
            storedItem = id == -1 ? null : Vars.content.item(id);
            timer = read.i();
            canAccept = read.bool();
            if (storedItem != null) {
                items.add(storedItem, 1);
            }
        }
    }
}