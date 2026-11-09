package pl.zombek.client.core.hud;

/**
 * Screen corner anchor points for positioning in-game HUD modules.
 * Allows modules to dynamically adapt when the window or GUI scale changes.
 */
public enum HudAnchor {
    TOP_LEFT("Lewy Górny"),
    TOP_CENTER("Środek Góra"),
    TOP_RIGHT("Prawy Górny"),
    BOTTOM_LEFT("Lewy Dolny"),
    BOTTOM_CENTER("Środek Dół"),
    BOTTOM_RIGHT("Prawy Dolny"),
    CENTER("Środek");

    private final String displayName;

    HudAnchor(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Calculates the absolute screen X coordinate for an element of given width.
     *
     * @param offsetX     Relative horizontal offset configured by the user
     * @param width       Element width
     * @param screenWidth Scaled screen width
     * @return Absolute X position bounded within the screen
     */
    public int calculateX(int offsetX, int width, int screenWidth) {
        if (screenWidth <= 0) return 0;
        int clampedWidth = Math.min(width, screenWidth);
        switch (this) {
            case TOP_RIGHT:
            case BOTTOM_RIGHT:
                return Math.max(0, screenWidth - clampedWidth - offsetX);
            case TOP_CENTER:
            case BOTTOM_CENTER:
            case CENTER:
                int centerX = (screenWidth - clampedWidth) / 2 + offsetX;
                return Math.max(0, Math.min(centerX, screenWidth - clampedWidth));
            case TOP_LEFT:
            case BOTTOM_LEFT:
            default:
                return Math.max(0, Math.min(offsetX, screenWidth - clampedWidth));
        }
    }

    /**
     * Calculates the un-clamped screen X coordinate for an element.
     */
    public int calculateRawX(int offsetX, int width, int screenWidth) {
        if (screenWidth <= 0) return 0;
        switch (this) {
            case TOP_RIGHT:
            case BOTTOM_RIGHT:
                return screenWidth - width - offsetX;
            case TOP_CENTER:
            case BOTTOM_CENTER:
            case CENTER:
                return (screenWidth - width) / 2 + offsetX;
            case TOP_LEFT:
            case BOTTOM_LEFT:
            default:
                return offsetX;
        }
    }

    /**
     * Calculates the absolute screen Y coordinate for an element of given height.
     *
     * @param offsetY      Relative vertical offset configured by the user
     * @param height       Element height
     * @param screenHeight Scaled screen height
     * @return Absolute Y position bounded within the screen
     */
    public int calculateY(int offsetY, int height, int screenHeight) {
        if (screenHeight <= 0) return 0;
        int clampedHeight = Math.min(height, screenHeight);
        switch (this) {
            case BOTTOM_LEFT:
            case BOTTOM_RIGHT:
            case BOTTOM_CENTER:
                return Math.max(0, screenHeight - clampedHeight - offsetY);
            case CENTER:
                int centerY = (screenHeight - clampedHeight) / 2 + offsetY;
                return Math.max(0, Math.min(centerY, screenHeight - clampedHeight));
            case TOP_LEFT:
            case TOP_RIGHT:
            case TOP_CENTER:
            default:
                return Math.max(0, Math.min(offsetY, screenHeight - clampedHeight));
        }
    }

    /**
     * Calculates the un-clamped screen Y coordinate for an element.
     */
    public int calculateRawY(int offsetY, int height, int screenHeight) {
        if (screenHeight <= 0) return 0;
        switch (this) {
            case BOTTOM_LEFT:
            case BOTTOM_RIGHT:
            case BOTTOM_CENTER:
                return screenHeight - height - offsetY;
            case CENTER:
                return (screenHeight - height) / 2 + offsetY;
            case TOP_LEFT:
            case TOP_RIGHT:
            case TOP_CENTER:
            default:
                return offsetY;
        }
    }

    /**
     * Inverts absolute screen X into anchor-relative offset X.
     */
    public int screenXToOffsetX(int screenX, int width, int screenWidth) {
        if (screenWidth <= 0) return 0;
        int clampedWidth = Math.min(width, screenWidth);
        switch (this) {
            case TOP_RIGHT:
            case BOTTOM_RIGHT:
                return screenWidth - clampedWidth - screenX;
            case TOP_CENTER:
            case BOTTOM_CENTER:
            case CENTER:
                return screenX - (screenWidth - clampedWidth) / 2;
            case TOP_LEFT:
            case BOTTOM_LEFT:
            default:
                return screenX;
        }
    }

    /**
     * Inverts absolute screen Y into anchor-relative offset Y.
     */
    public int screenYToOffsetY(int screenY, int height, int screenHeight) {
        if (screenHeight <= 0) return 0;
        int clampedHeight = Math.min(height, screenHeight);
        switch (this) {
            case BOTTOM_LEFT:
            case BOTTOM_RIGHT:
            case BOTTOM_CENTER:
                return screenHeight - clampedHeight - screenY;
            case CENTER:
                return screenY - (screenHeight - clampedHeight) / 2;
            case TOP_LEFT:
            case TOP_RIGHT:
            case TOP_CENTER:
            default:
                return screenY;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}
