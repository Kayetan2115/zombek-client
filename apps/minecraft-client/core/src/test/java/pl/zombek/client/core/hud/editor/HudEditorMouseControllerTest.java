package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.hud.HudAnchor;

import static org.junit.jupiter.api.Assertions.*;

class HudEditorMouseControllerTest {

    private HudEditorMouseController mouseController;
    private HudEditorState state;
    private HudLayout layout;
    private HudSelectionManager selectionManager;
    private HudCanvasViewport viewport;

    @BeforeEach
    void setUp() {
        HudComponentRegistry.getInstance().resetForTesting();

        HudComponentDescriptor desc = HudComponentDescriptor.builder("scoreboard", "Scoreboard")
                .defaultPosition(100, 100)
                .defaultSize(100, 80)
                .defaultAnchor(HudAnchor.TOP_LEFT)
                .capabilities(HudComponentCapability.POSITION, HudComponentCapability.SIZE, HudComponentCapability.SCALE)
                .build();
        HudComponentRegistry.getInstance().register(desc);

        layout = new HudLayout("default", "Domyślny Układ");
        layout.setComponentState(desc.createDefaultState());

        state = new HudEditorState();
        selectionManager = HudSelectionManager.getInstance();
        selectionManager.resetForTesting();

        viewport = state.getViewport();
        viewport.updateBounds(100, 50, 800, 500, 960, 540);
        viewport.resetZoom();

        mouseController = new HudEditorMouseController(state.getDragController(), state.getResizeController());
    }

    @Test
    void testMiddleClickPansViewport() {
        // Middle click (button = 2) inside canvas
        int mouseX = 200;
        int mouseY = 200;

        boolean consumed = mouseController.handleMouseClick(mouseX, mouseY, 2, state, layout, selectionManager, viewport);
        assertTrue(consumed);
        assertTrue(viewport.isPanning());
        assertEquals(CursorMode.PAN, state.getCursorMode());

        mouseController.handleMouseRelease(mouseX, mouseY, 2, state, layout);
        assertFalse(viewport.isPanning());
        assertEquals(CursorMode.DEFAULT, state.getCursorMode());
    }

    @Test
    void testClickSelectsComponentAndStartsDrag() {
        // Scoreboard is at (100, 100) with size 100x80. Screen coordinate with viewport (origin 20, 30 approx):
        int screenCompX = viewport.virtualToScreenX(120);
        int screenCompY = viewport.virtualToScreenY(120);

        boolean consumed = mouseController.handleMouseClick(screenCompX, screenCompY, 0, state, layout, selectionManager, viewport);
        assertTrue(consumed);
        assertEquals("scoreboard", selectionManager.getSelectedComponentId());
        assertTrue(state.getDragController().isDragging());

        mouseController.handleMouseRelease(screenCompX, screenCompY, 0, state, layout);
        assertFalse(state.getDragController().isDragging());
    }

    @Test
    void testClickOnResizeHandleStartsResize() {
        // First select the component
        selectionManager.select("scoreboard");
        state.setActiveTool(EditorTool.RESIZE);

        // Scoreboard handle Bottom-Right is at (200, 180) in virtual space
        int screenHandleX = viewport.virtualToScreenX(200);
        int screenHandleY = viewport.virtualToScreenY(180);

        boolean consumed = mouseController.handleMouseClick(screenHandleX, screenHandleY, 0, state, layout, selectionManager, viewport);
        assertTrue(consumed);
        assertTrue(state.getResizeController().isResizing());
        assertEquals(HudResizeHandle.BOTTOM_RIGHT, state.getResizeController().getActiveHandle());

        mouseController.handleMouseRelease(screenHandleX, screenHandleY, 0, state, layout);
        assertFalse(state.getResizeController().isResizing());
    }

    @Test
    void testClickOnBackgroundClearsSelection() {
        selectionManager.select("scoreboard");
        assertEquals("scoreboard", selectionManager.getSelectedComponentId());

        // Click on background far from component (e.g. virtX = 500, virtY = 400)
        int screenX = viewport.virtualToScreenX(500);
        int screenY = viewport.virtualToScreenY(400);

        boolean consumed = mouseController.handleMouseClick(screenX, screenY, 0, state, layout, selectionManager, viewport);
        assertTrue(consumed);
        assertNull(selectionManager.getSelectedComponentId());
    }

    @Test
    void testMouseScrollZoomsWhenCtrlHeld() {
        int screenX = viewport.virtualToScreenX(300);
        int screenY = viewport.virtualToScreenY(250);

        assertEquals(1.0, viewport.getZoom(), 0.001);

        // Ctrl + scroll up
        boolean consumed = mouseController.handleMouseScroll(screenX, screenY, 1.0, state, viewport, true, false);
        assertTrue(consumed);
        assertTrue(viewport.getZoom() > 1.0);
    }
}
