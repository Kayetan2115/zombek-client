package pl.zombek.client.neoforge.config;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * NeoForge configuration directory helper ensuring clean instance isolation.
 */
public final class NeoForgeConfigHelper {
    private NeoForgeConfigHelper() {}

    /**
     * Resolves the primary directory where ZombekClient configuration files are stored.
     */
    public static Path getConfigDir() {
        try {
            Path dir = FMLPaths.CONFIGDIR.get().resolve("zombekclient");
            if (!Files.exists(dir)) {
                try {
                    Files.createDirectories(dir);
                } catch (IOException ignored) {}
            }
            return dir;
        } catch (Throwable t) {
            return Paths.get("config", "zombekclient");
        }
    }

    /**
     * Resolves the root game directory in NeoForge.
     */
    public static Path getGameDir() {
        try {
            return FMLPaths.GAMEDIR.get();
        } catch (Throwable t) {
            return Paths.get(".");
        }
    }
}
