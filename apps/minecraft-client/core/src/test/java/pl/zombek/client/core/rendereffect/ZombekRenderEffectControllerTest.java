package pl.zombek.client.core.rendereffect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.rendereffect.color.ColorGradingController;
import pl.zombek.client.core.rendereffect.fog.FogVisualController;
import pl.zombek.client.core.rendereffect.glint.GlintRenderController;
import pl.zombek.client.core.rendereffect.particle.ParticleRenderController;

import static org.junit.jupiter.api.Assertions.*;

public class ZombekRenderEffectControllerTest {

    private ZombekRenderEffectController controller;

    @BeforeEach
    public void setup() {
        controller = ZombekRenderEffectController.getInstance();
        controller.resetForTesting();
    }

    @Test
    public void testSafeWorldLeaveRestoration() {
        // Activate all 4 subsystems
        FogVisualController.getInstance().setActive(true);
        ColorGradingController.getInstance().setActive(true);
        GlintRenderController.getInstance().setActive(true);
        ParticleRenderController.getInstance().setActive(true);

        assertTrue(FogVisualController.getInstance().isActive());
        assertTrue(ColorGradingController.getInstance().isActive());
        assertTrue(GlintRenderController.getInstance().isActive());
        assertTrue(ParticleRenderController.getInstance().isActive());

        // Call onWorldLeave
        controller.onWorldLeave();

        // All should be safely restored/reset
        assertFalse(ColorGradingController.getInstance().isActive());
        assertFalse(GlintRenderController.getInstance().isActive());
        assertFalse(ParticleRenderController.getInstance().isActive());
    }

    @Test
    public void testSnapshotIntegrity() {
        RenderEffectSnapshot snapshot = controller.getSnapshot();
        assertNotNull(snapshot);
        assertNotNull(snapshot.getFogParameters());
        assertNotNull(snapshot.getColorSettings());
        assertNotNull(snapshot.getGlintSettings());
        assertNotNull(snapshot.getParticleSettings());
    }

    @Test
    public void testPipelineLayerPriorityOrdering() {
        assertTrue(RenderEffectLayer.WORLD_FOG.getPriority() < RenderEffectLayer.POST_PROCESSING.getPriority());
        assertTrue(RenderEffectLayer.POST_PROCESSING.getPriority() < RenderEffectLayer.ITEM_RENDERING.getPriority());
        assertTrue(RenderEffectLayer.ITEM_RENDERING.getPriority() < RenderEffectLayer.PARTICLE_RENDERING.getPriority());
    }
}
