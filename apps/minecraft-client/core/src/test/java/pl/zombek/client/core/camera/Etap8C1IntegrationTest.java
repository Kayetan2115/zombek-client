package pl.zombek.client.core.camera;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.core.hud.editor.DefaultHudComponentRegistrar;
import pl.zombek.client.core.hud.editor.HudComponentDescriptor;
import pl.zombek.client.core.hud.editor.HudComponentRegistry;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.modules.visual.FovChangerModule;
import pl.zombek.client.core.modules.visual.HurtCamModule;
import pl.zombek.client.core.modules.visual.MotionBlurModule;
import pl.zombek.client.core.modules.visual.ZoomModule;
import pl.zombek.client.core.platform.IPlatformAdapter;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class Etap8C1IntegrationTest {

    private ZombekCore core;

    @BeforeEach
    void setUp() {
        ZombekCore.resetInstanceForTesting();
        core = ZombekCore.init(new TestPlatformAdapter());
    }

    @AfterEach
    void tearDown() {
        ZombekCore.resetInstanceForTesting();
    }

    @Test
    void testAllFourVisualModulesRegisteredInCore() {
        ModuleManager mm = ModuleManager.getInstance();

        AbstractModule zoom = mm.getModuleById(ZoomModule.ID);
        AbstractModule fov = mm.getModuleById(FovChangerModule.ID);
        AbstractModule motionBlur = mm.getModuleById(MotionBlurModule.ID);
        AbstractModule hurtCam = mm.getModuleById(HurtCamModule.ID);

        assertNotNull(zoom, "ZoomModule must be registered in ModuleManager");
        assertNotNull(fov, "FovChangerModule must be registered in ModuleManager");
        assertNotNull(motionBlur, "MotionBlurModule must be registered in ModuleManager");
        assertNotNull(hurtCam, "HurtCamModule must be registered in ModuleManager");

        assertTrue(zoom instanceof ZoomModule);
        assertTrue(fov instanceof FovChangerModule);
        assertTrue(motionBlur instanceof MotionBlurModule);
        assertTrue(hurtCam instanceof HurtCamModule);
    }

    @Test
    void testModuleKeybindAssignments() {
        ZoomModule zoom = (ZoomModule) ModuleManager.getInstance().getModuleById(ZoomModule.ID);
        FovChangerModule fov = (FovChangerModule) ModuleManager.getInstance().getModuleById(FovChangerModule.ID);
        MotionBlurModule motionBlur = (MotionBlurModule) ModuleManager.getInstance().getModuleById(MotionBlurModule.ID);
        HurtCamModule hurtCam = (HurtCamModule) ModuleManager.getInstance().getModuleById(HurtCamModule.ID);

        // Zoom keybind is GLFW_KEY_Z by default
        assertEquals(GLFW.GLFW_KEY_Z, zoom.getKeybind());
        assertTrue(zoom.getKeybindSetting().isBound());

        // The other 3 modules are unbound by default
        assertFalse(fov.getKeybindSetting().isBound(), "FOV Changer should be unbound by default");
        assertFalse(motionBlur.getKeybindSetting().isBound(), "Motion Blur should be unbound by default");
        assertFalse(hurtCam.getKeybindSetting().isBound(), "Hurt Cam should be unbound by default");
    }

    @Test
    void testZoomIndicatorHudComponentRegistered() {
        Optional<HudComponentDescriptor> opt = HudComponentRegistry.getInstance().getDescriptor("zoom_indicator");
        assertTrue(opt.isPresent(), "zoom_indicator must be registered in HudComponentRegistry");
        HudComponentDescriptor descriptor = opt.get();
        assertEquals("Zoom Indicator", descriptor.getDisplayName());
        assertEquals("Visual", descriptor.getCategory());
        assertEquals(64, descriptor.getDefaultWidth());
        assertEquals(20, descriptor.getDefaultHeight());
    }

    @Test
    void testResetToDefaultsWorkflow() {
        ZoomModule zoom = (ZoomModule) ModuleManager.getInstance().getModuleById(ZoomModule.ID);
        zoom.getDefaultZoomLevelSetting().setValue(12.0);
        zoom.getActivationModeSetting().setValue(ZoomActivationMode.TOGGLE);
        assertEquals(12.0, zoom.getDefaultZoomLevelSetting().getValue());
        assertEquals(ZoomActivationMode.TOGGLE, zoom.getActivationModeSetting().getValue());

        zoom.resetToDefaults();
        assertEquals(4.0, zoom.getDefaultZoomLevelSetting().getValue());
        assertEquals(ZoomActivationMode.HOLD, zoom.getActivationModeSetting().getValue());

        FovChangerModule fov = (FovChangerModule) ModuleManager.getInstance().getModuleById(FovChangerModule.ID);
        fov.getCustomFovSetting().setValue(120.0);
        fov.resetToDefaults();
        assertEquals(90.0, fov.getCustomFovSetting().getValue());

        MotionBlurModule mb = (MotionBlurModule) ModuleManager.getInstance().getModuleById(MotionBlurModule.ID);
        mb.getBlurIntensitySetting().setValue(0.95);
        mb.resetToDefaults();
        assertEquals(0.5, mb.getBlurIntensitySetting().getValue());

        HurtCamModule hc = (HurtCamModule) ModuleManager.getInstance().getModuleById(HurtCamModule.ID);
        hc.getModeSetting().setValue(HurtCamMode.DISABLED);
        hc.resetToDefaults();
        assertEquals(HurtCamMode.REDUCED, hc.getModeSetting().getValue());
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
                return java.nio.file.Files.createTempDirectory("zombek_etap8c1_test");
            } catch (Exception e) {
                return Path.of(".");
            }
        }
    }
}
