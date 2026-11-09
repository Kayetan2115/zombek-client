package pl.zombek.client.core.modules.visual.tabeditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Formatter and manager for Tab list header and footer contents.
 */
public final class TabHeaderFooterManager {

    private TabHeaderFooterManager() {}

    public static List<String> resolveLines(HeaderFooterMode mode, String serverText, String customText, boolean isHeader) {
        if (mode == HeaderFooterMode.HIDDEN) {
            return Collections.emptyList();
        }

        String raw = (mode == HeaderFooterMode.CUSTOM) ? customText : serverText;
        if (raw == null || raw.isBlank()) {
            if (mode == HeaderFooterMode.ORIGINAL && (serverText == null || serverText.isBlank())) {
                return Collections.emptyList();
            }
            raw = isHeader ? "§6§lZombekClient§r §7— §fPlayer List" : "§7Enjoy your game on the server!";
        }

        String[] parts = raw.split("\n");
        List<String> lines = new ArrayList<>();
        for (String p : parts) {
            lines.add(p.trim());
        }
        return lines;
    }
}
