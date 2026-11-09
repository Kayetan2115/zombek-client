package pl.zombek.client.integration;

import net.minecraft.client.MinecraftClient;
import net.minecraft.stat.Stats;
import pl.zombek.client.core.time.ITimeDataProvider;

/**
 * Fabric platform data provider providing game world time, total world age,
 * session metadata, and singleplayer statistics using Yarn mappings.
 */
public class FabricTimeDataProvider implements ITimeDataProvider {

    @Override
    public boolean isWorldLoaded() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.world != null && client.player != null;
    }

    @Override
    public long getWorldTimeOfDay() {
        MinecraftClient client = MinecraftClient.getInstance();
        return (client != null && client.world != null) ? client.world.getTimeOfDay() : -1L;
    }

    @Override
    public long getWorldGameTime() {
        MinecraftClient client = MinecraftClient.getInstance();
        return (client != null && client.world != null) ? client.world.getTime() : -1L;
    }

    @Override
    public boolean isTimeAvailable() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.world != null;
    }

    @Override
    public boolean isSingleplayer() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.isInSingleplayer();
    }

    @Override
    public String getWorldName() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.isInSingleplayer() && client.getServer() != null) {
            return client.getServer().getSaveProperties().getLevelName();
        }
        return null;
    }

    @Override
    public String getServerAddress() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && !client.isInSingleplayer() && client.getCurrentServerEntry() != null) {
            return client.getCurrentServerEntry().address;
        }
        return null;
    }

    @Override
    public long getSingleplayerPlaytimeTicks() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.isInSingleplayer() && client.player != null && client.player.getStatHandler() != null) {
            try {
                return client.player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));
            } catch (Throwable ignored) {
                try {
                    return client.player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.TOTAL_WORLD_TIME));
                } catch (Throwable ignored2) {}
            }
        }
        return -1L;
    }

    @Override
    public String getDimensionId() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.world != null) {
            return client.world.getRegistryKey().getValue().toString();
        }
        return "minecraft:overworld";
    }
}
