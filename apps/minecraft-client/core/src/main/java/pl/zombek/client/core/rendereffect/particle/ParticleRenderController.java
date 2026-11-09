package pl.zombek.client.core.rendereffect.particle;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller regulating particle emission density, scale, tints, and performance limits.
 */
public class ParticleRenderController {
    private static final ParticleRenderController INSTANCE = new ParticleRenderController();

    private boolean active = false;
    private ParticleVisualSettings settings = ParticleVisualSettings.vanilla();
    private final AtomicInteger activeParticleCount = new AtomicInteger(0);
    private IParticleRenderAdapter adapter;

    private ParticleRenderController() {
    }

    public static ParticleRenderController getInstance() {
        return INSTANCE;
    }

    public void setAdapter(IParticleRenderAdapter adapter) {
        this.adapter = adapter;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        if (!active) {
            reset();
        }
    }

    public void updateSettings(ParticleVisualSettings newSettings) {
        this.settings = newSettings != null ? newSettings : ParticleVisualSettings.vanilla();
        if (adapter != null) {
            adapter.onParticleSettingsUpdated(this.settings);
        }
    }

    /**
     * Determines whether a particle spawn should proceed based on performance limits,
     * distance cutoff, category density, and granular particle rules.
     */
    public boolean shouldSpawnParticle(
            String particleId,
            double x, double y, double z,
            double playerX, double playerY, double playerZ
    ) {
        if (!active) {
            return true;
        }

        // 1. Performance hard cap
        if (activeParticleCount.get() >= settings.getMaxParticleLimit()) {
            return false;
        }

        // 2. Distance cutoff
        double dx = x - playerX;
        double dy = y - playerY;
        double dz = z - playerZ;
        double distSq = dx * dx + dy * dy + dz * dz;
        float maxDist = settings.getMaxDistanceCutoff();
        if (distSq > (maxDist * maxDist)) {
            return false;
        }

        // 3. Custom rule check
        CustomParticleRule rule = settings.getRule(particleId);
        if (rule != null) {
            if (!rule.isEnabled()) {
                return false;
            }
            float density = rule.getDensityMultiplier();
            if (density <= 0.0f) return false;
            if (density < 1.0f) {
                return ThreadLocalRandom.current().nextFloat() <= density;
            }
            return true;
        }

        // 4. Category-based density check
        ParticleCategory category = ParticleCategoryClassifier.classify(particleId);
        float effectiveDensity = settings.getGlobalDensity() * settings.getCategoryMultiplier(category);

        if (effectiveDensity <= 0.0f) {
            return false;
        }
        if (effectiveDensity < 1.0f) {
            return ThreadLocalRandom.current().nextFloat() <= effectiveDensity;
        }

        return true;
    }

    public float resolveScaleMultiplier(String particleId) {
        if (!active) {
            return 1.0f;
        }
        CustomParticleRule rule = settings.getRule(particleId);
        if (rule != null) {
            return rule.getScaleMultiplier();
        }
        return settings.getGlobalScale();
    }

    public int resolveParticleColor(String particleId, int originalColor) {
        if (!active) {
            return originalColor;
        }
        CustomParticleRule rule = settings.getRule(particleId);
        if (rule != null && rule.isCustomColor()) {
            int alpha = originalColor & 0xFF000000;
            return alpha | (rule.getColorRgb() & 0x00FFFFFF);
        }
        return originalColor;
    }

    public float resolveOpacity(String particleId, float originalOpacity) {
        if (!active) {
            return originalOpacity;
        }
        CustomParticleRule rule = settings.getRule(particleId);
        if (rule != null) {
            return originalOpacity * rule.getOpacity();
        }
        return originalOpacity * settings.getGlobalOpacity();
    }

    public void onParticleSpawned() {
        activeParticleCount.incrementAndGet();
    }

    public void onParticleRemoved() {
        activeParticleCount.updateAndGet(curr -> Math.max(0, curr - 1));
    }

    public void tick() {
        // Sanity clamp: slowly decay active counter if particles deallocated without callback
        if (activeParticleCount.get() > 0 && ThreadLocalRandom.current().nextInt(100) == 0) {
            activeParticleCount.updateAndGet(curr -> Math.max(0, (int) (curr * 0.95)));
        }
    }

    public void reset() {
        this.active = false;
        this.activeParticleCount.set(0);
        this.settings = ParticleVisualSettings.vanilla();
        if (adapter != null) {
            adapter.onParticleSettingsUpdated(this.settings);
        }
    }

    public int getActiveParticleCount() {
        return activeParticleCount.get();
    }

    public ParticleVisualSettings getSettings() {
        return settings;
    }
}
