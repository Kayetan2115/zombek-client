package pl.zombek.client.core.hud.editor;

import java.util.*;

/**
 * Command recording a drag or alignment displacement for one or more components.
 */
public class MoveComponentsCommand implements IHudCommand {

    public static class Point {
        public final int x;
        public final int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() { return x; }
        public int getY() { return y; }
    }

    public static class ComponentPosition {
        private final String componentId;
        private final int oldX;
        private final int oldY;
        private final int newX;
        private final int newY;

        public ComponentPosition(String componentId, int oldX, int oldY, int newX, int newY) {
            this.componentId = componentId;
            this.oldX = oldX;
            this.oldY = oldY;
            this.newX = newX;
            this.newY = newY;
        }

        public String getComponentId() { return componentId; }
        public int getOldX() { return oldX; }
        public int getOldY() { return oldY; }
        public int getNewX() { return newX; }
        public int getNewY() { return newY; }
    }

    private final List<ComponentPosition> positions = new ArrayList<>();
    private final String description;
    private final long timestamp = System.currentTimeMillis();

    public MoveComponentsCommand(String description) {
        this.description = description != null ? description : "Przesunięto elementy";
    }

    public MoveComponentsCommand(Map<String, Point> oldPositions, Map<String, Point> newPositions, HudLayout layout) {
        this("Przesunięto " + (newPositions != null ? newPositions.size() : 0) + " elementów");
        if (oldPositions != null && newPositions != null) {
            for (Map.Entry<String, Point> entry : newPositions.entrySet()) {
                String id = entry.getKey();
                Point oldP = oldPositions.get(id);
                Point newP = entry.getValue();
                if (oldP != null && newP != null) {
                    record(id, oldP.x, oldP.y, newP.x, newP.y);
                }
            }
        }
    }

    public MoveComponentsCommand(Map<String, Point> oldPositions, Map<String, Point> newPositions) {
        this(oldPositions, newPositions, null);
    }

    public void record(String id, int oldX, int oldY, int newX, int newY) {
        if (id != null) {
            positions.add(new ComponentPosition(id, oldX, oldY, newX, newY));
        }
    }

    @Override
    public void execute(HudLayout layout) {
        redo(layout);
    }

    @Override
    public void undo(HudLayout layout) {
        if (layout == null) return;
        for (ComponentPosition pos : positions) {
            HudComponentState state = layout.getComponentState(pos.getComponentId());
            if (state != null) {
                state.setPosX(pos.getOldX());
                state.setPosY(pos.getOldY());
            }
        }
    }

    @Override
    public void redo(HudLayout layout) {
        if (layout == null) return;
        for (ComponentPosition pos : positions) {
            HudComponentState state = layout.getComponentState(pos.getComponentId());
            if (state != null) {
                state.setPosX(pos.getNewX());
                state.setPosY(pos.getNewY());
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

    public List<ComponentPosition> getPositions() {
        return Collections.unmodifiableList(positions);
    }
}
