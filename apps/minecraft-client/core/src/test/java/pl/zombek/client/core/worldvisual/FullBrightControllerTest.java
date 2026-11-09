package pl.zombek.client.core.worldvisual;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.worldvisual.fullbright.FullBrightController;
import pl.zombek.client.core.worldvisual.fullbright.FullBrightMode;

import static org.junit.jupiter.api.Assertions.*;

public class FullBrightControllerTest {

    private FullBrightController controller;

    @BeforeEach
    public void setUp() {
        controller = FullBrightController.getInstance();
        controller.resetForTesting();
    }

    @Test
    public void testFullBrightModesAndTargetBrightness() {
        controller.setSmoothBrightness(false); // instant

        controller.setEnabled(true);
        controller.setMode(FullBrightMode.FULL_BRIGHT);
        assertEquals(15.0, controller.getCurrentBrightness(), 0.01);

        controller.setMode(FullBrightMode.NIGHT_VISION_STYLE);
        assertEquals(12.0, controller.getCurrentBrightness(), 0.01);

        controller.setMode(FullBrightMode.CUSTOM);
        controller.setCustomBrightness(8.5);
        assertEquals(8.5, controller.getCurrentBrightness(), 0.01);

        controller.setMode(FullBrightMode.VANILLA);
        assertEquals(0.0, controller.getCurrentBrightness(), 0.01);
    }

    @Test
    public void testComputeBoostedLightLevel() {
        controller.setSmoothBrightness(false);
        controller.setEnabled(true);
        controller.setMode(FullBrightMode.FULL_BRIGHT);

        // When shadow preservation is true, 0-light block gets 14 max
        controller.setShadowPreservation(true);
        assertEquals(14, controller.computeBoostedLightLevel(0));

        // When shadow preservation is false, full 15
        controller.setShadowPreservation(false);
        assertEquals(15, controller.computeBoostedLightLevel(0));

        // Block with existing light 15 stays 15
        assertEquals(15, controller.computeBoostedLightLevel(15));

        // When disabled, original light returned
        controller.setEnabled(false);
        assertEquals(3, controller.computeBoostedLightLevel(3));
    }

    @Test
    public void testComputeEffectiveGammaBounds() {
        controller.setSmoothBrightness(false);
        controller.setEnabled(true);
        controller.setMode(FullBrightMode.FULL_BRIGHT);

        double effectiveGamma = controller.computeEffectiveGamma(0.5);
        assertTrue(effectiveGamma >= 0.5 && effectiveGamma <= 1.0);

        controller.setEnabled(false);
        assertEquals(0.5, controller.computeEffectiveGamma(0.5), 0.001);
    }
}
