package pl.zombek.client.core.hud.editor;

import java.util.Objects;

/**
 * Metadata descriptor for a HUD profile (without loading full layout).
 */
public class HudProfileMetadata {
    private String id;
    private String name;
    private String description;
    private int version = 1;
    private long createdAt;
    private long updatedAt;
    private int componentCount;
    private boolean systemProfile = false;

    public HudProfileMetadata() {
    }

    public HudProfileMetadata(String id, String name, String description, boolean systemProfile) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.description = description != null ? description : "";
        this.systemProfile = systemProfile;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getComponentCount() {
        return componentCount;
    }

    public void setComponentCount(int componentCount) {
        this.componentCount = componentCount;
    }

    public boolean isSystemProfile() {
        return systemProfile;
    }

    public boolean isReadOnly() {
        return systemProfile;
    }

    public void setSystemProfile(boolean systemProfile) {
        this.systemProfile = systemProfile;
    }
}
