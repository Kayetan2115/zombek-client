package pl.zombek.client.core.rendereffect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.rendereffect.color.*;

import static org.junit.jupiter.api.Assertions.*;

public class ColorGradingControllerTest {

    private ColorGradingController controller;

    @BeforeEach
    public void setup() {
        controller = ColorGradingController.getInstance();
        controller.reset();
        controller.setActive(true);
    }

    @Test
    public void testMonochromeProfileMath() {
        ColorSettings mono = ColorProfileManager.getSettings(ColorProfile.MONOCHROME);
        float[] out = new float[3];

        // Give a vibrant green input
        ColorAdjustmentPipeline.applyAdjustments(0.2f, 0.8f, 0.1f, mono, out);

        // In monochrome, R == G == B within floating point epsilon
        assertEquals(out[0], out[1], 0.001f, "Red and green should be identical in monochrome");
        assertEquals(out[1], out[2], 0.001f, "Green and blue should be identical in monochrome");
    }

    @Test
    public void testPreserveHudColors() {
        ColorSettings vibrant = ColorProfileManager.getSettings(ColorProfile.VIBRANT);
        controller.applySettings(vibrant, false, 0);

        // When target is HUD and preserveHud is true, transformRgb should return original unaltered colors
        float[] out = new float[3];
        controller.transformRgb(0.2f, 0.5f, 0.9f, TargetType.HUD, out);

        assertEquals(0.2f, out[0], 0.0001f);
        assertEquals(0.5f, out[1], 0.0001f);
        assertEquals(0.9f, out[2], 0.0001f);

        // However, for WORLD target it should be transformed
        float[] worldOut = new float[3];
        controller.transformRgb(0.2f, 0.5f, 0.9f, TargetType.WORLD, worldOut);
        assertNotEquals(0.2f, worldOut[0]);
    }

    @Test
    public void testArgbPacking() {
        ColorSettings mono = ColorProfileManager.getSettings(ColorProfile.MONOCHROME);
        controller.applySettings(mono, false, 0);

        int originalArgb = 0xFF33CC66;
        int transformed = controller.transformPackedArgb(originalArgb, TargetType.WORLD);

        int a = (transformed >> 24) & 0xFF;
        int r = (transformed >> 16) & 0xFF;
        int g = (transformed >> 8) & 0xFF;
        int b = transformed & 0xFF;

        assertEquals(0xFF, a, "Alpha must be preserved");
        assertEquals(r, g, 1, "R and G should match in monochrome");
        assertEquals(g, b, 1, "G and B should match in monochrome");
    }
}
