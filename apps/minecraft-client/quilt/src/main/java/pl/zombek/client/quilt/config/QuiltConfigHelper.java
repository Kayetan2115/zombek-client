package pl.zombek.client.quilt.config;

import org.quiltmc.loader.api.QuiltLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolves the configuration directory for ZombekClient on Quilt Loader.
 */
public final class QuiltConfigHelper {
    private static Path configDirOverride = null;

    private QuiltConfigHelper() {}

    public static Path getConfigDirectory() {
        if (configDirOverride != null) {
            return configDirOverride;
        }

        try {
            return QuiltLoader.getConfigDir().resolve("zombekclient");
        } catch (Throwable ignored) {
            return Paths.get("config").resolve("zombekclient");
        }
    }

    public static void setConfigDirectoryOverride(Path path) {
        configDirOverride = path;
    }
}
