package pl.zombek.client.core.modules.visual.chat;

import pl.zombek.client.core.chat.*;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

/**
 * Universal Chat Customization Module for ZombekClient.
 */
public class ChatModule extends AbstractModule implements IHudElement {
    private static ChatModule instance;

    // Appearance Settings
    private final SettingGroup appearanceGroup;
    private final EnumSetting<ChatPreset> presetSetting;
    private final BooleanSetting showBackgroundSetting;
    private final ColorSetting backgroundColorSetting;
    private final NumberSetting backgroundOpacitySetting;
    private final BooleanSetting roundedCornersSetting;
    private final BooleanSetting borderSetting;
    private final BooleanSetting shadowSetting;

    // Dimensions Settings
    private final SettingGroup dimensionsGroup;
    private final NumberSetting chatWidthSetting;
    private final NumberSetting chatHeightSetting;
    private final NumberSetting chatScaleSetting;
    private final NumberSetting lineSpacingSetting;
    private final NumberSetting maxVisibleLinesSetting;

    // Timestamps Settings
    private final SettingGroup timestampsGroup;
    private final BooleanSetting showTimestampsSetting;
    private final EnumSetting<TimestampFormat> timestampFormatSetting;
    private final StringSetting customTimestampFormatSetting;

    // Message Grouping Settings
    private final SettingGroup messageGroupingGroup;
    private final BooleanSetting repeatedGroupingSetting;
    private final BooleanSetting senderGroupingSetting;

    // Tabs Settings
    private final SettingGroup tabsGroup;
    private final BooleanSetting enableTabsSetting;
    private final BooleanSetting showUnreadCounterSetting;

    // Filters Settings
    private final SettingGroup filtersGroup;
    private final BooleanSetting enableFiltersSetting;
    private final BooleanSetting hideJoinLeaveSetting;
    private final BooleanSetting hideDeathMessagesSetting;
    private final BooleanSetting hideAdvancementsSetting;

    // History Settings
    private final SettingGroup historyGroup;
    private final NumberSetting maxMessagesSetting;
    private final BooleanSetting clearOnDisconnectSetting;

    // Input Settings
    private final SettingGroup inputGroup;
    private final BooleanSetting preserveDraftsSetting;
    private final BooleanSetting customInputBoxSetting;

    // Notifications Settings
    private final SettingGroup notificationsGroup;
    private final BooleanSetting enableMentionsSetting;
    private final BooleanSetting enableDirectAlertsSetting;
    private final BooleanSetting playSoundSetting;
    private final NumberSetting soundCooldownSetting;

    // Privacy Settings
    private final SettingGroup privacyGroup;
    private final BooleanSetting privacyModeSetting;
    private final BooleanSetting streamerModeSetting;
    private final BooleanSetting excludePrivateMessagesSetting;

    public ChatModule() {
        super("chat", "Chat", "Kompleksowa personalizacja czatu Minecraft, zakładek, filtrów i historii", ModuleCategory.VISUAL);
        instance = this;

        // 1. Appearance Group
        this.appearanceGroup = new SettingGroup("appearance", "Wygląd i Styl");
        this.presetSetting = new EnumSetting<>("preset", "Preset Stylu", "Gotowy preset wizualny czatu", ChatPreset.MODERN);
        this.showBackgroundSetting = new BooleanSetting("show_background", "Tło Czatu", "Wyświetla tło pod wiadomościami", true);
        this.backgroundColorSetting = new ColorSetting("background_color", "Kolor Tła", "Kolor tła czatu", 0xAA121216);
        this.backgroundOpacitySetting = new NumberSetting("background_opacity", "Przezroczystość Tła", "Poziom krycia tła", 0.65, 0.0, 1.0, 0.05, "");
        this.roundedCornersSetting = new BooleanSetting("rounded_corners", "Zaokrąglone Narożniki", "Zaokrągla krawędzie panelu", true);
        this.borderSetting = new BooleanSetting("border", "Obramowanie", "Wyświetla subtelną ramkę wokół czatu", true);
        this.shadowSetting = new BooleanSetting("shadow", "Cień Tekstu", "Rysuje cień pod znakami tekstu", true);
        appearanceGroup.addSetting(presetSetting);
        appearanceGroup.addSetting(showBackgroundSetting);
        appearanceGroup.addSetting(backgroundColorSetting);
        appearanceGroup.addSetting(backgroundOpacitySetting);
        appearanceGroup.addSetting(roundedCornersSetting);
        appearanceGroup.addSetting(borderSetting);
        appearanceGroup.addSetting(shadowSetting);
        registerGroup(appearanceGroup);

        // 2. Dimensions Group
        this.dimensionsGroup = new SettingGroup("dimensions", "Wymiary i Skala");
        this.chatWidthSetting = new NumberSetting("chat_width", "Szerokość", "Szerokość okna czatu w pikselach", 320.0, 160.0, 600.0, 10.0, "px");
        this.chatHeightSetting = new NumberSetting("chat_height", "Wysokość", "Wysokość okna czatu w pikselach", 160.0, 80.0, 400.0, 10.0, "px");
        this.chatScaleSetting = new NumberSetting("chat_scale", "Skala", "Skala renderowania czatu", 1.0, 0.5, 2.0, 0.1, "");
        this.lineSpacingSetting = new NumberSetting("line_spacing", "Odstępy Wierszy", "Wysokość pojedynczego wiersza w pikselach", 10.0, 8.0, 20.0, 1.0, "px");
        this.maxVisibleLinesSetting = new NumberSetting("max_visible_lines", "Maks. Widocznych Linii", "Maksymalna liczba wierszy", 10.0, 4.0, 30.0, 1.0, "");
        dimensionsGroup.addSetting(chatWidthSetting);
        dimensionsGroup.addSetting(chatHeightSetting);
        dimensionsGroup.addSetting(chatScaleSetting);
        dimensionsGroup.addSetting(lineSpacingSetting);
        dimensionsGroup.addSetting(maxVisibleLinesSetting);
        registerGroup(dimensionsGroup);

        // 3. Timestamps Group
        this.timestampsGroup = new SettingGroup("timestamps", "Znaczniki Czasu");
        this.showTimestampsSetting = new BooleanSetting("show_timestamps", "Pokaż Czas", "Dodaje timestamp przed wiadomością", true);
        this.timestampFormatSetting = new EnumSetting<>("timestamp_format", "Format Czasu", "Format prezentacji czasu", TimestampFormat.HH_MM);
        this.customTimestampFormatSetting = new StringSetting("custom_timestamp_format", "Własny Format", "Format SimpleDateFormat", "HH:mm");
        timestampsGroup.addSetting(showTimestampsSetting);
        timestampsGroup.addSetting(timestampFormatSetting);
        timestampsGroup.addSetting(customTimestampFormatSetting);
        registerGroup(timestampsGroup);

        // 4. Message Grouping Group
        this.messageGroupingGroup = new SettingGroup("message_grouping", "Grupowanie Wiadomości");
        this.repeatedGroupingSetting = new BooleanSetting("repeated_grouping", "Grupuj Powtórzenia", "Łączy identyczne wiadomości (np. x3)", true);
        this.senderGroupingSetting = new BooleanSetting("sender_grouping", "Grupuj po Nadawcy", "Łączy kolejne wiadomości tego samego gracza", false);
        messageGroupingGroup.addSetting(repeatedGroupingSetting);
        messageGroupingGroup.addSetting(senderGroupingSetting);
        registerGroup(messageGroupingGroup);

        // 5. Tabs Group
        this.tabsGroup = new SettingGroup("chat_tabs", "Zakładki Czatu");
        this.enableTabsSetting = new BooleanSetting("enable_tabs", "Włącz Zakładki", "Podział czatu na lokalne kanały", true);
        this.showUnreadCounterSetting = new BooleanSetting("show_unread", "Licznik Nieprzeczytanych", "Pokazuje liczbę nowych wiadomości w zakładce", true);
        tabsGroup.addSetting(enableTabsSetting);
        tabsGroup.addSetting(showUnreadCounterSetting);
        registerGroup(tabsGroup);

        // 6. Filters Group
        this.filtersGroup = new SettingGroup("filters", "Filtry Wiadomości");
        this.enableFiltersSetting = new BooleanSetting("enable_filters", "Włącz Filtry", "Globalny przełącznik reguł filtrowania", true);
        this.hideJoinLeaveSetting = new BooleanSetting("hide_join_leave", "Ukryj Dołączenia/Wyjścia", "Filtruje powiadomienia graczy", false);
        this.hideDeathMessagesSetting = new BooleanSetting("hide_deaths", "Ukryj Śmierci", "Filtruje komunikaty o śmierci", false);
        this.hideAdvancementsSetting = new BooleanSetting("hide_advancements", "Ukryj Osiągnięcia", "Filtruje powiadomienia o advancementach", false);
        filtersGroup.addSetting(enableFiltersSetting);
        filtersGroup.addSetting(hideJoinLeaveSetting);
        filtersGroup.addSetting(hideDeathMessagesSetting);
        filtersGroup.addSetting(hideAdvancementsSetting);
        registerGroup(filtersGroup);

        // 7. History Group
        this.historyGroup = new SettingGroup("history", "Historia Czatu");
        this.maxMessagesSetting = new NumberSetting("max_messages", "Maks. Wiadomości", "Limit wiadomości w pamięci", 1000.0, 100.0, 5000.0, 100.0, "");
        this.clearOnDisconnectSetting = new BooleanSetting("clear_on_disconnect", "Czyść po Rozłączeniu", "Usuwa historię przy wyjściu z serwera", true);
        historyGroup.addSetting(maxMessagesSetting);
        historyGroup.addSetting(clearOnDisconnectSetting);
        registerGroup(historyGroup);

        // 8. Input Group
        this.inputGroup = new SettingGroup("input", "Pole Wpisywania");
        this.preserveDraftsSetting = new BooleanSetting("preserve_drafts", "Zachowuj Wpisany Tekst", "Nie czyści draftu przy zmianie zakładki/zamknięciu", true);
        this.customInputBoxSetting = new BooleanSetting("custom_input", "Własne Pole Tekstowe", "Używa dostosowanego stylu pola input", true);
        inputGroup.addSetting(preserveDraftsSetting);
        inputGroup.addSetting(customInputBoxSetting);
        registerGroup(inputGroup);

        // 9. Notifications Group
        this.notificationsGroup = new SettingGroup("notifications", "Powiadomienia Lokalne");
        this.enableMentionsSetting = new BooleanSetting("enable_mentions", "Wzmianki o Graczu", "Wyróżnia wiadomości zawierające Twój nick", true);
        this.enableDirectAlertsSetting = new BooleanSetting("direct_alerts", "Powiadomienia /msg", "Powiadamia o prywatnych wiadomościach", true);
        this.playSoundSetting = new BooleanSetting("play_sound", "Dźwięk Powiadomienia", "Odtwarza dźwięk przy wzmiance", false);
        this.soundCooldownSetting = new NumberSetting("sound_cooldown", "Cooldown Dźwięku (s)", "Odstęp między dźwiękami", 5.0, 1.0, 30.0, 1.0, "s");
        notificationsGroup.addSetting(enableMentionsSetting);
        notificationsGroup.addSetting(enableDirectAlertsSetting);
        notificationsGroup.addSetting(playSoundSetting);
        notificationsGroup.addSetting(soundCooldownSetting);
        registerGroup(notificationsGroup);

        // 10. Privacy Group
        this.privacyGroup = new SettingGroup("privacy", "Prywatność i Streamer");
        this.privacyModeSetting = new BooleanSetting("privacy_mode", "Tryb Prywatności", "Ogranicza zapisywanie prywatnych wiadomości", false);
        this.streamerModeSetting = new BooleanSetting("streamer_mode", "Streamer Mode", "Maskuje nadawców w historii eksportu", false);
        this.excludePrivateMessagesSetting = new BooleanSetting("exclude_private", "Wyklucz Prywatne z Historii", "Nie zapisuje /msg w pamięci", false);
        privacyGroup.addSetting(privacyModeSetting);
        privacyGroup.addSetting(streamerModeSetting);
        privacyGroup.addSetting(excludePrivateMessagesSetting);
        registerGroup(privacyGroup);

        // Wire listener for preset changes
        this.presetSetting.addChangeListener(newPreset -> {
            ChatLayoutManager.getInstance().applyPreset(newPreset);
        });
    }

    public static synchronized ChatModule getInstance() {
        return instance;
    }

    public static boolean shouldOverrideVanilla() {
        return instance != null && instance.isEnabled();
    }

    @Override
    public void resetToDefaults() {
        super.resetToDefaults();
        ChatLayoutManager.getInstance().applyPreset(ChatPreset.MODERN);
        ChatMessageManager.getInstance().setShowTimestamps(true);
        ChatMessageManager.getInstance().setTimestampFormat(TimestampFormat.HH_MM);
        ChatMessageManager.getInstance().setEnableRepeatedMessageGrouping(true);
        ChatFilterManager.getInstance().initDefaults();
        ChatHistoryManager.getInstance().setMaxMessages(1000);
        ChatNotificationController.getInstance().setEnableMentions(true);
        ChatNotificationController.getInstance().setPlaySound(false);
    }

    // IHudElement Preview Support
    @Override
    public String getElementId() { return "chat"; }
    @Override
    public boolean isVisible() { return isEnabled(); }

    @Override
    public void render(IHudRenderer renderer) {
        if (!isEnabled()) return;
        // Preview render in GUI
        int x = 20;
        int y = 50;
        int w = ChatLayoutManager.getInstance().getChatWidth();
        int h = ChatLayoutManager.getInstance().getChatHeight();

        renderer.fill(x, y, w, h, 0xAA121216);
        renderer.drawBorder(x, y, w, h, 0xFF555566, 1);
        renderer.drawText("§b[PREVIEW] ZombekClient Custom Chat", x + 6, y + 6, 0xFFFFFFFF, true, 1.0f, false);
        renderer.drawText("§7[14:32] §f<Alex> §aHej! Gotowy na mecz? §e×2", x + 6, y + 22, 0xFFFFFFFF, true, 1.0f, false);
        renderer.drawText("§7[14:33] §d[Prywatne od Steve]: §fSprawdź ekwipunek!", x + 6, y + 36, 0xFFFF55FF, true, 1.0f, false);
        renderer.drawText("§7[14:34] §e[System]: §fWydarzenie rozpocznie się za 10s", x + 6, y + 50, 0xFFFFFF55, true, 1.0f, false);
    }

    // Getters for Settings
    public EnumSetting<ChatPreset> getPresetSetting() { return presetSetting; }
    public BooleanSetting getShowBackgroundSetting() { return showBackgroundSetting; }
    public ColorSetting getBackgroundColorSetting() { return backgroundColorSetting; }
    public NumberSetting getBackgroundOpacitySetting() { return backgroundOpacitySetting; }
    public BooleanSetting getRoundedCornersSetting() { return roundedCornersSetting; }
    public BooleanSetting getBorderSetting() { return borderSetting; }
    public BooleanSetting getShadowSetting() { return shadowSetting; }
    public NumberSetting getChatWidthSetting() { return chatWidthSetting; }
    public NumberSetting getChatHeightSetting() { return chatHeightSetting; }
    public NumberSetting getChatScaleSetting() { return chatScaleSetting; }
    public NumberSetting getLineSpacingSetting() { return lineSpacingSetting; }
    public NumberSetting getMaxVisibleLinesSetting() { return maxVisibleLinesSetting; }
    public BooleanSetting getShowTimestampsSetting() { return showTimestampsSetting; }
    public EnumSetting<TimestampFormat> getTimestampFormatSetting() { return timestampFormatSetting; }
    public StringSetting getCustomTimestampFormatSetting() { return customTimestampFormatSetting; }
    public BooleanSetting getRepeatedGroupingSetting() { return repeatedGroupingSetting; }
    public BooleanSetting getSenderGroupingSetting() { return senderGroupingSetting; }
    public BooleanSetting getEnableTabsSetting() { return enableTabsSetting; }
    public BooleanSetting getShowUnreadCounterSetting() { return showUnreadCounterSetting; }
    public BooleanSetting getEnableFiltersSetting() { return enableFiltersSetting; }
    public BooleanSetting getHideJoinLeaveSetting() { return hideJoinLeaveSetting; }
    public BooleanSetting getHideDeathMessagesSetting() { return hideDeathMessagesSetting; }
    public BooleanSetting getHideAdvancementsSetting() { return hideAdvancementsSetting; }
    public NumberSetting getMaxMessagesSetting() { return maxMessagesSetting; }
    public BooleanSetting getClearOnDisconnectSetting() { return clearOnDisconnectSetting; }
    public BooleanSetting getPreserveDraftsSetting() { return preserveDraftsSetting; }
    public BooleanSetting getCustomInputBoxSetting() { return customInputBoxSetting; }
    public BooleanSetting getEnableMentionsSetting() { return enableMentionsSetting; }
    public BooleanSetting getEnableDirectAlertsSetting() { return enableDirectAlertsSetting; }
    public BooleanSetting getPlaySoundSetting() { return playSoundSetting; }
    public NumberSetting getSoundCooldownSetting() { return soundCooldownSetting; }
    public BooleanSetting getPrivacyModeSetting() { return privacyModeSetting; }
    public BooleanSetting getStreamerModeSetting() { return streamerModeSetting; }
    public BooleanSetting getExcludePrivateMessagesSetting() { return excludePrivateMessagesSetting; }
}
