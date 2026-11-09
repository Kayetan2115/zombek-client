package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class Etap9CIntegrationTest {

    private HudEditorManager editorManager;
    private HudLayoutManager layoutManager;
    private Path layoutFile;

    @BeforeEach
    public void setUp(@TempDir Path tempDir) {
        HudComponentRegistry.getInstance().resetForTesting();
        DefaultHudComponentRegistrar.registerAll();

        editorManager = HudEditorManager.getInstance();
        layoutManager = HudLayoutManager.getInstance();

        layoutManager.resetForTesting();
        editorManager.resetForTesting();

        layoutFile = tempDir.resolve("hud_layout.json");
        layoutManager.setLayoutFilePath(layoutFile);
        layoutManager.load();
    }

    @AfterEach
    public void tearDown() {
        editorManager.closeEditor();
        editorManager.resetForTesting();
        layoutManager.resetForTesting();
        HudComponentRegistry.getInstance().resetForTesting();
    }

    @Test
    public void testFullEtap9CWorkflow() throws IOException {
        // 1. Open editor session
        editorManager.openEditor();
        assertTrue(editorManager.isEditorOpen());

        HudLayout staged = layoutManager.getStagedLayout();
        assertNotNull(staged);

        // 2. Multi-selection
        HudMultiSelectionManager multiSel = editorManager.getState().getMultiSelectionManager();
        multiSel.selectAll(Arrays.asList("fps", "ping"));
        assertEquals(2, multiSel.getSelectedCount());

        // 3. Create Group from selection
        HudGroup group = editorManager.getState().getGroupManager().createGroup(staged, "net_group", "Sieć i Płynność", multiSel.getSelectedIds());
        assertNotNull(group);
        assertEquals("net_group", staged.getGroup("net_group").getGroupId());
        assertEquals("net_group", staged.getComponentState("fps").getGroupId());
        assertEquals("net_group", staged.getComponentState("ping").getGroupId());

        // 4. Layer management: assign to information layer
        HudLayerManager layerMgr = editorManager.getState().getLayerManager();
        layerMgr.assignComponentToLayer(staged, "fps", HudLayer.LAYER_INFORMATION);
        assertEquals(HudLayer.LAYER_INFORMATION, staged.getComponentState("fps").getLayerId());

        // 5. Move components with undo tracking
        int oldFpsX = staged.getComponentState("fps").getPosX();
        int oldFpsY = staged.getComponentState("fps").getPosY();

        MoveComponentsCommand moveCmd = new MoveComponentsCommand("Move fps");
        moveCmd.record("fps", oldFpsX, oldFpsY, oldFpsX + 50, oldFpsY + 50);
        editorManager.getState().getUndoRedoManager().executeCommand(moveCmd, staged);

        assertEquals(oldFpsX + 50, staged.getComponentState("fps").getPosX());
        assertEquals(oldFpsY + 50, staged.getComponentState("fps").getPosY());
        assertTrue(editorManager.getState().getUndoRedoManager().canUndo());

        // 6. Undo the move
        editorManager.getState().getUndoRedoManager().undo(staged);
        assertEquals(oldFpsX, staged.getComponentState("fps").getPosX());
        assertEquals(oldFpsY, staged.getComponentState("fps").getPosY());

        // 7. Clipboard copy & paste
        HudComponentClipboard clipboard = editorManager.getState().getClipboard();
        clipboard.copyComponents(staged, Collections.singleton("fps"));
        assertTrue(clipboard.hasContent());

        clipboard.paste(staged, HudComponentClipboard.PasteMode.IN_PLACE, 0, 0);
        assertTrue(staged.getComponentState("fps").isVisible());

        // 8. Commit changes to active layout and disk
        layoutManager.commitStagedChanges();
        assertFalse(layoutManager.hasUnsavedChanges());
        assertTrue(Files.exists(layoutFile));

        // 9. Close editor session
        editorManager.closeEditor();
        assertFalse(editorManager.isEditorOpen());
    }
}
