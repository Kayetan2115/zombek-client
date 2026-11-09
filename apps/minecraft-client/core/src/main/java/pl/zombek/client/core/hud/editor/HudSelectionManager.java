package pl.zombek.client.core.hud.editor;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Handles selection state and click disambiguation for HUD components on the canvas and list.
 */
public class HudSelectionManager {
    private static final HudSelectionManager INSTANCE = new HudSelectionManager();

    private String selectedComponentId = null;
    private final List<Consumer<String>> selectionListeners = new CopyOnWriteArrayList<>();

    private HudSelectionManager() {
    }

    public static HudSelectionManager getInstance() {
        return INSTANCE;
    }

    public synchronized String getSelectedComponentId() {
        return selectedComponentId;
    }

    public synchronized boolean hasSelection() {
        return selectedComponentId != null;
    }

    public synchronized boolean isSelected(String componentId) {
        if (componentId == null || selectedComponentId == null) return false;
        return selectedComponentId.equalsIgnoreCase(componentId);
    }

    public synchronized void select(String componentId) {
        if (Objects.equals(this.selectedComponentId, componentId)) {
            return;
        }
        this.selectedComponentId = componentId != null ? componentId.toLowerCase(Locale.ROOT) : null;
        notifyListeners();
    }

    public synchronized void clearSelection() {
        if (this.selectedComponentId != null) {
            this.selectedComponentId = null;
            notifyListeners();
        }
    }

    /**
     * Hit-tests screen coordinate (x, y) against components in the layout.
     * When multiple components overlap under the click point, cycles through candidates on repeated clicks.
     *
     * @param screenX      Virtual screen X coordinate
     * @param screenY      Virtual screen Y coordinate
     * @param layout       Current HUD layout
     * @param screenWidth  Virtual screen width
     * @param screenHeight Virtual screen height
     * @return true if a component was selected, false if no hit
     */
    public synchronized boolean selectAt(int screenX, int screenY, HudLayout layout, int screenWidth, int screenHeight) {
        if (layout == null) {
            clearSelection();
            return false;
        }

        List<HudComponentState> hits = new ArrayList<>();
        for (HudComponentState state : layout.getComponents().values()) {
            if (state != null && state.isVisible() && state.containsScreenPoint(screenX, screenY, screenWidth, screenHeight)) {
                hits.add(state);
            }
        }

        if (hits.isEmpty()) {
            clearSelection();
            return false;
        }

        // Sort descending by z-index so topmost is first candidate
        hits.sort((a, b) -> Integer.compare(b.getZIndex(), a.getZIndex()));

        if (hits.size() == 1) {
            select(hits.get(0).getComponentId());
            return true;
        }

        // Multiple overlapping components: cycle through candidates
        int currentIndex = -1;
        if (selectedComponentId != null) {
            for (int i = 0; i < hits.size(); i++) {
                if (hits.get(i).getComponentId().equalsIgnoreCase(selectedComponentId)) {
                    currentIndex = i;
                    break;
                }
            }
        }

        int nextIndex = (currentIndex + 1) % hits.size();
        select(hits.get(nextIndex).getComponentId());
        return true;
    }

    public void addSelectionListener(Consumer<String> listener) {
        if (listener != null) {
            selectionListeners.add(listener);
        }
    }

    public void removeSelectionListener(Consumer<String> listener) {
        if (listener != null) {
            selectionListeners.remove(listener);
        }
    }

    private void notifyListeners() {
        for (Consumer<String> listener : selectionListeners) {
            try {
                listener.accept(selectedComponentId);
            } catch (Throwable ignored) {
            }
        }
    }

    public synchronized void resetForTesting() {
        selectedComponentId = null;
        selectionListeners.clear();
    }
}
