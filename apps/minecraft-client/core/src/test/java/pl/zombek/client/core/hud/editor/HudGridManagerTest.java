package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HudGridManagerTest {

    private HudGridManager gridManager;

    @BeforeEach
    void setUp() {
        gridManager = new HudGridManager();
    }

    @Test
    void testGridStylesAndPresets() {
        assertEquals(HudGridManager.GridStyle.LINES, gridManager.getStyle());

        for (HudGridManager.GridStyle style : HudGridManager.GridStyle.values()) {
            gridManager.setStyle(style);
            assertEquals(style, gridManager.getStyle());
            assertNotNull(style.getDisplayName());
        }

        assertArrayEquals(new int[]{2, 4, 8, 16, 24, 32}, HudGridManager.PRESET_SPACINGS);
    }

    @Test
    void testSpacingAndOpacity() {
        gridManager.setSpacing(24);
        assertEquals(24, gridManager.getSpacing());
        assertEquals(24, gridManager.getHorizontalSpacing());
        assertEquals(24, gridManager.getVerticalSpacing());

        // Custom individual spacing
        gridManager.setHorizontalSpacing(32);
        gridManager.setVerticalSpacing(16);
        assertEquals(32, gridManager.getHorizontalSpacing());
        assertEquals(16, gridManager.getVerticalSpacing());

        // Opacity adjustment
        gridManager.setOpacity(0.5f);
        assertEquals(0.5f, gridManager.getOpacity(), 0.01);
        int alpha = (gridManager.getGridColor() >> 24) & 0xFF;
        assertEquals(127, alpha, 2);
    }

    @Test
    void testSnapToggles() {
        assertTrue(gridManager.isSnapToGrid());
        assertTrue(gridManager.isSnapToCenter());
        assertTrue(gridManager.isSnapToEdges());

        gridManager.setSnapToGrid(false);
        assertFalse(gridManager.isSnapToGrid());

        gridManager.setSnapThreshold(10);
        assertEquals(10, gridManager.getSnapThreshold());
    }
}
