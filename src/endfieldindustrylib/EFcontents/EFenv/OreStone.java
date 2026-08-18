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
 * 矿化岩石 — 废墟态富矿岩体。
 * <p>
 * 仅编辑器可放置，锁定为废墟，不可修复，免疫攻击伤害。
 * 拆毁时通过建造成本自动返还对应矿石。
 */
public class OreStone extends Block {

    public OreStone(String name, Item dropItem) {
        super(name);
        update = false;
        solid = true;
        destructible = true;
        breakable = true;
        health = 400;
        size = 1;
        buildVisibility = BuildVisibility.editorOnly;
        forceTeam = Team.derelict;          // 恒为废墟阵营
        allowDerelictRepair = false;        // 不可修复
        drawTeamOverlay = false;            // 不绘制阵营色带
        // 建造成本 = 25 个矿石，拆毁时按返还比例退回
        requirements(Category.defense, ItemStack.with(dropItem, 50));
        // 拆除时间：100% 速度 25s
        buildTime = 150;
    }

    public class OreStoneBuild extends Building {

        @Override
        public void onDestroyed() {
            // 不受攻击摧毁，留空即可
        }

        @Override
        public void onDeconstructed(Unit unit) {
            // 拆毁返还由父类 ConstructBlock 自动处理建造成本
            super.onDeconstructed(unit);
        }

        @Override
        public float handleDamage(float amount) {
            return 0f; // 免疫一切伤害
        }
    }
}
