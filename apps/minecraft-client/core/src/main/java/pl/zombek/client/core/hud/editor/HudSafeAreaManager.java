package pl.zombek.client.core.hud.editor;

import pl.zombek.client.core.hud.IHudRenderer;

/**
 * Manages screen safe area margins and guide overlay rendering in the HUD Editor.
 */
public class HudSafeAreaManager {

    private boolean enabled = false;
    private int marginX = 10;
    private int marginY = 10;
    private int guideColor = 0x66FF5555;

    public void renderGuides(IHudRenderer renderer, HudCanvasViewport viewport) {
        if (!enabled || renderer == null || viewport == null) return;

        int virtW = viewport.getVirtWidth();
        int virtH = viewport.getVirtHeight();

        int screenX1 = viewport.virtualToScreenX(marginX);
        int screenY1 = viewport.virtualToScreenY(marginY);
        int screenX2 = viewport.virtualToScreenX(virtW - marginX);
        int screenY2 = viewport.virtualToScreenY(virtH - marginY);

        int w = screenX2 - screenX1;
        int h = screenY2 - screenY1;

        if (w > 0 && h > 0) {
            renderer.drawBorder(screenX1, screenY1, w, h, guideColor, 1);
            renderer.drawText("SAFE AREA", screenX1 + 4, screenY1 + 4, 0xAAFF7777, false, 0.7f, false);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMarginX() {
        return marginX;
    }

    public void setMarginX(int marginX) {
        this.marginX = Math.max(0, marginX);
    }

    public int getMarginY() {
        return marginY;
    }

    public void setMarginY(int marginY) {
        this.marginY = Math.max(0, marginY);
    }

    public int getGuideColor() {
        return guideColor;
    }

    public void setGuideColor(int guideColor) {
        this.guideColor = guideColor;
    }
}
