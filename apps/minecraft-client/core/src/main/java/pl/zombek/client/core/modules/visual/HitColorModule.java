package pl.zombek.client.core.modules.visual;

import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;
import pl.zombek.client.core.tracking.HitColorTracker;

/**
 * Visual module modifying entity damage tinting when receiving hits.
 * Purely client-side visual effect; does not modify health, damage, packets or combat mechanics.
 */
public class HitColorModule extends AbstractModule {
    private static HitColorModule instance;

    // General
    private final EnumSetting<HitColorMode> modeSetting;
    private final BooleanSetting onlyTargetedEntity;
    private final BooleanSetting onlyLocalPlayerDamage;
    private final BooleanSetting playersSetting;
    private final BooleanSetting hostileMobsSetting;
    private final BooleanSetting passiveMobsSetting;
    private final BooleanSetting otherEntitiesSetting;

    // Colors
    private final ColorSetting hitColorSetting;
    private final ColorSetting outlineColorSetting;
    private final ColorSetting glowColorSetting;
    private final NumberSetting opacitySetting;
    private final NumberSetting intensitySetting;

    // Timing
    private final NumberSetting durationSetting;
    private final NumberSetting fadeDurationSetting;
    private final NumberSetting animationSpeedSetting;
    private final EnumSetting<FadeCurve> fadeCurveSetting;

    // Appearance
    private final BooleanSetting solidColorEnabled;
    private final BooleanSetting overlayEnabled;
    private final BooleanSetting outlineEnabled;
    private final BooleanSetting glowEnabled;
    private final BooleanSetting customEnabled;

    // Advanced
    private final NumberSetting renderPrioritySetting;
    private final NumberSetting maxActiveEffectsSetting;
    private final BooleanSetting performanceModeSetting;

    public HitColorModule() {
        super("hit-color", "Hit Color", "Wizualne podświetlenie i zmiana koloru encji podczas otrzymywania obrażeń", ModuleCategory.VISUAL);
        instance = this;

        // --- General ---
        SettingGroup generalGroup = new SettingGroup("general", "General");
        this.modeSetting = generalGroup.addSetting(new EnumSetting<>(
                "mode",
                "Tryb efektu",
                "Wybierz styl wizualny podświetlenia obrażeń",
                HitColorMode.COLOR_OVERLAY
        ));
        this.onlyTargetedEntity = generalGroup.addSetting(new BooleanSetting(
                "only_targeted",
                "Tylko aktualny cel",
                "Podświetlaj wyłącznie encję, w którą bezpośrednio celujesz",
                false
        ));
        this.onlyLocalPlayerDamage = generalGroup.addSetting(new BooleanSetting(
                "only_local_damage",
                "Tylko ciosy gracza",
                "Podświetlaj wyłącznie wtedy, gdy obrażenia zadał lokalny gracz",
                false
        ));
        this.playersSetting = generalGroup.addSetting(new BooleanSetting("players", "Gracze", "Podświetlaj innych graczy", true));
        this.hostileMobsSetting = generalGroup.addSetting(new BooleanSetting("hostile_mobs", "Wrogie moby", "Podświetlaj potwory i agresywne stworzenia", true));
        this.passiveMobsSetting = generalGroup.addSetting(new BooleanSetting("passive_mobs", "Pasywne moby", "Podświetlaj zwierzęta i pasywne stworzenia", true));
        this.otherEntitiesSetting = generalGroup.addSetting(new BooleanSetting("other_entities", "Pozostałe encje", "Podświetlaj pozostałe żywe istoty", true));
        registerGroup(generalGroup);

        // --- Colors ---
        SettingGroup colorsGroup = new SettingGroup("colors", "Kolory");
        this.hitColorSetting = colorsGroup.addSetting(new ColorSetting("hit_color", "Kolor uderzenia", "Główny kolor podświetlenia przy trafieniu", 0xFFFF0055));
        this.outlineColorSetting = colorsGroup.addSetting(new ColorSetting("outline_color", "Kolor obrysu", "Kolor świetlistego obrysu encji", 0xFFFF2200));
        this.glowColorSetting = colorsGroup.addSetting(new ColorSetting("glow_color", "Kolor poświaty", "Kolor poświaty modelu", 0xFFFF5588));
        this.opacitySetting = colorsGroup.addSetting(new NumberSetting("opacity", "Nieprzezroczystość", "Globalna przezroczystość nałożonego koloru", 70.0, 10.0, 100.0, 1.0, "%"));
        this.intensitySetting = colorsGroup.addSetting(new NumberSetting("intensity", "Intensywność", "Moc i nasycenie początkowego błysku", 100.0, 10.0, 100.0, 1.0, "%"));
        registerGroup(colorsGroup);

        // --- Timing ---
        SettingGroup timingGroup = new SettingGroup("timing", "Czas trwania i animacja");
        this.durationSetting = timingGroup.addSetting(new NumberSetting("duration", "Czas trwania", "Czas pełnej widoczności koloru przed wygaszaniem", 180.0, 50.0, 1000.0, 10.0, "ms"));
        this.fadeDurationSetting = timingGroup.addSetting(new NumberSetting("fade_duration", "Czas wygaszania", "Czas płynnego zanikania koloru do normy", 150.0, 0.0, 1000.0, 10.0, "ms"));
        this.animationSpeedSetting = timingGroup.addSetting(new NumberSetting("anim_speed", "Prędkość animacji", "Mnożnik tempa wygaszania", 1.0, 0.2, 3.0, 0.1, "x"));
        this.fadeCurveSetting = timingGroup.addSetting(new EnumSetting<>(
                "fade_curve",
                "Krzywa wygaszania",
                "Matematyczna funkcja spadku intensywności w czasie",
                FadeCurve.EASE_OUT
        ));
        registerGroup(timingGroup);

        // --- Appearance ---
        SettingGroup appearanceGroup = new SettingGroup("appearance", "Dostępne warianty");
        this.solidColorEnabled = appearanceGroup.addSetting(new BooleanSetting("solid_color", "Solid Color", "Włączony tryb litego koloru", true));
        this.overlayEnabled = appearanceGroup.addSetting(new BooleanSetting("overlay", "Color Overlay", "Włączony tryb nakładki", true));
        this.outlineEnabled = appearanceGroup.addSetting(new BooleanSetting("outline", "Outline Glow", "Włączony tryb obrysu", true));
        this.glowEnabled = appearanceGroup.addSetting(new BooleanSetting("glow", "Soft Tint", "Włączony tryb delikatnego tintu", true));
        this.customEnabled = appearanceGroup.addSetting(new BooleanSetting("custom", "Custom Color", "Włączony tryb własnego koloru", true));
        registerGroup(appearanceGroup);

        // --- Advanced ---
        SettingGroup advancedGroup = new SettingGroup("advanced", "Zaawansowane");
        this.renderPrioritySetting = advancedGroup.addSetting(new NumberSetting("render_priority", "Priorytet renderowania", "Priorytet nakładki względem innych warstw", 1.0, 1.0, 5.0, 1.0));
        this.maxActiveEffectsSetting = advancedGroup.addSetting(new NumberSetting("max_effects", "Limit aktywnych efektów", "Maksymalna liczba jednocześnie śledzonych encji", 32.0, 1.0, 128.0, 1.0));
        this.performanceModeSetting = advancedGroup.addSetting(new BooleanSetting("performance_mode", "Tryb wydajnościowy", "Uproszczona kalkulacja dla zachowania wysokich FPS", false));
        registerGroup(advancedGroup);
    }

    public static HitColorModule getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        // Ready for tracking
    }

    @Override
    public void onDisable() {
        HitColorTracker.getInstance().clear();
    }

    // Getters
    public EnumSetting<HitColorMode> getModeSetting() {
        return modeSetting;
    }

    public BooleanSetting getOnlyTargetedEntitySetting() {
        return onlyTargetedEntity;
    }

    public BooleanSetting getOnlyLocalPlayerDamageSetting() {
        return onlyLocalPlayerDamage;
    }

    public BooleanSetting getPlayersSetting() {
        return playersSetting;
    }

    public BooleanSetting getHostileMobsSetting() {
        return hostileMobsSetting;
    }

    public BooleanSetting getPassiveMobsSetting() {
        return passiveMobsSetting;
    }

    public BooleanSetting getOtherEntitiesSetting() {
        return otherEntitiesSetting;
    }

    public ColorSetting getHitColorSetting() {
        return hitColorSetting;
    }

    public NumberSetting getOpacitySetting() {
        return opacitySetting;
    }

    public NumberSetting getIntensitySetting() {
        return intensitySetting;
    }

    public NumberSetting getDurationSetting() {
        return durationSetting;
    }

    public NumberSetting getFadeDurationSetting() {
        return fadeDurationSetting;
    }

    public EnumSetting<FadeCurve> getFadeCurveSetting() {
        return fadeCurveSetting;
    }

    public NumberSetting getMaxActiveEffectsSetting() {
        return maxActiveEffectsSetting;
    }
}
