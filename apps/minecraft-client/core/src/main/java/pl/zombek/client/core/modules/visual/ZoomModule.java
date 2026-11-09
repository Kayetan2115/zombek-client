package pl.zombek.client.core.modules.visual;

import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.camera.ZoomActivationMode;
import pl.zombek.client.core.camera.ZoomEasing;
import pl.zombek.client.core.camera.ZoomIndicatorStyle;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

/**
 * Zoom module enabling smooth optical magnification of the Minecraft viewport (Lunar Client style).
 * Purely local camera perspective adjustment; never changes player reach, position, or server packets.
 */
public class ZoomModule extends AbstractModule {
    public static final String ID = "zoom";
    private static ZoomModule instance;

    // General
    private final EnumSetting<ZoomActivationMode> activationModeSetting;
    private final NumberSetting defaultZoomSetting;
    private final NumberSetting minZoomSetting;
    private final NumberSetting maxZoomSetting;
    private final NumberSetting zoomMultiplierSetting;

    // Controls
    private final KeybindSetting zoomKeySetting;
    private final BooleanSetting scrollZoomSetting;
    private final NumberSetting scrollSensitivitySetting;
    private final NumberSetting scrollStepSetting;
    private final KeybindSetting resetZoomKeySetting;

    // Animation
    private final BooleanSetting smoothZoomSetting;
    private final NumberSetting animationSpeedSetting;
    private final EnumSetting<ZoomEasing> easingSetting;
    private final NumberSetting transitionDurationSetting;
    private final BooleanSetting instantZoomSetting;

    // Behavior
    private final BooleanSetting resetOnFocusLostSetting;
    private final BooleanSetting resetOnWorldChangeSetting;
    private final BooleanSetting disableInGuiSetting;
    private final BooleanSetting restorePreviousFovSetting;
    private final BooleanSetting disableInThirdPersonSetting;

    // HUD
    private final BooleanSetting showZoomIndicatorSetting;
    private final BooleanSetting showZoomLevelSetting;
    private final EnumSetting<ZoomIndicatorStyle> indicatorStyleSetting;
    private final NumberSetting indicatorScaleSetting;
    private final ColorSetting indicatorColorSetting;
    private final NumberSetting backgroundOpacitySetting;

    public ZoomModule() {
        super(
                ID,
                "Zoom",
                "Umożliwia płynne przybliżenie widoku (Lunar Client style) z obsługą rolki myszy i wskaźnika HUD.",
                ModuleCategory.VISUAL,
                GLFW.GLFW_KEY_Z
        );
        instance = this;

        // 1. General
        SettingGroup generalGroup = new SettingGroup("general", "General");
        this.activationModeSetting = new EnumSetting<>(
                "activation_mode", "Activation Mode", "Tryb aktywacji: Hold (przytrzymanie), Toggle (przełącznik) lub Toggle+Hold",
                ZoomActivationMode.HOLD
        );
        this.defaultZoomSetting = new NumberSetting(
                "default_zoom", "Default Zoom", "Domyślny współczynnik przybliżenia (np. 4.0x)", 4.0, 1.2, 15.0, 0.1
        );
        this.minZoomSetting = new NumberSetting(
                "min_zoom", "Minimum Zoom", "Minimalny dopuszczalny współczynnik przybliżenia", 1.2, 1.1, 5.0, 0.1
        );
        this.maxZoomSetting = new NumberSetting(
                "max_zoom", "Maximum Zoom", "Maksymalny współczynnik powiększenia", 15.0, 5.0, 30.0, 0.5
        );
        this.zoomMultiplierSetting = new NumberSetting(
                "zoom_multiplier", "Zoom Multiplier", "Mnożnik skoku zoomu", 1.0, 0.5, 3.0, 0.1
        );
        generalGroup.addSetting(activationModeSetting);
        generalGroup.addSetting(defaultZoomSetting);
        generalGroup.addSetting(minZoomSetting);
        generalGroup.addSetting(maxZoomSetting);
        generalGroup.addSetting(zoomMultiplierSetting);
        addSettingGroup(generalGroup);

        // 2. Controls
        SettingGroup controlsGroup = new SettingGroup("controls", "Controls");
        this.zoomKeySetting = new KeybindSetting(
                "zoom_key", "Zoom Key", "Domyślny klawisz przybliżenia", GLFW.GLFW_KEY_Z
        );
        this.scrollZoomSetting = new BooleanSetting(
                "scroll_zoom", "Enable Scroll Zoom", "Regulacja powiększenia rolką myszy podczas aktywnego zoomu", true
        );
        this.scrollSensitivitySetting = new NumberSetting(
                "scroll_sensitivity", "Scroll Sensitivity", "Czułość zmiany zoomu rolką", 1.0, 0.1, 3.0, 0.1
        );
        this.scrollStepSetting = new NumberSetting(
                "scroll_step", "Scroll Step", "Krok powiększenia na jeden impuls rolki", 0.5, 0.1, 2.0, 0.1
        );
        this.resetZoomKeySetting = new KeybindSetting(
                "reset_zoom_key", "Reset Zoom Key", "Klawisz resetujący poziom powiększenia do domyślnego", GLFW.GLFW_KEY_UNKNOWN
        );
        controlsGroup.addSetting(zoomKeySetting);
        controlsGroup.addSetting(scrollZoomSetting);
        controlsGroup.addSetting(scrollSensitivitySetting);
        controlsGroup.addSetting(scrollStepSetting);
        controlsGroup.addSetting(resetZoomKeySetting);
        addSettingGroup(controlsGroup);

        // 3. Animation
        SettingGroup animGroup = new SettingGroup("animation", "Animation");
        this.smoothZoomSetting = new BooleanSetting(
                "smooth_zoom", "Smooth Zoom", "Płynne animowane przybliżanie i oddalanie", true
        );
        this.animationSpeedSetting = new NumberSetting(
                "animation_speed", "Animation Speed", "Szybkość interpolacji zoomu", 12.0, 1.0, 30.0, 1.0
        );
        this.easingSetting = new EnumSetting<>(
                "easing", "Zoom Easing", "Krzywa animacji płynnego przejścia", ZoomEasing.EASE_OUT
        );
        this.transitionDurationSetting = new NumberSetting(
                "transition_duration", "Transition Duration (ms)", "Czas trwania animacji w milisekundach", 150.0, 50.0, 500.0, 10.0
        );
        this.instantZoomSetting = new BooleanSetting(
                "instant_zoom", "Instant Zoom", "Natychmiastowe przybliżenie bez animacji", false
        );
        animGroup.addSetting(smoothZoomSetting);
        animGroup.addSetting(animationSpeedSetting);
        animGroup.addSetting(easingSetting);
        animGroup.addSetting(transitionDurationSetting);
        animGroup.addSetting(instantZoomSetting);
        addSettingGroup(animGroup);

        // 4. Behavior
        SettingGroup behaviorGroup = new SettingGroup("behavior", "Behavior");
        this.resetOnFocusLostSetting = new BooleanSetting(
                "reset_on_focus_lost", "Reset on Focus Lost", "Wyłącza przybliżenie po utracie fokusu okna gry", true
        );
        this.resetOnWorldChangeSetting = new BooleanSetting(
                "reset_on_world_change", "Reset on World Change", "Resetuje stan zoomu przy zmianie świata lub serwera", true
        );
        this.disableInGuiSetting = new BooleanSetting(
                "disable_in_gui", "Disable in GUI", "Zawiesza przybliżenie podczas otwarcia menu lub ekwipunku", true
        );
        this.restorePreviousFovSetting = new BooleanSetting(
                "restore_previous_fov", "Restore Previous FOV", "Przywraca pole widzenia sprzed aktywacji", true
        );
        this.disableInThirdPersonSetting = new BooleanSetting(
                "disable_in_third_person", "Disable in Third Person", "Blokuje działanie zoomu w perspektywie trzeciej osoby", false
        );
        behaviorGroup.addSetting(resetOnFocusLostSetting);
        behaviorGroup.addSetting(resetOnWorldChangeSetting);
        behaviorGroup.addSetting(disableInGuiSetting);
        behaviorGroup.addSetting(restorePreviousFovSetting);
        behaviorGroup.addSetting(disableInThirdPersonSetting);
        addSettingGroup(behaviorGroup);

        // 5. HUD
        SettingGroup hudGroup = new SettingGroup("hud", "HUD Overlay");
        this.showZoomIndicatorSetting = new BooleanSetting(
                "show_zoom_indicator", "Show Zoom Indicator", "Wyświetla graficzny wskaźnik aktywnego zoomu", true
        );
        this.showZoomLevelSetting = new BooleanSetting(
                "show_zoom_level", "Show Zoom Level", "Pokazuje wartość powiększenia (np. 4.0x)", true
        );
        this.indicatorStyleSetting = new EnumSetting<>(
                "indicator_style", "Indicator Style", "Styl wizualny wskaźnika zoomu", ZoomIndicatorStyle.BADGE
        );
        this.indicatorScaleSetting = new NumberSetting(
                "indicator_scale", "Indicator Scale", "Skala wskaźnika ekranowego", 1.0, 0.5, 2.0, 0.1
        );
        this.indicatorColorSetting = new ColorSetting(
                "indicator_color", "Indicator Color", "Kolor wskaźnika zoomu", 0xFF00E5FF
        );
        this.backgroundOpacitySetting = new NumberSetting(
                "background_opacity", "Background Opacity", "Przezroczystość tła wskaźnika", 0.6, 0.0, 1.0, 0.05
        );
        hudGroup.addSetting(showZoomIndicatorSetting);
        hudGroup.addSetting(showZoomLevelSetting);
        hudGroup.addSetting(indicatorStyleSetting);
        hudGroup.addSetting(indicatorScaleSetting);
        hudGroup.addSetting(indicatorColorSetting);
        hudGroup.addSetting(backgroundOpacitySetting);
        addSettingGroup(hudGroup);
    }

    public static ZoomModule getInstance() {
        if (instance == null) {
            new ZoomModule();
        }
        return instance;
    }

    public EnumSetting<ZoomActivationMode> getActivationModeSetting() {
        return activationModeSetting;
    }

    public NumberSetting getDefaultZoomSetting() {
        return defaultZoomSetting;
    }

    public NumberSetting getDefaultZoomLevelSetting() {
        return defaultZoomSetting;
    }

    public NumberSetting getMinZoomSetting() {
        return minZoomSetting;
    }

    public NumberSetting getMinZoomLevelSetting() {
        return minZoomSetting;
    }

    public NumberSetting getMaxZoomSetting() {
        return maxZoomSetting;
    }

    public NumberSetting getMaxZoomLevelSetting() {
        return maxZoomSetting;
    }

    public NumberSetting getZoomMultiplierSetting() {
        return zoomMultiplierSetting;
    }

    public KeybindSetting getZoomKeySetting() {
        return zoomKeySetting;
    }

    public BooleanSetting getScrollZoomSetting() {
        return scrollZoomSetting;
    }

    public NumberSetting getScrollSensitivitySetting() {
        return scrollSensitivitySetting;
    }

    public NumberSetting getScrollStepSetting() {
        return scrollStepSetting;
    }

    public KeybindSetting getResetZoomKeySetting() {
        return resetZoomKeySetting;
    }

    public BooleanSetting getSmoothZoomSetting() {
        return smoothZoomSetting;
    }

    public NumberSetting getAnimationSpeedSetting() {
        return animationSpeedSetting;
    }

    public EnumSetting<ZoomEasing> getEasingSetting() {
        return easingSetting;
    }

    public NumberSetting getTransitionDurationSetting() {
        return transitionDurationSetting;
    }

    public BooleanSetting getInstantZoomSetting() {
        return instantZoomSetting;
    }

    public BooleanSetting getResetOnFocusLostSetting() {
        return resetOnFocusLostSetting;
    }

    public BooleanSetting getResetOnWorldChangeSetting() {
        return resetOnWorldChangeSetting;
    }

    public BooleanSetting getDisableInGuiSetting() {
        return disableInGuiSetting;
    }

    public BooleanSetting getRestorePreviousFovSetting() {
        return restorePreviousFovSetting;
    }

    public BooleanSetting getDisableInThirdPersonSetting() {
        return disableInThirdPersonSetting;
    }

    public BooleanSetting getShowZoomIndicatorSetting() {
        return showZoomIndicatorSetting;
    }

    public BooleanSetting getShowZoomLevelSetting() {
        return showZoomLevelSetting;
    }

    public EnumSetting<ZoomIndicatorStyle> getIndicatorStyleSetting() {
        return indicatorStyleSetting;
    }

    public NumberSetting getIndicatorScaleSetting() {
        return indicatorScaleSetting;
    }

    public ColorSetting getIndicatorColorSetting() {
        return indicatorColorSetting;
    }

    public NumberSetting getBackgroundOpacitySetting() {
        return backgroundOpacitySetting;
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        pl.zombek.client.core.camera.ZoomController.getInstance().setActive(false);
    }
}
