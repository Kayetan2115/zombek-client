package pl.zombek.client.core.modules.visual.tabeditor;

import java.util.*;

/**
 * Manages visibility, order, and column widths for Tab Player List.
 */
public final class TabColumnManager {

    private final Set<TabColumn> enabledColumns = EnumSet.of(
            TabColumn.AVATAR,
            TabColumn.PREFIX,
            TabColumn.NAME,
            TabColumn.PING
    );

    private final Map<TabColumn, Integer> columnWidths = new EnumMap<>(TabColumn.class);

    public TabColumnManager() {
        for (TabColumn col : TabColumn.values()) {
            columnWidths.put(col, col.getDefaultWidth());
        }
    }

    public boolean isColumnEnabled(TabColumn column) {
        return enabledColumns.contains(column);
    }

    public void setColumnEnabled(TabColumn column, boolean enabled) {
        if (column != null) {
            if (enabled) {
                enabledColumns.add(column);
            } else {
                enabledColumns.remove(column);
            }
        }
    }

    public int getColumnWidth(TabColumn column) {
        return columnWidths.getOrDefault(column, column.getDefaultWidth());
    }

    public void setColumnWidth(TabColumn column, int width) {
        if (column != null) {
            columnWidths.put(column, Math.max(10, width));
        }
    }

    public List<TabColumn> getOrderedActiveColumns() {
        List<TabColumn> list = new ArrayList<>();
        for (TabColumn col : TabColumn.values()) {
            if (enabledColumns.contains(col)) {
                list.add(col);
            }
        }
        return list;
    }

    public int calculateTotalRowWidth(int columnSpacing) {
        int width = 0;
        List<TabColumn> active = getOrderedActiveColumns();
        for (TabColumn col : active) {
            width += getColumnWidth(col);
        }
        if (!active.isEmpty()) {
            width += (active.size() - 1) * columnSpacing;
        }
        return width;
    }
}
