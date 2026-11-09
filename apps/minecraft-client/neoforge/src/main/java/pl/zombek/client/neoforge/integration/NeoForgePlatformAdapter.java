package pl.zombek.client.neoforge.integration;

import net.minecraft.client.Minecraft;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import pl.zombek.client.core.platform.IPlatformAdapter;
import pl.zombek.client.neoforge.config.NeoForgeConfigHelper;
import pl.zombek.client.neoforge.gui.ZombekNeoForgeScreen;

import java.nio.file.Path;

/**
 * NeoForge platform adapter providing loader context to ZombekClient Core.
 */
public class NeoForgePlatformAdapter implements IPlatformAdapter {

    @Override
    public String getLoaderName() {
        return "neoforge";
    }

    @Override
    public String getLoaderVersion() {
        try {
            return ModList.get().getModContainerById("neoforge")
                    .map(c -> c.getModInfo().getVersion().toString())
                    .orElse("20.4.237");
        } catch (Throwable t) {
            return "20.4.237";
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
        return NeoForgeConfigHelper.getGameDir();
    }

    @Override
    public Path getConfigDirectory() {
        return NeoForgeConfigHelper.getConfigDir();
    }

    @Override
    public boolean isClient() {
        return FMLEnvironment.dist.isClient();
    }

    @Override
    public void openMenu() {
        if (!isClient()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.execute(() -> mc.setScreen(new ZombekNeoForgeScreen()));
        }
    }
}
