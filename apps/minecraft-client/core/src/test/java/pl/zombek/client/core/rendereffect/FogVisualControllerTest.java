package pl.zombek.client.core.rendereffect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.visual.FogCustomizerModule;
import pl.zombek.client.core.rendereffect.fog.FogColorController;
import pl.zombek.client.core.rendereffect.fog.FogDistanceController;
import pl.zombek.client.core.rendereffect.fog.FogEnvironmentType;
import pl.zombek.client.core.rendereffect.fog.FogPreset;
import pl.zombek.client.core.rendereffect.fog.FogVisualController;

import static org.junit.jupiter.api.Assertions.*;

public class FogVisualControllerTest {

    private FogVisualController controller;

    @BeforeEach
    public void setup() {
        controller = FogVisualController.getInstance();
        controller.reset();
        controller.setActive(true);
    }

    @Test
    public void testSafeDistanceClipping() {
        FogDistanceController distanceController = new FogDistanceController();
        distanceController.setMinFogDistance(5.0f);
        distanceController.setMaxFogDistance(128.0f);

        float[] distances = distanceController.computeDistances(
                16.0f, 128.0f, FogPreset.REDUCED_FOG, null
        );

        assertNotNull(distances);
        assertEquals(2, distances.length);
        assertTrue(distances[0] < distances[1], "Fog start must be less than fog end");
        assertTrue(distances[0] >= 5.0f, "Fog start must respect min fog distance");
    }

    @Test
    public void testNeverExceedRenderDistance() {
        FogDistanceController distanceController = new FogDistanceController();
        distanceController.setMinFogDistance(0.0f);
        distanceController.setMaxFogDistance(64.0f);

        float[] distances = distanceController.computeDistances(
                10.0f, 64.0f, FogPreset.VANILLA, null
        );

        assertTrue(distances[1] <= 64.0f, "Fog end must be capped at view distance");
    }

    @Test
    public void testFogColorBlending() {
        FogColorController colorController = new FogColorController();
        colorController.setUseCustomColor(true);
        colorController.setCustomColorArgb(0xFFFF0000); // Red
        colorController.setColorBlend(1.0f);

        float[] blended = colorController.computeColor(
                0.8f, 0.8f, 0.9f, 1.0f, "minecraft:overworld", null
        );

        assertEquals(1.0f, blended[0], 0.01f, "Red should be 1.0");
        assertEquals(0.0f, blended[1], 0.01f, "Green should be 0.0");
        assertEquals(0.0f, blended[2], 0.01f, "Blue should be 0.0");
    }

    @Test
    public void testModuleSyncAndPresets() {
        FogCustomizerModule module = new FogCustomizerModule();
        module.setEnabled(true);
        module.getPresetSetting().setValue(FogPreset.CLEAR_VIEW);
        module.syncToController();

        assertTrue(controller.isActive());
        assertEquals(FogPreset.CLEAR_VIEW, controller.getProfileManager().getActivePreset());

        module.setEnabled(false);
        assertFalse(controller.isActive());
    }
}
