package pl.zombek.client.core.time;

/**
 * Thread-safe singleton registry for the active Minecraft time and world data provider.
 */
public final class TimeDataProviderRegistry {

    private static final TimeDataProviderRegistry INSTANCE = new TimeDataProviderRegistry();
    private volatile ITimeDataProvider provider = new FallbackTimeDataProvider();

    private TimeDataProviderRegistry() {}

    public static TimeDataProviderRegistry getInstance() {
        return INSTANCE;
    }

    public void registerProvider(ITimeDataProvider newProvider) {
        if (newProvider != null) {
            this.provider = newProvider;
        } else {
            this.provider = new FallbackTimeDataProvider();
        }
    }

    public ITimeDataProvider getProvider() {
        return provider;
    }
}
