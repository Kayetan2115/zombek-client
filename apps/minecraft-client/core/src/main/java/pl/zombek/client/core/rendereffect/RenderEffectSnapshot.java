package pl.zombek.client.core.rendereffect;

import pl.zombek.client.core.rendereffect.color.ColorSettings;
import pl.zombek.client.core.rendereffect.fog.FogParameters;
import pl.zombek.client.core.rendereffect.glint.GlintSettings;
import pl.zombek.client.core.rendereffect.particle.ParticleVisualSettings;

/**
 * Diagnostic snapshot of the state of all 4 visual render effect controllers.
 */
public class RenderEffectSnapshot {
    private final boolean fogActive;
    private final FogParameters fogParameters;

    private final boolean colorGradingActive;
    private final ColorSettings colorSettings;

    private final boolean glintActive;
    private final GlintSettings glintSettings;

    private final boolean particleActive;
    private final ParticleVisualSettings particleSettings;
    private final int activeParticleCount;

    public RenderEffectSnapshot(
            boolean fogActive,
            FogParameters fogParameters,
            boolean colorGradingActive,
            ColorSettings colorSettings,
            boolean glintActive,
            GlintSettings glintSettings,
            boolean particleActive,
            ParticleVisualSettings particleSettings,
            int activeParticleCount
    ) {
        this.fogActive = fogActive;
        this.fogParameters = fogParameters;
        this.colorGradingActive = colorGradingActive;
        this.colorSettings = colorSettings;
        this.glintActive = glintActive;
        this.glintSettings = glintSettings;
        this.particleActive = particleActive;
        this.particleSettings = particleSettings;
        this.activeParticleCount = activeParticleCount;
    }

    public boolean isFogActive() {
        return fogActive;
    }

    public FogParameters getFogParameters() {
        return fogParameters;
    }

    public boolean isColorGradingActive() {
        return colorGradingActive;
    }

    public ColorSettings getColorSettings() {
        return colorSettings;
    }

    public boolean isGlintActive() {
        return glintActive;
    }

    public GlintSettings getGlintSettings() {
        return glintSettings;
    }

    public boolean isParticleActive() {
        return particleActive;
    }

    public ParticleVisualSettings getParticleSettings() {
        return particleSettings;
    }

    public int getActiveParticleCount() {
        return activeParticleCount;
    }
}
