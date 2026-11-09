package pl.zombek.client.core.hud.editor;

/**
 * 8 control points / handles around a selected component's bounding box used for manual resizing.
 */
public enum HudResizeHandle {
    TOP_LEFT("TL", -1, -1),
    TOP_CENTER("TC", 0, -1),
    TOP_RIGHT("TR", 1, -1),
    MIDDLE_LEFT("ML", -1, 0),
    MIDDLE_RIGHT("MR", 1, 0),
    BOTTOM_LEFT("BL", -1, 1),
    BOTTOM_CENTER("BC", 0, 1),
    BOTTOM_RIGHT("BR", 1, 1);

    private final String code;
    private final int deltaX;
    private final int deltaY;

    HudResizeHandle(String code, int deltaX, int deltaY) {
        this.code = code;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    public String getCode() {
        return code;
    }

    public int getDeltaX() {
        return deltaX;
    }

    public int getDeltaY() {
        return deltaY;
    }

    public boolean affectsLeft() {
        return deltaX < 0;
    }

    public boolean affectsRight() {
        return deltaX > 0;
    }

    public boolean affectsTop() {
        return deltaY < 0;
    }

    public boolean affectsBottom() {
        return deltaY > 0;
    }

    public boolean affectsHorizontal() {
        return deltaX != 0;
    }

    public boolean affectsVertical() {
        return deltaY != 0;
    }

    /**
     * Calculates the center (X, Y) of this handle on screen given the component bounding box.
     */
    public int getCenterX(int boxX, int boxW) {
        if (deltaX < 0) return boxX;
        if (deltaX > 0) return boxX + boxW;
        return boxX + boxW / 2;
    }

    public int getCenterY(int boxY, int boxH) {
        if (deltaY < 0) return boxY;
        if (deltaY > 0) return boxY + boxH;
        return boxY + boxH / 2;
    }

    /**
     * Hit tests whether point (x, y) falls inside this handle rectangle.
     */
    public boolean contains(int pointX, int pointY, int boxX, int boxY, int boxW, int boxH, int handleSize) {
        int cx = getCenterX(boxX, boxW);
        int cy = getCenterY(boxY, boxH);
        int half = handleSize / 2;
        return pointX >= cx - half && pointX <= cx + half && pointY >= cy - half && pointY <= cy + half;
    }
}
