package pl.zombek.client.core.hud.bossbar;

import java.util.Objects;

/**
 * Registry holding the platform data provider for Boss Bars.
 */
public final class BossBarDataProviderRegistry {

    private static final BossBarDataProviderRegistry INSTANCE = new BossBarDataProviderRegistry();

    private IBossBarDataProvider provider = new FallbackBossBarDataProvider();

    private BossBarDataProviderRegistry() {}

    public static BossBarDataProviderRegistry getInstance() {
        return INSTANCE;
    }

    public IBossBarDataProvider getProvider() {
        return provider;
    }

    public void registerProvider(IBossBarDataProvider provider) {
        this.provider = Objects.requireNonNull(provider, "Boss bar provider cannot be null");
    }

    public void reset() {
        this.provider = new FallbackBossBarDataProvider();
    }

    public void resetToDefault() {
        reset();
    }
}
