package pl.zombek.client.core.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry holding the active platform provider for player inventory data.
 */
public final class InventoryDataProviderRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryDataProviderRegistry.class);
    private static final InventoryDataProviderRegistry INSTANCE = new InventoryDataProviderRegistry();

    private IInventoryDataProvider provider = new FallbackInventoryDataProvider();

    private InventoryDataProviderRegistry() {}

    public static InventoryDataProviderRegistry getInstance() {
        return INSTANCE;
    }

    public synchronized void registerProvider(IInventoryDataProvider provider) {
        if (provider != null) {
            this.provider = provider;
            LOGGER.info("[InventoryDataProviderRegistry] Registered provider: {}", provider.getClass().getName());
        }
    }

    public synchronized IInventoryDataProvider getProvider() {
        return provider;
    }

    public synchronized void resetForTesting() {
        this.provider = new FallbackInventoryDataProvider();
    }
}
