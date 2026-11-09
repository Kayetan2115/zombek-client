package pl.zombek.client.core.time;

/**
 * Universal platform contract providing game world time, total age,
 * session metadata, and statistics across Fabric, Forge, NeoForge, and Quilt.
 */
public interface ITimeDataProvider {

    /**
     * Checks if the client is currently in an active game world or connected to a server.
     */
    boolean isWorldLoaded();

    /**
     * Time of day in Minecraft ticks [0..24000+).
     * Returns -1 if unavailable.
     */
    long getWorldTimeOfDay();

    /**
     * Total world age / game time in Minecraft ticks (continuously increments).
     * Returns -1 if unavailable.
     */
    long getWorldGameTime();

    /**
     * Indicates whether time information is legitimately reported by the world/server.
     */
    boolean isTimeAvailable();

    /**
     * Checks whether the client is playing on an integrated Singleplayer server.
     */
    boolean isSingleplayer();

    /**
     * Local world save name if available, or null in multiplayer.
     */
    String getWorldName();

    /**
     * Current multiplayer server address (host:port) if connected, or null in singleplayer.
     */
    String getServerAddress();

    /**
     * Total world playtime in ticks from the local player's statistics in Singleplayer.
     * Returns -1 if playing on multiplayer or statistics are not loaded.
     */
    long getSingleplayerPlaytimeTicks();

    /**
     * Identifier of the current dimension (e.g. "minecraft:overworld").
     */
    String getDimensionId();
}
