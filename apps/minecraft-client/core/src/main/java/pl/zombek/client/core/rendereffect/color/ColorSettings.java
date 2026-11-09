package pl.zombek.client.core.rendereffect.color;

/**
 * Immutable configuration snapshot for color grading parameters.
 */
public class ColorSettings {
    private final float saturation;
    private final float vibrance;
    private final float contrast;
    private final float brightness;
    private final float hueShift;
    private final float temperature;
    private final float redMultiplier;
    private final float greenMultiplier;
    private final float blueMultiplier;
    private final boolean affectWorld;
    private final boolean affectEntities;
    private final boolean affectItems;
    private final boolean affectParticles;
    private final boolean preserveHud;

    public ColorSettings(
            float saturation,
            float vibrance,
            float contrast,
            float brightness,
            float hueShift,
            float temperature,
            float redMultiplier,
            float greenMultiplier,
            float blueMultiplier,
            boolean affectWorld,
            boolean affectEntities,
            boolean affectItems,
            boolean affectParticles,
            boolean preserveHud
    ) {
        this.saturation = saturation;
        this.vibrance = vibrance;
        this.contrast = contrast;
        this.brightness = brightness;
        this.hueShift = hueShift;
        this.temperature = temperature;
        this.redMultiplier = redMultiplier;
        this.greenMultiplier = greenMultiplier;
        this.blueMultiplier = blueMultiplier;
        this.affectWorld = affectWorld;
        this.affectEntities = affectEntities;
        this.affectItems = affectItems;
        this.affectParticles = affectParticles;
        this.preserveHud = preserveHud;
    }

    public static ColorSettings vanilla() {
        return new ColorSettings(
                1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f,
                1.0f, 1.0f, 1.0f,
                true, true, true, true, true
        );
    }

    public float getSaturation() {
        return saturation;
    }

    public float getVibrance() {
        return vibrance;
    }

    public float getContrast() {
        return contrast;
    }

    public float getBrightness() {
        return brightness;
    }

    public float getHueShift() {
        return hueShift;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getRedMultiplier() {
        return redMultiplier;
    }

    public float getGreenMultiplier() {
        return greenMultiplier;
    }

    public float getBlueMultiplier() {
        return blueMultiplier;
    }

    public boolean isAffectWorld() {
        return affectWorld;
    }

    public boolean isAffectEntities() {
        return affectEntities;
    }

    public boolean isAffectItems() {
        return affectItems;
    }

    public boolean isAffectParticles() {
        return affectParticles;
    }

    public boolean isPreserveHud() {
        return preserveHud;
    }

    public boolean isTargetEnabled(TargetType target) {
        if (target == TargetType.HUD) {
            return !preserveHud;
        }
        return switch (target) {
            case WORLD -> affectWorld;
            case ENTITY -> affectEntities;
            case ITEM -> affectItems;
            case PARTICLE -> affectParticles;
            case HUD -> false;
        };
    }
}
