package pl.zombek.client.core.modules.pvp.snaplook;

import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

/**
 * Snaplook Module.
 * Quickly switches camera perspective upon pressing or holding an assigned key (default V)
 * with robust restoration and conflict resolution.
 */
public class SnaplookModule extends AbstractModule {
    private static SnaplookModule instance;

    // General
    private final SettingGroup groupGeneral;
    private final EnumSetting<SnaplookActivationMode> activationModeSetting;
    private final EnumSetting<SnaplookPerspective> targetPerspectiveSetting;
    private final BooleanSetting restorePreviousPerspectiveSetting;
    private final BooleanSetting smoothCameraSetting;
    private final BooleanSetting disableInGuiSetting;

    // Camera
    private final SettingGroup groupCamera;
    private final NumberSetting transitionDurationSetting;
    private final EnumSetting<SnaplookEasing> easingSetting;
    private final NumberSetting transitionSpeedSetting;

    // Behavior
    private final SettingGroup groupBehavior;
    private final BooleanSetting resetOnWorldChangeSetting;
    private final BooleanSetting resetOnDeathSetting;
    private final BooleanSetting resetOnRespawnSetting;
    private final BooleanSetting resetOnFocusLostSetting;
    private final BooleanSetting respectManualChangesSetting;

    public SnaplookModule() {
        super(
                "snaplook",
                "Snaplook",
                "Szybkie przełączanie perspektywy kamery po naciśnięciu lub przytrzymaniu klawisza z bezpiecznym powrotem.",
                ModuleCategory.PVP,
                GLFW.GLFW_KEY_V
        );
        instance = this;

        // General
        this.groupGeneral = new SettingGroup("general", "General Settings");
        this.activationModeSetting = new EnumSetting<>(
                "activation_mode", "Activation Mode",
                "Tryb działania skrótu (HOLD, TOGGLE, TAP).", SnaplookActivationMode.HOLD
        );
        this.targetPerspectiveSetting = new EnumSetting<>(
                "target_perspective", "Target Perspective",
                "Perspektywa docelowa po aktywacji Snaplook.", SnaplookPerspective.THIRD_PERSON_BACK
        );
        this.restorePreviousPerspectiveSetting = new BooleanSetting(
                "restore_previous_perspective", "Restore Previous Perspective",
                "Przywraca perspektywę używaną przed aktywacją modułu.", true
        );
        this.smoothCameraSetting = new BooleanSetting(
                "smooth_camera", "Smooth Camera",
                "Płynna animacja przejścia kamery.", false
        );
        this.disableInGuiSetting = new BooleanSetting(
                "disable_in_gui", "Disable in GUI",
                "Automatycznie resetuj perspektywę przy otwarciu menu.", true
        );
        this.groupGeneral.addSetting(activationModeSetting);
        this.groupGeneral.addSetting(targetPerspectiveSetting);
        this.groupGeneral.addSetting(restorePreviousPerspectiveSetting);
        this.groupGeneral.addSetting(smoothCameraSetting);
        this.groupGeneral.addSetting(disableInGuiSetting);
        registerGroup(this.groupGeneral);

        // Camera
        this.groupCamera = new SettingGroup("camera", "Camera Smoothing");
        this.transitionDurationSetting = new NumberSetting(
                "transition_duration", "Transition Duration (ms)",
                "Czas trwania płynnego przejścia w milisekundach.", 150.0, 50.0, 500.0, 25.0
        );
        this.easingSetting = new EnumSetting<>(
                "easing", "Camera Easing",
                "Krzywa interpolacji przejścia kamery.", SnaplookEasing.EASE_OUT
        );
        this.transitionSpeedSetting = new NumberSetting(
                "transition_speed", "Transition Speed",
                "Mnożnik szybkości przejścia.", 1.0, 0.5, 3.0, 0.1
        );
        this.groupCamera.addSetting(transitionDurationSetting);
        this.groupCamera.addSetting(easingSetting);
        this.groupCamera.addSetting(transitionSpeedSetting);
        registerGroup(this.groupCamera);

        // Behavior
        this.groupBehavior = new SettingGroup("behavior", "Conflict & Reset Behavior");
        this.resetOnWorldChangeSetting = new BooleanSetting(
                "reset_on_world_change", "Reset on World Change",
                "Resetuj perspektywę przy zmianie świata lub rozłączeniu.", true
        );
        this.resetOnDeathSetting = new BooleanSetting(
                "reset_on_death", "Reset on Death",
                "Resetuj perspektywę przy śmierci gracza.", true
        );
        this.resetOnRespawnSetting = new BooleanSetting(
                "reset_on_respawn", "Reset on Respawn",
                "Resetuj perspektywę po odrodzeniu.", true
        );
        this.resetOnFocusLostSetting = new BooleanSetting(
                "reset_on_focus_lost", "Reset on Focus Lost",
                "Resetuj perspektywę, gdy okno gry traci fokus.", true
        );
        this.respectManualChangesSetting = new BooleanSetting(
                "respect_manual_changes", "Respect Manual Perspective Changes",
                "Jeżeli gracz wciśnie F5 podczas Snaplook, uszanuj jego wybór i nie przywracaj starej perspektywy.", true
        );
        this.groupBehavior.addSetting(resetOnWorldChangeSetting);
        this.groupBehavior.addSetting(resetOnDeathSetting);
        this.groupBehavior.addSetting(resetOnRespawnSetting);
        this.groupBehavior.addSetting(resetOnFocusLostSetting);
        this.groupBehavior.addSetting(respectManualChangesSetting);
        registerGroup(this.groupBehavior);
    }

    public static SnaplookModule getInstance() {
        return instance;
    }

    @Override
    public void onInitialize() {}

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {
        SnaplookController.getInstance().reset();
    }

    @Override
    public void onTick() {}

    // Getters for settings
    public EnumSetting<SnaplookActivationMode> getActivationModeSetting() { return activationModeSetting; }
    public EnumSetting<SnaplookPerspective> getTargetPerspectiveSetting() { return targetPerspectiveSetting; }
    public BooleanSetting getRestorePreviousPerspectiveSetting() { return restorePreviousPerspectiveSetting; }
    public BooleanSetting getSmoothCameraSetting() { return smoothCameraSetting; }
    public BooleanSetting getDisableInGuiSetting() { return disableInGuiSetting; }

    public NumberSetting getTransitionDurationSetting() { return transitionDurationSetting; }
    public EnumSetting<SnaplookEasing> getEasingSetting() { return easingSetting; }
    public NumberSetting getTransitionSpeedSetting() { return transitionSpeedSetting; }

    public BooleanSetting getResetOnWorldChangeSetting() { return resetOnWorldChangeSetting; }
    public BooleanSetting getResetOnDeathSetting() { return resetOnDeathSetting; }
    public BooleanSetting getResetOnRespawnSetting() { return resetOnRespawnSetting; }
    public BooleanSetting getResetOnFocusLostSetting() { return resetOnFocusLostSetting; }
    public BooleanSetting getRespectManualChangesSetting() { return respectManualChangesSetting; }
}
