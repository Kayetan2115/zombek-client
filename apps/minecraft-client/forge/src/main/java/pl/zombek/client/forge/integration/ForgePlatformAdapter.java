package pl.zombek.client.forge.integration;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import pl.zombek.client.core.platform.IPlatformAdapter;
import pl.zombek.client.forge.config.ForgeConfigHelper;
import pl.zombek.client.forge.gui.ZombekForgeScreen;

import java.nio.file.Path;

/**
 * Forge platform adapter providing loader context to ZombekClient Core.
 */
public class ForgePlatformAdapter implements IPlatformAdapter {

    @Override
    public String getLoaderName() {
        return "forge";
    }

    @Override
    public String getLoaderVersion() {
        try {
            return ModList.get().getModContainerById("forge")
                    .map(c -> c.getModInfo().getVersion().toString())
                    .orElse("49.2.13");
        } catch (Throwable t) {
            return "49.2.13";
        }
    }

    @Override
    public String getMinecraftVersion() {
        try {
            return ModList.get().getModContainerById("minecraft")
                    .map(c -> c.getModInfo().getVersion().toString())
                    .orElse("1.20.4");
        } catch (Throwable t) {
            return "1.20.4";
        }
    }

    @Override
    public Path getGameDirectory() {
        return ForgeConfigHelper.getGameDir();
    }

    @Override
    public Path getConfigDirectory() {
        return ForgeConfigHelper.getConfigDir();
    }

    @Override
    public boolean isClient() {
        return FMLEnvironment.dist.isClient();
    }

    @Override
    public void openMenu() {
        Minecraft client = Minecraft.getInstance();
        if (client != null) {
            client.tell(() -> client.setScreen(new ZombekForgeScreen()));
        }
    }
}
