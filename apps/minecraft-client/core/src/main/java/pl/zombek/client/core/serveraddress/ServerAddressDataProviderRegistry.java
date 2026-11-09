package pl.zombek.client.core.serveraddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry holding the active platform provider for server address queries.
 */
public final class ServerAddressDataProviderRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerAddressDataProviderRegistry.class);
    private static final ServerAddressDataProviderRegistry INSTANCE = new ServerAddressDataProviderRegistry();

    private IServerAddressDataProvider provider = new FallbackServerAddressDataProvider();

    private ServerAddressDataProviderRegistry() {}

    public static ServerAddressDataProviderRegistry getInstance() {
        return INSTANCE;
    }

    public synchronized void registerProvider(IServerAddressDataProvider provider) {
        if (provider != null) {
            this.provider = provider;
            LOGGER.info("[ServerAddressDataProviderRegistry] Registered provider: {}", provider.getClass().getName());
        }
    }

    public synchronized IServerAddressDataProvider getProvider() {
        return provider;
    }

    public synchronized void resetForTesting() {
        this.provider = new FallbackServerAddressDataProvider();
    }
}
