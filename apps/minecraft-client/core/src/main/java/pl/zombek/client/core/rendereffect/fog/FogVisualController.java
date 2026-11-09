package pl.zombek.client.core.rendereffect.fog;

/**
 * Central controller coordinating fog parameters, smooth transitions, dimension filtering,
 * and platform renderer integration.
 */
public class FogVisualController {

    private static final FogVisualController INSTANCE = new FogVisualController();

    private boolean enabled = false;
    private final FogDistanceController distanceController;
    private final FogColorController colorController;
    private final FogEnvironmentManager environmentManager;
    private final FogProfileManager profileManager;
    private final FogParameterManager parameterManager;
    private IFogRendererAdapter rendererAdapter;

    // Dimension filtering
    private boolean enableInOverworld = true;
    private boolean enableInNether = true;
    private boolean enableInEnd = true;
    private boolean enableInCustomDimensions = true;

    // Smooth transitions
    private boolean smoothTransition = true;
    private long transitionDurationMs = 400L;
    private long animationStartTime = 0L;
    private float animatedStartDistance = 16.0f;
    private float animatedEndDistance = 128.0f;
    private float targetStartDistance = 16.0f;
    private float targetEndDistance = 128.0f;

    private FogVisualController() {
        this.distanceController = new FogDistanceController();
        this.colorController = new FogColorController();
        this.environmentManager = new FogEnvironmentManager();
        this.profileManager = new FogProfileManager();
        this.parameterManager = new FogParameterManager(distanceController, colorController, environmentManager, profileManager);
    }

    public static FogVisualController getInstance() {
        return INSTANCE;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isActive() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            triggerTransitionReset();
        }
    }

    public void setActive(boolean active) {
        setEnabled(active);
    }

    public boolean shouldApplyInDimension(String dimensionKey) {
        if (!enabled) return false;
        if (dimensionKey == null) return enableInOverworld;

        String lower = dimensionKey.toLowerCase();
        if (lower.contains("overworld")) {
            return enableInOverworld;
        } else if (lower.contains("the_nether") || lower.contains("nether")) {
            return enableInNether;
        } else if (lower.contains("the_end") || lower.contains("end")) {
            return enableInEnd;
        }
        return enableInCustomDimensions;
    }

    /**
     * Resolves effective fog parameters for the given frame and environment.
     */
    public FogParameters computeFogParameters(
            float vanillaStart, float vanillaEnd,
            float vanillaR, float vanillaG, float vanillaB, float vanillaA,
            String dimensionId, FogEnvironmentType envType
    ) {
        boolean active = enabled && shouldApplyInDimension(dimensionId);
        FogParameters params = parameterManager.resolveParameters(
                vanillaStart, vanillaEnd, vanillaR, vanillaG, vanillaB, vanillaA, dimensionId, envType, active
        );

        if (!active || !smoothTransition) {
            this.animatedStartDistance = params.startDistance();
            this.animatedEndDistance = params.endDistance();
            return params;
        }

        // Apply smooth easing to distance transitions
        updateDistanceAnimation(params.startDistance(), params.endDistance());
        return new FogParameters(
                animatedStartDistance,
                animatedEndDistance,
                params.red(),
                params.green(),
                params.blue(),
                params.alpha(),
                params.density(),
                true
        );
    }

    private void updateDistanceAnimation(float targetStart, float targetEnd) {
        if (this.targetStartDistance != targetStart || this.targetEndDistance != targetEnd) {
            this.targetStartDistance = targetStart;
            this.targetEndDistance = targetEnd;
            this.animationStartTime = System.currentTimeMillis();
        }

        long now = System.currentTimeMillis();
        long elapsed = now - animationStartTime;
        if (elapsed >= transitionDurationMs) {
            this.animatedStartDistance = targetStartDistance;
            this.animatedEndDistance = targetEndDistance;
            return;
        }

        float progress = Math.min(1.0f, Math.max(0.0f, (float) elapsed / (float) transitionDurationMs));
        // Ease-In-Out quadratic
        float eased = progress < 0.5f ? 2.0f * progress * progress : 1.0f - (float) Math.pow(-2.0f * progress + 2.0f, 2.0) / 2.0f;
        this.animatedStartDistance = animatedStartDistance + (targetStartDistance - animatedStartDistance) * eased;
        this.animatedEndDistance = animatedEndDistance + (targetEndDistance - animatedEndDistance) * eased;
    }

    private void triggerTransitionReset() {
        this.animationStartTime = 0L;
    }

    public void onClientTick() {
        // Reserved for per-tick animation stepping
    }

    public void tick() {
        onClientTick();
    }

    public void reset() {
        resetForTesting();
    }

    public FogParameters getCurrentParameters() {
        return parameterManager.resolveParameters(
                16.0f, 128.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                "minecraft:overworld", FogEnvironmentType.AIR, isEnabled()
        );
    }

    public void onWorldChange() {
        triggerTransitionReset();
    }

    public FogDistanceController getDistanceController() {
        return distanceController;
    }

    public FogColorController getColorController() {
        return colorController;
    }

    public FogEnvironmentManager getEnvironmentManager() {
        return environmentManager;
    }

    public FogProfileManager getProfileManager() {
        return profileManager;
    }

    public FogParameterManager getParameterManager() {
        return parameterManager;
    }

    public IFogRendererAdapter getRendererAdapter() {
        return rendererAdapter;
    }

    public void setRendererAdapter(IFogRendererAdapter rendererAdapter) {
        this.rendererAdapter = rendererAdapter;
    }

    public boolean isEnableInOverworld() {
        return enableInOverworld;
    }

    public void setEnableInOverworld(boolean enableInOverworld) {
        this.enableInOverworld = enableInOverworld;
    }

    public boolean isEnableInNether() {
        return enableInNether;
    }

    public void setEnableInNether(boolean enableInNether) {
        this.enableInNether = enableInNether;
    }

    public boolean isEnableInEnd() {
        return enableInEnd;
    }

    public void setEnableInEnd(boolean enableInEnd) {
        this.enableInEnd = enableInEnd;
    }

    public boolean isEnableInCustomDimensions() {
        return enableInCustomDimensions;
    }

    public void setEnableInCustomDimensions(boolean enableInCustomDimensions) {
        this.enableInCustomDimensions = enableInCustomDimensions;
    }

    public boolean isSmoothTransition() {
        return smoothTransition;
    }

    public void setSmoothTransition(boolean smoothTransition) {
        this.smoothTransition = smoothTransition;
    }

    public long getTransitionDurationMs() {
        return transitionDurationMs;
    }

    public void setTransitionDurationMs(long transitionDurationMs) {
        this.transitionDurationMs = Math.max(50L, Math.min(3000L, transitionDurationMs));
    }

    public void resetForTesting() {
        this.enabled = false;
        this.enableInOverworld = true;
        this.enableInNether = true;
        this.enableInEnd = true;
        this.enableInCustomDimensions = true;
        this.smoothTransition = true;
        this.transitionDurationMs = 400L;
        this.distanceController.setCustomFogStart(16.0f);
        this.distanceController.setCustomFogEnd(128.0f);
        this.distanceController.setFogStrength(1.0f);
        this.distanceController.setFogDensity(1.0f);
        this.colorController.setUseCustomColor(false);
        this.colorController.setColorBlend(1.0f);
        this.environmentManager.resetToDefaults();
        this.profileManager.reset();
        this.rendererAdapter = null;
        triggerTransitionReset();
    }
}
