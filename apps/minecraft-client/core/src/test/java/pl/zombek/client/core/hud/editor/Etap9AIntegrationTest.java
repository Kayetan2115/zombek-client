package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.platform.IPlatformAdapter;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class Etap9AIntegrationTest {

    private ZombekCore core;

    @BeforeEach
    public void setup() {
        ZombekCore.resetInstanceForTesting();
        core = ZombekCore.init(new TestPlatformAdapter());
    }

    @AfterEach
    public void teardown() {
        ZombekCore.resetInstanceForTesting();
    }

    @Test
    public void testRegistryPopulatedOnBootstrap() {
        HudComponentRegistry registry = HudComponentRegistry.getInstance();
        Collection<HudComponentDescriptor> descriptors = registry.getAllDescriptors();

        assertTrue(descriptors.size() >= 30, "Expected at least 30 registered HUD descriptors, got: " + descriptors.size());

        // Check key elements from each category
        assertTrue(registry.hasDescriptor("keystrokes"));
        assertTrue(registry.hasDescriptor("cps"));
        assertTrue(registry.hasDescriptor("coordinates"));
        assertTrue(registry.hasDescriptor("fps"));
        assertTrue(registry.hasDescriptor("ping"));
        assertTrue(registry.hasDescriptor("memory_usage"));
        assertTrue(registry.hasDescriptor("scoreboard"));
        assertTrue(registry.hasDescriptor("potion_effects"));
        assertTrue(registry.hasDescriptor("saturation"));
        assertTrue(registry.hasDescriptor("bossbar"));
        assertTrue(registry.hasDescriptor("clock"));
        assertTrue(registry.hasDescriptor("day_counter"));
        assertTrue(registry.hasDescriptor("playtime"));
        assertTrue(registry.hasDescriptor("stopwatch"));
        assertTrue(registry.hasDescriptor("item_counter"));
        assertTrue(registry.hasDescriptor("item_tracker"));
        assertTrue(registry.hasDescriptor("server_address"));
        assertTrue(registry.hasDescriptor("net_graph"));
        assertTrue(registry.hasDescriptor("uhc_session_hud"));
        assertTrue(registry.hasDescriptor("bedwars_helper"));
        assertTrue(registry.hasDescriptor("crosshair"));
        assertTrue(registry.hasDescriptor("nametags"));
        assertTrue(registry.hasDescriptor("chat"));
        assertTrue(registry.hasDescriptor("tab_editor"));
        assertTrue(registry.hasDescriptor("titles"));

        // Check Categories
        Set<String> categories = registry.getCategories();
        assertTrue(categories.contains("HUD"));
        assertTrue(categories.contains("PvP"));
        assertTrue(categories.contains("Survival"));
        assertTrue(categories.contains("Visual"));
        assertTrue(categories.contains("Other"));

        // Check Types
        assertFalse(registry.getDescriptorsByType(HudComponentType.SCREEN_SPACE).isEmpty());
        assertFalse(registry.getDescriptorsByType(HudComponentType.CENTER_SCREEN).isEmpty());
        assertFalse(registry.getDescriptorsByType(HudComponentType.WORLD_SPACE).isEmpty());
        assertFalse(registry.getDescriptorsByType(HudComponentType.GUI_OVERLAY).isEmpty());
    }

    @Test
    public void testLayoutManagerPopulatedAndStagingWorkflow() {
        HudLayoutManager layoutManager = HudLayoutManager.getInstance();
        assertNotNull(layoutManager.getActiveLayout());
        assertTrue(layoutManager.getActiveLayout().getComponents().size() >= 30);

        HudEditorManager editorManager = HudEditorManager.getInstance();
        editorManager.openEditor();
        assertTrue(editorManager.isEditorOpen());
        assertFalse(layoutManager.hasUnsavedChanges());

        // Mutate a component in staged layout
        HudLayout staged = layoutManager.getStagedLayout();
        HudComponentState fps = staged.getComponentState("fps");
        assertNotNull(fps);
        int oldX = fps.getPosX();
        fps.setPosX(oldX + 40);
        fps.setAnchor(HudAnchor.BOTTOM_RIGHT);

        assertTrue(layoutManager.hasUnsavedChanges());

        // Commit staged changes
        boolean saved = layoutManager.commitStagedChanges();
        assertTrue(saved);
        assertFalse(layoutManager.hasUnsavedChanges());

        // Verify active layout has the new position
        assertEquals(oldX + 40, layoutManager.getActiveLayout().getComponentState("fps").getPosX());
        assertEquals(HudAnchor.BOTTOM_RIGHT, layoutManager.getActiveLayout().getComponentState("fps").getAnchor());

        editorManager.closeEditor();
        assertFalse(editorManager.isEditorOpen());
    }

    private static class TestPlatformAdapter implements IPlatformAdapter {
        @Override
        public String getLoaderName() { return "Test"; }
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
                return java.nio.file.Files.createTempDirectory("zombek_etap9a_test");
            } catch (Exception e) {
                return Path.of(".");
            }
        }
    }
}
