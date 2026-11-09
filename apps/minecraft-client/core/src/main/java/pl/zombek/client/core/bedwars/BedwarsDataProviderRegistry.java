package pl.zombek.client.core.bedwars;

import java.util.Collections;
import java.util.List;

/**
 * Registry holding the platform-specific IBedwarsDataProvider.
 */
public class BedwarsDataProviderRegistry {
    private static IBedwarsDataProvider provider = new DefaultBedwarsDataProvider();

    public static synchronized void setProvider(IBedwarsDataProvider newProvider) {
        if (newProvider != null) {
            provider = newProvider;
        }
    }

    public static synchronized IBedwarsDataProvider getProvider() {
        return provider;
    }

    private static class DefaultBedwarsDataProvider implements IBedwarsDataProvider {
        @Override
        public boolean isPlayerInGame() {
            return false;
        }

        @Override
        public String getPlayerName() {
            return "Player";
        }

        @Override
        public String getPlayerUuid() {
            return "";
        }

        @Override
        public List<String> getTabListPlayerNames() {
            return Collections.emptyList();
        }

        @Override
        public BedwarsTeamColor getPlayerTeamColor() {
            return null;
        }
    }
}
