package pl.zombek.client.core.modules.pvp.tnt;

import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

/**
 * TNT Countdown Module.
 * Renders a dynamic in-world billboard countdown above active primed TNT entities.
 */
public class TntCountdownModule extends AbstractModule {
    private static TntCountdownModule instance;

    // General
    private final SettingGroup groupGeneral;
    private final BooleanSetting showCountdownSetting;
    private final BooleanSetting showPrefixSetting;
    private final BooleanSetting showDecimalPlacesSetting;
    private final NumberSetting decimalPrecisionSetting;
    private final EnumSetting<TntDisplayFormat> displayFormatSetting;
    private final NumberSetting fuseOffsetTicksSetting;

    // Appearance
    private final SettingGroup groupAppearance;
    private final BooleanSetting dynamicColorSetting;
    private final ColorSetting staticColorSetting;
    private final ColorSetting prefixColorSetting;
    private final ColorSetting textColorSetting;
    private final BooleanSetting backgroundSetting;
    private final BooleanSetting textShadowSetting;
    private final NumberSetting backgroundOpacitySetting;
    private final NumberSetting scaleSetting;
    private final NumberSetting textSizeSetting;

    // Visibility
    private final SettingGroup groupVisibility;
    private final NumberSetting maxDistanceSetting;
    private final BooleanSetting hideBehindBlocksSetting;
    private final BooleanSetting onlyVisibleSetting;
    private final BooleanSetting hideInGuiSetting;

    // Position
    private final SettingGroup groupPosition;
    private final NumberSetting verticalOffsetSetting;
    private final NumberSetting horizontalOffsetSetting;
    private final NumberSetting customScaleSetting;

    public TntCountdownModule() {
        super(
                "tnt_countdown",
                "TNT Countdown",
                "Wyświetla wskaźnik z odliczaniem czasu do wybuchu nad aktywnym Primed TNT w świecie gry.",
                ModuleCategory.PVP
        );
        instance = this;

        // General
        this.groupGeneral = new SettingGroup("general", "General");
        this.showCountdownSetting = new BooleanSetting(
                "show_countdown", "Show TNT Countdown",
                "Włącza renderowanie odliczania nad TNT.", true
        );
        this.showPrefixSetting = new BooleanSetting(
                "show_prefix", "Show Prefix",
                "Wyświetla prefiks 'TNT' przed czasem.", true
        );
        this.showDecimalPlacesSetting = new BooleanSetting(
                "show_decimal_places", "Show Decimal Places",
                "Wyświetla części dziesiętne sekundy.", true
        );
        this.decimalPrecisionSetting = new NumberSetting(
                "decimal_precision", "Decimal Precision",
                "Liczba miejsc po przecinku (1-3).", 1.0, 1.0, 3.0, 1.0
        );
        this.displayFormatSetting = new EnumSetting<>(
                "display_format", "Display Format",
                "Format wyświetlanego tekstu.", TntDisplayFormat.PREFIX_AND_TIME
        );
        this.fuseOffsetTicksSetting = new NumberSetting(
                "fuse_offset_ticks", "Display Offset in Ticks",
                "Wizualne przesunięcie czasu zapalnika w tickach (nie modyfikuje serwera).", 0.0, -20.0, 20.0, 1.0
        );
        this.groupGeneral.addSetting(showCountdownSetting);
        this.groupGeneral.addSetting(showPrefixSetting);
        this.groupGeneral.addSetting(showDecimalPlacesSetting);
        this.groupGeneral.addSetting(decimalPrecisionSetting);
        this.groupGeneral.addSetting(displayFormatSetting);
        this.groupGeneral.addSetting(fuseOffsetTicksSetting);
        registerGroup(this.groupGeneral);

        // Appearance
        this.groupAppearance = new SettingGroup("appearance", "Appearance & Colors");
        this.dynamicColorSetting = new BooleanSetting(
                "dynamic_color", "Dynamic Color",
                "Zmienia kolor w zależności od czasu (zielony -> żółty -> czerwony).", true
        );
        this.staticColorSetting = new ColorSetting(
                "static_color", "Static Countdown Color",
                "Statyczny kolor tekstu odliczania.", 0xFFFFFFFF
        );
        this.prefixColorSetting = new ColorSetting(
                "prefix_color", "Prefix Color",
                "Kolor prefiksu 'TNT'.", 0xFFFF5555
        );
        this.textColorSetting = new ColorSetting(
                "text_color", "Text Color",
                "Bazowy kolor etykiety.", 0xFFFFFFFF
        );
        this.backgroundSetting = new BooleanSetting(
                "background", "Background",
                "Rysuj półprzezroczyste tło pod etykietą.", true
        );
        this.textShadowSetting = new BooleanSetting(
                "text_shadow", "Text Shadow",
                "Renderuj cień tekstu.", true
        );
        this.backgroundOpacitySetting = new NumberSetting(
                "background_opacity", "Background Opacity",
                "Przezroczystość tła (0-100%).", 60.0, 0.0, 100.0, 1.0
        );
        this.scaleSetting = new NumberSetting(
                "scale", "Scale",
                "Globalna skala etykiety.", 1.0, 0.5, 3.0, 0.1
        );
        this.textSizeSetting = new NumberSetting(
                "text_size", "Text Size",
                "Względny rozmiar czcionki.", 1.0, 0.5, 2.0, 0.1
        );
        this.groupAppearance.addSetting(dynamicColorSetting);
        this.groupAppearance.addSetting(staticColorSetting);
        this.groupAppearance.addSetting(prefixColorSetting);
        this.groupAppearance.addSetting(textColorSetting);
        this.groupAppearance.addSetting(backgroundSetting);
        this.groupAppearance.addSetting(textShadowSetting);
        this.groupAppearance.addSetting(backgroundOpacitySetting);
        this.groupAppearance.addSetting(scaleSetting);
        this.groupAppearance.addSetting(textSizeSetting);
        registerGroup(this.groupAppearance);

        // Visibility
        this.groupVisibility = new SettingGroup("visibility", "Visibility & Culling");
        this.maxDistanceSetting = new NumberSetting(
                "max_distance", "Maximum Render Distance",
                "Maksymalny zasięg widoczności odliczania w blokach.", 48.0, 4.0, 128.0, 2.0
        );
        this.hideBehindBlocksSetting = new BooleanSetting(
                "hide_behind_blocks", "Hide Behind Blocks",
                "Ukryj odliczanie, gdy TNT jest całkowicie zasłonięte przez bloki.", false
        );
        this.onlyVisibleSetting = new BooleanSetting(
                "only_visible", "Only Show Visible TNT",
                "Renderuj odliczanie wyłącznie dla załadowanych encji.", true
        );
        this.hideInGuiSetting = new BooleanSetting(
                "hide_in_gui", "Hide When GUI Open",
                "Ukryj wskaźnik przy otwartym menu gry.", false
        );
        this.groupVisibility.addSetting(maxDistanceSetting);
        this.groupVisibility.addSetting(hideBehindBlocksSetting);
        this.groupVisibility.addSetting(onlyVisibleSetting);
        this.groupVisibility.addSetting(hideInGuiSetting);
        registerGroup(this.groupVisibility);

        // Position
        this.groupPosition = new SettingGroup("position", "In-World Position Offset");
        this.verticalOffsetSetting = new NumberSetting(
                "vertical_offset", "Vertical Offset",
                "Wysokość etykiety nad środkiem TNT (bloki).", 0.75, 0.0, 3.0, 0.05
        );
        this.horizontalOffsetSetting = new NumberSetting(
                "horizontal_offset", "Horizontal Offset",
                "Przesunięcie poziome etykiety.", 0.0, -2.0, 2.0, 0.05
        );
        this.customScaleSetting = new NumberSetting(
                "custom_scale", "Custom Scale",
                "Mnożnik skali perspektywicznej w świecie.", 1.0, 0.5, 2.0, 0.05
        );
        this.groupPosition.addSetting(verticalOffsetSetting);
        this.groupPosition.addSetting(horizontalOffsetSetting);
        this.groupPosition.addSetting(customScaleSetting);
        registerGroup(this.groupPosition);
    }

    public static TntCountdownModule getInstance() {
        return instance;
    }

    @Override
    public void onInitialize() {}

    @Override
    public void onEnable() {
        TntTracker.getInstance().clear();
    }

    @Override
    public void onDisable() {
        TntTracker.getInstance().clear();
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        // Prune entries not updated for over 500ms
        TntTracker.getInstance().pruneStale(500L);
    }

    // Getters for settings
    public BooleanSetting getShowCountdownSetting() { return showCountdownSetting; }
    public BooleanSetting getShowPrefixSetting() { return showPrefixSetting; }
    public BooleanSetting getShowDecimalPlacesSetting() { return showDecimalPlacesSetting; }
    public NumberSetting getDecimalPrecisionSetting() { return decimalPrecisionSetting; }
    public EnumSetting<TntDisplayFormat> getDisplayFormatSetting() { return displayFormatSetting; }
    public NumberSetting getFuseOffsetTicksSetting() { return fuseOffsetTicksSetting; }

    public BooleanSetting getDynamicColorSetting() { return dynamicColorSetting; }
    public ColorSetting getStaticColorSetting() { return staticColorSetting; }
    public ColorSetting getPrefixColorSetting() { return prefixColorSetting; }
    public ColorSetting getTextColorSetting() { return textColorSetting; }
    public BooleanSetting getBackgroundSetting() { return backgroundSetting; }
    public BooleanSetting getTextShadowSetting() { return textShadowSetting; }
    public NumberSetting getBackgroundOpacitySetting() { return backgroundOpacitySetting; }
    public NumberSetting getScaleSetting() { return scaleSetting; }
    public NumberSetting getTextSizeSetting() { return textSizeSetting; }

    public NumberSetting getMaxDistanceSetting() { return maxDistanceSetting; }
    public BooleanSetting getHideBehindBlocksSetting() { return hideBehindBlocksSetting; }
    public BooleanSetting getOnlyVisibleSetting() { return onlyVisibleSetting; }
    public BooleanSetting getHideInGuiSetting() { return hideInGuiSetting; }

    public NumberSetting getVerticalOffsetSetting() { return verticalOffsetSetting; }
    public NumberSetting getHorizontalOffsetSetting() { return horizontalOffsetSetting; }
    public NumberSetting getCustomScaleSetting() { return customScaleSetting; }
}
