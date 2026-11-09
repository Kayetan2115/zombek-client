package pl.zombek.client.core.info;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.TestHudRenderer;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.modules.hud.info.*;

import static org.junit.jupiter.api.Assertions.*;

public class UniversalHudIntegrationTest {

    private CoordinatesModule coordinatesModule;
    private DirectionHudModule directionHudModule;
    private FpsModule fpsModule;
    private PingModule pingModule;
    private MemoryUsageModule memoryUsageModule;

    @BeforeEach
    public void setup() {
        HudElementRegistry.getInstance().clear();
        InfoHudDataProviderRegistry.getInstance().resetToDefault();

        coordinatesModule = new CoordinatesModule();
        directionHudModule = new DirectionHudModule();
        fpsModule = new FpsModule();
        pingModule = new PingModule();
        memoryUsageModule = new MemoryUsageModule();
    }

    @AfterEach
    public void tearDown() {
        HudElementRegistry.getInstance().clear();
    }

    @Test
    public void testCoexistenceOfAllFiveHudModules() {
        coordinatesModule.setEnabled(true);
        directionHudModule.setEnabled(true);
        fpsModule.setEnabled(true);
        pingModule.setEnabled(true);
        memoryUsageModule.setEnabled(true);

        assertEquals(5, HudElementRegistry.getInstance().getElements().size());

        TestHudRenderer renderer = new TestHudRenderer(1920, 1080);
        HudElementRegistry.getInstance().renderAll(renderer);

        // All 5 modules should have drawn their texts and background boxes
        assertTrue(renderer.drawTextCallCount >= 5);
        assertTrue(renderer.fillCallCount >= 5);

        // Disable one module and verify registry updates
        coordinatesModule.setEnabled(false);
        assertEquals(4, HudElementRegistry.getInstance().getElements().size());
    }

    @Test
    public void testResponsivenessAndResolutionChange() {
        coordinatesModule.setEnabled(true);
        fpsModule.setEnabled(true);

        // Small window (e.g. 640x480)
        TestHudRenderer smallRenderer = new TestHudRenderer(640, 480);
        assertDoesNotThrow(() -> HudElementRegistry.getInstance().renderAll(smallRenderer));

        // 4K Ultra HD (3840x2160)
        TestHudRenderer uhdRenderer = new TestHudRenderer(3840, 2160);
        assertDoesNotThrow(() -> HudElementRegistry.getInstance().renderAll(uhdRenderer));
    }
}
