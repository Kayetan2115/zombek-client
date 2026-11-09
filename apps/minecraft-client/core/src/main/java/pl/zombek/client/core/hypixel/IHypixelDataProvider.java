package pl.zombek.client.core.hypixel;

/**
 * Platform abstraction contract providing Minecraft client connection metadata,
 * clipboard access, and audio feedback for Hypixel Mods.
 */
public interface IHypixelDataProvider {
    /**
     * Gets the current multiplayer server address (host:port or domain), or null if in singleplayer/menu.
     */
    String getCurrentServerAddress();

    /**
     * Checks if the client is currently running in a local singleplayer world.
     */
    boolean isSingleplayer();

    /**
     * Copies the provided text string into the operating system clipboard.
     */
    void copyToClipboard(String text);

    /**
     * Plays a local client sound effect.
     */
    void playSound(String soundId, float volume, float pitch);

    /**
     * Checks if the local player is currently active in a game world.
     */
    boolean isPlayerInGame();
}
