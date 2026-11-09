package pl.zombek.client.core.hud.editor;

import java.util.Objects;

/**
 * Represents a rendering and organization layer in the HUD Editor.
 */
public class HudLayer {
    public static final String LAYER_BACKGROUND = "background";
    public static final String LAYER_MAIN_HUD = "main_hud";
    public static final String LAYER_INFORMATION = "information";
    public static final String LAYER_NOTIFICATIONS = "notifications";

    private String layerId;
    private String name;
    private int zIndexBase;
    private boolean visible = true;
    private boolean locked = false;

    public HudLayer() {
        this.layerId = LAYER_MAIN_HUD;
        this.name = "Główny HUD";
        this.zIndexBase = 100;
    }

    public HudLayer(String layerId, String name, int zIndexBase) {
        this.layerId = Objects.requireNonNull(layerId, "layerId cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.zIndexBase = zIndexBase;
    }

    public HudLayer(HudLayer other) {
        if (other != null) {
            this.layerId = other.layerId;
            this.name = other.name;
            this.zIndexBase = other.zIndexBase;
            this.visible = other.visible;
            this.locked = other.locked;
        }
    }

    public String getLayerId() {
        return layerId;
    }

    public void setLayerId(String layerId) {
        this.layerId = layerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getZIndexBase() {
        return zIndexBase;
    }

    public void setZIndexBase(int zIndexBase) {
        this.zIndexBase = zIndexBase;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HudLayer hudLayer = (HudLayer) o;
        return Objects.equals(layerId, hudLayer.layerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(layerId);
    }
}
