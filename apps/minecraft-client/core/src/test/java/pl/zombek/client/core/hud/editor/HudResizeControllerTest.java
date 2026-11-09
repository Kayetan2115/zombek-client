package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.hud.HudAnchor;

import static org.junit.jupiter.api.Assertions.*;

class HudResizeControllerTest {

    private HudResizeController resizeController;
    private HudComponentDescriptor resizableDesc;

    @BeforeEach
    void setUp() {
        resizeController = new HudResizeController();
        resizableDesc = HudComponentDescriptor.builder("scoreboard", "Scoreboard")
                .defaultPosition(50, 60)
                .defaultSize(120, 80)
                .minSize(40, 30)
                .maxSize(400, 300)
                .defaultAnchor(HudAnchor.TOP_LEFT)
                .capabilities(HudComponentCapability.POSITION, HudComponentCapability.SIZE, HudComponentCapability.SCALE)
                .build();
    }

    @Test
    void test8ResizeHandlesHitTest() {
        int boxX = 100;
        int boxY = 100;
        int boxW = 100;
        int boxH = 60;

        assertEquals(HudResizeHandle.TOP_LEFT, resizeController.getHandleAt(100, 100, boxX, boxY, boxW, boxH));
        assertEquals(HudResizeHandle.TOP_CENTER, resizeController.getHandleAt(150, 100, boxX, boxY, boxW, boxH));
        assertEquals(HudResizeHandle.TOP_RIGHT, resizeController.getHandleAt(200, 100, boxX, boxY, boxW, boxH));
        assertEquals(HudResizeHandle.MIDDLE_LEFT, resizeController.getHandleAt(100, 130, boxX, boxY, boxW, boxH));
        assertEquals(HudResizeHandle.MIDDLE_RIGHT, resizeController.getHandleAt(200, 130, boxX, boxY, boxW, boxH));
        assertEquals(HudResizeHandle.BOTTOM_LEFT, resizeController.getHandleAt(100, 160, boxX, boxY, boxW, boxH));
        assertEquals(HudResizeHandle.BOTTOM_CENTER, resizeController.getHandleAt(150, 160, boxX, boxY, boxW, boxH));
        assertEquals(HudResizeHandle.BOTTOM_RIGHT, resizeController.getHandleAt(200, 160, boxX, boxY, boxW, boxH));

        // Outside handle bounds
        assertNull(resizeController.getHandleAt(120, 120, boxX, boxY, boxW, boxH));
    }

    @Test
    void testResizeBottomRightHandle() {
        HudComponentState state = resizableDesc.createDefaultState();
        int virtW = 960;
        int virtH = 540;

        boolean started = resizeController.startResize("scoreboard", HudResizeHandle.BOTTOM_RIGHT,
                170, 140, state, resizableDesc, virtW, virtH);
        assertTrue(started);
        assertTrue(resizeController.isResizing());

        // Drag handle by +30px horizontally and +20px vertically
        resizeController.updateResize(200, 160, state, resizableDesc, virtW, virtH, false);
        assertEquals(150, state.getWidth()); // 120 + 30
        assertEquals(100, state.getHeight()); // 80 + 20
        assertEquals(50, state.getPosX()); // Origin stayed stationary
        assertEquals(60, state.getPosY());

        resizeController.endResize();
        assertFalse(resizeController.isResizing());
    }

    @Test
    void testResizeTopLeftHandleShiftsPosition() {
        HudComponentState state = resizableDesc.createDefaultState();
        int virtW = 960;
        int virtH = 540;

        resizeController.startResize("scoreboard", HudResizeHandle.TOP_LEFT,
                50, 60, state, resizableDesc, virtW, virtH);

        // Drag handle to the left by -20px and up by -15px (expanding box)
        resizeController.updateResize(30, 45, state, resizableDesc, virtW, virtH, false);
        assertEquals(140, state.getWidth()); // 120 + 20
        assertEquals(95, state.getHeight()); // 80 + 15
        assertEquals(30, state.getPosX()); // Shifted left so right edge (170) remains stationary
        assertEquals(45, state.getPosY()); // Shifted up so bottom edge (140) remains stationary

        resizeController.endResize();
    }

    @Test
    void testMinMaxLimitsEnforced() {
        HudComponentState state = resizableDesc.createDefaultState();
        int virtW = 960;
        int virtH = 540;

        resizeController.startResize("scoreboard", HudResizeHandle.BOTTOM_RIGHT,
                170, 140, state, resizableDesc, virtW, virtH);

        // Shrink below minimum (min is 40x30)
        resizeController.updateResize(-500, -500, state, resizableDesc, virtW, virtH, false);
        assertEquals(40, state.getWidth());
        assertEquals(30, state.getHeight());

        // Expand beyond maximum (max is 400x300)
        resizeController.updateResize(2000, 2000, state, resizableDesc, virtW, virtH, false);
        assertEquals(400, state.getWidth());
        assertEquals(300, state.getHeight());

        resizeController.endResize();
    }

    @Test
    void testLockAspectRatio() {
        HudComponentState state = resizableDesc.createDefaultState();
        state.setLockAspectRatio(true);
        int virtW = 960;
        int virtH = 540;

        // Initial aspect = 120 / 80 = 1.5
        resizeController.startResize("scoreboard", HudResizeHandle.BOTTOM_RIGHT,
                170, 140, state, resizableDesc, virtW, virtH);

        // Drag width to 150 (+30) -> height should proportionally become 150 / 1.5 = 100
        resizeController.updateResize(200, 140, state, resizableDesc, virtW, virtH, true);
        assertEquals(150, state.getWidth());
        assertEquals(100, state.getHeight());

        resizeController.endResize();
    }

    @Test
    void testCancelResizeRestoresDimensions() {
        HudComponentState state = resizableDesc.createDefaultState();
        int virtW = 960;
        int virtH = 540;

        resizeController.startResize("scoreboard", HudResizeHandle.BOTTOM_RIGHT,
                170, 140, state, resizableDesc, virtW, virtH);
        resizeController.updateResize(250, 200, state, resizableDesc, virtW, virtH, false);
        assertEquals(200, state.getWidth());
        assertEquals(140, state.getHeight());

        resizeController.cancelResize(state, virtW, virtH);
        assertFalse(resizeController.isResizing());
        assertEquals(120, state.getWidth());
        assertEquals(80, state.getHeight());
        assertEquals(50, state.getPosX());
        assertEquals(60, state.getPosY());
    }
}
