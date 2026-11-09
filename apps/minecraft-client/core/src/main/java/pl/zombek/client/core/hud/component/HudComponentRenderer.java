package pl.zombek.client.core.hud.component;

import pl.zombek.client.core.hud.IHudRenderer;

/**
 * Shared rendering utilities for HUD information modules.
 * Ensures consistent design tokens, background transparency, and typography.
 */
public final class HudComponentRenderer {

    private HudComponentRenderer() {}

    /**
     * Renders background container box and border according to component style.
     */
    public static void renderBackground(
            IHudRenderer renderer,
            int x, int y, int width, int height,
            HudComponentStyle style,
            int customBgColor,
            double customOpacity,
            int customBorderColor,
            boolean customShowBorder,
            int borderThickness
    ) {
        if (renderer == null || width <= 0 || height <= 0) return;

        switch (style) {
            case VANILLA:
                // Standard semi-transparent dark box without borders
                renderer.fill(x, y, width, height, 0x50000000);
                break;

            case MINIMAL:
                // No background or border in minimal mode
                break;

            case CLASSIC:
                // Semi-transparent dark background with subtle outline
                int classicBg = 0x8A10141D; // ~54% opacity dark
                int classicBorder = 0x33FFFFFF; // 20% white outline
                renderer.fill(x, y, width, height, classicBg);
                renderer.drawBorder(x, y, width, height, classicBorder, 1);
                break;

            case MODERN:
                // Festive Slate with dark header tint and subtle Christmas red accent
                int modernBg = 0xCD0B1120; // ~80% opacity Festive Dark
                int modernBorder = 0x77EF4444; // Christmas red accent border
                renderer.fill(x, y, width, height, modernBg);
                renderer.drawBorder(x, y, width, height, modernBorder, 1);
                break;

            case CUSTOM:
            default:
                if (customOpacity > 0.0) {
                    double op = Math.max(0.0, Math.min(1.0, customOpacity / 100.0));
                    int alpha = (int) (((customBgColor >>> 24) & 0xFF) * op);
                    int finalBg = (alpha << 24) | (customBgColor & 0x00FFFFFF);
                    renderer.fill(x, y, width, height, finalBg);
                }
                if (customShowBorder && borderThickness > 0) {
                    renderer.drawBorder(x, y, width, height, customBorderColor, borderThickness);
                }
                break;
        }
    }

    /**
     * Helper to compute combined ARGB color with opacity factor.
     */
    public static int applyAlpha(int color, double opacity) {
        double op = Math.max(0.0, Math.min(1.0, opacity));
        int a = (int) (((color >>> 24) & 0xFF) * op);
        return (a << 24) | (color & 0x00FFFFFF);
    }
}
