package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.survival.UHCOverlayModule;
import pl.zombek.client.core.uhc.UhcItemScalingController;
import pl.zombek.client.core.uhc.UhcScaleMode;

import static org.junit.jupiter.api.Assertions.*;

public class UhcItemScalingControllerTest {

    private UhcItemScalingController controller;
    private UHCOverlayModule module;

    @BeforeEach
    public void setUp() {
        module = new UHCOverlayModule();
        controller = UhcItemScalingController.getInstance();
        controller.clearCustomRules();
    }

    @Test
    public void testScaleWhenDisabled() {
        module.setEnabled(false);
        assertEquals(1.0f, controller.resolveScale("minecraft:golden_apple", null));
    }

    @Test
    public void testScalePerItemMode() {
        module.setEnabled(true);
        module.getScaleModeSetting().setValue(UhcScaleMode.PER_ITEM);
        module.getGoldenApplesScaleSetting().setValue(2.5);
        module.getEnderPearlsScaleSetting().setValue(1.8);

        assertEquals(2.5f, controller.resolveScale("minecraft:golden_apple", null), 0.001f);
        assertEquals(1.8f, controller.resolveScale("minecraft:ender_pearl", null), 0.001f);
        assertEquals(1.2f, controller.resolveScale("minecraft:dirt", null), 0.001f);
        assertEquals(1.0f, controller.resolveScale("minecraft:stick", null), 0.001f);
    }

    @Test
    public void testGoldenHeadDetection() {
        module.setEnabled(true);
        module.getGoldenHeadsScaleSetting().setValue(3.0);

        assertTrue(controller.isGoldenHead("minecraft:player_head", "Golden Head"));
        assertTrue(controller.isGoldenHead("minecraft:player_head", "Złota Głowa"));
        assertFalse(controller.isGoldenHead("minecraft:player_head", "Notch's Head"));

        assertEquals(3.0f, controller.resolveScale("minecraft:player_head", "Golden Head"), 0.001f);
    }

    @Test
    public void testCustomRules() {
        module.setEnabled(true);
        controller.addCustomRule("minecraft:diamond_sword", 2.2f);

        assertEquals(2.2f, controller.resolveScale("minecraft:diamond_sword", null), 0.001f);

        controller.removeCustomRule("minecraft:diamond_sword");
        assertEquals(1.0f, controller.resolveScale("minecraft:diamond_sword", null), 0.001f);
    }

    @Test
    public void testCombinedScaleMode() {
        module.setEnabled(true);
        module.getScaleModeSetting().setValue(UhcScaleMode.COMBINED);
        module.getGlobalScaleSetting().setValue(1.5);
        module.getArrowsScaleSetting().setValue(2.0);

        // 2.0 * 1.5 = 3.0
        assertEquals(3.0f, controller.resolveScale("minecraft:arrow", null), 0.001f);
    }
}
