package endfieldindustrylib.ui;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.Align;
import mindustry.core.UI;
import mindustry.type.Item;
import mindustry.ui.Styles;

import java.util.function.Supplier;

/**
 * 网格物品显示组件，用于显示一组槽位（每个槽位有固定物品类型和数量）。
 * 支持静态数据或动态数据源自动更新。
 * 适用于输入/输出槽位的网格化展示。支持局部更新单个槽位内容。
 * 列数可固定，也可根据槽位数量动态调整（通过 maxColumns 限制，尽量接近正方形）。
 */
public class GridItemsDisplay extends Table {
    private int fixedColumns = -1;          // 固定列数模式下的列数（>0 时生效）
    private int maxColumns = 3;              // 动态模式下的最大列数限制
    private boolean useDynamicColumns;       // true = 使用动态列数计算，false = 使用 fixedColumns
    private Supplier<Slot[]> dataSupplier;   // 数据提供者，每帧调用以获取最新槽位数据
    private boolean dynamic;                  // true = 数据源动态更新（有 dataSupplier）
    private Slot[] lastData;

    // 私有核心构造，统一初始化（只能通过工厂方法调用）
    private GridItemsDisplay(int colOrMax, Supplier<Slot[]> dataSupplier, boolean useDynamicColumns) {
        this.useDynamicColumns = useDynamicColumns;
        if (useDynamicColumns) {
            this.maxColumns = colOrMax;
            this.fixedColumns = -1;
        } else {
            this.fixedColumns = colOrMax;
            this.maxColumns = -1;
        }
        this.dataSupplier = dataSupplier;
        this.dynamic = dataSupplier != null;
        if (dynamic) {
            update(this::updateDynamic);
        }
    }

    /** 固定列数模式，无数据提供者（静态数据） */
    public static GridItemsDisplay withFixedColumns(int columns) {
        return new GridItemsDisplay(columns, null, false);
    }

    /** 固定列数模式，带数据提供者（动态数据） */
    public static GridItemsDisplay withFixedColumns(int columns, Supplier<Slot[]> dataSupplier) {
        return new GridItemsDisplay(columns, dataSupplier, false);
    }

    /** 动态列数模式，指定最大列数，无数据提供者（静态数据） */
    public static GridItemsDisplay withMaxColumns(int maxColumns) {
        return new GridItemsDisplay(maxColumns, null, true);
    }

    /** 动态列数模式，指定最大列数，带数据提供者（动态数据） */
    public static GridItemsDisplay withMaxColumns(int maxColumns, Supplier<Slot[]> dataSupplier) {
        return new GridItemsDisplay(maxColumns, dataSupplier, true);
    }

    // ========== 静态数据设置 ==========
    /** 静态模式：设置数据并重建网格（动态模式下调用无效） */
    public void setSlots(Slot[] slots) {
        if (dynamic) {
            // 动态模式下不应手动设置
            return;
        }
        rebuild(slots);
    }

    // ========== 动态更新逻辑 ==========
    private void updateDynamic() {
        if (!dynamic || dataSupplier == null) return;
        Slot[] newData = dataSupplier.get();
        if (newData == null) return;

        // 检查数据是否变化，避免不必要的重建
        if (lastData == null || hasDataChanged(newData)) {
            rebuild(newData);
            lastData = newData.clone();
        }
    }

    private boolean hasDataChanged(Slot[] newData) {
        if (lastData.length != newData.length) return true;
        for (int i = 0; i < newData.length; i++) {
            Slot s1 = lastData[i];
            Slot s2 = newData[i];
            if (s1.item != s2.item || s1.amount != s2.amount) {
                return true;
            }
        }
        return false;
    }

    // ========== 网格重建 ==========
    /** 重建网格（清空并重新添加所有槽位） */
    private void rebuild(Slot[] slots) {
        clear();
        top().left();
        margin(0);

        if (slots == null || slots.length == 0) return;

        // 计算实际列数
        int actualColumns;
        if (useDynamicColumns) {
            int slotCount = slots.length;
            if (slotCount == 0) {
                actualColumns = 1; // 无槽位时默认1列
            } else {
                // 使行列数尽可能接近正方形：列数 = min(maxColumns, ceil(sqrt(n)))
                int colsBySqrt = (int) Math.ceil(Math.sqrt(slotCount));
                actualColumns = Math.min(maxColumns, colsBySqrt);
            }
        } else {
            actualColumns = fixedColumns;
        }

        table(t -> {
            t.margin(10).left();

            int rowCount = (slots.length + actualColumns - 1) / actualColumns;
            for (int row = 0; row < rowCount; row++) {
                for (int col = 0; col < actualColumns; col++) {
                    int index = row * actualColumns + col;
                    if (index < slots.length) {
                        SlotDisplay display = new SlotDisplay(slots[index]);
                        t.add(display).size(50).pad(2);
                    } else {
                        t.add().size(50).pad(2);
                    }
                }
                t.row();
            }
        }).growX();
    }

    // ========== 内部数据类 ==========
    public static class Slot {
        public Item item;
        public int amount;
        public Slot(Item item, int amount) {
            this.item = item;
            this.amount = amount;
        }
    }

    // ========== 单个槽位显示组件 ==========
    private static class SlotDisplay extends Table {
        public SlotDisplay(Slot slot) {
            background(Styles.black5);
            update(slot);
        }

        public void update(Slot slot) {
            clearChildren();
            defaults().center();

            if (slot.item != null) {
                // 使用 Stack 堆叠图标和标签
                Stack stack = new Stack();
                stack.setSize(32, 32);

                // 图标
                Image image = new Image(slot.item.uiIcon);
                image.setSize(32, 32);
                stack.add(image);

                // 数量标签（如果有）
                if (slot.amount > 0) {
                    Label amountLabel = new Label(UI.formatAmount(slot.amount), Styles.outlineLabel);
                    amountLabel.setColor(Color.lightGray);
                    amountLabel.setAlignment(Align.bottomLeft);
                    amountLabel.setFontScale(0.8f);
                    stack.add(amountLabel);
                }

                // 将 Stack 添加到表格，单元格大小固定为 32x32
                add(stack).size(32);
            } else {
                Image image = new Image(Core.atlas.white());
                image.setColor(Color.gray);
                add(image).size(32);
            }
        }
    }
}