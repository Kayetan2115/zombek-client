package pl.zombek.client.neoforge.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.stats.Stats;
import pl.zombek.client.core.time.ITimeDataProvider;

/**
 * NeoForge platform data provider supplying game world time, total world age,
 * session metadata, and singleplayer statistics using official Mojang mappings.
 */
public class NeoForgeTimeDataProvider implements ITimeDataProvider {

    @Override
    public boolean isWorldLoaded() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.level != null && mc.player != null;
    }

    @Override
    public long getWorldTimeOfDay() {
        Minecraft mc = Minecraft.getInstance();
        return (mc != null && mc.level != null) ? mc.level.getDayTime() : -1L;
    }

    @Override
    public long getWorldGameTime() {
        Minecraft mc = Minecraft.getInstance();
        return (mc != null && mc.level != null) ? mc.level.getGameTime() : -1L;
    }

    @Override
    public boolean isTimeAvailable() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.level != null;
    }

    @Override
    public boolean isSingleplayer() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.hasSingleplayerServer();
    }

    @Override
    public String getWorldName() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null) {
            return mc.getSingleplayerServer().getWorldData().getLevelName();
        }
        return null;
    }

    @Override
    public String getServerAddress() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && !mc.hasSingleplayerServer() && mc.getCurrentServer() != null) {
            return mc.getCurrentServer().ip;
        }
        return null;
    }

    @Override
    public long getSingleplayerPlaytimeTicks() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.hasSingleplayerServer() && mc.player != null && mc.player.getStats() != null) {
            try {
                return mc.player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
            } catch (Throwable ignored) {
                try {
                    return mc.player.getStats().getValue(Stats.CUSTOM.get(Stats.TOTAL_WORLD_TIME));
                } catch (Throwable ignored2) {}
            }
        }
        return -1L;
    }

    @Override
    public String getDimensionId() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.level != null) {
            return mc.level.dimension().location().toString();
        }
        return "minecraft:overworld";
    }
}
