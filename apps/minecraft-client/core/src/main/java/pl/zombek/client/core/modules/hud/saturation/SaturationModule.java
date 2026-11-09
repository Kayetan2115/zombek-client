package pl.zombek.client.core.modules.hud.saturation;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.component.HudComponentModel;
import pl.zombek.client.core.hud.component.HudComponentRenderer;
import pl.zombek.client.core.hud.component.HudComponentStyle;
import pl.zombek.client.core.hud.component.HudPriorityResolver;
import pl.zombek.client.core.hud.saturation.*;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

/**
 * Saturation Module displaying player hunger, saturation, and exhaustion levels.
 * Honors strict data availability: when saturation is not synced by the server,
 * it displays 'Unavailable' and never fabricates or guesses values.
 */
public class SaturationModule extends AbstractModule implements IHudElement {

    private static SaturationModule instance;

    private final HudComponentModel model;

    // General
    private BooleanSetting showHungerSetting;
    private BooleanSetting showSaturationSetting;
    private BooleanSetting showExhaustionSetting;
    private BooleanSetting showNumericValuesSetting;
    private BooleanSetting previewModeSetting;

    // Hunger
    private BooleanSetting showHungerBarSetting;
    private BooleanSetting highlightLowHungerSetting;
    private NumberSetting lowHungerThresholdSetting;

    // Saturation
    private BooleanSetting showSaturationBarSetting;
    private NumberSetting saturationPrecisionSetting;
    private NumberSetting saturationWarningThresholdSetting;

    // Appearance
    private EnumSetting<SaturationLayoutMode> layoutModeSetting;
    private NumberSetting barWidthSetting;
    private NumberSetting barHeightSetting;
    private EnumSetting<HudComponentStyle> styleSetting;

    // Colors
    private ColorSetting hungerColorSetting;
    private ColorSetting saturationColorSetting;
    private ColorSetting exhaustionColorSetting;
    private ColorSetting warningColorSetting;
    private ColorSetting textColorSetting;
    private ColorSetting bgColorSetting;

    // Layout
    private EnumSetting<HudAnchor> anchorSetting;
    private NumberSetting posXSetting;
    private NumberSetting posYSetting;
    private NumberSetting scaleSetting;

    public SaturationModule() {
        super(
                "saturation",
                "Saturation",
                "Wyświetlanie poziomu najedzenia (Hunger), nasycenia (Saturation) i zmęczenia (Exhaustion) gracza.",
                ModuleCategory.HUD
        );
        this.model = new HudComponentModel("saturation", 110, 36, HudAnchor.BOTTOM_RIGHT);
        this.model.setRenderPriority(HudPriorityResolver.PRIORITY_STANDARD);
        this.model.setPosX(4);
        this.model.setPosY(24);
        initSettings();
        instance = this;
    }

    public static SaturationModule getInstance() {
        return instance;
    }

    protected void initSettings() {
        // --- Grupa General ---
        SettingGroup general = new SettingGroup("general", "Ogólne");
        showHungerSetting = new BooleanSetting("show_hunger", "Pokaż Głód (Hunger)", "Wyświetla punkty najedzenia gracza", true);
        showSaturationSetting = new BooleanSetting("show_saturation", "Pokaż Nasycenie (Saturation)", "Wyświetla punkty nasycenia gracza", true);
        showExhaustionSetting = new BooleanSetting("show_exhaustion", "Pokaż Zmęczenie (Exhaustion)", "Wyświetla poziom zmęczenia pokarmowego", false);
        showNumericValuesSetting = new BooleanSetting("show_numeric", "Wartości Liczbowe", "Pokazuje dokładne liczby obok pasków lub etykiet", true);
        previewModeSetting = new BooleanSetting("preview_mode", "Tryb Podglądu", "Pokazuje przykładowe dane w celach konfiguracji", false);
        general.addSetting(showHungerSetting);
        general.addSetting(showSaturationSetting);
        general.addSetting(showExhaustionSetting);
        general.addSetting(showNumericValuesSetting);
        general.addSetting(previewModeSetting);
        registerGroup(general);

        // --- Grupa Hunger ---
        SettingGroup hunger = new SettingGroup("hunger", "Głód");
        showHungerBarSetting = new BooleanSetting("show_hunger_bar", "Pasek Głodu", "Rysuje graficzny pasek poziomu głodu", true);
        highlightLowHungerSetting = new BooleanSetting("highlight_low_hunger", "Ostrzeżenie o Głodzie", "Zmienia kolor na alarmowy przy niskim najedzeniu", true);
        lowHungerThresholdSetting = new NumberSetting("low_hunger_threshold", "Próg Ostrzegawczy Głodu", "Limit punktów głodu aktywujący ostrzeżenie", 6.0, 1.0, 14.0, 1.0);
        hunger.addSetting(showHungerBarSetting);
        hunger.addSetting(highlightLowHungerSetting);
        hunger.addSetting(lowHungerThresholdSetting);
        registerGroup(hunger);

        // --- Grupa Saturation ---
        SettingGroup sat = new SettingGroup("saturation", "Nasycenie");
        showSaturationBarSetting = new BooleanSetting("show_saturation_bar", "Pasek Nasycenia", "Rysuje graficzny pasek nasycenia", true);
        saturationPrecisionSetting = new NumberSetting("saturation_precision", "Precyzja Miejsc po Przecinku", "Liczba miejsc po przecinku nasycenia", 1.0, 0.0, 2.0, 1.0);
        saturationWarningThresholdSetting = new NumberSetting("sat_warning_threshold", "Próg Ostrzegawczy Nasycenia", "Próg nasycenia aktywujący ostrzeżenie", 3.0, 0.0, 10.0, 1.0);
        sat.addSetting(showSaturationBarSetting);
        sat.addSetting(saturationPrecisionSetting);
        sat.addSetting(saturationWarningThresholdSetting);
        registerGroup(sat);

        // --- Grupa Appearance ---
        SettingGroup appearance = new SettingGroup("appearance", "Wygląd");
        layoutModeSetting = new EnumSetting<>("layout_mode", "Układ", "Format prezentacji danych", SaturationLayoutMode.COMPACT);
        barWidthSetting = new NumberSetting("bar_width", "Szerokość Pasków", "Szerokość pasków postępu w pikselach", 75.0, 40.0, 160.0, 5.0);
        barHeightSetting = new NumberSetting("bar_height", "Wysokość Pasków", "Wysokość pasków postępu w pikselach", 5.0, 3.0, 12.0, 1.0);
        styleSetting = new EnumSetting<>("hud_style", "Styl Tła", "Styl ramki i kontenera", HudComponentStyle.CLASSIC);
        appearance.addSetting(layoutModeSetting);
        appearance.addSetting(barWidthSetting);
        appearance.addSetting(barHeightSetting);
        appearance.addSetting(styleSetting);
        registerGroup(appearance);

        // --- Grupa Colors ---
        SettingGroup colors = new SettingGroup("colors", "Kolory");
        hungerColorSetting = new ColorSetting("hunger_color", "Kolor Głodu", "Kolor wskaźnika najedzenia", 0xFFFF9933);
        saturationColorSetting = new ColorSetting("saturation_color", "Kolor Nasycenia", "Kolor wskaźnika nasycenia", 0xFFFFD700);
        exhaustionColorSetting = new ColorSetting("exhaustion_color", "Kolor Zmęczenia", "Kolor wskaźnika zmęczenia", 0xFFAAAAAA);
        warningColorSetting = new ColorSetting("warning_color", "Kolor Ostrzeżenia", "Kolor alarmowy przy wyczerpaniu", 0xFFFF3333);
        textColorSetting = new ColorSetting("text_color", "Kolor Tekstu", "Domyślny kolor etykiet", 0xFFFFFFFF);
        bgColorSetting = new ColorSetting("bg_color", "Kolor Tła", "Kolor tła kontenera", 0x60000000);
        colors.addSetting(hungerColorSetting);
        colors.addSetting(saturationColorSetting);
        colors.addSetting(exhaustionColorSetting);
        colors.addSetting(warningColorSetting);
        colors.addSetting(textColorSetting);
        colors.addSetting(bgColorSetting);
        registerGroup(colors);

        // --- Grupa Layout ---
        SettingGroup layout = new SettingGroup("layout", "Pozycja");
        anchorSetting = new EnumSetting<>("anchor", "Punkt Zakotwiczenia", "Kotwica ekranowa", HudAnchor.BOTTOM_RIGHT);
        posXSetting = new NumberSetting("pos_x", "Pozycja X", "Przesunięcie w poziomie", 4.0, 0.0, 4000.0, 1.0);
        posYSetting = new NumberSetting("pos_y", "Pozycja Y", "Przesunięcie w pionie", 24.0, 0.0, 4000.0, 1.0);
        scaleSetting = new NumberSetting("scale", "Skala", "Współczynnik wielkości", 1.0, 0.5, 2.0, 0.05);
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

        ISaturationDataProvider provider = SaturationDataProviderRegistry.getInstance().getProvider();
        boolean isPreview = previewModeSetting.getValue();

        boolean inGame = provider != null && provider.isPlayerAvailable();
        if (!inGame && !isPreview) {
            return;
        }

        int food = inGame ? provider.getFoodLevel() : 18;
        boolean satAvail = inGame ? provider.isSaturationAvailable() : true;
        float sat = inGame && satAvail ? provider.getSaturationLevel() : (isPreview ? 12.5f : 0.0f);
        boolean exhAvail = inGame ? provider.isExhaustionAvailable() : true;
        float exh = inGame && exhAvail ? provider.getExhaustionLevel() : (isPreview ? 1.25f : 0.0f);

        int fontH = renderer.getFontHeight();
        SaturationLayoutMode layoutMode = layoutModeSetting.getValue();

        int barW = barWidthSetting.getValue().intValue();
        int barH = barHeightSetting.getValue().intValue();
        int prec = saturationPrecisionSetting.getValue().intValue();

        String satStr = satAvail ? formatFloat(sat, prec) : "Niedostępne";

        int width = 110;
        int height = 30;

        if (layoutMode == SaturationLayoutMode.DETAILED) {
            height = showExhaustionSetting.getValue() ? 48 : 38;
            width = Math.max(120, barW + 45);
        } else if (layoutMode == SaturationLayoutMode.MINIMAL) {
            height = 18;
            width = 95;
        } else if (showExhaustionSetting.getValue()) {
            height = 38;
        }

        model.setWidth(width);
        model.setHeight(height);
        model.setAnchor(anchorSetting.getValue());
        model.setPosX(posXSetting.getValue().intValue());
        model.setPosY(posYSetting.getValue().intValue());
        model.setScale(scaleSetting.getValue());

        int screenW = renderer.getScreenWidth();
        int screenH = renderer.getScreenHeight();
        int startX = model.getScreenX(screenW);
        int startY = model.getScreenY(screenH);

        HudComponentRenderer.renderBackground(
                renderer, startX, startY, width, height,
                styleSetting.getValue(), bgColorSetting.getValue(), 1.0, 0x40FFFFFF, true, 1
        );

        int padX = 4;
        int currentY = startY + 3;

        boolean lowHunger = highlightLowHungerSetting.getValue() && food <= lowHungerThresholdSetting.getValue().intValue();
        int hColor = lowHunger ? warningColorSetting.getValue() : hungerColorSetting.getValue();

        boolean lowSat = satAvail && sat <= saturationWarningThresholdSetting.getValue().floatValue();
        int sColor = lowSat ? warningColorSetting.getValue() : saturationColorSetting.getValue();

        switch (layoutMode) {
            case MINIMAL:
                String minText = String.format("Głód: %d | Nasycenie: %s", food, satStr);
                renderer.drawText(minText, startX + padX, currentY + (height - fontH) / 2, textColorSetting.getValue(), true, 0.9f, false);
                break;

            case DETAILED:
                // Hunger row with progress bar
                if (showHungerSetting.getValue()) {
                    renderer.drawText("Głód: " + food + "/20", startX + padX, currentY, hColor, true, 0.85f, true);
                    if (showHungerBarSetting.getValue()) {
                        int barX = startX + width - barW - padX;
                        renderer.fill(barX, currentY + 2, barW, barH, 0x50000000);
                        int fillW = (int) Math.round((food / 20.0) * barW);
                        renderer.fill(barX, currentY + 2, fillW, barH, hColor);
                    }
                    currentY += fontH + 3;
                }

                // Saturation row with progress bar
                if (showSaturationSetting.getValue()) {
                    renderer.drawText("Nasycenie: " + (satAvail ? satStr : "N/A"), startX + padX, currentY, sColor, true, 0.85f, true);
                    if (satAvail && showSaturationBarSetting.getValue()) {
                        int barX = startX + width - barW - padX;
                        renderer.fill(barX, currentY + 2, barW, barH, 0x50000000);
                        int fillW = (int) Math.round(Math.min(1.0f, sat / 20.0f) * barW);
                        renderer.fill(barX, currentY + 2, fillW, barH, sColor);
                    }
                    currentY += fontH + 3;
                }

                // Exhaustion row
                if (showExhaustionSetting.getValue() && exhAvail) {
                    String exhStr = formatFloat(exh, 1);
                    renderer.drawText("Zmęczenie: " + exhStr + "/4.0", startX + padX, currentY, exhaustionColorSetting.getValue(), true, 0.85f, true);
                    currentY += fontH + 3;
                }
                break;

            case COMPACT:
            case CLASSIC:
            default:
                if (showHungerSetting.getValue()) {
                    renderer.drawText("Głód: " + food + "/20", startX + padX, currentY, hColor, true, 0.9f, true);
                    currentY += fontH + 2;
                }
                if (showSaturationSetting.getValue()) {
                    renderer.drawText("Nasycenie: " + satStr, startX + padX, currentY, sColor, true, 0.9f, true);
                    currentY += fontH + 2;
                }
                if (showExhaustionSetting.getValue() && exhAvail) {
                    String exhStr = formatFloat(exh, 1);
                    renderer.drawText("Zmęczenie: " + exhStr, startX + padX, currentY, exhaustionColorSetting.getValue(), true, 0.85f, true);
                }
                break;
        }

        if (isPreview) {
            String badge = "§8[§cPREVIEW§8]";
            renderer.drawText(badge, startX + width - renderer.getTextWidth(badge) - 2, startY + height - fontH, 0xFFAAAAAA, true, 0.7f, false);
        }
    }

    private String formatFloat(float val, int precision) {
        if (precision <= 0) {
            return String.valueOf(Math.round(val));
        } else if (precision == 1) {
            return String.format("%.1f", val);
        } else {
            return String.format("%.2f", val);
        }
    }

    public String getStatusDescription() {
        ISaturationDataProvider provider = SaturationDataProviderRegistry.getInstance().getProvider();
        if (provider != null && provider.isPlayerAvailable()) {
            return provider.isSaturationAvailable() ? "Dane Nasycenia: Dostępne" : "Dane Nasycenia: Niedostępne (Serwer Vanilla)";
        }
        if (previewModeSetting.getValue()) {
            return "Status: Podgląd (Preview)";
        }
        return "Brak aktywnego gracza";
    }

    public HudComponentModel getModel() {
        return model;
    }

    public BooleanSetting getShowHungerSetting() {
        return showHungerSetting;
    }

    public BooleanSetting getShowSaturationSetting() {
        return showSaturationSetting;
    }

    public BooleanSetting getShowExhaustionSetting() {
        return showExhaustionSetting;
    }

    public BooleanSetting getShowNumericValuesSetting() {
        return showNumericValuesSetting;
    }

    public BooleanSetting getPreviewModeSetting() {
        return previewModeSetting;
    }

    public BooleanSetting getShowHungerBarSetting() {
        return showHungerBarSetting;
    }

    public BooleanSetting getHighlightLowHungerSetting() {
        return highlightLowHungerSetting;
    }

    public NumberSetting getLowHungerThresholdSetting() {
        return lowHungerThresholdSetting;
    }

    public BooleanSetting getShowSaturationBarSetting() {
        return showSaturationBarSetting;
    }

    public NumberSetting getSaturationPrecisionSetting() {
        return saturationPrecisionSetting;
    }

    public NumberSetting getSaturationWarningThresholdSetting() {
        return saturationWarningThresholdSetting;
    }

    public EnumSetting<SaturationLayoutMode> getLayoutModeSetting() {
        return layoutModeSetting;
    }

    public NumberSetting getBarWidthSetting() {
        return barWidthSetting;
    }

    public NumberSetting getBarHeightSetting() {
        return barHeightSetting;
    }

    public EnumSetting<HudComponentStyle> getStyleSetting() {
        return styleSetting;
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
