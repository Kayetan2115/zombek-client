package pl.zombek.client.core.modules.hud;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.hud.playtime.PlaytimeFormat;
import pl.zombek.client.core.modules.hud.playtime.PlaytimeModule;
import pl.zombek.client.core.settings.BooleanSetting;
import pl.zombek.client.core.settings.EnumSetting;
import pl.zombek.client.core.time.FallbackTimeDataProvider;
import pl.zombek.client.core.time.MockTimeSource;
import pl.zombek.client.core.time.TimeDataProviderRegistry;
import pl.zombek.client.core.time.ZombekTimeService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PlaytimeModuleTest {

    private PlaytimeModule module;
    private MockTimeSource mockTimeSource;
    private FallbackTimeDataProvider mockDataProvider;

    @BeforeEach
    public void setUp() {
        mockTimeSource = new MockTimeSource();
        ZombekTimeService.resetForTesting(mockTimeSource);

        mockDataProvider = new FallbackTimeDataProvider();
        TimeDataProviderRegistry.getInstance().registerProvider(mockDataProvider);

        module = new PlaytimeModule();
        module.setEnabled(true);
    }

    @AfterEach
    public void tearDown() {
        ZombekTimeService.resetForTesting(null);
    }

    @Test
    public void testModuleMetadataAndCategory() {
        assertEquals("playtime", module.getId());
        assertEquals("Playtime", module.getName());
        assertEquals(ModuleCategory.HUD, module.getCategory());
        assertTrue(module.isVisible());
        assertEquals(7, module.getSettingGroups().size()); // general, tracking, behavior, privacy, format, appearance, layout
    }

    @Test
    public void testClientAndWorldSessionDistinction() {
        ZombekTimeService svc = ZombekTimeService.getInstance();
        mockDataProvider.setWorldLoaded(true);

        // Client running for 100 seconds
        mockTimeSource.advanceSeconds(100);

        // World joined 40 seconds later
        svc.onWorldJoin("MyWorld", null, true);
        mockTimeSource.advanceSeconds(40);

        // Client session should be 140s, World session should be 40s
        assertEquals(140L, svc.getClientSessionSeconds());
        assertEquals(40L, svc.getWorldSessionSeconds());

        List<String> lines = module.buildDisplayLines(svc, mockDataProvider, true);
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("00:00:40"), "World session should display 40s");
        assertTrue(lines.get(1).contains("00:02:20"), "Client session should display 140s (02:20)");
    }

    @Test
    public void testSingleplayerStatsVsMultiplayer() {
        ZombekTimeService svc = ZombekTimeService.getInstance();
        mockDataProvider.setWorldLoaded(true);

        BooleanSetting showWorldStats = (BooleanSetting) module.getSetting("show_world_stats");
        showWorldStats.setValue(true);

        // Singleplayer with 72000 ticks (3600 seconds = 1 hour)
        mockDataProvider.setSingleplayer(true);
        mockDataProvider.setSingleplayerPlaytimeTicks(72000);

        List<String> lines = module.buildDisplayLines(svc, mockDataProvider, true);
        boolean foundWorldStats = false;
        for (String line : lines) {
            if (line.contains("Świat:") && line.contains("01:00:00")) {
                foundWorldStats = true;
                break;
            }
        }
        assertTrue(foundWorldStats, "Should display Singleplayer world stats");

        // Multiplayer mode
        mockDataProvider.setSingleplayer(false);
        lines = module.buildDisplayLines(svc, mockDataProvider, true);
        boolean foundMpNa = false;
        for (String line : lines) {
            if (line.contains("Świat: N/A (Multiplayer)")) {
                foundMpNa = true;
                break;
            }
        }
        assertTrue(foundMpNa, "Should display N/A for multiplayer world stats");
    }

    @Test
    public void testTimeFormats() {
        ZombekTimeService svc = ZombekTimeService.getInstance();
        mockDataProvider.setWorldLoaded(true);
        svc.onWorldJoin("Test", null, true);

        // 3665 seconds = 1h 1m 5s
        mockTimeSource.advanceSeconds(3665);

        @SuppressWarnings("unchecked")
        EnumSetting<PlaytimeFormat> formatSetting = (EnumSetting<PlaytimeFormat>) module.getSetting("time_format");

        // HH_MM_SS
        formatSetting.setValue(PlaytimeFormat.HH_MM_SS);
        List<String> lines = module.buildDisplayLines(svc, mockDataProvider, true);
        assertTrue(lines.get(0).contains("01:01:05"));

        // HUMAN_READABLE
        formatSetting.setValue(PlaytimeFormat.HUMAN_READABLE);
        lines = module.buildDisplayLines(svc, mockDataProvider, true);
        assertTrue(lines.get(0).contains("1h 1m 5s"));

        // COMPACT
        formatSetting.setValue(PlaytimeFormat.COMPACT);
        lines = module.buildDisplayLines(svc, mockDataProvider, true);
        assertTrue(lines.get(0).contains("1h 1m"));
    }

    @Test
    public void testPrivacyDefaultSettings() {
        BooleanSetting saveServerName = (BooleanSetting) module.getSetting("save_server_name");
        assertFalse(saveServerName.getValue(), "Saving server name must be strictly opt-in (default false)");
    }

    @Test
    public void testPreviewFallback() {
        mockDataProvider.setWorldLoaded(false);

        List<String> lines = module.buildDisplayLines(ZombekTimeService.getInstance(), mockDataProvider, false);
        assertEquals(1, lines.size());
        assertEquals("Session: 02:35:41 [PREVIEW]", lines.get(0));
    }
}
