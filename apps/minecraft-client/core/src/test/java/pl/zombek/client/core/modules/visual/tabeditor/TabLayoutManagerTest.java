package pl.zombek.client.core.modules.visual.tabeditor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TabLayoutManagerTest {

    @Test
    void testComputeGridSinglePlayer() {
        TabLayoutManager.GridLayoutResult grid = TabLayoutManager.computeGrid(
                1, 4, 20, 120, 12, 4, 2, 854, true
        );

        assertNotNull(grid);
        assertEquals(1, grid.columns);
        assertEquals(1, grid.rows);
        assertEquals(120, grid.effectiveColumnWidth);
        assertEquals(12, grid.effectiveRowHeight);
        assertEquals(120, grid.totalGridWidth);
        assertEquals(12, grid.totalGridHeight);
    }

    @Test
    void testComputeGridMultipleColumns() {
        // 45 players with maxRows = 20 -> 3 columns, balanced to 15 rows each
        TabLayoutManager.GridLayoutResult grid = TabLayoutManager.computeGrid(
                45, 4, 20, 100, 12, 4, 2, 854, false
        );

        assertEquals(3, grid.columns);
        assertEquals(15, grid.rows);
        int expectedW = 3 * 100 + 2 * 4;
        assertEquals(expectedW, grid.totalGridWidth);
    }

    @Test
    void testCellPositions() {
        TabLayoutManager.GridLayoutResult grid = TabLayoutManager.computeGrid(
                10, 2, 5, 80, 14, 6, 3, 600, false
        );

        int startX = 50;
        int startY = 100;

        assertEquals(50, grid.getCellX(0, startX, 6));
        assertEquals(50 + 80 + 6, grid.getCellX(1, startX, 6));

        assertEquals(100, grid.getCellY(0, startY, 3));
        assertEquals(100 + 14 + 3, grid.getCellY(1, startY, 3));
    }
}
