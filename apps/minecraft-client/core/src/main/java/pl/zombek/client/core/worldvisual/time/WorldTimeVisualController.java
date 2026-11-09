package pl.zombek.client.core.worldvisual.time;

/**
 * Coordinates local client-side sky and day time presentation.
 * Explicitly separates true world time from visual rendering override.
 */
public class WorldTimeVisualController {
    private static final WorldTimeVisualController INSTANCE = new WorldTimeVisualController();

    private boolean enabled = false;
    private TimeChangerPreset preset = TimeChangerPreset.NOON;
    private long customTimeTicks = 6000L;
    private long timeOffsetTicks = 0L;

    // Dimension filters
    private boolean enableInOverworld = true;
    private boolean enableInNether = false;
    private boolean enableInEnd = false;
    private boolean enableInCustomDimensions = false;

    // Celestial options
    private boolean changeSunPosition = true;
    private boolean changeMoonPosition = true;
    private boolean changeSkyColor = true;
    private boolean showStars = true;
    private boolean preserveVanillaSky = false;

    private final TimeTransitionManager transitionManager = new TimeTransitionManager();

    private WorldTimeVisualController() {}

    public static WorldTimeVisualController getInstance() {
        return INSTANCE;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        updateTargetTime();
    }

    public TimeChangerPreset getPreset() {
        return preset;
    }

    public void setPreset(TimeChangerPreset preset) {
        this.preset = preset != null ? preset : TimeChangerPreset.NOON;
        updateTargetTime();
    }

    public long getCustomTimeTicks() {
        return customTimeTicks;
    }

    public void setCustomTimeTicks(long customTimeTicks) {
        this.customTimeTicks = Math.floorMod(customTimeTicks, 24000L);
        if (preset == TimeChangerPreset.CUSTOM) {
            updateTargetTime();
        }
    }

    public long getTimeOffsetTicks() {
        return timeOffsetTicks;
    }

    public void setTimeOffsetTicks(long timeOffsetTicks) {
        this.timeOffsetTicks = timeOffsetTicks;
        updateTargetTime();
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

    public boolean isChangeSunPosition() {
        return changeSunPosition;
    }

    public void setChangeSunPosition(boolean changeSunPosition) {
        this.changeSunPosition = changeSunPosition;
    }

    public boolean isChangeMoonPosition() {
        return changeMoonPosition;
    }

    public void setChangeMoonPosition(boolean changeMoonPosition) {
        this.changeMoonPosition = changeMoonPosition;
    }

    public boolean isChangeSkyColor() {
        return changeSkyColor;
    }

    public void setChangeSkyColor(boolean changeSkyColor) {
        this.changeSkyColor = changeSkyColor;
    }

    public boolean isShowStars() {
        return showStars;
    }

    public void setShowStars(boolean showStars) {
        this.showStars = showStars;
    }

    public boolean isPreserveVanillaSky() {
        return preserveVanillaSky;
    }

    public void setPreserveVanillaSky(boolean preserveVanillaSky) {
        this.preserveVanillaSky = preserveVanillaSky;
    }

    public TimeTransitionManager getTransitionManager() {
        return transitionManager;
    }

    private void updateTargetTime() {
        if (!enabled || preset == TimeChangerPreset.VANILLA) {
            return;
        }
        long baseTicks = (preset == TimeChangerPreset.CUSTOM) ? customTimeTicks : preset.getTicks();
        long targetTicks = Math.floorMod(baseTicks + timeOffsetTicks, 24000L);
        transitionManager.setTargetTime(targetTicks, false);
    }

    /**
     * Determines whether visual time override should be applied for the current dimension.
     */
    public boolean shouldApplyInDimension(String dimensionKey) {
        if (!enabled || preset == TimeChangerPreset.VANILLA || preserveVanillaSky) {
            return false;
        }
        if (dimensionKey == null) {
            return enableInOverworld;
        }
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
     * Resolves the visual time of day to be used for sky and celestial rendering.
     * @param trueWorldTime real world time ticks reported by the server
     * @param dimensionKey current dimension identifier
     * @return time of day ticks for rendering
     */
    public long getVisualTimeOfDay(long trueWorldTime, String dimensionKey) {
        if (!shouldApplyInDimension(dimensionKey)) {
            return trueWorldTime;
        }
        transitionManager.update();
        return transitionManager.getCurrentVisualTicks();
    }

    /**
     * Resolves the visual sky angle for rendering.
     * @param vanillaSkyAngle vanilla calculated sky angle [0.0, 1.0]
     * @param dimensionKey current dimension identifier
     * @return visual celestial angle
     */
    public float getVisualSkyAngle(float vanillaSkyAngle, String dimensionKey) {
        if (!shouldApplyInDimension(dimensionKey)) {
            return vanillaSkyAngle;
        }
        transitionManager.update();
        return SkyTimeOverride.calculateSkyAngle(transitionManager.getCurrentVisualTicks());
    }

    public void resetForTesting() {
        enabled = false;
        preset = TimeChangerPreset.NOON;
        customTimeTicks = 6000L;
        timeOffsetTicks = 0L;
        enableInOverworld = true;
        enableInNether = false;
        enableInEnd = false;
        enableInCustomDimensions = false;
        changeSunPosition = true;
        changeMoonPosition = true;
        changeSkyColor = true;
        showStars = true;
        preserveVanillaSky = false;
        transitionManager.reset();
    }
}
