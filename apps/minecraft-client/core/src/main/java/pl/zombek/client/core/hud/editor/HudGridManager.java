package pl.zombek.client.core.hud.editor;

import pl.zombek.client.core.hud.IHudRenderer;

/**
 * Manages rendering of the reference grid, preset spacing options, and grid snapping rules.
 */
public class HudGridManager {

    public enum GridStyle {
        NONE("Brak"),
        DOTS("Punkty (Dots)"),
        LINES("Linie (Lines)"),
        CROSSHAIR_GRID("Siatka celowników"),
        CUSTOM("Własna");

        private final String displayName;

        GridStyle(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static final int[] PRESET_SPACINGS = {2, 4, 8, 16, 24, 32};

    private GridStyle style = GridStyle.LINES;
    private int spacing = 16;
    private int horizontalSpacing = 16;
    private int verticalSpacing = 16;
    private int gridColor = 0x22FFFFFF;
    private float opacity = 0.20f;
    private int thickness = 1;
    private boolean visible = true;
    private boolean showDuringDrag = true;

    private boolean snapToGrid = true;
    private boolean snapToCenter = true;
    private boolean snapToEdges = true;
    private int snapThreshold = 6;

    public void renderGrid(IHudRenderer renderer, HudCanvasViewport viewport) {
        if (!visible || style == GridStyle.NONE || renderer == null || viewport == null) {
            return;
        }

        int virtW = viewport.getVirtWidth();
        int virtH = viewport.getVirtHeight();
        int hSpace = Math.max(2, horizontalSpacing > 0 ? horizontalSpacing : spacing);
        int vSpace = Math.max(2, verticalSpacing > 0 ? verticalSpacing : spacing);

        int color = gridColor;

        switch (style) {
            case LINES:
            case CUSTOM:
                // Vertical lines
                for (int vx = 0; vx <= virtW; vx += hSpace) {
                    int screenX = viewport.virtualToScreenX(vx);
                    int screenY1 = viewport.virtualToScreenY(0);
                    int screenY2 = viewport.virtualToScreenY(virtH);
                    renderer.fill(screenX, screenY1, thickness, screenY2 - screenY1, color);
                }
                // Horizontal lines
                for (int vy = 0; vy <= virtH; vy += vSpace) {
                    int screenY = viewport.virtualToScreenY(vy);
                    int screenX1 = viewport.virtualToScreenX(0);
                    int screenX2 = viewport.virtualToScreenX(virtW);
                    renderer.fill(screenX1, screenY, screenX2 - screenX1, thickness, color);
                }
                break;

            case DOTS:
                for (int vx = 0; vx <= virtW; vx += hSpace) {
                    int sx = viewport.virtualToScreenX(vx);
                    for (int vy = 0; vy <= virtH; vy += vSpace) {
                        int sy = viewport.virtualToScreenY(vy);
                        renderer.fill(sx - 1, sy - 1, 2, 2, color);
                    }
                }
                break;

            case CROSSHAIR_GRID:
                for (int vx = 0; vx <= virtW; vx += hSpace) {
                    int sx = viewport.virtualToScreenX(vx);
                    for (int vy = 0; vy <= virtH; vy += vSpace) {
                        int sy = viewport.virtualToScreenY(vy);
                        renderer.fill(sx - 3, sy, 7, 1, color);
                        renderer.fill(sx, sy - 3, 1, 7, color);
                    }
                }
                break;

            default:
                break;
        }
    }

    public HudSnapping.SnapResult calculateSnap(int rawX, int rawY, int compW, int compH, int screenW, int screenH) {
        return HudSnapping.calculateSnap(rawX, rawY, compW, compH, screenW, screenH,
                spacing, snapToGrid, snapToCenter, snapToEdges, snapThreshold);
    }

    public GridStyle getStyle() {
        return style;
    }

    public void setStyle(GridStyle style) {
        this.style = style != null ? style : GridStyle.LINES;
    }

    public int getSpacing() {
        return spacing;
    }

    public void setSpacing(int spacing) {
        this.spacing = Math.max(2, spacing);
        this.horizontalSpacing = this.spacing;
        this.verticalSpacing = this.spacing;
    }

    public int getHorizontalSpacing() {
        return horizontalSpacing;
    }

    public void setHorizontalSpacing(int horizontalSpacing) {
        this.horizontalSpacing = Math.max(2, horizontalSpacing);
    }

    public int getVerticalSpacing() {
        return verticalSpacing;
    }

    public void setVerticalSpacing(int verticalSpacing) {
        this.verticalSpacing = Math.max(2, verticalSpacing);
    }

    public int getGridColor() {
        return gridColor;
    }

    public void setGridColor(int gridColor) {
        this.gridColor = gridColor;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = Math.max(0.05f, Math.min(1.0f, opacity));
        int alpha = (int) (this.opacity * 255.0f);
        this.gridColor = (alpha << 24) | (this.gridColor & 0x00FFFFFF);
    }

    public int getThickness() {
        return thickness;
    }

    public void setThickness(int thickness) {
        this.thickness = Math.max(1, Math.min(4, thickness));
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isShowDuringDrag() {
        return showDuringDrag;
    }

    public void setShowDuringDrag(boolean showDuringDrag) {
        this.showDuringDrag = showDuringDrag;
    }

    public boolean isSnapToGrid() {
        return snapToGrid;
    }

    public void setSnapToGrid(boolean snapToGrid) {
        this.snapToGrid = snapToGrid;
    }

    public boolean isSnapToCenter() {
        return snapToCenter;
    }

    public void setSnapToCenter(boolean snapToCenter) {
        this.snapToCenter = snapToCenter;
    }

    public boolean isSnapToEdges() {
        return snapToEdges;
    }

    public void setSnapToEdges(boolean snapToEdges) {
        this.snapToEdges = snapToEdges;
    }

    public int getSnapThreshold() {
        return snapThreshold;
    }

    public void setSnapThreshold(int snapThreshold) {
        this.snapThreshold = Math.max(1, Math.min(20, snapThreshold));
    }
}
