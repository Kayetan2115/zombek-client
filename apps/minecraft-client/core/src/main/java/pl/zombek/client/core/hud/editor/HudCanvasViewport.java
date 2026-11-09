package pl.zombek.client.core.hud.editor;

/**
 * Manages the zoom magnification and panning offset of the HUD Editor virtual canvas.
 * Translates between physical screen pixels and virtual reference screen space.
 */
public class HudCanvasViewport {

    public static final double MIN_ZOOM = 0.25;
    public static final double MAX_ZOOM = 4.0;
    private static final double[] ZOOM_PRESETS = {0.25, 0.50, 0.75, 1.00, 1.25, 1.50, 2.00, 3.00, 4.00};

    private double zoom = 1.0;
    private double panX = 0;
    private double panY = 0;

    private int canvasX;
    private int canvasY;
    private int canvasWidth = 600;
    private int canvasHeight = 400;

    private int virtWidth = 960;
    private int virtHeight = 540;

    private boolean isPanning = false;
    private double panStartMouseX;
    private double panStartMouseY;
    private double panStartPanX;
    private double panStartPanY;

    public void updateBounds(int canvasX, int canvasY, int canvasWidth, int canvasHeight, int virtWidth, int virtHeight) {
        this.canvasX = canvasX;
        this.canvasY = canvasY;
        this.canvasWidth = Math.max(50, canvasWidth);
        this.canvasHeight = Math.max(50, canvasHeight);
        this.virtWidth = Math.max(100, virtWidth);
        this.virtHeight = Math.max(100, virtHeight);
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double targetZoom) {
        this.zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, targetZoom));
    }

    public double getPanX() {
        return panX;
    }

    public double getPanY() {
        return panY;
    }

    public void setPan(double panX, double panY) {
        this.panX = panX;
        this.panY = panY;
    }

    public void zoomIn() {
        for (double preset : ZOOM_PRESETS) {
            if (preset > zoom + 0.02) {
                setZoom(preset);
                return;
            }
        }
        setZoom(zoom + 0.25);
    }

    public void zoomOut() {
        for (int i = ZOOM_PRESETS.length - 1; i >= 0; i--) {
            if (ZOOM_PRESETS[i] < zoom - 0.02) {
                setZoom(ZOOM_PRESETS[i]);
                return;
            }
        }
        setZoom(zoom - 0.25);
    }

    public void zoomAt(double targetZoom, double focalScreenX, double focalScreenY) {
        double clamped = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, targetZoom));
        if (Math.abs(clamped - zoom) < 0.001) return;

        double virtX = screenToVirtualX(focalScreenX);
        double virtY = screenToVirtualY(focalScreenY);

        this.zoom = clamped;

        // Adjust pan so the virtual point remains under the focal screen coordinate
        double newScreenX = virtualToScreenX(virtX);
        double newScreenY = virtualToScreenY(virtY);

        this.panX += (focalScreenX - newScreenX);
        this.panY += (focalScreenY - newScreenY);
    }

    public void resetZoom() {
        this.zoom = 1.0;
        this.panX = 0;
        this.panY = 0;
    }

    public void fitCanvas() {
        this.panX = 0;
        this.panY = 0;
        double fitW = (double) (canvasWidth - 30) / virtWidth;
        double fitH = (double) (canvasHeight - 30) / virtHeight;
        double bestFit = Math.min(fitW, fitH);
        setZoom(Math.max(MIN_ZOOM, Math.min(1.0, bestFit)));
    }

    public void zoomToSelection(int compVirtX, int compVirtY, int compVirtW, int compVirtH) {
        if (compVirtW <= 0 || compVirtH <= 0) return;
        double fitW = (double) (canvasWidth - 80) / compVirtW;
        double fitH = (double) (canvasHeight - 80) / compVirtH;
        double targetZoom = Math.max(MIN_ZOOM, Math.min(2.5, Math.min(fitW, fitH)));
        this.zoom = targetZoom;

        // Center on component
        double compCenterVirtX = compVirtX + compVirtW / 2.0;
        double compCenterVirtY = compVirtY + compVirtH / 2.0;
        double canvasCenterX = canvasX + canvasWidth / 2.0;
        double canvasCenterY = canvasY + canvasHeight / 2.0;

        double baseScreenX = canvasX + (canvasWidth - virtWidth * zoom) / 2.0;
        double baseScreenY = canvasY + (canvasHeight - virtHeight * zoom) / 2.0;

        this.panX = canvasCenterX - (baseScreenX + compCenterVirtX * zoom);
        this.panY = canvasCenterY - (baseScreenY + compCenterVirtY * zoom);
    }

    public void startPan(double mouseX, double mouseY) {
        this.isPanning = true;
        this.panStartMouseX = mouseX;
        this.panStartMouseY = mouseY;
        this.panStartPanX = panX;
        this.panStartPanY = panY;
    }

    public void updatePan(double mouseX, double mouseY) {
        if (!isPanning) return;
        this.panX = panStartPanX + (mouseX - panStartMouseX);
        this.panY = panStartPanY + (mouseY - panStartMouseY);
    }

    public void endPan() {
        this.isPanning = false;
    }

    public boolean isPanning() {
        return isPanning;
    }

    public double getBaseOriginX() {
        return canvasX + (canvasWidth - virtWidth * zoom) / 2.0 + panX;
    }

    public double getBaseOriginY() {
        return canvasY + (canvasHeight - virtHeight * zoom) / 2.0 + panY;
    }

    public int virtualToScreenX(double virtX) {
        return (int) Math.round(getBaseOriginX() + virtX * zoom);
    }

    public int virtualToScreenY(double virtY) {
        return (int) Math.round(getBaseOriginY() + virtY * zoom);
    }

    public int screenToVirtualX(double screenX) {
        return (int) Math.round((screenX - getBaseOriginX()) / zoom);
    }

    public int screenToVirtualY(double screenY) {
        return (int) Math.round((screenY - getBaseOriginY()) / zoom);
    }

    public int virtualToScreenWidth(double width) {
        return (int) Math.round(width * zoom);
    }

    public int virtualToScreenHeight(double height) {
        return (int) Math.round(height * zoom);
    }

    public int screenToVirtualWidth(double width) {
        return (int) Math.round(width / zoom);
    }

    public int screenToVirtualHeight(double height) {
        return (int) Math.round(height / zoom);
    }

    public boolean isPointInCanvas(double screenX, double screenY) {
        return screenX >= canvasX && screenX <= canvasX + canvasWidth &&
                screenY >= canvasY && screenY <= canvasY + canvasHeight;
    }

    public boolean isPointInVirtualScreen(double screenX, double screenY) {
        int vx = screenToVirtualX(screenX);
        int vy = screenToVirtualY(screenY);
        return vx >= 0 && vx <= virtWidth && vy >= 0 && vy <= virtHeight;
    }

    public String getZoomPercentString() {
        return String.format("%d%%", (int) Math.round(zoom * 100));
    }

    public int getCanvasX() {
        return canvasX;
    }

    public int getCanvasY() {
        return canvasY;
    }

    public int getCanvasWidth() {
        return canvasWidth;
    }

    public int getCanvasHeight() {
        return canvasHeight;
    }

    public int getVirtWidth() {
        return virtWidth;
    }

    public int getVirtHeight() {
        return virtHeight;
    }
}
