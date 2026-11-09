package pl.zombek.client.core.playerinfo;

/**
 * Registry holding active platform provider for player and entity information.
 */
public final class PlayerInfoDataProviderRegistry {

    private static final PlayerInfoDataProviderRegistry INSTANCE = new PlayerInfoDataProviderRegistry();

    private IPlayerInfoDataProvider provider = new FallbackPlayerInfoDataProvider();

    private PlayerInfoDataProviderRegistry() {}

    public static PlayerInfoDataProviderRegistry getInstance() {
        return INSTANCE;
    }

    public synchronized void registerProvider(IPlayerInfoDataProvider provider) {
        if (provider != null) {
            this.provider = provider;
        }
    }

    public synchronized IPlayerInfoDataProvider getProvider() {
        return provider;
    }

    public synchronized void resetForTesting() {
        this.provider = new FallbackPlayerInfoDataProvider();
    }
}
