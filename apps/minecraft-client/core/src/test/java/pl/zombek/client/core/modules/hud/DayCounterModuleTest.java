package pl.zombek.client.core.modules.hud;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.hud.daycounter.DayCounterMode;
import pl.zombek.client.core.modules.hud.daycounter.DayCounterModule;
import pl.zombek.client.core.modules.hud.daycounter.DayDisplayFormat;
import pl.zombek.client.core.settings.EnumSetting;
import pl.zombek.client.core.settings.NumberSetting;
import pl.zombek.client.core.time.FallbackTimeDataProvider;
import pl.zombek.client.core.time.MockTimeSource;
import pl.zombek.client.core.time.TimeDataProviderRegistry;
import pl.zombek.client.core.time.ZombekTimeService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DayCounterModuleTest {

    private DayCounterModule module;
    private MockTimeSource mockTimeSource;
    private FallbackTimeDataProvider mockDataProvider;

    @BeforeEach
    public void setUp() {
        mockTimeSource = new MockTimeSource();
        ZombekTimeService.resetForTesting(mockTimeSource);

        mockDataProvider = new FallbackTimeDataProvider();
        TimeDataProviderRegistry.getInstance().registerProvider(mockDataProvider);

        module = new DayCounterModule();
        module.setEnabled(true);
    }

    @AfterEach
    public void tearDown() {
        ZombekTimeService.resetForTesting(null);
    }

    @Test
    public void testModuleMetadataAndCategory() {
        assertEquals("day_counter", module.getId());
        assertEquals("Day Counter", module.getName());
        assertEquals(ModuleCategory.HUD, module.getCategory());
        assertTrue(module.isVisible());
        assertEquals(5, module.getSettingGroups().size()); // general, format, behavior, appearance, layout
    }

    @Test
    public void testWorldDayDisplayAndFormats() {
        mockDataProvider.setWorldLoaded(true);
        // 126 * 24000 = 3024000 ticks -> Day 127
        mockDataProvider.setWorldGameTime(3024000);
        mockDataProvider.setWorldTimeOfDay(6000);

        @SuppressWarnings("unchecked")
        EnumSetting<DayDisplayFormat> formatSetting = (EnumSetting<DayDisplayFormat>) module.getSetting("display_format");

        // DAY_SPACE ("Day 127")
        formatSetting.setValue(DayDisplayFormat.DAY_SPACE);
        List<String> lines = module.buildDisplayLines(ZombekTimeService.getInstance(), mockDataProvider, true);
        assertEquals(1, lines.size());
        assertEquals("Day 127", lines.get(0));

        // DAY_COLON ("Day: 127")
        formatSetting.setValue(DayDisplayFormat.DAY_COLON);
        lines = module.buildDisplayLines(ZombekTimeService.getInstance(), mockDataProvider, true);
        assertEquals("Day: 127", lines.get(0));

        // COMPACT ("D127")
        formatSetting.setValue(DayDisplayFormat.COMPACT);
        lines = module.buildDisplayLines(ZombekTimeService.getInstance(), mockDataProvider, true);
        assertEquals("D127", lines.get(0));
    }

    @Test
    public void testWorldAgeMode() {
        @SuppressWarnings("unchecked")
        EnumSetting<DayCounterMode> modeSetting = (EnumSetting<DayCounterMode>) module.getSetting("counter_mode");
        modeSetting.setValue(DayCounterMode.WORLD_AGE);

        mockDataProvider.setWorldLoaded(true);
        // 10 days and 14 hours: 10 * 24000 + 14 * 1000 = 254000 ticks
        mockDataProvider.setWorldGameTime(254000);
        mockDataProvider.setWorldTimeOfDay(14000);

        List<String> lines = module.buildDisplayLines(ZombekTimeService.getInstance(), mockDataProvider, true);
        assertEquals(1, lines.size());
        assertEquals("Age: 10d 14h", lines.get(0));
    }

    @Test
    public void testStrictUnavailablePolicy() {
        mockDataProvider.setWorldLoaded(true);
        mockDataProvider.setTimeAvailable(false); // E.g. server does not send time
        mockDataProvider.setWorldGameTime(-1);

        List<String> lines = module.buildDisplayLines(ZombekTimeService.getInstance(), mockDataProvider, true);
        assertEquals(1, lines.size());
        assertEquals("Day: N/A", lines.get(0), "Should display Day: N/A and never guess numbers");
    }

    @Test
    public void testTimeResetAndManualOffsetIndicators() {
        mockDataProvider.setWorldLoaded(true);
        mockDataProvider.setTimeAvailable(true);
        mockDataProvider.setWorldTimeOfDay(6000);
        mockDataProvider.setWorldGameTime(24000); // Day 2

        // Manual offset +5
        NumberSetting offsetSetting = (NumberSetting) module.getSetting("manual_offset");
        offsetSetting.setValue(5.0);

        List<String> lines = module.buildDisplayLines(ZombekTimeService.getInstance(), mockDataProvider, true);
        assertEquals("Day 7 (Manual)", lines.get(0));

        // Trigger time reset
        ZombekTimeService.getInstance().updateWorldTime(6000, 100000);
        ZombekTimeService.getInstance().updateWorldTime(6000, 50000); // rolled back

        lines = module.buildDisplayLines(ZombekTimeService.getInstance(), mockDataProvider, true);
        assertTrue(lines.get(0).contains("[Reset]"), "Should display [Reset] diagnostic flag");
    }

    @Test
    public void testPreviewFallback() {
        mockDataProvider.setWorldLoaded(false);

        List<String> lines = module.buildDisplayLines(ZombekTimeService.getInstance(), mockDataProvider, false);
        assertEquals(1, lines.size());
        assertEquals("Day 127 [PREVIEW]", lines.get(0));
    }
}
