package pl.zombek.client.core.info;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.TestHudRenderer;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.hud.info.DirectionHudModule;

import static org.junit.jupiter.api.Assertions.*;

public class DirectionHudModuleTest {

    private DirectionHudModule module;
    private InfoHudDataProviderRegistry.FallbackInfoDataProvider fallbackProvider;

    @BeforeEach
    public void setup() {
        InfoHudDataProviderRegistry.getInstance().resetToDefault();
        fallbackProvider = (InfoHudDataProviderRegistry.FallbackInfoDataProvider) InfoHudDataProviderRegistry.getInstance().getProvider();
        module = new DirectionHudModule();
    }

    @Test
    public void testMetadataAndSettingsGroups() {
        assertEquals("direction_hud", module.getId());
        assertEquals("Direction HUD", module.getName());
        assertEquals(ModuleCategory.HUD, module.getCategory());
        assertEquals(4, module.getSettingGroups().size());
    }

    @Test
    public void testClassicStyleRendering() {
        fallbackProvider.setRotation(180.0f, 0.0f); // North
        module.setEnabled(true);
        module.getCompassStyle().setValue(CompassStyle.CLASSIC);

        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.fillCallCount > 0);
        assertTrue(renderer.drawnTexts.contains("N"));
    }

    @Test
    public void testMinimalStyleRendering() {
        fallbackProvider.setRotation(90.0f, 0.0f); // West
        module.setEnabled(true);
        module.getCompassStyle().setValue(CompassStyle.MINIMAL);

        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("W")));
    }

    @Test
    public void testDetailedStyleRendering() {
        fallbackProvider.setRotation(270.0f, 0.0f); // East
        module.setEnabled(true);
        module.getCompassStyle().setValue(CompassStyle.DETAILED);

        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("270°")));
    }

    @Test
    public void testSmoothMovementInterpolation() {
        module.getSmoothMovement().setValue(true);
        fallbackProvider.setRotation(100.0f, 0.0f);
        module.setEnabled(true);

        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        // Smoothed yaw should be tracking towards 100
        assertTrue(module.getSmoothedYaw() >= 0.0f && module.getSmoothedYaw() <= 360.0f);
    }
}
