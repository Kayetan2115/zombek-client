package pl.zombek.client.core.hud.saturation;

import java.util.Objects;

/**
 * Registry holding the platform data provider for Saturation, Hunger, and Exhaustion.
 */
public final class SaturationDataProviderRegistry {

    private static final SaturationDataProviderRegistry INSTANCE = new SaturationDataProviderRegistry();

    private ISaturationDataProvider provider = new FallbackSaturationDataProvider();

    private SaturationDataProviderRegistry() {}

    public static SaturationDataProviderRegistry getInstance() {
        return INSTANCE;
    }

    public ISaturationDataProvider getProvider() {
        return provider;
    }

    public void registerProvider(ISaturationDataProvider provider) {
        this.provider = Objects.requireNonNull(provider, "Saturation provider cannot be null");
    }

    public void reset() {
        this.provider = new FallbackSaturationDataProvider();
    }

    public void resetToDefault() {
        reset();
    }
}
