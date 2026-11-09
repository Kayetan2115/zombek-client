package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.zombek.client.core.hud.HudAnchor;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HudEditorManagerTest {

    private HudEditorManager editorManager;
    private HudLayoutManager layoutManager;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        HudComponentRegistry.getInstance().resetForTesting();
        DefaultHudComponentRegistrar.registerAll();

        editorManager = HudEditorManager.getInstance();
        editorManager.resetForTesting();

        layoutManager = HudLayoutManager.getInstance();
        layoutManager.setLayoutFilePath(tempDir.resolve("hud_layout.json"));
        layoutManager.resetForTesting();
    }

    @Test
    void testOpenCloseLifecycle() {
        assertFalse(editorManager.isEditorOpen());

        editorManager.openEditor();
        assertTrue(editorManager.isEditorOpen());
        assertFalse(layoutManager.hasUnsavedChanges());

        editorManager.closeEditor();
        assertFalse(editorManager.isEditorOpen());
    }

    @Test
    void testStagedChangesCommitAndDiscard() {
        editorManager.openEditor();
        HudLayout staged = layoutManager.getStagedLayout();
        assertNotNull(staged);

        HudComponentState fpsState = staged.getComponentState("fps");
        assertNotNull(fpsState);
        int initialX = fpsState.getPosX();

        // Mutate staged state
        fpsState.setPosX(initialX + 50);
        assertTrue(layoutManager.hasUnsavedChanges());

        // Discard changes
        layoutManager.discardStagedChanges();
        assertFalse(layoutManager.hasUnsavedChanges());
        assertEquals(initialX, layoutManager.getStagedLayout().getComponentState("fps").getPosX());

        // Mutate again and commit
        layoutManager.getStagedLayout().getComponentState("fps").setPosX(initialX + 75);
        assertTrue(layoutManager.hasUnsavedChanges());
        layoutManager.commitStagedChanges();
        assertFalse(layoutManager.hasUnsavedChanges());
        assertEquals(initialX + 75, layoutManager.getActiveLayout().getComponentState("fps").getPosX());
    }

    @Test
    void testResetComponentToDefault() {
        editorManager.openEditor();
        HudLayout staged = layoutManager.getStagedLayout();
        HudComponentState fpsState = staged.getComponentState("fps");

        // Mutate
        fpsState.setPosX(999);
        fpsState.setScale(2.5);
        fpsState.setAnchor(HudAnchor.BOTTOM_RIGHT);

        layoutManager.resetComponentToDefault("fps");

        HudComponentState resetFps = staged.getComponentState("fps");
        HudComponentDescriptor desc = HudComponentRegistry.getInstance().getDescriptor("fps").orElseThrow();
        assertEquals(desc.getDefaultX(), resetFps.getPosX());
        assertEquals(desc.getDefaultScale(), resetFps.getScale(), 0.001);
        assertEquals(desc.getDefaultAnchor(), resetFps.getAnchor());
    }
}
