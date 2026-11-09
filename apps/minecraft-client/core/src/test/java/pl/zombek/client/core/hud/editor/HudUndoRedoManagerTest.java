package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HudUndoRedoManagerTest {

    private HudUndoRedoManager undoManager;
    private HudLayout layout;

    @BeforeEach
    public void setUp() {
        undoManager = HudUndoRedoManager.getInstance();
        undoManager.resetForTesting();

        layout = new HudLayout("test", "Układ Testowy");
        layout.setComponentState(new HudComponentState("fps", 10, 10, 50, 20));
    }

    @Test
    public void testUndoRedoMove() {
        HudComponentState state = layout.getComponentState("fps");
        assertEquals(10, state.getPosX());

        MoveComponentsCommand cmd = new MoveComponentsCommand("Move fps");
        cmd.record("fps", 10, 10, 100, 150);

        undoManager.executeCommand(cmd, layout);
        assertEquals(100, state.getPosX());
        assertEquals(150, state.getPosY());
        assertTrue(undoManager.canUndo());
        assertFalse(undoManager.canRedo());

        // Undo
        boolean undid = undoManager.undo(layout);
        assertTrue(undid);
        assertEquals(10, state.getPosX());
        assertEquals(10, state.getPosY());
        assertFalse(undoManager.canUndo());
        assertTrue(undoManager.canRedo());

        // Redo
        boolean redid = undoManager.redo(layout);
        assertTrue(redid);
        assertEquals(100, state.getPosX());
        assertEquals(150, state.getPosY());
    }

    @Test
    public void testUndoRedoResize() {
        HudComponentState state = layout.getComponentState("fps");
        assertEquals(50, state.getWidth());
        assertEquals(20, state.getHeight());

        ResizeComponentCommand cmd = new ResizeComponentCommand("fps", 10, 10, 50, 20, 10, 10, 80, 40);
        undoManager.executeCommand(cmd, layout);
        assertEquals(80, state.getWidth());
        assertEquals(40, state.getHeight());

        undoManager.undo(layout);
        assertEquals(50, state.getWidth());
        assertEquals(20, state.getHeight());

        undoManager.redo(layout);
        assertEquals(80, state.getWidth());
        assertEquals(40, state.getHeight());
    }

    @Test
    public void testUndoRedoVisibility() {
        HudComponentState state = layout.getComponentState("fps");
        state.setVisible(true);

        ChangeVisibilityCommand cmd = new ChangeVisibilityCommand("fps", true, false);
        undoManager.executeCommand(cmd, layout);
        assertFalse(state.isVisible());

        undoManager.undo(layout);
        assertTrue(state.isVisible());

        undoManager.redo(layout);
        assertFalse(state.isVisible());
    }

    @Test
    public void testHistoryLimitFifty() {
        for (int i = 0; i < 70; i++) {
            MoveComponentsCommand cmd = new MoveComponentsCommand("Move " + i);
            cmd.record("fps", i, i, i + 1, i + 1);
            undoManager.push(cmd);
        }

        assertEquals(50, undoManager.getUndoHistory().size(), "Stack size should be capped at 50");
    }
}
