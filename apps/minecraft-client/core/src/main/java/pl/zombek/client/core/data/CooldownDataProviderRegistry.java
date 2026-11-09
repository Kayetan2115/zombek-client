package pl.zombek.client.core.data;

import java.util.Collections;
import java.util.List;

/**
 * Registry holding the active ICooldownDataProvider instance.
 * Defaults to an empty list for headless/test environments.
 */
public class CooldownDataProviderRegistry {

    private static final ICooldownDataProvider DEFAULT_PROVIDER = Collections::emptyList;

    private static volatile ICooldownDataProvider provider = DEFAULT_PROVIDER;

    public static ICooldownDataProvider getProvider() {
        return provider;
    }

    public static void setProvider(ICooldownDataProvider newProvider) {
        provider = (newProvider != null) ? newProvider : DEFAULT_PROVIDER;
    }

    public static void reset() {
        provider = DEFAULT_PROVIDER;
    }
}
