package pl.zombek.client.core.modules.hud.playtime;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.component.HudComponentModel;
import pl.zombek.client.core.hud.component.HudComponentRenderer;
import pl.zombek.client.core.hud.component.HudComponentStyle;
import pl.zombek.client.core.hud.component.HudPriorityResolver;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;
import pl.zombek.client.core.time.ITimeDataProvider;
import pl.zombek.client.core.time.PlaytimeStats;
import pl.zombek.client.core.time.ZombekTimeService;

import java.util.ArrayList;
import java.util.List;

/**
 * Universal Playtime HUD module tracking client instance uptime, current world/server session,
 * and historical session statistics while strictly respecting user privacy.
 */
public class PlaytimeModule extends AbstractModule implements IHudElement {

    public static final String ID = "playtime";

    private final HudComponentModel model;

    // 1. General Group
    private final BooleanSetting showClientSessionSetting;
    private final BooleanSetting showWorldSessionSetting;
    private final BooleanSetting showTotalClientSetting;
    private final BooleanSetting showWorldStatsSetting;
    private final BooleanSetting showAvgSessionSetting;
    private final BooleanSetting showLongestSessionSetting;
    private final NumberSetting prioritySetting;

    // 2. Tracking Group
    private final BooleanSetting trackClientSessionSetting;
    private final BooleanSetting trackWorldSessionSetting;
    private final BooleanSetting trackSingleplayerSetting;
    private final BooleanSetting trackMultiplayerSetting;
    private final BooleanSetting saveSessionHistorySetting;
    private final BooleanSetting trackInMenusSetting;

    // 3. Behavior Group
    private final BooleanSetting pauseMinimizedSetting;
    private final BooleanSetting pauseDisconnectedSetting;
    private final BooleanSetting resetOnRestartSetting;
    private final BooleanSetting autoSaveSetting;
    private final NumberSetting saveIntervalSecSetting;

    // 4. Privacy Group
    private final BooleanSetting saveServerNameSetting;
    private final BooleanSetting saveWorldNameSetting;

    // 5. Format Group
    private final EnumSetting<PlaytimeFormat> timeFormatSetting;
    private final BooleanSetting showSecondsSetting;
    private final BooleanSetting showLabelsSetting;

    // 6. Appearance Group
    private final EnumSetting<HudComponentStyle> styleSetting;
    private final ColorSetting textColorSetting;
    private final ColorSetting secondaryColorSetting;
    private final ColorSetting backgroundColorSetting;
    private final NumberSetting backgroundOpacitySetting;
    private final ColorSetting borderColorSetting;
    private final BooleanSetting showBorderSetting;
    private final NumberSetting borderThicknessSetting;
    private final BooleanSetting shadowSetting;

    // 7. Layout Group
    private final EnumSetting<HudAnchor> anchorSetting;
    private final NumberSetting posXSetting;
    private final NumberSetting posYSetting;
    private final NumberSetting scaleSetting;

    public PlaytimeModule() {
        super(ID, "Playtime", "Precyzyjnie mierzy czas spędzony w kliencie oraz w aktualnym świecie gry.", ModuleCategory.HUD);

        this.model = new HudComponentModel(ID, 110, 24, HudAnchor.TOP_LEFT);
        this.model.setRenderPriority(HudPriorityResolver.PRIORITY_PLAYTIME);

        // 1. General
        SettingGroup generalGroup = new SettingGroup("general", "Ogólne");
        this.showClientSessionSetting = generalGroup.addSetting(new BooleanSetting("show_client_session", "Czas sesji klienta", "Wyświetla czas od startu gry", true));
        this.showWorldSessionSetting = generalGroup.addSetting(new BooleanSetting("show_world_session", "Czas sesji świata", "Wyświetla czas na bieżącym świecie/serwerze", true));
        this.showTotalClientSetting = generalGroup.addSetting(new BooleanSetting("show_total_client", "Łączny czas gry klienta", "Wyświetla łączny czas klienta ze wszystkich sesji", false));
        this.showWorldStatsSetting = generalGroup.addSetting(new BooleanSetting("show_world_stats", "Statystyka świata SP", "Statystyka czasu z pliku zapisu w Singleplayer", false));
        this.showAvgSessionSetting = generalGroup.addSetting(new BooleanSetting("show_avg_session", "Średnia długość sesji", "Średnia długość z historii", false));
        this.showLongestSessionSetting = generalGroup.addSetting(new BooleanSetting("show_longest_session", "Najdłuższa sesja", "Rekordowa długość sesji", false));
        this.prioritySetting = generalGroup.addSetting(new NumberSetting("render_priority", "Priorytet renderowania", "Priorytet w resolverze kolizji", (double) HudPriorityResolver.PRIORITY_PLAYTIME, 0.0, 100.0, 1.0));
        registerGroup(generalGroup);

        // 2. Tracking
        SettingGroup trackingGroup = new SettingGroup("tracking", "Śledzenie");
        this.trackClientSessionSetting = trackingGroup.addSetting(new BooleanSetting("track_client_session", "Śledź czas instancji", "Liczy czas procesu klienta", true));
        this.trackWorldSessionSetting = trackingGroup.addSetting(new BooleanSetting("track_world_session", "Śledź czas w świecie", "Liczy czas w świecie gry", true));
        this.trackSingleplayerSetting = trackingGroup.addSetting(new BooleanSetting("track_singleplayer", "Śledź Singleplayer", "Uwzględnia gry jednoosobowe", true));
        this.trackMultiplayerSetting = trackingGroup.addSetting(new BooleanSetting("track_multiplayer", "Śledź Multiplayer", "Uwzględnia serwery sieciowe", true));
        this.saveSessionHistorySetting = trackingGroup.addSetting(new BooleanSetting("save_session_history", "Zapisuj historię sesji", "Zapisuje rekordy sesji w pliku", true));
        this.trackInMenusSetting = trackingGroup.addSetting(new BooleanSetting("track_in_menus", "Licz czas w menu", "Zlicza czas podczas pauzy/menu", true));
        registerGroup(trackingGroup);

        // 3. Behavior
        SettingGroup behaviorGroup = new SettingGroup("behavior", "Zachowanie");
        this.pauseMinimizedSetting = behaviorGroup.addSetting(new BooleanSetting("pause_minimized", "Wstrzymaj po zminimalizowaniu", "Pauzuje licznik gdy okno jest zminimalizowane", false));
        this.pauseDisconnectedSetting = behaviorGroup.addSetting(new BooleanSetting("pause_disconnected", "Wstrzymaj po rozłączeniu", "Pauzuje licznik po wyjściu z serwera", true));
        this.resetOnRestartSetting = behaviorGroup.addSetting(new BooleanSetting("reset_on_restart", "Zeruj przy restarcie klienta", "Resetuje licznik po restarcie", false));
        this.autoSaveSetting = behaviorGroup.addSetting(new BooleanSetting("auto_save", "Automatyczny zapis historii", "Okresowo zapisuje historię na dysk", true));
        this.saveIntervalSecSetting = behaviorGroup.addSetting(new NumberSetting("save_interval_sec", "Interwał zapisu (sekundy)", "Częstotliwość zapisu historii", 60.0, 10.0, 600.0, 10.0));
        registerGroup(behaviorGroup);

        // 4. Privacy
        SettingGroup privacyGroup = new SettingGroup("privacy", "Prywatność");
        this.saveServerNameSetting = privacyGroup.addSetting(new BooleanSetting("save_server_name", "Zapisuj adresy serwerów (Opt-In)", "Zezwala na zapisywanie IP serwerów w historii", false));
        this.saveWorldNameSetting = privacyGroup.addSetting(new BooleanSetting("save_world_name", "Zapisuj nazwy lokalnych światów", "Zezwala na zapisywanie nazw światów SP", true));
        registerGroup(privacyGroup);

        // 5. Format
        SettingGroup formatGroup = new SettingGroup("format", "Formatowanie");
        this.timeFormatSetting = formatGroup.addSetting(new EnumSetting<>("time_format", "Format czasu", "Wybór stylu formatowania czasu", PlaytimeFormat.HH_MM_SS));
        this.showSecondsSetting = formatGroup.addSetting(new BooleanSetting("show_seconds", "Pokaż sekundy", "Wyświetla sekundy w czasie", true));
        this.showLabelsSetting = formatGroup.addSetting(new BooleanSetting("show_labels", "Pokaż etykiety", "Dodaje etykiety Sesja:, Klient: itp.", true));
        registerGroup(formatGroup);

        // 6. Appearance
        SettingGroup appearanceGroup = new SettingGroup("appearance", "Wygląd");
        this.styleSetting = appearanceGroup.addSetting(new EnumSetting<>("style", "Styl", "Styl obramowania i tła", HudComponentStyle.CLASSIC));
        this.textColorSetting = appearanceGroup.addSetting(new ColorSetting("text_color", "Kolor tekstu", "Główny kolor tekstu", 0xFFFFFFFF));
        this.secondaryColorSetting = appearanceGroup.addSetting(new ColorSetting("secondary_color", "Kolor etykiet", "Kolor prefiksów i etykiet", 0xFFFFD700));
        this.backgroundColorSetting = appearanceGroup.addSetting(new ColorSetting("background_color", "Kolor tła", "Własny kolor tła", 0x80000000));
        this.backgroundOpacitySetting = appearanceGroup.addSetting(new NumberSetting("background_opacity", "Przezroczystość tła", "Stopień krycia tła", 50.0, 0.0, 100.0, 5.0));
        this.borderColorSetting = appearanceGroup.addSetting(new ColorSetting("border_color", "Kolor ramki", "Kolor obramowania", 0x40FFFFFF));
        this.showBorderSetting = appearanceGroup.addSetting(new BooleanSetting("show_border", "Pokaż obramowanie", "Rysuje ramkę wokół komponentu", true));
        this.borderThicknessSetting = appearanceGroup.addSetting(new NumberSetting("border_thickness", "Grubość ramki", "Grubość obramowania w pikselach", 1.0, 1.0, 4.0, 1.0));
        this.shadowSetting = appearanceGroup.addSetting(new BooleanSetting("shadow", "Cień tekstu", "Włącza cień pod tekstem", true));
        registerGroup(appearanceGroup);

        // 7. Layout
        SettingGroup layoutGroup = new SettingGroup("layout", "Pozycja");
        this.anchorSetting = layoutGroup.addSetting(new EnumSetting<>("anchor", "Zakotwiczenie", "Punkt odniesienia pozycji", HudAnchor.TOP_LEFT));
        this.posXSetting = layoutGroup.addSetting(new NumberSetting("pos_x", "Pozycja X", "Przesunięcie w poziomie", 10.0, 0.0, 1000.0, 1.0));
        this.posYSetting = layoutGroup.addSetting(new NumberSetting("pos_y", "Pozycja Y", "Przesunięcie w pionie", 120.0, 0.0, 1000.0, 1.0));
        this.scaleSetting = layoutGroup.addSetting(new NumberSetting("scale", "Skala", "Współczynnik wielkości", 1.0, 0.5, 2.0, 0.1));
        registerGroup(layoutGroup);
    }

    @Override
    public String getElementId() {
        return getId();
    }

    @Override
    public boolean isVisible() {
        return isEnabled() && !hasError();
    }

    public HudComponentModel getHudModel() {
        model.setAnchor(anchorSetting.getValue());
        model.setPosX(posXSetting.getValue().intValue());
        model.setPosY(posYSetting.getValue().intValue());
        model.setScale(scaleSetting.getValue());
        model.setRenderPriority(prioritySetting.getValue().intValue());
        model.setVisible(isVisible());
        return model;
    }

    @Override
    public void render(IHudRenderer renderer) {
        if (!isVisible()) return;

        ZombekTimeService timeService = ZombekTimeService.getInstance();
        timeService.setSaveServerNameOptIn(saveServerNameSetting.getValue());
        timeService.setSaveWorldNameOptIn(saveWorldNameSetting.getValue());
        timeService.setSaveSessionHistoryEnabled(saveSessionHistorySetting.getValue());

        ITimeDataProvider provider = timeService.getTimeDataProvider();
        boolean inGame = provider != null && provider.isWorldLoaded();

        List<String> lines = buildDisplayLines(timeService, provider, inGame);
        if (lines.isEmpty()) return;

        int screenWidth = renderer.getScreenWidth();
        int screenHeight = renderer.getScreenHeight();
        float txtScale = scaleSetting.getValue().floatValue();

        int maxTextWidth = 0;
        for (String line : lines) {
            maxTextWidth = Math.max(maxTextWidth, (int) Math.ceil(renderer.getTextWidth(line) * txtScale));
        }

        int paddingH = 6;
        int paddingV = 4;
        int lineHeight = (int) Math.ceil(renderer.getFontHeight() * txtScale) + 2;
        int contentWidth = maxTextWidth + (paddingH * 2);
        int contentHeight = (lines.size() * lineHeight) + (paddingV * 2);

        model.setWidth(contentWidth);
        model.setHeight(contentHeight);
        model.setAnchor(anchorSetting.getValue());
        model.setPosX(posXSetting.getValue().intValue());
        model.setPosY(posYSetting.getValue().intValue());
        model.clampToScreen(screenWidth, screenHeight);

        int renderX = model.getScreenX(screenWidth);
        int renderY = model.getScreenY(screenHeight);

        // Render Background
        HudComponentRenderer.renderBackground(
                renderer,
                renderX, renderY, contentWidth, contentHeight,
                styleSetting.getValue(),
                backgroundColorSetting.getValue(),
                backgroundOpacitySetting.getValue(),
                borderColorSetting.getValue(),
                showBorderSetting.getValue(),
                borderThicknessSetting.getValue().intValue()
        );

        // Render Lines
        boolean shadow = shadowSetting.getValue();
        int curY = renderY + paddingV;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int color = (i == 0) ? textColorSetting.getValue() : secondaryColorSetting.getValue();
            renderer.drawText(line, renderX + paddingH, curY, color, shadow, txtScale, false);
            curY += lineHeight;
        }
    }

    public List<String> buildDisplayLines(ZombekTimeService timeService, ITimeDataProvider provider, boolean inGame) {
        List<String> lines = new ArrayList<>();
        PlaytimeFormat fmt = timeFormatSetting.getValue();
        boolean showSec = showSecondsSetting.getValue();
        boolean showLbl = showLabelsSetting.getValue();

        if (!inGame) {
            lines.add("Session: 02:35:41 [PREVIEW]");
            return lines;
        }

        if (showWorldSessionSetting.getValue()) {
            long worldSec = timeService.getWorldSessionSeconds();
            String formatted = fmt.format(worldSec, showSec);
            lines.add(showLbl ? "Sesja: " + formatted : formatted);
        }

        if (showClientSessionSetting.getValue()) {
            long clientSec = timeService.getClientSessionSeconds();
            String formatted = fmt.format(clientSec, showSec);
            lines.add(showLbl ? "Klient: " + formatted : formatted);
        }

        if (showTotalClientSetting.getValue()) {
            long totalSec = timeService.getTotalClientPlaytimeSeconds();
            String formatted = fmt.format(totalSec, showSec);
            lines.add(showLbl ? "Łącznie: " + formatted : formatted);
        }

        if (showWorldStatsSetting.getValue()) {
            if (provider.isSingleplayer()) {
                long spTicks = provider.getSingleplayerPlaytimeTicks();
                if (spTicks >= 0) {
                    long spSeconds = spTicks / 20L;
                    String formatted = fmt.format(spSeconds, showSec);
                    lines.add(showLbl ? "Świat: " + formatted : formatted);
                } else {
                    lines.add(showLbl ? "Świat: N/A" : "N/A");
                }
            } else {
                lines.add(showLbl ? "Świat: N/A (Multiplayer)" : "N/A");
            }
        }

        PlaytimeStats stats = timeService.getPlaytimeStats();
        if (showAvgSessionSetting.getValue()) {
            String formatted = fmt.format(stats.getAverageSessionSeconds(), showSec);
            lines.add(showLbl ? "Średnia: " + formatted : formatted);
        }
        if (showLongestSessionSetting.getValue()) {
            String formatted = fmt.format(stats.getLongestSessionSeconds(), showSec);
            lines.add(showLbl ? "Rekord: " + formatted : formatted);
        }

        if (lines.isEmpty()) {
            long worldSec = timeService.getWorldSessionSeconds();
            lines.add("Sesja: " + fmt.format(worldSec, showSec));
        }

        return lines;
    }
}
