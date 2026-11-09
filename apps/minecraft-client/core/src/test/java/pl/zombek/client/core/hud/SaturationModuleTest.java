package pl.zombek.client.core.hud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.TestHudRenderer;
import pl.zombek.client.core.hud.component.HudComponentStyle;
import pl.zombek.client.core.hud.saturation.*;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.hud.saturation.SaturationModule;

import static org.junit.jupiter.api.Assertions.*;

public class SaturationModuleTest {

    private SaturationModule module;
    private FallbackSaturationDataProvider provider;

    @BeforeEach
    public void setup() {
        SaturationDataProviderRegistry.getInstance().resetToDefault();
        provider = (FallbackSaturationDataProvider) SaturationDataProviderRegistry.getInstance().getProvider();
        provider.setPreviewMode(false);
        module = new SaturationModule();
    }

    @Test
    public void testModuleMetadata() {
        assertEquals("saturation", module.getId());
        assertEquals("Saturation", module.getName());
        assertEquals(ModuleCategory.HUD, module.getCategory());
        assertEquals(6, module.getSettingGroups().size());
        assertFalse(module.isVisible());
        module.setEnabled(true);
        assertTrue(module.isVisible());
    }

    @Test
    public void testRenderNormalHungerAndSaturation() {
        provider.setFoodLevel(18);
        provider.setSaturationLevel(12.5f);
        provider.setSaturationAvailable(true);

        module.setEnabled(true);
        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawTextCallCount > 0);
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("18/20")));
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("12.5") || t.contains("12,5")));
    }

    @Test
    public void testStrictPolicyWhenSaturationUnavailable() {
        provider.setFoodLevel(15);
        provider.setSaturationLevel(0.0f);
        provider.setSaturationAvailable(false); // Server did not send saturation packets

        module.setEnabled(true);
        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawTextCallCount > 0);
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("15/20")));
        // Must show N/A or Unavailable, never guess a value!
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("N/A") || t.contains("Niedostępne")));
        assertTrue(module.getStatusDescription().contains("Niedostępne"));
    }

    @Test
    public void testLowHungerWarningTrigger() {
        provider.setFoodLevel(4); // Below 6.0 threshold
        provider.setSaturationLevel(0.0f);
        provider.setSaturationAvailable(true);

        module.setEnabled(true);
        module.getHighlightLowHungerSetting().setValue(true);
        module.getLowHungerThresholdSetting().setValue(6.0);

        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawTextCallCount > 0);
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("4/20")));
    }

    @Test
    public void testLayoutModes() {
        provider.setFoodLevel(20);
        provider.setSaturationLevel(20.0f);
        provider.setSaturationAvailable(true);

        module.setEnabled(true);

        for (SaturationLayoutMode layout : SaturationLayoutMode.values()) {
            module.getLayoutModeSetting().setValue(layout);
            TestHudRenderer renderer = new TestHudRenderer(800, 600);
            module.render(renderer);
            assertTrue(renderer.drawTextCallCount > 0, "Layout mode " + layout + " failed to render");
        }
    }

    @Test
    public void testPreviewModeFallback() {
        provider.setPlayerAvailable(false);
        module.setEnabled(true);
        module.getPreviewModeSetting().setValue(true);

        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawTextCallCount > 0);
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("PREVIEW")));
    }

    @Test
    public void testExhaustionDisplay() {
        provider.setFoodLevel(20);
        provider.setSaturationLevel(10.0f);
        provider.setExhaustionLevel(2.5f);
        provider.setSaturationAvailable(true);

        module.setEnabled(true);
        module.getShowExhaustionSetting().setValue(true);
        module.getLayoutModeSetting().setValue(SaturationLayoutMode.DETAILED);

        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawTextCallCount > 0);
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("Zmęczenie") || t.contains("Exhaustion") || t.contains("2.5") || t.contains("2,5")));
    }
}
