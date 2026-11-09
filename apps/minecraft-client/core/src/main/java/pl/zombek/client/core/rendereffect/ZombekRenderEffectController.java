package pl.zombek.client.core.rendereffect;

import pl.zombek.client.core.rendereffect.color.ColorGradingController;
import pl.zombek.client.core.rendereffect.fog.FogVisualController;
import pl.zombek.client.core.rendereffect.glint.GlintRenderController;
import pl.zombek.client.core.rendereffect.particle.ParticleRenderController;

/**
 * Unified central coordinator for client-side visual render effects:
 * Fog Customizer, Color Saturation, Glint Colorizer, and Particle Changer.
 *
 * Enforces deterministic pipeline priority:
 * Vanilla -> Fog Customizer -> World Visual Color Processing -> Color Saturation ->
 * Item Rendering / Glint Colorizer -> Particle Changer -> HUD Rendering.
 */
public class ZombekRenderEffectController {
    private static final ZombekRenderEffectController INSTANCE = new ZombekRenderEffectController();

    private ZombekRenderEffectController() {
    }

    public static ZombekRenderEffectController getInstance() {
        return INSTANCE;
    }

    /**
     * Ticks internal animation curves, transitions, and performance decay across all render effects.
     */
    public void onClientTick() {
        FogVisualController.getInstance().tick();
        ColorGradingController.getInstance().tick();
        ParticleRenderController.getInstance().tick();
    }

    /**
     * Dispatches world change event to reset controllers if configured to do so.
     */
    public void onWorldChange() {
        FogVisualController.getInstance().onWorldChange();
    }

    /**
     * Safely resets all render effects back to standard vanilla behavior upon leaving a world.
     */
    public void onWorldLeave() {
        FogVisualController.getInstance().reset();
        ColorGradingController.getInstance().reset();
        GlintRenderController.getInstance().reset();
        ParticleRenderController.getInstance().reset();
    }

    /**
     * Complete shutdown and cleanup of render effect controllers.
     */
    public void shutdown() {
        onWorldLeave();
    }

    /**
     * Resets all controllers for isolated test runs.
     */
    public void resetForTesting() {
        shutdown();
    }

    /**
     * Returns a consolidated snapshot of active render effect states.
     */
    public RenderEffectSnapshot getSnapshot() {
        return new RenderEffectSnapshot(
                FogVisualController.getInstance().isActive(),
                FogVisualController.getInstance().getCurrentParameters(),
                ColorGradingController.getInstance().isActive(),
                ColorGradingController.getInstance().getCurrentSettings(),
                GlintRenderController.getInstance().isActive(),
                GlintRenderController.getInstance().getSettings(),
                ParticleRenderController.getInstance().isActive(),
                ParticleRenderController.getInstance().getSettings(),
                ParticleRenderController.getInstance().getActiveParticleCount()
        );
    }
}
