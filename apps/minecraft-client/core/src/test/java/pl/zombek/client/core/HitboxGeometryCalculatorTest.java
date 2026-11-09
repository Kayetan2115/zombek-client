package pl.zombek.client.core;

import org.junit.jupiter.api.Test;
import pl.zombek.client.core.data.HitboxEntityData;
import pl.zombek.client.core.data.IHitboxRenderer;
import pl.zombek.client.core.data.TargetEntityType;
import pl.zombek.client.core.modules.visual.HitboxGeometryCalculator;
import pl.zombek.client.core.modules.visual.HitboxRenderStyle;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HitboxGeometryCalculatorTest {

    static class RecordingHitboxRenderer implements IHitboxRenderer {
        int linesDrawn = 0;
        int quadsDrawn = 0;
        final List<String> labels = new ArrayList<>();

        @Override
        public void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, int colorArgb, float lineWidth) {
            linesDrawn++;
        }

        @Override
        public void drawQuad(double x1, double y1, double z1, double x2, double y2, double z2,
                             double x3, double y3, double z3, double x4, double y4, double z4, int colorArgb) {
            quadsDrawn++;
        }

        @Override
        public void drawLabel(double x, double y, double z, String text, int colorArgb) {
            labels.add(text);
        }
    }

    @Test
    void testRenderOutlineGenerates12Edges() {
        RecordingHitboxRenderer renderer = new RecordingHitboxRenderer();
        HitboxEntityData data = new HitboxEntityData(
                1, "TestZombie", TargetEntityType.HOSTILE,
                10.0, 64.0, 10.0,
                10.6, 65.8, 10.6,
                65.4, 0.0, 0.0, 1.0,
                5.0, false, false, false, false
        );

        HitboxGeometryCalculator.renderEntityHitbox(
                renderer,
                data,
                10.0, 64.0, 10.0, // Camera at (10, 64, 10)
                HitboxRenderStyle.OUTLINE,
                0xFFFF0000,
                0x40FF0000,
                1.5f,
                false, // eye pos
                false, // look dir
                false, false, false // labels
        );

        assertEquals(12, renderer.linesDrawn, "Standard outline must render 12 box edges");
        assertEquals(0, renderer.quadsDrawn);
    }

    @Test
    void testRenderFilledGenerates6Quads() {
        RecordingHitboxRenderer renderer = new RecordingHitboxRenderer();
        HitboxEntityData data = new HitboxEntityData(
                2, "Pig", TargetEntityType.PASSIVE,
                0.0, 0.0, 0.0,
                1.0, 1.0, 1.0,
                0.8, 1.0, 0.0, 0.0,
                3.0, false, false, false, false
        );

        HitboxGeometryCalculator.renderEntityHitbox(
                renderer,
                data,
                0.0, 0.0, 0.0,
                HitboxRenderStyle.FILLED,
                0xFF00FF00,
                0x4000FF00,
                1.5f,
                false, false, false, false, false
        );

        assertEquals(0, renderer.linesDrawn);
        assertEquals(6, renderer.quadsDrawn, "Filled cuboid must render 6 faces");
    }

    @Test
    void testRenderOutlineAndFilledCombinesBoth() {
        RecordingHitboxRenderer renderer = new RecordingHitboxRenderer();
        HitboxEntityData data = new HitboxEntityData(
                3, "Player", TargetEntityType.PLAYER,
                0.0, 0.0, 0.0,
                0.6, 1.8, 0.6,
                1.62, 0.0, 0.0, 1.0,
                2.0, false, false, false, false
        );

        HitboxGeometryCalculator.renderEntityHitbox(
                renderer,
                data,
                0.0, 0.0, 0.0,
                HitboxRenderStyle.OUTLINE_AND_FILLED,
                0xFF00FF00,
                0x4000FF00,
                1.5f,
                true,  // +4 lines for eye level
                true,  // +1 line for look direction
                true, true, true // label
        );

        // 12 edges + 4 eye square edges + 1 look vector = 17 lines
        assertEquals(17, renderer.linesDrawn);
        assertEquals(6, renderer.quadsDrawn);
        assertEquals(1, renderer.labels.size());
        assertTrue(renderer.labels.get(0).contains("Player"));
        assertTrue(renderer.labels.get(0).contains("[#3]"));
    }
}
