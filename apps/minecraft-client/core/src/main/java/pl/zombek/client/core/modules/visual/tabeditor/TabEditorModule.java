package pl.zombek.client.core.modules.visual.tabeditor;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.component.HudComponentModel;
import pl.zombek.client.core.hud.component.HudPriorityResolver;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.settings.*;

/**
 * Universal Tab Editor module for Minecraft.
 * Customizes the player list displayed when pressing the TAB key,
 * including headers, footers, columns, friend highlights, ping format, and layouts.
 */
public class TabEditorModule extends AbstractModule implements IHudElement {

    public static final String ID = "tab_editor";

    private final HudComponentModel model;

    // 1. General
    private final SettingGroup groupGeneral;
    private final BooleanSetting useCustomTabSetting;
    private final EnumSetting<TabLayout> layoutPresetSetting;

    // 2. Header
    private final SettingGroup groupHeader;
    private final BooleanSetting showHeaderSetting;
    private final EnumSetting<HeaderFooterMode> headerModeSetting;
    private final StringSetting customHeaderSetting;
    private final BooleanSetting hideServerHeaderSetting;
    private final BooleanSetting showServerIpSetting;
    private final BooleanSetting showPlayerCountSetting;
    private final BooleanSetting showPingSummarySetting;

    // 3. Footer
    private final SettingGroup groupFooter;
    private final BooleanSetting showFooterSetting;
    private final EnumSetting<HeaderFooterMode> footerModeSetting;
    private final StringSetting customFooterSetting;
    private final BooleanSetting hideServerFooterSetting;
    private final BooleanSetting showClientBrandSetting;
    private final BooleanSetting showTimeSetting;

    // 4. Player Entry
    private final SettingGroup groupPlayerEntry;
    private final BooleanSetting showAvatarSetting;
    private final NumberSetting avatarSizeSetting;
    private final BooleanSetting showPrefixSetting;
    private final BooleanSetting showNameSetting;
    private final BooleanSetting showSuffixSetting;
    private final BooleanSetting showTeamSetting;
    private final BooleanSetting showPingSetting;
    private final BooleanSetting showGamemodeSetting;
    private final BooleanSetting showScoreSetting;

    // 5. Appearance
    private final SettingGroup groupAppearance;
    private final ColorSetting backgroundColorSetting;
    private final NumberSetting backgroundOpacitySetting;
    private final BooleanSetting showBorderSetting;
    private final ColorSetting borderColorSetting;
    private final NumberSetting borderThicknessSetting;
    private final NumberSetting cornerRadiusSetting;
    private final ColorSetting headerColorSetting;
    private final ColorSetting footerColorSetting;
    private final ColorSetting nameColorSetting;
    private final BooleanSetting shadowSetting;
    private final NumberSetting fontScaleSetting;

    // 6. Grid
    private final SettingGroup groupGrid;
    private final NumberSetting maxColumnsSetting;
    private final NumberSetting maxRowsSetting;
    private final NumberSetting columnWidthSetting;
    private final NumberSetting rowHeightSetting;
    private final NumberSetting hSpacingSetting;
    private final NumberSetting vSpacingSetting;
    private final BooleanSetting adaptiveColumnsSetting;
    private final BooleanSetting adaptiveWidthSetting;

    // 7. Sorting
    private final SettingGroup groupSorting;
    private final EnumSetting<TabSortingMode> sortingModeSetting;
    private final BooleanSetting reverseSortSetting;
    private final BooleanSetting friendsFirstSetting;
    private final BooleanSetting teamGroupingSetting;
    private final BooleanSetting teamHeadersSetting;

    // 8. Ping
    private final SettingGroup groupPing;
    private final EnumSetting<TabPingMode> pingModeSetting;
    private final BooleanSetting showNumericPingSetting;
    private final BooleanSetting pingColoredSetting;
    private final ColorSetting goodColorSetting;
    private final ColorSetting mediumColorSetting;
    private final ColorSetting highColorSetting;
    private final ColorSetting unknownColorSetting;
    private final NumberSetting thresholdGreenSetting;
    private final NumberSetting thresholdYellowSetting;
    private final NumberSetting thresholdRedSetting;

    // 9. Friends
    private final SettingGroup groupFriends;
    private final BooleanSetting enableFriendHighlightSetting;
    private final ColorSetting friendColorSetting;
    private final StringSetting friendIconSetting;
    private final BooleanSetting showFriendIconSetting;
    private final BooleanSetting friendAtTopSetting;
    private final BooleanSetting useFriendAliasSetting;

    // 10. Animations
    private final SettingGroup groupAnimations;
    private final BooleanSetting fadeInSetting;
    private final NumberSetting animationDurationSetting;
    private final BooleanSetting enableSlideSetting;
    private final BooleanSetting smoothResizeSetting;

    // 11. Privacy
    private final SettingGroup groupPrivacy;
    private final BooleanSetting hideSelfSetting;
    private final BooleanSetting hideSpectatorsSetting;
    private final BooleanSetting anonymizeNamesSetting;

    // 12. Position
    private final SettingGroup groupPosition;
    private final NumberSetting verticalOffsetSetting;
    private final BooleanSetting centerHorizontallySetting;

    public TabEditorModule() {
        super(ID, "Tab Editor", "Umożliwia pełne dostosowanie wyglądu, układu i informacji na liście graczy (TAB) z zachowaniem Fair Play.", ModuleCategory.VISUAL);

        this.model = new HudComponentModel(ID, 220, 120, HudAnchor.CENTER);
        this.model.setRenderPriority(HudPriorityResolver.PRIORITY_TAB_EDITOR);

        // 1. General
        this.groupGeneral = new SettingGroup("general", "General");
        this.useCustomTabSetting = new BooleanSetting("use_custom_tab", "Use Custom Tab", "Zastępuje domyślny vanilla render listy graczy (TAB).", true);
        this.layoutPresetSetting = new EnumSetting<>("layout_preset", "Layout Preset", "Predefiniowany styl układu listy graczy.", TabLayout.MODERN);
        this.groupGeneral.addSetting(useCustomTabSetting);
        this.groupGeneral.addSetting(layoutPresetSetting);

        // 2. Header
        this.groupHeader = new SettingGroup("header", "Header");
        this.showHeaderSetting = new BooleanSetting("show_header", "Show Header", "Wyświetla nagłówek listy graczy.", true);
        this.headerModeSetting = new EnumSetting<>("header_mode", "Header Mode", "Tryb wyświetlania nagłówka.", HeaderFooterMode.ORIGINAL);
        this.customHeaderSetting = new StringSetting("custom_header", "Custom Header", "Własny tekst nagłówka.", "§6§lZombekClient Server");
        this.hideServerHeaderSetting = new BooleanSetting("hide_server_header", "Hide Server Header", "Ukrywa oryginalny nagłówek wysłany przez serwer.", false);
        this.showServerIpSetting = new BooleanSetting("show_server_ip", "Show Server IP", "Dołącza adres serwera do nagłówka.", true);
        this.showPlayerCountSetting = new BooleanSetting("show_player_count", "Show Player Count", "Dołącza liczbę graczy online do nagłówka.", true);
        this.showPingSummarySetting = new BooleanSetting("show_ping_summary", "Show Ping Summary", "Dołącza średni ping do nagłówka.", false);
        this.groupHeader.addSetting(showHeaderSetting);
        this.groupHeader.addSetting(headerModeSetting);
        this.groupHeader.addSetting(customHeaderSetting);
        this.groupHeader.addSetting(hideServerHeaderSetting);
        this.groupHeader.addSetting(showServerIpSetting);
        this.groupHeader.addSetting(showPlayerCountSetting);
        this.groupHeader.addSetting(showPingSummarySetting);

        // 3. Footer
        this.groupFooter = new SettingGroup("footer", "Footer");
        this.showFooterSetting = new BooleanSetting("show_footer", "Show Footer", "Wyświetla stopkę listy graczy.", true);
        this.footerModeSetting = new EnumSetting<>("footer_mode", "Footer Mode", "Tryb wyświetlania stopki.", HeaderFooterMode.ORIGINAL);
        this.customFooterSetting = new StringSetting("custom_footer", "Custom Footer", "Własny tekst stopki.", "§ePowered by §bZombekClient");
        this.hideServerFooterSetting = new BooleanSetting("hide_server_footer", "Hide Server Footer", "Ukrywa oryginalną stopkę wysłaną przez serwer.", false);
        this.showClientBrandSetting = new BooleanSetting("show_client_brand", "Show Client Brand", "Wyświetla podpis klienta w stopce.", true);
        this.showTimeSetting = new BooleanSetting("show_time", "Show Time", "Wyświetla aktualną godzinę w stopce.", false);
        this.groupFooter.addSetting(showFooterSetting);
        this.groupFooter.addSetting(footerModeSetting);
        this.groupFooter.addSetting(customFooterSetting);
        this.groupFooter.addSetting(hideServerFooterSetting);
        this.groupFooter.addSetting(showClientBrandSetting);
        this.groupFooter.addSetting(showTimeSetting);

        // 4. Player Entry
        this.groupPlayerEntry = new SettingGroup("player_entry", "Player Entry");
        this.showAvatarSetting = new BooleanSetting("show_avatar", "Show Avatar", "Renderuje główkę gracza.", true);
        this.avatarSizeSetting = new NumberSetting("avatar_size", "Avatar Size", "Rozmiar główki gracza w pikselach.", 8.0, 8.0, 16.0, 1.0);
        this.showPrefixSetting = new BooleanSetting("show_prefix", "Show Prefix", "Wyświetla prefix/rangę gracza.", true);
        this.showNameSetting = new BooleanSetting("show_name", "Show Name", "Wyświetla nick gracza.", true);
        this.showSuffixSetting = new BooleanSetting("show_suffix", "Show Suffix", "Wyświetla suffix gracza.", true);
        this.showTeamSetting = new BooleanSetting("show_team", "Show Team", "Wyświetla przynależność do drużyny.", true);
        this.showPingSetting = new BooleanSetting("show_ping", "Show Ping", "Wyświetla wskaźnik opóźnienia ping.", true);
        this.showGamemodeSetting = new BooleanSetting("show_gamemode", "Show Gamemode", "Wyświetla tryb gry gracza.", false);
        this.showScoreSetting = new BooleanSetting("show_score", "Show Score", "Wyświetla punkty/wynik gracza w scoreboard.", false);
        this.groupPlayerEntry.addSetting(showAvatarSetting);
        this.groupPlayerEntry.addSetting(avatarSizeSetting);
        this.groupPlayerEntry.addSetting(showPrefixSetting);
        this.groupPlayerEntry.addSetting(showNameSetting);
        this.groupPlayerEntry.addSetting(showSuffixSetting);
        this.groupPlayerEntry.addSetting(showTeamSetting);
        this.groupPlayerEntry.addSetting(showPingSetting);
        this.groupPlayerEntry.addSetting(showGamemodeSetting);
        this.groupPlayerEntry.addSetting(showScoreSetting);

        // 5. Appearance
        this.groupAppearance = new SettingGroup("appearance", "Appearance");
        this.backgroundColorSetting = new ColorSetting("background_color", "Background Color", "Kolor tła listy.", 0x000000);
        this.backgroundOpacitySetting = new NumberSetting("background_opacity", "Background Opacity", "Przezroczystość tła.", 0.6, 0.0, 1.0, 0.05);
        this.showBorderSetting = new BooleanSetting("show_border", "Show Border", "Rysuje obramowanie wokół listy.", false);
        this.borderColorSetting = new ColorSetting("border_color", "Border Color", "Kolor obramowania.", 0x40FFFFFF);
        this.borderThicknessSetting = new NumberSetting("border_thickness", "Border Thickness", "Grubość obramowania.", 1.0, 1.0, 4.0, 1.0);
        this.cornerRadiusSetting = new NumberSetting("corner_radius", "Corner Radius", "Zaokrąglenie narożników tła.", 0.0, 0.0, 10.0, 1.0);
        this.headerColorSetting = new ColorSetting("header_color", "Header Color", "Kolor tekstu nagłówka.", 0xFFFFAA00);
        this.footerColorSetting = new ColorSetting("footer_color", "Footer Color", "Kolor tekstu stopki.", 0xFFAAAAAA);
        this.nameColorSetting = new ColorSetting("name_color", "Name Color", "Domyślny kolor nicku gracza.", 0xFFFFFFFF);
        this.shadowSetting = new BooleanSetting("shadow", "Text Shadow", "Rysuje cień pod tekstem.", true);
        this.fontScaleSetting = new NumberSetting("font_scale", "Font Scale", "Skala czcionki.", 1.0, 0.5, 2.0, 0.1);
        this.groupAppearance.addSetting(backgroundColorSetting);
        this.groupAppearance.addSetting(backgroundOpacitySetting);
        this.groupAppearance.addSetting(showBorderSetting);
        this.groupAppearance.addSetting(borderColorSetting);
        this.groupAppearance.addSetting(borderThicknessSetting);
        this.groupAppearance.addSetting(cornerRadiusSetting);
        this.groupAppearance.addSetting(headerColorSetting);
        this.groupAppearance.addSetting(footerColorSetting);
        this.groupAppearance.addSetting(nameColorSetting);
        this.groupAppearance.addSetting(shadowSetting);
        this.groupAppearance.addSetting(fontScaleSetting);

        // 6. Grid
        this.groupGrid = new SettingGroup("grid", "Grid");
        this.maxColumnsSetting = new NumberSetting("max_columns", "Max Columns", "Maksymalna liczba kolumn w siatce.", 4.0, 1.0, 6.0, 1.0);
        this.maxRowsSetting = new NumberSetting("max_rows", "Max Rows", "Maksymalna liczba wierszy na kolumnę.", 20.0, 5.0, 40.0, 1.0);
        this.columnWidthSetting = new NumberSetting("column_width", "Column Width", "Szerokość kolumny w pikselach.", 120.0, 60.0, 250.0, 5.0);
        this.rowHeightSetting = new NumberSetting("row_height", "Row Height", "Wysokość wiersza w pikselach.", 12.0, 10.0, 24.0, 1.0);
        this.hSpacingSetting = new NumberSetting("h_spacing", "Horizontal Spacing", "Odstęp poziomy między kolumnami.", 4.0, 1.0, 10.0, 1.0);
        this.vSpacingSetting = new NumberSetting("v_spacing", "Vertical Spacing", "Odstęp pionowy między wierszami.", 2.0, 1.0, 10.0, 1.0);
        this.adaptiveColumnsSetting = new BooleanSetting("adaptive_columns", "Adaptive Columns", "Automatycznie dostosowuje liczbę kolumn.", true);
        this.adaptiveWidthSetting = new BooleanSetting("adaptive_width", "Adaptive Width", "Dostosowuje szerokość kolumn do najdłuższych wpisów.", true);
        this.groupGrid.addSetting(maxColumnsSetting);
        this.groupGrid.addSetting(maxRowsSetting);
        this.groupGrid.addSetting(columnWidthSetting);
        this.groupGrid.addSetting(rowHeightSetting);
        this.groupGrid.addSetting(hSpacingSetting);
        this.groupGrid.addSetting(vSpacingSetting);
        this.groupGrid.addSetting(adaptiveColumnsSetting);
        this.groupGrid.addSetting(adaptiveWidthSetting);

        // 7. Sorting
        this.groupSorting = new SettingGroup("sorting", "Sorting");
        this.sortingModeSetting = new EnumSetting<>("sorting_mode", "Sorting Mode", "Kryterium sortowania wpisów na liście.", TabSortingMode.VANILLA_ORDER);
        this.reverseSortSetting = new BooleanSetting("reverse_sort", "Reverse Sort", "Odwraca kolejność sortowania.", false);
        this.friendsFirstSetting = new BooleanSetting("friends_first", "Friends First", "Umieszcza znajomych na początku listy.", false);
        this.teamGroupingSetting = new BooleanSetting("team_grouping", "Team Grouping", "Grupuje graczy według drużyn.", false);
        this.teamHeadersSetting = new BooleanSetting("team_headers", "Team Headers", "Wyświetla nagłówki dla poszczególnych drużyn.", false);
        this.groupSorting.addSetting(sortingModeSetting);
        this.groupSorting.addSetting(reverseSortSetting);
        this.groupSorting.addSetting(friendsFirstSetting);
        this.groupSorting.addSetting(teamGroupingSetting);
        this.groupSorting.addSetting(teamHeadersSetting);

        // 8. Ping
        this.groupPing = new SettingGroup("ping", "Ping");
        this.pingModeSetting = new EnumSetting<>("ping_mode", "Ping Mode", "Format prezentacji pingu gracza.", TabPingMode.NUMERIC);
        this.showNumericPingSetting = new BooleanSetting("show_numeric_ping", "Show Numeric Ping", "Wyświetla wartość liczbową w milisekundach.", true);
        this.pingColoredSetting = new BooleanSetting("ping_colored", "Ping Colored", "Koloruje wartość w zależności od wysokości opóźnienia.", true);
        this.goodColorSetting = new ColorSetting("good_color", "Good Ping Color", "Kolor dla niskiego pingu.", 0xFF55FF55);
        this.mediumColorSetting = new ColorSetting("medium_color", "Medium Ping Color", "Kolor dla średniego pingu.", 0xFFFFFF55);
        this.highColorSetting = new ColorSetting("high_color", "High Ping Color", "Kolor dla wysokiego pingu.", 0xFFFF5555);
        this.unknownColorSetting = new ColorSetting("unknown_color", "Unknown Ping Color", "Kolor dla nieznanego pingu.", 0xFF888888);
        this.thresholdGreenSetting = new NumberSetting("threshold_green", "Threshold Green (ms)", "Maksymalny ping dla dobrego wskaźnika.", 60.0, 10.0, 150.0, 5.0);
        this.thresholdYellowSetting = new NumberSetting("threshold_yellow", "Threshold Yellow (ms)", "Maksymalny ping dla średniego wskaźnika.", 150.0, 50.0, 300.0, 10.0);
        this.thresholdRedSetting = new NumberSetting("threshold_red", "Threshold Red (ms)", "Próg wysokiego pingu.", 300.0, 100.0, 600.0, 20.0);
        this.groupPing.addSetting(pingModeSetting);
        this.groupPing.addSetting(showNumericPingSetting);
        this.groupPing.addSetting(pingColoredSetting);
        this.groupPing.addSetting(goodColorSetting);
        this.groupPing.addSetting(mediumColorSetting);
        this.groupPing.addSetting(highColorSetting);
        this.groupPing.addSetting(unknownColorSetting);
        this.groupPing.addSetting(thresholdGreenSetting);
        this.groupPing.addSetting(thresholdYellowSetting);
        this.groupPing.addSetting(thresholdRedSetting);

        // 9. Friends
        this.groupFriends = new SettingGroup("friends", "Friends");
        this.enableFriendHighlightSetting = new BooleanSetting("enable_friend_highlight", "Highlight Friends", "Wyróżnia dodanych znajomych na liście.", true);
        this.friendColorSetting = new ColorSetting("friend_color", "Friend Color", "Kolor wyróżnienia znajomych.", 0xFF55FF55);
        this.friendIconSetting = new StringSetting("friend_icon", "Friend Icon", "Ikona/symbol znajomego.", "★");
        this.showFriendIconSetting = new BooleanSetting("show_friend_icon", "Show Friend Icon", "Wyświetla ikonę przy nicku znajomego.", true);
        this.friendAtTopSetting = new BooleanSetting("friend_at_top", "Friend At Top", "Wymusza pozycję znajomych na samej górze.", false);
        this.useFriendAliasSetting = new BooleanSetting("use_friend_alias", "Use Friend Alias", "Wyświetla lokalny alias znajomego zamiast nicku.", true);
        this.groupFriends.addSetting(enableFriendHighlightSetting);
        this.groupFriends.addSetting(friendColorSetting);
        this.groupFriends.addSetting(friendIconSetting);
        this.groupFriends.addSetting(showFriendIconSetting);
        this.groupFriends.addSetting(friendAtTopSetting);
        this.groupFriends.addSetting(useFriendAliasSetting);

        // 10. Animations
        this.groupAnimations = new SettingGroup("animations", "Animations");
        this.fadeInSetting = new BooleanSetting("fade_in", "Fade In", "Płynne pojawianie się listy po naciśnięciu TAB.", true);
        this.animationDurationSetting = new NumberSetting("animation_duration", "Animation Duration (ms)", "Czas trwania animacji w milisekundach.", 150.0, 0.0, 500.0, 25.0);
        this.enableSlideSetting = new BooleanSetting("enable_slide", "Enable Slide", "Płynny efekt wysuwania z góry ekranu.", false);
        this.smoothResizeSetting = new BooleanSetting("smooth_resize", "Smooth Resize", "Płynne dopasowanie wymiarów przy zmianie liczby graczy.", true);
        this.groupAnimations.addSetting(fadeInSetting);
        this.groupAnimations.addSetting(animationDurationSetting);
        this.groupAnimations.addSetting(enableSlideSetting);
        this.groupAnimations.addSetting(smoothResizeSetting);

        // 11. Privacy
        this.groupPrivacy = new SettingGroup("privacy", "Privacy");
        this.hideSelfSetting = new BooleanSetting("hide_self", "Hide Self", "Ukrywa własny wpis na liście graczy.", false);
        this.hideSpectatorsSetting = new BooleanSetting("hide_spectators", "Hide Spectators", "Ukrywa graczy w trybie obserwatora.", false);
        this.anonymizeNamesSetting = new BooleanSetting("anonymize_names", "Anonymize Names", "Ukrywa nicki graczy (np. do nagrań i streamingu).", false);
        this.groupPrivacy.addSetting(hideSelfSetting);
        this.groupPrivacy.addSetting(hideSpectatorsSetting);
        this.groupPrivacy.addSetting(anonymizeNamesSetting);

        // 12. Position
        this.groupPosition = new SettingGroup("position", "Position");
        this.verticalOffsetSetting = new NumberSetting("vertical_offset", "Vertical Offset", "Odstęp listy od górnej krawędzi ekranu.", 10.0, 0.0, 200.0, 5.0);
        this.centerHorizontallySetting = new BooleanSetting("center_horizontally", "Center Horizontally", "Automatycznie wyśrodkowuje listę w poziomie.", true);
        this.groupPosition.addSetting(verticalOffsetSetting);
        this.groupPosition.addSetting(centerHorizontallySetting);

        // Register all groups
        registerGroup(groupGeneral);
        registerGroup(groupHeader);
        registerGroup(groupFooter);
        registerGroup(groupPlayerEntry);
        registerGroup(groupAppearance);
        registerGroup(groupGrid);
        registerGroup(groupSorting);
        registerGroup(groupPing);
        registerGroup(groupFriends);
        registerGroup(groupAnimations);
        registerGroup(groupPrivacy);
        registerGroup(groupPosition);
    }

    public static boolean shouldOverrideVanilla() {
        TabEditorModule mod = (TabEditorModule) ModuleManager.getInstance().getModuleById(ID);
        return mod != null && mod.isEnabled() && mod.getUseCustomTabSetting().getValue();
    }

    @Override
    public String getElementId() {
        return ID;
    }

    @Override
    public boolean isVisible() {
        return isEnabled() && !hasError();
    }

    @Override
    public void render(IHudRenderer renderer) {
        if (!isVisible()) return;
        // In 2D HUD / Right Shift menu preview mode
        CustomTabRenderer.renderTabList(renderer, this, null, "§6Preview Server Header", "§bPreview Footer", true);
    }

    public HudComponentModel getHudModel() { return model; }

    // Getters for Settings
    public BooleanSetting getUseCustomTabSetting() { return useCustomTabSetting; }
    public EnumSetting<TabLayout> getLayoutPresetSetting() { return layoutPresetSetting; }

    public BooleanSetting getShowHeaderSetting() { return showHeaderSetting; }
    public EnumSetting<HeaderFooterMode> getHeaderModeSetting() { return headerModeSetting; }
    public StringSetting getCustomHeaderSetting() { return customHeaderSetting; }
    public BooleanSetting getHideServerHeaderSetting() { return hideServerHeaderSetting; }
    public BooleanSetting getShowServerIpSetting() { return showServerIpSetting; }
    public BooleanSetting getShowPlayerCountSetting() { return showPlayerCountSetting; }
    public BooleanSetting getShowPingSummarySetting() { return showPingSummarySetting; }

    public BooleanSetting getShowFooterSetting() { return showFooterSetting; }
    public EnumSetting<HeaderFooterMode> getFooterModeSetting() { return footerModeSetting; }
    public StringSetting getCustomFooterSetting() { return customFooterSetting; }
    public BooleanSetting getHideServerFooterSetting() { return hideServerFooterSetting; }
    public BooleanSetting getShowClientBrandSetting() { return showClientBrandSetting; }
    public BooleanSetting getShowTimeSetting() { return showTimeSetting; }

    public BooleanSetting getShowAvatarSetting() { return showAvatarSetting; }
    public NumberSetting getAvatarSizeSetting() { return avatarSizeSetting; }
    public BooleanSetting getShowPrefixSetting() { return showPrefixSetting; }
    public BooleanSetting getShowNameSetting() { return showNameSetting; }
    public BooleanSetting getShowSuffixSetting() { return showSuffixSetting; }
    public BooleanSetting getShowTeamSetting() { return showTeamSetting; }
    public BooleanSetting getShowPingSetting() { return showPingSetting; }
    public BooleanSetting getShowGamemodeSetting() { return showGamemodeSetting; }
    public BooleanSetting getShowScoreSetting() { return showScoreSetting; }

    public ColorSetting getBackgroundColorSetting() { return backgroundColorSetting; }
    public NumberSetting getBackgroundOpacitySetting() { return backgroundOpacitySetting; }
    public BooleanSetting getShowBorderSetting() { return showBorderSetting; }
    public ColorSetting getBorderColorSetting() { return borderColorSetting; }
    public NumberSetting getBorderThicknessSetting() { return borderThicknessSetting; }
    public NumberSetting getCornerRadiusSetting() { return cornerRadiusSetting; }
    public ColorSetting getHeaderColorSetting() { return headerColorSetting; }
    public ColorSetting getFooterColorSetting() { return footerColorSetting; }
    public ColorSetting getNameColorSetting() { return nameColorSetting; }
    public BooleanSetting getShadowSetting() { return shadowSetting; }
    public NumberSetting getFontScaleSetting() { return fontScaleSetting; }

    public NumberSetting getMaxColumnsSetting() { return maxColumnsSetting; }
    public NumberSetting getMaxRowsSetting() { return maxRowsSetting; }
    public NumberSetting getColumnWidthSetting() { return columnWidthSetting; }
    public NumberSetting getRowHeightSetting() { return rowHeightSetting; }
    public NumberSetting getHSpacingSetting() { return hSpacingSetting; }
    public NumberSetting getVSpacingSetting() { return vSpacingSetting; }
    public BooleanSetting getAdaptiveColumnsSetting() { return adaptiveColumnsSetting; }
    public BooleanSetting getAdaptiveWidthSetting() { return adaptiveWidthSetting; }

    public EnumSetting<TabSortingMode> getSortingModeSetting() { return sortingModeSetting; }
    public BooleanSetting getReverseSortSetting() { return reverseSortSetting; }
    public BooleanSetting getFriendsFirstSetting() { return friendsFirstSetting; }
    public BooleanSetting getTeamGroupingSetting() { return teamGroupingSetting; }
    public BooleanSetting getTeamHeadersSetting() { return teamHeadersSetting; }

    public EnumSetting<TabPingMode> getPingModeSetting() { return pingModeSetting; }
    public BooleanSetting getShowNumericPingSetting() { return showNumericPingSetting; }
    public BooleanSetting getPingColoredSetting() { return pingColoredSetting; }
    public ColorSetting getGoodColorSetting() { return goodColorSetting; }
    public ColorSetting getMediumColorSetting() { return mediumColorSetting; }
    public ColorSetting getHighColorSetting() { return highColorSetting; }
    public ColorSetting getUnknownColorSetting() { return unknownColorSetting; }
    public NumberSetting getThresholdGreenSetting() { return thresholdGreenSetting; }
    public NumberSetting getThresholdYellowSetting() { return thresholdYellowSetting; }
    public NumberSetting getThresholdRedSetting() { return thresholdRedSetting; }

    public BooleanSetting getEnableFriendHighlightSetting() { return enableFriendHighlightSetting; }
    public ColorSetting getFriendColorSetting() { return friendColorSetting; }
    public StringSetting getFriendIconSetting() { return friendIconSetting; }
    public BooleanSetting getShowFriendIconSetting() { return showFriendIconSetting; }
    public BooleanSetting getFriendAtTopSetting() { return friendAtTopSetting; }
    public BooleanSetting getUseFriendAliasSetting() { return useFriendAliasSetting; }

    public BooleanSetting getFadeInSetting() { return fadeInSetting; }
    public NumberSetting getAnimationDurationSetting() { return animationDurationSetting; }
    public BooleanSetting getEnableSlideSetting() { return enableSlideSetting; }
    public BooleanSetting getSmoothResizeSetting() { return smoothResizeSetting; }

    public BooleanSetting getHideSelfSetting() { return hideSelfSetting; }
    public BooleanSetting getHideSpectatorsSetting() { return hideSpectatorsSetting; }
    public BooleanSetting getAnonymizeNamesSetting() { return anonymizeNamesSetting; }

    public NumberSetting getVerticalOffsetSetting() { return verticalOffsetSetting; }
    public BooleanSetting getCenterHorizontallySetting() { return centerHorizontallySetting; }
}
