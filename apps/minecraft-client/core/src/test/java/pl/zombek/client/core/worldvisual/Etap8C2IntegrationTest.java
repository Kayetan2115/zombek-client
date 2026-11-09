package pl.zombek.client.core.worldvisual;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.modules.visual.*;
import pl.zombek.client.core.platform.IPlatformAdapter;
import pl.zombek.client.core.worldvisual.fullbright.FullBrightMode;
import pl.zombek.client.core.worldvisual.time.TimeChangerPreset;
import pl.zombek.client.core.worldvisual.weather.WeatherPreset;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class Etap8C2IntegrationTest {

    private ZombekCore core;

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
                return Files.createTempDirectory("zombek_etap8c2_test");
            } catch (Exception e) {
                return Path.of(".");
            }
        }
    }

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
    void testAllVisualModulesRegisteredInCore() {
        ModuleManager manager = ModuleManager.getInstance();

        assertNotNull(manager.getModuleById(LightingModule.ID), "LightingModule must be registered");
        assertNotNull(manager.getModuleById(FullBrightModule.ID), "FullBrightModule must be registered");
        assertNotNull(manager.getModuleById(TimeChangerModule.ID), "TimeChangerModule must be registered");
        assertNotNull(manager.getModuleById(WeatherChangerModule.ID), "WeatherChangerModule must be registered");

        assertEquals(ModuleCategory.VISUAL, manager.getModuleById(LightingModule.ID).getCategory());
        assertEquals(ModuleCategory.VISUAL, manager.getModuleById(FullBrightModule.ID).getCategory());
        assertEquals(ModuleCategory.VISUAL, manager.getModuleById(TimeChangerModule.ID).getCategory());
        assertEquals(ModuleCategory.VISUAL, manager.getModuleById(WeatherChangerModule.ID).getCategory());
    }

    @Test
    void testLightingModuleSettingsAndSync() {
        LightingModule module = (LightingModule) ModuleManager.getInstance().getModuleById(LightingModule.ID);
        assertNotNull(module.getGroup("general"));
        assertNotNull(module.getGroup("intensity"));
        assertNotNull(module.getGroup("distance"));
        assertNotNull(module.getGroup("appearance"));
        assertNotNull(module.getGroup("visibility"));

        module.setEnabled(true);
        module.getMaxLightDistanceSetting().setValue(32.0);
        module.syncToManager();

        assertTrue(ZombekWorldVisualController.getInstance().getDynamicLightManager().isEnabled());
    }

    @Test
    void testFullBrightModuleSettingsAndSync() {
        FullBrightModule module = (FullBrightModule) ModuleManager.getInstance().getModuleById(FullBrightModule.ID);
        assertNotNull(module.getGroup("general"));
        assertNotNull(module.getGroup("rendering"));
        assertNotNull(module.getGroup("animation"));
        assertNotNull(module.getGroup("compatibility"));
        assertNotNull(module.getGroup("behavior"));

        module.setEnabled(true);
        module.getModeSetting().setValue(FullBrightMode.NIGHT_VISION_STYLE);
        module.syncToController();

        assertTrue(ZombekWorldVisualController.getInstance().getFullBrightController().isEnabled());
        assertEquals(FullBrightMode.NIGHT_VISION_STYLE, ZombekWorldVisualController.getInstance().getFullBrightController().getMode());
    }

    @Test
    void testTimeChangerModuleSettingsAndSync() {
        TimeChangerModule module = (TimeChangerModule) ModuleManager.getInstance().getModuleById(TimeChangerModule.ID);
        assertNotNull(module.getGroup("general"));
        assertNotNull(module.getGroup("sky"));
        assertNotNull(module.getGroup("lighting"));
        assertNotNull(module.getGroup("animation"));
        assertNotNull(module.getGroup("dimensions"));
        assertNotNull(module.getGroup("compatibility"));

        module.setEnabled(true);
        module.getTimePresetSetting().setValue(TimeChangerPreset.SUNSET);
        module.syncToController();

        assertTrue(ZombekWorldVisualController.getInstance().getTimeVisualController().isEnabled());
        assertEquals(TimeChangerPreset.SUNSET, ZombekWorldVisualController.getInstance().getTimeVisualController().getPreset());
    }

    @Test
    void testWeatherChangerModuleSettingsAndSync() {
        WeatherChangerModule module = (WeatherChangerModule) ModuleManager.getInstance().getModuleById(WeatherChangerModule.ID);
        assertNotNull(module.getGroup("general"));
        assertNotNull(module.getGroup("rain"));
        assertNotNull(module.getGroup("snow"));
        assertNotNull(module.getGroup("clouds"));
        assertNotNull(module.getGroup("thunder"));
        assertNotNull(module.getGroup("performance"));
        assertNotNull(module.getGroup("animation"));
        assertNotNull(module.getGroup("dimensions"));
        assertNotNull(module.getGroup("compatibility"));

        module.setEnabled(true);
        module.getWeatherPresetSetting().setValue(WeatherPreset.THUNDER);
        module.syncToController();

        assertTrue(ZombekWorldVisualController.getInstance().getWeatherVisualController().isEnabled());
        assertEquals(WeatherPreset.THUNDER, ZombekWorldVisualController.getInstance().getWeatherVisualController().getPreset());
    }

    @Test
    void testResetToDefaultsWorkflow() {
        LightingModule lighting = (LightingModule) ModuleManager.getInstance().getModuleById(LightingModule.ID);
        lighting.getMaxLightDistanceSetting().setValue(50.0);
        lighting.resetToDefaults();
        assertEquals(24.0, lighting.getMaxLightDistanceSetting().getValue());

        FullBrightModule fb = (FullBrightModule) ModuleManager.getInstance().getModuleById(FullBrightModule.ID);
        fb.getModeSetting().setValue(FullBrightMode.CUSTOM);
        fb.resetToDefaults();
        assertEquals(FullBrightMode.FULL_BRIGHT, fb.getModeSetting().getValue());

        TimeChangerModule tc = (TimeChangerModule) ModuleManager.getInstance().getModuleById(TimeChangerModule.ID);
        tc.getTimePresetSetting().setValue(TimeChangerPreset.MIDNIGHT);
        tc.resetToDefaults();
        assertEquals(TimeChangerPreset.NOON, tc.getTimePresetSetting().getValue());

        WeatherChangerModule wc = (WeatherChangerModule) ModuleManager.getInstance().getModuleById(WeatherChangerModule.ID);
        wc.getWeatherPresetSetting().setValue(WeatherPreset.SNOW);
        wc.resetToDefaults();
        assertEquals(WeatherPreset.CLEAR, wc.getWeatherPresetSetting().getValue());
    }
}
