package pl.zombek.client.core.hud.editor;

import java.util.*;

/**
 * Data container storing copied HUD components and groups in the clipboard.
 */
public class HudClipboardData {
    private final Map<String, HudComponentState> components = new LinkedHashMap<>();
    private final List<HudGroup> groups = new ArrayList<>();
    private final String sourceProfileId;
    private final long timestamp = System.currentTimeMillis();

    public HudClipboardData(String sourceProfileId) {
        this.sourceProfileId = sourceProfileId;
    }

    public void addComponent(HudComponentState state) {
        if (state != null && state.getComponentId() != null) {
            components.put(state.getComponentId(), new HudComponentState(state));
        }
    }

    public void addGroup(HudGroup group) {
        if (group != null) {
            groups.add(new HudGroup(group));
        }
    }

    public Map<String, HudComponentState> getComponents() {
        return components;
    }

    public List<HudGroup> getGroups() {
        return groups;
    }

    public String getSourceProfileId() {
        return sourceProfileId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isEmpty() {
        return components.isEmpty() && groups.isEmpty();
    }
}
