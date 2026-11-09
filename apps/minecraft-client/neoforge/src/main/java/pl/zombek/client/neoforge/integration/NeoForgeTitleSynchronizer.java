package pl.zombek.client.neoforge.integration;

import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import pl.zombek.client.core.titles.ZombekTitleService;

import java.lang.reflect.Field;

/**
 * Synchronizes vanilla NeoForge title state with ZombekTitleService.
 */
public class NeoForgeTitleSynchronizer {

    private static Field titleField;
    private static Field subtitleField;
    private static Field overlayMessageField;
    private static String lastTitle;
    private static String lastSubtitle;
    private static String lastActionBar;
    private static boolean initialized = false;

    private static void init() {
        if (initialized) return;
        initialized = true;
        try {
            for (Field f : Gui.class.getDeclaredFields()) {
                f.setAccessible(true);
                if (Component.class.isAssignableFrom(f.getType())) {
                    String name = f.getName().toLowerCase();
                    if (name.contains("title") && !name.contains("sub") && !name.contains("overlay")) {
                        titleField = f;
                    } else if (name.contains("subtitle") || name.contains("sub")) {
                        subtitleField = f;
                    } else if (name.contains("overlay") || name.contains("record") || name.contains("action")) {
                        overlayMessageField = f;
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    public static void sync(Gui gui) {
        if (gui == null) return;
        init();
        try {
            if (titleField != null) {
                Component c = (Component) titleField.get(gui);
                String t = c != null ? c.getString() : null;
                if (t != null && !t.equals(lastTitle)) {
                    lastTitle = t;
                    ZombekTitleService.getInstance().onTitle(t);
                } else if (t == null && lastTitle != null) {
                    lastTitle = null;
                }
            }
            if (subtitleField != null) {
                Component c = (Component) subtitleField.get(gui);
                String s = c != null ? c.getString() : null;
                if (s != null && !s.equals(lastSubtitle)) {
                    lastSubtitle = s;
                    ZombekTitleService.getInstance().onSubtitle(s);
                } else if (s == null && lastSubtitle != null) {
                    lastSubtitle = null;
                }
            }
            if (overlayMessageField != null) {
                Component c = (Component) overlayMessageField.get(gui);
                String a = c != null ? c.getString() : null;
                if (a != null && !a.equals(lastActionBar)) {
                    lastActionBar = a;
                    ZombekTitleService.getInstance().onActionBar(a);
                } else if (a == null && lastActionBar != null) {
                    lastActionBar = null;
                }
            }
        } catch (Throwable ignored) {}
    }
}
