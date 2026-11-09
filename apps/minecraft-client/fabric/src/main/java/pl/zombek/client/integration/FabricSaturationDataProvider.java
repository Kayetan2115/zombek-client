package pl.zombek.client.integration;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.HungerManager;
import pl.zombek.client.core.hud.saturation.ISaturationDataProvider;

/**
 * Fabric platform data provider supplying real hunger, saturation, and exhaustion metrics.
 */
public class FabricSaturationDataProvider implements ISaturationDataProvider {

    @Override
    public boolean isPlayerAvailable() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null;
    }

    @Override
    public int getFoodLevel() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) {
            HungerManager hunger = client.player.getHungerManager();
            if (hunger != null) {
                return hunger.getFoodLevel();
            }
        }
        return 20;
    }

    @Override
    public boolean isSaturationAvailable() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return false;
        }
        // Saturation is guaranteed accurate in singleplayer or when hunger manager has non-zero saturation
        if (client.isInSingleplayer()) {
            return true;
        }
        HungerManager hunger = client.player.getHungerManager();
        return hunger != null && hunger.getSaturationLevel() > 0.0f;
    }

    @Override
    public float getSaturationLevel() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) {
            HungerManager hunger = client.player.getHungerManager();
            if (hunger != null) {
                return hunger.getSaturationLevel();
            }
        }
        return 0.0f;
    }

    @Override
    public boolean isExhaustionAvailable() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.isInSingleplayer();
    }

    @Override
    public float getExhaustionLevel() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) {
            HungerManager hunger = client.player.getHungerManager();
            if (hunger != null) {
                return hunger.getExhaustion();
            }
        }
        return 0.0f;
    }
}
