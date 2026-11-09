package pl.zombek.client.core.modules.visual;

import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.BooleanSetting;
import pl.zombek.client.core.settings.EnumSetting;
import pl.zombek.client.core.settings.NumberSetting;
import pl.zombek.client.core.settings.SettingGroup;

/**
 * Freelook module allowing players to rotate camera independently from player entity.
 * Operates purely locally on the client perspective; never modifies server packets or body rotation.
 */
public class FreelookModule extends AbstractModule {
    private static FreelookModule instance;

    // General
    private final EnumSetting<FreelookActivationMode> activationModeSetting;
    private final BooleanSetting snapbackOnReleaseSetting;

    // Camera
    private final NumberSetting hSensitivitySetting;
    private final NumberSetting vSensitivitySetting;
    private final NumberSetting maxPitchSetting;
    private final NumberSetting maxYawSetting;
    private final BooleanSetting invertHSetting;
    private final BooleanSetting invertVSetting;
    private final BooleanSetting mouseSmoothingSetting;

    // Animation
    private final BooleanSetting smoothCameraSetting;
    private final BooleanSetting snapbackAnimationSetting;
    private final NumberSetting animationDurationSetting;
    private final EnumSetting<FreelookInterpolation> interpolationModeSetting;

    // Advanced
    private final BooleanSetting resetOnWorldChangeSetting;
    private final BooleanSetting resetOnFocusLostSetting;
    private final BooleanSetting resetOnDeathSetting;
    private final BooleanSetting disableInMenusSetting;

    public FreelookModule() {
        super(
                "freelook",
                "Freelook",
                "Umożliwia swobodne obracanie kamery wokół postaci (Lunar Client style). Zmienia wyłącznie perspektywę lokalną.",
                ModuleCategory.VISUAL
        );
        instance = this;

        // 1. General Group
        SettingGroup generalGroup = new SettingGroup("general", "General");
        this.activationModeSetting = new EnumSetting<>(
                "activation_mode", "Activation Mode", "Tryb aktywacji: Hold (przytrzymanie) lub Toggle (przełącznik)",
                FreelookActivationMode.HOLD
        );
        this.snapbackOnReleaseSetting = new BooleanSetting(
                "snapback_on_release", "Snapback on Release", "Przywraca kamerę do pozycji za graczem po zwolnieniu", true
        );
        generalGroup.addSetting(this.activationModeSetting);
        generalGroup.addSetting(this.snapbackOnReleaseSetting);
        registerGroup(generalGroup);

        // 2. Camera Group
        SettingGroup cameraGroup = new SettingGroup("camera", "Camera");
        this.hSensitivitySetting = new NumberSetting(
                "h_sensitivity", "Horizontal Sensitivity", "Czułość pozioma kamery", 1.0, 0.1, 5.0, 0.1
        );
        this.vSensitivitySetting = new NumberSetting(
                "v_sensitivity", "Vertical Sensitivity", "Czułość pionowa kamery", 1.0, 0.1, 5.0, 0.1
        );
        this.maxPitchSetting = new NumberSetting(
                "max_pitch", "Maximum Pitch", "Maksymalny kąt pochylenia góra/dół (stopnie)", 90.0, 45.0, 90.0, 5.0
        );
        this.maxYawSetting = new NumberSetting(
                "max_yaw", "Maximum Yaw", "Maksymalny kąt obrotu w poziomie (0 = bez limitu 360)", 0.0, 0.0, 360.0, 15.0
        );
        this.invertHSetting = new BooleanSetting(
                "invert_h", "Invert Horizontal", "Odwrócenie osi poziomej", false
        );
        this.invertVSetting = new BooleanSetting(
                "invert_v", "Invert Vertical", "Odwrócenie osi pionowej", false
        );
        this.mouseSmoothingSetting = new BooleanSetting(
                "mouse_smoothing", "Mouse Smoothing", "Wygładzanie wejścia myszy", false
        );
        cameraGroup.addSetting(this.hSensitivitySetting);
        cameraGroup.addSetting(this.vSensitivitySetting);
        cameraGroup.addSetting(this.maxPitchSetting);
        cameraGroup.addSetting(this.maxYawSetting);
        cameraGroup.addSetting(this.invertHSetting);
        cameraGroup.addSetting(this.invertVSetting);
        cameraGroup.addSetting(this.mouseSmoothingSetting);
        registerGroup(cameraGroup);

        // 3. Animation Group
        SettingGroup animationGroup = new SettingGroup("animation", "Animation");
        this.smoothCameraSetting = new BooleanSetting(
                "smooth_camera", "Smooth Camera", "Płynny ruch kamery", true
        );
        this.snapbackAnimationSetting = new BooleanSetting(
                "snapback_animation", "Snapback Animation", "Płynna animacja powrotu kamery", true
        );
        this.animationDurationSetting = new NumberSetting(
                "animation_duration", "Animation Duration (ms)", "Czas trwania animacji powrotu w ms", 150.0, 50.0, 500.0, 25.0
        );
        this.interpolationModeSetting = new EnumSetting<>(
                "interpolation_mode", "Interpolation Mode", "Krzywa animacji powrotu",
                FreelookInterpolation.EASE_OUT
        );
        animationGroup.addSetting(this.smoothCameraSetting);
        animationGroup.addSetting(this.snapbackAnimationSetting);
        animationGroup.addSetting(this.animationDurationSetting);
        animationGroup.addSetting(this.interpolationModeSetting);
        registerGroup(animationGroup);

        // 4. Advanced Group
        SettingGroup advancedGroup = new SettingGroup("advanced", "Advanced");
        this.resetOnWorldChangeSetting = new BooleanSetting(
                "reset_on_world_change", "Reset on World Change", "Resetowanie kamery przy zmianie świata/serwera", true
        );
        this.resetOnFocusLostSetting = new BooleanSetting(
                "reset_on_focus_lost", "Reset on Focus Lost", "Resetowanie kamery po utracie fokusu okna", true
        );
        this.resetOnDeathSetting = new BooleanSetting(
                "reset_on_death", "Reset on Death", "Resetowanie kamery po śmierci gracza", true
        );
        this.disableInMenusSetting = new BooleanSetting(
                "disable_in_menus", "Disable in Menus", "Dezaktywacja kamery przy otwartym menu/czacie", true
        );
        advancedGroup.addSetting(this.resetOnWorldChangeSetting);
        advancedGroup.addSetting(this.resetOnFocusLostSetting);
        advancedGroup.addSetting(this.resetOnDeathSetting);
        advancedGroup.addSetting(this.disableInMenusSetting);
        registerGroup(advancedGroup);
    }

    public static FreelookModule getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        // Ready for keybind activation
    }

    @Override
    public void onDisable() {
        FreelookCameraController.getInstance().reset();
    }

    /**
     * Handles key input event from platform keybind manager.
     */
    public void handleKeyInput(boolean isDown, float currentBodyYaw, float currentBodyPitch) {
        if (!isEnabled()) {
            return;
        }

        FreelookCameraController controller = FreelookCameraController.getInstance();
        FreelookActivationMode mode = this.activationModeSetting.getValue();

        if (mode == FreelookActivationMode.HOLD) {
            if (isDown) {
                if (!controller.isActive()) {
                    controller.startFreelook(currentBodyYaw, currentBodyPitch);
                }
            } else {
                if (controller.isActive()) {
                    boolean snapback = this.snapbackOnReleaseSetting.getValue() && this.snapbackAnimationSetting.getValue();
                    long duration = this.animationDurationSetting.getValue().longValue();
                    controller.stopFreelook(snapback, duration, this.interpolationModeSetting.getValue(),
                            currentBodyYaw, currentBodyPitch);
                }
            }
        } else if (mode == FreelookActivationMode.TOGGLE) {
            if (isDown) { // On edge press
                if (controller.isActive()) {
                    boolean snapback = this.snapbackOnReleaseSetting.getValue() && this.snapbackAnimationSetting.getValue();
                    long duration = this.animationDurationSetting.getValue().longValue();
                    controller.stopFreelook(snapback, duration, this.interpolationModeSetting.getValue(),
                            currentBodyYaw, currentBodyPitch);
                } else {
                    controller.startFreelook(currentBodyYaw, currentBodyPitch);
                }
            }
        }
    }

    // Getters for settings
    public EnumSetting<FreelookActivationMode> getActivationModeSetting() { return activationModeSetting; }
    public BooleanSetting getSnapbackOnReleaseSetting() { return snapbackOnReleaseSetting; }
    public NumberSetting getHSensitivitySetting() { return hSensitivitySetting; }
    public NumberSetting getVSensitivitySetting() { return vSensitivitySetting; }
    public NumberSetting getMaxPitchSetting() { return maxPitchSetting; }
    public NumberSetting getMaxYawSetting() { return maxYawSetting; }
    public BooleanSetting getInvertHSetting() { return invertHSetting; }
    public BooleanSetting getInvertVSetting() { return invertVSetting; }
    public BooleanSetting getMouseSmoothingSetting() { return mouseSmoothingSetting; }
    public BooleanSetting getSmoothCameraSetting() { return smoothCameraSetting; }
    public BooleanSetting getSnapbackAnimationSetting() { return snapbackAnimationSetting; }
    public NumberSetting getAnimationDurationSetting() { return animationDurationSetting; }
    public EnumSetting<FreelookInterpolation> getInterpolationModeSetting() { return interpolationModeSetting; }
    public BooleanSetting getResetOnWorldChangeSetting() { return resetOnWorldChangeSetting; }
    public BooleanSetting getResetOnFocusLostSetting() { return resetOnFocusLostSetting; }
    public BooleanSetting getResetOnDeathSetting() { return resetOnDeathSetting; }
    public BooleanSetting getDisableInMenusSetting() { return disableInMenusSetting; }
}
