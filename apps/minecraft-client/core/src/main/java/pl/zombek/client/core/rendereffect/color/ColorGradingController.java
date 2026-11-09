package pl.zombek.client.core.rendereffect.color;

/**
 * Central controller for color saturation, grading, and post-processing adjustments.
 * Supports smooth transitions between color profiles and selective target application.
 */
public class ColorGradingController {
    private static final ColorGradingController INSTANCE = new ColorGradingController();

    private boolean active = false;
    private ColorSettings currentSettings = ColorSettings.vanilla();
    private ColorSettings targetSettings = ColorSettings.vanilla();
    private ColorSettings startSettings = ColorSettings.vanilla();

    private boolean transitioning = false;
    private long transitionStartTime = 0;
    private long transitionDurationMs = 300;

    private IColorGradingAdapter adapter;

    private ColorGradingController() {
    }

    public static ColorGradingController getInstance() {
        return INSTANCE;
    }

    public void setAdapter(IColorGradingAdapter adapter) {
        this.adapter = adapter;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        if (!active) {
            reset();
        }
    }

    public synchronized void applySettings(ColorSettings newSettings, boolean smooth, long durationMs) {
        if (!smooth || durationMs <= 0) {
            this.currentSettings = newSettings;
            this.targetSettings = newSettings;
            this.transitioning = false;
            notifyAdapter();
            return;
        }

        this.startSettings = this.currentSettings;
        this.targetSettings = newSettings;
        this.transitionDurationMs = durationMs;
        this.transitionStartTime = System.currentTimeMillis();
        this.transitioning = true;
    }

    public synchronized void tick() {
        if (!transitioning) {
            return;
        }

        long elapsed = System.currentTimeMillis() - transitionStartTime;
        if (elapsed >= transitionDurationMs) {
            this.currentSettings = this.targetSettings;
            this.transitioning = false;
            notifyAdapter();
            return;
        }

        float progress = (float) elapsed / (float) transitionDurationMs;
        // Ease in-out cubic
        float eased = progress < 0.5f
                ? 4.0f * progress * progress * progress
                : 1.0f - (float) Math.pow(-2.0f * progress + 2.0f, 3) / 2.0f;

        this.currentSettings = interpolate(startSettings, targetSettings, eased);
        notifyAdapter();
    }

    private ColorSettings interpolate(ColorSettings a, ColorSettings b, float t) {
        return new ColorSettings(
                lerp(a.getSaturation(), b.getSaturation(), t),
                lerp(a.getVibrance(), b.getVibrance(), t),
                lerp(a.getContrast(), b.getContrast(), t),
                lerp(a.getBrightness(), b.getBrightness(), t),
                lerp(a.getHueShift(), b.getHueShift(), t),
                lerp(a.getTemperature(), b.getTemperature(), t),
                lerp(a.getRedMultiplier(), b.getRedMultiplier(), t),
                lerp(a.getGreenMultiplier(), b.getGreenMultiplier(), t),
                lerp(a.getBlueMultiplier(), b.getBlueMultiplier(), t),
                b.isAffectWorld(),
                b.isAffectEntities(),
                b.isAffectItems(),
                b.isAffectParticles(),
                b.isPreserveHud()
        );
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private void notifyAdapter() {
        if (adapter != null) {
            adapter.onColorSettingsUpdated(currentSettings);
        }
    }

    /**
     * Modulates RGB color floats if the target is active.
     */
    public void transformRgb(float r, float g, float b, TargetType target, float[] out) {
        if (!active || !currentSettings.isTargetEnabled(target)) {
            out[0] = r;
            out[1] = g;
            out[2] = b;
            return;
        }
        ColorAdjustmentPipeline.applyAdjustments(r, g, b, currentSettings, out);
    }

    /**
     * Modulates 32-bit ARGB packed color if the target is active.
     */
    public int transformPackedArgb(int argb, TargetType target) {
        if (!active || !currentSettings.isTargetEnabled(target)) {
            return argb;
        }
        return ColorAdjustmentPipeline.transformPackedArgb(argb, currentSettings);
    }

    public synchronized void reset() {
        this.active = false;
        this.currentSettings = ColorSettings.vanilla();
        this.targetSettings = ColorSettings.vanilla();
        this.startSettings = ColorSettings.vanilla();
        this.transitioning = false;
        if (adapter != null) {
            adapter.reset();
        }
    }

    public ColorSettings getCurrentSettings() {
        return currentSettings;
    }

    public boolean isTransitioning() {
        return transitioning;
    }
}
