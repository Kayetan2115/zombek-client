package pl.zombek.client.core.hypixel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry holding the active platform provider instance for Hypixel metadata.
 */
public final class HypixelDataProviderRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(HypixelDataProviderRegistry.class);

    private static final IHypixelDataProvider DEFAULT_PROVIDER = new IHypixelDataProvider() {
        @Override
        public String getCurrentServerAddress() {
            return null;
        }

        @Override
        public boolean isSingleplayer() {
            return false;
        }

        @Override
        public void copyToClipboard(String text) {
            // Headless fallback
            LOGGER.debug("[HypixelDataProvider] Mock copyToClipboard: {}", text);
        }

        @Override
        public void playSound(String soundId, float volume, float pitch) {
            // Headless fallback
        }

        @Override
        public boolean isPlayerInGame() {
            return false;
        }
    };

    private static IHypixelDataProvider activeProvider = DEFAULT_PROVIDER;

    private HypixelDataProviderRegistry() {}

    public static synchronized void setProvider(IHypixelDataProvider provider) {
        activeProvider = provider != null ? provider : DEFAULT_PROVIDER;
    }

    public static synchronized IHypixelDataProvider getProvider() {
        return activeProvider;
    }

    public static synchronized void reset() {
        activeProvider = DEFAULT_PROVIDER;
    }
}
