package pl.zombek.client.core.uhc;

/**
 * Registry holding the platform-specific IUhcDataProvider.
 */
public class UhcDataProviderRegistry {
    private static IUhcDataProvider provider = new DefaultUhcDataProvider();

    public static synchronized void setProvider(IUhcDataProvider newProvider) {
        if (newProvider != null) {
            provider = newProvider;
        }
    }

    public static synchronized IUhcDataProvider getProvider() {
        return provider;
    }

    private static class DefaultUhcDataProvider implements IUhcDataProvider {
        @Override
        public boolean isPlayerInGame() { return false; }
        @Override
        public float getPlayerHealth() { return 20.0f; }
        @Override
        public float getPlayerMaxHealth() { return 20.0f; }
        @Override
        public float getPlayerAbsorption() { return 0.0f; }
        @Override
        public int getPlayerFoodLevel() { return 20; }
        @Override
        public float getPlayerSaturation() { return 5.0f; }
        @Override
        public int getItemCount(UhcTrackedItem item) { return 0; }
    }
}
