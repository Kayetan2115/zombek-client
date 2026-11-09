package pl.zombek.client.forge.render;

/**
 * Design tokens and colors for ZombekClient on Minecraft Forge.
 * Exactly matches the Festive Dark Theme used in Fabric.
 */
public final class ForgeTheme {
    // Backdrop
    public static final int COLOR_OVERLAY = 0xAA020617;
    public static final int COLOR_WINDOW_BG = 0xF50F172A;
    public static final int COLOR_SIDEBAR_BG = 0xF80B1120;
    public static final int COLOR_TOPBAR_BG = 0xE6131D33;
    public static final int COLOR_BOTTOMBAR_BG = 0xE60B1120;

    // Card & Widget Backgrounds
    public static final int COLOR_CARD_BG = 0xFF1E293B;
    public static final int COLOR_CARD_HOVER = 0xFF283548;
    public static final int COLOR_CARD_BORDER = 0xFF334155;
    public static final int COLOR_INPUT_BG = 0xFF0B1120;

    // Accents
    public static final int COLOR_ACCENT_GREEN = 0xFF10B981; // Emerald Pine
    public static final int COLOR_ACCENT_GREEN_HOVER = 0xFF059669;
    public static final int COLOR_ACCENT_GOLD = 0xFFF59E0B; // Festive Gold
    public static final int COLOR_ACCENT_RED = 0xFFEF4444; // Holiday Crimson
    public static final int COLOR_ACCENT_BLUE = 0xFF3B82F6;

    // Text
    public static final int TEXT_PRIMARY = 0xFFF8FAFC;
    public static final int TEXT_SECONDARY = 0xFF94A3B8;
    public static final int TEXT_MUTED = 0xFF64748B;
    public static final int TEXT_GOLD = 0xFFFCD34D;
    public static final int TEXT_SUCCESS = 0xFF34D399;
    public static final int TEXT_ERROR = 0xFFF87171;

    private ForgeTheme() {}
}
