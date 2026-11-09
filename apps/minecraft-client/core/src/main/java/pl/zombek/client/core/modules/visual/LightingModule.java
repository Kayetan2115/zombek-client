package pl.zombek.client.core.modules.visual;

import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;
import pl.zombek.client.core.worldvisual.lighting.DynamicLightColorMode;
import pl.zombek.client.core.worldvisual.lighting.DynamicLightManager;

/**
 * Lighting module enabling dynamic ambient illumination from held items,
 * dropped items, glowing entities, and custom configured light sources.
 */
public class LightingModule extends AbstractModule {
    public static final String ID = "lighting";
    private static LightingModule instance;

    // General
    private final BooleanSetting mainHandLightSetting;
    private final BooleanSetting offhandLightSetting;
    private final BooleanSetting droppedItemLightSetting;
    private final BooleanSetting entityLightSetting;
    private final BooleanSetting customLightSourcesSetting;

    // Intensity
    private final NumberSetting lightIntensitySetting;
    private final NumberSetting maxLightLevelSetting;
    private final NumberSetting sourceStrengthSetting;
    private final NumberSetting lightFalloffSetting;

    // Distance & Capacity
    private final NumberSetting maxLightDistanceSetting;
    private final NumberSetting maxActiveSourcesSetting;
    private final NumberSetting updateIntervalSetting;

    // Appearance
    private final BooleanSetting preserveVanillaLightingSetting;
    private final EnumSetting<DynamicLightColorMode> lightColorModeSetting;
    private final ColorSetting customLightColorSetting;
    private final BooleanSetting lightFlickerSetting;
    private final NumberSetting flickerStrengthSetting;

    // Visibility
    private final BooleanSetting disableInGuiSetting;
    private final BooleanSetting disableInMenusSetting;
    private final BooleanSetting disableInSpectatorSetting;
    private final BooleanSetting disableInHudEditorSetting;

    public LightingModule() {
        super(
                ID,
                "Lighting",
                "Dynamiczne oświetlenie otoczenia przez trzymane pochodnie, przedmioty na ziemi i świecące encje.",
                ModuleCategory.VISUAL,
                GLFW.GLFW_KEY_UNKNOWN
        );
        instance = this;

        // 1. General
        SettingGroup generalGroup = new SettingGroup("general", "General");
        this.mainHandLightSetting = new BooleanSetting(
                "enable_main_hand_light", "Enable Main Hand Light", "Dynamiczne światło z przedmiotu w prawej ręce", true
        );
        this.offhandLightSetting = new BooleanSetting(
                "enable_offhand_light", "Enable Offhand Light", "Dynamiczne światło z przedmiotu w lewej ręce (offhand)", true
        );
        this.droppedItemLightSetting = new BooleanSetting(
                "enable_dropped_item_light", "Enable Dropped Item Light", "Oświetlenie z upuszczonych świecących przedmiotów na ziemi", true
        );
        this.entityLightSetting = new BooleanSetting(
                "enable_entity_light", "Enable Entity Light", "Oświetlenie z naturalnie świecących mobów (np. Blaze, Magma Cube)", true
        );
        this.customLightSourcesSetting = new BooleanSetting(
                "enable_custom_light_sources", "Enable Custom Sources", "Uwzględnianie niestandardowych źródeł światła użytkownika", true
        );
        generalGroup.addSetting(mainHandLightSetting);
        generalGroup.addSetting(offhandLightSetting);
        generalGroup.addSetting(droppedItemLightSetting);
        generalGroup.addSetting(entityLightSetting);
        generalGroup.addSetting(customLightSourcesSetting);
        addSettingGroup(generalGroup);

        // 2. Intensity
        SettingGroup intensityGroup = new SettingGroup("intensity", "Intensity");
        this.lightIntensitySetting = new NumberSetting(
                "light_intensity", "Light Intensity", "Mnożnik ogólnej jasności dynamicznego światła", 1.0, 0.1, 2.0, 0.05, 2, ""
        );
        this.maxLightLevelSetting = new NumberSetting(
                "max_light_level", "Maximum Light Level", "Maksymalny generowany poziom światła (1-15)", 15.0, 1.0, 15.0, 1.0
        );
        this.sourceStrengthSetting = new NumberSetting(
                "source_strength", "Source Strength", "Mnożnik mocy pojedynczego źródła", 1.0, 0.5, 2.0, 0.05, 2, ""
        );
        this.lightFalloffSetting = new NumberSetting(
                "light_falloff", "Light Falloff", "Współczynnik zanikania światła z odległością", 1.0, 0.5, 3.0, 0.05, 2, ""
        );
        intensityGroup.addSetting(lightIntensitySetting);
        intensityGroup.addSetting(maxLightLevelSetting);
        intensityGroup.addSetting(sourceStrengthSetting);
        intensityGroup.addSetting(lightFalloffSetting);
        addSettingGroup(intensityGroup);

        // 3. Distance & Limits
        SettingGroup distanceGroup = new SettingGroup("distance", "Distance & Performance");
        this.maxLightDistanceSetting = new NumberSetting(
                "max_light_distance", "Max Light Distance", "Maksymalny zasięg działania dynamicznych źródeł (bloki)", 24.0, 4.0, 64.0, 1.0
        );
        this.maxActiveSourcesSetting = new NumberSetting(
                "max_active_sources", "Max Active Sources", "Maksymalna liczba jednocześnie aktywnych źródeł", 12.0, 1.0, 32.0, 1.0
        );
        this.updateIntervalSetting = new NumberSetting(
                "update_interval", "Update Interval (Ticks)", "Częstotliwość odświeżania listy źródeł w tickach", 2.0, 1.0, 20.0, 1.0
        );
        distanceGroup.addSetting(maxLightDistanceSetting);
        distanceGroup.addSetting(maxActiveSourcesSetting);
        distanceGroup.addSetting(updateIntervalSetting);
        addSettingGroup(distanceGroup);

        // 4. Appearance
        SettingGroup appearanceGroup = new SettingGroup("appearance", "Appearance");
        this.preserveVanillaLightingSetting = new BooleanSetting(
                "preserve_vanilla_lighting", "Preserve Vanilla Lighting", "Ścisłe zachowanie natywnej jasności bez nasycania", false
        );
        this.lightColorModeSetting = new EnumSetting<>(
                "light_color_mode", "Color Mode", "Tonacja kolorystyczna światła: Natural, Warm, Cool, Custom",
                DynamicLightColorMode.NATURAL
        );
        this.customLightColorSetting = new ColorSetting(
                "custom_light_color", "Custom Light Color", "Niestandardowy kolor poświaty światła", 0xFFFFD700
        );
        this.lightFlickerSetting = new BooleanSetting(
                "light_flicker", "Atmospheric Flicker", "Subtelne, realistyczne migotanie pochodni", false
        );
        this.flickerStrengthSetting = new NumberSetting(
                "flicker_strength", "Flicker Strength", "Głębokość efektu migotania", 0.15, 0.0, 1.0, 0.05, 2, ""
        );
        appearanceGroup.addSetting(preserveVanillaLightingSetting);
        appearanceGroup.addSetting(lightColorModeSetting);
        appearanceGroup.addSetting(customLightColorSetting);
        appearanceGroup.addSetting(lightFlickerSetting);
        appearanceGroup.addSetting(flickerStrengthSetting);
        addSettingGroup(appearanceGroup);

        // 5. Visibility Guards
        SettingGroup visibilityGroup = new SettingGroup("visibility", "Visibility Guards");
        this.disableInGuiSetting = new BooleanSetting(
                "disable_in_gui", "Disable in GUI", "Wyłącz oświetlenie podczas otwartego GUI/ekwipunku", true
        );
        this.disableInMenusSetting = new BooleanSetting(
                "disable_in_menus", "Disable in Menus", "Wyłącz oświetlenie w menu gry", true
        );
        this.disableInSpectatorSetting = new BooleanSetting(
                "disable_in_spectator", "Disable in Spectator", "Wyłącz w trybie obserwatora", true
        );
        this.disableInHudEditorSetting = new BooleanSetting(
                "disable_in_hud_editor", "Disable in HUD Editor", "Wyłącz podczas edycji interfejsu", true
        );
        visibilityGroup.addSetting(disableInGuiSetting);
        visibilityGroup.addSetting(disableInMenusSetting);
        visibilityGroup.addSetting(disableInSpectatorSetting);
        visibilityGroup.addSetting(disableInHudEditorSetting);
        addSettingGroup(visibilityGroup);

        syncToManager();
    }

    public static LightingModule getInstance() {
        if (instance == null) {
            instance = new LightingModule();
        }
        return instance;
    }

    public void syncToManager() {
        DynamicLightManager manager = DynamicLightManager.getInstance();
        manager.setEnabled(isEnabled());
        manager.setUpdateIntervalTicks(updateIntervalSetting.getValue().intValue());
        manager.getCache().configure(
                maxActiveSourcesSetting.getValue().intValue(),
                maxLightDistanceSetting.getValue()
        );
        manager.getRenderer().configure(
                lightIntensitySetting.getValue(),
                maxLightLevelSetting.getValue().intValue(),
                sourceStrengthSetting.getValue(),
                lightFalloffSetting.getValue(),
                lightFlickerSetting.getValue(),
                flickerStrengthSetting.getValue()
        );
    }

    @Override
    public void onEnable() {
        syncToManager();
    }

    @Override
    public void onDisable() {
        DynamicLightManager.getInstance().setEnabled(false);
    }

    public BooleanSetting getMainHandLightSetting() { return mainHandLightSetting; }
    public BooleanSetting getOffhandLightSetting() { return offhandLightSetting; }
    public BooleanSetting getDroppedItemLightSetting() { return droppedItemLightSetting; }
    public BooleanSetting getEntityLightSetting() { return entityLightSetting; }
    public BooleanSetting getCustomLightSourcesSetting() { return customLightSourcesSetting; }

    public NumberSetting getLightIntensitySetting() { return lightIntensitySetting; }
    public NumberSetting getMaxLightLevelSetting() { return maxLightLevelSetting; }
    public NumberSetting getSourceStrengthSetting() { return sourceStrengthSetting; }
    public NumberSetting getLightFalloffSetting() { return lightFalloffSetting; }

    public NumberSetting getMaxLightDistanceSetting() { return maxLightDistanceSetting; }
    public NumberSetting getMaxActiveSourcesSetting() { return maxActiveSourcesSetting; }
    public NumberSetting getUpdateIntervalSetting() { return updateIntervalSetting; }

    public BooleanSetting getPreserveVanillaLightingSetting() { return preserveVanillaLightingSetting; }
    public EnumSetting<DynamicLightColorMode> getLightColorModeSetting() { return lightColorModeSetting; }
    public ColorSetting getCustomLightColorSetting() { return customLightColorSetting; }
    public BooleanSetting getLightFlickerSetting() { return lightFlickerSetting; }
    public NumberSetting getFlickerStrengthSetting() { return flickerStrengthSetting; }

    public BooleanSetting getDisableInGuiSetting() { return disableInGuiSetting; }
    public BooleanSetting getDisableInMenusSetting() { return disableInMenusSetting; }
    public BooleanSetting getDisableInSpectatorSetting() { return disableInSpectatorSetting; }
    public BooleanSetting getDisableInHudEditorSetting() { return disableInHudEditorSetting; }
}
