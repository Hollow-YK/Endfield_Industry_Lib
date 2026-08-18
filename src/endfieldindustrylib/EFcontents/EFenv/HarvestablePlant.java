package endfieldindustrylib.EFcontents.EFenv;

import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;

/**
 * 可采集植株 — 拆除/摧毁后掉落对应植物物品。
 * <p>
 * 仅编辑器可放置，挖掘时间 0.5 秒，掉落 5 个对应物品。
 */
public class HarvestablePlant extends Block {

    public HarvestablePlant(String name, Item dropItem) {
        super(name);
        update = false;
        solid = false;
        destructible = true;
        breakable = true;
        health = 40;
        size = 1;
        forceTeam = Team.derelict;          // 恒为废墟阵营
        allowDerelictRepair = false; 
        drawTeamOverlay = false; 
        buildVisibility = BuildVisibility.editorOnly;
        // 建造成本 = 掉落物品，拆毁时自动返还
        requirements(Category.defense, ItemStack.with(dropItem, 5));
        buildTime = 20;
    }

    public class HarvestablePlantBuild extends Building {
        @Override
        public void onDeconstructed(Unit unit) {
            super.onDeconstructed(unit);
        }

        @Override
        public void onDestroyed() {
            if (team != null && team.core() != null && block != null) {
                ItemStack[] reqs = block.requirements;
                if (reqs != null) {
                    for (ItemStack stack : reqs) {
                        team.core().items.add(stack.item, stack.amount);
                    }
                }
            }
            super.onDestroyed();
        }
    }
}
