package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.core.platform.IPlatformAdapter;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class Etap9BIntegrationTest {

    private ZombekCore core;
    private HudEditorManager editorManager;
    private HudLayoutManager layoutManager;
    private Path layoutFile;

    private static class TestPlatformAdapter implements IPlatformAdapter {
        @Override
        public String getLoaderName() { return "TestPlatform"; }
        @Override
        public String getLoaderVersion() { return "1.0.0"; }
        @Override
        public String getMinecraftVersion() { return "1.20.4"; }
        @Override
        public void openMenu() {}
        @Override
        public boolean isClient() { return true; }
        @Override
        public Path getGameDirectory() { return Path.of("."); }
        @Override
        public Path getConfigDirectory() {
            try {
                return java.nio.file.Files.createTempDirectory("zombek_etap9b_test");
            } catch (Exception e) {
                return Path.of(".");
            }
        }
    }

    @BeforeEach
    public void setup(@TempDir Path tempDir) {
        HudComponentRegistry.getInstance().resetForTesting();
        DefaultHudComponentRegistrar.registerAll();

        ZombekCore.resetInstanceForTesting();
        core = ZombekCore.init(new TestPlatformAdapter());

        editorManager = HudEditorManager.getInstance();
        editorManager.resetForTesting();

        layoutManager = HudLayoutManager.getInstance();
        layoutManager.resetForTesting();
        layoutFile = tempDir.resolve("hud_layout.json");
        layoutManager.setLayoutFilePath(layoutFile);
    }

    @AfterEach
    public void teardown() {
        ZombekCore.resetInstanceForTesting();
    }

    @Test
    public void testFullInteractionLifecycle() {
        editorManager.openEditor();
        assertTrue(editorManager.isEditorOpen());
        assertFalse(layoutManager.hasUnsavedChanges());

        HudLayout staged = layoutManager.getStagedLayout();
        HudComponentState fpsState = staged.getComponentState("fps");
        assertNotNull(fpsState);
        int initialFpsX = fpsState.getPosX();
        int initialFpsY = fpsState.getPosY();

        // 1. Drag & Drop interaction
        HudComponentDescriptor fpsDesc = HudComponentRegistry.getInstance().getDescriptor("fps").orElseThrow();
        HudDragController drag = editorManager.getState().getDragController();
        assertTrue(drag.startDrag("fps", initialFpsX + 10, initialFpsY + 10, fpsState, fpsDesc, 960, 540));
        drag.updateDrag(200, 150, fpsState, fpsDesc, 960, 540, editorManager.getState().getGridManager(), true);
        drag.endDrag();
        assertFalse(drag.isDragging());
        assertNotEquals(initialFpsX, fpsState.getPosX());
        assertTrue(layoutManager.hasUnsavedChanges());

        // 2. Resize interaction on Scoreboard
        HudComponentState sbState = staged.getComponentState("scoreboard");
        assertNotNull(sbState);
        HudComponentDescriptor sbDesc = HudComponentRegistry.getInstance().getDescriptor("scoreboard").orElseThrow();
        int initialSbW = sbState.getWidth();
        int initialSbH = sbState.getHeight();

        HudResizeController resize = editorManager.getState().getResizeController();
        int handleX = sbState.calculateScreenX(960) + sbState.getEffectiveWidth();
        int handleY = sbState.calculateScreenY(540) + sbState.getEffectiveHeight();
        assertTrue(resize.startResize("scoreboard", HudResizeHandle.BOTTOM_RIGHT, handleX, handleY, sbState, sbDesc, 960, 540));
        resize.updateResize(handleX + 30, handleY + 20, sbState, sbDesc, 960, 540, false);
        resize.endResize();
        assertFalse(resize.isResizing());
        assertEquals(initialSbW + 30, sbState.getWidth());
        assertEquals(initialSbH + 20, sbState.getHeight());

        // 3. Scaling interaction
        assertTrue(HudScaleController.setScale(fpsState, fpsDesc, 1.50));
        assertEquals(1.50, fpsState.getScale(), 0.001);

        // 4. Alignment interaction
        assertTrue(HudAlignmentManager.centerOnCanvas(fpsState, fpsDesc, 960, 540));
        int expectedCenterX = (960 - fpsState.getEffectiveWidth()) / 2;
        int expectedCenterY = (540 - fpsState.getEffectiveHeight()) / 2;
        assertEquals(expectedCenterX, fpsState.getPosX());
        assertEquals(expectedCenterY, fpsState.getPosY());

        // 5. Commit & Persistence
        assertTrue(layoutManager.commitStagedChanges());
        assertFalse(layoutManager.hasUnsavedChanges());
        assertTrue(Files.exists(layoutFile));

        // Active layout contains all changes
        HudLayout active = layoutManager.getActiveLayout();
        assertEquals(expectedCenterX, active.getComponentState("fps").getPosX());
        assertEquals(expectedCenterY, active.getComponentState("fps").getPosY());
        assertEquals(1.50, active.getComponentState("fps").getScale(), 0.001);
        assertEquals(initialSbW + 30, active.getComponentState("scoreboard").getWidth());

        editorManager.closeEditor();
        assertFalse(editorManager.isEditorOpen());
    }

    @Test
    public void testCancelRevertsAllInteractions() {
        editorManager.openEditor();
        HudLayout staged = layoutManager.getStagedLayout();
        HudComponentState fpsState = staged.getComponentState("fps");
        int initialX = fpsState.getPosX();

        // Mutate
        fpsState.setPosX(initialX + 150);
        fpsState.setScale(2.0);
        assertTrue(layoutManager.hasUnsavedChanges());

        // Discard
        layoutManager.discardStagedChanges();
        assertFalse(layoutManager.hasUnsavedChanges());
        assertEquals(initialX, layoutManager.getStagedLayout().getComponentState("fps").getPosX());
        assertNotEquals(2.0, layoutManager.getStagedLayout().getComponentState("fps").getScale());

        editorManager.closeEditor();
    }

    @Test
    public void testIntermediateDragFramesDoNotWriteToDisk() {
        assertFalse(Files.exists(layoutFile));

        editorManager.openEditor();
        HudComponentState fpsState = layoutManager.getStagedLayout().getComponentState("fps");
        HudComponentDescriptor fpsDesc = HudComponentRegistry.getInstance().getDescriptor("fps").orElseThrow();

        HudDragController drag = editorManager.getState().getDragController();
        drag.startDrag("fps", 100, 100, fpsState, fpsDesc, 960, 540);

        for (int i = 0; i < 20; i++) {
            drag.updateDrag(100 + i * 5, 100 + i * 5, fpsState, fpsDesc, 960, 540, editorManager.getState().getGridManager(), false);
            // Intermediate frames must NOT create or update disk file
            assertFalse(Files.exists(layoutFile), "Intermediate drag frame must not write to disk!");
        }

        drag.endDrag();
        assertFalse(Files.exists(layoutFile));

        // Only commit writes to disk
        layoutManager.commitStagedChanges();
        assertTrue(Files.exists(layoutFile));
    }
}
