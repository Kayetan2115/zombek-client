package pl.zombek.client.core.hud.potions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Fallback provider used in tests or when no player is active.
 * Supplies realistic sample data when preview mode is toggled.
 */
public class FallbackPotionEffectsDataProvider implements IPotionEffectsDataProvider {

    private boolean previewMode = false;
    private List<StatusEffectData> customEffects = null;

    public void setPreviewMode(boolean previewMode) {
        this.previewMode = previewMode;
    }

    public void setActiveEffects(List<StatusEffectData> effects) {
        this.customEffects = effects;
    }

    @Override
    public boolean isPlayerAvailable() {
        return (customEffects != null && !customEffects.isEmpty()) || previewMode;
    }

    @Override
    public List<StatusEffectData> getActiveEffects() {
        if (customEffects != null) {
            return customEffects;
        }
        if (!previewMode) {
            return Collections.emptyList();
        }

        return Arrays.asList(
                new StatusEffectData("minecraft:speed", "Szybkość", 1, 1720, false, false, StatusEffectCategory.BENEFICIAL, 0xFF7CAFC6),
                new StatusEffectData("minecraft:strength", "Siła", 0, 840, false, false, StatusEffectCategory.BENEFICIAL, 0xFF932423),
                new StatusEffectData("minecraft:regeneration", "Regeneracja", 0, 160, false, false, StatusEffectCategory.BENEFICIAL, 0xFFCD5CAB)
        );
    }
}
