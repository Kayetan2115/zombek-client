package pl.zombek.client.core.modules.visual;

import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;
import pl.zombek.client.core.worldvisual.fullbright.FullBrightController;
import pl.zombek.client.core.worldvisual.fullbright.FullBrightMode;

/**
 * Full Bright module providing visual illumination boost without modifying server light data or gamma hacking.
 */
public class FullBrightModule extends AbstractModule {
    public static final String ID = "full_bright";
    private static FullBrightModule instance;

    // General
    private final EnumSetting<FullBrightMode> modeSetting;
    private final NumberSetting customBrightnessSetting;
    private final NumberSetting brightnessStrengthSetting;

    // Rendering & Color Preservation
    private final BooleanSetting preserveColorsSetting;
    private final BooleanSetting preserveVanillaLightmapSetting;
    private final BooleanSetting colorCompensationSetting;
    private final BooleanSetting reduceWashedOutEffectSetting;
    private final BooleanSetting shadowPreservationSetting;

    // Animation
    private final BooleanSetting smoothBrightnessSetting;
    private final NumberSetting transitionDurationSetting;
    private final NumberSetting transitionSpeedSetting;

    // Compatibility
    private final BooleanSetting shaderCompatibilitySetting;
    private final BooleanSetting rendererCompatibilitySetting;
    private final BooleanSetting resourcePackCompatibilitySetting;

    // Behavior
    private final BooleanSetting disableInGuiSetting;
    private final BooleanSetting restoreVanillaStateSetting;

    public FullBrightModule() {
        super(
                ID,
                "Full Bright",
                "Podwyższa jasność ciemnych obszarów świata z zachowaniem naturalnych kolorów i cieniowania.",
                ModuleCategory.VISUAL,
                GLFW.GLFW_KEY_UNKNOWN
        );
        instance = this;

        // 1. General
        SettingGroup generalGroup = new SettingGroup("general", "General");
        this.modeSetting = new EnumSetting<>(
                "mode", "Mode", "Tryb jasności: Vanilla, Full Bright, Custom, Night Vision Style",
                FullBrightMode.FULL_BRIGHT
        );
        this.customBrightnessSetting = new NumberSetting(
                "custom_brightness", "Custom Brightness", "Poziom jasności w trybie Custom (0-15)", 10.0, 0.0, 15.0, 0.5
        );
        this.brightnessStrengthSetting = new NumberSetting(
                "brightness_strength", "Brightness Strength", "Mnożnik intensywności rozjaśniania", 1.0, 0.1, 2.0, 0.05, 2, ""
        );
        generalGroup.addSetting(modeSetting);
        generalGroup.addSetting(customBrightnessSetting);
        generalGroup.addSetting(brightnessStrengthSetting);
        addSettingGroup(generalGroup);

        // 2. Rendering
        SettingGroup renderingGroup = new SettingGroup("rendering", "Color & Texture Preservation");
        this.preserveColorsSetting = new BooleanSetting(
                "preserve_colors", "Preserve Colors", "Ochrona naturalnej saturacji i balansu barw", true
        );
        this.preserveVanillaLightmapSetting = new BooleanSetting(
                "preserve_vanilla_lightmap", "Preserve Lightmap Behavior", "Zachowanie gradientu lightmapy", true
        );
        this.colorCompensationSetting = new BooleanSetting(
                "color_compensation", "Color Compensation", "Kompensacja bieli zapobiegająca wypalaniu kolorów", true
        );
        this.reduceWashedOutEffectSetting = new BooleanSetting(
                "reduce_washed_out_effect", "Reduce Washed Out Effect", "Eliminacja efektu wyblakłych szarych powierzchni", true
        );
        this.shadowPreservationSetting = new BooleanSetting(
                "shadow_preservation", "Shadow Preservation", "Subtelne zachowanie głębi i cieniowania krawędzi", true
        );
        renderingGroup.addSetting(preserveColorsSetting);
        renderingGroup.addSetting(preserveVanillaLightmapSetting);
        renderingGroup.addSetting(colorCompensationSetting);
        renderingGroup.addSetting(reduceWashedOutEffectSetting);
        renderingGroup.addSetting(shadowPreservationSetting);
        addSettingGroup(renderingGroup);

        // 3. Animation
        SettingGroup animationGroup = new SettingGroup("animation", "Transitions & Smoothing");
        this.smoothBrightnessSetting = new BooleanSetting(
                "smooth_brightness", "Smooth Transitions", "Płynna zmiana jasności zapobiegająca migotaniu ekranu", true
        );
        this.transitionDurationSetting = new NumberSetting(
                "transition_duration", "Transition Duration (ms)", "Czas trwania przejścia w milisekundach", 200.0, 50.0, 1000.0, 10.0
        );
        this.transitionSpeedSetting = new NumberSetting(
                "transition_speed", "Transition Speed", "Prędkość interpolacji jasności", 1.0, 0.5, 3.0, 0.1
        );
        animationGroup.addSetting(smoothBrightnessSetting);
        animationGroup.addSetting(transitionDurationSetting);
        animationGroup.addSetting(transitionSpeedSetting);
        addSettingGroup(animationGroup);

        // 4. Compatibility
        SettingGroup compatGroup = new SettingGroup("compatibility", "Compatibility & Safety");
        this.shaderCompatibilitySetting = new BooleanSetting(
                "shader_compatibility", "Shader Compatibility", "Automatyczne dostosowanie do zewnętrznych shaderów", true
        );
        this.rendererCompatibilitySetting = new BooleanSetting(
                "renderer_compatibility", "Renderer Compatibility", "Zgodność z optymalizacjami Sodium / Iris", true
        );
        this.resourcePackCompatibilitySetting = new BooleanSetting(
                "resource_pack_compatibility", "Resource Pack Compatibility", "Zgodność z niestandardowymi lightmapami resource packów", true
        );
        compatGroup.addSetting(shaderCompatibilitySetting);
        compatGroup.addSetting(rendererCompatibilitySetting);
        compatGroup.addSetting(resourcePackCompatibilitySetting);
        addSettingGroup(compatGroup);

        // 5. Behavior
        SettingGroup behaviorGroup = new SettingGroup("behavior", "Behavior");
        this.disableInGuiSetting = new BooleanSetting(
                "disable_in_gui", "Disable in GUI", "Wyłącz rozjaśnianie podczas otwartego GUI/ekwipunku", false
        );
        this.restoreVanillaStateSetting = new BooleanSetting(
                "restore_vanilla_state", "Restore Vanilla State", "Przywróć natywne światło po wyłączeniu modułu", true
        );
        behaviorGroup.addSetting(disableInGuiSetting);
        behaviorGroup.addSetting(restoreVanillaStateSetting);
        addSettingGroup(behaviorGroup);

        syncToController();
    }

    public static FullBrightModule getInstance() {
        if (instance == null) {
            instance = new FullBrightModule();
        }
        return instance;
    }

    public void syncToController() {
        FullBrightController controller = FullBrightController.getInstance();
        controller.setEnabled(isEnabled());
        controller.setMode(modeSetting.getValue());
        controller.setCustomBrightness(customBrightnessSetting.getValue());
        controller.setBrightnessStrength(brightnessStrengthSetting.getValue());
        controller.setPreserveColors(preserveColorsSetting.getValue());
        controller.setPreserveLightmapBehavior(preserveVanillaLightmapSetting.getValue());
        controller.setColorCompensation(colorCompensationSetting.getValue());
        controller.setReduceWashedOutEffect(reduceWashedOutEffectSetting.getValue());
        controller.setShadowPreservation(shadowPreservationSetting.getValue());
        controller.setSmoothBrightness(smoothBrightnessSetting.getValue());
        controller.setTransitionDurationMs(transitionDurationSetting.getValue().longValue());
    }

    @Override
    public void onEnable() {
        syncToController();
    }

    @Override
    public void onDisable() {
        FullBrightController.getInstance().setEnabled(false);
    }

    public EnumSetting<FullBrightMode> getModeSetting() { return modeSetting; }
    public NumberSetting getCustomBrightnessSetting() { return customBrightnessSetting; }
    public NumberSetting getBrightnessStrengthSetting() { return brightnessStrengthSetting; }

    public BooleanSetting getPreserveColorsSetting() { return preserveColorsSetting; }
    public BooleanSetting getPreserveVanillaLightmapSetting() { return preserveVanillaLightmapSetting; }
    public BooleanSetting getColorCompensationSetting() { return colorCompensationSetting; }
    public BooleanSetting getReduceWashedOutEffectSetting() { return reduceWashedOutEffectSetting; }
    public BooleanSetting getShadowPreservationSetting() { return shadowPreservationSetting; }

    public BooleanSetting getSmoothBrightnessSetting() { return smoothBrightnessSetting; }
    public NumberSetting getTransitionDurationSetting() { return transitionDurationSetting; }
    public NumberSetting getTransitionSpeedSetting() { return transitionSpeedSetting; }

    public BooleanSetting getShaderCompatibilitySetting() { return shaderCompatibilitySetting; }
    public BooleanSetting getRendererCompatibilitySetting() { return rendererCompatibilitySetting; }
    public BooleanSetting getResourcePackCompatibilitySetting() { return resourcePackCompatibilitySetting; }

    public BooleanSetting getDisableInGuiSetting() { return disableInGuiSetting; }
    public BooleanSetting getRestoreVanillaStateSetting() { return restoreVanillaStateSetting; }
}
