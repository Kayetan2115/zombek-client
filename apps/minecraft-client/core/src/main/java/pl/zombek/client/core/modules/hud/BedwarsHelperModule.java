package pl.zombek.client.core.modules.hud;

import pl.zombek.client.core.bedwars.*;
import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

import java.util.List;

/**
 * Bedwars Helper HUD Module.
 * Provides a customizable in-game heads-up display tracking Bedwars matches:
 * game info, team beds, player counts, session performance, timers, and event feed.
 */
public class BedwarsHelperModule extends AbstractModule implements IHudElement {
    private static BedwarsHelperModule instance;

    // 1. General
    private final SettingGroup groupGeneral;
    private final BooleanSetting autoDetectBedwarsSetting;
    private final BooleanSetting autoEnableOverlaySetting;
    private final BooleanSetting requireHighConfidenceSetting;
    private final BooleanSetting manualModeSetting;

    // 2. Game Info
    private final SettingGroup groupGameInfo;
    private final BooleanSetting showGameInfoSetting;
    private final BooleanSetting showModeSetting;
    private final BooleanSetting showMapSetting;
    private final BooleanSetting showMatchTimeSetting;
    private final BooleanSetting showGameStateSetting;

    // 3. Team Board
    private final SettingGroup groupTeamBoard;
    private final BooleanSetting showTeamBoardSetting;
    private final BooleanSetting showBedStatusSetting;
    private final BooleanSetting showAlivePlayersSetting;
    private final BooleanSetting showEliminatedTeamsSetting;
    private final EnumSetting<TeamBoardLayout> teamLayoutSetting;

    // 4. My Team
    private final SettingGroup groupMyTeam;
    private final BooleanSetting showMyTeamSetting;
    private final BooleanSetting showPlayerNamesSetting;
    private final BooleanSetting showAvailableStatusSetting;

    // 5. Session Stats
    private final SettingGroup groupSessionStats;
    private final BooleanSetting showSessionStatsSetting;
    private final BooleanSetting showKillsSetting;
    private final BooleanSetting showFinalKillsSetting;
    private final BooleanSetting showDeathsSetting;
    private final BooleanSetting showBedsBrokenSetting;
    private final BooleanSetting showSessionTimeSetting;
    private final BooleanSetting autoResetSetting;

    // 6. Event Feed
    private final SettingGroup groupEventFeed;
    private final BooleanSetting enableEventFeedSetting;
    private final NumberSetting maxEventsSetting;
    private final NumberSetting eventDurationSetting;
    private final BooleanSetting showTimestampsSetting;
    private final BooleanSetting showIconsSetting;
    private final BooleanSetting fadeAnimationSetting;

    // 7. Timers
    private final SettingGroup groupTimers;
    private final BooleanSetting showTimersSetting;
    private final BooleanSetting showMatchTimerSetting;
    private final BooleanSetting showBedDestructionTimerSetting;
    private final BooleanSetting showSuddenDeathTimerSetting;
    private final BooleanSetting showUpgradeTimersSetting;

    // 8. Parser
    private final SettingGroup groupParser;
    private final EnumSetting<ParserProfile> parserProfileSetting;
    private final StringSetting languageSetting;
    private final StringSetting customPatternsSetting;
    private final BooleanSetting debugModeSetting;
    private final NumberSetting parserPrioritySetting;

    // 9. Appearance
    private final SettingGroup groupAppearance;
    private final EnumSetting<BedwarsLayout> layoutStyleSetting;
    private final EnumSetting<HudAnchor> anchorSetting;
    private final NumberSetting offsetXSetting;
    private final NumberSetting offsetYSetting;
    private final NumberSetting scaleSetting;
    private final NumberSetting spacingSetting;
    private final NumberSetting paddingSetting;
    private final NumberSetting backgroundOpacitySetting;
    private final ColorSetting titleColorSetting;
    private final ColorSetting textColorSetting;
    private final ColorSetting valueColorSetting;

    public BedwarsHelperModule() {
        super(
                "bedwars_helper",
                "Bedwars Helper",
                "Kompleksowy panel HUD śledzący przebieg meczu Bedwars: status łóżek, drużyny, statystyki i timery.",
                ModuleCategory.HUD
        );
        instance = this;

        // 1. General
        this.groupGeneral = new SettingGroup("general", "Ustawienia ogólne");
        this.autoDetectBedwarsSetting = new BooleanSetting("auto_detect", "Wykrywaj Bedwars", "Automatyczne rozpoznawanie meczu Bedwars", true);
        this.autoEnableOverlaySetting = new BooleanSetting("auto_enable", "Auto-aktywacja HUD", "Automatyczne wyświetlanie HUD po wykryciu Bedwars", true);
        this.requireHighConfidenceSetting = new BooleanSetting("require_high_conf", "Wymagaj wysokiej pewności", "Włączaj tylko przy wysokim poziomie pewności detekcji", false);
        this.manualModeSetting = new BooleanSetting("manual_mode", "Tryb ręczny (Testowy)", "Wymusza wyświetlanie HUD poza serwerem Bedwars", false);
        this.groupGeneral.addSetting(autoDetectBedwarsSetting);
        this.groupGeneral.addSetting(autoEnableOverlaySetting);
        this.groupGeneral.addSetting(requireHighConfidenceSetting);
        this.groupGeneral.addSetting(manualModeSetting);
        registerGroup(this.groupGeneral);

        // 2. Game Info
        this.groupGameInfo = new SettingGroup("game_info", "Informacje o grze");
        this.showGameInfoSetting = new BooleanSetting("show_game_info", "Pokaż sekcję gry", "Wyświetla tryb, mapę i status meczu", true);
        this.showModeSetting = new BooleanSetting("show_mode", "Pokaż tryb gry", "Wyświetla rozpoznany tryb Bedwars (np. 4v4v4v4)", true);
        this.showMapSetting = new BooleanSetting("show_map", "Pokaż mapę", "Wyświetla nazwę mapy, jeśli jest dostępna", true);
        this.showMatchTimeSetting = new BooleanSetting("show_match_time", "Pokaż czas meczu", "Wyświetla czas trwania meczu", true);
        this.showGameStateSetting = new BooleanSetting("show_game_state", "Pokaż stan meczu", "Wyświetla bieżącą fazę rozgrywki", true);
        this.groupGameInfo.addSetting(showGameInfoSetting);
        this.groupGameInfo.addSetting(showModeSetting);
        this.groupGameInfo.addSetting(showMapSetting);
        this.groupGameInfo.addSetting(showMatchTimeSetting);
        this.groupGameInfo.addSetting(showGameStateSetting);
        registerGroup(this.groupGameInfo);

        // 3. Team Board
        this.groupTeamBoard = new SettingGroup("team_board", "Panel drużyn (Team Board)");
        this.showTeamBoardSetting = new BooleanSetting("show_team_board", "Pokaż Team Board", "Włącza panel z listą drużyn i stanem łóżek", true);
        this.showBedStatusSetting = new BooleanSetting("show_bed_status", "Pokaż status łóżek", "Wyświetla symbole ✓ lub ✕ stanu łóżek", true);
        this.showAlivePlayersSetting = new BooleanSetting("show_alive_players", "Pokaż liczbę graczy", "Wyświetla liczbę żywych graczy w drużynie", true);
        this.showEliminatedTeamsSetting = new BooleanSetting("show_eliminated", "Pokaż wyeliminowane", "Pokazuje wyeliminowane drużyny z symbolem [E]", true);
        this.teamLayoutSetting = new EnumSetting<>("team_layout", "Układ Team Board", "Sposób prezentacji drużyn", TeamBoardLayout.COMPACT);
        this.groupTeamBoard.addSetting(showTeamBoardSetting);
        this.groupTeamBoard.addSetting(showBedStatusSetting);
        this.groupTeamBoard.addSetting(showAlivePlayersSetting);
        this.groupTeamBoard.addSetting(showEliminatedTeamsSetting);
        this.groupTeamBoard.addSetting(teamLayoutSetting);
        registerGroup(this.groupTeamBoard);

        // 4. My Team
        this.groupMyTeam = new SettingGroup("my_team", "Moja drużyna");
        this.showMyTeamSetting = new BooleanSetting("show_my_team", "Pokaż moją drużynę", "Wyświetla listę członków własnej drużyny", false);
        this.showPlayerNamesSetting = new BooleanSetting("show_player_names", "Pokaż nicki graczy", "Wyświetla nazwy sojuszników", true);
        this.showAvailableStatusSetting = new BooleanSetting("show_available", "Status obecności", "Pokazuje dostępność sojuszników na liście graczy", true);
        this.groupMyTeam.addSetting(showMyTeamSetting);
        this.groupMyTeam.addSetting(showPlayerNamesSetting);
        this.groupMyTeam.addSetting(showAvailableStatusSetting);
        registerGroup(this.groupMyTeam);

        // 5. Session Stats
        this.groupSessionStats = new SettingGroup("session_stats", "Statystyki sesji");
        this.showSessionStatsSetting = new BooleanSetting("show_session_stats", "Pokaż statystyki", "Wyświetla liczniki zabójstw, łóżek i zgonów", true);
        this.showKillsSetting = new BooleanSetting("show_kills", "Zabójstwa (Kills)", "Licznik pokonanych graczy", true);
        this.showFinalKillsSetting = new BooleanSetting("show_final_kills", "Final Kills", "Licznik ostatecznych eliminacji", true);
        this.showDeathsSetting = new BooleanSetting("show_deaths", "Zgony (Deaths)", "Licznik zgonów własnych", true);
        this.showBedsBrokenSetting = new BooleanSetting("show_beds_broken", "Zniszczone łóżka", "Licznik zniszczonych łóżek przez gracza", true);
        this.showSessionTimeSetting = new BooleanSetting("show_session_time", "Czas sesji", "Czas trwania bieżącej sesji meczowej", true);
        this.autoResetSetting = new BooleanSetting("auto_reset", "Automatyczny reset", "Automatyczny reset statystyk przy nowym meczu", true);
        this.groupSessionStats.addSetting(showSessionStatsSetting);
        this.groupSessionStats.addSetting(showKillsSetting);
        this.groupSessionStats.addSetting(showFinalKillsSetting);
        this.groupSessionStats.addSetting(showDeathsSetting);
        this.groupSessionStats.addSetting(showBedsBrokenSetting);
        this.groupSessionStats.addSetting(showSessionTimeSetting);
        this.groupSessionStats.addSetting(autoResetSetting);
        registerGroup(this.groupSessionStats);

        // 6. Event Feed
        this.groupEventFeed = new SettingGroup("event_feed", "Dziennik zdarzeń (Event Feed)");
        this.enableEventFeedSetting = new BooleanSetting("enable_event_feed", "Włącz Event Feed", "Wyświetla ostatnie wydarzenia w meczu", true);
        this.maxEventsSetting = new NumberSetting("max_events", "Maksymalna liczba", "Liczba wyświetlanych ostatnich zdarzeń", 5, 1, 10, 1);
        this.eventDurationSetting = new NumberSetting("event_duration", "Czas trwania (s)", "Czas widoczności zdarzenia w sekundach", 8.0, 3.0, 20.0, 1.0);
        this.showTimestampsSetting = new BooleanSetting("show_timestamps", "Pokaż znaczniki czasu", "Wyświetla godzinę zdarzenia", true);
        this.showIconsSetting = new BooleanSetting("show_icons", "Pokaż ikony", "Wyświetla ikony zdarzeń", true);
        this.fadeAnimationSetting = new BooleanSetting("fade_animation", "Płynne wygaszanie", "Animacja zanikania starych zdarzeń", true);
        this.groupEventFeed.addSetting(enableEventFeedSetting);
        this.groupEventFeed.addSetting(maxEventsSetting);
        this.groupEventFeed.addSetting(eventDurationSetting);
        this.groupEventFeed.addSetting(showTimestampsSetting);
        this.groupEventFeed.addSetting(showIconsSetting);
        this.groupEventFeed.addSetting(fadeAnimationSetting);
        registerGroup(this.groupEventFeed);

        // 7. Timers
        this.groupTimers = new SettingGroup("timers", "Timery i odliczanie");
        this.showTimersSetting = new BooleanSetting("show_timers", "Pokaż timery", "Włącza sekcję odliczania wydarzeń", true);
        this.showMatchTimerSetting = new BooleanSetting("show_match_timer", "Zegar meczu", "Wyświetla stoper czasu gry", true);
        this.showBedDestructionTimerSetting = new BooleanSetting("show_bed_dest_timer", "Zniszczenie łóżek", "Odliczanie do globalnego zniszczenia łóżek", true);
        this.showSuddenDeathTimerSetting = new BooleanSetting("show_sudden_death_timer", "Nagła śmierć", "Odliczanie do fazy smoków", true);
        this.showUpgradeTimersSetting = new BooleanSetting("show_upgrades", "Ulepszenia surowców", "Odliczanie do ulepszeń diamentów i szmaragdów", true);
        this.groupTimers.addSetting(showTimersSetting);
        this.groupTimers.addSetting(showMatchTimerSetting);
        this.groupTimers.addSetting(showBedDestructionTimerSetting);
        this.groupTimers.addSetting(showSuddenDeathTimerSetting);
        this.groupTimers.addSetting(showUpgradeTimersSetting);
        registerGroup(this.groupTimers);

        // 8. Parser
        this.groupParser = new SettingGroup("parser", "Konfiguracja parsera");
        this.parserProfileSetting = new EnumSetting<>("parser_profile", "Profil parsera", "Format scoreboardu serwera", ParserProfile.HYPIXEL_ENGLISH);
        this.languageSetting = new StringSetting("language", "Język wiadomości", "Język komunikatów (ALL, EN, PL)", "ALL");
        this.customPatternsSetting = new StringSetting("custom_patterns", "Własne wzorce", "Reguły niestandardowe rozdzielane średnikami", "");
        this.debugModeSetting = new BooleanSetting("debug_mode", "Tryb diagnostyczny", "Zapisuje linie czatu i scoreboard w logach", false);
        this.parserPrioritySetting = new NumberSetting("parser_priority", "Priorytet parsera", "Priorytet przetwarzania reguł", 1, 1, 5, 1);
        this.groupParser.addSetting(parserProfileSetting);
        this.groupParser.addSetting(languageSetting);
        this.groupParser.addSetting(customPatternsSetting);
        this.groupParser.addSetting(debugModeSetting);
        this.groupParser.addSetting(parserPrioritySetting);
        registerGroup(this.groupParser);

        // 9. Appearance
        this.groupAppearance = new SettingGroup("appearance", "Wygląd i pozycja");
        this.layoutStyleSetting = new EnumSetting<>("layout_style", "Układ główny", "Ogólny format panelu Bedwars", BedwarsLayout.COMPACT);
        this.anchorSetting = new EnumSetting<>("anchor", "Punkt zakotwiczenia", "Kotwica panelu na ekranie", HudAnchor.TOP_RIGHT);
        this.offsetXSetting = new NumberSetting("offset_x", "Przesunięcie X", "Poziome przesunięcie od kotwicy", 10, -500, 500, 1);
        this.offsetYSetting = new NumberSetting("offset_y", "Przesunięcie Y", "Pionowe przesunięcie od kotwicy", 10, -500, 500, 1);
        this.scaleSetting = new NumberSetting("scale", "Skala HUD", "Mnożnik wielkości panelu", 1.0, 0.5, 2.0, 0.05);
        this.spacingSetting = new NumberSetting("spacing", "Odstępy elementów", "Odstęp między liniami w pikselach", 3, 1, 10, 1);
        this.paddingSetting = new NumberSetting("padding", "Margines wewnętrzny", "Margines od krawędzi ramki", 6, 2, 15, 1);
        this.backgroundOpacitySetting = new NumberSetting("bg_opacity", "Przezroczystość tła", "Stopień krycia tła", 0.65, 0.0, 1.0, 0.05);
        this.titleColorSetting = new ColorSetting("title_color", "Kolor nagłówka", "Kolor tytułu panelu", 0xFFFF5555);
        this.textColorSetting = new ColorSetting("text_color", "Kolor etykiet", "Kolor tekstu informacyjnego", 0xFFFFFFFF);
        this.valueColorSetting = new ColorSetting("value_color", "Kolor wartości", "Kolor liczb i statusów", 0xFF55FF55);
        this.groupAppearance.addSetting(layoutStyleSetting);
        this.groupAppearance.addSetting(anchorSetting);
        this.groupAppearance.addSetting(offsetXSetting);
        this.groupAppearance.addSetting(offsetYSetting);
        this.groupAppearance.addSetting(scaleSetting);
        this.groupAppearance.addSetting(spacingSetting);
        this.groupAppearance.addSetting(paddingSetting);
        this.groupAppearance.addSetting(backgroundOpacitySetting);
        this.groupAppearance.addSetting(titleColorSetting);
        this.groupAppearance.addSetting(textColorSetting);
        this.groupAppearance.addSetting(valueColorSetting);
        registerGroup(this.groupAppearance);
    }

    public static synchronized BedwarsHelperModule getInstance() {
        return instance;
    }

    @Override
    public String getElementId() {
        return getId();
    }

    @Override
    public void onInitialize() {
        HudElementRegistry.getInstance().register(this);
        syncParserSettings();
    }

    @Override
    public void onEnable() {
        syncParserSettings();
    }

    @Override
    public void onDisable() {
        // Safe disable
    }

    @Override
    public void onTick() {
        BedwarsManager.getInstance().onTick();
    }

    private void syncParserSettings() {
        BedwarsManager manager = BedwarsManager.getInstance();
        manager.getScoreboardParser().setProfile(parserProfileSetting.getValue());
        manager.getScoreboardParser().setCustomPatterns(customPatternsSetting.getValue());
        manager.getScoreboardParser().setDebugMode(debugModeSetting.getValue());
        manager.getScoreboardParser().setParserPriority(parserPrioritySetting.getValue().intValue());

        manager.getChatParser().setLanguage(languageSetting.getValue());
        manager.getChatParser().setDebugMode(debugModeSetting.getValue());
        manager.getChatParser().setCustomPatterns(customPatternsSetting.getValue());

        manager.getEventTracker().setMaxEvents(maxEventsSetting.getValue().intValue());
        manager.getEventTracker().setEventDurationSeconds(eventDurationSetting.getValue().floatValue());
        manager.getEventTracker().setShowTimestamps(showTimestampsSetting.getValue());
        manager.getEventTracker().setShowIcons(showIconsSetting.getValue());
        manager.getEventTracker().setFadeAnimation(fadeAnimationSetting.getValue());

        manager.getSessionTracker().setAutoResetOnNewMatch(autoResetSetting.getValue());
    }

    @Override
    public boolean isVisible() {
        if (!isEnabled() || hasError()) {
            return false;
        }
        BedwarsManager manager = BedwarsManager.getInstance();
        return manager.getGameDetector().shouldDisplayOverlay(
                autoDetectBedwarsSetting.getValue(),
                autoEnableOverlaySetting.getValue(),
                manualModeSetting.getValue(),
                requireHighConfidenceSetting.getValue()
        );
    }

    @Override
    public void render(IHudRenderer renderer) {
        if (!isVisible() || renderer == null) {
            return;
        }

        syncParserSettings();
        BedwarsManager manager = BedwarsManager.getInstance();

        int screenWidth = renderer.getScreenWidth();
        int screenHeight = renderer.getScreenHeight();
        float scale = scaleSetting.getValue().floatValue();
        int padding = paddingSetting.getValue().intValue();
        int spacing = spacingSetting.getValue().intValue();
        int fontHeight = renderer.getFontHeight();

        // Calculate maximum width and height required
        int contentWidth = 140;
        int currentY = padding;

        // 1. Title Line
        String title = "BED WARS";
        contentWidth = Math.max(contentWidth, renderer.getTextWidth(title) + 10);
        currentY += fontHeight + spacing;

        // 2. Game Info Section
        if (showGameInfoSetting.getValue()) {
            String modeStr = showModeSetting.getValue() ? ("[" + manager.getGameDetector().getDetectedMode().getDisplayName() + "]") : "";
            String mapStr = showMapSetting.getValue() && !manager.getGameDetector().getDetectedMap().equals("N/A")
                    ? (" " + manager.getGameDetector().getDetectedMap()) : "";
            String infoLine = (modeStr + mapStr).trim();
            if (!infoLine.isEmpty()) {
                contentWidth = Math.max(contentWidth, renderer.getTextWidth(infoLine) + 8);
                currentY += fontHeight + spacing;
            }
            if (showGameStateSetting.getValue() && manager.getGameDetector().getCurrentState() != BedwarsGameState.UNKNOWN) {
                String stateLine = "Faza: " + manager.getGameDetector().getCurrentState().getDisplayName();
                contentWidth = Math.max(contentWidth, renderer.getTextWidth(stateLine) + 8);
                currentY += fontHeight + spacing;
            }
        }

        // 3. Team Board Section
        if (showTeamBoardSetting.getValue()) {
            currentY += 2;
            TeamBoardLayout teamLayout = teamLayoutSetting.getValue();
            if (teamLayout == TeamBoardLayout.COMPACT || teamLayout == TeamBoardLayout.HORIZONTAL) {
                StringBuilder compactSb = new StringBuilder();
                for (BedwarsTeam team : manager.getScoreboardParser().getParsedTeams().values()) {
                    if (team.isEliminated() && !showEliminatedTeamsSetting.getValue()) continue;
                    compactSb.append(team.getColor().getCode()).append(":")
                            .append(team.getBedStatus().getSymbol()).append(" ");
                }
                String compactLine = compactSb.toString().trim();
                if (!compactLine.isEmpty()) {
                    contentWidth = Math.max(contentWidth, renderer.getTextWidth(compactLine) + 12);
                    currentY += fontHeight + spacing;
                }
            } else {
                // Detailed / Vertical list
                for (BedwarsTeam team : manager.getScoreboardParser().getParsedTeams().values()) {
                    if (team.isEliminated() && !showEliminatedTeamsSetting.getValue()) continue;
                    String line = team.getName() + ": " + team.getBedStatus().getSymbol() +
                            (showAlivePlayersSetting.getValue() && team.getAlivePlayers() >= 0 ? (" (" + team.getAlivePlayers() + ")") : "");
                    contentWidth = Math.max(contentWidth, renderer.getTextWidth(line) + 14);
                    currentY += fontHeight + spacing;
                }
            }
        }

        // 4. Session Stats Section
        if (showSessionStatsSetting.getValue()) {
            currentY += 2;
            StringBuilder statsSb = new StringBuilder();
            if (showKillsSetting.getValue()) statsSb.append("K: ").append(manager.getSessionTracker().getObservedKills()).append(" ");
            if (showFinalKillsSetting.getValue()) statsSb.append("FK: ").append(manager.getSessionTracker().getConfirmedFinalKills()).append(" ");
            if (showDeathsSetting.getValue()) statsSb.append("D: ").append(manager.getSessionTracker().getObservedDeaths()).append(" ");
            if (showBedsBrokenSetting.getValue()) statsSb.append("B: ").append(manager.getSessionTracker().getConfirmedBedsBroken()).append(" ");
            String statsLine = statsSb.toString().trim();
            if (!statsLine.isEmpty()) {
                contentWidth = Math.max(contentWidth, renderer.getTextWidth(statsLine) + 8);
                currentY += fontHeight + spacing;
            }
        }

        // 5. Timers Section
        if (showTimersSetting.getValue()) {
            if (showMatchTimerSetting.getValue()) {
                String matchTimeLine = "Czas: " + manager.getTimerManager().getMatchTimer().getFormattedTime();
                contentWidth = Math.max(contentWidth, renderer.getTextWidth(matchTimeLine) + 8);
                currentY += fontHeight + spacing;
            }
            if (showUpgradeTimersSetting.getValue()) {
                for (BedwarsTimer t : manager.getTimerManager().getUpgradeTimers()) {
                    String upgradeLine = t.getName() + ": " + t.getFormattedTime();
                    contentWidth = Math.max(contentWidth, renderer.getTextWidth(upgradeLine) + 8);
                    currentY += fontHeight + spacing;
                }
            }
        }

        // 6. Event Feed Section
        if (enableEventFeedSetting.getValue()) {
            List<BedwarsEvent> events = manager.getEventTracker().getVisibleEvents();
            if (!events.isEmpty()) {
                currentY += 2;
                for (BedwarsEvent ev : events) {
                    String evLine = (showIconsSetting.getValue() ? (ev.getIcon() + " ") : "") + ev.getTitle();
                    contentWidth = Math.max(contentWidth, renderer.getTextWidth(evLine) + 12);
                    currentY += fontHeight + spacing;
                }
            }
        }

        int totalWidth = contentWidth + (padding * 2);
        int totalHeight = currentY + padding;

        // Position coordinates based on anchor
        HudAnchor anchor = anchorSetting.getValue();
        int renderX = anchor.calculateX(offsetXSetting.getValue().intValue(), totalWidth, screenWidth);
        int renderY = anchor.calculateY(offsetYSetting.getValue().intValue(), totalHeight, screenHeight);

        // 7. Render Background & Border
        int alphaInt = (int) (backgroundOpacitySetting.getValue().floatValue() * 255.0f);
        int bgArgb = (alphaInt << 24) | 0x12141A;
        renderer.fill(renderX, renderY, totalWidth, totalHeight, bgArgb);
        renderer.drawBorder(renderX, renderY, totalWidth, totalHeight, 0xFF3A4250, 1);

        // 8. Render Content
        int drawY = renderY + padding;
        int textX = renderX + padding;

        // Title
        renderer.drawText(title, textX, drawY, titleColorSetting.getValue(), true, scale, true);
        drawY += fontHeight + spacing;

        // Game Info
        if (showGameInfoSetting.getValue()) {
            String modeStr = showModeSetting.getValue() ? ("[" + manager.getGameDetector().getDetectedMode().getDisplayName() + "]") : "";
            String mapStr = showMapSetting.getValue() && !manager.getGameDetector().getDetectedMap().equals("N/A")
                    ? (" " + manager.getGameDetector().getDetectedMap()) : "";
            String infoLine = (modeStr + mapStr).trim();
            if (!infoLine.isEmpty()) {
                renderer.drawText(infoLine, textX, drawY, textColorSetting.getValue(), true, scale, false);
                drawY += fontHeight + spacing;
            }
            if (showGameStateSetting.getValue() && manager.getGameDetector().getCurrentState() != BedwarsGameState.UNKNOWN) {
                renderer.drawText("Faza: " + manager.getGameDetector().getCurrentState().getDisplayName(), textX, drawY, 0xFFAAAAAA, true, scale, false);
                drawY += fontHeight + spacing;
            }
        }

        // Team Board
        if (showTeamBoardSetting.getValue()) {
            TeamBoardLayout teamLayout = teamLayoutSetting.getValue();
            if (teamLayout == TeamBoardLayout.COMPACT || teamLayout == TeamBoardLayout.HORIZONTAL) {
                int badgeX = textX;
                for (BedwarsTeam team : manager.getScoreboardParser().getParsedTeams().values()) {
                    if (team.isEliminated() && !showEliminatedTeamsSetting.getValue()) continue;
                    String badge = team.getColor().getCode() + ":" + team.getBedStatus().getSymbol() + " ";
                    renderer.drawText(badge, badgeX, drawY, team.getColor().getArgb(), true, scale, true);
                    badgeX += renderer.getTextWidth(badge) + 3;
                }
                drawY += fontHeight + spacing;
            } else {
                for (BedwarsTeam team : manager.getScoreboardParser().getParsedTeams().values()) {
                    if (team.isEliminated() && !showEliminatedTeamsSetting.getValue()) continue;
                    String statusText = team.getBedStatus().getSymbol() +
                            (showAlivePlayersSetting.getValue() && team.getAlivePlayers() >= 0 ? (" (" + team.getAlivePlayers() + ")") : "");
                    renderer.drawText(team.getName() + ":", textX, drawY, team.getColor().getArgb(), true, scale, true);
                    renderer.drawText(statusText, textX + renderer.getTextWidth(team.getName() + ": "), drawY, team.getBedStatus().getDefaultColor(), true, scale, false);
                    drawY += fontHeight + spacing;
                }
            }
        }

        // Session Stats
        if (showSessionStatsSetting.getValue()) {
            StringBuilder statsSb = new StringBuilder();
            if (showKillsSetting.getValue()) statsSb.append("K: ").append(manager.getSessionTracker().getObservedKills()).append(" ");
            if (showFinalKillsSetting.getValue()) statsSb.append("FK: ").append(manager.getSessionTracker().getConfirmedFinalKills()).append(" ");
            if (showDeathsSetting.getValue()) statsSb.append("D: ").append(manager.getSessionTracker().getObservedDeaths()).append(" ");
            if (showBedsBrokenSetting.getValue()) statsSb.append("B: ").append(manager.getSessionTracker().getConfirmedBedsBroken()).append(" ");
            String statsLine = statsSb.toString().trim();
            if (!statsLine.isEmpty()) {
                renderer.drawText(statsLine, textX, drawY, valueColorSetting.getValue(), true, scale, false);
                drawY += fontHeight + spacing;
            }
        }

        // Timers
        if (showTimersSetting.getValue()) {
            if (showMatchTimerSetting.getValue()) {
                renderer.drawText("Czas: " + manager.getTimerManager().getMatchTimer().getFormattedTime(), textX, drawY, textColorSetting.getValue(), true, scale, false);
                drawY += fontHeight + spacing;
            }
            if (showUpgradeTimersSetting.getValue()) {
                for (BedwarsTimer t : manager.getTimerManager().getUpgradeTimers()) {
                    renderer.drawText(t.getName() + ": " + t.getFormattedTime(), textX, drawY, 0xFF55FFFF, true, scale, false);
                    drawY += fontHeight + spacing;
                }
            }
        }

        // Event Feed
        if (enableEventFeedSetting.getValue()) {
            List<BedwarsEvent> events = manager.getEventTracker().getVisibleEvents();
            for (BedwarsEvent ev : events) {
                float alpha = fadeAnimationSetting.getValue() ? ev.getFadeAlpha(eventDurationSetting.getValue().floatValue()) : 1.0f;
                int evAlpha = (int) (alpha * 255.0f);
                if (evAlpha <= 5) continue;
                int evColor = (evAlpha << 24) | (ev.getColor() & 0x00FFFFFF);
                String line = (showIconsSetting.getValue() ? (ev.getIcon() + " ") : "") + ev.getTitle();
                renderer.drawText(line, textX, drawY, evColor, true, scale, false);
                drawY += fontHeight + spacing;
            }
        }
    }

    // Getters for settings to facilitate testing
    public SettingGroup getGroupGeneral() { return groupGeneral; }
    public BooleanSetting getAutoDetectBedwarsSetting() { return autoDetectBedwarsSetting; }
    public BooleanSetting getAutoEnableOverlaySetting() { return autoEnableOverlaySetting; }
    public BooleanSetting getRequireHighConfidenceSetting() { return requireHighConfidenceSetting; }
    public BooleanSetting getManualModeSetting() { return manualModeSetting; }
    public SettingGroup getGroupGameInfo() { return groupGameInfo; }
    public SettingGroup getGroupTeamBoard() { return groupTeamBoard; }
    public SettingGroup getGroupMyTeam() { return groupMyTeam; }
    public SettingGroup getGroupSessionStats() { return groupSessionStats; }
    public SettingGroup getGroupEventFeed() { return groupEventFeed; }
    public SettingGroup getGroupTimers() { return groupTimers; }
    public SettingGroup getGroupParser() { return groupParser; }
    public SettingGroup getGroupAppearance() { return groupAppearance; }
    public EnumSetting<BedwarsLayout> getLayoutStyleSetting() { return layoutStyleSetting; }
    public EnumSetting<TeamBoardLayout> getTeamLayoutSetting() { return teamLayoutSetting; }
}
