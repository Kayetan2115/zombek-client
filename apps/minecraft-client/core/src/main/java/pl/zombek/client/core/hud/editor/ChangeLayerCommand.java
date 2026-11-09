package pl.zombek.client.core.hud.editor;

/**
 * Command recording layer and Z-index adjustments.
 */
public class ChangeLayerCommand implements IHudCommand {
    private final String componentId;
    private final String oldLayerId;
    private final int oldZIndex;
    private final String newLayerId;
    private final int newZIndex;
    private final String description;
    private final long timestamp = System.currentTimeMillis();

    public ChangeLayerCommand(String componentId, String oldLayerId, int oldZIndex, String newLayerId, int newZIndex) {
        this.componentId = componentId;
        this.oldLayerId = oldLayerId;
        this.oldZIndex = oldZIndex;
        this.newLayerId = newLayerId;
        this.newZIndex = newZIndex;
        this.description = "Zmieniono warstwę: " + componentId;
    }

    public ChangeLayerCommand(String componentId, String oldLayerId, String newLayerId, HudLayout layout) {
        this(componentId, oldLayerId,
                (layout != null && layout.getComponentState(componentId) != null) ? layout.getComponentState(componentId).getZIndex() : 0,
                newLayerId,
                (layout != null && layout.getLayer(newLayerId) != null) ? layout.getLayer(newLayerId).getZIndexBase() : 100);
    }

    @Override
    public void execute(HudLayout layout) {
        redo(layout);
    }

    @Override
    public void undo(HudLayout layout) {
        if (layout == null) return;
        HudComponentState state = layout.getComponentState(componentId);
        if (state != null) {
            state.setLayerId(oldLayerId);
            state.setZIndex(oldZIndex);
        }
    }

    @Override
    public void redo(HudLayout layout) {
        if (layout == null) return;
        HudComponentState state = layout.getComponentState(componentId);
        if (state != null) {
            state.setLayerId(newLayerId);
            state.setZIndex(newZIndex);
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

    public String getComponentId() {
        return componentId;
    }

    public String getOldLayerId() {
        return oldLayerId;
    }

    public String getNewLayerId() {
        return newLayerId;
    }
}
