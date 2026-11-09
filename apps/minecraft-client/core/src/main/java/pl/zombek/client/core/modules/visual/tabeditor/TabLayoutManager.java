package pl.zombek.client.core.modules.visual.tabeditor;

/**
 * Layout and geometry calculator for the Tab Player List grid.
 * Adaptively fits player entries to the current screen dimensions and GUI scale.
 */
public final class TabLayoutManager {

    public static final class GridLayoutResult {
        public final int columns;
        public final int rows;
        public final int totalGridWidth;
        public final int totalGridHeight;
        public final int effectiveColumnWidth;
        public final int effectiveRowHeight;

        public GridLayoutResult(int columns, int rows, int totalGridWidth, int totalGridHeight, int effectiveColumnWidth, int effectiveRowHeight) {
            this.columns = columns;
            this.rows = rows;
            this.totalGridWidth = totalGridWidth;
            this.totalGridHeight = totalGridHeight;
            this.effectiveColumnWidth = effectiveColumnWidth;
            this.effectiveRowHeight = effectiveRowHeight;
        }

        public int getCellX(int colIndex, int startX, int hSpacing) {
            return startX + colIndex * (effectiveColumnWidth + hSpacing);
        }

        public int getCellY(int rowIndex, int startY, int vSpacing) {
            return startY + rowIndex * (effectiveRowHeight + vSpacing);
        }
    }

    private TabLayoutManager() {}

    public static GridLayoutResult computeGrid(
            int playerCount,
            int maxColumns,
            int maxRows,
            int baseColumnWidth,
            int baseRowHeight,
            int hSpacing,
            int vSpacing,
            int availableWidth,
            boolean adaptiveWidth) {

        int count = Math.max(1, playerCount);
        int maxR = Math.max(1, maxRows);
        int maxC = Math.max(1, maxColumns);

        int cols = (int) Math.ceil((double) count / maxR);
        cols = Math.max(1, Math.min(cols, maxC));

        int rows = (int) Math.ceil((double) count / cols);

        int colWidth = baseColumnWidth;
        if (adaptiveWidth && availableWidth > 0) {
            int maxPossibleWidth = (availableWidth - 40 - (cols - 1) * hSpacing) / cols;
            if (maxPossibleWidth > colWidth && maxPossibleWidth <= colWidth * 2) {
                colWidth = maxPossibleWidth;
            }
        }

        int totalWidth = cols * colWidth + (cols - 1) * hSpacing;
        int totalHeight = rows * baseRowHeight + (rows - 1) * vSpacing;

        return new GridLayoutResult(cols, rows, totalWidth, totalHeight, colWidth, baseRowHeight);
    }
}
