package pl.zombek.client.core.modules.visual;

import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.rendereffect.color.ColorGradingController;
import pl.zombek.client.core.rendereffect.color.ColorProfile;
import pl.zombek.client.core.rendereffect.color.ColorProfileManager;
import pl.zombek.client.core.rendereffect.color.ColorSettings;
import pl.zombek.client.core.settings.BooleanSetting;
import pl.zombek.client.core.settings.EnumSetting;
import pl.zombek.client.core.settings.NumberSetting;
import pl.zombek.client.core.settings.SettingGroup;

/**
 * Color Saturation Module offering comprehensive visual grading, vibrance, contrast,
 * temperature, and selective layer application while preserving HUD readability.
 */
public class ColorSaturationModule extends AbstractModule {
    public static final String ID = "color_saturation";
    private static ColorSaturationModule instance;

    // General
    private final EnumSetting<ColorProfile> presetSetting;
    private final NumberSetting saturationSetting;

    // Color Adjustment
    private final NumberSetting vibranceSetting;
    private final NumberSetting contrastSetting;
    private final NumberSetting brightnessSetting;
    private final NumberSetting hueShiftSetting;
    private final NumberSetting temperatureSetting;

    // Channels
    private final NumberSetting redMultiplierSetting;
    private final NumberSetting greenMultiplierSetting;
    private final NumberSetting blueMultiplierSetting;

    // Selective Targets
    private final BooleanSetting affectWorldSetting;
    private final BooleanSetting affectEntitiesSetting;
    private final BooleanSetting affectItemsSetting;
    private final BooleanSetting affectParticlesSetting;
    private final BooleanSetting preserveHudSetting;

    // Animation
    private final BooleanSetting smoothTransitionSetting;
    private final NumberSetting transitionDurationMsSetting;

    // Behavior
    private final BooleanSetting resetOnWorldChangeSetting;

    public ColorSaturationModule() {
        super(
                ID,
                "Color Saturation",
                "Personalizacja nasycenia, kontrastu, temperatury barwowej i kanałów RGB świata gry.",
                ModuleCategory.VISUAL,
                GLFW.GLFW_KEY_UNKNOWN
        );
        instance = this;

        // 1. General
        SettingGroup generalGroup = new SettingGroup("general", "General");
        this.presetSetting = new EnumSetting<>(
                "preset", "Color Preset", "Wybór profilu kolorystycznego (Vanilla, Vibrant, Natural, Soft, Cinematic, Monochrome, Custom)",
                ColorProfile.VANILLA
        );
        this.saturationSetting = new NumberSetting(
                "saturation", "Master Saturation", "Główne nasycenie barw (0% - 200%)", 1.0, 0.0, 2.0, 0.05, 2, ""
        );
        generalGroup.addSetting(presetSetting);
        generalGroup.addSetting(saturationSetting);
        addSettingGroup(generalGroup);

        // 2. Color Adjustment
        SettingGroup adjustmentGroup = new SettingGroup("adjustment", "Color Adjustment");
        this.vibranceSetting = new NumberSetting(
                "vibrance", "Vibrance", "Inteligentne nasycenie wyciągające mniej nasycone kolory", 0.0, -1.0, 1.0, 0.05, 2, ""
        );
        this.contrastSetting = new NumberSetting(
                "contrast", "Contrast", "Kontrast tonalny", 1.0, 0.5, 1.5, 0.05, 2, ""
        );
        this.brightnessSetting = new NumberSetting(
                "brightness", "Brightness", "Jasność barw", 1.0, 0.5, 1.5, 0.05, 2, ""
        );
        this.hueShiftSetting = new NumberSetting(
                "hue_shift", "Hue Shift", "Przesunięcie spektrum barw w stopniach", 0.0, -180.0, 180.0, 5.0, 0, "°"
        );
        this.temperatureSetting = new NumberSetting(
                "temperature", "Color Temperature", "Temperatura barwowa (ujemne: chłodna, dodatnie: ciepła)", 0.0, -1.0, 1.0, 0.05, 2, ""
        );
        adjustmentGroup.addSetting(vibranceSetting);
        adjustmentGroup.addSetting(contrastSetting);
        adjustmentGroup.addSetting(brightnessSetting);
        adjustmentGroup.addSetting(hueShiftSetting);
        adjustmentGroup.addSetting(temperatureSetting);
        addSettingGroup(adjustmentGroup);

        // 3. Channels
        SettingGroup channelsGroup = new SettingGroup("channels", "RGB Channels");
        this.redMultiplierSetting = new NumberSetting(
                "red_multiplier", "Red Channel", "Mnożnik kanału czerwonego", 1.0, 0.0, 2.0, 0.05, 2, ""
        );
        this.greenMultiplierSetting = new NumberSetting(
                "green_multiplier", "Green Channel", "Mnożnik kanału zielonego", 1.0, 0.0, 2.0, 0.05, 2, ""
        );
        this.blueMultiplierSetting = new NumberSetting(
                "blue_multiplier", "Blue Channel", "Mnożnik kanału niebieskiego", 1.0, 0.0, 2.0, 0.05, 2, ""
        );
        channelsGroup.addSetting(redMultiplierSetting);
        channelsGroup.addSetting(greenMultiplierSetting);
        channelsGroup.addSetting(blueMultiplierSetting);
        addSettingGroup(channelsGroup);

        // 4. Selective Targets
        SettingGroup targetsGroup = new SettingGroup("targets", "Targets");
        this.affectWorldSetting = new BooleanSetting(
                "affect_world", "Affect World", "Aplikuj gradację barw do terenu i bloków", true
        );
        this.affectEntitiesSetting = new BooleanSetting(
                "affect_entities", "Affect Entities", "Aplikuj gradację do postaci i mobów", true
        );
        this.affectItemsSetting = new BooleanSetting(
                "affect_items", "Affect Items", "Aplikuj gradację do modeli przedmiotów", true
        );
        this.affectParticlesSetting = new BooleanSetting(
                "affect_particles", "Affect Particles", "Aplikuj gradację do cząsteczek", true
        );
        this.preserveHudSetting = new BooleanSetting(
                "preserve_hud", "Preserve HUD Colors", "Utrzymuj naturalne kolory interfejsu HUD bez zniekształceń", true
        );
        targetsGroup.addSetting(affectWorldSetting);
        targetsGroup.addSetting(affectEntitiesSetting);
        targetsGroup.addSetting(affectItemsSetting);
        targetsGroup.addSetting(affectParticlesSetting);
        targetsGroup.addSetting(preserveHudSetting);
        addSettingGroup(targetsGroup);

        // 5. Animation
        SettingGroup animGroup = new SettingGroup("animation", "Animation");
        this.smoothTransitionSetting = new BooleanSetting(
                "smooth_transition", "Smooth Transition", "Płynne przejścia między profilami i suwakami", true
        );
        this.transitionDurationMsSetting = new NumberSetting(
                "transition_duration_ms", "Transition Duration", "Czas trwania płynnego przejścia w milisekundach", 300.0, 50.0, 2000.0, 50.0, 0, "ms"
        );
        animGroup.addSetting(smoothTransitionSetting);
        animGroup.addSetting(transitionDurationMsSetting);
        addSettingGroup(animGroup);

        // 6. Behavior
        SettingGroup behaviorGroup = new SettingGroup("behavior", "Behavior");
        this.resetOnWorldChangeSetting = new BooleanSetting(
                "reset_on_world_change", "Reset on World Change", "Przywróć profil Vanilla przy zmianie świata lub rozłączeniu", false
        );
        behaviorGroup.addSetting(resetOnWorldChangeSetting);
        addSettingGroup(behaviorGroup);

        // Preset change listener
        this.presetSetting.addChangeListener(newVal -> {
            if (newVal != ColorProfile.CUSTOM) {
                applyPresetValues(newVal);
            }
            syncToController();
        });

        // Other listeners
        this.saturationSetting.addChangeListener(n -> syncToController());
        this.vibranceSetting.addChangeListener(n -> syncToController());
        this.contrastSetting.addChangeListener(n -> syncToController());
        this.brightnessSetting.addChangeListener(n -> syncToController());
        this.hueShiftSetting.addChangeListener(n -> syncToController());
        this.temperatureSetting.addChangeListener(n -> syncToController());
        this.redMultiplierSetting.addChangeListener(n -> syncToController());
        this.greenMultiplierSetting.addChangeListener(n -> syncToController());
        this.blueMultiplierSetting.addChangeListener(n -> syncToController());
        this.affectWorldSetting.addChangeListener(n -> syncToController());
        this.affectEntitiesSetting.addChangeListener(n -> syncToController());
        this.affectItemsSetting.addChangeListener(n -> syncToController());
        this.affectParticlesSetting.addChangeListener(n -> syncToController());
        this.preserveHudSetting.addChangeListener(n -> syncToController());
    }

    public static ColorSaturationModule getInstance() {
        return instance;
    }

    private void applyPresetValues(ColorProfile profile) {
        ColorSettings p = ColorProfileManager.getSettings(profile);
        this.saturationSetting.setValue((double) p.getSaturation());
        this.vibranceSetting.setValue((double) p.getVibrance());
        this.contrastSetting.setValue((double) p.getContrast());
        this.brightnessSetting.setValue((double) p.getBrightness());
        this.hueShiftSetting.setValue((double) p.getHueShift());
        this.temperatureSetting.setValue((double) p.getTemperature());
        this.redMultiplierSetting.setValue((double) p.getRedMultiplier());
        this.greenMultiplierSetting.setValue((double) p.getGreenMultiplier());
        this.blueMultiplierSetting.setValue((double) p.getBlueMultiplier());
    }

    public void syncToController() {
        if (!isEnabled()) {
            ColorGradingController.getInstance().setActive(false);
            return;
        }

        ColorSettings settings = new ColorSettings(
                saturationSetting.getValue().floatValue(),
                vibranceSetting.getValue().floatValue(),
                contrastSetting.getValue().floatValue(),
                brightnessSetting.getValue().floatValue(),
                hueShiftSetting.getValue().floatValue(),
                temperatureSetting.getValue().floatValue(),
                redMultiplierSetting.getValue().floatValue(),
                greenMultiplierSetting.getValue().floatValue(),
                blueMultiplierSetting.getValue().floatValue(),
                affectWorldSetting.getValue(),
                affectEntitiesSetting.getValue(),
                affectItemsSetting.getValue(),
                affectParticlesSetting.getValue(),
                preserveHudSetting.getValue()
        );

        ColorGradingController.getInstance().setActive(true);
        ColorGradingController.getInstance().applySettings(
                settings,
                smoothTransitionSetting.getValue(),
                transitionDurationMsSetting.getValue().longValue()
        );
    }

    @Override
    public void onEnable() {
        super.onEnable();
        syncToController();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        ColorGradingController.getInstance().setActive(false);
    }

    public void onWorldChange() {
        if (resetOnWorldChangeSetting.getValue()) {
            presetSetting.setValue(ColorProfile.VANILLA);
            applyPresetValues(ColorProfile.VANILLA);
            syncToController();
        }
    }

    // Getters for settings
    public EnumSetting<ColorProfile> getPresetSetting() { return presetSetting; }
    public NumberSetting getSaturationSetting() { return saturationSetting; }
    public NumberSetting getVibranceSetting() { return vibranceSetting; }
    public NumberSetting getContrastSetting() { return contrastSetting; }
    public NumberSetting getBrightnessSetting() { return brightnessSetting; }
    public NumberSetting getHueShiftSetting() { return hueShiftSetting; }
    public NumberSetting getTemperatureSetting() { return temperatureSetting; }
    public NumberSetting getRedMultiplierSetting() { return redMultiplierSetting; }
    public NumberSetting getGreenMultiplierSetting() { return greenMultiplierSetting; }
    public NumberSetting getBlueMultiplierSetting() { return blueMultiplierSetting; }
    public BooleanSetting getAffectWorldSetting() { return affectWorldSetting; }
    public BooleanSetting getAffectEntitiesSetting() { return affectEntitiesSetting; }
    public BooleanSetting getAffectItemsSetting() { return affectItemsSetting; }
    public BooleanSetting getAffectParticlesSetting() { return affectParticlesSetting; }
    public BooleanSetting getPreserveHudSetting() { return preserveHudSetting; }
    public BooleanSetting getSmoothTransitionSetting() { return smoothTransitionSetting; }
    public NumberSetting getTransitionDurationMsSetting() { return transitionDurationMsSetting; }
    public BooleanSetting getResetOnWorldChangeSetting() { return resetOnWorldChangeSetting; }
}
