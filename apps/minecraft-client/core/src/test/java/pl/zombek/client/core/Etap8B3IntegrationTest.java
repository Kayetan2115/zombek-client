package pl.zombek.client.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.component.HudComponentModel;
import pl.zombek.client.core.hud.component.HudPriorityResolver;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.modules.hud.clock.ClockModule;
import pl.zombek.client.core.modules.hud.daycounter.DayCounterModule;
import pl.zombek.client.core.modules.hud.playtime.PlaytimeModule;
import pl.zombek.client.core.modules.hud.stopwatch.StopwatchModule;
import pl.zombek.client.core.platform.IPlatformAdapter;
import pl.zombek.client.core.time.FallbackTimeDataProvider;
import pl.zombek.client.core.time.MockTimeSource;
import pl.zombek.client.core.time.TimeDataProviderRegistry;
import pl.zombek.client.core.time.ZombekTimeService;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Etap8B3IntegrationTest {

    @TempDir
    Path tempConfigDir;

    private ZombekCore core;
    private FallbackTimeDataProvider timeDataProvider;

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
        ZombekTimeService.resetForTesting(new MockTimeSource());

        timeDataProvider = new FallbackTimeDataProvider();
        timeDataProvider.setWorldLoaded(true);
        timeDataProvider.setWorldGameTime(240000); // Day 11
        timeDataProvider.setWorldTimeOfDay(6000);   // Noon
        TimeDataProviderRegistry.getInstance().registerProvider(timeDataProvider);

        core = ZombekCore.init(new MockPlatformAdapter(tempConfigDir));
    }

    @AfterEach
    public void tearDown() {
        ZombekCore.resetInstanceForTesting();
        ModuleManager.getInstance().clear();
    }

    @Test
    public void testAllFourModulesRegisteredInCore() {
        ModuleManager mm = core.getModuleManager();
        assertNotNull(mm.getModuleById("clock"), "ClockModule should be registered in core");
        assertNotNull(mm.getModuleById("day_counter"), "DayCounterModule should be registered in core");
        assertNotNull(mm.getModuleById("playtime"), "PlaytimeModule should be registered in core");
        assertNotNull(mm.getModuleById("stopwatch"), "StopwatchModule should be registered in core");
    }

    @Test
    public void testModulesRenderPipelineAndPriorityCollisions() {
        ModuleManager mm = core.getModuleManager();
        AbstractModule clock = mm.getModuleById("clock");
        AbstractModule dayCounter = mm.getModuleById("day_counter");
        AbstractModule playtime = mm.getModuleById("playtime");
        AbstractModule stopwatch = mm.getModuleById("stopwatch");

        clock.setEnabled(true);
        dayCounter.setEnabled(true);
        playtime.setEnabled(true);
        stopwatch.setEnabled(true);

        // Mock HUD renderer
        IHudRenderer mockRenderer = new IHudRenderer() {
            @Override public int getScreenWidth() { return 800; }
            @Override public int getScreenHeight() { return 600; }
            @Override public void fill(int x, int y, int width, int height, int color) {}
            @Override public void drawBorder(int x, int y, int width, int height, int color, int thickness) {}
            @Override public void drawText(String text, int x, int y, int color, boolean shadow, float scale, boolean bold) {}
            @Override public int getTextWidth(String text) { return text != null ? text.length() * 6 : 0; }
            @Override public int getFontHeight() { return 9; }
        };

        // Render each without throwing exceptions
        assertDoesNotThrow(() -> ((IHudElement) clock).render(mockRenderer));
        assertDoesNotThrow(() -> ((IHudElement) dayCounter).render(mockRenderer));
        assertDoesNotThrow(() -> ((IHudElement) playtime).render(mockRenderer));
        assertDoesNotThrow(() -> ((IHudElement) stopwatch).render(mockRenderer));

        // Test collision resolver with clock and dayCounter stacked at TOP_RIGHT
        List<HudComponentModel> components = new ArrayList<>();
        components.add(((ClockModule) clock).getHudModel());
        components.add(((DayCounterModule) dayCounter).getHudModel());

        // Set both to same position to force collision
        components.get(0).setAnchor(HudAnchor.TOP_RIGHT);
        components.get(0).setPosX(5);
        components.get(0).setPosY(5);
        components.get(0).setWidth(80);
        components.get(0).setHeight(20);

        components.get(1).setAnchor(HudAnchor.TOP_RIGHT);
        components.get(1).setPosX(5);
        components.get(1).setPosY(5);
        components.get(1).setWidth(80);
        components.get(1).setHeight(20);

        HudPriorityResolver.resolveCollisions(components, 800, 600, 4);

        // Clock has higher priority (40 vs 35), so clock stays at Y=5, dayCounter shifted down
        assertEquals(5, components.get(0).getPosY());
        assertTrue(components.get(1).getPosY() >= 25, "Lower priority DayCounter should be shifted down to avoid collision");
    }
}
