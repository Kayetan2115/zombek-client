package pl.zombek.client.core.modules.visual;

import pl.zombek.client.core.camera.FovEasing;
import pl.zombek.client.core.camera.FovPreset;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

/**
 * FOV Changer module providing customized Field of View, dynamic sprint/bow multipliers,
 * and smooth transitions without affecting player reach or speed mechanics.
 */
public class FovChangerModule extends AbstractModule {
    public static final String ID = "fov_changer";
    private static FovChangerModule instance;

    // General
    private final NumberSetting customFovSetting;
    private final NumberSetting minFovSetting;
    private final NumberSetting maxFovSetting;
    private final NumberSetting defaultFovSetting;
    private final EnumSetting<FovPreset> presetSetting;

    // Dynamic FOV
    private final BooleanSetting preserveVanillaDynamicFovSetting;
    private final BooleanSetting disableDynamicFovSetting;
    private final NumberSetting customSprintFovMultiplierSetting;
    private final NumberSetting customMovementFovMultiplierSetting;
    private final NumberSetting customStillFovMultiplierSetting;
    private final NumberSetting customItemUseFovMultiplierSetting;

    // Animation
    private final BooleanSetting smoothFovSetting;
    private final NumberSetting transitionSpeedSetting;
    private final NumberSetting transitionDurationSetting;
    private final EnumSetting<FovEasing> easingSetting;
    private final BooleanSetting instantTransitionSetting;

    // Compatibility
    private final BooleanSetting integrateWithZoomSetting;
    private final BooleanSetting preserveOtherModifiersSetting;

    // Behavior
    private final BooleanSetting resetOnWorldChangeSetting;
    private final BooleanSetting resetOnFocusLostSetting;
    private final BooleanSetting disableInGuiSetting;

    public FovChangerModule() {
        super(
                ID,
                "FOV Changer",
                "Pozwala na swobodne dostosowanie pola widzenia (FOV), dynamicznego FOV sprintu oraz płynnych przejść.",
                ModuleCategory.VISUAL
        );
        instance = this;

        // 1. General
        SettingGroup generalGroup = new SettingGroup("general", "General");
        this.customFovSetting = new NumberSetting(
                "custom_fov", "Custom FOV", "Własna wartość pola widzenia w stopniach", 90.0, 30.0, 150.0, 1.0
        );
        this.minFovSetting = new NumberSetting(
                "min_fov", "Minimum FOV", "Minimalne dopuszczalne pole widzenia", 30.0, 10.0, 60.0, 1.0
        );
        this.maxFovSetting = new NumberSetting(
                "max_fov", "Maximum FOV", "Maksymalne dopuszczalne pole widzenia", 150.0, 100.0, 170.0, 1.0
        );
        this.defaultFovSetting = new NumberSetting(
                "default_fov", "Default FOV", "Wartość domyślna Minecrafta", 70.0, 30.0, 110.0, 1.0
        );
        this.presetSetting = new EnumSetting<>(
                "fov_preset", "FOV Preset", "Gotowy profil pola widzenia", FovPreset.CUSTOM
        );
        generalGroup.addSetting(customFovSetting);
        generalGroup.addSetting(minFovSetting);
        generalGroup.addSetting(maxFovSetting);
        generalGroup.addSetting(defaultFovSetting);
        generalGroup.addSetting(presetSetting);
        addSettingGroup(generalGroup);

        // 2. Dynamic FOV
        SettingGroup dynamicGroup = new SettingGroup("dynamic", "Dynamic FOV");
        this.preserveVanillaDynamicFovSetting = new BooleanSetting(
                "preserve_vanilla_dynamic_fov", "Preserve Vanilla Dynamic FOV", "Zachowuje oryginalne skalowanie FOV Minecrafta", false
        );
        this.disableDynamicFovSetting = new BooleanSetting(
                "disable_dynamic_fov", "Disable Dynamic FOV", "Całkowicie blokuje zmiany FOV podczas sprintu i chodzenia", false
        );
        this.customSprintFovMultiplierSetting = new NumberSetting(
                "custom_sprint_fov_multiplier", "Sprint FOV Multiplier", "Mnożnik FOV podczas sprintu", 1.15, 1.0, 1.5, 0.05, 2, ""
        );
        this.customMovementFovMultiplierSetting = new NumberSetting(
                "custom_movement_fov_multiplier", "Movement FOV Multiplier", "Mnożnik FOV podczas zwykłego chodu", 1.05, 1.0, 1.3, 0.01, 2, ""
        );
        this.customStillFovMultiplierSetting = new NumberSetting(
                "custom_still_fov_multiplier", "Still FOV Multiplier", "Mnożnik FOV w bezruchu", 1.0, 0.8, 1.2, 0.05, 2, ""
        );
        this.customItemUseFovMultiplierSetting = new NumberSetting(
                "custom_item_use_fov_multiplier", "Item Use FOV Multiplier", "Mnożnik FOV podczas naciągania łuku lub jedzenia", 0.85, 0.5, 1.0, 0.05, 2, ""
        );
        dynamicGroup.addSetting(preserveVanillaDynamicFovSetting);
        dynamicGroup.addSetting(disableDynamicFovSetting);
        dynamicGroup.addSetting(customSprintFovMultiplierSetting);
        dynamicGroup.addSetting(customMovementFovMultiplierSetting);
        dynamicGroup.addSetting(customStillFovMultiplierSetting);
        dynamicGroup.addSetting(customItemUseFovMultiplierSetting);
        addSettingGroup(dynamicGroup);

        // 3. Animation
        SettingGroup animGroup = new SettingGroup("animation", "Animation");
        this.smoothFovSetting = new BooleanSetting(
                "smooth_fov", "Smooth FOV", "Płynne przejścia pomiędzy stanami FOV", true
        );
        this.transitionSpeedSetting = new NumberSetting(
                "transition_speed", "Transition Speed", "Prędkość animacji zmiany FOV", 12.0, 1.0, 30.0, 1.0
        );
        this.transitionDurationSetting = new NumberSetting(
                "transition_duration", "Transition Duration (ms)", "Czas przejścia w milisekundach", 100.0, 20.0, 500.0, 10.0
        );
        this.easingSetting = new EnumSetting<>(
                "easing", "FOV Easing", "Krzywa wygładzania przejścia", FovEasing.EASE_OUT
        );
        this.instantTransitionSetting = new BooleanSetting(
                "instant_transition", "Instant Transition", "Błyskawiczna zmiana bez wygładzania", false
        );
        animGroup.addSetting(smoothFovSetting);
        animGroup.addSetting(transitionSpeedSetting);
        animGroup.addSetting(transitionDurationSetting);
        animGroup.addSetting(easingSetting);
        animGroup.addSetting(instantTransitionSetting);
        addSettingGroup(animGroup);

        // 4. Compatibility
        SettingGroup compGroup = new SettingGroup("compatibility", "Compatibility");
        this.integrateWithZoomSetting = new BooleanSetting(
                "integrate_with_zoom", "Integrate with Zoom", "Umożliwia Zoomowi nadpisywanie FOV na czas przybliżenia", true
        );
        this.preserveOtherModifiersSetting = new BooleanSetting(
                "preserve_other_modifiers", "Preserve Other Modifiers", "Uwzględnia modyfikatory innych aktywnych efektów", true
        );
        compGroup.addSetting(integrateWithZoomSetting);
        compGroup.addSetting(preserveOtherModifiersSetting);
        addSettingGroup(compGroup);

        // 5. Behavior
        SettingGroup behaviorGroup = new SettingGroup("behavior", "Behavior");
        this.resetOnWorldChangeSetting = new BooleanSetting(
                "reset_on_world_change", "Reset on World Change", "Przywraca FOV domyślny przy zmianie świata", false
        );
        this.resetOnFocusLostSetting = new BooleanSetting(
                "reset_on_focus_lost", "Reset on Focus Lost", "Przywraca FOV domyślny po utracie fokusu okna", false
        );
        this.disableInGuiSetting = new BooleanSetting(
                "disable_in_gui", "Disable in GUI", "Zawiesza modyfikacje FOV w menu", true
        );
        behaviorGroup.addSetting(resetOnWorldChangeSetting);
        behaviorGroup.addSetting(resetOnFocusLostSetting);
        behaviorGroup.addSetting(disableInGuiSetting);
        addSettingGroup(behaviorGroup);
    }

    public static FovChangerModule getInstance() {
        if (instance == null) {
            new FovChangerModule();
        }
        return instance;
    }

    public NumberSetting getCustomFovSetting() {
        return customFovSetting;
    }

    public NumberSetting getMinFovSetting() {
        return minFovSetting;
    }

    public NumberSetting getMaxFovSetting() {
        return maxFovSetting;
    }

    public NumberSetting getDefaultFovSetting() {
        return defaultFovSetting;
    }

    public EnumSetting<FovPreset> getPresetSetting() {
        return presetSetting;
    }

    public BooleanSetting getPreserveVanillaDynamicFovSetting() {
        return preserveVanillaDynamicFovSetting;
    }

    public BooleanSetting getDisableDynamicFovSetting() {
        return disableDynamicFovSetting;
    }

    public NumberSetting getCustomSprintFovMultiplierSetting() {
        return customSprintFovMultiplierSetting;
    }

    public NumberSetting getCustomMovementFovMultiplierSetting() {
        return customMovementFovMultiplierSetting;
    }

    public NumberSetting getCustomStillFovMultiplierSetting() {
        return customStillFovMultiplierSetting;
    }

    public NumberSetting getCustomItemUseFovMultiplierSetting() {
        return customItemUseFovMultiplierSetting;
    }

    public BooleanSetting getSmoothFovSetting() {
        return smoothFovSetting;
    }

    public NumberSetting getTransitionSpeedSetting() {
        return transitionSpeedSetting;
    }

    public NumberSetting getTransitionDurationSetting() {
        return transitionDurationSetting;
    }

    public EnumSetting<FovEasing> getEasingSetting() {
        return easingSetting;
    }

    public BooleanSetting getInstantTransitionSetting() {
        return instantTransitionSetting;
    }

    public BooleanSetting getIntegrateWithZoomSetting() {
        return integrateWithZoomSetting;
    }

    public BooleanSetting getPreserveOtherModifiersSetting() {
        return preserveOtherModifiersSetting;
    }

    public BooleanSetting getResetOnWorldChangeSetting() {
        return resetOnWorldChangeSetting;
    }

    public BooleanSetting getResetOnFocusLostSetting() {
        return resetOnFocusLostSetting;
    }

    public BooleanSetting getDisableInGuiSetting() {
        return disableInGuiSetting;
    }
}
