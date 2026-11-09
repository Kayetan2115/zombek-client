package pl.zombek.client.core.info;

/**
 * Universal data provider contract providing live player coordinates,
 * rotation, dimension, FPS, and network latency across all loader platforms.
 */
public interface IInfoHudDataProvider {

    /**
     * Checks if a player entity is active in an open game world.
     */
    boolean isPlayerInGame();

    /**
     * Absolute player X coordinate.
     */
    double getPlayerX();

    /**
     * Absolute player Y coordinate.
     */
    double getPlayerY();

    /**
     * Absolute player Z coordinate.
     */
    double getPlayerZ();

    /**
     * Player horizontal view angle in degrees (yaw).
     */
    float getPlayerYaw();

    /**
     * Player vertical view angle in degrees (pitch).
     */
    float getPlayerPitch();

    /**
     * Identifier of the current dimension (e.g. "minecraft:overworld").
     */
    String getDimensionId();

    /**
     * Classified dimension type.
     */
    default DimensionType getDimensionType() {
        return DimensionType.fromString(getDimensionId());
    }

    /**
     * Current game rendering frames per second.
     */
    int getCurrentFps();

    /**
     * Network latency in milliseconds.
     * Returns -1 if playing in Singleplayer or latency is unavailable.
     */
    int getPingMs();

    /**
     * Checks whether the client is currently running in Singleplayer.
     */
    boolean isSingleplayer();
}
