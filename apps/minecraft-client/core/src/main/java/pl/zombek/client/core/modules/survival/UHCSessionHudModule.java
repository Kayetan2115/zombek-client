package pl.zombek.client.core.modules.survival;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;
import pl.zombek.client.core.uhc.*;

import java.util.ArrayList;
import java.util.List;

/**
 * UHC Session HUD Module.
 * Preserves the full real-time HUD displaying player health/hunger,
 * tracked UHC inventory counts, monotonic match timers, and scoreboard match status.
 */
public class UHCSessionHudModule extends AbstractModule implements IHudElement {
    private static UHCSessionHudModule instance;

    // General
    private final SettingGroup groupGeneral;
    private final EnumSetting<UhcMode> modeSetting;
    private final BooleanSetting autoStartSessionSetting;
    private final BooleanSetting resetOnWorldChangeSetting;

    // Player Section
    private final SettingGroup groupPlayer;
    private final BooleanSetting showPlayerSectionSetting;
    private final BooleanSetting showHealthSetting;
    private final BooleanSetting showAbsorptionSetting;
    private final BooleanSetting showHungerSetting;
    private final BooleanSetting showSaturationSetting;
    private final BooleanSetting showSessionTimeSetting;

    // Items Section
    private final SettingGroup groupItems;
    private final BooleanSetting showItemsSectionSetting;
    private final BooleanSetting showGoldenApplesSetting;
    private final BooleanSetting showGoldenHeadsSetting;
    private final BooleanSetting showArrowsSetting;
    private final BooleanSetting showEnderPearlsSetting;
    private final BooleanSetting showHealingItemsSetting;
    private final BooleanSetting showBlocksSetting;
    private final BooleanSetting showFlintAndSteelSetting;
    private final BooleanSetting showBucketsSetting;

    // Timers Section
    private final SettingGroup groupTimers;
    private final BooleanSetting showTimersSectionSetting;
    private final BooleanSetting showGameTimerSetting;
    private final BooleanSetting showPvpTimerSetting;
    private final BooleanSetting showBorderTimerSetting;
    private final BooleanSetting showMeetupTimerSetting;
    private final BooleanSetting showDeathmatchTimerSetting;
    private final BooleanSetting showCustomTimerSetting;

    // Data Sources & Match Section
    private final SettingGroup groupDataSources;
    private final BooleanSetting showMatchSectionSetting;
    private final BooleanSetting readScoreboardSetting;
    private final BooleanSetting enableScoreboardParserSetting;
    private final BooleanSetting enableChatParserSetting;
    private final BooleanSetting showAlivePlayersSetting;
    private final BooleanSetting showTeamsSetting;
    private final BooleanSetting showBorderStatusSetting;

    // Behavior
    private final SettingGroup groupBehavior;
    private final BooleanSetting resetOnDeathSetting;
    private final BooleanSetting resetSessionSetting;

    // Layout
    private final SettingGroup groupLayout;
    private final EnumSetting<UhcLayoutStyle> layoutStyleSetting;
    private final EnumSetting<HudAnchor> anchorSetting;
    private final NumberSetting offsetXSetting;
    private final NumberSetting offsetYSetting;
    private final NumberSetting paddingSetting;

    // Colors & Appearance
    private final SettingGroup groupColors;
    private final ColorSetting titleColorSetting;
    private final ColorSetting textColorSetting;
    private final ColorSetting valueColorSetting;
    private final ColorSetting timerColorSetting;
    private final ColorSetting warningColorSetting;
    private final ColorSetting backgroundColorSetting;
    private final NumberSetting opacitySetting;
    private final NumberSetting scaleSetting;

    public UHCSessionHudModule() {
        super(
                "uhc_session_hud",
                "UHC Session HUD",
                "Zaawansowany panel HUD dla trybu UHC: stan zdrowia, śledzenie koxów/pereł/strzał, timery gry i status meczu.",
                ModuleCategory.SURVIVAL
        );
        instance = this;

        // General
        this.groupGeneral = new SettingGroup("general", "General");
        this.modeSetting = new EnumSetting<>(
                "uhc_mode", "UHC Mode",
                "Tryb rozgrywki UHC (Solo, Teams, Custom).", UhcMode.SOLO
        );
        this.autoStartSessionSetting = new BooleanSetting(
                "auto_start_session", "Auto Start Session",
                "Automatycznie rozpoczyna sesję po wejściu do świata.", true
        );
        this.resetOnWorldChangeSetting = new BooleanSetting(
                "reset_on_world_change", "Reset on World Change",
                "Resetuje timery i stan sesji przy zmianie świata lub rozłączeniu.", true
        );
        this.groupGeneral.addSetting(modeSetting);
        this.groupGeneral.addSetting(autoStartSessionSetting);
        this.groupGeneral.addSetting(resetOnWorldChangeSetting);
        registerGroup(this.groupGeneral);

        // Player
        this.groupPlayer = new SettingGroup("player", "Player Status");
        this.showPlayerSectionSetting = new BooleanSetting(
                "show_player_section", "Show Player Section",
                "Włącza sekcję ze statystykami gracza.", true
        );
        this.showHealthSetting = new BooleanSetting(
                "show_health", "Show Health",
                "Pokazuje punkty życia gracza (HP).", true
        );
        this.showAbsorptionSetting = new BooleanSetting(
                "show_absorption", "Show Absorption",
                "Pokazuje punkty absorpcji (złote serca).", true
        );
        this.showHungerSetting = new BooleanSetting(
                "show_hunger", "Show Hunger",
                "Pokazuje poziom głodu gracza.", true
        );
        this.showSaturationSetting = new BooleanSetting(
                "show_saturation", "Show Saturation",
                "Pokazuje nasycenie głodu.", false
        );
        this.showSessionTimeSetting = new BooleanSetting(
                "show_session_time", "Show Session Time",
                "Pokazuje czas trwania bieżącej sesji.", true
        );
        this.groupPlayer.addSetting(showPlayerSectionSetting);
        this.groupPlayer.addSetting(showHealthSetting);
        this.groupPlayer.addSetting(showAbsorptionSetting);
        this.groupPlayer.addSetting(showHungerSetting);
        this.groupPlayer.addSetting(showSaturationSetting);
        this.groupPlayer.addSetting(showSessionTimeSetting);
        registerGroup(this.groupPlayer);

        // Items
        this.groupItems = new SettingGroup("items", "Tracked Items");
        this.showItemsSectionSetting = new BooleanSetting(
                "show_items_section", "Show Items Section",
                "Włącza licznik kluczowych przedmiotów UHC.", true
        );
        this.showGoldenApplesSetting = new BooleanSetting(
                "show_golden_apples", "Show Golden Apples",
                "Zlicza złote jabłka (GApple).", true
        );
        this.showGoldenHeadsSetting = new BooleanSetting(
                "show_golden_heads", "Show Golden Heads",
                "Zlicza złote głowy (Golden Head).", true
        );
        this.showArrowsSetting = new BooleanSetting(
                "show_arrows", "Show Arrows",
                "Zlicza strzały w ekwipunku.", true
        );
        this.showEnderPearlsSetting = new BooleanSetting(
                "show_ender_pearls", "Show Ender Pearls",
                "Zlicza perły kresu.", true
        );
        this.showHealingItemsSetting = new BooleanSetting(
                "show_healing_items", "Show Healing Items",
                "Zlicza mikstury lecznicze i regeneracji.", true
        );
        this.showBlocksSetting = new BooleanSetting(
                "show_blocks", "Show Blocks",
                "Zlicza klocki budowlane.", false
        );
        this.showFlintAndSteelSetting = new BooleanSetting(
                "show_flint_and_steel", "Show Flint & Steel",
                "Pokazuje trwałość zapalniczki.", false
        );
        this.showBucketsSetting = new BooleanSetting(
                "show_buckets", "Show Buckets",
                "Zlicza wiadra wody i lawy.", false
        );
        this.groupItems.addSetting(showItemsSectionSetting);
        this.groupItems.addSetting(showGoldenApplesSetting);
        this.groupItems.addSetting(showGoldenHeadsSetting);
        this.groupItems.addSetting(showArrowsSetting);
        this.groupItems.addSetting(showEnderPearlsSetting);
        this.groupItems.addSetting(showHealingItemsSetting);
        this.groupItems.addSetting(showBlocksSetting);
        this.groupItems.addSetting(showFlintAndSteelSetting);
        this.groupItems.addSetting(showBucketsSetting);
        registerGroup(this.groupItems);

        // Timers
        this.groupTimers = new SettingGroup("timers", "Match Timers");
        this.showTimersSectionSetting = new BooleanSetting(
                "show_timers_section", "Show Timers Section",
                "Włącza sekcję timerów rozgrywki.", true
        );
        this.showGameTimerSetting = new BooleanSetting(
                "show_game_timer", "Game Elapsed Timer",
                "Stoper czasu trwania gry.", true
        );
        this.showPvpTimerSetting = new BooleanSetting(
                "show_pvp_timer", "PvP Countdown",
                "Odliczanie do aktywacji PvP.", true
        );
        this.showBorderTimerSetting = new BooleanSetting(
                "show_border_timer", "Border Shrink Countdown",
                "Odliczanie do kurczenia się borderu.", true
        );
        this.showMeetupTimerSetting = new BooleanSetting(
                "show_meetup_timer", "Meetup Countdown",
                "Odliczanie do fazy Meetup.", true
        );
        this.showDeathmatchTimerSetting = new BooleanSetting(
                "show_deathmatch_timer", "Deathmatch Countdown",
                "Odliczanie do Deathmatchu.", false
        );
        this.showCustomTimerSetting = new BooleanSetting(
                "show_custom_timer", "Custom Timer",
                "Własny konfigurowalny timer.", false
        );
        this.groupTimers.addSetting(showTimersSectionSetting);
        this.groupTimers.addSetting(showGameTimerSetting);
        this.groupTimers.addSetting(showPvpTimerSetting);
        this.groupTimers.addSetting(showBorderTimerSetting);
        this.groupTimers.addSetting(showMeetupTimerSetting);
        this.groupTimers.addSetting(showDeathmatchTimerSetting);
        this.groupTimers.addSetting(showCustomTimerSetting);
        registerGroup(this.groupTimers);

        // Data Sources & Match
        this.groupDataSources = new SettingGroup("match", "Match Info & Scoreboard");
        this.showMatchSectionSetting = new BooleanSetting(
                "show_match_section", "Show Match Section",
                "Włącza podsumowanie statusu meczu.", true
        );
        this.readScoreboardSetting = new BooleanSetting(
                "read_scoreboard", "Read Scoreboard",
                "Odczytuje linie scoreboardu bocznego.", true
        );
        this.enableScoreboardParserSetting = new BooleanSetting(
                "enable_scoreboard_parser", "Enable Scoreboard Parser",
                "Automatycznie parsuje liczbę graczy/drużyn i border ze scoreboardu.", true
        );
        this.enableChatParserSetting = new BooleanSetting(
                "enable_chat_parser", "Enable Chat Parser",
                "Automatycznie synchronizuje timery po wykryciu komunikatów na czacie.", true
        );
        this.showAlivePlayersSetting = new BooleanSetting(
                "show_alive_players", "Show Alive Players",
                "Wyświetla liczbę żywych graczy.", true
        );
        this.showTeamsSetting = new BooleanSetting(
                "show_teams", "Show Teams Count",
                "Wyświetla liczbę pozostałych drużyn.", true
        );
        this.showBorderStatusSetting = new BooleanSetting(
                "show_border_status", "Show Border Status",
                "Wyświetla aktualny rozmiar granicy świata.", true
        );
        this.groupDataSources.addSetting(showMatchSectionSetting);
        this.groupDataSources.addSetting(readScoreboardSetting);
        this.groupDataSources.addSetting(enableScoreboardParserSetting);
        this.groupDataSources.addSetting(enableChatParserSetting);
        this.groupDataSources.addSetting(showAlivePlayersSetting);
        this.groupDataSources.addSetting(showTeamsSetting);
        this.groupDataSources.addSetting(showBorderStatusSetting);
        registerGroup(this.groupDataSources);

        // Behavior
        this.groupBehavior = new SettingGroup("behavior", "Behavior & Reset");
        this.resetOnDeathSetting = new BooleanSetting(
                "reset_on_death", "Reset on Death",
                "Automatycznie kończy sesję i zatrzymuje timery po śmierci gracza.", true
        );
        this.resetSessionSetting = new BooleanSetting(
                "reset_session_now", "Reset Session Now",
                "Kliknij, aby zresetować obecną sesję i timery UHC.", false
        );
        this.groupBehavior.addSetting(resetOnDeathSetting);
        this.groupBehavior.addSetting(resetSessionSetting);
        registerGroup(this.groupBehavior);

        // Layout
        this.groupLayout = new SettingGroup("layout", "Layout & Position");
        this.layoutStyleSetting = new EnumSetting<>(
                "layout_style", "Layout Style",
                "Styl prezentacji panelu UHC.", UhcLayoutStyle.COMPACT
        );
        this.anchorSetting = new EnumSetting<>(
                "anchor", "HUD Anchor",
                "Punkt zakotwiczenia panelu na ekranie.", HudAnchor.TOP_LEFT
        );
        this.offsetXSetting = new NumberSetting(
                "offset_x", "Offset X",
                "Przesunięcie poziome panelu (px).", 10.0, -1000.0, 1000.0, 1.0
        );
        this.offsetYSetting = new NumberSetting(
                "offset_y", "Offset Y",
                "Przesunięcie pionowe panelu (px).", 10.0, -1000.0, 1000.0, 1.0
        );
        this.paddingSetting = new NumberSetting(
                "padding", "Padding",
                "Wewnętrzny odstęp elementów (px).", 4.0, 0.0, 20.0, 1.0
        );
        this.groupLayout.addSetting(layoutStyleSetting);
        this.groupLayout.addSetting(anchorSetting);
        this.groupLayout.addSetting(offsetXSetting);
        this.groupLayout.addSetting(offsetYSetting);
        this.groupLayout.addSetting(paddingSetting);
        registerGroup(this.groupLayout);

        // Colors
        this.groupColors = new SettingGroup("colors", "Colors & Appearance");
        this.titleColorSetting = new ColorSetting(
                "title_color", "Title Color",
                "Kolor nagłówka UHC Overlay.", 0xFFFFAA00
        );
        this.textColorSetting = new ColorSetting(
                "text_color", "Text Label Color",
                "Kolor etykiet pól.", 0xFFAAAAAA
        );
        this.valueColorSetting = new ColorSetting(
                "value_color", "Value Color",
                "Kolor wartości liczbowych.", 0xFFFFFFFF
        );
        this.timerColorSetting = new ColorSetting(
                "timer_color", "Timer Color",
                "Kolor aktywnych timerów.", 0xFF55FF55
        );
        this.warningColorSetting = new ColorSetting(
                "warning_color", "Warning Color",
                "Kolor ostrzeżeń i kończących się timerów.", 0xFFFF5555
        );
        this.backgroundColorSetting = new ColorSetting(
                "background_color", "Background Color",
                "Kolor tła panelu HUD.", 0x000000
        );
        this.opacitySetting = new NumberSetting(
                "background_opacity", "Background Opacity",
                "Przezroczystość tła panelu (0-100%).", 60.0, 0.0, 100.0, 1.0
        );
        this.scaleSetting = new NumberSetting(
                "scale", "Scale",
                "Skala rozmiaru tekstu i elementów.", 1.0, 0.5, 2.0, 0.05
        );
        this.groupColors.addSetting(titleColorSetting);
        this.groupColors.addSetting(textColorSetting);
        this.groupColors.addSetting(valueColorSetting);
        this.groupColors.addSetting(timerColorSetting);
        this.groupColors.addSetting(warningColorSetting);
        this.groupColors.addSetting(backgroundColorSetting);
        this.groupColors.addSetting(opacitySetting);
        this.groupColors.addSetting(scaleSetting);
        registerGroup(this.groupColors);
    }

    public static UHCSessionHudModule getInstance() {
        return instance;
    }

    @Override
    public String getElementId() {
        return getId();
    }

    @Override
    public void onInitialize() {
        HudElementRegistry.getInstance().register(this);
    }

    @Override
    public void onEnable() {
        if (autoStartSessionSetting.getValue()) {
            UhcSessionTracker.getInstance().startSession(modeSetting.getValue());
        }
    }

    @Override
    public void onDisable() {
        // Safe flush
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;

        if (resetSessionSetting.getValue()) {
            UhcSessionTracker.getInstance().reset();
            UhcSessionTracker.getInstance().startSession(modeSetting.getValue());
            resetSessionSetting.setValue(false);
        }

        IUhcDataProvider provider = UhcDataProviderRegistry.getProvider();
        UhcSessionTracker session = UhcSessionTracker.getInstance();

        if (provider != null) {
            session.updatePlayerStatus(
                    provider.getPlayerHealth(),
                    provider.getPlayerAbsorption(),
                    provider.getPlayerFoodLevel(),
                    provider.getPlayerSaturation()
            );

            session.setTrackedItemCount(UhcTrackedItem.GOLDEN_APPLE, provider.getItemCount(UhcTrackedItem.GOLDEN_APPLE));
            session.setTrackedItemCount(UhcTrackedItem.GOLDEN_HEAD, provider.getItemCount(UhcTrackedItem.GOLDEN_HEAD));
            session.setTrackedItemCount(UhcTrackedItem.ARROW, provider.getItemCount(UhcTrackedItem.ARROW));
            session.setTrackedItemCount(UhcTrackedItem.ENDER_PEARL, provider.getItemCount(UhcTrackedItem.ENDER_PEARL));
            session.setTrackedItemCount(UhcTrackedItem.HEALING, provider.getItemCount(UhcTrackedItem.HEALING));
            session.setTrackedItemCount(UhcTrackedItem.BLOCKS, provider.getItemCount(UhcTrackedItem.BLOCKS));
            session.setTrackedItemCount(UhcTrackedItem.FLINT_AND_STEEL, provider.getItemCount(UhcTrackedItem.FLINT_AND_STEEL));
            session.setTrackedItemCount(UhcTrackedItem.WATER_BUCKET, provider.getItemCount(UhcTrackedItem.WATER_BUCKET));
            session.setTrackedItemCount(UhcTrackedItem.LAVA_BUCKET, provider.getItemCount(UhcTrackedItem.LAVA_BUCKET));
        }

        session.tick();
    }

    @Override
    public boolean isVisible() {
        return isEnabled() && !hasError();
    }

    @Override
    public void render(IHudRenderer renderer) {
        if (!isVisible() || renderer == null) return;

        UhcSessionTracker session = UhcSessionTracker.getInstance();
        UhcScoreboardParser scoreboard = session.getScoreboardParser();

        List<RenderLine> lines = buildLines(session, scoreboard);
        if (lines.isEmpty()) return;

        int screenWidth = renderer.getScreenWidth();
        int screenHeight = renderer.getScreenHeight();
        float scale = scaleSetting.getValue().floatValue();
        int padding = paddingSetting.getValue().intValue();
        int fontHeight = renderer.getFontHeight();

        int maxTextWidth = 0;
        for (RenderLine line : lines) {
            int w = renderer.getTextWidth(line.label + line.value);
            if (w > maxTextWidth) maxTextWidth = w;
        }

        int boxWidth = (int) ((maxTextWidth + padding * 2) * scale);
        int lineHeight = (int) ((fontHeight + 2) * scale);
        int boxHeight = (lines.size() * lineHeight) + (int) (padding * 2 * scale);

        int posX = anchorSetting.getValue().calculateX(offsetXSetting.getValue().intValue(), boxWidth, screenWidth);
        int posY = anchorSetting.getValue().calculateY(offsetYSetting.getValue().intValue(), boxHeight, screenHeight);

        int baseBgColor = backgroundColorSetting.getValue() & 0x00FFFFFF;
        int alpha = (int) (opacitySetting.getValue() * 255.0 / 100.0) & 0xFF;
        int fullBgColor = (alpha << 24) | baseBgColor;

        if (alpha > 0) {
            renderer.fill(posX, posY, boxWidth, boxHeight, fullBgColor);
            renderer.drawBorder(posX, posY, boxWidth, boxHeight, (0x44 << 24) | baseBgColor, 1);
        }

        int currY = posY + (int) (padding * scale);
        int textX = posX + (int) (padding * scale);

        for (RenderLine line : lines) {
            if (line.isHeader) {
                renderer.drawText(line.label, textX, currY, line.labelColor, true, scale, true);
            } else {
                renderer.drawText(line.label, textX, currY, line.labelColor, true, scale, false);
                int labelW = (int) (renderer.getTextWidth(line.label) * scale);
                renderer.drawText(line.value, textX + labelW, currY, line.valueColor, true, scale, true);
            }
            currY += lineHeight;
        }
    }

    private List<RenderLine> buildLines(UhcSessionTracker session, UhcScoreboardParser scoreboard) {
        List<RenderLine> lines = new ArrayList<>();
        int titleColor = titleColorSetting.getValue();
        int labelColor = textColorSetting.getValue();
        int valueColor = valueColorSetting.getValue();
        int timerColor = timerColorSetting.getValue();
        int warnColor = warningColorSetting.getValue();

        lines.add(new RenderLine("UHC SESSION", "", titleColor, titleColor, true));

        // 1. PLAYER STATUS
        if (showPlayerSectionSetting.getValue()) {
            if (showHealthSetting.getValue()) {
                float hp = session.getPlayerHealth();
                String hpStr = String.format("%.1f/20 HP", hp);
                int hpColor = hp > 10.0f ? 0xFF55FF55 : (hp > 5.0f ? 0xFFFFFF55 : 0xFFFF5555);
                lines.add(new RenderLine("Health: ", hpStr, labelColor, hpColor, false));
            }
            if (showAbsorptionSetting.getValue() && session.getPlayerAbsorption() > 0.0f) {
                lines.add(new RenderLine("Abs: ", String.format("+%.1f", session.getPlayerAbsorption()), labelColor, 0xFFFFAA00, false));
            }
            if (showHungerSetting.getValue()) {
                lines.add(new RenderLine("Hunger: ", session.getPlayerHunger() + "/20", labelColor, valueColor, false));
            }
            if (showSessionTimeSetting.getValue()) {
                lines.add(new RenderLine("Session: ", session.getFormattedSessionTime(), labelColor, timerColor, false));
            }
        }

        // 2. ITEMS SECTION
        if (showItemsSectionSetting.getValue()) {
            lines.add(new RenderLine("--- ITEMS ---", "", labelColor, labelColor, true));
            if (showGoldenApplesSetting.getValue()) {
                lines.add(new RenderLine("GApples: ", String.valueOf(session.getTrackedItemCount(UhcTrackedItem.GOLDEN_APPLE)), labelColor, 0xFFFFFF55, false));
            }
            if (showGoldenHeadsSetting.getValue()) {
                lines.add(new RenderLine("GHeads: ", String.valueOf(session.getTrackedItemCount(UhcTrackedItem.GOLDEN_HEAD)), labelColor, 0xFFFFAA00, false));
            }
            if (showEnderPearlsSetting.getValue()) {
                lines.add(new RenderLine("Pearls: ", String.valueOf(session.getTrackedItemCount(UhcTrackedItem.ENDER_PEARL)), labelColor, 0xFF55FFFF, false));
            }
            if (showArrowsSetting.getValue()) {
                lines.add(new RenderLine("Arrows: ", String.valueOf(session.getTrackedItemCount(UhcTrackedItem.ARROW)), labelColor, valueColor, false));
            }
            if (showHealingItemsSetting.getValue()) {
                lines.add(new RenderLine("Healing: ", String.valueOf(session.getTrackedItemCount(UhcTrackedItem.HEALING)), labelColor, 0xFFFF55FF, false));
            }
            if (showBlocksSetting.getValue()) {
                lines.add(new RenderLine("Blocks: ", String.valueOf(session.getTrackedItemCount(UhcTrackedItem.BLOCKS)), labelColor, valueColor, false));
            }
            if (showFlintAndSteelSetting.getValue()) {
                lines.add(new RenderLine("F&S: ", String.valueOf(session.getTrackedItemCount(UhcTrackedItem.FLINT_AND_STEEL)), labelColor, 0xFFFF5555, false));
            }
            if (showBucketsSetting.getValue()) {
                int buckets = session.getTrackedItemCount(UhcTrackedItem.WATER_BUCKET) + session.getTrackedItemCount(UhcTrackedItem.LAVA_BUCKET);
                lines.add(new RenderLine("Buckets: ", String.valueOf(buckets), labelColor, 0xFF55FFFF, false));
            }
        }

        // 3. TIMERS SECTION
        if (showTimersSectionSetting.getValue()) {
            lines.add(new RenderLine("--- TIMERS ---", "", labelColor, labelColor, true));
            if (showGameTimerSetting.getValue()) {
                UhcTimer t = session.getTimer(UhcTimerType.GAME);
                lines.add(new RenderLine("Game: ", t.getFormattedTime(), labelColor, timerColor, false));
            }
            if (showPvpTimerSetting.getValue()) {
                UhcTimer t = session.getTimer(UhcTimerType.PVP);
                lines.add(new RenderLine("PvP: ", t.isExpired() ? "ACTIVE" : t.getFormattedTime(), labelColor, t.isExpired() ? warnColor : timerColor, false));
            }
            if (showBorderTimerSetting.getValue()) {
                UhcTimer t = session.getTimer(UhcTimerType.BORDER);
                lines.add(new RenderLine("Border: ", t.getFormattedTime(), labelColor, timerColor, false));
            }
            if (showMeetupTimerSetting.getValue()) {
                UhcTimer t = session.getTimer(UhcTimerType.MEETUP);
                lines.add(new RenderLine("Meetup: ", t.getFormattedTime(), labelColor, timerColor, false));
            }
            if (showDeathmatchTimerSetting.getValue()) {
                UhcTimer t = session.getTimer(UhcTimerType.DEATHMATCH);
                lines.add(new RenderLine("DM: ", t.getFormattedTime(), labelColor, timerColor, false));
            }
            if (showCustomTimerSetting.getValue()) {
                UhcTimer t = session.getTimer(UhcTimerType.CUSTOM);
                lines.add(new RenderLine(t.getName() + ": ", t.getFormattedTime(), labelColor, timerColor, false));
            }
        }

        // 4. MATCH SECTION
        if (showMatchSectionSetting.getValue()) {
            lines.add(new RenderLine("--- MATCH ---", "", labelColor, labelColor, true));
            lines.add(new RenderLine("Status: ", scoreboard.getMatchStatus(), labelColor, valueColor, false));
            if (showAlivePlayersSetting.getValue()) {
                lines.add(new RenderLine("Alive: ", scoreboard.getAlivePlayers(), labelColor, valueColor, false));
            }
            if (showTeamsSetting.getValue() && !scoreboard.getTeamCount().equals("N/A")) {
                lines.add(new RenderLine("Teams: ", scoreboard.getTeamCount(), labelColor, valueColor, false));
            }
            if (showBorderStatusSetting.getValue()) {
                lines.add(new RenderLine("Border: ", scoreboard.getBorderStatus(), labelColor, valueColor, false));
            }
        }

        return lines;
    }

    private static class RenderLine {
        final String label;
        final String value;
        final int labelColor;
        final int valueColor;
        final boolean isHeader;

        RenderLine(String label, String value, int labelColor, int valueColor, boolean isHeader) {
            this.label = label;
            this.value = value;
            this.labelColor = labelColor;
            this.valueColor = valueColor;
            this.isHeader = isHeader;
        }
    }

    // Getters for settings
    public EnumSetting<UhcMode> getModeSetting() { return modeSetting; }
    public BooleanSetting getAutoStartSessionSetting() { return autoStartSessionSetting; }
    public BooleanSetting getResetOnWorldChangeSetting() { return resetOnWorldChangeSetting; }
    public BooleanSetting getResetOnDeathSetting() { return resetOnDeathSetting; }
    public EnumSetting<UhcLayoutStyle> getLayoutStyleSetting() { return layoutStyleSetting; }
}
