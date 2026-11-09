package pl.zombek.client.core.hud.editor;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a logical group of HUD components in the HUD Editor.
 */
public class HudGroup {
    private String groupId;
    private String name;
    private int color = 0xFF00E5FF; // Default Cyan accent
    private Set<String> componentIds = new LinkedHashSet<>();
    private boolean locked = false;
    private boolean visible = true;

    public HudGroup() {
    }

    public HudGroup(String groupId, String name) {
        this.groupId = Objects.requireNonNull(groupId, "groupId cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
    }

    public HudGroup(String groupId, String name, int color) {
        this(groupId, name);
        this.color = color;
    }

    public HudGroup(HudGroup other) {
        if (other != null) {
            this.groupId = other.groupId;
            this.name = other.name;
            this.color = other.color;
            this.locked = other.locked;
            this.visible = other.visible;
            if (other.componentIds != null) {
                this.componentIds = new LinkedHashSet<>(other.componentIds);
            }
        }
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public Set<String> getComponentIds() {
        return componentIds;
    }

    public void setComponentIds(Set<String> componentIds) {
        this.componentIds = componentIds != null ? componentIds : new LinkedHashSet<>();
    }

    public void addComponent(String componentId) {
        if (componentId != null) {
            this.componentIds.add(componentId.toLowerCase(java.util.Locale.ROOT));
        }
    }

    public void removeComponent(String componentId) {
        if (componentId != null) {
            this.componentIds.remove(componentId.toLowerCase(java.util.Locale.ROOT));
        }
    }

    public boolean hasComponent(String componentId) {
        if (componentId == null) return false;
        return this.componentIds.contains(componentId.toLowerCase(java.util.Locale.ROOT));
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HudGroup hudGroup = (HudGroup) o;
        return Objects.equals(groupId, hudGroup.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId);
    }
}
