package endfieldindustrylib.ui.fragments;

import arc.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import endfieldindustrylib.EFworld.blocks.AICBasicFacility.GenericAICBasicFacility.*;
import mindustry.game.EventType.*;

import static mindustry.Vars.*;

/**
 * 为通用多槽位工厂（GenericAICBasicFacility）设计的浮动配置面板。
 * 支持实时更新物品槽位显示，每帧刷新。
 */
public class AICFacilityConfigFragment {
    private Table table = new Table();
    private GenericAICBasicFacilityBuild selected;

    public void build(Group parent) {
        table.visible = false;
        parent.addChild(table);

        Events.on(ResetEvent.class, e -> forceHide());
    }

    public void forceHide() {
        table.visible = false;
        if (selected != null) {
            selected.onConfigureClosed();
            selected = null;
        }
    }

    public boolean isShown() {
        return table.visible && selected != null;
    }

    public GenericAICBasicFacilityBuild getSelected() {
        return selected;
    }

    public void showConfig(GenericAICBasicFacilityBuild facility) {
        if (facility == null) return;

        if (selected != null) {
            selected.onConfigureClosed();
        }

        selected = facility;

        table.visible = true;
        table.clear();
        table.background(null);

        facility.buildConfiguration(table);

        table.pack();
        table.setTransform(true);

        table.actions(
            Actions.scaleTo(0f, 1f),
            Actions.visible(true),
            Actions.scaleTo(1f, 1f, 0.07f, Interp.pow3Out)
        );

        table.update(() -> {
            if (selected == null || !selected.isValid() || selected.shouldHideConfigure(player)) {
                hideConfig();
                return;
            }

            table.setOrigin(Align.center);
            selected.updateTableAlign(table);
        });
    }

    public void hideConfig() {
        if (selected != null) {
            selected.onConfigureClosed();
            selected = null;
        }

        table.actions(
            Actions.scaleTo(0f, 1f, 0.06f, Interp.pow3Out),
            Actions.visible(false)
        );
    }

    public boolean hasConfigMouse() {
        Element e = Core.scene.getHoverElement();
        return e != null && (e == table || e.isDescendantOf(table));
    }
}