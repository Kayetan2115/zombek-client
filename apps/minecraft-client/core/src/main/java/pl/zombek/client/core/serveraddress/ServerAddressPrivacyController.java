package pl.zombek.client.core.serveraddress;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * Controller applying masking, hiding, alias substitution, and streamer protection rules
 * to server connection details.
 */
public final class ServerAddressPrivacyController {

    private ServerAddressPrivacyController() {}

    /**
     * Formats server host string according to the active privacy mode.
     */
    public static String formatHost(String rawHost, PrivacyMode mode, boolean streamerMode) {
        if (rawHost == null || rawHost.trim().isEmpty()) {
            return "UNAVAILABLE";
        }

        if (streamerMode || mode == PrivacyMode.STREAMER_MODE || mode == PrivacyMode.HIDE_ADDRESS) {
            return "[HIDDEN]";
        }

        if (mode == PrivacyMode.MASK_HOST) {
            return maskHostString(rawHost);
        }

        return rawHost;
    }

    /**
     * Masks host keeping the first 3 characters and the domain extension, e.g. "play.example.net" -> "pla*****.net".
     */
    public static String maskHostString(String host) {
        if (host == null || host.length() <= 4) {
            return "****";
        }

        int dotIndex = host.lastIndexOf('.');
        if (dotIndex > 3) {
            String prefix = host.substring(0, 3);
            String extension = host.substring(dotIndex);
            return prefix + "****" + extension;
        }

        return host.substring(0, 2) + "****";
    }

    /**
     * Formats world name for singleplayer display, respecting streamer mode and privacy toggles.
     */
    public static String formatWorldName(String rawWorldName, boolean hideWorldName, boolean streamerMode) {
        if (streamerMode || hideWorldName) {
            return "[HIDDEN]";
        }
        return (rawWorldName != null && !rawWorldName.trim().isEmpty()) ? rawWorldName : "Singleplayer World";
    }

    /**
     * Copies text to system clipboard upon deliberate user interaction.
     */
    public static boolean copyToClipboard(String text) {
        if (text == null || text.isEmpty()) return false;
        try {
            if (!GraphicsEnvironment.isHeadless()) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
                return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }
}
