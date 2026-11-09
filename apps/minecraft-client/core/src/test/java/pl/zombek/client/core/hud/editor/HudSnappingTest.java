package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HudSnappingTest {

    @Test
    void testSnapToCenterLines() {
        int screenW = 960;
        int screenH = 540;
        int compW = 100;
        int compH = 60;

        // Screen center is at 480. If comp center (rawX + 50) is near 480, e.g. rawX = 428 (comp center = 478, diff 2)
        HudSnapping.SnapResult result = HudSnapping.calculateSnap(
                428, 238, compW, compH, screenW, screenH,
                16, false, true, false, 6
        );

        assertTrue(result.isSnappedXActive());
        assertTrue(result.isSnappedYActive());
        assertEquals(430, result.getSnappedX()); // 480 - 50 = 430
        assertEquals(240, result.getSnappedY()); // 270 - 30 = 240
        assertEquals(480, result.getGuideLineX());
        assertEquals(270, result.getGuideLineY());
        assertEquals(HudSnapping.GuideType.CENTER, result.getGuideTypeX());
        assertEquals(HudSnapping.GuideType.CENTER, result.getGuideTypeY());
    }

    @Test
    void testSnapToEdges() {
        int screenW = 960;
        int screenH = 540;
        int compW = 80;
        int compH = 40;

        // Candidate near left edge: rawX = 4 (within 6px of 0)
        HudSnapping.SnapResult resultLeft = HudSnapping.calculateSnap(
                4, 5, compW, compH, screenW, screenH,
                16, false, false, true, 6
        );

        assertTrue(resultLeft.isSnappedXActive());
        assertTrue(resultLeft.isSnappedYActive());
        assertEquals(0, resultLeft.getSnappedX());
        assertEquals(0, resultLeft.getSnappedY());
        assertEquals(HudSnapping.GuideType.EDGE, resultLeft.getGuideTypeX());

        // Candidate near right edge: right edge = rawX + 80 = 958 -> diff with 960 is 2
        HudSnapping.SnapResult resultRight = HudSnapping.calculateSnap(
                878, 498, compW, compH, screenW, screenH,
                16, false, false, true, 6
        );
        assertEquals(880, resultRight.getSnappedX()); // 960 - 80 = 880
        assertEquals(500, resultRight.getSnappedY()); // 540 - 40 = 500
        assertEquals(HudSnapping.GuideType.EDGE, resultRight.getGuideTypeX());
    }

    @Test
    void testSnapToGridLines() {
        int screenW = 960;
        int screenH = 540;
        int compW = 50;
        int compH = 30;
        int spacing = 16;

        // rawX = 66 -> nearest 16 multiple is 64 (diff 2 <= 6)
        HudSnapping.SnapResult result = HudSnapping.calculateSnap(
                66, 94, compW, compH, screenW, screenH,
                spacing, true, false, false, 6
        );

        assertTrue(result.isSnappedXActive());
        assertTrue(result.isSnappedYActive());
        assertEquals(64, result.getSnappedX());
        assertEquals(96, result.getSnappedY());
        assertEquals(64, result.getGuideLineX());
        assertEquals(96, result.getGuideLineY());
        assertEquals(HudSnapping.GuideType.GRID, result.getGuideTypeX());
    }

    @Test
    void testBeyondThresholdDoesNotSnap() {
        int screenW = 960;
        int screenH = 540;

        // rawX = 71, rawY = 71, compW = 32, compH = 32 -> nearest grid (64 or 80) diff is 7 (> 6)
        HudSnapping.SnapResult result = HudSnapping.calculateSnap(
                71, 71, 32, 32, screenW, screenH,
                16, true, true, true, 6
        );

        assertFalse(result.isSnappedXActive());
        assertFalse(result.isSnappedYActive());
        assertEquals(71, result.getSnappedX());
        assertEquals(71, result.getSnappedY());
        assertNull(result.getGuideLineX());
        assertNull(result.getGuideLineY());
    }
}
