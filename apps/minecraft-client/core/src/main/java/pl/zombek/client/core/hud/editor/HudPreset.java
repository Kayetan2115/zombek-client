package pl.zombek.client.core.hud.editor;

import java.util.*;

/**
 * Represents a pre-configured template layout preset that can be applied to any HUD profile.
 */
public class HudPreset {
    private String id;
    private String name;
    private String description;
    private boolean systemPreset = false;
    private int version = 1;
    private Map<String, HudComponentState> components = new LinkedHashMap<>();
    private Map<String, HudGroup> groups = new LinkedHashMap<>();
    private Map<String, HudLayer> layers = new LinkedHashMap<>();

    public HudPreset() {
    }

    public HudPreset(String id, String name, String description, boolean systemPreset) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.description = description != null ? description : "";
        this.systemPreset = systemPreset;
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

    public boolean isSystemPreset() {
        return systemPreset;
    }

    public void setSystemPreset(boolean systemPreset) {
        this.systemPreset = systemPreset;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Map<String, HudComponentState> getComponents() {
        return components;
    }

    public void setComponents(Map<String, HudComponentState> components) {
        this.components = components != null ? components : new LinkedHashMap<>();
    }

    public Map<String, HudGroup> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, HudGroup> groups) {
        this.groups = groups != null ? groups : new LinkedHashMap<>();
    }

    public Map<String, HudLayer> getLayers() {
        return layers;
    }

    public void setLayers(Map<String, HudLayer> layers) {
        this.layers = layers != null ? layers : new LinkedHashMap<>();
    }

    public void addComponent(HudComponentState state) {
        if (state != null && state.getComponentId() != null) {
            this.components.put(state.getComponentId().toLowerCase(Locale.ROOT), state);
        }
    }
}
