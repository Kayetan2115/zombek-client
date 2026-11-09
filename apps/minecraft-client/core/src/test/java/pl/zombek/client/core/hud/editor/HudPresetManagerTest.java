package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class HudPresetManagerTest {

    private HudPresetManager presetManager;

    @BeforeEach
    public void setUp(@TempDir Path tempDir) {
        HudComponentRegistry.getInstance().resetForTesting();
        DefaultHudComponentRegistrar.registerAll();

        presetManager = HudPresetManager.getInstance();
        presetManager.resetForTesting();
        presetManager.initialize(tempDir);
    }

    @AfterEach
    public void tearDown() {
        presetManager.resetForTesting();
        HudComponentRegistry.getInstance().resetForTesting();
    }

    @Test
    public void testEightSystemPresetsRegistered() {
        Collection<HudPreset> presets = presetManager.getAllPresets();
        assertTrue(presets.size() >= 8, "Expected at least 8 system presets");

        assertNotNull(presetManager.getPreset("default"));
        assertNotNull(presetManager.getPreset("minimal"));
        assertNotNull(presetManager.getPreset("pvp"));
        assertNotNull(presetManager.getPreset("survival"));
        assertNotNull(presetManager.getPreset("uhc"));
        assertNotNull(presetManager.getPreset("building"));
        assertNotNull(presetManager.getPreset("performance"));
        assertNotNull(presetManager.getPreset("streamer"));
    }

    @Test
    public void testApplyPvpPreset() {
        HudLayout layout = new HudLayout("test", "Układ Testowy");
        HudLayoutValidator.repairLayout(layout);

        PresetApplyResult result = presetManager.applyPreset("pvp", layout);
        assertTrue(result.isSuccess());
        assertFalse(result.getChangedComponents().isEmpty(), "PvP preset should configure components");

        HudComponentState fpsState = layout.getComponentState("fps");
        assertNotNull(fpsState);
        assertTrue(fpsState.isVisible(), "FPS should be visible in PvP preset");
    }

    @Test
    public void testApplyMinimalPresetHidesSecondary() {
        HudLayout layout = new HudLayout("test", "Układ Testowy");
        HudLayoutValidator.repairLayout(layout);

        PresetApplyResult result = presetManager.applyPreset("minimal", layout);
        assertTrue(result.isSuccess());

        HudComponentState coords = layout.getComponentState("coordinates");
        assertNotNull(coords);
        assertTrue(coords.isVisible());
    }

    @Test
    public void testCreateCustomPresetFromLayout() {
        HudLayout customLayout = new HudLayout("my_layout", "Mój Układ");
        HudComponentState comp = new HudComponentState("fps", 120, 15, 60, 20);
        comp.setVisible(true);
        customLayout.setComponentState(comp);

        boolean created = presetManager.createPresetFromLayout("custom_p1", "Własny Preset", "Opis", customLayout);
        assertTrue(created);

        HudPreset customPreset = presetManager.getPreset("custom_p1");
        assertNotNull(customPreset);
        assertEquals("Własny Preset", customPreset.getName());
        assertFalse(customPreset.isSystemPreset());

        // Delete custom preset
        boolean deleted = presetManager.deleteCustomPreset("custom_p1");
        assertTrue(deleted);
        assertNull(presetManager.getPreset("custom_p1"));
    }

    @Test
    public void testCannotDeleteSystemPreset() {
        assertFalse(presetManager.deleteCustomPreset("pvp"), "System presets must be protected against deletion");
        assertNotNull(presetManager.getPreset("pvp"));
    }
}
