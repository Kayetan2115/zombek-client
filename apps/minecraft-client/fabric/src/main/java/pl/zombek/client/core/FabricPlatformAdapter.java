package pl.zombek.client.core;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import pl.zombek.client.core.platform.IPlatformAdapter;
import pl.zombek.client.gui.ZombekMenuScreen;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Fabric-specific adapter implementing IPlatformAdapter for ZombekClient Core.
 */
public class FabricPlatformAdapter implements IPlatformAdapter {

    @Override
    public String getLoaderName() {
        return "fabric";
    }

    @Override
    public String getLoaderVersion() {
        try {
            return FabricLoader.getInstance().getModContainer("fabricloader")
                    .map(c -> c.getMetadata().getVersion().getFriendlyString())
                    .orElse("0.15.11");
        } catch (Throwable t) {
            return "0.15.11";
        }
    }

    @Override
    public String getMinecraftVersion() {
        try {
            return FabricLoader.getInstance().getModContainer("minecraft")
                    .map(c -> c.getMetadata().getVersion().getFriendlyString())
                    .orElse("1.20.4");
        } catch (Throwable t) {
            return "1.20.4";
        }
    }

    @Override
    public Path getGameDirectory() {
        try {
            return FabricLoader.getInstance().getGameDir();
        } catch (Throwable t) {
            return Paths.get(".");
        }
    }

    @Override
    public Path getConfigDirectory() {
        return getGameDirectory().resolve("config").resolve("zombekclient");
    }

    @Override
    public boolean isClient() {
        return true;
    }

    @Override
    public void openMenu() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.send(() -> client.setScreen(new ZombekMenuScreen()));
        }
    }
}
