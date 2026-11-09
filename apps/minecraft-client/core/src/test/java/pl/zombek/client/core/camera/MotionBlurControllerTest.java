package pl.zombek.client.core.camera;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.visual.MotionBlurModule;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class MotionBlurControllerTest {

    private MotionBlurController controller;
    private MotionBlurModule module;

    @BeforeEach
    void setUp() {
        controller = MotionBlurController.getInstance();
        controller.resetForTesting();
        module = MotionBlurModule.getInstance();
        module.resetToDefaults();
        module.setEnabled(true);
    }

    @Test
    void test360DegreeAngleWrapping() {
        // Initial sample
        controller.updateCameraRotation(359.0f, 0.0f);
        // Small 2-degree turn crossing 360 wrap
        controller.updateCameraRotation(1.0f, 0.0f);

        // Alpha should be based on small 2 degree movement, not 358 degrees
        assertTrue(controller.getCurrentBlurAlpha() >= 0.0f);
        assertTrue(controller.getCurrentBlurAlpha() < 0.5f, "Crossing 360 degree boundary must wrap correctly");
    }

    @Test
    void testCameraMovementThresholdAndDecay() {
        module.getBlurSmoothingSetting().setValue(false);
        module.getCameraMovementThresholdSetting().setValue(1.0);
        module.getBlurIntensitySetting().setValue(1.0);
        module.getBlurStrengthSetting().setValue(1.0);

        // First sample
        controller.updateCameraRotation(0.0f, 0.0f);
        // Significant motion (20 degrees)
        controller.updateCameraRotation(20.0f, 0.0f);
        float activeAlpha = controller.getCurrentBlurAlpha();
        assertTrue(activeAlpha > 0.05f, "Active rotation should generate blur alpha");

        // Now move below threshold (0.2 degrees)
        controller.updateCameraRotation(20.2f, 0.0f);
        float decayedAlpha = controller.getCurrentBlurAlpha();
        assertTrue(decayedAlpha < activeAlpha, "Sub-threshold movement should trigger decay");
    }

    @Test
    void testAdapterIntegration() {
        AtomicReference<Float> renderedAlpha = new AtomicReference<>(0.0f);
        AtomicBoolean rendered = new AtomicBoolean(false);

        IMotionBlurRendererAdapter testAdapter = new IMotionBlurRendererAdapter() {
            @Override
            public boolean isSupported() {
                return true;
            }

            @Override
            public void onResolutionChanged(int width, int height) {}

            @Override
            public void renderBlur(float blurAlpha) {
                renderedAlpha.set(blurAlpha);
                rendered.set(true);
            }

            @Override
            public void clearHistory() {}

            @Override
            public void cleanup() {}
        };

        controller.setAdapter(testAdapter);
        assertFalse(controller.isFallbackEngaged());

        // First sample
        controller.updateCameraRotation(0.0f, 0.0f);
        // Big turn
        controller.updateCameraRotation(40.0f, 10.0f);

        controller.renderBlurPass();
        assertTrue(rendered.get(), "Adapter renderBlur should be executed");
        assertTrue(renderedAlpha.get() > 0.0f);
    }

    @Test
    void testSafeFallbackOnUnsupportedOrException() {
        // Unsupported adapter
        IMotionBlurRendererAdapter unsupportedAdapter = new IMotionBlurRendererAdapter() {
            @Override
            public boolean isSupported() {
                return false;
            }

            @Override
            public void onResolutionChanged(int width, int height) {}

            @Override
            public void renderBlur(float blurAlpha) {}

            @Override
            public void clearHistory() {}

            @Override
            public void cleanup() {}
        };

        controller.setAdapter(unsupportedAdapter);
        controller.updateCameraRotation(0.0f, 0.0f);
        controller.updateCameraRotation(30.0f, 0.0f);
        controller.renderBlurPass();
        assertTrue(controller.isFallbackEngaged(), "Unsupported adapter must engage fallback without throwing");

        // Crashing adapter
        IMotionBlurRendererAdapter crashingAdapter = new IMotionBlurRendererAdapter() {
            @Override
            public boolean isSupported() {
                return true;
            }

            @Override
            public void onResolutionChanged(int width, int height) {}

            @Override
            public void renderBlur(float blurAlpha) {
                throw new RuntimeException("GL FBO context error");
            }

            @Override
            public void clearHistory() {}

            @Override
            public void cleanup() {}
        };

        controller.setAdapter(crashingAdapter);
        controller.renderBlurPass();
        assertTrue(controller.isFallbackEngaged(), "Exception inside adapter must engage fallback safely");
    }
}
