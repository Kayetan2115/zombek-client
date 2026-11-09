package pl.zombek.client.forge.config;

import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helper resolving configuration directories and paths in the Forge runtime.
 */
public final class ForgeConfigHelper {

    private ForgeConfigHelper() {}

    public static Path getConfigDir() {
        try {
            return FMLPaths.CONFIGDIR.get().resolve("zombekclient");
        } catch (Throwable t) {
            return Paths.get("config", "zombekclient");
        }
    }

    public static Path getGameDir() {
        try {
            return FMLPaths.GAMEDIR.get();
        } catch (Throwable t) {
            return Paths.get(".");
        }
    }
}
