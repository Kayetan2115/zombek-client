package pl.zombek.client.core.hud.editor;

/**
 * Standard aspect ratio simulation presets for responsive HUD layout testing.
 */
public enum HudAspectRatio {
    RATIO_16_9("16:9 (Standard Widescreen)", 16.0 / 9.0),
    RATIO_16_10("16:10 (Laptops / Mac)", 16.0 / 10.0),
    RATIO_21_9("21:9 (Ultrawide)", 21.0 / 9.0),
    RATIO_32_9("32:9 (Super Ultrawide)", 32.0 / 9.0),
    RATIO_4_3("4:3 (Classic / CRT)", 4.0 / 3.0),
    CUSTOM("Własny", 16.0 / 9.0);

    private final String title;
    private final double ratio;

    HudAspectRatio(String title, double ratio) {
        this.title = title;
        this.ratio = ratio;
    }

    public String getTitle() {
        return title;
    }

    public String getDisplayName() {
        return title;
    }

    public double getRatio() {
        return ratio;
    }

    public int calculateHeight(int width) {
        if (width <= 0) return 0;
        return (int) Math.round(width / ratio);
    }

    public int calculateWidth(int height) {
        if (height <= 0) return 0;
        return (int) Math.round(height * ratio);
    }
}
