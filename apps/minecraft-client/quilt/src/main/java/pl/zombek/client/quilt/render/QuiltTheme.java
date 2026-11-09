package pl.zombek.client.quilt.render;

/**
 * Visual design tokens for ZombekClient Festive Dark theme on Quilt Loader.
 * Keeps 100% visual parity with Fabric, Forge, and NeoForge editions.
 */
public final class QuiltTheme {
    private QuiltTheme() {}

    // Backgrounds & Surfaces
    public static final int BG_OVERLAY = 0xD00A0E17;
    public static final int PANEL_BG = 0xF0101826;
    public static final int PANEL_BORDER = 0x4038BDF8;
    public static final int CARD_BG = 0xE0172033;
    public static final int CARD_BG_HOVER = 0xE01E293B;
    public static final int CARD_BG_ENABLED = 0xE01A2E44;
    public static final int CARD_BORDER = 0x3038BDF8;
    public static final int CARD_BORDER_ENABLED = 0x8038BDF8;

    // Brand & Accents
    public static final int ACCENT_CYAN = 0xFF38BDF8;
    public static final int ACCENT_GREEN = 0xFF4ADE80;
    public static final int ACCENT_RED = 0xFFF87171;
    public static final int ACCENT_AMBER = 0xFFF59E0B;
    public static final int ACCENT_GOLD = 0xFFFBBF24;

    // Typography
    public static final int TEXT_PRIMARY = 0xFFF1F5F9;
    public static final int TEXT_MUTED = 0xFF94A3B8;
    public static final int TEXT_DIM = 0xFF64748B;

    // Interactive Controls
    public static final int TOGGLE_TRACK_OFF = 0xFF334155;
    public static final int TOGGLE_TRACK_ON = 0xFF166534;
    public static final int TOGGLE_KNOB_OFF = 0xFF94A3B8;
    public static final int TOGGLE_KNOB_ON = 0xFF4ADE80;

    public static final int SLIDER_TRACK = 0xFF1E293B;
    public static final int SLIDER_FILL = 0xFF0284C7;
    public static final int SLIDER_THUMB = 0xFF38BDF8;

    public static final int BUTTON_BG = 0xFF1E293B;
    public static final int BUTTON_BG_HOVER = 0xFF334155;
    public static final int BUTTON_BORDER = 0x5038BDF8;
}
