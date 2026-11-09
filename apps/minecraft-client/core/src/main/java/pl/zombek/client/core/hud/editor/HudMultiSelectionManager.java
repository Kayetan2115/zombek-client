package pl.zombek.client.core.hud.editor;

import java.util.*;

/**
 * Manages simultaneous multi-selection of HUD components with marquee (box) selection,
 * collective drag/scale operations, and mixed-property inspection.
 */
public class HudMultiSelectionManager {
    private static final HudMultiSelectionManager INSTANCE = new HudMultiSelectionManager();

    public enum MarqueeMode {
        INTERSECTING,
        FULLY_INSIDE
    }

    private final Set<String> selectedIds = new LinkedHashSet<>();
    private MarqueeMode marqueeMode = MarqueeMode.INTERSECTING;
    private boolean marqueeActive = false;
    private int marqueeStartX;
    private int marqueeStartY;
    private int marqueeCurrentX;
    private int marqueeCurrentY;

    private boolean skipLockedComponents = true;

    private final List<Runnable> selectionListeners = new ArrayList<>();

    public HudMultiSelectionManager() {
    }

    public static HudMultiSelectionManager getInstance() {
        return INSTANCE;
    }

    public synchronized void selectSingle(String id) {
        selectedIds.clear();
        if (id != null) {
            selectedIds.add(id.toLowerCase(Locale.ROOT));
        }
        notifyListeners();
    }

    public synchronized void toggleSelection(String id) {
        if (id == null) return;
        String cleanId = id.toLowerCase(Locale.ROOT);
        if (selectedIds.contains(cleanId)) {
            selectedIds.remove(cleanId);
        } else {
            selectedIds.add(cleanId);
        }
        notifyListeners();
    }

    public synchronized void addToSelection(String id) {
        if (id != null) {
            selectedIds.add(id.toLowerCase(Locale.ROOT));
            notifyListeners();
        }
    }

    public synchronized void removeFromSelection(String id) {
        if (id != null && selectedIds.remove(id.toLowerCase(Locale.ROOT))) {
            notifyListeners();
        }
    }

    public synchronized void selectAll(Collection<String> allIds) {
        selectedIds.clear();
        if (allIds != null) {
            for (String id : allIds) {
                if (id != null) selectedIds.add(id.toLowerCase(Locale.ROOT));
            }
        }
        notifyListeners();
    }

    public synchronized void clearSelection() {
        if (!selectedIds.isEmpty()) {
            selectedIds.clear();
            notifyListeners();
        }
    }

    public synchronized boolean isSelected(String id) {
        if (id == null) return false;
        return selectedIds.contains(id.toLowerCase(Locale.ROOT));
    }

    public synchronized Set<String> getSelectedIds() {
        return new LinkedHashSet<>(selectedIds);
    }

    public synchronized int getSelectedCount() {
        return selectedIds.size();
    }

    public synchronized String getPrimarySelectedId() {
        if (selectedIds.isEmpty()) return null;
        return selectedIds.iterator().next();
    }

    // --- MARQUEE SELECTION ---

    public synchronized void startMarquee(int virtualX, int virtualY) {
        this.marqueeActive = true;
        this.marqueeStartX = virtualX;
        this.marqueeStartY = virtualY;
        this.marqueeCurrentX = virtualX;
        this.marqueeCurrentY = virtualY;
    }

    public synchronized void updateMarquee(int currentVirtualX, int currentVirtualY) {
        if (this.marqueeActive) {
            this.marqueeCurrentX = currentVirtualX;
            this.marqueeCurrentY = currentVirtualY;
        }
    }

    public synchronized void endMarquee(HudLayout layout, int screenW, int screenH, boolean addToSelection) {
        if (!marqueeActive) return;
        this.marqueeActive = false;

        int boxX = Math.min(marqueeStartX, marqueeCurrentX);
        int boxY = Math.min(marqueeStartY, marqueeCurrentY);
        int boxW = Math.abs(marqueeCurrentX - marqueeStartX);
        int boxH = Math.abs(marqueeCurrentY - marqueeStartY);

        // Click on canvas without drag clears selection unless holding shift
        if (boxW < 4 && boxH < 4) {
            if (!addToSelection) {
                clearSelection();
            }
            return;
        }

        if (!addToSelection) {
            selectedIds.clear();
        }

        if (layout != null) {
            for (HudComponentState state : layout.getComponents().values()) {
                if (!state.isVisible()) continue;
                int sx = state.calculateScreenX(screenW);
                int sy = state.calculateScreenY(screenH);
                int sw = state.getEffectiveWidth();
                int sh = state.getEffectiveHeight();

                boolean match;
                if (marqueeMode == MarqueeMode.FULLY_INSIDE) {
                    match = (sx >= boxX && sy >= boxY && (sx + sw) <= (boxX + boxW) && (sy + sh) <= (boxY + boxH));
                } else {
                    // Intersecting
                    match = !(sx + sw < boxX || sx > boxX + boxW || sy + sh < boxY || sy > boxY + boxH);
                }

                if (match) {
                    selectedIds.add(state.getComponentId().toLowerCase(Locale.ROOT));
                }
            }
        }
        notifyListeners();
    }

    public boolean isMarqueeActive() {
        return marqueeActive;
    }

    public int getMarqueeBoxX() {
        return Math.min(marqueeStartX, marqueeCurrentX);
    }

    public int getMarqueeBoxY() {
        return Math.min(marqueeStartY, marqueeCurrentY);
    }

    public int getMarqueeBoxW() {
        return Math.abs(marqueeCurrentX - marqueeStartX);
    }

    public int getMarqueeBoxH() {
        return Math.abs(marqueeCurrentY - marqueeStartY);
    }

    public MarqueeMode getMarqueeMode() {
        return marqueeMode;
    }

    public void setMarqueeMode(MarqueeMode marqueeMode) {
        this.marqueeMode = marqueeMode != null ? marqueeMode : MarqueeMode.INTERSECTING;
    }

    public boolean isSkipLockedComponents() {
        return skipLockedComponents;
    }

    public void setSkipLockedComponents(boolean skipLockedComponents) {
        this.skipLockedComponents = skipLockedComponents;
    }

    public void addListener(Runnable listener) {
        if (listener != null) selectionListeners.add(listener);
    }

    private void notifyListeners() {
        for (Runnable r : selectionListeners) {
            try {
                r.run();
            } catch (Exception ignored) {
            }
        }
    }

    public synchronized void resetForTesting() {
        selectedIds.clear();
        marqueeActive = false;
        marqueeMode = MarqueeMode.INTERSECTING;
        skipLockedComponents = true;
        selectionListeners.clear();
    }
}
