package pl.zombek.client.core.worldvisual;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.worldvisual.weather.WeatherPreset;
import pl.zombek.client.core.worldvisual.weather.WeatherVisualController;

import static org.junit.jupiter.api.Assertions.*;

public class WeatherVisualControllerTest {

    private WeatherVisualController controller;

    @BeforeEach
    public void setUp() {
        controller = WeatherVisualController.getInstance();
        controller.resetForTesting();
        controller.getTransitionManager().configure(false, 0); // instant
    }

    @Test
    public void testWeatherPresets() {
        controller.setEnabled(true);

        // CLEAR: 0.0 rain, 0.0 thunder even if server is storming
        controller.setPreset(WeatherPreset.CLEAR);
        assertEquals(0.0f, controller.getVisualRainGradient(1.0f, "minecraft:overworld", true), 0.01f);
        assertEquals(0.0f, controller.getVisualThunderGradient(1.0f, "minecraft:overworld"), 0.01f);

        // RAIN: 1.0 rain, 0.0 thunder
        controller.setPreset(WeatherPreset.RAIN);
        assertEquals(1.0f, controller.getVisualRainGradient(0.0f, "minecraft:overworld", true), 0.01f);
        assertEquals(0.0f, controller.getVisualThunderGradient(0.0f, "minecraft:overworld"), 0.01f);

        // THUNDER: 1.0 rain, 0.8 thunder
        controller.setPreset(WeatherPreset.THUNDER);
        assertEquals(1.0f, controller.getVisualRainGradient(0.0f, "minecraft:overworld", true), 0.01f);
        assertEquals(0.8f, controller.getVisualThunderGradient(0.0f, "minecraft:overworld"), 0.01f);
    }

    @Test
    public void testBiomeClimateRespect() {
        controller.setEnabled(true);
        controller.setPreset(WeatherPreset.RAIN);
        controller.setRespectBiomeClimate(true);
        controller.setOverrideBiomeRestrictions(false);

        // In a biome where it naturally rains (forest/plains): 1.0
        assertEquals(1.0f, controller.getVisualRainGradient(0.0f, "minecraft:overworld", true), 0.01f);

        // In desert/savanna where it cannot rain (biomeCanRain = false): 0.0
        assertEquals(0.0f, controller.getVisualRainGradient(0.0f, "minecraft:overworld", false), 0.01f);

        // If overrideBiomeRestrictions is true: 1.0 even in desert
        controller.setOverrideBiomeRestrictions(true);
        assertEquals(1.0f, controller.getVisualRainGradient(0.0f, "minecraft:overworld", false), 0.01f);
    }

    @Test
    public void testDisabledLeavesServerWeatherUnaffected() {
        controller.setEnabled(false);
        assertEquals(0.75f, controller.getVisualRainGradient(0.75f, "minecraft:overworld", true), 0.01f);
        assertEquals(0.5f, controller.getVisualThunderGradient(0.5f, "minecraft:overworld"), 0.01f);
    }
}
