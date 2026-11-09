package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.hud.HudAnchor;

import static org.junit.jupiter.api.Assertions.*;

class HudDragControllerTest {

    private HudDragController dragController;
    private HudComponentDescriptor normalDesc;
    private HudComponentDescriptor worldDesc;
    private HudGridManager gridManager;

    @BeforeEach
    void setUp() {
        dragController = new HudDragController();
        gridManager = new HudGridManager();

        normalDesc = HudComponentDescriptor.builder("fps", "FPS Display")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(20, 30)
                .defaultSize(80, 25)
                .defaultAnchor(HudAnchor.TOP_LEFT)
                .capabilities(HudComponentCapability.POSITION, HudComponentCapability.SCALE, HudComponentCapability.SIZE)
                .build();

        worldDesc = HudComponentDescriptor.builder("nametags", "NameTags")
                .type(HudComponentType.WORLD_SPACE)
                .defaultPosition(0, 0)
                .defaultSize(100, 20)
                .defaultAnchor(HudAnchor.CENTER)
                .capabilities(HudComponentCapability.VISIBILITY)
                .build();
    }

    @Test
    void testStartDragAndOffsetPreservation() {
        HudComponentState state = normalDesc.createDefaultState();
        int virtW = 960;
        int virtH = 540;

        // Grab at (35, 42) -> component is at (20, 30)
        boolean started = dragController.startDrag("fps", 35, 42, state, normalDesc, virtW, virtH);
        assertTrue(started);
        assertTrue(dragController.isDragging());
        assertEquals("fps", dragController.getDraggedComponentId());
        assertEquals(15, dragController.getDragOffsetVirtX()); // 35 - 20 = 15
        assertEquals(12, dragController.getDragOffsetVirtY()); // 42 - 30 = 12
        assertEquals(20, dragController.getInitialVirtX());
        assertEquals(30, dragController.getInitialVirtY());
    }

    @Test
    void testDragUpdateAndBoundaryClamping() {
        HudComponentState state = normalDesc.createDefaultState();
        int virtW = 960;
        int virtH = 540;

        dragController.startDrag("fps", 20, 30, state, normalDesc, virtW, virtH);

        // Move to (100, 150)
        dragController.updateDrag(100, 150, state, normalDesc, virtW, virtH, gridManager, false);
        assertEquals(100, state.getPosX());
        assertEquals(150, state.getPosY());

        // Drag beyond right/bottom boundaries
        dragController.updateDrag(2000, 2000, state, normalDesc, virtW, virtH, gridManager, false);
        int maxAllowedX = virtW - state.getEffectiveWidth();
        int maxAllowedY = virtH - state.getEffectiveHeight();
        assertEquals(maxAllowedX, state.getPosX());
        assertEquals(maxAllowedY, state.getPosY());

        // Drag beyond left/top boundaries (negative)
        dragController.updateDrag(-500, -500, state, normalDesc, virtW, virtH, gridManager, false);
        assertEquals(0, state.getPosX());
        assertEquals(0, state.getPosY());

        dragController.endDrag();
        assertFalse(dragController.isDragging());
    }

    @Test
    void testWorldSpaceAndLockedCannotDrag() {
        int virtW = 960;
        int virtH = 540;

        // WORLD_SPACE rejection
        HudComponentState worldState = worldDesc.createDefaultState();
        boolean worldStarted = dragController.startDrag("nametags", 50, 50, worldState, worldDesc, virtW, virtH);
        assertFalse(worldStarted);
        assertFalse(dragController.isDragging());

        // Locked component rejection
        HudComponentState lockedState = normalDesc.createDefaultState();
        lockedState.setLocked(true);
        boolean lockedStarted = dragController.startDrag("fps", 50, 50, lockedState, normalDesc, virtW, virtH);
        assertFalse(lockedStarted);
        assertFalse(dragController.isDragging());
    }

    @Test
    void testCancelDragRestoresPosition() {
        HudComponentState state = normalDesc.createDefaultState();
        int virtW = 960;
        int virtH = 540;

        dragController.startDrag("fps", 20, 30, state, normalDesc, virtW, virtH);
        dragController.updateDrag(250, 350, state, normalDesc, virtW, virtH, gridManager, false);
        assertEquals(250, state.getPosX());
        assertEquals(350, state.getPosY());

        dragController.cancelDrag(state, virtW, virtH);
        assertFalse(dragController.isDragging());
        assertEquals(20, state.getPosX());
        assertEquals(30, state.getPosY());
    }

    @Test
    void testDragWithSnapping() {
        HudComponentState state = normalDesc.createDefaultState();
        int virtW = 960;
        int virtH = 540;

        gridManager.setSpacing(16);
        gridManager.setSnapToGrid(true);
        gridManager.setSnapThreshold(6);

        dragController.startDrag("fps", 20, 30, state, normalDesc, virtW, virtH);

        // Move to 31 (within 6px of 32, which is 2 * 16)
        dragController.updateDrag(31, 63, state, normalDesc, virtW, virtH, gridManager, true);

        assertEquals(32, state.getPosX());
        assertEquals(64, state.getPosY());
        assertNotNull(dragController.getLastSnapResult());
        assertTrue(dragController.getLastSnapResult().isSnappedXActive());
        assertTrue(dragController.getLastSnapResult().isSnappedYActive());

        dragController.endDrag();
    }
}
