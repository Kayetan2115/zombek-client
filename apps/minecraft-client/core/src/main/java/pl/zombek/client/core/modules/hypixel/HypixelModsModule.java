package pl.zombek.client.core.modules.hypixel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hypixel.*;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

import java.util.List;

/**
 * Hypixel Mods module offering optional Fair Play HUD enhancements, chat filters,
 * short channel aliases, command autocomplete, and manual social suggestions.
 */
public class HypixelModsModule extends AbstractModule implements IHudElement {
    private static final Logger LOGGER = LoggerFactory.getLogger(HypixelModsModule.class);
    private static HypixelModsModule instance;

    // 1. Server Detection
    private final SettingGroup groupServerDetection;
    private final BooleanSetting autoDetectSetting;
    private final BooleanSetting customProfileOverrideSetting;
    private final BooleanSetting showServerStatusSetting;

    // 2. Safety Mode
    private final SettingGroup groupSafetyMode;
    private final EnumSetting<SafetyPreset> safetyPresetSetting;
    private final StringSetting unofficialDisclaimerSetting;

    // 3. Chat Customization
    private final SettingGroup groupChatCustomization;
    private final BooleanSetting enableShortChannelsSetting;
    private final EnumSetting<ChannelPrefixStyle> prefixStyleSetting;
    private final BooleanSetting hideGuildMotdSetting;
    private final BooleanSetting hideGuildOnTabSetting;
    private final BooleanSetting showTimestampsSetting;
    private final BooleanSetting preserveColorsSetting;

    // 4. Chat Filters
    private final SettingGroup groupChatFilters;
    private final BooleanSetting enableChatFiltersSetting;
    private final BooleanSetting filterPrivateSetting;
    private final BooleanSetting filterTeamSetting;
    private final BooleanSetting filterPartySetting;
    private final BooleanSetting filterGuildSetting;
    private final BooleanSetting filterLobbySetting;
    private final BooleanSetting filterMysteryBoxSetting;
    private final BooleanSetting filterSoulWellSetting;
    private final BooleanSetting filterJoinLeaveSetting;

    // 5. Tablist Customization
    private final SettingGroup groupTablist;
    private final BooleanSetting tablistHideGuildTagSetting;
    private final BooleanSetting compactTablistSpacingSetting;

    // 6. Command Autocomplete
    private final SettingGroup groupAutocomplete;
    private final BooleanSetting enableAutocompleteSetting;
    private final NumberSetting suggestionCountSetting;
    private final BooleanSetting showDescriptionsSetting;

    // 7. Level Display
    private final SettingGroup groupLevelDisplay;
    private final BooleanSetting enableLevelDisplaySetting;
    private final BooleanSetting displayInTabSetting;
    private final BooleanSetting displayAboveHeadSetting;

    // 8. Manual Suggestions
    private final SettingGroup groupManualSuggestions;
    private final BooleanSetting enableManualSuggestionsSetting;
    private final BooleanSetting suggestGgSetting;
    private final BooleanSetting suggestGlSetting;
    private final BooleanSetting copyButtonSetting;

    // 9. HUD Notifications
    private final SettingGroup groupNotifications;
    private final BooleanSetting enableNotificationsSetting;
    private final BooleanSetting showDetectionSetting;
    private final BooleanSetting showWarningsSetting;
    private final NumberSetting notificationDurationSetting;
    private final BooleanSetting soundSetting;
    private final EnumSetting<HudAnchor> notificationAnchorSetting;

    public HypixelModsModule() {
        super(
                "hypixel_mods",
                "Hypixel Mods",
                "Pakiet opcjonalnych usprawnień interfejsu, czatu i powiadomień na Hypixel (100% Fair Play)",
                ModuleCategory.OTHER
        );
        instance = this;

        // 1. Server Detection
        this.groupServerDetection = new SettingGroup("server_detection", "Detekcja serwera");
        this.autoDetectSetting = new BooleanSetting("auto_detect", "Automatyczne wykrywanie Hypixel", "Wykrywa Hypixel na podstawie adresu serwera", true);
        this.customProfileOverrideSetting = new BooleanSetting("custom_profile", "Wymuś profil Hypixel", "Wymusza profil Hypixel przy korzystaniu z proxy", false);
        this.showServerStatusSetting = new BooleanSetting("show_status", "Pokaż status serwera", "Wyświetla stan połączenia w powiadomieniach", true);
        groupServerDetection.addSetting(autoDetectSetting);
        groupServerDetection.addSetting(customProfileOverrideSetting);
        groupServerDetection.addSetting(showServerStatusSetting);

        // 2. Safety Mode
        this.groupSafetyMode = new SettingGroup("safety_mode", "Tryb bezpieczeństwa");
        this.safetyPresetSetting = new EnumSetting<>("safety_preset", "Profil bezpieczeństwa", "Poziom restrykcji funkcji zgodnie z zasadami Hypixel", SafetyPreset.STRICT_SAFETY);
        this.unofficialDisclaimerSetting = new StringSetting("disclaimer", "Zastrzeżenie", "Modyfikacja nieoficjalna", HypixelSafetyController.UNOFFICIAL_DISCLAIMER);
        groupSafetyMode.addSetting(safetyPresetSetting);
        groupSafetyMode.addSetting(unofficialDisclaimerSetting);

        // 3. Chat Customization
        this.groupChatCustomization = new SettingGroup("chat_customization", "Dostosowanie czatu");
        this.enableShortChannelsSetting = new BooleanSetting("short_channels", "Skrócone nazwy kanałów", "Zastępuje Party/Guild/Team literami P/G/T", true);
        this.prefixStyleSetting = new EnumSetting<>("prefix_style", "Styl prefiksu kanału", "Format prezentacji skróconej nazwy kanału", ChannelPrefixStyle.BRACKETED);
        this.hideGuildMotdSetting = new BooleanSetting("hide_guild_motd", "Ukryj Guild MOTD", "Ukrywa powiadomienia powitalne gildii na czacie", false);
        this.hideGuildOnTabSetting = new BooleanSetting("hide_guild_tab", "Ukryj tag gildii na tabliście", "Usuwa [TAG] z listy graczy pod TAB", false);
        this.showTimestampsSetting = new BooleanSetting("show_timestamps", "Znaczniki czasu", "Dodaje [HH:mm] przed wiadomościami na czacie", false);
        this.preserveColorsSetting = new BooleanSetting("preserve_colors", "Zachowaj kolory", "Utrzymuje oryginalne barwy i formatowanie tekstu", true);
        groupChatCustomization.addSetting(enableShortChannelsSetting);
        groupChatCustomization.addSetting(prefixStyleSetting);
        groupChatCustomization.addSetting(hideGuildMotdSetting);
        groupChatCustomization.addSetting(hideGuildOnTabSetting);
        groupChatCustomization.addSetting(showTimestampsSetting);
        groupChatCustomization.addSetting(preserveColorsSetting);

        // 4. Chat Filters
        this.groupChatFilters = new SettingGroup("chat_filters", "Filtry wiadomości");
        this.enableChatFiltersSetting = new BooleanSetting("enable_filters", "Włącz filtry czatu", "Aktywuje lokalne ukrywanie wybranych wiadomości", true);
        this.filterPrivateSetting = new BooleanSetting("filter_private", "Ukryj wiadomości prywatne", "Ukrywa wiadomości /msg i /tell", false);
        this.filterTeamSetting = new BooleanSetting("filter_team", "Ukryj czat drużynowy", "Ukrywa wiadomości z kanału Team", false);
        this.filterPartySetting = new BooleanSetting("filter_party", "Ukryj czat Party", "Ukrywa wiadomości z kanału Party", false);
        this.filterGuildSetting = new BooleanSetting("filter_guild", "Ukryj czat gildii", "Ukrywa wiadomości z kanału Guild", false);
        this.filterLobbySetting = new BooleanSetting("filter_lobby", "Ukryj spam lobby", "Ukrywa komunikaty przełączania krain i restartów", true);
        this.filterMysteryBoxSetting = new BooleanSetting("filter_mystery_box", "Ukryj Mystery Box", "Ukrywa publiczne ogłoszenia otwarcia skrzyń", true);
        this.filterSoulWellSetting = new BooleanSetting("filter_soul_well", "Ukryj Soul Well", "Ukrywa publiczne ogłoszenia losowań w studni dusz", true);
        this.filterJoinLeaveSetting = new BooleanSetting("filter_join_leave", "Ukryj dołączenia/wyjścia", "Ukrywa komunikaty dołączania do poczekalni", false);
        groupChatFilters.addSetting(enableChatFiltersSetting);
        groupChatFilters.addSetting(filterPrivateSetting);
        groupChatFilters.addSetting(filterTeamSetting);
        groupChatFilters.addSetting(filterPartySetting);
        groupChatFilters.addSetting(filterGuildSetting);
        groupChatFilters.addSetting(filterLobbySetting);
        groupChatFilters.addSetting(filterMysteryBoxSetting);
        groupChatFilters.addSetting(filterSoulWellSetting);
        groupChatFilters.addSetting(filterJoinLeaveSetting);

        // 5. Tablist Customization
        this.groupTablist = new SettingGroup("tablist", "Tablista");
        this.tablistHideGuildTagSetting = new BooleanSetting("tab_hide_guild", "Ukryj tagi gildii", "Ukrywa oznaczenia gildii na liście graczy", false);
        this.compactTablistSpacingSetting = new BooleanSetting("compact_tab", "Kompaktowa tablista", "Zmniejsza odstępy wierszy na liście graczy", false);
        groupTablist.addSetting(tablistHideGuildTagSetting);
        groupTablist.addSetting(compactTablistSpacingSetting);

        // 6. Command Autocomplete
        this.groupAutocomplete = new SettingGroup("autocomplete", "Podpowiedzi komend");
        this.enableAutocompleteSetting = new BooleanSetting("enable_autocomplete", "Lokalne autocomplete", "Lokalne podpowiedzi znanych komend Hypixel", true);
        this.suggestionCountSetting = new NumberSetting("suggestion_count", "Liczba sugestii", "Maksymalna liczba wyświetlanych podpowiedzi", 5.0, 1.0, 10.0, 1.0);
        this.showDescriptionsSetting = new BooleanSetting("show_descriptions", "Pokazuj opisy komend", "Wyświetla objaśnienie działania sugerowanej komendy", true);
        groupAutocomplete.addSetting(enableAutocompleteSetting);
        groupAutocomplete.addSetting(suggestionCountSetting);
        groupAutocomplete.addSetting(showDescriptionsSetting);

        // 7. Level Display
        this.groupLevelDisplay = new SettingGroup("level_display", "Wyświetlanie poziomu");
        this.enableLevelDisplaySetting = new BooleanSetting("enable_level_display", "Włącz poziom gracza", "Wyświetla poziom z widocznych danych (Zablokowane w Strict Safety)", false);
        this.displayInTabSetting = new BooleanSetting("level_in_tab", "Poziom na tabliście", "Pokazuje poziom gracza na liście TAB", true);
        this.displayAboveHeadSetting = new BooleanSetting("level_above_head", "Poziom nad modelem", "Pokazuje poziom gracza nad jego nickiem", false);
        groupLevelDisplay.addSetting(enableLevelDisplaySetting);
        groupLevelDisplay.addSetting(displayInTabSetting);
        groupLevelDisplay.addSetting(displayAboveHeadSetting);

        // 8. Manual Suggestions
        this.groupManualSuggestions = new SettingGroup("manual_suggestions", "Ręczne sugestie (GG/GL)");
        this.enableManualSuggestionsSetting = new BooleanSetting("enable_suggestions", "Włącz ręczne sugestie", "Dyskretna podpowiedź wiadomości po zakończeniu gry", true);
        this.suggestGgSetting = new BooleanSetting("suggest_gg", "Sugeruj GG po meczu", "Wyświetla kartę z propozycją wysłania 'GG'", true);
        this.suggestGlSetting = new BooleanSetting("suggest_gl", "Sugeruj GL na starcie", "Wyświetla propozycję 'GL HF' przy rozpoczęciu gry", true);
        this.copyButtonSetting = new BooleanSetting("copy_button", "Przycisk kopiowania", "Pozwala skopiować tekst do schowka 1 kliknięciem", true);
        groupManualSuggestions.addSetting(enableManualSuggestionsSetting);
        groupManualSuggestions.addSetting(suggestGgSetting);
        groupManualSuggestions.addSetting(suggestGlSetting);
        groupManualSuggestions.addSetting(copyButtonSetting);

        // 9. HUD Notifications
        this.groupNotifications = new SettingGroup("notifications", "Powiadomienia HUD");
        this.enableNotificationsSetting = new BooleanSetting("enable_notifications", "Powiadomienia HUD", "Wyświetla lokalne toasty o statusie serwera i zdarzeniach", true);
        this.showDetectionSetting = new BooleanSetting("notify_detection", "Powiadomienie o serwerze", "Informuje o wykryciu sieci Hypixel", true);
        this.showWarningsSetting = new BooleanSetting("notify_warnings", "Ostrzeżenia bezpieczeństwa", "Informuje o zablokowaniu niedozwolonej funkcji", true);
        this.notificationDurationSetting = new NumberSetting("notification_duration", "Czas wyświetlania (s)", "Czas trwania powiadomienia na ekranie", 5.0, 1.0, 15.0, 0.5);
        this.soundSetting = new BooleanSetting("notification_sound", "Dźwięk powiadomienia", "Odtwarza cichy dźwięk toastu przy powiadomieniu", true);
        this.notificationAnchorSetting = new EnumSetting<>("notification_anchor", "Pozycja powiadomień", "Narożnik ekranu dla powiadomień", HudAnchor.TOP_RIGHT);
        groupNotifications.addSetting(enableNotificationsSetting);
        groupNotifications.addSetting(showDetectionSetting);
        groupNotifications.addSetting(showWarningsSetting);
        groupNotifications.addSetting(notificationDurationSetting);
        groupNotifications.addSetting(soundSetting);
        groupNotifications.addSetting(notificationAnchorSetting);

        // Register all 9 groups
        registerGroup(groupServerDetection);
        registerGroup(groupSafetyMode);
        registerGroup(groupChatCustomization);
        registerGroup(groupChatFilters);
        registerGroup(groupTablist);
        registerGroup(groupAutocomplete);
        registerGroup(groupLevelDisplay);
        registerGroup(groupManualSuggestions);
        registerGroup(groupNotifications);
    }

    public static synchronized HypixelModsModule getInstance() {
        if (instance == null) {
            instance = new HypixelModsModule();
        }
        return instance;
    }

    @Override
    public void onEnable() {
        LOGGER.info("[HypixelModsModule] Enabled Hypixel Mods.");
        applySettingsToManagers();
        pl.zombek.client.core.hud.HudElementRegistry.getInstance().register(this);
    }

    @Override
    public void onDisable() {
        LOGGER.info("[HypixelModsModule] Disabled Hypixel Mods.");
        pl.zombek.client.core.hud.HudElementRegistry.getInstance().unregister(this);
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        applySettingsToManagers();
        HypixelManager.getInstance().onTick();
    }

    public void applySettingsToManagers() {
        HypixelManager mgr = HypixelManager.getInstance();

        // 1. Server Detection
        mgr.getServerDetector().setCustomProfileActive(customProfileOverrideSetting.getValue());

        // 2. Safety Mode
        mgr.getSafetyController().setPreset(safetyPresetSetting.getValue());

        // 3. Chat Customization
        mgr.getChatController().setEnableShortChannelNames(enableShortChannelsSetting.getValue());
        mgr.getChatController().setPrefixStyle(prefixStyleSetting.getValue());
        mgr.getChatController().setHideGuildMotd(hideGuildMotdSetting.getValue());
        mgr.getChatController().setHideGuildOnTab(hideGuildOnTabSetting.getValue());
        mgr.getChatController().setShowTimestamps(showTimestampsSetting.getValue());
        mgr.getChatController().setPreserveOriginalFormatting(preserveColorsSetting.getValue());

        // 4. Chat Filters
        mgr.getChatFilter().setEnabled(enableChatFiltersSetting.getValue());
        mgr.getChatFilter().setFilterPrivateMessages(filterPrivateSetting.getValue());
        mgr.getChatFilter().setFilterTeamChat(filterTeamSetting.getValue());
        mgr.getChatFilter().setFilterPartyChat(filterPartySetting.getValue());
        mgr.getChatFilter().setFilterGuildChat(filterGuildSetting.getValue());
        mgr.getChatFilter().setFilterLobbyStatusMessages(filterLobbySetting.getValue());
        mgr.getChatFilter().setFilterMysteryBoxAnnouncements(filterMysteryBoxSetting.getValue());
        mgr.getChatFilter().setFilterSoulWellAnnouncements(filterSoulWellSetting.getValue());
        mgr.getChatFilter().setFilterJoinMessages(filterJoinLeaveSetting.getValue());
        mgr.getChatFilter().setFilterLeaveMessages(filterJoinLeaveSetting.getValue());

        // 6. Autocomplete
        mgr.getAutocomplete().setEnabled(enableAutocompleteSetting.getValue());
        mgr.getAutocomplete().setSuggestionCount(suggestionCountSetting.getIntValue());
        mgr.getAutocomplete().setShowDescription(showDescriptionsSetting.getValue());

        // 7. Level Display
        // Enforce safety rule: if connected to Hypixel and in Strict Safety, level display is locked
        if (enableLevelDisplaySetting.getValue() && !mgr.getLevelDisplay().isLevelDisplayPermitted()) {
            enableLevelDisplaySetting.setValue(false);
            mgr.getSafetyController().logDiagnostic("Level Display locked by Hypixel Strict Safety policy.");
        }
        mgr.getLevelDisplay().setEnabled(enableLevelDisplaySetting.getValue());
        mgr.getLevelDisplay().setDisplayInTab(displayInTabSetting.getValue());
        mgr.getLevelDisplay().setDisplayAbovePlayer(displayAboveHeadSetting.getValue());

        // 8. Manual Suggestions
        mgr.getManualSuggestions().setEnabled(enableManualSuggestionsSetting.getValue());
        mgr.getManualSuggestions().setSuggestGg(suggestGgSetting.getValue());
        mgr.getManualSuggestions().setSuggestGl(suggestGlSetting.getValue());
        mgr.getManualSuggestions().setCopyToClipboardButton(copyButtonSetting.getValue());

        // 9. Notifications
        mgr.getNotificationController().setEnabled(enableNotificationsSetting.getValue());
        mgr.getNotificationController().setShowServerDetection(showDetectionSetting.getValue());
        mgr.getNotificationController().setShowSafetyWarnings(showWarningsSetting.getValue());
        mgr.getNotificationController().setNotificationDuration(notificationDurationSetting.getValue().floatValue());
        mgr.getNotificationController().setSound(soundSetting.getValue());
    }

    @Override
    public String getElementId() {
        return getId();
    }

    @Override
    public boolean isVisible() {
        return isEnabled() && !hasError();
    }

    @Override
    public void render(IHudRenderer renderer) {
        if (!isVisible() || renderer == null) return;

        try {
            int screenWidth = renderer.getScreenWidth();
            int screenHeight = renderer.getScreenHeight();

            // 1. Render Active Notification Toasts
            List<HypixelNotification> notifications = HypixelManager.getInstance().getNotificationController().getVisibleNotifications();
            if (!notifications.isEmpty()) {
                int toastWidth = 180;
                int toastHeight = 28;
                int toastSpacing = 6;
                HudAnchor anchor = notificationAnchorSetting.getValue();

                int startX = anchor.calculateX(10, toastWidth, screenWidth);
                int startY = anchor.calculateY(10, toastHeight, screenHeight);

                for (int i = 0; i < notifications.size(); i++) {
                    HypixelNotification notif = notifications.get(i);
                    int currentY = startY + (i * (toastHeight + toastSpacing));

                    float alpha = notif.getAlpha();
                    int alphaByte = Math.max(0, Math.min(255, (int) (alpha * 220)));
                    int bgCol = (alphaByte << 24) | 0x1A1E24;
                    int borderCol = ((int) (alpha * 255) << 24) | (notif.getAccentColor() & 0x00FFFFFF);
                    int textCol = ((int) (alpha * 255) << 24) | 0xFFFFFF;

                    renderer.fill(startX, currentY, toastWidth, toastHeight, bgCol);
                    renderer.drawBorder(startX, currentY, toastWidth, toastHeight, borderCol, 1);
                    renderer.drawText(notif.getTitle(), startX + 6, currentY + 4, textCol, true, 0.9f, true);
                    renderer.drawText(notif.getMessage(), startX + 6, currentY + 15, 0xFFAAAAAA, true, 0.8f, false);
                }
            }

            // 2. Render Active Manual Social Suggestion Card (if available)
            ManualSuggestion suggestion = HypixelManager.getInstance().getManualSuggestions().getActiveSuggestion();
            if (suggestion != null && !suggestion.isExpired()) {
                int cardWidth = 200;
                int cardHeight = 36;
                int cardX = (screenWidth - cardWidth) / 2;
                int cardY = screenHeight - 90;

                renderer.fill(cardX, cardY, cardWidth, cardHeight, 0xEE1E2430);
                renderer.drawBorder(cardX, cardY, cardWidth, cardHeight, 0xFF55FF55, 1);
                renderer.drawText("Sugestia meczowa: " + suggestion.getSuggestedText(), cardX + 8, cardY + 6, 0xFF55FF55, true, 1.0f, true);
                renderer.drawText("[Naciśnij C aby skopiować do schowka]", cardX + 8, cardY + 20, 0xFFAAAAAA, true, 0.8f, false);
            }
        } catch (Throwable t) {
            LOGGER.error("[HypixelModsModule] Rendering error caught safely: {}", t.getMessage());
        }
    }

    public int getX() { return 10; }
    public int getY() { return 10; }
    public int getWidth() { return 180; }
    public int getHeight() { return 80; }

    // Setting Group Accessors
    public SettingGroup getGroupServerDetection() { return groupServerDetection; }
    public SettingGroup getGroupSafetyMode() { return groupSafetyMode; }
    public SettingGroup getGroupChatCustomization() { return groupChatCustomization; }
    public SettingGroup getGroupChatFilters() { return groupChatFilters; }
    public SettingGroup getGroupTablist() { return groupTablist; }
    public SettingGroup getGroupAutocomplete() { return groupAutocomplete; }
    public SettingGroup getGroupLevelDisplay() { return groupLevelDisplay; }
    public SettingGroup getGroupManualSuggestions() { return groupManualSuggestions; }
    public SettingGroup getGroupNotifications() { return groupNotifications; }

    // Key settings
    public BooleanSetting getAutoDetectSetting() { return autoDetectSetting; }
    public EnumSetting<SafetyPreset> getSafetyPresetSetting() { return safetyPresetSetting; }
    public BooleanSetting getEnableShortChannelsSetting() { return enableShortChannelsSetting; }
    public BooleanSetting getEnableChatFiltersSetting() { return enableChatFiltersSetting; }
    public BooleanSetting getEnableAutocompleteSetting() { return enableAutocompleteSetting; }
    public BooleanSetting getEnableLevelDisplaySetting() { return enableLevelDisplaySetting; }
    public BooleanSetting getEnableManualSuggestionsSetting() { return enableManualSuggestionsSetting; }
    public BooleanSetting getEnableNotificationsSetting() { return enableNotificationsSetting; }
}
