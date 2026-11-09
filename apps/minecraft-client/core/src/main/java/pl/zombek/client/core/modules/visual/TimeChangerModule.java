package pl.zombek.client.core.modules.visual;

import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;
import pl.zombek.client.core.worldvisual.time.TimeChangerPreset;
import pl.zombek.client.core.worldvisual.time.WorldTimeVisualController;

/**
 * Time Changer module allowing purely local client-side override of time of day and celestial angle.
 */
public class TimeChangerModule extends AbstractModule {
    public static final String ID = "time_changer";
    private static TimeChangerModule instance;

    // General
    private final EnumSetting<TimeChangerPreset> timePresetSetting;
    private final NumberSetting customTimeSetting;
    private final NumberSetting timeOffsetSetting;

    // Sky
    private final BooleanSetting changeSunPositionSetting;
    private final BooleanSetting changeMoonPositionSetting;
    private final BooleanSetting changeSkyColorSetting;
    private final BooleanSetting showStarsSetting;
    private final BooleanSetting preserveVanillaSkySetting;

    // Lighting
    private final BooleanSetting changeVisualLightCycleSetting;
    private final BooleanSetting preserveBlockLightingSetting;
    private final BooleanSetting preserveDynamicLightingSetting;

    // Animation
    private final BooleanSetting smoothTransitionSetting;
    private final NumberSetting transitionDurationSetting;
    private final NumberSetting transitionSpeedSetting;
    private final BooleanSetting instantChangeSetting;

    // Dimensions
    private final BooleanSetting enableInOverworldSetting;
    private final BooleanSetting enableInNetherSetting;
    private final BooleanSetting enableInEndSetting;
    private final BooleanSetting enableInCustomDimensionsSetting;

    // Compatibility
    private final BooleanSetting shaderCompatibilitySetting;
    private final BooleanSetting resourcePackCompatibilitySetting;

    // Behavior
    private final BooleanSetting resetOnWorldChangeSetting;
    private final BooleanSetting preserveSettingBetweenWorldsSetting;
    private final BooleanSetting restoreVanillaTimeSetting;

    public TimeChangerModule() {
        super(
                ID,
                "Time Changer",
                "Lokalna, wizualna zmiana pory dnia i pozycji słońca/księżyca bez ingerencji w czas serwera.",
                ModuleCategory.VISUAL,
                GLFW.GLFW_KEY_UNKNOWN
        );
        instance = this;

        // 1. General
        SettingGroup generalGroup = new SettingGroup("general", "General");
        this.timePresetSetting = new EnumSetting<>(
                "time_preset", "Time Preset", "Wybierz gotową porę dnia (Sunrise, Noon, Sunset, Midnight, Custom)",
                TimeChangerPreset.NOON
        );
        this.customTimeSetting = new NumberSetting(
                "custom_time", "Custom Time (Ticks)", "Dokładny czas dnia w tickach (0-24000)", 6000.0, 0.0, 24000.0, 500.0
        );
        this.timeOffsetSetting = new NumberSetting(
                "time_offset", "Time Offset", "Korekta przesunięcia czasu w tickach", 0.0, -12000.0, 12000.0, 500.0
        );
        generalGroup.addSetting(timePresetSetting);
        generalGroup.addSetting(customTimeSetting);
        generalGroup.addSetting(timeOffsetSetting);
        addSettingGroup(generalGroup);

        // 2. Sky & Celestial
        SettingGroup skyGroup = new SettingGroup("sky", "Sky & Celestials");
        this.changeSunPositionSetting = new BooleanSetting(
                "change_sun_position", "Change Sun Position", "Wizualne przesunięcie słońca zgodnie z wybraną porą", true
        );
        this.changeMoonPositionSetting = new BooleanSetting(
                "change_moon_position", "Change Moon Position", "Wizualne przesunięcie księżyca", true
        );
        this.changeSkyColorSetting = new BooleanSetting(
                "change_sky_color", "Change Sky Color", "Dostosowanie barwy nieba i horyzontu", true
        );
        this.showStarsSetting = new BooleanSetting(
                "show_stars", "Show Stars", "Wyświetlanie gwiazd w nocy", true
        );
        this.preserveVanillaSkySetting = new BooleanSetting(
                "preserve_vanilla_sky", "Preserve Vanilla Sky", "Pozostaw natywny kolor nieba", false
        );
        skyGroup.addSetting(changeSunPositionSetting);
        skyGroup.addSetting(changeMoonPositionSetting);
        skyGroup.addSetting(changeSkyColorSetting);
        skyGroup.addSetting(showStarsSetting);
        skyGroup.addSetting(preserveVanillaSkySetting);
        addSettingGroup(skyGroup);

        // 3. Lighting
        SettingGroup lightingGroup = new SettingGroup("lighting", "Lighting Coordination");
        this.changeVisualLightCycleSetting = new BooleanSetting(
                "change_visual_light_cycle", "Visual Light Cycle", "Dopasowanie jasności otoczenia do wizualnej pory", true
        );
        this.preserveBlockLightingSetting = new BooleanSetting(
                "preserve_block_lighting", "Preserve Block Lighting", "Ścisła ochrona poziomów światła bloków", true
        );
        this.preserveDynamicLightingSetting = new BooleanSetting(
                "preserve_dynamic_lighting", "Preserve Dynamic Lighting", "Płynna koordynacja z modułem Lighting", true
        );
        lightingGroup.addSetting(changeVisualLightCycleSetting);
        lightingGroup.addSetting(preserveBlockLightingSetting);
        lightingGroup.addSetting(preserveDynamicLightingSetting);
        addSettingGroup(lightingGroup);

        // 4. Animation
        SettingGroup animationGroup = new SettingGroup("animation", "Transitions & Smoothing");
        this.smoothTransitionSetting = new BooleanSetting(
                "smooth_transition", "Smooth Transition", "Płynne przesuwanie słońca/księżyca przy zmianie presetu", true
        );
        this.transitionDurationSetting = new NumberSetting(
                "transition_duration", "Duration (ms)", "Czas trwania animacji zmiany czasu", 500.0, 100.0, 3000.0, 50.0
        );
        this.transitionSpeedSetting = new NumberSetting(
                "transition_speed", "Speed Multiplier", "Mnożnik prędkości animacji", 1.0, 0.5, 3.0, 0.1
        );
        this.instantChangeSetting = new BooleanSetting(
                "instant_change", "Instant Change", "Natychmiastowe ustawienie bez animacji", false
        );
        animationGroup.addSetting(smoothTransitionSetting);
        animationGroup.addSetting(transitionDurationSetting);
        animationGroup.addSetting(transitionSpeedSetting);
        animationGroup.addSetting(instantChangeSetting);
        addSettingGroup(animationGroup);

        // 5. Dimensions
        SettingGroup dimGroup = new SettingGroup("dimensions", "Dimensions");
        this.enableInOverworldSetting = new BooleanSetting(
                "enable_in_overworld", "Enable in Overworld", "Aktywny w standardowym świecie", true
        );
        this.enableInNetherSetting = new BooleanSetting(
                "enable_in_nether", "Enable in Nether", "Wymuszenie w Netherze", false
        );
        this.enableInEndSetting = new BooleanSetting(
                "enable_in_end", "Enable in End", "Wymuszenie w Endzie", false
        );
        this.enableInCustomDimensionsSetting = new BooleanSetting(
                "enable_in_custom_dims", "Custom Dimensions", "Wymuszenie w niestandardowych wymiarach modów", false
        );
        dimGroup.addSetting(enableInOverworldSetting);
        dimGroup.addSetting(enableInNetherSetting);
        dimGroup.addSetting(enableInEndSetting);
        dimGroup.addSetting(enableInCustomDimensionsSetting);
        addSettingGroup(dimGroup);

        // 6. Compatibility & Behavior
        SettingGroup compatGroup = new SettingGroup("compatibility", "Compatibility & Behavior");
        this.shaderCompatibilitySetting = new BooleanSetting(
                "shader_compatibility", "Shader Compatibility", "Zgodność z zewnętrznymi shaderami nieba", true
        );
        this.resourcePackCompatibilitySetting = new BooleanSetting(
                "resource_pack_compatibility", "Resource Pack Compatibility", "Zachowanie niestandardowych słońc z paczek", true
        );
        this.resetOnWorldChangeSetting = new BooleanSetting(
                "reset_on_world_change", "Reset on World Change", "Reset do czasu serwera po zmianie świata", false
        );
        this.preserveSettingBetweenWorldsSetting = new BooleanSetting(
                "preserve_setting_between_worlds", "Preserve Between Worlds", "Zapamiętaj wybrany czas", true
        );
        this.restoreVanillaTimeSetting = new BooleanSetting(
                "restore_vanilla_time", "Restore Vanilla Time", "Przywróć natywny czas po wyłączeniu modułu", true
        );
        compatGroup.addSetting(shaderCompatibilitySetting);
        compatGroup.addSetting(resourcePackCompatibilitySetting);
        compatGroup.addSetting(resetOnWorldChangeSetting);
        compatGroup.addSetting(preserveSettingBetweenWorldsSetting);
        compatGroup.addSetting(restoreVanillaTimeSetting);
        addSettingGroup(compatGroup);

        syncToController();
    }

    public static TimeChangerModule getInstance() {
        if (instance == null) {
            instance = new TimeChangerModule();
        }
        return instance;
    }

    public void syncToController() {
        WorldTimeVisualController controller = WorldTimeVisualController.getInstance();
        controller.setEnabled(isEnabled());
        controller.setPreset(timePresetSetting.getValue());
        controller.setCustomTimeTicks(customTimeSetting.getValue().longValue());
        controller.setTimeOffsetTicks(timeOffsetSetting.getValue().longValue());
        controller.setChangeSunPosition(changeSunPositionSetting.getValue());
        controller.setChangeMoonPosition(changeMoonPositionSetting.getValue());
        controller.setChangeSkyColor(changeSkyColorSetting.getValue());
        controller.setShowStars(showStarsSetting.getValue());
        controller.setPreserveVanillaSky(preserveVanillaSkySetting.getValue());
        controller.setEnableInOverworld(enableInOverworldSetting.getValue());
        controller.setEnableInNether(enableInNetherSetting.getValue());
        controller.setEnableInEnd(enableInEndSetting.getValue());
        controller.setEnableInCustomDimensions(enableInCustomDimensionsSetting.getValue());
        controller.getTransitionManager().configure(
                smoothTransitionSetting.getValue() && !instantChangeSetting.getValue(),
                transitionDurationSetting.getValue().longValue()
        );
    }

    @Override
    public void onEnable() {
        syncToController();
    }

    @Override
    public void onDisable() {
        WorldTimeVisualController.getInstance().setEnabled(false);
    }

    public EnumSetting<TimeChangerPreset> getTimePresetSetting() { return timePresetSetting; }
    public NumberSetting getCustomTimeSetting() { return customTimeSetting; }
    public NumberSetting getTimeOffsetSetting() { return timeOffsetSetting; }

    public BooleanSetting getChangeSunPositionSetting() { return changeSunPositionSetting; }
    public BooleanSetting getChangeMoonPositionSetting() { return changeMoonPositionSetting; }
    public BooleanSetting getChangeSkyColorSetting() { return changeSkyColorSetting; }
    public BooleanSetting getShowStarsSetting() { return showStarsSetting; }
    public BooleanSetting getPreserveVanillaSkySetting() { return preserveVanillaSkySetting; }

    public BooleanSetting getChangeVisualLightCycleSetting() { return changeVisualLightCycleSetting; }
    public BooleanSetting getPreserveBlockLightingSetting() { return preserveBlockLightingSetting; }
    public BooleanSetting getPreserveDynamicLightingSetting() { return preserveDynamicLightingSetting; }

    public BooleanSetting getSmoothTransitionSetting() { return smoothTransitionSetting; }
    public NumberSetting getTransitionDurationSetting() { return transitionDurationSetting; }
    public NumberSetting getTransitionSpeedSetting() { return transitionSpeedSetting; }
    public BooleanSetting getInstantChangeSetting() { return instantChangeSetting; }

    public BooleanSetting getEnableInOverworldSetting() { return enableInOverworldSetting; }
    public BooleanSetting getEnableInNetherSetting() { return enableInNetherSetting; }
    public BooleanSetting getEnableInEndSetting() { return enableInEndSetting; }
    public BooleanSetting getEnableInCustomDimensionsSetting() { return enableInCustomDimensionsSetting; }

    public BooleanSetting getShaderCompatibilitySetting() { return shaderCompatibilitySetting; }
    public BooleanSetting getResourcePackCompatibilitySetting() { return resourcePackCompatibilitySetting; }
    public BooleanSetting getResetOnWorldChangeSetting() { return resetOnWorldChangeSetting; }
    public BooleanSetting getPreserveSettingBetweenWorldsSetting() { return preserveSettingBetweenWorldsSetting; }
    public BooleanSetting getRestoreVanillaTimeSetting() { return restoreVanillaTimeSetting; }
}
