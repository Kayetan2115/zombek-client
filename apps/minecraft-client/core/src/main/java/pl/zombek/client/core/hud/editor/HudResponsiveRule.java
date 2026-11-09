package pl.zombek.client.core.hud.editor;

import pl.zombek.client.core.hud.HudAnchor;

import java.util.Objects;

/**
 * Responsive positioning and scaling rules for a HUD component across different aspect ratios.
 */
public class HudResponsiveRule {
    private HudAnchor anchor = HudAnchor.TOP_LEFT;
    private int marginX = 10;
    private int marginY = 10;
    private boolean relativeScaling = false;
    private boolean snapToSafeArea = true;

    public HudResponsiveRule() {
    }

    public HudResponsiveRule(HudAnchor anchor, int marginX, int marginY) {
        this.anchor = Objects.requireNonNull(anchor, "anchor cannot be null");
        this.marginX = marginX;
        this.marginY = marginY;
    }

    public HudResponsiveRule(HudResponsiveRule other) {
        if (other != null) {
            this.anchor = other.anchor != null ? other.anchor : HudAnchor.TOP_LEFT;
            this.marginX = other.marginX;
            this.marginY = other.marginY;
            this.relativeScaling = other.relativeScaling;
            this.snapToSafeArea = other.snapToSafeArea;
        }
    }

    public HudAnchor getAnchor() {
        return anchor;
    }

    public void setAnchor(HudAnchor anchor) {
        this.anchor = anchor != null ? anchor : HudAnchor.TOP_LEFT;
    }

    public int getMarginX() {
        return marginX;
    }

    public void setMarginX(int marginX) {
        this.marginX = marginX;
    }

    public int getMarginY() {
        return marginY;
    }

    public void setMarginY(int marginY) {
        this.marginY = marginY;
    }

    public boolean isRelativeScaling() {
        return relativeScaling;
    }

    public void setRelativeScaling(boolean relativeScaling) {
        this.relativeScaling = relativeScaling;
    }

    public boolean isSnapToSafeArea() {
        return snapToSafeArea;
    }

    public void setSnapToSafeArea(boolean snapToSafeArea) {
        this.snapToSafeArea = snapToSafeArea;
    }
}
