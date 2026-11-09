package pl.zombek.client.core.hud.editor;

import pl.zombek.client.core.hud.HudAnchor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Mutable state and layout properties for a single HUD component inside a HudLayout.
 */
public class HudComponentState {
    private String componentId;
    private boolean enabled = true;
    private boolean visible = true;
    private boolean editorVisible = true;
    private boolean locked = false;
    private boolean lockAspectRatio = false;
    private boolean autoSize = false;
    private HudPositionMode positionMode = HudPositionMode.ABSOLUTE_PIXEL;
    private int posX = 10;
    private int posY = 10;
    private int width = 100;
    private int height = 30;
    private double scale = 1.0;
    private HudAnchor anchor = HudAnchor.TOP_LEFT;
    private int zIndex = 0;
    private String layerId = HudLayer.LAYER_MAIN_HUD;
    private String groupId = null;
    private HudResponsiveRule responsiveRule = null;
    private Map<String, String> customProps = new HashMap<>();

    public HudComponentState() {
    }

    public HudComponentState(String componentId) {
        this.componentId = Objects.requireNonNull(componentId, "componentId cannot be null");
    }

    public HudComponentState(String componentId, int posX, int posY, int width, int height) {
        this.componentId = Objects.requireNonNull(componentId, "componentId cannot be null");
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
    }

    /**
     * Copy constructor for deep cloning during editing/staging.
     */
    public HudComponentState(HudComponentState other) {
        if (other != null) {
            this.componentId = other.componentId;
            this.enabled = other.enabled;
            this.visible = other.visible;
            this.editorVisible = other.editorVisible;
            this.locked = other.locked;
            this.lockAspectRatio = other.lockAspectRatio;
            this.autoSize = other.autoSize;
            this.positionMode = other.positionMode != null ? other.positionMode : HudPositionMode.ABSOLUTE_PIXEL;
            this.posX = other.posX;
            this.posY = other.posY;
            this.width = other.width;
            this.height = other.height;
            this.scale = other.scale;
            this.anchor = other.anchor != null ? other.anchor : HudAnchor.TOP_LEFT;
            this.zIndex = other.zIndex;
            this.layerId = other.layerId != null ? other.layerId : HudLayer.LAYER_MAIN_HUD;
            this.groupId = other.groupId;
            this.responsiveRule = other.responsiveRule != null ? new HudResponsiveRule(other.responsiveRule) : null;
            if (other.customProps != null) {
                this.customProps = new HashMap<>(other.customProps);
            }
        }
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isEditorVisible() {
        return editorVisible;
    }

    public void setEditorVisible(boolean editorVisible) {
        this.editorVisible = editorVisible;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isLockAspectRatio() {
        return lockAspectRatio;
    }

    public void setLockAspectRatio(boolean lockAspectRatio) {
        this.lockAspectRatio = lockAspectRatio;
    }

    public boolean isAutoSize() {
        return autoSize;
    }

    public void setAutoSize(boolean autoSize) {
        this.autoSize = autoSize;
    }

    public HudPositionMode getPositionMode() {
        return positionMode != null ? positionMode : HudPositionMode.ABSOLUTE_PIXEL;
    }

    public void setPositionMode(HudPositionMode positionMode) {
        this.positionMode = positionMode != null ? positionMode : HudPositionMode.ABSOLUTE_PIXEL;
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

    public HudAnchor getAnchor() {
        return anchor;
    }

    public void setAnchor(HudAnchor anchor) {
        this.anchor = anchor != null ? anchor : HudAnchor.TOP_LEFT;
    }

    public int getZIndex() {
        return zIndex;
    }

    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public String getLayerId() {
        return layerId != null ? layerId : HudLayer.LAYER_MAIN_HUD;
    }

    public void setLayerId(String layerId) {
        this.layerId = layerId != null ? layerId : HudLayer.LAYER_MAIN_HUD;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public HudResponsiveRule getResponsiveRule() {
        return responsiveRule;
    }

    public void setResponsiveRule(HudResponsiveRule responsiveRule) {
        this.responsiveRule = responsiveRule;
    }

    public Map<String, String> getCustomProps() {
        return customProps;
    }

    public void setCustomProps(Map<String, String> customProps) {
        this.customProps = customProps != null ? customProps : new HashMap<>();
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
    public int calculateScreenX(int screenWidth) {
        if (anchor == null) return posX;
        int effW = getEffectiveWidth();
        int rawX = anchor.calculateX(posX, effW, screenWidth);
        return Math.max(0, Math.min(rawX, Math.max(0, screenWidth - effW)));
    }

    /**
     * Resolves the un-clamped raw screen X coordinate for this component based on anchor and offset.
     */
    public int calculateRawScreenX(int screenWidth) {
        if (anchor == null) return posX;
        return anchor.calculateRawX(posX, getEffectiveWidth(), screenWidth);
    }

    /**
     * Resolves the absolute screen Y coordinate for this component, clamped to viewport.
     */
    public int calculateScreenY(int screenHeight) {
        if (anchor == null) return posY;
        int effH = getEffectiveHeight();
        int rawY = anchor.calculateY(posY, effH, screenHeight);
        return Math.max(0, Math.min(rawY, Math.max(0, screenHeight - effH)));
    }

    /**
     * Resolves the un-clamped raw screen Y coordinate for this component based on anchor and offset.
     */
    public int calculateRawScreenY(int screenHeight) {
        if (anchor == null) return posY;
        return anchor.calculateRawY(posY, getEffectiveHeight(), screenHeight);
    }

    /**
     * Checks whether point (x, y) falls inside the rendered screen bounding box.
     */
    public boolean containsScreenPoint(int x, int y, int screenWidth, int screenHeight) {
        int sx = calculateScreenX(screenWidth);
        int sy = calculateScreenY(screenHeight);
        int sw = getEffectiveWidth();
        int sh = getEffectiveHeight();
        return x >= sx && x <= sx + sw && y >= sy && y <= sy + sh;
    }

    /**
     * Automatically bounds offset values to keep the element fully within screen boundaries.
     */
    public void clampToScreen(int screenWidth, int screenHeight) {
        if (screenWidth <= 0 || screenHeight <= 0) return;
        int effW = getEffectiveWidth();
        int effH = getEffectiveHeight();
        this.posX = Math.max(0, Math.min(posX, Math.max(0, screenWidth - effW)));
        this.posY = Math.max(0, Math.min(posY, Math.max(0, screenHeight - effH)));
    }
}
