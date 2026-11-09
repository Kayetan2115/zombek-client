package pl.zombek.client.core.hud.potions;

import java.util.Objects;

/**
 * Central registry holding the platform data provider for Potion Effects.
 */
public final class PotionEffectsDataProviderRegistry {

    private static final PotionEffectsDataProviderRegistry INSTANCE = new PotionEffectsDataProviderRegistry();

    private IPotionEffectsDataProvider provider = new FallbackPotionEffectsDataProvider();

    private PotionEffectsDataProviderRegistry() {}

    public static PotionEffectsDataProviderRegistry getInstance() {
        return INSTANCE;
    }

    public IPotionEffectsDataProvider getProvider() {
        return provider;
    }

    public void registerProvider(IPotionEffectsDataProvider provider) {
        this.provider = Objects.requireNonNull(provider, "Potion effects provider cannot be null");
    }

    public void reset() {
        this.provider = new FallbackPotionEffectsDataProvider();
    }

    public void resetToDefault() {
        reset();
    }
}
