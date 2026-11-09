package pl.zombek.client.core.hud.editor;

/**
 * Handles proportional visual scaling of HUD components within descriptor boundaries.
 */
public class HudScaleController {

    public static final double DEFAULT_MIN_SCALE = 0.50;
    public static final double DEFAULT_MAX_SCALE = 2.50;
    public static final double DEFAULT_STEP = 0.05;

    public static boolean setScale(HudComponentState state, HudComponentDescriptor desc, double targetScale) {
        if (state == null || desc == null) return false;
        if (!desc.hasCapability(HudComponentCapability.SCALE) || state.isLocked()) return false;

        double minS = desc.getMinScale() > 0 ? desc.getMinScale() : DEFAULT_MIN_SCALE;
        double maxS = desc.getMaxScale() >= minS ? desc.getMaxScale() : DEFAULT_MAX_SCALE;

        double clamped = Math.max(minS, Math.min(maxS, targetScale));
        double rounded = Math.round(clamped * 100.0) / 100.0;
        state.setScale(rounded);
        return true;
    }

    public static boolean stepScale(HudComponentState state, HudComponentDescriptor desc, double delta) {
        if (state == null || desc == null) return false;
        return setScale(state, desc, state.getScale() + delta);
    }

    public static boolean resetScale(HudComponentState state, HudComponentDescriptor desc) {
        if (state == null || desc == null) return false;
        double defScale = desc.getDefaultScale() > 0 ? desc.getDefaultScale() : 1.0;
        return setScale(state, desc, defScale);
    }
}
