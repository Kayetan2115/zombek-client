package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class HudLayoutImportExportTest {

    @BeforeEach
    public void setUp() {
        HudComponentRegistry.getInstance().resetForTesting();
        DefaultHudComponentRegistrar.registerAll();
    }

    @AfterEach
    public void tearDown() {
        HudComponentRegistry.getInstance().resetForTesting();
    }

    @Test
    public void testExportAndImportLayoutReplace(@TempDir Path tempDir) throws IOException {
        HudLayoutImportExportManager manager = HudLayoutImportExportManager.getInstance();

        HudLayout exportLayout = new HudLayout("exp", "Eksportowany");
        HudComponentState fps = new HudComponentState("fps", 123, 456, 60, 25);
        fps.setVisible(true);
        fps.setScale(1.25);
        exportLayout.setComponentState(fps);

        Path file = tempDir.resolve("export_test.json");
        boolean exported = manager.exportLayout(exportLayout, file);
        assertTrue(exported);
        assertTrue(Files.exists(file));

        HudLayout importTarget = new HudLayout("target", "Docelowy");
        ImportResult result = manager.importLayout(file, importTarget, HudLayoutImportExportManager.ImportMode.REPLACE);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getImportedCount());

        HudComponentState importedFps = importTarget.getComponentState("fps");
        assertNotNull(importedFps);
        assertEquals(123, importedFps.getPosX());
        assertEquals(456, importedFps.getPosY());
        assertEquals(1.25, importedFps.getScale(), 0.001);
    }

    @Test
    public void testImportMergePreservesOtherComponents(@TempDir Path tempDir) throws IOException {
        HudLayoutImportExportManager manager = HudLayoutImportExportManager.getInstance();

        HudLayout exportLayout = new HudLayout("exp", "Eksportowany");
        HudComponentState fps = new HudComponentState("fps", 150, 150, 60, 25);
        fps.setVisible(true);
        exportLayout.setComponentState(fps);

        Path file = tempDir.resolve("merge_test.json");
        manager.exportLayout(exportLayout, file);

        HudLayout importTarget = new HudLayout("target", "Docelowy");
        HudComponentState ping = new HudComponentState("ping", 10, 10, 50, 20);
        ping.setVisible(true);
        importTarget.setComponentState(ping);

        ImportResult result = manager.importLayout(file, importTarget, HudLayoutImportExportManager.ImportMode.MERGE);
        assertTrue(result.isSuccess());

        // Both fps and ping should be in importTarget
        assertNotNull(importTarget.getComponentState("fps"));
        assertNotNull(importTarget.getComponentState("ping"));
        assertEquals(10, importTarget.getComponentState("ping").getPosX());
    }

    @Test
    public void testPathTraversalGuard(@TempDir Path tempDir) {
        HudLayoutImportExportManager manager = HudLayoutImportExportManager.getInstance();
        HudLayout layout = new HudLayout("test", "Test");

        Path dangerousPath = tempDir.resolve("..").resolve("unsafe.json");
        assertThrows(SecurityException.class, () -> {
            manager.exportLayout(layout, dangerousPath);
        });
    }
}
