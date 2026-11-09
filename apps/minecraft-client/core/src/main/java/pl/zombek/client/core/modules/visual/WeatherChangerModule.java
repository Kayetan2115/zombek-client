package pl.zombek.client.core.modules.visual;

import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;
import pl.zombek.client.core.worldvisual.weather.WeatherPreset;
import pl.zombek.client.core.worldvisual.weather.WeatherVisualController;

/**
 * Weather Changer module providing local visual atmosphere customization
 * (Clear, Rain, Snow, Thunder) without executing commands or modifying server weather.
 */
public class WeatherChangerModule extends AbstractModule {
    public static final String ID = "weather_changer";
    private static WeatherChangerModule instance;

    // General
    private final EnumSetting<WeatherPreset> weatherPresetSetting;

    // Rain
    private final BooleanSetting enableRainSetting;
    private final NumberSetting rainIntensitySetting;
    private final NumberSetting rainParticleDensitySetting;
    private final NumberSetting rainSpeedSetting;
    private final NumberSetting rainOpacitySetting;

    // Snow
    private final BooleanSetting enableSnowSetting;
    private final NumberSetting snowIntensitySetting;
    private final NumberSetting snowParticleDensitySetting;
    private final NumberSetting snowSizeSetting;
    private final NumberSetting snowOpacitySetting;

    // Clouds
    private final BooleanSetting showCloudsSetting;
    private final NumberSetting cloudDensitySetting;
    private final NumberSetting cloudOpacitySetting;
    private final NumberSetting cloudSpeedSetting;

    // Thunder
    private final BooleanSetting enableVisualThunderSetting;
    private final NumberSetting thunderIntensitySetting;
    private final NumberSetting flashIntensitySetting;

    // Performance
    private final NumberSetting maxParticlesSetting;

    // Animation
    private final BooleanSetting smoothTransitionSetting;
    private final NumberSetting transitionDurationSetting;
    private final BooleanSetting instantChangeSetting;

    // Dimensions & Climate
    private final BooleanSetting enableInOverworldSetting;
    private final BooleanSetting enableInNetherSetting;
    private final BooleanSetting enableInEndSetting;
    private final BooleanSetting respectBiomeClimateSetting;
    private final BooleanSetting overrideBiomeRestrictionsSetting;

    // Compatibility & Behavior
    private final BooleanSetting shaderCompatibilitySetting;
    private final BooleanSetting resourcePackCompatibilitySetting;
    private final BooleanSetting resetOnWorldChangeSetting;
    private final BooleanSetting preserveSettingBetweenWorldsSetting;
    private final BooleanSetting restoreVanillaWeatherSetting;

    public WeatherChangerModule() {
        super(
                ID,
                "Weather Changer",
                "Lokalna, wizualna zmiana pogody (Słońce, Deszcz, Śnieg, Burza) bez ingerencji w pogodę serwera.",
                ModuleCategory.VISUAL,
                GLFW.GLFW_KEY_UNKNOWN
        );
        instance = this;

        // 1. General
        SettingGroup generalGroup = new SettingGroup("general", "General");
        this.weatherPresetSetting = new EnumSetting<>(
                "weather_preset", "Weather Preset", "Gotowy profil pogody: Clear, Rain, Snow, Thunder, Custom, Vanilla",
                WeatherPreset.CLEAR
        );
        generalGroup.addSetting(weatherPresetSetting);
        addSettingGroup(generalGroup);

        // 2. Rain
        SettingGroup rainGroup = new SettingGroup("rain", "Rain Visuals");
        this.enableRainSetting = new BooleanSetting(
                "enable_rain", "Enable Rain", "Renderowanie kropel deszczu", true
        );
        this.rainIntensitySetting = new NumberSetting(
                "rain_intensity", "Rain Intensity", "Gęstość opadów deszczu", 1.0, 0.0, 1.0, 0.05, 2, ""
        );
        this.rainParticleDensitySetting = new NumberSetting(
                "rain_particle_density", "Particle Density", "Zagęszczenie cząsteczek deszczu", 1.0, 0.1, 2.0, 0.1
        );
        this.rainSpeedSetting = new NumberSetting(
                "rain_speed", "Falling Speed", "Prędkość opadania kropel", 1.0, 0.5, 2.0, 0.1
        );
        this.rainOpacitySetting = new NumberSetting(
                "rain_opacity", "Rain Opacity", "Przezroczystość kropel", 1.0, 0.1, 1.0, 0.05, 2, ""
        );
        rainGroup.addSetting(enableRainSetting);
        rainGroup.addSetting(rainIntensitySetting);
        rainGroup.addSetting(rainParticleDensitySetting);
        rainGroup.addSetting(rainSpeedSetting);
        rainGroup.addSetting(rainOpacitySetting);
        addSettingGroup(rainGroup);

        // 3. Snow
        SettingGroup snowGroup = new SettingGroup("snow", "Snow Visuals");
        this.enableSnowSetting = new BooleanSetting(
                "enable_snow", "Enable Snow", "Renderowanie płatków śniegu", true
        );
        this.snowIntensitySetting = new NumberSetting(
                "snow_intensity", "Snow Intensity", "Intensywność opadów śniegu", 1.0, 0.0, 1.0, 0.05, 2, ""
        );
        this.snowParticleDensitySetting = new NumberSetting(
                "snow_particle_density", "Snow Particle Density", "Zagęszczenie płatków", 1.0, 0.1, 2.0, 0.1
        );
        this.snowSizeSetting = new NumberSetting(
                "snow_size", "Flake Size", "Wielkość płatków śniegu", 1.0, 0.5, 2.0, 0.1
        );
        this.snowOpacitySetting = new NumberSetting(
                "snow_opacity", "Snow Opacity", "Przezroczystość śniegu", 1.0, 0.1, 1.0, 0.05, 2, ""
        );
        snowGroup.addSetting(enableSnowSetting);
        snowGroup.addSetting(snowIntensitySetting);
        snowGroup.addSetting(snowParticleDensitySetting);
        snowGroup.addSetting(snowSizeSetting);
        snowGroup.addSetting(snowOpacitySetting);
        addSettingGroup(snowGroup);

        // 4. Clouds
        SettingGroup cloudGroup = new SettingGroup("clouds", "Clouds & Sky");
        this.showCloudsSetting = new BooleanSetting(
                "show_clouds", "Show Clouds", "Wyświetlanie chmur", true
        );
        this.cloudDensitySetting = new NumberSetting(
                "cloud_density", "Cloud Density", "Gęstość chmur", 1.0, 0.1, 2.0, 0.1
        );
        this.cloudOpacitySetting = new NumberSetting(
                "cloud_opacity", "Cloud Opacity", "Przezroczystość chmur", 1.0, 0.0, 1.0, 0.05, 2, ""
        );
        this.cloudSpeedSetting = new NumberSetting(
                "cloud_speed", "Cloud Speed", "Prędkość przemieszczania chmur", 1.0, 0.1, 3.0, 0.1
        );
        cloudGroup.addSetting(showCloudsSetting);
        cloudGroup.addSetting(cloudDensitySetting);
        cloudGroup.addSetting(cloudOpacitySetting);
        cloudGroup.addSetting(cloudSpeedSetting);
        addSettingGroup(cloudGroup);

        // 5. Thunder & Lightning
        SettingGroup thunderGroup = new SettingGroup("thunder", "Thunder & Lightning");
        this.enableVisualThunderSetting = new BooleanSetting(
                "enable_visual_thunder", "Visual Thunder", "Efekt zachmurzenia burzowego", false
        );
        this.thunderIntensitySetting = new NumberSetting(
                "thunder_intensity", "Thunder Intensity", "Głębokość zaciemnienia nieba", 0.8, 0.0, 1.0, 0.05, 2, ""
        );
        this.flashIntensitySetting = new NumberSetting(
                "flash_intensity", "Flash Intensity", "Intensywność błysków na niebie", 0.5, 0.0, 1.0, 0.05, 2, ""
        );
        thunderGroup.addSetting(enableVisualThunderSetting);
        thunderGroup.addSetting(thunderIntensitySetting);
        thunderGroup.addSetting(flashIntensitySetting);
        addSettingGroup(thunderGroup);

        // 6. Performance
        SettingGroup perfGroup = new SettingGroup("performance", "Performance");
        this.maxParticlesSetting = new NumberSetting(
                "max_weather_particles", "Max Weather Particles", "Limit cząsteczek pogodowych", 500.0, 50.0, 2000.0, 50.0
        );
        perfGroup.addSetting(maxParticlesSetting);
        addSettingGroup(perfGroup);

        // 7. Animation
        SettingGroup animGroup = new SettingGroup("animation", "Transitions");
        this.smoothTransitionSetting = new BooleanSetting(
                "smooth_transition", "Smooth Transition", "Płynne narastanie/zanikanie deszczu", true
        );
        this.transitionDurationSetting = new NumberSetting(
                "transition_duration", "Duration (ms)", "Czas trwania przejścia pogodowego", 1000.0, 100.0, 3000.0, 100.0
        );
        this.instantChangeSetting = new BooleanSetting(
                "instant_change", "Instant Change", "Natychmiastowa zmiana bez animacji", false
        );
        animGroup.addSetting(smoothTransitionSetting);
        animGroup.addSetting(transitionDurationSetting);
        animGroup.addSetting(instantChangeSetting);
        addSettingGroup(animGroup);

        // 8. Dimensions & Climate
        SettingGroup dimGroup = new SettingGroup("dimensions", "Dimensions & Climate");
        this.enableInOverworldSetting = new BooleanSetting(
                "enable_in_overworld", "Enable in Overworld", "Aktywny w standardowym świecie", true
        );
        this.enableInNetherSetting = new BooleanSetting(
                "enable_in_nether", "Enable in Nether", "Wymuszenie w Netherze", false
        );
        this.enableInEndSetting = new BooleanSetting(
                "enable_in_end", "Enable in End", "Wymuszenie w Endzie", false
        );
        this.respectBiomeClimateSetting = new BooleanSetting(
                "respect_biome_climate", "Respect Biome Climate", "Nie wymuszaj deszczu na pustyniach", true
        );
        this.overrideBiomeRestrictionsSetting = new BooleanSetting(
                "override_biome_restrictions", "Override Climate", "Wymuś opady bez względu na suchość biomu", false
        );
        dimGroup.addSetting(enableInOverworldSetting);
        dimGroup.addSetting(enableInNetherSetting);
        dimGroup.addSetting(enableInEndSetting);
        dimGroup.addSetting(respectBiomeClimateSetting);
        dimGroup.addSetting(overrideBiomeRestrictionsSetting);
        addSettingGroup(dimGroup);

        // 9. Compatibility & Behavior
        SettingGroup compatGroup = new SettingGroup("compatibility", "Compatibility & Behavior");
        this.shaderCompatibilitySetting = new BooleanSetting(
                "shader_compatibility", "Shader Compatibility", "Zgodność z shaderami pogody", true
        );
        this.resourcePackCompatibilitySetting = new BooleanSetting(
                "resource_pack_compatibility", "Resource Pack Compatibility", "Zachowanie tekstur deszczu z paczek", true
        );
        this.resetOnWorldChangeSetting = new BooleanSetting(
                "reset_on_world_change", "Reset on World Change", "Reset do pogody serwera po zmianie świata", false
        );
        this.preserveSettingBetweenWorldsSetting = new BooleanSetting(
                "preserve_setting_between_worlds", "Preserve Between Worlds", "Zapamiętaj wybraną pogodę", true
        );
        this.restoreVanillaWeatherSetting = new BooleanSetting(
                "restore_vanilla_weather", "Restore Vanilla Weather", "Przywróć pogodę serwera po wyłączeniu modułu", true
        );
        compatGroup.addSetting(shaderCompatibilitySetting);
        compatGroup.addSetting(resourcePackCompatibilitySetting);
        compatGroup.addSetting(resetOnWorldChangeSetting);
        compatGroup.addSetting(preserveSettingBetweenWorldsSetting);
        compatGroup.addSetting(restoreVanillaWeatherSetting);
        addSettingGroup(compatGroup);

        syncToController();
    }

    public static WeatherChangerModule getInstance() {
        if (instance == null) {
            instance = new WeatherChangerModule();
        }
        return instance;
    }

    public void syncToController() {
        WeatherVisualController controller = WeatherVisualController.getInstance();
        controller.setEnabled(isEnabled());
        controller.setPreset(weatherPresetSetting.getValue());
        controller.setEnableRain(enableRainSetting.getValue());
        controller.setRainIntensity(rainIntensitySetting.getValue().floatValue());
        controller.setRainSpeed(rainSpeedSetting.getValue().floatValue());
        controller.setRainOpacity(rainOpacitySetting.getValue().floatValue());
        controller.setEnableSnow(enableSnowSetting.getValue());
        controller.setSnowIntensity(snowIntensitySetting.getValue().floatValue());
        controller.setSnowSize(snowSizeSetting.getValue().floatValue());
        controller.setSnowOpacity(snowOpacitySetting.getValue().floatValue());
        controller.setShowClouds(showCloudsSetting.getValue());
        controller.setCloudDensity(cloudDensitySetting.getValue().floatValue());
        controller.setCloudOpacity(cloudOpacitySetting.getValue().floatValue());
        controller.setCloudSpeed(cloudSpeedSetting.getValue().floatValue());
        controller.setEnableVisualThunder(enableVisualThunderSetting.getValue());
        controller.setThunderIntensity(thunderIntensitySetting.getValue().floatValue());
        controller.setFlashIntensity(flashIntensitySetting.getValue().floatValue());
        controller.setMaxWeatherParticles(maxParticlesSetting.getValue().intValue());
        controller.setEnableInOverworld(enableInOverworldSetting.getValue());
        controller.setEnableInNether(enableInNetherSetting.getValue());
        controller.setEnableInEnd(enableInEndSetting.getValue());
        controller.setRespectBiomeClimate(respectBiomeClimateSetting.getValue());
        controller.setOverrideBiomeRestrictions(overrideBiomeRestrictionsSetting.getValue());
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
        WeatherVisualController.getInstance().setEnabled(false);
    }

    public EnumSetting<WeatherPreset> getWeatherPresetSetting() { return weatherPresetSetting; }

    public BooleanSetting getEnableRainSetting() { return enableRainSetting; }
    public NumberSetting getRainIntensitySetting() { return rainIntensitySetting; }
    public NumberSetting getRainParticleDensitySetting() { return rainParticleDensitySetting; }
    public NumberSetting getRainSpeedSetting() { return rainSpeedSetting; }
    public NumberSetting getRainOpacitySetting() { return rainOpacitySetting; }

    public BooleanSetting getEnableSnowSetting() { return enableSnowSetting; }
    public NumberSetting getSnowIntensitySetting() { return snowIntensitySetting; }
    public NumberSetting getSnowParticleDensitySetting() { return snowParticleDensitySetting; }
    public NumberSetting getSnowSizeSetting() { return snowSizeSetting; }
    public NumberSetting getSnowOpacitySetting() { return snowOpacitySetting; }

    public BooleanSetting getShowCloudsSetting() { return showCloudsSetting; }
    public NumberSetting getCloudDensitySetting() { return cloudDensitySetting; }
    public NumberSetting getCloudOpacitySetting() { return cloudOpacitySetting; }
    public NumberSetting getCloudSpeedSetting() { return cloudSpeedSetting; }

    public BooleanSetting getEnableVisualThunderSetting() { return enableVisualThunderSetting; }
    public NumberSetting getThunderIntensitySetting() { return thunderIntensitySetting; }
    public NumberSetting getFlashIntensitySetting() { return flashIntensitySetting; }

    public NumberSetting getMaxParticlesSetting() { return maxParticlesSetting; }

    public BooleanSetting getSmoothTransitionSetting() { return smoothTransitionSetting; }
    public NumberSetting getTransitionDurationSetting() { return transitionDurationSetting; }
    public BooleanSetting getInstantChangeSetting() { return instantChangeSetting; }

    public BooleanSetting getEnableInOverworldSetting() { return enableInOverworldSetting; }
    public BooleanSetting getEnableInNetherSetting() { return enableInNetherSetting; }
    public BooleanSetting getEnableInEndSetting() { return enableInEndSetting; }
    public BooleanSetting getRespectBiomeClimateSetting() { return respectBiomeClimateSetting; }
    public BooleanSetting getOverrideBiomeRestrictionsSetting() { return overrideBiomeRestrictionsSetting; }

    public BooleanSetting getShaderCompatibilitySetting() { return shaderCompatibilitySetting; }
    public BooleanSetting getResourcePackCompatibilitySetting() { return resourcePackCompatibilitySetting; }
    public BooleanSetting getResetOnWorldChangeSetting() { return resetOnWorldChangeSetting; }
    public BooleanSetting getPreserveSettingBetweenWorldsSetting() { return preserveSettingBetweenWorldsSetting; }
    public BooleanSetting getRestoreVanillaWeatherSetting() { return restoreVanillaWeatherSetting; }
}
