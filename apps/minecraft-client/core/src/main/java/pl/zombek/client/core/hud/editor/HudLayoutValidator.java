package pl.zombek.client.core.hud.editor;

import pl.zombek.client.core.hud.HudAnchor;

import java.util.Locale;
import java.util.Map;

/**
 * Validates and repairs HUD layout data to prevent crashes or corrupt states.
 */
public class HudLayoutValidator {

    /**
     * Checks if a layout meets core validity invariants.
     */
    public static boolean isValid(HudLayout layout) {
        if (layout == null) return false;
        if (layout.getLayoutId() == null || layout.getLayoutId().trim().isEmpty()) return false;
        if (layout.getComponents() == null) return false;

        for (Map.Entry<String, HudComponentState> entry : layout.getComponents().entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) return false;
            HudComponentState state = entry.getValue();
            if (state.getComponentId() == null) return false;
            if (state.getWidth() < 5 || state.getHeight() < 5) return false;
            if (state.getScale() < 0.1 || state.getScale() > 5.0) return false;
        }
        return true;
    }

    /**
     * Repairs an incomplete or corrupted layout by repairing values and populating missing components
     * from registered descriptors.
     */
    public static HudLayout repairLayout(HudLayout layout) {
        if (layout == null) {
            layout = new HudLayout("default", "Domyślny Układ");
        }
        if (layout.getLayoutId() == null || layout.getLayoutId().trim().isEmpty()) {
            layout.setLayoutId("default");
        }
        if (layout.getName() == null || layout.getName().trim().isEmpty()) {
            layout.setName("Domyślny Układ");
        }
        if (layout.getComponents() == null) {
            layout.setComponents(new java.util.LinkedHashMap<>());
        }
        if (layout.getGroups() == null) {
            layout.setGroups(new java.util.LinkedHashMap<>());
        }
        if (layout.getLayers() == null || layout.getLayers().isEmpty()) {
            layout.setLayers(new java.util.LinkedHashMap<>());
            layout.addLayer(new HudLayer(HudLayer.LAYER_BACKGROUND, "Tło", 0));
            layout.addLayer(new HudLayer(HudLayer.LAYER_MAIN_HUD, "Główny HUD", 100));
            layout.addLayer(new HudLayer(HudLayer.LAYER_INFORMATION, "Informacje", 200));
            layout.addLayer(new HudLayer(HudLayer.LAYER_NOTIFICATIONS, "Powiadomienia", 300));
        }

        // Validate and sanitize existing components
        for (Map.Entry<String, HudComponentState> entry : layout.getComponents().entrySet()) {
            HudComponentState state = entry.getValue();
            if (state != null) {
                if (state.getComponentId() == null) {
                    state.setComponentId(entry.getKey());
                }
                if (state.getAnchor() == null) {
                    state.setAnchor(HudAnchor.TOP_LEFT);
                }
                if (state.getWidth() < 10) {
                    state.setWidth(10);
                }
                if (state.getHeight() < 10) {
                    state.setHeight(10);
                }
                if (state.getScale() < 0.2 || state.getScale() > 3.0) {
                    state.setScale(Math.max(0.2, Math.min(3.0, state.getScale())));
                }
                if (state.getPosX() < 0) {
                    state.setPosX(0);
                }
                if (state.getPosY() < 0) {
                    state.setPosY(0);
                }
                if (state.getPositionMode() == null) {
                    state.setPositionMode(HudPositionMode.ABSOLUTE_PIXEL);
                }
                if (state.getLayerId() == null) {
                    state.setLayerId(HudLayer.LAYER_MAIN_HUD);
                }
            }
        }

        // Populate any missing components from registry
        HudComponentRegistry registry = HudComponentRegistry.getInstance();
        for (HudComponentDescriptor descriptor : registry.getAllDescriptors()) {
            String idKey = descriptor.getId().toLowerCase(Locale.ROOT);
            if (!layout.getComponents().containsKey(idKey)) {
                layout.setComponentState(descriptor.createDefaultState());
            }
        }

        return layout;
    }
}
