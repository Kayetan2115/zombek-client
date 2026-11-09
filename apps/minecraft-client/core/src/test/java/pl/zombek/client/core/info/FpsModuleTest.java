package pl.zombek.client.core.info;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.TestHudRenderer;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.hud.info.FpsModule;

import static org.junit.jupiter.api.Assertions.*;

public class FpsModuleTest {

    private FpsModule module;
    private InfoHudDataProviderRegistry.FallbackInfoDataProvider fallbackProvider;

    @BeforeEach
    public void setup() {
        InfoHudDataProviderRegistry.getInstance().resetToDefault();
        fallbackProvider = (InfoHudDataProviderRegistry.FallbackInfoDataProvider) InfoHudDataProviderRegistry.getInstance().getProvider();
        module = new FpsModule();
    }

    @Test
    public void testMetadataAndSettingsGroups() {
        assertEquals("fps", module.getId());
        assertEquals("FPS Counter", module.getName());
        assertEquals(ModuleCategory.HUD, module.getCategory());
        assertEquals(4, module.getSettingGroups().size());
    }

    @Test
    public void testRenderingCurrentFps() {
        fallbackProvider.setFps(144);
        module.setEnabled(true);

        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawTextCallCount > 0);
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("144 FPS")));
    }

    @Test
    public void testRenderingStatisticalValues() {
        module.getShowAverageFps().setValue(true);
        module.getShowMinimumFps().setValue(true);
        module.getShowMaximumFps().setValue(true);
        module.setEnabled(true);

        fallbackProvider.setFps(120);
        TestHudRenderer renderer = new TestHudRenderer();
        module.render(renderer);

        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("Avg:")));
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("Min:")));
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("Max:")));
    }

    @Test
    public void testGraphRendering() {
        module.getEnableGraph().setValue(true);
        fallbackProvider.setFps(75);
        module.setEnabled(true);

        TestHudRenderer renderer = new TestHudRenderer();
        module.render(renderer);

        // Fill count includes box outline and background
        assertTrue(renderer.fillCallCount > 1);
    }

    @Test
    public void testResetStatistics() {
        fallbackProvider.setFps(200);
        module.setEnabled(true);
        module.render(new TestHudRenderer());

        assertEquals(200, module.getTracker().getCurrentFps());
        module.resetStatistics();
        assertEquals(60, module.getTracker().getCurrentFps());
    }
}
