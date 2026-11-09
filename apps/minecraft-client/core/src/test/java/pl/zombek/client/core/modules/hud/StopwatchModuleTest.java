package pl.zombek.client.core.modules.hud;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.hud.stopwatch.StopwatchModule;
import pl.zombek.client.core.settings.BooleanSetting;
import pl.zombek.client.core.time.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StopwatchModuleTest {

    private StopwatchModule module;
    private MockTimeSource mockTimeSource;

    @BeforeEach
    public void setUp() {
        mockTimeSource = new MockTimeSource();
        ZombekTimeService.resetForTesting(mockTimeSource);

        module = new StopwatchModule();
        module.setEnabled(true);
    }

    @AfterEach
    public void tearDown() {
        ZombekTimeService.resetForTesting(null);
    }

    @Test
    public void testModuleMetadataAndCategory() {
        assertEquals("stopwatch", module.getId());
        assertEquals("Stopwatch", module.getName());
        assertEquals(ModuleCategory.HUD, module.getCategory());
        assertTrue(module.isVisible());
        assertEquals(6, module.getSettingGroups().size()); // general, display, behavior, keybinds, appearance, layout
    }

    @Test
    public void testStartPauseResetControls() {
        StopwatchProfile profile = module.getActiveProfile();
        assertNotNull(profile);
        assertTrue(profile.isStopped());

        module.start();
        assertTrue(profile.isRunning());

        mockTimeSource.advanceSeconds(15);
        List<String> lines = module.buildDisplayLines(profile);
        assertTrue(lines.get(0).contains("00:15.00"), "Should display 15s elapsed");

        module.pause();
        assertTrue(profile.isPaused());
        lines = module.buildDisplayLines(profile);
        assertTrue(lines.get(0).contains("[PAUSED]"), "Should show [PAUSED] indicator");

        module.reset();
        assertTrue(profile.isStopped());
    }

    @Test
    public void testLapDisplayInModule() {
        StopwatchProfile profile = module.getActiveProfile();
        module.start();

        mockTimeSource.advanceSeconds(35);
        module.recordLap();

        mockTimeSource.advanceSeconds(42);
        module.recordLap();

        List<String> lines = module.buildDisplayLines(profile);
        assertTrue(lines.size() >= 3);
        assertTrue(lines.get(1).contains("LAP 1"), "Line 1 should be LAP 1");
        assertTrue(lines.get(2).contains("LAP 2"), "Line 2 should be LAP 2");
    }

    @Test
    public void testBestAndAverageLapDisplay() {
        StopwatchProfile profile = module.getActiveProfile();
        module.start();

        mockTimeSource.advanceSeconds(20);
        module.recordLap();
        mockTimeSource.advanceSeconds(40);
        module.recordLap();

        BooleanSetting showBest = (BooleanSetting) module.getSetting("show_best_lap");
        BooleanSetting showAvg = (BooleanSetting) module.getSetting("show_avg_lap");
        showBest.setValue(true);
        showAvg.setValue(true);

        List<String> lines = module.buildDisplayLines(profile);
        boolean hasBest = false;
        boolean hasAvg = false;
        for (String l : lines) {
            if (l.startsWith("Best: 00:20")) hasBest = true;
            if (l.startsWith("Avg: 00:30")) hasAvg = true;
        }
        assertTrue(hasBest, "Should display Best Lap");
        assertTrue(hasAvg, "Should display Average Lap");
    }

    @Test
    public void testPreviewFallback() {
        StopwatchProfile profile = module.getActiveProfile();
        profile.reset();

        List<String> lines = module.buildDisplayLines(profile);
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("[PREVIEW]"), "Should display preview when stopped at 0");
    }
}
