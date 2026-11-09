package pl.zombek.client.core.modules.visual;

import pl.zombek.client.core.data.HitboxEntityData;
import pl.zombek.client.core.data.TargetEntityType;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

/**
 * World-space hitbox visualization module displaying exact bounding boxes of Minecraft entities.
 * Strictly visual & informational debug tool; does not alter physics, reach, packets, or collisions.
 */
public class HitboxModule extends AbstractModule {
    private static HitboxModule instance;

    // General
    private final BooleanSetting outlineEnabled;
    private final BooleanSetting fillEnabled;
    private final BooleanSetting showTargetedEntity;
    private final BooleanSetting showPlayers;
    private final BooleanSetting showHostileMobs;
    private final BooleanSetting showPassiveMobs;
    private final BooleanSetting showProjectiles;
    private final BooleanSetting showVehicles;

    // Appearance
    private final NumberSetting lineWidthSetting;
    private final NumberSetting fillOpacitySetting;
    private final NumberSetting outlineOpacitySetting;
    private final EnumSetting<HitboxRenderStyle> renderStyleSetting;
    private final BooleanSetting smoothEdgesSetting;

    // Colors
    private final ColorSetting playersColorSetting;
    private final ColorSetting hostileColorSetting;
    private final ColorSetting passiveColorSetting;
    private final ColorSetting projectileColorSetting;
    private final ColorSetting vehicleColorSetting;
    private final ColorSetting targetedEntityColorSetting;
    private final ColorSetting fillColorSetting;

    // Filters
    private final NumberSetting maxDistanceSetting;
    private final BooleanSetting onlyTargetedSetting;
    private final BooleanSetting ignoreDeadEntitiesSetting;
    private final BooleanSetting ignoreSpectatorsSetting;
    private final BooleanSetting ignoreInvisibleEntitiesSetting;

    // Information
    private final BooleanSetting showNameSetting;
    private final BooleanSetting showDistanceSetting;
    private final BooleanSetting showEntityIdSetting;
    private final BooleanSetting showEyePositionSetting;
    private final BooleanSetting showLookDirectionSetting;

    // Advanced
    private final BooleanSetting performanceModeSetting;
    private final NumberSetting maxRenderedEntitiesSetting;
    private final BooleanSetting interpolationSetting;

    public HitboxModule() {
        super("hitbox", "Hitbox", "Wizualizacja rzeczywistych obszarów kolizji i bounding boxów encji", ModuleCategory.VISUAL);
        instance = this;

        // --- General ---
        SettingGroup generalGroup = new SettingGroup("general", "General");
        this.outlineEnabled = generalGroup.addSetting(new BooleanSetting("outline", "Obrys obwiedni", "Rysuj linie krawędzi hitboxa", true));
        this.fillEnabled = generalGroup.addSetting(new BooleanSetting("fill", "Wypełnienie ścian", "Wypełniaj ściany półprzezroczystym kolorem", false));
        this.showTargetedEntity = generalGroup.addSetting(new BooleanSetting("show_targeted", "Wyróżnij cel", "Zastosuj specjalny kolor dla aktualnego celu", true));
        this.showPlayers = generalGroup.addSetting(new BooleanSetting("show_players", "Gracze", "Wyświetlaj hitboxy innych graczy", true));
        this.showHostileMobs = generalGroup.addSetting(new BooleanSetting("show_hostile", "Wrogie moby", "Wyświetlaj hitboxy potworów i agresorów", true));
        this.showPassiveMobs = generalGroup.addSetting(new BooleanSetting("show_passive", "Pasywne moby", "Wyświetlaj hitboxy zwierząt i istot neutralnych", true));
        this.showProjectiles = generalGroup.addSetting(new BooleanSetting("show_projectiles", "Pociski", "Wyświetlaj hitboxy strzał, pereł i mikstur", true));
        this.showVehicles = generalGroup.addSetting(new BooleanSetting("show_vehicles", "Pojazdy", "Wyświetlaj hitboxy łódek i wagoników", true));
        registerGroup(generalGroup);

        // --- Appearance ---
        SettingGroup appearanceGroup = new SettingGroup("appearance", "Wygląd");
        this.lineWidthSetting = appearanceGroup.addSetting(new NumberSetting("line_width", "Grubość linii", "Grubość krawędzi obrysu", 1.5, 0.5, 5.0, 0.5, "px"));
        this.fillOpacitySetting = appearanceGroup.addSetting(new NumberSetting("fill_opacity", "Przezroczystość wypełnienia", "Krycie wypełnienia ścian", 30.0, 5.0, 100.0, 5.0, "%"));
        this.outlineOpacitySetting = appearanceGroup.addSetting(new NumberSetting("outline_opacity", "Przezroczystość obrysu", "Krycie krawędzi hitboxa", 100.0, 10.0, 100.0, 5.0, "%"));
        this.renderStyleSetting = appearanceGroup.addSetting(new EnumSetting<>(
                "render_style",
                "Styl renderowania",
                "Tryb geometrii hitboxa",
                HitboxRenderStyle.OUTLINE
        ));
        this.smoothEdgesSetting = appearanceGroup.addSetting(new BooleanSetting("smooth_edges", "Wygładzanie krawędzi", "Wygładzanie antyaliasingowe linii", true));
        registerGroup(appearanceGroup);

        // --- Colors ---
        SettingGroup colorsGroup = new SettingGroup("colors", "Kolory typów encji");
        this.playersColorSetting = colorsGroup.addSetting(new ColorSetting("players_color", "Gracze", "Kolor obwiedni graczy", 0xFF00FF00));
        this.hostileColorSetting = colorsGroup.addSetting(new ColorSetting("hostile_color", "Wrogie moby", "Kolor obwiedni wrogów", 0xFFFF0000));
        this.passiveColorSetting = colorsGroup.addSetting(new ColorSetting("passive_color", "Pasywne moby", "Kolor obwiedni stworzeń pasywnych", 0xFF00AAFF));
        this.projectileColorSetting = colorsGroup.addSetting(new ColorSetting("projectile_color", "Pociski", "Kolor obwiedni pocisków", 0xFFFFFF00));
        this.vehicleColorSetting = colorsGroup.addSetting(new ColorSetting("vehicle_color", "Pojazdy", "Kolor obwiedni łódek i wagoników", 0xFFFF8800));
        this.targetedEntityColorSetting = colorsGroup.addSetting(new ColorSetting("targeted_color", "Aktualny cel", "Wyróżniający kolor celu", 0xFFFF00FF));
        this.fillColorSetting = colorsGroup.addSetting(new ColorSetting("fill_color", "Kolor wypełnienia", "Kolor ścian prostopadłościanu", 0x40FFFFFF));
        registerGroup(colorsGroup);

        // --- Filters ---
        SettingGroup filtersGroup = new SettingGroup("filters", "Filtry i zasięg");
        this.maxDistanceSetting = filtersGroup.addSetting(new NumberSetting("max_distance", "Maksymalny zasięg", "Maksymalna odległość renderowania hitboxów", 32.0, 4.0, 96.0, 2.0, "m"));
        this.onlyTargetedSetting = filtersGroup.addSetting(new BooleanSetting("only_targeted", "Tylko celowany obiekt", "Ukryj wszystkie hitboxy poza aktualnym celem", false));
        this.ignoreDeadEntitiesSetting = filtersGroup.addSetting(new BooleanSetting("ignore_dead", "Ignoruj martwe encje", "Nie renderuj hitboxów bytów w fazie śmierci", true));
        this.ignoreSpectatorsSetting = filtersGroup.addSetting(new BooleanSetting("ignore_spectators", "Ignoruj obserwatorów", "Pomiń graczy w trybie Spectator", true));
        this.ignoreInvisibleEntitiesSetting = filtersGroup.addSetting(new BooleanSetting("ignore_invisible", "Ignoruj niewidzialne encje", "Respektuj niewidzialność klienta (brak wallhacka)", true));
        registerGroup(filtersGroup);

        // --- Information ---
        SettingGroup infoGroup = new SettingGroup("information", "Dodatkowe informacje");
        this.showNameSetting = infoGroup.addSetting(new BooleanSetting("show_name", "Nazwa encji", "Wyświetlaj etykietę nazwy nad hitboxem", false));
        this.showDistanceSetting = infoGroup.addSetting(new BooleanSetting("show_distance", "Odległość", "Wyświetlaj dystans w metrach", false));
        this.showEntityIdSetting = infoGroup.addSetting(new BooleanSetting("show_id", "ID encji", "Wyświetlaj numeryczny identyfikator encji", false));
        this.showEyePositionSetting = infoGroup.addSetting(new BooleanSetting("show_eye_pos", "Poziom oczu", "Rysuj płaszczyznę poziomu wzroku encji", true));
        this.showLookDirectionSetting = infoGroup.addSetting(new BooleanSetting("show_look_dir", "Kierunek patrzenia", "Rysuj wektor kierunku spojrzenia", true));
        registerGroup(infoGroup);

        // --- Advanced ---
        SettingGroup advancedGroup = new SettingGroup("advanced", "Wydajność");
        this.performanceModeSetting = advancedGroup.addSetting(new BooleanSetting("perf_mode", "Tryb wydajności", "Optymalizacja renderowania przy dużej liczbie mobów", false));
        this.maxRenderedEntitiesSetting = advancedGroup.addSetting(new NumberSetting("max_entities", "Limit encji", "Maksymalna liczba renderowanych hitboxów jednocześnie", 64.0, 8.0, 256.0, 8.0));
        this.interpolationSetting = advancedGroup.addSetting(new BooleanSetting("interpolation", "Płynna interpolacja", "Interpoluj płynnie pozycję encji pomiędzy tickami", true));
        registerGroup(advancedGroup);
    }

    public static HitboxModule getInstance() {
        return instance;
    }

    public boolean shouldRenderEntity(HitboxEntityData data) {
        if (!isEnabled() || hasError() || data == null) {
            return false;
        }

        // 1. Distance filter
        if (data.getDistanceToCamera() > maxDistanceSetting.getValue()) {
            return false;
        }

        // 2. Only targeted
        if (onlyTargetedSetting.getValue() && !data.isTargeted()) {
            return false;
        }

        // 3. State filters
        if (ignoreDeadEntitiesSetting.getValue() && data.isDead()) {
            return false;
        }
        if (ignoreSpectatorsSetting.getValue() && data.isSpectator()) {
            return false;
        }
        if (ignoreInvisibleEntitiesSetting.getValue() && data.isInvisible()) {
            return false;
        }

        // 4. Type filters
        return switch (data.getType()) {
            case PLAYER -> showPlayers.getValue();
            case HOSTILE -> showHostileMobs.getValue();
            case PASSIVE, NEUTRAL -> showPassiveMobs.getValue();
            case PROJECTILE -> showProjectiles.getValue();
            case VEHICLE -> showVehicles.getValue();
            case OTHER -> true;
        };
    }

    public int resolveOutlineColor(HitboxEntityData data) {
        if (data.isTargeted() && showTargetedEntity.getValue()) {
            return applyAlpha(targetedEntityColorSetting.getValue(), outlineOpacitySetting.getValue().floatValue());
        }

        int baseColor = switch (data.getType()) {
            case PLAYER -> playersColorSetting.getValue();
            case HOSTILE -> hostileColorSetting.getValue();
            case PASSIVE, NEUTRAL -> passiveColorSetting.getValue();
            case PROJECTILE -> projectileColorSetting.getValue();
            case VEHICLE -> vehicleColorSetting.getValue();
            case OTHER -> 0xFFCCCCCC;
        };

        return applyAlpha(baseColor, outlineOpacitySetting.getValue().floatValue());
    }

    public int resolveFillColor(HitboxEntityData data) {
        int baseColor = fillColorSetting.getValue();
        if (data.isTargeted() && showTargetedEntity.getValue()) {
            baseColor = targetedEntityColorSetting.getValue();
        }
        return applyAlpha(baseColor, fillOpacitySetting.getValue().floatValue());
    }

    private static int applyAlpha(int argb, float opacityPercent) {
        float factor = Math.min(1.0f, Math.max(0.0f, opacityPercent / 100.0f));
        int a = (argb >> 24) & 0xFF;
        int newA = Math.round(a * factor);
        return (newA << 24) | (argb & 0x00FFFFFF);
    }

    // Getters for settings
    public EnumSetting<HitboxRenderStyle> getRenderStyleSetting() {
        return renderStyleSetting;
    }

    public NumberSetting getLineWidthSetting() {
        return lineWidthSetting;
    }

    public BooleanSetting getShowEyePositionSetting() {
        return showEyePositionSetting;
    }

    public BooleanSetting getShowLookDirectionSetting() {
        return showLookDirectionSetting;
    }

    public BooleanSetting getShowNameSetting() {
        return showNameSetting;
    }

    public BooleanSetting getShowDistanceSetting() {
        return showDistanceSetting;
    }

    public BooleanSetting getShowEntityIdSetting() {
        return showEntityIdSetting;
    }

    public NumberSetting getMaxRenderedEntitiesSetting() {
        return maxRenderedEntitiesSetting;
    }

    public BooleanSetting getInterpolationSetting() {
        return interpolationSetting;
    }
}
