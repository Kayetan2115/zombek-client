package pl.zombek.client.core.modules.visual;

import pl.zombek.client.core.camera.HurtCamEasing;
import pl.zombek.client.core.camera.HurtCamMode;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

/**
 * Hurt Cam module controlling camera tilt and screen shake upon taking damage.
 * Purely local presentation; does not alter damage, player immunity, or server physics.
 */
public class HurtCamModule extends AbstractModule {
    public static final String ID = "hurt_cam";
    private static HurtCamModule instance;

    // General
    private final EnumSetting<HurtCamMode> modeSetting;
    private final BooleanSetting preserveVanillaSetting;
    private final NumberSetting customIntensitySetting;

    // Camera
    private final NumberSetting cameraTiltSetting;
    private final NumberSetting cameraShakeSetting;
    private final NumberSetting horizontalOffsetSetting;
    private final NumberSetting verticalOffsetSetting;
    private final BooleanSetting smoothRecoverySetting;

    // Animation
    private final NumberSetting recoveryDurationSetting;
    private final NumberSetting recoverySpeedSetting;
    private final EnumSetting<HurtCamEasing> easingSetting;

    // Behavior
    private final BooleanSetting disableInGuiSetting;
    private final BooleanSetting disableInSpectatorSetting;
    private final BooleanSetting disableInHudEditorSetting;
    private final BooleanSetting resetOnWorldChangeSetting;

    public HurtCamModule() {
        super(
                ID,
                "Hurt Cam",
                "Dostosowuje lub wyłącza efekt drżenia i przechylania kamery po otrzymaniu obrażeń.",
                ModuleCategory.VISUAL
        );
        instance = this;

        // 1. General
        SettingGroup generalGroup = new SettingGroup("general", "General");
        this.modeSetting = new EnumSetting<>(
                "hurt_cam_mode", "Hurt Cam Mode", "Tryb zachowania: Vanilla, Wyłączony, Zredukowany lub Własny", HurtCamMode.REDUCED
        );
        this.preserveVanillaSetting = new BooleanSetting(
                "preserve_vanilla", "Preserve Vanilla", "Wymusza standardowe zachowanie Minecrafta", false
        );
        this.customIntensitySetting = new NumberSetting(
                "custom_intensity", "Custom Intensity", "Ogólna intensywność efektu w trybie Custom", 0.5, 0.0, 2.0, 0.05, 2, ""
        );
        generalGroup.addSetting(modeSetting);
        generalGroup.addSetting(preserveVanillaSetting);
        generalGroup.addSetting(customIntensitySetting);
        addSettingGroup(generalGroup);

        // 2. Camera
        SettingGroup cameraGroup = new SettingGroup("camera", "Camera");
        this.cameraTiltSetting = new NumberSetting(
                "camera_tilt", "Camera Tilt Multiplier", "Mnożnik kąta nachylenia kamery", 0.5, 0.0, 2.0, 0.05, 2, ""
        );
        this.cameraShakeSetting = new NumberSetting(
                "camera_shake", "Camera Shake Multiplier", "Mnożnik wstrząsów kamery", 0.5, 0.0, 2.0, 0.05, 2, ""
        );
        this.horizontalOffsetSetting = new NumberSetting(
                "horizontal_offset", "Horizontal Offset", "Poziomy współczynnik odchylenia", 1.0, 0.0, 2.0, 0.1
        );
        this.verticalOffsetSetting = new NumberSetting(
                "vertical_offset", "Vertical Offset", "Pionowy współczynnik odchylenia", 1.0, 0.0, 2.0, 0.1
        );
        this.smoothRecoverySetting = new BooleanSetting(
                "smooth_recovery", "Smooth Recovery", "Płynny powrót kamery do pozycji pionowej", true
        );
        cameraGroup.addSetting(cameraTiltSetting);
        cameraGroup.addSetting(cameraShakeSetting);
        cameraGroup.addSetting(horizontalOffsetSetting);
        cameraGroup.addSetting(verticalOffsetSetting);
        cameraGroup.addSetting(smoothRecoverySetting);
        addSettingGroup(cameraGroup);

        // 3. Animation
        SettingGroup animGroup = new SettingGroup("animation", "Animation");
        this.recoveryDurationSetting = new NumberSetting(
                "recovery_duration", "Recovery Duration (ms)", "Czas trwania wygaszania wstrząsu w milisekundach", 250.0, 50.0, 1000.0, 25.0
        );
        this.recoverySpeedSetting = new NumberSetting(
                "recovery_speed", "Recovery Speed", "Szybkość powrotu do normy", 5.0, 1.0, 15.0, 0.5
        );
        this.easingSetting = new EnumSetting<>(
                "easing", "Recovery Easing", "Krzywa powrotu kamery (Spring, Ease Out, Damped)", HurtCamEasing.SPRING
        );
        animGroup.addSetting(recoveryDurationSetting);
        animGroup.addSetting(recoverySpeedSetting);
        animGroup.addSetting(easingSetting);
        addSettingGroup(animGroup);

        // 4. Behavior
        SettingGroup behaviorGroup = new SettingGroup("behavior", "Behavior");
        this.disableInGuiSetting = new BooleanSetting(
                "disable_in_gui", "Disable in GUI", "Wyłącza efekt podczas otwarcia menu", true
        );
        this.disableInSpectatorSetting = new BooleanSetting(
                "disable_in_spectator", "Disable in Spectator", "Wyłącza efekt w trybie obserwatora", true
        );
        this.disableInHudEditorSetting = new BooleanSetting(
                "disable_in_hud_editor", "Disable in HUD Editor", "Wyłącza efekt w edytorze HUD", true
        );
        this.resetOnWorldChangeSetting = new BooleanSetting(
                "reset_on_world_change", "Reset on World Change", "Resetuje wstrząs przy zmianie świata", true
        );
        behaviorGroup.addSetting(disableInGuiSetting);
        behaviorGroup.addSetting(disableInSpectatorSetting);
        behaviorGroup.addSetting(disableInHudEditorSetting);
        behaviorGroup.addSetting(resetOnWorldChangeSetting);
        addSettingGroup(behaviorGroup);
    }

    public static HurtCamModule getInstance() {
        if (instance == null) {
            new HurtCamModule();
        }
        return instance;
    }

    public EnumSetting<HurtCamMode> getModeSetting() {
        return modeSetting;
    }

    public BooleanSetting getPreserveVanillaSetting() {
        return preserveVanillaSetting;
    }

    public NumberSetting getCustomIntensitySetting() {
        return customIntensitySetting;
    }

    public NumberSetting getCameraTiltSetting() {
        return cameraTiltSetting;
    }

    public NumberSetting getCameraShakeSetting() {
        return cameraShakeSetting;
    }

    public NumberSetting getHorizontalOffsetSetting() {
        return horizontalOffsetSetting;
    }

    public NumberSetting getVerticalOffsetSetting() {
        return verticalOffsetSetting;
    }

    public BooleanSetting getSmoothRecoverySetting() {
        return smoothRecoverySetting;
    }

    public NumberSetting getRecoveryDurationSetting() {
        return recoveryDurationSetting;
    }

    public NumberSetting getRecoverySpeedSetting() {
        return recoverySpeedSetting;
    }

    public EnumSetting<HurtCamEasing> getEasingSetting() {
        return easingSetting;
    }

    public BooleanSetting getDisableInGuiSetting() {
        return disableInGuiSetting;
    }

    public BooleanSetting getDisableInSpectatorSetting() {
        return disableInSpectatorSetting;
    }

    public BooleanSetting getDisableInHudEditorSetting() {
        return disableInHudEditorSetting;
    }

    public BooleanSetting getResetOnWorldChangeSetting() {
        return resetOnWorldChangeSetting;
    }
}
