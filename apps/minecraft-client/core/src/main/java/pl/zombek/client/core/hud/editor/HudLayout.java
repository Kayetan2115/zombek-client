package pl.zombek.client.core.hud.editor;

import java.util.*;

/**
 * Data container representing a complete layout profile of HUD components.
 */
public class HudLayout {
    private int schemaVersion = 1;
    private String layoutId = "default";
    private String name = "Domyślny Układ";
    private int refWidth = 960;
    private int refHeight = 540;
    private int refGuiScale = 2;
    private String description = "Układ elementów interfejsu HUD";
    private int version = 1;
    private long createdAt = System.currentTimeMillis();
    private long updatedAt = System.currentTimeMillis();
    private Map<String, HudComponentState> components = new LinkedHashMap<>();
    private Map<String, HudGroup> groups = new LinkedHashMap<>();
    private Map<String, HudLayer> layers = new LinkedHashMap<>();

    public HudLayout() {
        initDefaultLayers();
    }

    public HudLayout(String layoutId, String name) {
        this.layoutId = Objects.requireNonNull(layoutId, "layoutId cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        initDefaultLayers();
    }

    private void initDefaultLayers() {
        layers.put(HudLayer.LAYER_BACKGROUND, new HudLayer(HudLayer.LAYER_BACKGROUND, "Tło", 0));
        layers.put(HudLayer.LAYER_MAIN_HUD, new HudLayer(HudLayer.LAYER_MAIN_HUD, "Główny HUD", 100));
        layers.put(HudLayer.LAYER_INFORMATION, new HudLayer(HudLayer.LAYER_INFORMATION, "Informacje", 200));
        layers.put(HudLayer.LAYER_NOTIFICATIONS, new HudLayer(HudLayer.LAYER_NOTIFICATIONS, "Powiadomienia", 300));
    }

    /**
     * Deep clones the layout for safe in-editor staging.
     */
    public HudLayout cloneLayout() {
        HudLayout clone = new HudLayout(this.layoutId, this.name);
        clone.schemaVersion = this.schemaVersion;
        clone.version = this.version;
        clone.description = this.description;
        clone.refWidth = this.refWidth;
        clone.refHeight = this.refHeight;
        clone.refGuiScale = this.refGuiScale;
        clone.createdAt = this.createdAt;
        clone.updatedAt = this.updatedAt;

        for (Map.Entry<String, HudComponentState> entry : this.components.entrySet()) {
            clone.components.put(entry.getKey(), new HudComponentState(entry.getValue()));
        }
        for (Map.Entry<String, HudGroup> entry : this.groups.entrySet()) {
            clone.groups.put(entry.getKey(), new HudGroup(entry.getValue()));
        }
        for (Map.Entry<String, HudLayer> entry : this.layers.entrySet()) {
            clone.layers.put(entry.getKey(), new HudLayer(entry.getValue()));
        }
        return clone;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getLayoutId() {
        return layoutId;
    }

    public void setLayoutId(String layoutId) {
        this.layoutId = layoutId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRefWidth() {
        return refWidth;
    }

    public void setRefWidth(int refWidth) {
        this.refWidth = refWidth;
    }

    public int getRefHeight() {
        return refHeight;
    }

    public void setRefHeight(int refHeight) {
        this.refHeight = refHeight;
    }

    public int getRefGuiScale() {
        return refGuiScale;
    }

    public void setRefGuiScale(int refGuiScale) {
        this.refGuiScale = refGuiScale;
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

    public Map<String, HudComponentState> getComponents() {
        return components;
    }

    public void setComponents(Map<String, HudComponentState> components) {
        this.components = components != null ? components : new LinkedHashMap<>();
    }

    public HudComponentState getComponentState(String id) {
        if (id == null) return null;
        return components.get(id.toLowerCase(Locale.ROOT));
    }

    public void setComponentState(HudComponentState state) {
        if (state != null && state.getComponentId() != null) {
            components.put(state.getComponentId().toLowerCase(Locale.ROOT), state);
            this.updatedAt = System.currentTimeMillis();
        }
    }

    public boolean hasComponent(String id) {
        if (id == null) return false;
        return components.containsKey(id.toLowerCase(Locale.ROOT));
    }

    public void removeComponent(String id) {
        if (id != null) {
            components.remove(id.toLowerCase(Locale.ROOT));
            this.updatedAt = System.currentTimeMillis();
        }
    }

    public String getDescription() {
        return description != null ? description : "";
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

    public Map<String, HudGroup> getGroups() {
        if (groups == null) groups = new LinkedHashMap<>();
        return groups;
    }

    public void setGroups(Map<String, HudGroup> groups) {
        this.groups = groups != null ? groups : new LinkedHashMap<>();
    }

    public HudGroup getGroup(String groupId) {
        if (groupId == null || groups == null) return null;
        return groups.get(groupId);
    }

    public void addGroup(HudGroup group) {
        if (group != null && group.getGroupId() != null) {
            getGroups().put(group.getGroupId(), group);
            this.updatedAt = System.currentTimeMillis();
        }
    }

    public void removeGroup(String groupId) {
        if (groupId != null && groups != null) {
            groups.remove(groupId);
            this.updatedAt = System.currentTimeMillis();
        }
    }

    public Map<String, HudLayer> getLayers() {
        if (layers == null) {
            layers = new LinkedHashMap<>();
            initDefaultLayers();
        }
        return layers;
    }

    public void setLayers(Map<String, HudLayer> layers) {
        this.layers = layers != null ? layers : new LinkedHashMap<>();
    }

    public HudLayer getLayer(String layerId) {
        if (layerId == null) return null;
        return getLayers().get(layerId);
    }

    public void addLayer(HudLayer layer) {
        if (layer != null && layer.getLayerId() != null) {
            getLayers().put(layer.getLayerId(), layer);
            this.updatedAt = System.currentTimeMillis();
        }
    }

    public void removeLayer(String layerId) {
        if (layerId != null && layers != null) {
            layers.remove(layerId);
            this.updatedAt = System.currentTimeMillis();
        }
    }
}
