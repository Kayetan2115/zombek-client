package pl.zombek.client.core.hud.component;

import pl.zombek.client.core.hud.HudAnchor;

import java.util.Objects;

/**
 * Common data model representing a positioned HUD element.
 * Prepared for future Drag & Drop and visual HUD Editor integration.
 */
public class HudComponentModel {
    private final String moduleId;
    private int posX;
    private int posY;
    private int width;
    private int height;
    private double scale = 1.0;
    private boolean visible = true;
    private HudAnchor anchor = HudAnchor.TOP_LEFT;
    private int zIndex = 0;
    private int renderPriority = 0;
    private HudComponentStyle style = HudComponentStyle.CLASSIC;

    public HudComponentModel(String moduleId, int defaultWidth, int defaultHeight, HudAnchor defaultAnchor) {
        this.moduleId = Objects.requireNonNull(moduleId, "Module ID cannot be null");
        this.width = defaultWidth;
        this.height = defaultHeight;
        this.anchor = defaultAnchor != null ? defaultAnchor : HudAnchor.TOP_LEFT;
    }

    public String getModuleId() {
        return moduleId;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = Math.max(10, width);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = Math.max(10, height);
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = Math.max(0.2, Math.min(3.0, scale));
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public HudAnchor getAnchor() {
        return anchor;
    }

    public void setAnchor(HudAnchor anchor) {
        if (anchor != null) {
            this.anchor = anchor;
        }
    }

    public int getZIndex() {
        return zIndex;
    }

    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public int getRenderPriority() {
        return renderPriority;
    }

    public void setRenderPriority(int renderPriority) {
        this.renderPriority = renderPriority;
    }

    public HudComponentStyle getStyle() {
        return style;
    }

    public void setStyle(HudComponentStyle style) {
        if (style != null) {
            this.style = style;
        }
    }

    public int getEffectiveWidth() {
        return (int) Math.ceil(width * scale);
    }

    public int getEffectiveHeight() {
        return (int) Math.ceil(height * scale);
    }

    /**
     * Resolves the absolute screen X coordinate for this component, clamped to viewport.
     */
    public int getScreenX(int screenWidth) {
        int effW = getEffectiveWidth();
        int rawX = anchor.calculateX(posX, effW, screenWidth);
        return clamp(rawX, 0, Math.max(0, screenWidth - effW));
    }

    /**
     * Resolves the absolute screen Y coordinate for this component, clamped to viewport.
     */
    public int getScreenY(int screenHeight) {
        int effH = getEffectiveHeight();
        int rawY = anchor.calculateY(posY, effH, screenHeight);
        return clamp(rawY, 0, Math.max(0, screenHeight - effH));
    }

    /**
     * Automatically clamps offset values so the element cannot be pushed beyond screen boundaries.
     */
    public void clampToScreen(int screenWidth, int screenHeight) {
        if (screenWidth <= 0 || screenHeight <= 0) return;
        int effW = getEffectiveWidth();
        int effH = getEffectiveHeight();
        this.posX = Math.max(0, Math.min(posX, Math.max(0, screenWidth - effW)));
        this.posY = Math.max(0, Math.min(posY, Math.max(0, screenHeight - effH)));
    }

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
}
