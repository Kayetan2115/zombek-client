package pl.zombek.client.integration;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import pl.zombek.client.core.info.IInfoHudDataProvider;

/**
 * Fabric platform data provider supplying live coordinates, rotation,
 * dimension, current FPS, and multiplayer network latency.
 */
public class FabricInfoHudDataProvider implements IInfoHudDataProvider {

    @Override
    public boolean isPlayerInGame() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null && client.world != null;
    }

    @Override
    public double getPlayerX() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null ? client.player.getX() : 0.0;
    }

    @Override
    public double getPlayerY() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null ? client.player.getY() : 64.0;
    }

    @Override
    public double getPlayerZ() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null ? client.player.getZ() : 0.0;
    }

    @Override
    public float getPlayerYaw() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null ? client.player.getYaw() : 0.0f;
    }

    @Override
    public float getPlayerPitch() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null ? client.player.getPitch() : 0.0f;
    }

    @Override
    public String getDimensionId() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.world != null) {
            return client.world.getRegistryKey().getValue().toString();
        }
        return "minecraft:overworld";
    }

    @Override
    public int getCurrentFps() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null ? client.getCurrentFps() : 60;
    }

    @Override
    public int getPingMs() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.isInSingleplayer() || client.player == null || client.getNetworkHandler() == null) {
            return -1;
        }

        PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        return entry != null ? entry.getLatency() : -1;
    }

    @Override
    public boolean isSingleplayer() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client == null || client.isInSingleplayer();
    }
}
