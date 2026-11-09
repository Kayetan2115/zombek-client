package pl.zombek.client.core.bedwars;

import java.util.Collections;
import java.util.List;

/**
 * Interface providing platform-specific client data to the Bedwars module.
 */
public interface IBedwarsDataProvider {
    boolean isPlayerInGame();
    String getPlayerName();
    String getPlayerUuid();
    List<String> getTabListPlayerNames();
    BedwarsTeamColor getPlayerTeamColor();

    default List<String> getScoreboardLines() {
        return Collections.emptyList();
    }
}
