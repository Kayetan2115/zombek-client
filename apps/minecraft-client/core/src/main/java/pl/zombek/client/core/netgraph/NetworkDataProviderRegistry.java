package pl.zombek.client.core.netgraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry holding the active platform provider supplying network statistics.
 */
public final class NetworkDataProviderRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkDataProviderRegistry.class);
    private static final NetworkDataProviderRegistry INSTANCE = new NetworkDataProviderRegistry();

    private INetworkDataProvider provider = new FallbackNetworkDataProvider();

    private NetworkDataProviderRegistry() {}

    public static NetworkDataProviderRegistry getInstance() {
        return INSTANCE;
    }

    public synchronized void registerProvider(INetworkDataProvider provider) {
        if (provider != null) {
            this.provider = provider;
            LOGGER.info("[NetworkDataProviderRegistry] Registered provider: {}", provider.getClass().getName());
        }
    }

    public synchronized INetworkDataProvider getProvider() {
        return provider;
    }

    public synchronized void resetForTesting() {
        this.provider = new FallbackNetworkDataProvider();
    }
}
