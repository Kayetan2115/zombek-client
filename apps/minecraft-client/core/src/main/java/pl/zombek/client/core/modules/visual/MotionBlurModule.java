package pl.zombek.client.core.modules.visual;

import pl.zombek.client.core.camera.MotionBlurQuality;
import pl.zombek.client.core.camera.MotionDetectionMode;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

/**
 * Motion Blur module providing controlled temporal blending / framebuffer history blur
 * to smooth rapid camera pans and visual rotations with safe fallback and resource guards.
 */
public class MotionBlurModule extends AbstractModule {
    public static final String ID = "motion_blur";
    private static MotionBlurModule instance;

    // General
    private final NumberSetting blurIntensitySetting;
    private final NumberSetting blurStrengthSetting;
    private final NumberSetting maxBlurSetting;
    private final NumberSetting blurDecaySetting;

    // Sensitivity
    private final NumberSetting horizontalSensitivitySetting;
    private final NumberSetting verticalSensitivitySetting;
    private final NumberSetting cameraMovementThresholdSetting;
    private final EnumSetting<MotionDetectionMode> motionDetectionSetting;

    // Quality
    private final EnumSetting<MotionBlurQuality> renderQualitySetting;
    private final NumberSetting historySamplesSetting;
    private final NumberSetting resolutionScaleSetting;
    private final BooleanSetting blurSmoothingSetting;

    // Behavior
    private final BooleanSetting disableInGuiSetting;
    private final BooleanSetting disableInInventorySetting;
    private final BooleanSetting disableInHudEditorSetting;
    private final BooleanSetting disableWhilePausedSetting;
    private final BooleanSetting disableInSpectatorSetting;

    // Performance
    private final NumberSetting maxProcessingCostMsSetting;
    private final NumberSetting frameHistorySizeSetting;
    private final BooleanSetting automaticQualitySetting;
    private final BooleanSetting fallbackModeSetting;

    public MotionBlurModule() {
        super(
                ID,
                "Motion Blur",
                "Dodaje estetyczne, kontrolowane rozmycie ruchu przy szybkim obrocie kamery.",
                ModuleCategory.VISUAL
        );
        instance = this;

        // 1. General
        SettingGroup generalGroup = new SettingGroup("general", "General");
        this.blurIntensitySetting = new NumberSetting(
                "blur_intensity", "Blur Intensity", "Główna intensywność efektu rozmycia", 0.5, 0.0, 1.0, 0.05
        );
        this.blurStrengthSetting = new NumberSetting(
                "blur_strength", "Blur Strength", "Współczynnik siły rozmycia", 1.0, 0.1, 2.0, 0.1
        );
        this.maxBlurSetting = new NumberSetting(
                "max_blur", "Maximum Blur", "Górny limit nasycenia rozmycia", 0.8, 0.1, 0.95, 0.05
        );
        this.blurDecaySetting = new NumberSetting(
                "blur_decay", "Blur Decay", "Tempo wygaszania rozmycia po zatrzymaniu ruchu", 0.7, 0.1, 1.0, 0.05
        );
        generalGroup.addSetting(blurIntensitySetting);
        generalGroup.addSetting(blurStrengthSetting);
        generalGroup.addSetting(maxBlurSetting);
        generalGroup.addSetting(blurDecaySetting);
        addSettingGroup(generalGroup);

        // 2. Sensitivity
        SettingGroup sensGroup = new SettingGroup("sensitivity", "Sensitivity");
        this.horizontalSensitivitySetting = new NumberSetting(
                "horizontal_sensitivity", "Horizontal Sensitivity", "Czułość rozmycia przy obrocie w poziomie", 1.0, 0.1, 3.0, 0.1
        );
        this.verticalSensitivitySetting = new NumberSetting(
                "vertical_sensitivity", "Vertical Sensitivity", "Czułość rozmycia przy obrocie w pionie", 1.0, 0.1, 3.0, 0.1
        );
        this.cameraMovementThresholdSetting = new NumberSetting(
                "camera_movement_threshold", "Camera Movement Threshold (deg)", "Minimalny obrót kątowy aktywujący rozmycie", 0.05, 0.01, 2.0, 0.01
        );
        this.motionDetectionSetting = new EnumSetting<>(
                "motion_detection", "Motion Detection Source", "Źródło detekcji ruchu sceny", MotionDetectionMode.CAMERA_ROTATION
        );
        sensGroup.addSetting(horizontalSensitivitySetting);
        sensGroup.addSetting(verticalSensitivitySetting);
        sensGroup.addSetting(cameraMovementThresholdSetting);
        sensGroup.addSetting(motionDetectionSetting);
        addSettingGroup(sensGroup);

        // 3. Quality
        SettingGroup qualityGroup = new SettingGroup("quality", "Quality");
        this.renderQualitySetting = new EnumSetting<>(
                "render_quality", "Render Quality", "Profil jakości próbkowania rozmycia", MotionBlurQuality.HIGH
        );
        this.historySamplesSetting = new NumberSetting(
                "history_samples", "History Samples", "Liczba próbek historii klatek", 4.0, 2.0, 8.0, 1.0
        );
        this.resolutionScaleSetting = new NumberSetting(
                "resolution_scale", "Resolution Scale", "Skalowanie bufora historii klatek", 1.0, 0.5, 1.0, 0.1
        );
        this.blurSmoothingSetting = new BooleanSetting(
                "blur_smoothing", "Blur Smoothing", "Płynne wygładzanie przejść między klatkami rozmycia", true
        );
        qualityGroup.addSetting(renderQualitySetting);
        qualityGroup.addSetting(historySamplesSetting);
        qualityGroup.addSetting(resolutionScaleSetting);
        qualityGroup.addSetting(blurSmoothingSetting);
        addSettingGroup(qualityGroup);

        // 4. Behavior
        SettingGroup behaviorGroup = new SettingGroup("behavior", "Behavior");
        this.disableInGuiSetting = new BooleanSetting(
                "disable_in_gui", "Disable in GUI", "Wyłącza efekt podczas otwarcia menu lub czatu", true
        );
        this.disableInInventorySetting = new BooleanSetting(
                "disable_in_inventory", "Disable in Inventory", "Wyłącza efekt w ekwipunku", true
        );
        this.disableInHudEditorSetting = new BooleanSetting(
                "disable_in_hud_editor", "Disable in HUD Editor", "Wyłącza efekt w edytorze HUD", true
        );
        this.disableWhilePausedSetting = new BooleanSetting(
                "disable_while_paused", "Disable while Paused", "Wyłącza efekt podczas pauzy gry", true
        );
        this.disableInSpectatorSetting = new BooleanSetting(
                "disable_in_spectator", "Disable in Spectator Mode", "Wyłącza efekt w trybie obserwatora", false
        );
        behaviorGroup.addSetting(disableInGuiSetting);
        behaviorGroup.addSetting(disableInInventorySetting);
        behaviorGroup.addSetting(disableInHudEditorSetting);
        behaviorGroup.addSetting(disableWhilePausedSetting);
        behaviorGroup.addSetting(disableInSpectatorSetting);
        addSettingGroup(behaviorGroup);

        // 5. Performance
        SettingGroup perfGroup = new SettingGroup("performance", "Performance & Fallback");
        this.maxProcessingCostMsSetting = new NumberSetting(
                "max_processing_cost_ms", "Max Processing Cost (ms)", "Maksymalny dopuszczalny czas przetwarzania bufora", 5.0, 1.0, 16.0, 0.5
        );
        this.frameHistorySizeSetting = new NumberSetting(
                "frame_history_size", "Frame History Size", "Pojemność kolejki klatek historii", 3.0, 1.0, 6.0, 1.0
        );
        this.automaticQualitySetting = new BooleanSetting(
                "automatic_quality", "Automatic Quality Degradation", "Automatyczne obniżanie jakości przy spadkach FPS", true
        );
        this.fallbackModeSetting = new BooleanSetting(
                "fallback_mode", "Safe Fallback Mode", "Bezpieczny tryb zapasowy w razie braku wsparcia GPU/FBO", true
        );
        perfGroup.addSetting(maxProcessingCostMsSetting);
        perfGroup.addSetting(frameHistorySizeSetting);
        perfGroup.addSetting(automaticQualitySetting);
        perfGroup.addSetting(fallbackModeSetting);
        addSettingGroup(perfGroup);
    }

    public static MotionBlurModule getInstance() {
        if (instance == null) {
            new MotionBlurModule();
        }
        return instance;
    }

    public NumberSetting getBlurIntensitySetting() {
        return blurIntensitySetting;
    }

    public NumberSetting getBlurStrengthSetting() {
        return blurStrengthSetting;
    }

    public NumberSetting getMaxBlurSetting() {
        return maxBlurSetting;
    }

    public NumberSetting getBlurDecaySetting() {
        return blurDecaySetting;
    }

    public NumberSetting getHorizontalSensitivitySetting() {
        return horizontalSensitivitySetting;
    }

    public NumberSetting getVerticalSensitivitySetting() {
        return verticalSensitivitySetting;
    }

    public NumberSetting getCameraMovementThresholdSetting() {
        return cameraMovementThresholdSetting;
    }

    public EnumSetting<MotionDetectionMode> getMotionDetectionSetting() {
        return motionDetectionSetting;
    }

    public EnumSetting<MotionBlurQuality> getRenderQualitySetting() {
        return renderQualitySetting;
    }

    public NumberSetting getHistorySamplesSetting() {
        return historySamplesSetting;
    }

    public NumberSetting getResolutionScaleSetting() {
        return resolutionScaleSetting;
    }

    public BooleanSetting getBlurSmoothingSetting() {
        return blurSmoothingSetting;
    }

    public BooleanSetting getDisableInGuiSetting() {
        return disableInGuiSetting;
    }

    public BooleanSetting getDisableInInventorySetting() {
        return disableInInventorySetting;
    }

    public BooleanSetting getDisableInHudEditorSetting() {
        return disableInHudEditorSetting;
    }

    public BooleanSetting getDisableWhilePausedSetting() {
        return disableWhilePausedSetting;
    }

    public BooleanSetting getDisableInSpectatorSetting() {
        return disableInSpectatorSetting;
    }

    public NumberSetting getMaxProcessingCostMsSetting() {
        return maxProcessingCostMsSetting;
    }

    public NumberSetting getFrameHistorySizeSetting() {
        return frameHistorySizeSetting;
    }

    public BooleanSetting getAutomaticQualitySetting() {
        return automaticQualitySetting;
    }

    public BooleanSetting getFallbackModeSetting() {
        return fallbackModeSetting;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        pl.zombek.client.core.camera.MotionBlurController.getInstance().cleanup();
    }
}
