package pl.zombek.client.quilt.platform;

import net.minecraft.client.MinecraftClient;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.platform.IPlatformAdapter;
import pl.zombek.client.quilt.config.QuiltConfigHelper;
import pl.zombek.client.quilt.gui.ZombekQuiltScreen;
import pl.zombek.client.quilt.input.QuiltKeybindManager;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Platform adapter implementation for Quilt Loader connecting ZombekClient Core
 * with Minecraft Java Edition 1.20.4 running under Quilt.
 */
public class QuiltPlatformAdapter implements IPlatformAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuiltPlatformAdapter.class);
    private final ModContainer modContainer;

    public QuiltPlatformAdapter() {
        this(null);
    }

    public QuiltPlatformAdapter(ModContainer modContainer) {
        this.modContainer = modContainer;
    }

    @Override
    public String getLoaderName() {
        return "quilt";
    }

    @Override
    public String getLoaderVersion() {
        try {
            return QuiltLoader.getModContainer("quilt_loader")
                    .map(m -> m.metadata().version().raw())
                    .orElse("0.27.1-beta.1");
        } catch (Throwable t) {
            return "0.27.1-beta.1";
        }
    }

    @Override
    public String getMinecraftVersion() {
        try {
            return QuiltLoader.getModContainer("minecraft")
                    .map(m -> m.metadata().version().raw())
                    .orElse("1.20.4");
        } catch (Throwable t) {
            return "1.20.4";
        }
    }

    @Override
    public Path getGameDirectory() {
        try {
            return QuiltLoader.getGameDir();
        } catch (Throwable t) {
            return Paths.get(".");
        }
    }

    @Override
    public Path getConfigDirectory() {
        return QuiltConfigHelper.getConfigDirectory();
    }

    @Override
    public boolean isClient() {
        return true;
    }

    @Override
    public void openMenu() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.send(() -> client.setScreen(new ZombekQuiltScreen()));
        }
    }

    public void executeOnRenderThread(Runnable action) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.isOnThread()) {
            action.run();
        } else if (client != null) {
            client.send(action);
        } else {
            action.run();
        }
    }

    /**
     * Callback invoked from MinecraftClientMixin after client is ready.
     */
    public static void onClientReady(MinecraftClient client) {
        LOGGER.info("[ZombekClient-Quilt] MinecraftClient initialized, registering key mappings...");
        QuiltKeybindManager.getInstance().registerKeyMappings(client);
    }
}
