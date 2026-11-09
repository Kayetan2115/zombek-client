package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HudCanvasViewportTest {

    private HudCanvasViewport viewport;

    @BeforeEach
    void setUp() {
        viewport = new HudCanvasViewport();
        viewport.updateBounds(100, 50, 800, 500, 960, 540);
    }

    @Test
    void testZoomInAndZoomOut() {
        assertEquals(1.0, viewport.getZoom(), 0.001);

        viewport.zoomIn();
        assertEquals(1.25, viewport.getZoom(), 0.001);

        viewport.zoomIn();
        assertEquals(1.50, viewport.getZoom(), 0.001);

        viewport.zoomOut();
        assertEquals(1.25, viewport.getZoom(), 0.001);

        viewport.resetZoom();
        assertEquals(1.0, viewport.getZoom(), 0.001);
    }

    @Test
    void testZoomClamping() {
        viewport.setZoom(0.01);
        assertEquals(HudCanvasViewport.MIN_ZOOM, viewport.getZoom(), 0.001);

        viewport.setZoom(10.0);
        assertEquals(HudCanvasViewport.MAX_ZOOM, viewport.getZoom(), 0.001);
    }

    @Test
    void testCoordinateConversionsRoundTrip() {
        viewport.setZoom(1.5);
        viewport.setPan(30, -20);

        int origVirtX = 250;
        int origVirtY = 180;

        int screenX = viewport.virtualToScreenX(origVirtX);
        int screenY = viewport.virtualToScreenY(origVirtY);

        int backVirtX = viewport.screenToVirtualX(screenX);
        int backVirtY = viewport.screenToVirtualY(screenY);

        assertEquals(origVirtX, backVirtX);
        assertEquals(origVirtY, backVirtY);
    }

    @Test
    void testPanning() {
        assertEquals(0, viewport.getPanX(), 0.001);
        assertEquals(0, viewport.getPanY(), 0.001);
        assertFalse(viewport.isPanning());

        viewport.startPan(200, 150);
        assertTrue(viewport.isPanning());

        viewport.updatePan(250, 190);
        assertEquals(50, viewport.getPanX(), 0.001);
        assertEquals(40, viewport.getPanY(), 0.001);

        viewport.endPan();
        assertFalse(viewport.isPanning());
        assertEquals(50, viewport.getPanX(), 0.001);
        assertEquals(40, viewport.getPanY(), 0.001);
    }

    @Test
    void testFitCanvas() {
        viewport.setZoom(2.0);
        viewport.setPan(100, 100);

        viewport.fitCanvas();
        assertEquals(0, viewport.getPanX(), 0.001);
        assertEquals(0, viewport.getPanY(), 0.001);
        assertTrue(viewport.getZoom() <= 1.0);
        assertTrue(viewport.getZoom() >= HudCanvasViewport.MIN_ZOOM);
    }

    @Test
    void testZoomPercentFormatting() {
        viewport.setZoom(1.0);
        assertEquals("100%", viewport.getZoomPercentString());

        viewport.setZoom(1.5);
        assertEquals("150%", viewport.getZoomPercentString());
    }
}
