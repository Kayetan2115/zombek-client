package pl.zombek.client.quilt.platform;

import net.minecraft.client.MinecraftClient;
import pl.zombek.client.core.hypixel.IHypixelDataProvider;

/**
 * Quilt provider supplying active server address, singleplayer status,
 * and keyboard clipboard integration for Hypixel Mods.
 */
public class QuiltHypixelDataProvider implements IHypixelDataProvider {

    @Override
    public String getCurrentServerAddress() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.getCurrentServerEntry() != null) {
            return client.getCurrentServerEntry().address;
        }
        return null;
    }

    @Override
    public boolean isSingleplayer() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.isInSingleplayer();
    }

    @Override
    public void copyToClipboard(String text) {
        if (text == null) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.keyboard != null) {
            client.keyboard.setClipboard(text);
        }
    }

    @Override
    public void playSound(String soundId, float volume, float pitch) {
        // Safe no-op or audio feedback
    }

    @Override
    public boolean isPlayerInGame() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null && client.world != null;
    }
}
