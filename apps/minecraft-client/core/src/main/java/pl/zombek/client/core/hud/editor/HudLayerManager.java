package pl.zombek.client.core.hud.editor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manages rendering layers, Z-index ordering, forward/backward elevation,
 * and component depth sorting for the HUD Editor and in-game HUD.
 */
public class HudLayerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(HudLayerManager.class);
    private static final HudLayerManager INSTANCE = new HudLayerManager();

    private HudLayerManager() {
    }

    public static HudLayerManager getInstance() {
        return INSTANCE;
    }

    public synchronized HudLayer createLayer(HudLayout layout, String layerId, String name, int zIndexBase) {
        if (layout == null || layerId == null || name == null) return null;
        String cleanId = layerId.trim().toLowerCase(Locale.ROOT);
        if (layout.getLayers().containsKey(cleanId)) {
            LOGGER.warn("Layer '{}' already exists in layout.", cleanId);
            return null;
        }

        HudLayer layer = new HudLayer(cleanId, name.trim(), zIndexBase);
        layout.addLayer(layer);
        return layer;
    }

    public synchronized boolean renameLayer(HudLayout layout, String layerId, String newName) {
        if (layout == null || layerId == null || newName == null || newName.trim().isEmpty()) return false;
        HudLayer layer = layout.getLayer(layerId.toLowerCase(Locale.ROOT));
        if (layer == null) return false;
        layer.setName(newName.trim());
        return true;
    }

    public synchronized boolean deleteLayer(HudLayout layout, String layerId) {
        if (layout == null || layerId == null) return false;
        String cleanId = layerId.toLowerCase(Locale.ROOT);

        // System layers protection
        if (HudLayer.LAYER_BACKGROUND.equals(cleanId) ||
            HudLayer.LAYER_MAIN_HUD.equals(cleanId) ||
            HudLayer.LAYER_INFORMATION.equals(cleanId) ||
            HudLayer.LAYER_NOTIFICATIONS.equals(cleanId)) {
            LOGGER.warn("Cannot delete default system layer '{}'.", cleanId);
            return false;
        }

        // Reassign components from deleted layer to Main HUD
        for (HudComponentState state : layout.getComponents().values()) {
            if (cleanId.equals(state.getLayerId())) {
                state.setLayerId(HudLayer.LAYER_MAIN_HUD);
            }
        }
        layout.removeLayer(cleanId);
        return true;
    }

    public synchronized boolean assignComponentToLayer(HudLayout layout, String componentId, String layerId) {
        if (layout == null || componentId == null || layerId == null) return false;
        HudComponentState state = layout.getComponentState(componentId);
        HudLayer layer = layout.getLayer(layerId);
        if (state == null || layer == null) return false;

        state.setLayerId(layer.getLayerId());
        // Auto-assign Z-Index based on layer base
        state.setZIndex(layer.getZIndexBase() + (state.getZIndex() % 100));
        return true;
    }

    public synchronized void bringToFront(HudLayout layout, String componentId) {
        if (layout == null || componentId == null) return;
        HudComponentState target = layout.getComponentState(componentId);
        if (target == null) return;

        int maxZ = 0;
        for (HudComponentState state : layout.getComponents().values()) {
            if (state.getZIndex() > maxZ) {
                maxZ = state.getZIndex();
            }
        }
        target.setZIndex(maxZ + 1);
    }

    public synchronized void sendToBack(HudLayout layout, String componentId) {
        if (layout == null || componentId == null) return;
        HudComponentState target = layout.getComponentState(componentId);
        if (target == null) return;

        int minZ = Integer.MAX_VALUE;
        for (HudComponentState state : layout.getComponents().values()) {
            if (state.getZIndex() < minZ) {
                minZ = state.getZIndex();
            }
        }
        target.setZIndex(Math.max(0, minZ - 1));
    }

    public synchronized void moveForward(HudLayout layout, String componentId) {
        if (layout == null || componentId == null) return;
        HudComponentState target = layout.getComponentState(componentId);
        if (target != null) {
            target.setZIndex(target.getZIndex() + 1);
        }
    }

    public synchronized void moveBackward(HudLayout layout, String componentId) {
        if (layout == null || componentId == null) return;
        HudComponentState target = layout.getComponentState(componentId);
        if (target != null && target.getZIndex() > 0) {
            target.setZIndex(target.getZIndex() - 1);
        }
    }

    /**
     * Normalizes all Z-indices in the layout to 0, 1, 2, ... N to prevent integer overflow or drift.
     */
    public synchronized void normalizeZIndices(HudLayout layout) {
        if (layout == null) return;
        List<HudComponentState> sorted = new ArrayList<>(layout.getComponents().values());
        sorted.sort(Comparator.comparingInt(HudComponentState::getZIndex));

        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).setZIndex(i);
        }
    }

    /**
     * Returns all active layout components sorted by Z-index in ascending order for rendering.
     */
    public List<HudComponentState> getSortedComponents(HudLayout layout) {
        if (layout == null || layout.getComponents() == null) {
            return Collections.emptyList();
        }
        List<HudComponentState> list = new ArrayList<>(layout.getComponents().values());
        list.sort(Comparator.comparingInt(HudComponentState::getZIndex));
        return list;
    }
}
