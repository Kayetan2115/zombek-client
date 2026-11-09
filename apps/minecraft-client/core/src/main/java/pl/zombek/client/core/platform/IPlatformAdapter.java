package pl.zombek.client.core.platform;

import java.nio.file.Path;

/**
 * Platform abstraction decoupling common client logic from specific mod loaders (Fabric, Forge, NeoForge, Quilt).
 */
public interface IPlatformAdapter {
    /**
     * Identifier of the active loader (e.g. "fabric", "forge").
     */
    String getLoaderName();

    /**
     * Exact version string of the active loader.
     */
    String getLoaderVersion();

    /**
     * Minecraft version (e.g. "1.20.4").
     */
    String getMinecraftVersion();

    /**
     * Root game directory (e.g. .minecraft or instance directory).
     */
    Path getGameDirectory();

    /**
     * Mod configuration directory (typically <gameDir>/config/zombekclient).
     */
    Path getConfigDirectory();

    /**
     * True if running on a physical client environment.
     */
    boolean isClient();

    /**
     * Opens the in-game ZombekClient configuration screen on the client thread.
     */
    void openMenu();
}
