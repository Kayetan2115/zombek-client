package pl.zombek.client.core.settings;

import java.util.Locale;

/**
 * Color setting supporting RGBA components, ARGB pack, and hex formatting.
 */
public class ColorSetting extends Setting<Integer> {
    private final boolean supportAlpha;

    public ColorSetting(String id, String name, String description, int defaultArgb, boolean supportAlpha) {
        super(id, name, description, defaultArgb);
        this.supportAlpha = supportAlpha;
    }

    public ColorSetting(String id, String name, String description, int defaultArgb) {
        this(id, name, description, defaultArgb, true);
    }

    public ColorSetting(String id, String name, String description, String hexDefault, boolean supportAlpha) {
        this(id, name, description, parseHex(hexDefault), supportAlpha);
    }

    public boolean isSupportAlpha() {
        return supportAlpha;
    }

    public int getAlpha() {
        return (getValue() >> 24) & 0xFF;
    }

    public int getRed() {
        return (getValue() >> 16) & 0xFF;
    }

    public int getGreen() {
        return (getValue() >> 8) & 0xFF;
    }

    public int getBlue() {
        return getValue() & 0xFF;
    }

    public int getArgb() {
        return getValue();
    }

    public void setRgba(int red, int green, int blue, int alpha) {
        int r = Math.max(0, Math.min(255, red));
        int g = Math.max(0, Math.min(255, green));
        int b = Math.max(0, Math.min(255, blue));
        int a = supportAlpha ? Math.max(0, Math.min(255, alpha)) : 255;
        setValue((a << 24) | (r << 16) | (g << 8) | b);
    }

    public void setRgb(int red, int green, int blue) {
        setRgba(red, green, blue, getAlpha());
    }

    public String getHex() {
        if (supportAlpha) {
            return String.format(Locale.US, "#%02X%02X%02X%02X", getRed(), getGreen(), getBlue(), getAlpha());
        }
        return String.format(Locale.US, "#%02X%02X%02X", getRed(), getGreen(), getBlue());
    }

    public static int parseHex(String hex) {
        if (hex == null || hex.isEmpty()) {
            return 0xFFFFFFFF;
        }
        String clean = hex.startsWith("#") ? hex.substring(1).trim() : hex.trim();
        try {
            if (clean.length() == 6) {
                int rgb = Integer.parseInt(clean, 16);
                return 0xFF000000 | rgb;
            } else if (clean.length() == 8) {
                long val = Long.parseLong(clean, 16);
                int r = (int) ((val >> 24) & 0xFF);
                int g = (int) ((val >> 16) & 0xFF);
                int b = (int) ((val >> 8) & 0xFF);
                int a = (int) (val & 0xFF);
                return (a << 24) | (r << 16) | (g << 8) | b;
            }
        } catch (NumberFormatException ignored) {}
        return 0xFFFFFFFF;
    }

    @Override
    public String serialize() {
        return getHex();
    }

    @Override
    public boolean deserialize(String raw) {
        if (raw == null || raw.trim().isEmpty()) return false;
        return setValue(parseHex(raw));
    }
}
