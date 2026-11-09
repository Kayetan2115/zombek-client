package pl.zombek.client.core.hud.editor;

import pl.zombek.client.core.hud.HudAnchor;

import java.util.*;

/**
 * Immutable metadata describing an editable HUD component and its visual capabilities.
 */
public class HudComponentDescriptor {
    private final String id;
    private final String displayName;
    private final String description;
    private final String category;
    private final HudComponentType type;
    private final int defaultX;
    private final int defaultY;
    private final int defaultWidth;
    private final int defaultHeight;
    private final int minWidth;
    private final int minHeight;
    private final int maxWidth;
    private final int maxHeight;
    private final double defaultScale;
    private final double minScale;
    private final double maxScale;
    private final HudAnchor defaultAnchor;
    private final int defaultZIndex;
    private final boolean defaultVisible;
    private final boolean defaultLockAspectRatio;
    private final boolean defaultAutoSize;
    private final Set<HudComponentCapability> capabilities;
    private final HudComponentPreviewProvider previewProvider;
    private final String moduleId;

    private HudComponentDescriptor(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id cannot be null");
        this.displayName = Objects.requireNonNull(builder.displayName, "displayName cannot be null");
        this.description = builder.description != null ? builder.description : "";
        this.category = builder.category != null ? builder.category : "HUD";
        this.type = builder.type != null ? builder.type : HudComponentType.SCREEN_SPACE;
        this.defaultX = builder.defaultX;
        this.defaultY = builder.defaultY;
        this.defaultWidth = Math.max(10, builder.defaultWidth);
        this.defaultHeight = Math.max(10, builder.defaultHeight);
        this.minWidth = Math.max(5, builder.minWidth);
        this.minHeight = Math.max(5, builder.minHeight);
        this.maxWidth = Math.max(this.minWidth, builder.maxWidth);
        this.maxHeight = Math.max(this.minHeight, builder.maxHeight);
        this.defaultScale = builder.defaultScale > 0 ? builder.defaultScale : 1.0;
        this.minScale = builder.minScale > 0 ? builder.minScale : 0.5;
        this.maxScale = builder.maxScale >= this.minScale ? builder.maxScale : 2.5;
        this.defaultAnchor = builder.defaultAnchor != null ? builder.defaultAnchor : HudAnchor.TOP_LEFT;
        this.defaultZIndex = builder.defaultZIndex;
        this.defaultVisible = builder.defaultVisible;
        this.defaultLockAspectRatio = builder.defaultLockAspectRatio;
        this.defaultAutoSize = builder.defaultAutoSize;
        this.capabilities = Collections.unmodifiableSet(new HashSet<>(builder.capabilities));
        this.previewProvider = builder.previewProvider != null ? builder.previewProvider : (renderer, desc, x, y, w, h, s) -> {
            renderer.fill(x, y, w, h, 0x88222222);
            renderer.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
            renderer.drawText(desc.getDisplayName(), x + 4, y + 4, 0xFFFFFFFF, true, (float) s, false);
        };
        this.moduleId = builder.moduleId != null ? builder.moduleId : builder.id;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public HudComponentType getType() {
        return type;
    }

    public int getDefaultX() {
        return defaultX;
    }

    public int getDefaultY() {
        return defaultY;
    }

    public int getDefaultWidth() {
        return defaultWidth;
    }

    public int getDefaultHeight() {
        return defaultHeight;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public double getDefaultScale() {
        return defaultScale;
    }

    public double getMinScale() {
        return minScale;
    }

    public double getMaxScale() {
        return maxScale;
    }

    public boolean isDefaultLockAspectRatio() {
        return defaultLockAspectRatio;
    }

    public boolean isDefaultAutoSize() {
        return defaultAutoSize;
    }

    public HudAnchor getDefaultAnchor() {
        return defaultAnchor;
    }

    public int getDefaultZIndex() {
        return defaultZIndex;
    }

    public boolean isDefaultVisible() {
        return defaultVisible;
    }

    public Set<HudComponentCapability> getCapabilities() {
        return capabilities;
    }

    public boolean hasCapability(HudComponentCapability capability) {
        return capabilities.contains(capability);
    }

    public HudComponentPreviewProvider getPreviewProvider() {
        return previewProvider;
    }

    public String getModuleId() {
        return moduleId;
    }

    /**
     * Instantiates a fresh HudComponentState populated with this descriptor's defaults.
     */
    public HudComponentState createDefaultState() {
        HudComponentState state = new HudComponentState(id);
        state.setPosX(defaultX);
        state.setPosY(defaultY);
        state.setWidth(defaultWidth);
        state.setHeight(defaultHeight);
        state.setScale(defaultScale);
        state.setAnchor(defaultAnchor);
        state.setZIndex(defaultZIndex);
        state.setVisible(defaultVisible);
        state.setEnabled(defaultVisible);
        state.setLockAspectRatio(defaultLockAspectRatio);
        state.setAutoSize(defaultAutoSize);
        return state;
    }

    public static Builder builder(String id, String displayName) {
        return new Builder(id, displayName);
    }

    public static class Builder {
        private final String id;
        private final String displayName;
        private String description = "";
        private String category = "HUD";
        private HudComponentType type = HudComponentType.SCREEN_SPACE;
        private int defaultX = 10;
        private int defaultY = 10;
        private int defaultWidth = 100;
        private int defaultHeight = 30;
        private int minWidth = 20;
        private int minHeight = 10;
        private int maxWidth = 1920;
        private int maxHeight = 1080;
        private double defaultScale = 1.0;
        private double minScale = 0.5;
        private double maxScale = 2.5;
        private HudAnchor defaultAnchor = HudAnchor.TOP_LEFT;
        private int defaultZIndex = 0;
        private boolean defaultVisible = true;
        private boolean defaultLockAspectRatio = false;
        private boolean defaultAutoSize = false;
        private final Set<HudComponentCapability> capabilities = EnumSet.of(
                HudComponentCapability.POSITION,
                HudComponentCapability.SCALE,
                HudComponentCapability.VISIBILITY,
                HudComponentCapability.ANCHOR
        );
        private HudComponentPreviewProvider previewProvider;
        private String moduleId;

        public Builder(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
            this.moduleId = id;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder type(HudComponentType type) {
            this.type = type;
            return this;
        }

        public Builder defaultPosition(int x, int y) {
            this.defaultX = x;
            this.defaultY = y;
            return this;
        }

        public Builder defaultSize(int width, int height) {
            this.defaultWidth = width;
            this.defaultHeight = height;
            return this;
        }

        public Builder minSize(int minW, int minH) {
            this.minWidth = minW;
            this.minHeight = minH;
            return this;
        }

        public Builder maxSize(int maxW, int maxH) {
            this.maxWidth = maxW;
            this.maxHeight = maxH;
            return this;
        }

        public Builder scaleRange(double minScale, double maxScale) {
            this.minScale = minScale;
            this.maxScale = maxScale;
            return this;
        }

        public Builder lockAspectRatio(boolean lock) {
            this.defaultLockAspectRatio = lock;
            return this;
        }

        public Builder autoSize(boolean autoSize) {
            this.defaultAutoSize = autoSize;
            return this;
        }

        public Builder defaultScale(double scale) {
            this.defaultScale = scale;
            return this;
        }

        public Builder defaultAnchor(HudAnchor anchor) {
            this.defaultAnchor = anchor;
            return this;
        }

        public Builder defaultZIndex(int zIndex) {
            this.defaultZIndex = zIndex;
            return this;
        }

        public Builder defaultVisible(boolean defaultVisible) {
            this.defaultVisible = defaultVisible;
            return this;
        }

        public Builder capabilities(HudComponentCapability... caps) {
            this.capabilities.clear();
            if (caps != null) {
                Collections.addAll(this.capabilities, caps);
            }
            return this;
        }

        public Builder previewProvider(HudComponentPreviewProvider previewProvider) {
            this.previewProvider = previewProvider;
            return this;
        }

        public Builder moduleId(String moduleId) {
            this.moduleId = moduleId;
            return this;
        }

        public HudComponentDescriptor build() {
            return new HudComponentDescriptor(this);
        }
    }
}
