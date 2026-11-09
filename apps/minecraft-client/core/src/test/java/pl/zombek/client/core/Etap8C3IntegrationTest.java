package pl.zombek.client.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.modules.visual.ColorSaturationModule;
import pl.zombek.client.core.modules.visual.FogCustomizerModule;
import pl.zombek.client.core.modules.visual.GlintColorizerModule;
import pl.zombek.client.core.modules.visual.ParticleChangerModule;
import pl.zombek.client.core.platform.IPlatformAdapter;
import pl.zombek.client.core.rendereffect.RenderEffectSnapshot;
import pl.zombek.client.core.rendereffect.ZombekRenderEffectController;
import pl.zombek.client.core.rendereffect.color.ColorGradingController;
import pl.zombek.client.core.rendereffect.color.ColorProfile;
import pl.zombek.client.core.rendereffect.fog.FogPreset;
import pl.zombek.client.core.rendereffect.fog.FogVisualController;
import pl.zombek.client.core.rendereffect.glint.GlintLocation;
import pl.zombek.client.core.rendereffect.glint.GlintMode;
import pl.zombek.client.core.rendereffect.glint.GlintRenderController;
import pl.zombek.client.core.rendereffect.particle.ParticlePreset;
import pl.zombek.client.core.rendereffect.particle.ParticleRenderController;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class Etap8C3IntegrationTest {

    @TempDir
    Path tempConfigDir;

    private ZombekCore core;

    static class MockPlatformAdapter implements IPlatformAdapter {
        private final Path dir;
        MockPlatformAdapter(Path dir) { this.dir = dir; }
        @Override public String getLoaderName() { return "TestLoader"; }
        @Override public String getLoaderVersion() { return "1.0.0"; }
        @Override public String getMinecraftVersion() { return "1.20.4"; }
        @Override public Path getGameDirectory() { return dir; }
        @Override public Path getConfigDirectory() { return dir; }
        @Override public boolean isClient() { return true; }
        @Override public void openMenu() {}
    }

    @BeforeEach
    public void setUp() {
        ModuleManager.getInstance().clear();
        ZombekCore.resetInstanceForTesting();
        core = ZombekCore.init(new MockPlatformAdapter(tempConfigDir));
    }

    @AfterEach
    public void tearDown() {
        if (core != null) {
            core.shutdown();
        }
        ZombekCore.resetInstanceForTesting();
    }

    @Test
    public void testModulesRegisteredInCore() {
        ModuleManager manager = ModuleManager.getInstance();
        assertNotNull(manager.getModuleById("fog_customizer"), "Fog Customizer should be registered");
        assertNotNull(manager.getModuleById("color_saturation"), "Color Saturation should be registered");
        assertNotNull(manager.getModuleById("glint_colorizer"), "Glint Colorizer should be registered");
        assertNotNull(manager.getModuleById("particle_changer"), "Particle Changer should be registered");
    }

    @Test
    public void testFogCustomizerIntegration() {
        FogCustomizerModule module = (FogCustomizerModule) ModuleManager.getInstance().getModuleById("fog_customizer");
        assertNotNull(module);

        module.setEnabled(true);
        module.getPresetSetting().setValue(FogPreset.REDUCED_FOG);
        module.syncToController();

        assertTrue(FogVisualController.getInstance().isActive());
        assertEquals(FogPreset.REDUCED_FOG, FogVisualController.getInstance().getProfileManager().getActivePreset());

        module.setEnabled(false);
        assertFalse(FogVisualController.getInstance().isActive());
    }

    @Test
    public void testColorSaturationIntegration() {
        ColorSaturationModule module = (ColorSaturationModule) ModuleManager.getInstance().getModuleById("color_saturation");
        assertNotNull(module);

        module.setEnabled(true);
        module.getPresetSetting().setValue(ColorProfile.VIBRANT);
        module.syncToController();

        assertTrue(ColorGradingController.getInstance().isActive());

        module.setEnabled(false);
        assertFalse(ColorGradingController.getInstance().isActive());
    }

    @Test
    public void testGlintColorizerIntegration() {
        GlintColorizerModule module = (GlintColorizerModule) ModuleManager.getInstance().getModuleById("glint_colorizer");
        assertNotNull(module);

        module.setEnabled(true);
        module.getModeSetting().setValue(GlintMode.STATIC);
        module.getPrimaryColorSetting().setValue(0xFFFF2255);
        module.syncToController();

        assertTrue(GlintRenderController.getInstance().isActive());
        int color = GlintRenderController.getInstance().resolveGlintColor("minecraft:netherite_sword", true, GlintLocation.HOTBAR, 1000L);
        assertEquals(0xFFFF2255, color);

        module.setEnabled(false);
        assertFalse(GlintRenderController.getInstance().isActive());
    }

    @Test
    public void testParticleChangerIntegration() {
        ParticleChangerModule module = (ParticleChangerModule) ModuleManager.getInstance().getModuleById("particle_changer");
        assertNotNull(module);

        module.setEnabled(true);
        module.getPresetSetting().setValue(ParticlePreset.MINIMAL_PARTICLES);
        module.syncToController();

        assertTrue(ParticleRenderController.getInstance().isActive());
        assertEquals(300, ParticleRenderController.getInstance().getSettings().getMaxParticleLimit());

        module.setEnabled(false);
        assertFalse(ParticleRenderController.getInstance().isActive());
    }

    @Test
    public void testUnifiedSnapshotAndTickDispatch() {
        ZombekRenderEffectController controller = ZombekRenderEffectController.getInstance();

        // Tick call should execute cleanly without exceptions
        core.onClientTick();

        RenderEffectSnapshot snapshot = controller.getSnapshot();
        assertNotNull(snapshot);
        assertNotNull(snapshot.getFogParameters());
        assertNotNull(snapshot.getColorSettings());
        assertNotNull(snapshot.getGlintSettings());
        assertNotNull(snapshot.getParticleSettings());
    }
}
