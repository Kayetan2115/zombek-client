package pl.zombek.client.core.modules.visual;

import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.rendereffect.fog.FogEnvironmentType;
import pl.zombek.client.core.rendereffect.fog.FogPreset;
import pl.zombek.client.core.rendereffect.fog.FogVisualController;
import pl.zombek.client.core.settings.*;

/**
 * Fog Customizer Module allowing granular client-side control over Minecraft fog density,
 * distance, colors, and per-environment profiles without wallhack or chunk overflow exploits.
 */
public class FogCustomizerModule extends AbstractModule {
    public static final String ID = "fog_customizer";
    private static FogCustomizerModule instance;

    // General
    private final EnumSetting<FogPreset> presetSetting;
    private final NumberSetting fogStrengthSetting;
    private final NumberSetting fogDensitySetting;
    private final NumberSetting fogFalloffSetting;

    // Distance
    private final NumberSetting fogStartSetting;
    private final NumberSetting fogEndSetting;
    private final NumberSetting minFogDistanceSetting;
    private final NumberSetting maxFogDistanceSetting;

    // Color
    private final BooleanSetting useCustomColorSetting;
    private final ColorSetting customColorSetting;
    private final NumberSetting colorBlendSetting;

    // Environment
    private final BooleanSetting airFogSetting;
    private final BooleanSetting waterFogSetting;
    private final BooleanSetting lavaFogSetting;
    private final BooleanSetting powderSnowFogSetting;
    private final BooleanSetting dimensionFogSetting;
    private final BooleanSetting biomeFogSetting;
    private final BooleanSetting bossFogSetting;

    // Animation
    private final BooleanSetting smoothTransitionSetting;
    private final NumberSetting transitionDurationMsSetting;

    // Dimensions
    private final BooleanSetting enableInOverworldSetting;
    private final BooleanSetting enableInNetherSetting;
    private final BooleanSetting enableInEndSetting;
    private final BooleanSetting enableInCustomDimensionsSetting;

    // Behavior
    private final BooleanSetting resetOnWorldChangeSetting;
    private final BooleanSetting restoreVanillaFogSetting;

    public FogCustomizerModule() {
        super(
                ID,
                "Fog Customizer",
                "Personalizacja wyglądu, odległości i kolorów mgły w poszczególnych środowiskach i wymiarach.",
                ModuleCategory.VISUAL,
                GLFW.GLFW_KEY_UNKNOWN
        );
        instance = this;

        // 1. General
        SettingGroup generalGroup = new SettingGroup("general", "General");
        this.presetSetting = new EnumSetting<>(
                "preset", "Fog Preset", "Wybór profilu mgły (Vanilla, Reduced, Clear View, Dense, Soft, Custom)",
                FogPreset.VANILLA
        );
        this.fogStrengthSetting = new NumberSetting(
                "fog_strength", "Fog Strength", "Mnożnik intensywności rozmycia mgły", 1.0, 0.1, 3.0, 0.05, 2, ""
        );
        this.fogDensitySetting = new NumberSetting(
                "fog_density", "Fog Density", "Gęstość cząstek mgły", 1.0, 0.0, 2.0, 0.05, 2, ""
        );
        this.fogFalloffSetting = new NumberSetting(
                "fog_falloff", "Fog Falloff", "Krzywa zaniku krawędzi mgły", 1.0, 0.2, 3.0, 0.05, 2, ""
        );
        generalGroup.addSetting(presetSetting);
        generalGroup.addSetting(fogStrengthSetting);
        generalGroup.addSetting(fogDensitySetting);
        generalGroup.addSetting(fogFalloffSetting);
        addSettingGroup(generalGroup);

        // 2. Distance
        SettingGroup distanceGroup = new SettingGroup("distance", "Distance");
        this.fogStartSetting = new NumberSetting(
                "fog_start", "Fog Start Distance", "Odległość rozpoczęcia mgły od gracza w blokach", 16.0, 0.0, 512.0, 1.0, 0, "m"
        );
        this.fogEndSetting = new NumberSetting(
                "fog_end", "Fog End Distance", "Odległość całkowitego przesłonięcia mgłą w blokach", 128.0, 4.0, 1024.0, 1.0, 0, "m"
        );
        this.minFogDistanceSetting = new NumberSetting(
                "min_fog_distance", "Minimum Fog Distance", "Dolna granica bezpiecznego odcięcia mgły", 0.0, 0.0, 128.0, 1.0, 0, "m"
        );
        this.maxFogDistanceSetting = new NumberSetting(
                "max_fog_distance", "Maximum Fog Distance", "Górna granica zasięgu mgły ograniczona do widoczności", 512.0, 64.0, 1024.0, 1.0, 0, "m"
        );
        distanceGroup.addSetting(fogStartSetting);
        distanceGroup.addSetting(fogEndSetting);
        distanceGroup.addSetting(minFogDistanceSetting);
        distanceGroup.addSetting(maxFogDistanceSetting);
        addSettingGroup(distanceGroup);

        // 3. Color
        SettingGroup colorGroup = new SettingGroup("color", "Color");
        this.useCustomColorSetting = new BooleanSetting(
                "use_custom_color", "Use Custom Color", "Zezwalaj na własny kolor zabarwienia mgły", false
        );
        this.customColorSetting = new ColorSetting(
                "custom_color", "Custom Fog Color", "Własna barwa mgły w formacie ARGB", 0xFFC0D8FF
        );
        this.colorBlendSetting = new NumberSetting(
                "color_blend", "Color Blend Factor", "Stopień zmieszania własnego koloru z waniliowym", 1.0, 0.0, 1.0, 0.05, 2, ""
        );
        colorGroup.addSetting(useCustomColorSetting);
        colorGroup.addSetting(customColorSetting);
        colorGroup.addSetting(colorBlendSetting);
        addSettingGroup(colorGroup);

        // 4. Environment
        SettingGroup envGroup = new SettingGroup("environment", "Environment");
        this.airFogSetting = new BooleanSetting("air_fog", "Air Fog", "Aktywuj modyfikację mgły powietrznej", true);
        this.waterFogSetting = new BooleanSetting("water_fog", "Water Fog", "Aktywuj modyfikację mgły podwodnej", true);
        this.lavaFogSetting = new BooleanSetting("lava_fog", "Lava Fog", "Aktywuj modyfikację mgły w lawie", true);
        this.powderSnowFogSetting = new BooleanSetting("powder_snow_fog", "Powder Snow Fog", "Modyfikacja w sypkim śniegu", true);
        this.dimensionFogSetting = new BooleanSetting("dimension_fog", "Dimension Fog", "Modyfikacja mgły wymiaru", true);
        this.biomeFogSetting = new BooleanSetting("biome_fog", "Biome Fog", "Modyfikacja mgły powiązanej z biomem", true);
        this.bossFogSetting = new BooleanSetting("boss_fog", "Boss Fog", "Modyfikacja mgły podczas walk z bossami", true);
        envGroup.addSetting(airFogSetting);
        envGroup.addSetting(waterFogSetting);
        envGroup.addSetting(lavaFogSetting);
        envGroup.addSetting(powderSnowFogSetting);
        envGroup.addSetting(dimensionFogSetting);
        envGroup.addSetting(biomeFogSetting);
        envGroup.addSetting(bossFogSetting);
        addSettingGroup(envGroup);

        // 5. Animation
        SettingGroup animGroup = new SettingGroup("animation", "Animation");
        this.smoothTransitionSetting = new BooleanSetting(
                "smooth_transition", "Smooth Transition", "Płynne, nieliniowe animowanie zmian mgły", true
        );
        this.transitionDurationMsSetting = new NumberSetting(
                "transition_duration_ms", "Transition Duration", "Czas trwania płynnej tranzycji w ms", 400.0, 50.0, 3000.0, 25.0, 0, "ms"
        );
        animGroup.addSetting(smoothTransitionSetting);
        animGroup.addSetting(transitionDurationMsSetting);
        addSettingGroup(animGroup);

        // 6. Dimensions
        SettingGroup dimGroup = new SettingGroup("dimensions", "Dimensions");
        this.enableInOverworldSetting = new BooleanSetting("enable_in_overworld", "Enable in Overworld", "Aktywny w świecie Overworld", true);
        this.enableInNetherSetting = new BooleanSetting("enable_in_nether", "Enable in Nether", "Aktywny w wymiarze Nether", true);
        this.enableInEndSetting = new BooleanSetting("enable_in_end", "Enable in End", "Aktywny w wymiarze The End", true);
        this.enableInCustomDimensionsSetting = new BooleanSetting("enable_in_custom_dimensions", "Enable in Custom Dimensions", "Aktywny w wymiarach z modów", true);
        dimGroup.addSetting(enableInOverworldSetting);
        dimGroup.addSetting(enableInNetherSetting);
        dimGroup.addSetting(enableInEndSetting);
        dimGroup.addSetting(enableInCustomDimensionsSetting);
        addSettingGroup(dimGroup);

        // 7. Behavior
        SettingGroup behaviorGroup = new SettingGroup("behavior", "Behavior");
        this.resetOnWorldChangeSetting = new BooleanSetting("reset_on_world_change", "Reset on World Change", "Resetowanie buforów przy zmianie świata", true);
        this.restoreVanillaFogSetting = new BooleanSetting("restore_vanilla_fog", "Restore Vanilla on Disable", "Natychmiastowe przywrócenie mgły waniliowej", true);
        behaviorGroup.addSetting(resetOnWorldChangeSetting);
        behaviorGroup.addSetting(restoreVanillaFogSetting);
        addSettingGroup(behaviorGroup);
    }

    public static FogCustomizerModule getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        syncToController();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        FogVisualController.getInstance().setEnabled(false);
    }

    public void syncToController() {
        FogVisualController controller = FogVisualController.getInstance();
        controller.setEnabled(isEnabled());
        controller.getProfileManager().setActivePreset(presetSetting.getValue());

        controller.getDistanceController().setCustomFogStart(fogStartSetting.getValue().floatValue());
        controller.getDistanceController().setCustomFogEnd(fogEndSetting.getValue().floatValue());
        controller.getDistanceController().setFogStrength(fogStrengthSetting.getValue().floatValue());
        controller.getDistanceController().setFogDensity(fogDensitySetting.getValue().floatValue());
        controller.getDistanceController().setFogFalloff(fogFalloffSetting.getValue().floatValue());
        controller.getDistanceController().setMinFogDistance(minFogDistanceSetting.getValue().floatValue());
        controller.getDistanceController().setMaxFogDistance(maxFogDistanceSetting.getValue().floatValue());

        controller.getColorController().setUseCustomColor(useCustomColorSetting.getValue());
        controller.getColorController().setCustomColorArgb(customColorSetting.getValue());
        controller.getColorController().setColorBlend(colorBlendSetting.getValue().floatValue());

        controller.getEnvironmentManager().getProfile(FogEnvironmentType.AIR).setEnabled(airFogSetting.getValue());
        controller.getEnvironmentManager().getProfile(FogEnvironmentType.WATER).setEnabled(waterFogSetting.getValue());
        controller.getEnvironmentManager().getProfile(FogEnvironmentType.LAVA).setEnabled(lavaFogSetting.getValue());
        controller.getEnvironmentManager().getProfile(FogEnvironmentType.POWDER_SNOW).setEnabled(powderSnowFogSetting.getValue());
        controller.getEnvironmentManager().getProfile(FogEnvironmentType.DIMENSION).setEnabled(dimensionFogSetting.getValue());
        controller.getEnvironmentManager().getProfile(FogEnvironmentType.BIOME).setEnabled(biomeFogSetting.getValue());
        controller.getEnvironmentManager().getProfile(FogEnvironmentType.BOSS).setEnabled(bossFogSetting.getValue());

        controller.setSmoothTransition(smoothTransitionSetting.getValue());
        controller.setTransitionDurationMs(transitionDurationMsSetting.getValue().longValue());

        controller.setEnableInOverworld(enableInOverworldSetting.getValue());
        controller.setEnableInNether(enableInNetherSetting.getValue());
        controller.setEnableInEnd(enableInEndSetting.getValue());
        controller.setEnableInCustomDimensions(enableInCustomDimensionsSetting.getValue());
    }

    public EnumSetting<FogPreset> getPresetSetting() { return presetSetting; }
    public NumberSetting getFogStrengthSetting() { return fogStrengthSetting; }
    public NumberSetting getFogDensitySetting() { return fogDensitySetting; }
    public NumberSetting getFogFalloffSetting() { return fogFalloffSetting; }
    public NumberSetting getFogStartSetting() { return fogStartSetting; }
    public NumberSetting getFogEndSetting() { return fogEndSetting; }
    public NumberSetting getMinFogDistanceSetting() { return minFogDistanceSetting; }
    public NumberSetting getMaxFogDistanceSetting() { return maxFogDistanceSetting; }
    public BooleanSetting getUseCustomColorSetting() { return useCustomColorSetting; }
    public ColorSetting getCustomColorSetting() { return customColorSetting; }
    public NumberSetting getColorBlendSetting() { return colorBlendSetting; }
    public BooleanSetting getAirFogSetting() { return airFogSetting; }
    public BooleanSetting getWaterFogSetting() { return waterFogSetting; }
    public BooleanSetting getLavaFogSetting() { return lavaFogSetting; }
    public BooleanSetting getPowderSnowFogSetting() { return powderSnowFogSetting; }
    public BooleanSetting getDimensionFogSetting() { return dimensionFogSetting; }
    public BooleanSetting getBiomeFogSetting() { return biomeFogSetting; }
    public BooleanSetting getBossFogSetting() { return bossFogSetting; }
    public BooleanSetting getSmoothTransitionSetting() { return smoothTransitionSetting; }
    public NumberSetting getTransitionDurationMsSetting() { return transitionDurationMsSetting; }
    public BooleanSetting getEnableInOverworldSetting() { return enableInOverworldSetting; }
    public BooleanSetting getEnableInNetherSetting() { return enableInNetherSetting; }
    public BooleanSetting getEnableInEndSetting() { return enableInEndSetting; }
    public BooleanSetting getEnableInCustomDimensionsSetting() { return enableInCustomDimensionsSetting; }
    public BooleanSetting getResetOnWorldChangeSetting() { return resetOnWorldChangeSetting; }
    public BooleanSetting getRestoreVanillaFogSetting() { return restoreVanillaFogSetting; }
}
