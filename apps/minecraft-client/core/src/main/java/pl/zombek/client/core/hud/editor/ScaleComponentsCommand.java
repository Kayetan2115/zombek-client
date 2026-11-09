package pl.zombek.client.core.hud.editor;

import java.util.*;

/**
 * Command recording scale modifications for one or more components.
 */
public class ScaleComponentsCommand implements IHudCommand {
    public static class ComponentScale {
        private final String componentId;
        private final double oldScale;
        private final double newScale;

        public ComponentScale(String componentId, double oldScale, double newScale) {
            this.componentId = componentId;
            this.oldScale = oldScale;
            this.newScale = newScale;
        }

        public String getComponentId() { return componentId; }
        public double getOldScale() { return oldScale; }
        public double getNewScale() { return newScale; }
    }

    private final List<ComponentScale> scales = new ArrayList<>();
    private final String description;
    private final long timestamp = System.currentTimeMillis();

    public ScaleComponentsCommand(String description) {
        this.description = description != null ? description : "Przeskalowano elementy";
    }

    public ScaleComponentsCommand(Map<String, Double> oldScales, Map<String, Double> newScales, HudLayout layout) {
        this("Przeskalowano " + (newScales != null ? newScales.size() : 0) + " elementów");
        if (oldScales != null && newScales != null) {
            for (Map.Entry<String, Double> entry : newScales.entrySet()) {
                String id = entry.getKey();
                Double oldS = oldScales.get(id);
                Double newS = entry.getValue();
                if (oldS != null && newS != null) {
                    record(id, oldS, newS);
                }
            }
        }
    }

    public ScaleComponentsCommand(Map<String, Double> oldScales, Map<String, Double> newScales) {
        this(oldScales, newScales, null);
    }

    public void record(String id, double oldScale, double newScale) {
        if (id != null) {
            scales.add(new ComponentScale(id, oldScale, newScale));
        }
    }

    @Override
    public void execute(HudLayout layout) {
        redo(layout);
    }

    @Override
    public void undo(HudLayout layout) {
        if (layout == null) return;
        for (ComponentScale entry : scales) {
            HudComponentState state = layout.getComponentState(entry.getComponentId());
            if (state != null) {
                state.setScale(entry.getOldScale());
            }
        }
    }

    @Override
    public void redo(HudLayout layout) {
        if (layout == null) return;
        for (ComponentScale entry : scales) {
            HudComponentState state = layout.getComponentState(entry.getComponentId());
            if (state != null) {
                state.setScale(entry.getNewScale());
            }
        }
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public List<ComponentScale> getScales() {
        return Collections.unmodifiableList(scales);
    }
}
