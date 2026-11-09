package pl.zombek.client.core.hud.saturation;

/**
 * Platform provider interface for querying player food, saturation, and exhaustion levels.
 */
public interface ISaturationDataProvider {

    /**
     * Checks if the player is in an active game session.
     */
    boolean isPlayerAvailable();

    /**
     * Current player food level (0 to 20).
     */
    int getFoodLevel();

    /**
     * Checks if saturation level is accessible on this client environment.
     * In standard vanilla multiplayer, the server does not synchronize saturation to the client
     * unless custom mod networking or singleplayer is present.
     * When false, the client MUST NOT fabricate or guess values.
     */
    boolean isSaturationAvailable();

    /**
     * Player food saturation level (0.0 to 20.0).
     * Valid only if isSaturationAvailable() returns true.
     */
    float getSaturationLevel();

    /**
     * Checks if exhaustion level is accessible on this client.
     */
    boolean isExhaustionAvailable();

    /**
     * Player exhaustion level (0.0 to 4.0).
     */
    float getExhaustionLevel();
}
