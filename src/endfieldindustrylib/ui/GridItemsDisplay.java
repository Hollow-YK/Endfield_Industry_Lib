package endfieldindustrylib.ui;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.core.UI;
import mindustry.type.Item;
import mindustry.ui.Styles;

/**
 * 网格物品显示组件，用于显示一组槽位（每个槽位有固定物品类型和数量）。
 * 适用于输入/输出槽位的网格化展示。
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class GridItemsDisplay extends Table {
    private Slot[] slots;
    private int columns = 4;

    public GridItemsDisplay() {
        super();
        rebuild(new Slot[0]);
    }

    public GridItemsDisplay(int columns) {
        super();
        this.columns = columns;
        rebuild(new Slot[0]);
    }

    public void rebuild(Slot[] slots) {
        this.slots = slots;
        clear();
        top().left();
        margin(0);

        if (slots == null || slots.length == 0) return;

        table(Styles.black, t -> {
            t.margin(10).left();

            int rowCount = (slots.length + columns - 1) / columns;
            for (int row = 0; row < rowCount; row++) {
                for (int col = 0; col < columns; col++) {
                    int index = row * columns + col;
                    if (index < slots.length) {
                        Slot slot = slots[index];
                        t.add(new SlotDisplay(slot)).size(50).pad(2);
                    } else {
                        t.add().size(50).pad(2);
                    }
                }
                t.row();
            }
        }).growX();
    }

    public static class Slot {
        public Item item; // 可为 null，表示空槽位
        public int amount;

        public Slot(Item item, int amount) {
            this.item = item;
            this.amount = amount;
        }
    }

    private static class SlotDisplay extends Stack {
        public SlotDisplay(Slot slot) {
            Table table = new Table();
            if (slot.item != null) {
                table.add(new Image(slot.item.uiIcon)).size(32);
                if (slot.amount > 0) {
                    table.add(UI.formatAmount(slot.amount)).color(Color.lightGray).padLeft(2);
                }
            } else {
                // 空槽位显示灰色背景
                table.add(new Image(Core.atlas.white())).color(Color.gray).size(32);
            }
            add(table);
        }
    }
}