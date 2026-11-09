package pl.zombek.client.neoforge.integration;

import net.minecraft.client.Minecraft;
import pl.zombek.client.core.hypixel.IHypixelDataProvider;

/**
 * NeoForge provider supplying active server address, singleplayer status,
 * and keyboard clipboard integration for Hypixel Mods.
 */
public class NeoForgeHypixelDataProvider implements IHypixelDataProvider {

    @Override
    public String getCurrentServerAddress() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getCurrentServer() != null) {
            return mc.getCurrentServer().ip;
        }
        return null;
    }

    @Override
    public boolean isSingleplayer() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.isSingleplayer();
    }

    @Override
    public void copyToClipboard(String text) {
        if (text == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.keyboardHandler != null) {
            mc.keyboardHandler.setClipboard(text);
        }
    }

    @Override
    public void playSound(String soundId, float volume, float pitch) {
        // Safe no-op or audio feedback
    }

    @Override
    public boolean isPlayerInGame() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.player != null && mc.level != null;
    }
}
