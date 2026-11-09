package pl.zombek.client.core.camera;

/**
 * Snapshot representing current camera values, decoupled transformations,
 * and active modifier states across all visual subsystems.
 */
public class CameraState {
    // Vanilla baseline
    private final double vanillaFov;
    private final float vanillaYaw;
    private final float vanillaPitch;
    private final int perspectiveOrdinal;

    // Computed effective state
    private final double effectiveFov;
    private final boolean zoomActive;
    private final double zoomFactor;
    private final boolean freelookActive;
    private final float freelookYaw;
    private final float freelookPitch;
    private final boolean snaplookActive;
    private final boolean hurtCamActive;
    private final float hurtCamTiltMultiplier;
    private final boolean motionBlurActive;
    private final float motionBlurAlpha;

    public CameraState(double vanillaFov, float vanillaYaw, float vanillaPitch, int perspectiveOrdinal,
                       double effectiveFov, boolean zoomActive, double zoomFactor,
                       boolean freelookActive, float freelookYaw, float freelookPitch,
                       boolean snaplookActive, boolean hurtCamActive, float hurtCamTiltMultiplier,
                       boolean motionBlurActive, float motionBlurAlpha) {
        this.vanillaFov = vanillaFov;
        this.vanillaYaw = vanillaYaw;
        this.vanillaPitch = vanillaPitch;
        this.perspectiveOrdinal = perspectiveOrdinal;
        this.effectiveFov = effectiveFov;
        this.zoomActive = zoomActive;
        this.zoomFactor = zoomFactor;
        this.freelookActive = freelookActive;
        this.freelookYaw = freelookYaw;
        this.freelookPitch = freelookPitch;
        this.snaplookActive = snaplookActive;
        this.hurtCamActive = hurtCamActive;
        this.hurtCamTiltMultiplier = hurtCamTiltMultiplier;
        this.motionBlurActive = motionBlurActive;
        this.motionBlurAlpha = motionBlurAlpha;
    }

    public double getVanillaFov() {
        return vanillaFov;
    }

    public float getVanillaYaw() {
        return vanillaYaw;
    }

    public float getVanillaPitch() {
        return vanillaPitch;
    }

    public int getPerspectiveOrdinal() {
        return perspectiveOrdinal;
    }

    public double getEffectiveFov() {
        return effectiveFov;
    }

    public boolean isZoomActive() {
        return zoomActive;
    }

    public double getZoomFactor() {
        return zoomFactor;
    }

    public boolean isFreelookActive() {
        return freelookActive;
    }

    public float getFreelookYaw() {
        return freelookYaw;
    }

    public float getFreelookPitch() {
        return freelookPitch;
    }

    public boolean isSnaplookActive() {
        return snaplookActive;
    }

    public boolean isHurtCamActive() {
        return hurtCamActive;
    }

    public float getHurtCamTiltMultiplier() {
        return hurtCamTiltMultiplier;
    }

    public boolean isMotionBlurActive() {
        return motionBlurActive;
    }

    public float getMotionBlurAlpha() {
        return motionBlurAlpha;
    }
}
