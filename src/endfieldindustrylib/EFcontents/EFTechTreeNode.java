package endfieldindustrylib.EFcontents;

import arc.Core;
import arc.graphics.Color;
import arc.struct.Seq;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Objectives.Objective;
import mindustry.type.ItemStack;
import mindustry.type.StatusEffect;

/**
 * EF 科技树节点。
 *
 * 作为 extends StatusEffect，可以：
 * - 直接作为 TechNode.content 嵌入 Mindustry 原版研究对话框
 * - onUnlock() 在玩家研究完成后自动触发 rewards 解锁
 *
 * 此对象为纯配置，不持有 parent/children——树结构由 EFTechTree 中的嵌套 lambda 表达。
 */
public class EFTechTreeNode extends StatusEffect {
    /** 节点颜色（用户界面备用） */
    public Color nodeColor = Color.white;
    /** 图标来源（可选），非 null 时用其 uiIcon */
    public UnlockableContent iconContent;
    /** 自定义精灵路径，如 "eftechtree/logistics-i" */
    public String spritePath;

    /** 使本节点变为"可研究"的前置条件（同原版 Objective 系统） */
    public final Seq<Objective> unlockObjectives = new Seq<>();
    /** 研究消耗。null 或空数组 = 免消耗 */
    public ItemStack[] researchCost;
    /** true: 解锁后立即自动研究（免消耗） */
    public boolean autoResearch;

    /** 研究后 quietUnlock 的内容（方块/物品/单位/地区等） */
    public final Seq<UnlockableContent> rewards = new Seq<>();

    // ================================================================
    //  构造
    // ================================================================

    public EFTechTreeNode(String name) {
        super(name);
        hideDatabase = true;
        alwaysUnlocked = false;
        generateIcons = false;
        outline = false;
    }

    // ================================================================
    //  Builder 方法（链式调用，仅设属性）
    // ================================================================

    public EFTechTreeNode color(Color c) { this.nodeColor = c; return this; }
    public EFTechTreeNode icon(UnlockableContent c) { this.iconContent = c; return this; }
    public EFTechTreeNode sprite(String path) { this.spritePath = path; return this; }

    public EFTechTreeNode cost(ItemStack... stacks) {
        this.researchCost = stacks;
        return this;
    }

    /** 设为免消耗 */
    public EFTechTreeNode free() {
        this.autoResearch = true;
        this.researchCost = new ItemStack[]{new ItemStack(endfieldindustrylib.EFcontents.EFitems.researchGate, 0)};
        return this;
    }

    public EFTechTreeNode objectives(Objective... objs) {
        this.unlockObjectives.addAll(objs);
        return this;
    }

    public EFTechTreeNode rewards(UnlockableContent... contents) {
        this.rewards.addAll(contents);
        return this;
    }

    // ================================================================
    //  重写 UnlockableContent 方法
    // ================================================================

    @Override
    public String toString() {
        return Core.bundle.get("eftechtree." + name + ".name", name);
    }

    @Override
    public void loadIcon() {
        if (spritePath != null) {
            fullIcon = Core.atlas.find(spritePath);
            uiIcon = fullIcon;
        } else if (iconContent != null) {
            fullIcon = iconContent.fullIcon;
            uiIcon = iconContent.uiIcon;
        } else {
            // 自动从 sprites/eftechtree/{rawName}.png 查找
            // rawName = name 去掉模组前缀
            String modPrefix = minfo.mod != null ? minfo.mod.name + "-" : "";
            String rawName = name;
            if (rawName.startsWith(modPrefix)) {
                rawName = rawName.substring(modPrefix.length());
            }
            var region = Core.atlas.find("eftechtree/" + rawName);
            if (region.found()) {
                fullIcon = region;
                uiIcon = region;
            } else {
                super.loadIcon();
            }
        }
    }

    @Override
    public void load() {
        super.load();
        // 覆写 localizedName：优先使用 eftechtree.{name}.name
        String efName = Core.bundle.get("eftechtree." + name + ".name", "\u0000");
        if (!"\u0000".equals(efName)) {
            this.localizedName = efName;
        }
    }

    @Override
    public void onUnlock() {
        for (var content : rewards) {
            content.quietUnlock();
        }
        if (autoResearch) {
            Core.settings.put("ef-researched-" + name, true);
        }
    }

    @Override
    public boolean isHidden() {
        return true;
    }
}
