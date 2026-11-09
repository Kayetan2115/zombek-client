package pl.zombek.client.core.modules.hud;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.hud.clock.ClockMode;
import pl.zombek.client.core.modules.hud.clock.ClockModule;
import pl.zombek.client.core.settings.EnumSetting;
import pl.zombek.client.core.time.FallbackTimeDataProvider;
import pl.zombek.client.core.time.MockTimeSource;
import pl.zombek.client.core.time.TimeDataProviderRegistry;
import pl.zombek.client.core.time.ZombekTimeService;

import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ClockModuleTest {

    private ClockModule module;
    private MockTimeSource mockTimeSource;
    private FallbackTimeDataProvider mockDataProvider;

    @BeforeEach
    public void setUp() {
        // 2026-11-03 14:35:27 UTC (Tuesday)
        mockTimeSource = new MockTimeSource(1793716527000L, 0L, ZoneId.of("UTC"));
        ZombekTimeService.resetForTesting(mockTimeSource);

        mockDataProvider = new FallbackTimeDataProvider();
        TimeDataProviderRegistry.getInstance().registerProvider(mockDataProvider);

        module = new ClockModule();
        module.setEnabled(true);
    }

    @AfterEach
    public void tearDown() {
        ZombekTimeService.resetForTesting(null);
    }

    @Test
    public void testModuleMetadataAndCategory() {
        assertEquals("clock", module.getId());
        assertEquals("Clock", module.getName());
        assertEquals(ModuleCategory.HUD, module.getCategory());
        assertTrue(module.isVisible());
        assertEquals(5, module.getSettingGroups().size()); // general, format, minecraft_time, appearance, layout
    }

    @Test
    public void testDigitalAndMinimalModes() {
        @SuppressWarnings("unchecked")
        EnumSetting<ClockMode> modeSetting = (EnumSetting<ClockMode>) module.getSetting("clock_mode");

        // DIGITAL (with seconds)
        modeSetting.setValue(ClockMode.DIGITAL);
        List<String> digitalLines = module.buildDisplayLines(ZombekTimeService.getInstance(), mockDataProvider, false);
        assertEquals(1, digitalLines.size());
        assertTrue(digitalLines.get(0).contains("14:35:27"), "Digital mode should contain HH:MM:SS");

        // MINIMAL (without seconds)
        modeSetting.setValue(ClockMode.MINIMAL);
        List<String> minimalLines = module.buildDisplayLines(ZombekTimeService.getInstance(), mockDataProvider, false);
        assertEquals(1, minimalLines.size());
        assertEquals("14:35", minimalLines.get(0));
    }

    @Test
    public void testDetailedMode() {
        @SuppressWarnings("unchecked")
        EnumSetting<ClockMode> modeSetting = (EnumSetting<ClockMode>) module.getSetting("clock_mode");
        modeSetting.setValue(ClockMode.DETAILED);

        List<String> lines = module.buildDisplayLines(ZombekTimeService.getInstance(), mockDataProvider, false);
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("2026"), "Line 1 should contain date");
        assertTrue(lines.get(1).contains("14:35"), "Line 2 should contain time");
    }

    @Test
    public void testMinecraftModeInGame() {
        @SuppressWarnings("unchecked")
        EnumSetting<ClockMode> modeSetting = (EnumSetting<ClockMode>) module.getSetting("clock_mode");
        modeSetting.setValue(ClockMode.MINECRAFT);

        mockDataProvider.setWorldLoaded(true);
        mockDataProvider.setWorldTimeOfDay(6000); // 12:00 Noon

        List<String> lines = module.buildDisplayLines(ZombekTimeService.getInstance(), mockDataProvider, true);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("12:00"), "Should display 12:00");
        assertTrue(lines.get(0).contains("☀"), "Should contain sun icon");
        assertTrue(lines.get(0).contains("Południe"), "Should contain phase name");
    }

    @Test
    public void testDualMode() {
        @SuppressWarnings("unchecked")
        EnumSetting<ClockMode> modeSetting = (EnumSetting<ClockMode>) module.getSetting("clock_mode");
        modeSetting.setValue(ClockMode.DUAL);

        mockDataProvider.setWorldLoaded(true);
        mockDataProvider.setWorldTimeOfDay(18000); // 00:00 Midnight

        List<String> lines = module.buildDisplayLines(ZombekTimeService.getInstance(), mockDataProvider, true);
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).startsWith("Real: 14:35"), "Line 1 should be real time");
        assertTrue(lines.get(1).startsWith("MC: 🌙 00:00"), "Line 2 should be MC time");
    }

    @Test
    public void testPreviewModeFallback() {
        @SuppressWarnings("unchecked")
        EnumSetting<ClockMode> modeSetting = (EnumSetting<ClockMode>) module.getSetting("clock_mode");
        modeSetting.setValue(ClockMode.MINECRAFT);

        // World not loaded
        mockDataProvider.setWorldLoaded(false);

        List<String> lines = module.buildDisplayLines(ZombekTimeService.getInstance(), mockDataProvider, false);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("[PREVIEW]"), "Should show preview when world is not loaded");
    }

    @Test
    public void testRestoreDefaults() {
        module.getSetting("show_seconds").reset();
        module.getSetting("clock_mode").reset();
        assertEquals(ClockMode.DIGITAL, module.getClockMode());
    }
}
