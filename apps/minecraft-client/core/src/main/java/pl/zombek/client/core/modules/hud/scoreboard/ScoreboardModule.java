package pl.zombek.client.core.modules.hud.scoreboard;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.component.HudComponentModel;
import pl.zombek.client.core.hud.component.HudPriorityResolver;
import pl.zombek.client.core.hud.scoreboard.*;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

/**
 * Scoreboard Module providing complete visual customization of Minecraft's sidebar scoreboard.
 */
public class ScoreboardModule extends AbstractModule implements IHudElement {

    private static ScoreboardModule instance;

    private final HudComponentModel model;

    // General
    private BooleanSetting useCustomRendererSetting;
    private BooleanSetting previewModeSetting;
    private EnumSetting<ScoreboardStyle> styleSetting;

    // Title
    private BooleanSetting showTitleSetting;
    private StringSetting customTitleSetting;
    private ColorSetting titleColorSetting;
    private NumberSetting titleScaleSetting;
    private EnumSetting<TextAlignment> titleAlignmentSetting;
    private BooleanSetting titleShadowSetting;
    private BooleanSetting titleBgSetting;
    private NumberSetting titlePaddingSetting;

    // Lines
    private BooleanSetting showLinesSetting;
    private BooleanSetting showScoresSetting;
    private BooleanSetting showPrefixesSetting;
    private BooleanSetting showSuffixesSetting;
    private NumberSetting lineSpacingSetting;
    private EnumSetting<TextAlignment> lineAlignmentSetting;
    private NumberSetting lineScaleSetting;
    private BooleanSetting lineShadowSetting;
    private BooleanSetting lineBgSetting;
    private BooleanSetting alternatingBgSetting;

    // Advanced
    private NumberSetting maxLinesSetting;
    private BooleanSetting truncateLinesSetting;
    private NumberSetting maxLineLengthSetting;
    private BooleanSetting dynamicWidthSetting;
    private NumberSetting fixedWidthSetting;
    private BooleanSetting hideEmptyLinesSetting;
    private BooleanSetting hideEmptyScoresSetting;
    private NumberSetting borderThicknessSetting;
    private BooleanSetting showBorderSetting;

    // Colors
    private ColorSetting textColorSetting;
    private ColorSetting scoreColorSetting;
    private ColorSetting bgColorSetting;
    private ColorSetting altBgColorSetting;
    private ColorSetting borderColorSetting;

    // Layout
    private EnumSetting<HudAnchor> anchorSetting;
    private NumberSetting posXSetting;
    private NumberSetting posYSetting;
    private NumberSetting scaleSetting;

    public ScoreboardModule() {
        super(
                "scoreboard",
                "Scoreboard",
                "Rozbudowana personalizacja i stylizacja standardowej tablicy wyników (scoreboard) Minecraft.",
                ModuleCategory.HUD
        );
        this.model = new HudComponentModel("scoreboard", 140, 160, HudAnchor.TOP_RIGHT);
        this.model.setRenderPriority(HudPriorityResolver.PRIORITY_HIGH);
        this.model.setPosX(4);
        this.model.setPosY(80);
        initSettings();
        instance = this;
    }

    public static ScoreboardModule getInstance() {
        return instance;
    }

    /**
     * Helper for loader mixins/events to check if custom renderer is active and taking over vanilla scoreboard.
     */
    public static boolean shouldOverrideVanilla() {
        if (instance == null || !instance.isEnabled() || instance.hasError()) {
            return false;
        }
        if (!instance.useCustomRendererSetting.getValue()) {
            return false;
        }
        if (instance.previewModeSetting.getValue()) {
            return true;
        }
        IScoreboardDataProvider provider = ScoreboardDataProviderRegistry.getInstance().getProvider();
        return provider != null && provider.hasActiveSidebarObjective();
    }

    protected void initSettings() {
        // --- Grupa General ---
        SettingGroup general = new SettingGroup("general", "Ogólne");
        useCustomRendererSetting = new BooleanSetting(
                "use_custom_renderer",
                "Niestandardowy Renderer",
                "Przejmuje renderowanie vanilla scoreboardu na rzecz ZombekClient Custom Scoreboard",
                true
        );
        previewModeSetting = new BooleanSetting(
                "preview_mode",
                "Tryb Podglądu (Preview)",
                "Prezentuje przykładowe dane w celach konfiguracyjnych, gdy brak aktywnego scoreboardu",
                false
        );
        styleSetting = new EnumSetting<>(
                "scoreboard_style",
                "Styl Scoreboardu",
                "Wybór motywu wizualnego tablicy wyników",
                ScoreboardStyle.MODERN
        );
        general.addSetting(useCustomRendererSetting);
        general.addSetting(previewModeSetting);
        general.addSetting(styleSetting);
        registerGroup(general);

        // --- Grupa Title ---
        SettingGroup title = new SettingGroup("title", "Tytuł");
        showTitleSetting = new BooleanSetting("show_title", "Pokaż Tytuł", "Wyświetla nagłówek celu scoreboardu", true);
        customTitleSetting = new StringSetting("custom_title", "Własny Tytuł", "Zastępuje oryginalny tytuł lokalnym tekstem (pozostaw puste dla domyślnego)", "");
        titleColorSetting = new ColorSetting("title_color", "Kolor Tytułu", "Kolor czcionki nagłówka", 0xFFFFFFFF);
        titleScaleSetting = new NumberSetting("title_scale", "Skala Tytułu", "Współczynnik wielkości czcionki nagłówka", 1.0, 0.5, 2.0, 0.05);
        titleAlignmentSetting = new EnumSetting<>("title_alignment", "Wyrównanie Tytułu", "Pozycja pozioma nagłówka", TextAlignment.CENTER);
        titleShadowSetting = new BooleanSetting("title_shadow", "Cień Tytułu", "Renderowanie cienia pod nagłówkiem", true);
        titleBgSetting = new BooleanSetting("title_bg", "Tło Tytułu", "Rysowanie paska tła pod nagłówkiem", true);
        titlePaddingSetting = new NumberSetting("title_padding", "Margines Tytułu", "Odstęp wewnętrzny nagłówka", 3.0, 0.0, 10.0, 1.0);
        title.addSetting(showTitleSetting);
        title.addSetting(customTitleSetting);
        title.addSetting(titleColorSetting);
        title.addSetting(titleScaleSetting);
        title.addSetting(titleAlignmentSetting);
        title.addSetting(titleShadowSetting);
        title.addSetting(titleBgSetting);
        title.addSetting(titlePaddingSetting);
        registerGroup(title);

        // --- Grupa Lines ---
        SettingGroup lines = new SettingGroup("lines", "Linie");
        showLinesSetting = new BooleanSetting("show_lines", "Pokaż Linie", "Wyświetlanie zawartości wierszy scoreboardu", true);
        showScoresSetting = new BooleanSetting("show_scores", "Pokaż Wyniki (Numery)", "Wyświetlanie czerwonych liczb po prawej stronie", true);
        showPrefixesSetting = new BooleanSetting("show_prefixes", "Pokaż Prefiksy Drużyn", "Wyświetlanie prefiksów drużynowych wierszy", true);
        showSuffixesSetting = new BooleanSetting("show_suffixes", "Pokaż Sufiksy Drużyn", "Wyświetlanie sufiksów drużynowych wierszy", true);
        lineSpacingSetting = new NumberSetting("line_spacing", "Odstęp między Wierszami", "Pikselowy odstęp pionowy między liniami", 2.0, 0.0, 10.0, 1.0);
        lineAlignmentSetting = new EnumSetting<>("line_alignment", "Wyrównanie Wierszy", "Wyrównanie tekstu linii", TextAlignment.LEFT);
        lineScaleSetting = new NumberSetting("line_scale", "Skala Linii", "Wielkość czcionki wierszy", 1.0, 0.5, 2.0, 0.05);
        lineShadowSetting = new BooleanSetting("line_shadow", "Cień Linii", "Renderowanie cienia pod tekstem wierszy", true);
        lineBgSetting = new BooleanSetting("line_bg", "Tło Linii", "Renderowanie tła wierszy", true);
        alternatingBgSetting = new BooleanSetting("alternating_bg", "Naprzemienne Tło", "Subtelnie różne tło co drugiego wiersza", false);
        lines.addSetting(showLinesSetting);
        lines.addSetting(showScoresSetting);
        lines.addSetting(showPrefixesSetting);
        lines.addSetting(showSuffixesSetting);
        lines.addSetting(lineSpacingSetting);
        lines.addSetting(lineAlignmentSetting);
        lines.addSetting(lineScaleSetting);
        lines.addSetting(lineShadowSetting);
        lines.addSetting(lineBgSetting);
        lines.addSetting(alternatingBgSetting);
        registerGroup(lines);

        // --- Grupa Advanced ---
        SettingGroup advanced = new SettingGroup("advanced", "Zaawansowane");
        maxLinesSetting = new NumberSetting("max_lines", "Maksymalna Liczba Linii", "Limit wyświetlanych wierszy", 15.0, 1.0, 25.0, 1.0);
        truncateLinesSetting = new BooleanSetting("truncate_lines", "Obcinaj Długie Linie", "Skracanie wierszy przekraczających maksymalną długość", false);
        maxLineLengthSetting = new NumberSetting("max_line_length", "Maksymalna Długość Linii", "Maksymalna liczba znaków przed skróceniem", 32.0, 10.0, 80.0, 1.0);
        dynamicWidthSetting = new BooleanSetting("dynamic_width", "Dynamiczna Szerokość", "Automatyczne dopasowanie szerokości do najdłuższego napisu", true);
        fixedWidthSetting = new NumberSetting("fixed_width", "Stała Szerokość", "Szerokość scoreboardu przy wyłączonej szerokości dynamicznej", 140.0, 80.0, 300.0, 5.0);
        hideEmptyLinesSetting = new BooleanSetting("hide_empty_lines", "Ukryj Puste Linie", "Pomijanie całkowicie pustych linii", false);
        hideEmptyScoresSetting = new BooleanSetting("hide_empty_scores", "Ukryj Wyniki Równe 0", "Ukrywanie numeru wyniku, gdy wynosi 0", false);
        borderThicknessSetting = new NumberSetting("border_thickness", "Grubość Ramki", "Grubość obramowania w pikselach", 1.0, 0.0, 4.0, 1.0);
        showBorderSetting = new BooleanSetting("show_border", "Pokaż Ramkę", "Rysowanie obramowania panelu", true);
        advanced.addSetting(maxLinesSetting);
        advanced.addSetting(truncateLinesSetting);
        advanced.addSetting(maxLineLengthSetting);
        advanced.addSetting(dynamicWidthSetting);
        advanced.addSetting(fixedWidthSetting);
        advanced.addSetting(hideEmptyLinesSetting);
        advanced.addSetting(hideEmptyScoresSetting);
        advanced.addSetting(borderThicknessSetting);
        advanced.addSetting(showBorderSetting);
        registerGroup(advanced);

        // --- Grupa Colors ---
        SettingGroup colors = new SettingGroup("colors", "Kolory");
        textColorSetting = new ColorSetting("text_color", "Kolor Tekstu", "Domyślny kolor tekstu linii", 0xFFFFFFFF);
        scoreColorSetting = new ColorSetting("score_color", "Kolor Wyników", "Kolor numerów po prawej stronie", 0xFFFF5555);
        bgColorSetting = new ColorSetting("bg_color", "Kolor Tła", "Główny kolor i przezroczystość panelu", 0x60000000);
        altBgColorSetting = new ColorSetting("alt_bg_color", "Kolor Naprzemienny", "Kolor co drugiego wiersza w trybie naprzemiennym", 0x40000000);
        borderColorSetting = new ColorSetting("border_color", "Kolor Ramki", "Kolor obramowania panelu", 0x40FFFFFF);
        colors.addSetting(textColorSetting);
        colors.addSetting(scoreColorSetting);
        colors.addSetting(bgColorSetting);
        colors.addSetting(altBgColorSetting);
        colors.addSetting(borderColorSetting);
        registerGroup(colors);

        // --- Grupa Layout ---
        SettingGroup layout = new SettingGroup("layout", "Pozycja");
        anchorSetting = new EnumSetting<>("anchor", "Punkt Zakotwiczenia", "Kotwica ekranowa", HudAnchor.TOP_RIGHT);
        posXSetting = new NumberSetting("pos_x", "Pozycja X", "Przesunięcie w osi poziomej", 4.0, 0.0, 4000.0, 1.0);
        posYSetting = new NumberSetting("pos_y", "Pozycja Y", "Przesunięcie w osi pionowej", 80.0, 0.0, 4000.0, 1.0);
        scaleSetting = new NumberSetting("scale", "Skala Całości", "Współczynnik skali całego elementu", 1.0, 0.5, 2.0, 0.05);
        layout.addSetting(anchorSetting);
        layout.addSetting(posXSetting);
        layout.addSetting(posYSetting);
        layout.addSetting(scaleSetting);
        registerGroup(layout);
    }

    @Override
    public void onEnable() {
        HudElementRegistry.getInstance().register(this);
    }

    @Override
    public void onDisable() {
        HudElementRegistry.getInstance().unregister(this);
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
        if (!isVisible()) return;

        IScoreboardDataProvider provider = ScoreboardDataProviderRegistry.getInstance().getProvider();
        boolean isPreview = previewModeSetting.getValue();

        ScoreboardObjectiveData objective = null;
        if (provider != null && provider.hasActiveSidebarObjective()) {
            objective = provider.getSidebarObjective();
        }

        if (objective == null && isPreview) {
            FallbackScoreboardDataProvider fallback = new FallbackScoreboardDataProvider();
            fallback.setPreviewMode(true);
            objective = fallback.getSidebarObjective();
        }

        if (objective == null) {
            return;
        }

        model.setAnchor(anchorSetting.getValue());
        model.setPosX(posXSetting.getValue().intValue());
        model.setPosY(posYSetting.getValue().intValue());
        model.setScale(scaleSetting.getValue());

        CustomScoreboardRenderer.render(
                renderer,
                model,
                objective,
                styleSetting.getValue(),
                showTitleSetting.getValue(),
                customTitleSetting.getValue(),
                titleColorSetting.getValue(),
                titleScaleSetting.getValue().floatValue(),
                titleAlignmentSetting.getValue(),
                titleShadowSetting.getValue(),
                titleBgSetting.getValue(),
                titlePaddingSetting.getValue().intValue(),
                showLinesSetting.getValue(),
                showScoresSetting.getValue(),
                showPrefixesSetting.getValue(),
                showSuffixesSetting.getValue(),
                lineSpacingSetting.getValue().intValue(),
                lineAlignmentSetting.getValue(),
                lineScaleSetting.getValue().floatValue(),
                lineShadowSetting.getValue(),
                lineBgSetting.getValue(),
                alternatingBgSetting.getValue(),
                maxLinesSetting.getValue().intValue(),
                truncateLinesSetting.getValue(),
                maxLineLengthSetting.getValue().intValue(),
                dynamicWidthSetting.getValue(),
                fixedWidthSetting.getValue().intValue(),
                hideEmptyLinesSetting.getValue(),
                hideEmptyScoresSetting.getValue(),
                borderThicknessSetting.getValue().intValue(),
                showBorderSetting.getValue(),
                textColorSetting.getValue(),
                scoreColorSetting.getValue(),
                bgColorSetting.getValue(),
                altBgColorSetting.getValue(),
                borderColorSetting.getValue(),
                isPreview
        );
    }

    public String getStatusDescription() {
        IScoreboardDataProvider provider = ScoreboardDataProviderRegistry.getInstance().getProvider();
        if (provider != null && provider.hasActiveSidebarObjective()) {
            ScoreboardObjectiveData obj = provider.getSidebarObjective();
            return obj != null ? "Sidebar: Aktywny (" + obj.getTitle() + ")" : "Sidebar: Aktywny";
        }
        if (previewModeSetting.getValue()) {
            return "Sidebar: Podgląd (Preview)";
        }
        return "Sidebar: Brak aktywnego celu";
    }

    public HudComponentModel getModel() {
        return model;
    }

    public BooleanSetting getUseCustomRendererSetting() {
        return useCustomRendererSetting;
    }

    public BooleanSetting getPreviewModeSetting() {
        return previewModeSetting;
    }

    public EnumSetting<ScoreboardStyle> getStyleSetting() {
        return styleSetting;
    }

    public BooleanSetting getShowTitleSetting() {
        return showTitleSetting;
    }

    public StringSetting getCustomTitleSetting() {
        return customTitleSetting;
    }

    public BooleanSetting getShowLinesSetting() {
        return showLinesSetting;
    }

    public BooleanSetting getShowScoresSetting() {
        return showScoresSetting;
    }

    public NumberSetting getMaxLinesSetting() {
        return maxLinesSetting;
    }

    public BooleanSetting getTruncateLinesSetting() {
        return truncateLinesSetting;
    }

    public EnumSetting<HudAnchor> getAnchorSetting() {
        return anchorSetting;
    }

    public NumberSetting getPosXSetting() {
        return posXSetting;
    }

    public NumberSetting getPosYSetting() {
        return posYSetting;
    }

    public NumberSetting getScaleSetting() {
        return scaleSetting;
    }
}
