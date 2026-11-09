package pl.zombek.client.core.worldvisual;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.worldvisual.time.SkyTimeOverride;
import pl.zombek.client.core.worldvisual.time.TimeChangerPreset;
import pl.zombek.client.core.worldvisual.time.WorldTimeVisualController;

import static org.junit.jupiter.api.Assertions.*;

public class WorldTimeVisualControllerTest {

    private WorldTimeVisualController controller;

    @BeforeEach
    public void setUp() {
        controller = WorldTimeVisualController.getInstance();
        controller.resetForTesting();
        controller.getTransitionManager().configure(false, 0); // instant
    }

    @Test
    public void testVisualTimePresets() {
        controller.setEnabled(true);

        controller.setPreset(TimeChangerPreset.NOON);
        assertEquals(6000L, controller.getVisualTimeOfDay(18000L, "minecraft:overworld"));

        controller.setPreset(TimeChangerPreset.MIDNIGHT);
        assertEquals(18000L, controller.getVisualTimeOfDay(6000L, "minecraft:overworld"));

        controller.setPreset(TimeChangerPreset.SUNSET);
        assertEquals(12000L, controller.getVisualTimeOfDay(1000L, "minecraft:overworld"));

        controller.setPreset(TimeChangerPreset.CUSTOM);
        controller.setCustomTimeTicks(4500L);
        assertEquals(4500L, controller.getVisualTimeOfDay(1000L, "minecraft:overworld"));
    }

    @Test
    public void testDimensionFiltering() {
        controller.setEnabled(true);
        controller.setPreset(TimeChangerPreset.NOON);
        controller.setEnableInOverworld(true);
        controller.setEnableInNether(false);
        controller.setEnableInEnd(false);

        // Overworld gets override
        assertEquals(6000L, controller.getVisualTimeOfDay(18000L, "minecraft:overworld"));

        // Nether stays server time
        assertEquals(18000L, controller.getVisualTimeOfDay(18000L, "minecraft:the_nether"));

        // End stays server time
        assertEquals(18000L, controller.getVisualTimeOfDay(18000L, "minecraft:the_end"));
    }

    @Test
    public void testCelestialSkyAngleCalculation() {
        // Celestial angle at noon (tick 6000) is 0.0 (sun directly at peak)
        float noonAngle = SkyTimeOverride.calculateSkyAngle(6000L);
        assertEquals(0.0f, noonAngle, 0.05f);

        // At midnight (tick 18000), angle is approx 0.5
        float midnightAngle = SkyTimeOverride.calculateSkyAngle(18000L);
        assertEquals(0.5f, midnightAngle, 0.05f);
    }

    @Test
    public void testDisabledLeavesWorldTimeUnaffected() {
        controller.setEnabled(false);
        assertEquals(15420L, controller.getVisualTimeOfDay(15420L, "minecraft:overworld"));
    }
}
