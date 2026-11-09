package pl.zombek.client.core.hud.editor;

import pl.zombek.client.core.hud.HudAnchor;

/**
 * Command recording anchor and position adjustments for a component.
 */
public class ChangeAnchorCommand implements IHudCommand {
    private final String componentId;
    private final HudAnchor oldAnchor;
    private final int oldX;
    private final int oldY;
    private final HudAnchor newAnchor;
    private final int newX;
    private final int newY;
    private final String description;
    private final long timestamp = System.currentTimeMillis();

    public ChangeAnchorCommand(String componentId, HudAnchor oldAnchor, int oldX, int oldY,
                               HudAnchor newAnchor, int newX, int newY) {
        this.componentId = componentId;
        this.oldAnchor = oldAnchor;
        this.oldX = oldX;
        this.oldY = oldY;
        this.newAnchor = newAnchor;
        this.newX = newX;
        this.newY = newY;
        this.description = "Zmieniono kotwicę: " + componentId;
    }

    public ChangeAnchorCommand(String componentId, HudAnchor oldAnchor, HudAnchor newAnchor, HudLayout layout) {
        this(componentId, oldAnchor,
                (layout != null && layout.getComponentState(componentId) != null) ? layout.getComponentState(componentId).getPosX() : 0,
                (layout != null && layout.getComponentState(componentId) != null) ? layout.getComponentState(componentId).getPosY() : 0,
                newAnchor,
                (layout != null && layout.getComponentState(componentId) != null) ? layout.getComponentState(componentId).getPosX() : 0,
                (layout != null && layout.getComponentState(componentId) != null) ? layout.getComponentState(componentId).getPosY() : 0);
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
            state.setAnchor(oldAnchor);
            state.setPosX(oldX);
            state.setPosY(oldY);
        }
    }

    @Override
    public void redo(HudLayout layout) {
        if (layout == null) return;
        HudComponentState state = layout.getComponentState(componentId);
        if (state != null) {
            state.setAnchor(newAnchor);
            state.setPosX(newX);
            state.setPosY(newY);
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

    public HudAnchor getOldAnchor() {
        return oldAnchor;
    }

    public HudAnchor getNewAnchor() {
        return newAnchor;
    }
}
