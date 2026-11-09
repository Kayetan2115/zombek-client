package pl.zombek.client.forge.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import pl.zombek.client.core.info.IInfoHudDataProvider;

/**
 * Forge platform data provider supplying live coordinates, rotation,
 * dimension, current FPS, and multiplayer latency using official Mojang mappings.
 */
public class ForgeInfoHudDataProvider implements IInfoHudDataProvider {

    @Override
    public boolean isPlayerInGame() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.player != null && mc.level != null;
    }

    @Override
    public double getPlayerX() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.player != null ? mc.player.getX() : 0.0;
    }

    @Override
    public double getPlayerY() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.player != null ? mc.player.getY() : 64.0;
    }

    @Override
    public double getPlayerZ() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.player != null ? mc.player.getZ() : 0.0;
    }

    @Override
    public float getPlayerYaw() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.player != null ? mc.player.getYRot() : 0.0f;
    }

    @Override
    public float getPlayerPitch() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.player != null ? mc.player.getXRot() : 0.0f;
    }

    @Override
    public String getDimensionId() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.level != null) {
            return mc.level.dimension().location().toString();
        }
        return "minecraft:overworld";
    }

    @Override
    public int getCurrentFps() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null ? mc.getFps() : 60;
    }

    @Override
    public int getPingMs() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.isSingleplayer() || mc.player == null || mc.getConnection() == null) {
            return -1;
        }

        PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        return info != null ? info.getLatency() : -1;
    }

    @Override
    public boolean isSingleplayer() {
        Minecraft mc = Minecraft.getInstance();
        return mc == null || mc.isSingleplayer();
    }
}
