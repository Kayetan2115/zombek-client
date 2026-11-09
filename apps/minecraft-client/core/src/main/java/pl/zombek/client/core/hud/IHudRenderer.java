package pl.zombek.client.core.hud;

/**
 * Platform-agnostic contract for rendering primitives and text on the Minecraft in-game HUD.
 * Implemented by each loader adapter (Fabric DrawContext, Forge GuiGraphics, NeoForge GuiGraphics, Quilt DrawContext).
 */
public interface IHudRenderer {

    /**
     * Draws a solid or semi-transparent filled rectangle.
     *
     * @param x      X screen coordinate
     * @param y      Y screen coordinate
     * @param width  Rectangle width
     * @param height Rectangle height
     * @param color  ARGB packed integer color
     */
    void fill(int x, int y, int width, int height, int color);

    /**
     * Draws a rectangle border of specified pixel thickness.
     */
    void drawBorder(int x, int y, int width, int height, int color, int thickness);

    /**
     * Draws text with optional drop shadow and scale.
     *
     * @param text   String to render
     * @param x      X screen coordinate
     * @param y      Y screen coordinate
     * @param color  ARGB packed integer color
     * @param shadow Whether to render Minecraft font shadow
     * @param scale  Scale factor (1.0 = standard)
     * @param bold   Whether font should be bold
     */
    void drawText(String text, int x, int y, int color, boolean shadow, float scale, boolean bold);

    /**
     * Draws horizontally centered text with shadow.
     */
    default void drawCenteredTextWithShadow(String text, int centerX, int y, int color) {
        if (text == null) return;
        int textW = getTextWidth(text);
        drawText(text, centerX - textW / 2, y, color, true, 1.0f, false);
    }

    /**
     * Draws a horizontal line of 1px thickness.
     */
    default void drawHLine(int x, int y, int width, int color) {
        fill(x, y, width, 1, color);
    }

    /**
     * Draws a vertical line of 1px thickness.
     */
    default void drawVLine(int x, int y, int height, int color) {
        fill(x, y, 1, height, color);
    }

    /**
     * Computes the pixel width of the given text according to the current Minecraft font renderer.
     */
    int getTextWidth(String text);

    /**
     * Returns standard Minecraft font line height (default 9px).
     */
    int getFontHeight();

    /**
     * Returns scaled screen width in Minecraft GUI coordinates.
     */
    int getScreenWidth();

    /**
     * Returns scaled screen height in Minecraft GUI coordinates.
     */
    int getScreenHeight();

    /**
     * Renders a native item stack at specified screen coordinates.
     *
     * @param itemHandle Platform-specific ItemStack object
     * @param x          X screen coordinate
     * @param y          Y screen coordinate
     */
    default void renderItem(Object itemHandle, int x, int y) {
        renderItem(itemHandle, x, y, 1.0f);
    }

    /**
     * Renders a native item stack at specified screen coordinates with scaling.
     *
     * @param itemHandle Platform-specific ItemStack object
     * @param x          X screen coordinate
     * @param y          Y screen coordinate
     * @param scale      Scale factor
     */
    default void renderItem(Object itemHandle, int x, int y, float scale) {
        // Default no-op for mock/headless unit testing
    }
}
