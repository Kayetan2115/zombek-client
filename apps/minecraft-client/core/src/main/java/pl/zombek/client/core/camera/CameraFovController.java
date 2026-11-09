package pl.zombek.client.core.camera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.modules.visual.FovChangerModule;

/**
 * Central controller coordinating camera Field of View (FOV) calculation,
 * priority resolution between Vanilla, FOV Changer, Dynamic Movement, and Zoom,
 * with smooth easing transitions and projection bounds protection.
 */
public class CameraFovController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CameraFovController.class);
    private static final CameraFovController INSTANCE = new CameraFovController();

    public static final double MIN_SAFE_FOV = 5.0;
    public static final double MAX_SAFE_FOV = 170.0;

    // Dynamic FOV interpolation state
    private double currentDynamicMultiplier = 1.0;
    private double targetDynamicMultiplier = 1.0;
    private long dynamicAnimStartTime = 0L;
    private double dynamicAnimStartMultiplier = 1.0;

    // FOV Changer smooth transition state
    private double currentBaseFov = 70.0;
    private double targetBaseFov = 70.0;
    private long baseAnimStartTime = 0L;
    private double baseAnimStartFov = 70.0;

    private CameraFovController() {}

    public static CameraFovController getInstance() {
        return INSTANCE;
    }

    /**
     * Computes the final camera FOV to be applied to the projection matrix.
     *
     * @param currentFov   current FOV from game renderer (may already include vanilla dynamic multipliers)
     * @param tickDelta    frame interpolation delta in [0.0, 1.0]
     * @param isSprinting  whether player entity is sprinting
     * @param isMoving     whether player entity is moving horizontally
     * @param isUsingItem  whether player is actively pulling a bow, eating, or blocking
     * @return final calculated FOV clamped to safe projection boundaries
     */
    public synchronized double computeFov(double currentFov, float tickDelta,
                                          boolean isSprinting, boolean isMoving, boolean isUsingItem) {
        return computeFov(currentFov, 70.0, tickDelta, isSprinting, isMoving, isUsingItem);
    }

    /**
     * Computes the final camera FOV with explicit vanilla base FOV from GameOptions.
     */
    public synchronized double computeFov(double currentFov, double vanillaBaseFov, float tickDelta,
                                          boolean isSprinting, boolean isMoving, boolean isUsingItem) {
        FovChangerModule fovMod = FovChangerModule.getInstance();
        ZoomController zoomCtrl = ZoomController.getInstance();

        double finalFov;

        if (fovMod == null || !fovMod.isEnabled()) {
            // FOV Changer module is disabled: use Minecraft's current FOV as base
            this.currentBaseFov = currentFov;
            this.targetBaseFov = currentFov;
            finalFov = currentFov;
        } else {
            // 1. Determine Target Base FOV
            double chosenBaseFov = vanillaBaseFov > 0.0 ? vanillaBaseFov : 70.0;
            FovPreset preset = fovMod.getPresetSetting().getValue();
            if (preset != null && !preset.isCustom()) {
                chosenBaseFov = preset.getFovValue();
            } else {
                chosenBaseFov = fovMod.getCustomFovSetting().getValue();
            }

            // Smoothly interpolate Base FOV if enabled
            if (fovMod.getSmoothFovSetting().getValue() && !fovMod.getInstantTransitionSetting().getValue()) {
                updateBaseFovAnimation(chosenBaseFov, fovMod);
            } else {
                this.currentBaseFov = chosenBaseFov;
                this.targetBaseFov = chosenBaseFov;
            }

            // 2. Determine Dynamic Movement/Item FOV Multiplier
            double chosenDynamicMult = 1.0;
            if (fovMod.getDisableDynamicFovSetting().getValue()) {
                chosenDynamicMult = 1.0;
            } else if (fovMod.getPreserveVanillaDynamicFovSetting().getValue()) {
                if (vanillaBaseFov > 0.0) {
                    chosenDynamicMult = currentFov / vanillaBaseFov;
                } else {
                    if (isUsingItem) chosenDynamicMult *= 0.85;
                    if (isSprinting) chosenDynamicMult *= 1.15;
                }
            } else {
                if (isUsingItem) {
                    chosenDynamicMult *= fovMod.getCustomItemUseFovMultiplierSetting().getValue();
                }
                if (isSprinting) {
                    chosenDynamicMult *= fovMod.getCustomSprintFovMultiplierSetting().getValue();
                } else if (isMoving) {
                    chosenDynamicMult *= fovMod.getCustomMovementFovMultiplierSetting().getValue();
                } else {
                    chosenDynamicMult *= fovMod.getCustomStillFovMultiplierSetting().getValue();
                }
            }

            // Smoothly interpolate Dynamic Multiplier
            if (fovMod.getSmoothFovSetting().getValue() && !fovMod.getInstantTransitionSetting().getValue()) {
                updateDynamicMultiplierAnimation(chosenDynamicMult, fovMod);
            } else {
                this.currentDynamicMultiplier = chosenDynamicMult;
                this.targetDynamicMultiplier = chosenDynamicMult;
            }

            finalFov = currentBaseFov * currentDynamicMultiplier;
        }

        // 3. Apply Zoom Layer on top of User FOV
        zoomCtrl.updateAnimation();
        double zoomFactor = zoomCtrl.getCurrentZoomFactor();

        if (zoomFactor > 1.0001) {
            finalFov = finalFov / zoomFactor;
        }

        // 4. Clamping to Safe Projection Bounds
        return Math.max(MIN_SAFE_FOV, Math.min(MAX_SAFE_FOV, finalFov));
    }

    private void updateBaseFovAnimation(double target, FovChangerModule mod) {
        if (Math.abs(this.targetBaseFov - target) > 0.01) {
            this.targetBaseFov = target;
            this.baseAnimStartFov = this.currentBaseFov;
            this.baseAnimStartTime = System.currentTimeMillis();
        }

        double duration = mod.getTransitionDurationSetting().getValue();
        if (duration <= 0.0) {
            this.currentBaseFov = targetBaseFov;
            return;
        }

        long elapsed = System.currentTimeMillis() - baseAnimStartTime;
        double progress = Math.min(1.0, elapsed / duration);
        FovEasing easing = mod.getEasingSetting().getValue();
        double eased = easing != null ? easing.ease(progress) : progress;

        this.currentBaseFov = baseAnimStartFov + (targetBaseFov - baseAnimStartFov) * eased;
        if (progress >= 1.0) {
            this.currentBaseFov = targetBaseFov;
        }
    }

    private void updateDynamicMultiplierAnimation(double target, FovChangerModule mod) {
        if (Math.abs(this.targetDynamicMultiplier - target) > 0.001) {
            this.targetDynamicMultiplier = target;
            this.dynamicAnimStartMultiplier = this.currentDynamicMultiplier;
            this.dynamicAnimStartTime = System.currentTimeMillis();
        }

        double duration = mod.getTransitionDurationSetting().getValue();
        if (duration <= 0.0) {
            this.currentDynamicMultiplier = targetDynamicMultiplier;
            return;
        }

        long elapsed = System.currentTimeMillis() - dynamicAnimStartTime;
        double progress = Math.min(1.0, elapsed / duration);
        FovEasing easing = mod.getEasingSetting().getValue();
        double eased = easing != null ? easing.ease(progress) : progress;

        this.currentDynamicMultiplier = dynamicAnimStartMultiplier + (targetDynamicMultiplier - dynamicAnimStartMultiplier) * eased;
        if (progress >= 1.0) {
            this.currentDynamicMultiplier = targetDynamicMultiplier;
        }
    }

    public synchronized void resetForTesting() {
        this.currentDynamicMultiplier = 1.0;
        this.targetDynamicMultiplier = 1.0;
        this.currentBaseFov = 70.0;
        this.targetBaseFov = 70.0;
    }
}
