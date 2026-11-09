package pl.zombek.client.core.playerinfo;

import java.util.List;
import java.util.UUID;

/**
 * Platform adapter interface supplying client-known player and entity information.
 */
public interface IPlayerInfoDataProvider {

    /**
     * Returns all player info entries available in the client's current session (tablist / connection).
     */
    List<PlayerInfoData> getConnectedPlayers();

    /**
     * Looks up information for a specific player by UUID or username.
     */
    PlayerInfoData getPlayerInfo(UUID uuid, String username);

    /**
     * Extracts NameTag and display data for an entity visible to the client.
     */
    PlayerInfoData getEntityInfo(Object entityHandle);

    /**
     * Returns true if client is connected to a remote server or LAN world.
     */
    boolean isMultiplayer();

    /**
     * Returns header text sent by server for player list (if available).
     */
    String getServerHeader();

    /**
     * Returns footer text sent by server for player list (if available).
     */
    String getServerFooter();

    /**
     * Checks if the client's player list key (Tab) is currently held down.
     */
    boolean isTabListKeyPressed();
}
