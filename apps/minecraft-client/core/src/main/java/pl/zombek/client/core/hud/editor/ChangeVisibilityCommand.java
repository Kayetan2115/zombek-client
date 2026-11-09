package pl.zombek.client.core.hud.editor;

/**
 * Command recording visibility changes for a component.
 */
public class ChangeVisibilityCommand implements IHudCommand {
    private final String componentId;
    private final boolean oldVisible;
    private final boolean newVisible;
    private final String description;
    private final long timestamp = System.currentTimeMillis();

    public ChangeVisibilityCommand(String componentId, boolean oldVisible, boolean newVisible) {
        this.componentId = componentId;
        this.oldVisible = oldVisible;
        this.newVisible = newVisible;
        this.description = (newVisible ? "Włączono widoczność: " : "Ukryto: ") + componentId;
    }

    public ChangeVisibilityCommand(String componentId, boolean oldVisible, boolean newVisible, HudLayout layout) {
        this(componentId, oldVisible, newVisible);
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
            state.setVisible(oldVisible);
        }
    }

    @Override
    public void redo(HudLayout layout) {
        if (layout == null) return;
        HudComponentState state = layout.getComponentState(componentId);
        if (state != null) {
            state.setVisible(newVisible);
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

    public boolean isOldVisible() {
        return oldVisible;
    }

    public boolean isNewVisible() {
        return newVisible;
    }
}
