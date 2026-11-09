package pl.zombek.client.core.hud.editor;

/**
 * Command recording a resize operation for a component.
 */
public class ResizeComponentCommand implements IHudCommand {
    private final String componentId;
    private final int oldX;
    private final int oldY;
    private final int oldWidth;
    private final int oldHeight;
    private final int newX;
    private final int newY;
    private final int newWidth;
    private final int newHeight;
    private final String description;
    private final long timestamp = System.currentTimeMillis();

    public ResizeComponentCommand(String componentId, int oldX, int oldY, int oldW, int oldH,
                                  int newX, int newY, int newW, int newH) {
        this.componentId = componentId;
        this.oldX = oldX;
        this.oldY = oldY;
        this.oldWidth = oldW;
        this.oldHeight = oldH;
        this.newX = newX;
        this.newY = newY;
        this.newWidth = newW;
        this.newHeight = newH;
        this.description = "Zmieniono rozmiar: " + componentId;
    }

    public ResizeComponentCommand(String componentId, int oldX, int oldY, int oldW, int oldH,
                                  int newX, int newY, int newW, int newH, HudLayout layout) {
        this(componentId, oldX, oldY, oldW, oldH, newX, newY, newW, newH);
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
            state.setPosX(oldX);
            state.setPosY(oldY);
            state.setWidth(oldWidth);
            state.setHeight(oldHeight);
        }
    }

    @Override
    public void redo(HudLayout layout) {
        if (layout == null) return;
        HudComponentState state = layout.getComponentState(componentId);
        if (state != null) {
            state.setPosX(newX);
            state.setPosY(newY);
            state.setWidth(newWidth);
            state.setHeight(newHeight);
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

    public int getOldX() {
        return oldX;
    }

    public int getOldY() {
        return oldY;
    }

    public int getOldWidth() {
        return oldWidth;
    }

    public int getOldHeight() {
        return oldHeight;
    }

    public int getNewX() {
        return newX;
    }

    public int getNewY() {
        return newY;
    }

    public int getNewWidth() {
        return newWidth;
    }

    public int getNewHeight() {
        return newHeight;
    }
}
