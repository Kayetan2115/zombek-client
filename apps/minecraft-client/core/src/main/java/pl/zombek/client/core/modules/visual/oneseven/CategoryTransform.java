package pl.zombek.client.core.modules.visual.oneseven;

import java.util.Objects;

/**
 * Configuration and transform offset for a specific item category in first person view.
 */
public class CategoryTransform {
    private final ItemCategory category;
    private boolean enabled;
    private final TransformOffset offset;

    public CategoryTransform(ItemCategory category) {
        this(category, false, new TransformOffset());
    }

    public CategoryTransform(ItemCategory category, boolean enabled, TransformOffset offset) {
        this.category = Objects.requireNonNull(category, "category cannot be null");
        this.enabled = enabled;
        this.offset = offset != null ? offset : new TransformOffset();
    }

    public CategoryTransform copy() {
        return new CategoryTransform(category, enabled, offset.copy());
    }

    public void set(CategoryTransform other) {
        if (other != null) {
            this.enabled = other.enabled;
            this.offset.set(other.offset);
        }
    }

    public void reset() {
        this.enabled = false;
        this.offset.reset();
    }

    public ItemCategory getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public TransformOffset getOffset() {
        return offset;
    }
}
