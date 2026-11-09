package pl.zombek.client.core.modules.hud.teamview;

import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

/**
 * Team View Module.
 * Visually distinguishes teammates via in-world overhead markers, team color, nameplate,
 * and distance without wallhacks, ESP, or accessing hidden entity data.
 */
public class TeamViewModule extends AbstractModule {
    private static TeamViewModule instance;

    // General
    private final SettingGroup groupGeneral;
    private final EnumSetting<TeamDetectionMode> detectionModeSetting;
    private final BooleanSetting showMarkersSetting;
    private final BooleanSetting showNamesSetting;
    private final BooleanSetting showDistanceSetting;
    private final BooleanSetting showTeamColorSetting;

    // Appearance
    private final SettingGroup groupAppearance;
    private final EnumSetting<MarkerStyle> markerStyleSetting;
    private final NumberSetting markerSizeSetting;
    private final ColorSetting markerColorSetting;
    private final ColorSetting nameColorSetting;
    private final ColorSetting distanceColorSetting;
    private final BooleanSetting backgroundSetting;
    private final NumberSetting opacitySetting;

    // Visibility
    private final SettingGroup groupVisibility;
    private final NumberSetting maxDistanceSetting;
    private final BooleanSetting onlyVisibleTeammatesSetting;
    private final BooleanSetting hideInGuiSetting;
    private final BooleanSetting hideWhenDeadSetting;
    private final BooleanSetting hideWhenSpectatorSetting;

    // Team
    private final SettingGroup groupTeam;
    private final ColorSetting defaultTeamColorSetting;
    private final BooleanSetting resetTeamSetting;

    // Integration
    private final SettingGroup groupIntegration;
    private final BooleanSetting enableServerIntegrationSetting;

    public TeamViewModule() {
        super(
                "team_view",
                "Team View",
                "Wizualne oznaczanie członków drużyny markerami, kolorem teamu, nazwą i dystansem w świecie gry.",
                ModuleCategory.HUD
        );
        instance = this;

        // General
        this.groupGeneral = new SettingGroup("general", "General & Detection");
        this.detectionModeSetting = new EnumSetting<>(
                "detection_mode", "Team Detection Mode",
                "Źródło wykrywania drużyny (Scoreboard, Lista ręczna lub Oba).", TeamDetectionMode.BOTH
        );
        this.showMarkersSetting = new BooleanSetting(
                "show_markers", "Show Team Markers",
                "Wyświetlaj marker nad głową członka drużyny.", true
        );
        this.showNamesSetting = new BooleanSetting(
                "show_names", "Show Player Names",
                "Wyświetlaj nazwę gracza nad markerem.", true
        );
        this.showDistanceSetting = new BooleanSetting(
                "show_distance", "Show Distance",
                "Pokazuj dystans w blokach (np. 24m).", true
        );
        this.showTeamColorSetting = new BooleanSetting(
                "show_team_color", "Show Team Color",
                "Koloruj marker i nazwę kolorem drużyny.", true
        );
        this.groupGeneral.addSetting(detectionModeSetting);
        this.groupGeneral.addSetting(showMarkersSetting);
        this.groupGeneral.addSetting(showNamesSetting);
        this.groupGeneral.addSetting(showDistanceSetting);
        this.groupGeneral.addSetting(showTeamColorSetting);
        registerGroup(this.groupGeneral);

        // Appearance
        this.groupAppearance = new SettingGroup("appearance", "Marker Appearance");
        this.markerStyleSetting = new EnumSetting<>(
                "marker_style", "Marker Style",
                "Kształt markera nad graczem.", MarkerStyle.ARROW_DOWN
        );
        this.markerSizeSetting = new NumberSetting(
                "marker_size", "Marker Size",
                "Rozmiar markera.", 1.0, 0.5, 3.0, 0.1
        );
        this.markerColorSetting = new ColorSetting(
                "marker_color", "Marker Color",
                "Domyślny kolor markera.", 0xFF55FF55
        );
        this.nameColorSetting = new ColorSetting(
                "name_color", "Name Color",
                "Domyślny kolor nicku gracza.", 0xFFFFFFFF
        );
        this.distanceColorSetting = new ColorSetting(
                "distance_color", "Distance Color",
                "Kolor etykiety dystansu.", 0xFFAAAAAA
        );
        this.backgroundSetting = new BooleanSetting(
                "background", "Background",
                "Renderuj półprzezroczyste tło pod etykietą.", true
        );
        this.opacitySetting = new NumberSetting(
                "opacity", "Opacity",
                "Przezroczystość tła (0-100%).", 70.0, 0.0, 100.0, 5.0
        );
        this.groupAppearance.addSetting(markerStyleSetting);
        this.groupAppearance.addSetting(markerSizeSetting);
        this.groupAppearance.addSetting(markerColorSetting);
        this.groupAppearance.addSetting(nameColorSetting);
        this.groupAppearance.addSetting(distanceColorSetting);
        this.groupAppearance.addSetting(backgroundSetting);
        this.groupAppearance.addSetting(opacitySetting);
        registerGroup(this.groupAppearance);

        // Visibility
        this.groupVisibility = new SettingGroup("visibility", "Visibility & Culling");
        this.maxDistanceSetting = new NumberSetting(
                "max_distance", "Maximum Render Distance",
                "Maksymalny zasięg widoczności markerów drużyny (bloki).", 64.0, 8.0, 256.0, 4.0
        );
        this.onlyVisibleTeammatesSetting = new BooleanSetting(
                "only_visible_teammates", "Show Only Visible Teammates",
                "Ukryj marker, jeśli sojusznik jest poza zasięgiem wzroku.", true
        );
        this.hideInGuiSetting = new BooleanSetting(
                "hide_in_gui", "Hide in GUI",
                "Ukryj markery przy otwartym ekranie ekwipunku/menu.", false
        );
        this.hideWhenDeadSetting = new BooleanSetting(
                "hide_when_dead", "Hide When Dead",
                "Ukryj markery po śmierci gracza.", true
        );
        this.hideWhenSpectatorSetting = new BooleanSetting(
                "hide_when_spectator", "Hide When Spectator",
                "Ukryj markery w trybie obserwatora.", true
        );
        this.groupVisibility.addSetting(maxDistanceSetting);
        this.groupVisibility.addSetting(onlyVisibleTeammatesSetting);
        this.groupVisibility.addSetting(hideInGuiSetting);
        this.groupVisibility.addSetting(hideWhenDeadSetting);
        this.groupVisibility.addSetting(hideWhenSpectatorSetting);
        registerGroup(this.groupVisibility);

        // Team
        this.groupTeam = new SettingGroup("team", "Team Management");
        this.defaultTeamColorSetting = new ColorSetting(
                "default_team_color", "Default Team Color",
                "Bazowy kolor drużyny dla graczy z manualnej listy.", 0xFF55FF55
        );
        this.resetTeamSetting = new BooleanSetting(
                "reset_manual_team", "Reset Manual Team",
                "Wyczyść listę graczy dodanych ręcznie do drużyny.", false
        );
        this.groupTeam.addSetting(defaultTeamColorSetting);
        this.groupTeam.addSetting(resetTeamSetting);
        registerGroup(this.groupTeam);

        // Integration
        this.groupIntegration = new SettingGroup("integration", "Server Integration");
        this.enableServerIntegrationSetting = new BooleanSetting(
                "enable_server_integration", "Enable Server Integration",
                "Włącz autoryzowany protokół wymiany pozycji z dedykowanym serwerem Zombek.", false
        );
        this.groupIntegration.addSetting(enableServerIntegrationSetting);
        registerGroup(this.groupIntegration);
    }

    public static TeamViewModule getInstance() {
        return instance;
    }

    @Override
    public void onInitialize() {}

    @Override
    public void onEnable() {
        TeamTracker.getInstance().clear();
    }

    @Override
    public void onDisable() {
        TeamTracker.getInstance().clear();
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;

        if (resetTeamSetting.getValue()) {
            TeamTracker.getInstance().clearManualTeam();
            resetTeamSetting.setValue(false);
        }

        // Prune stale teammates not updated for over 1000ms
        TeamTracker.getInstance().pruneStale(1000L);
    }

    // Getters for settings
    public EnumSetting<TeamDetectionMode> getDetectionModeSetting() { return detectionModeSetting; }
    public BooleanSetting getShowMarkersSetting() { return showMarkersSetting; }
    public BooleanSetting getShowNamesSetting() { return showNamesSetting; }
    public BooleanSetting getShowDistanceSetting() { return showDistanceSetting; }
    public BooleanSetting getShowTeamColorSetting() { return showTeamColorSetting; }

    public EnumSetting<MarkerStyle> getMarkerStyleSetting() { return markerStyleSetting; }
    public NumberSetting getMarkerSizeSetting() { return markerSizeSetting; }
    public ColorSetting getMarkerColorSetting() { return markerColorSetting; }
    public ColorSetting getNameColorSetting() { return nameColorSetting; }
    public ColorSetting getDistanceColorSetting() { return distanceColorSetting; }
    public BooleanSetting getBackgroundSetting() { return backgroundSetting; }
    public NumberSetting getOpacitySetting() { return opacitySetting; }

    public NumberSetting getMaxDistanceSetting() { return maxDistanceSetting; }
    public BooleanSetting getOnlyVisibleTeammatesSetting() { return onlyVisibleTeammatesSetting; }
    public BooleanSetting getHideInGuiSetting() { return hideInGuiSetting; }
    public BooleanSetting getHideWhenDeadSetting() { return hideWhenDeadSetting; }
    public BooleanSetting getHideWhenSpectatorSetting() { return hideWhenSpectatorSetting; }

    public ColorSetting getDefaultTeamColorSetting() { return defaultTeamColorSetting; }
    public BooleanSetting getEnableServerIntegrationSetting() { return enableServerIntegrationSetting; }
}
