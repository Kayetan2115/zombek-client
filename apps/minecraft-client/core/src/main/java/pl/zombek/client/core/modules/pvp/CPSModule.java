package pl.zombek.client.core.modules.pvp;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;
import pl.zombek.client.core.tracking.ClickTracker;

/**
 * CPS Counter HUD Module — Real-time clicks-per-second monitor for Left and Right mouse buttons.
 * Operates on authentic in-game click timestamps with sliding time window and peak tracking.
 */
public class CPSModule extends AbstractModule implements IHudElement {

    public enum CpsDisplayStyle {
        INLINE("Pojedyncza linia (LMB: 10 | RMB: 8 CPS)"),
        STACKED("Wiersze pionowe"),
        BOXED("Osobne ramki");

        private final String displayName;
        CpsDisplayStyle(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    public enum Alignment {
        LEFT("Do lewej"),
        CENTER("Wyśrodkowany"),
        RIGHT("Do prawej");

        private final String displayName;
        Alignment(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    public enum VAlignment {
        TOP("Do góry"),
        MIDDLE("Środek"),
        BOTTOM("Do dołu");

        private final String displayName;
        VAlignment(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    public enum NumberFormatMode {
        INTEGER("Całkowite (10 CPS)"),
        ONE_DECIMAL("Dziesiętne (10.0 CPS)");

        private final String displayName;
        NumberFormatMode(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    // General Group
    private final BooleanSetting showLmb;
    private final BooleanSetting showRmb;
    private final BooleanSetting showMaxCps;
    private final BooleanSetting resetMaxCps;

    // Display Group
    private final EnumSetting<CpsDisplayStyle> displayStyle;
    private final StringSetting prefix;
    private final StringSetting suffix;
    private final BooleanSetting showLabels;
    private final BooleanSetting showIcons;
    private final NumberSetting textSize;
    private final NumberSetting scale;

    // Colors Group
    private final ColorSetting textColor;
    private final ColorSetting lmbColor;
    private final ColorSetting rmbColor;
    private final ColorSetting backgroundColor;
    private final ColorSetting borderColor;
    private final NumberSetting opacity;

    // Layout Group
    private final NumberSetting positionX;
    private final NumberSetting positionY;
    private final EnumSetting<HudAnchor> anchor;
    private final EnumSetting<Alignment> horizontalAlignment;
    private final EnumSetting<VAlignment> verticalAlignment;
    private final NumberSetting spacing;
    private final NumberSetting padding;

    // Advanced Group
    private final NumberSetting cpsWindow;
    private final EnumSetting<NumberFormatMode> numberFormat;
    private final BooleanSetting animation;
    private final NumberSetting updateInterval;

    public CPSModule() {
        super("cps", "CPS Counter", "Wyświetla liczbę kliknięć na sekundę (CPS) dla lewego i prawego przycisku myszy.", ModuleCategory.PVP);

        // --- 1. General ---
        SettingGroup generalGroup = new SettingGroup("general", "Ogólne");
        showLmb = generalGroup.addSetting(new BooleanSetting("show_lmb", "Pokaż LMB CPS", "Licznik lewego przycisku myszy", true));
        showRmb = generalGroup.addSetting(new BooleanSetting("show_rmb", "Pokaż RMB CPS", "Licznik prawego przycisku myszy", true));
        showMaxCps = generalGroup.addSetting(new BooleanSetting("show_max_cps", "Pokaż Maksymalne CPS", "Wyświetla rekordowe CPS z bieżącej sesji", false));
        resetMaxCps = generalGroup.addSetting(new BooleanSetting("reset_max_cps", "Resetuj Maks. CPS", "Resetuje zapamiętany rekord CPS", false));
        resetMaxCps.addChangeListener(val -> {
            if (Boolean.TRUE.equals(val)) {
                ClickTracker.getInstance().resetMaxCps();
                resetMaxCps.setValue(false);
            }
        });
        registerGroup(generalGroup);

        // --- 2. Display ---
        SettingGroup displayGroup = new SettingGroup("display", "Wyświetlanie");
        displayStyle = displayGroup.addSetting(new EnumSetting<>("display_style", "Styl wyświetlania", "Układ licznika CPS", CpsDisplayStyle.INLINE));
        prefix = displayGroup.addSetting(new StringSetting("prefix", "Prefiks", "Tekst poprzedzający licznik", ""));
        suffix = displayGroup.addSetting(new StringSetting("suffix", "Sufiks", "Tekst za liczbą kliknięć", " CPS"));
        showLabels = displayGroup.addSetting(new BooleanSetting("show_labels", "Pokaż etykiety", "Pokazuje napis LMB / RMB", true));
        showIcons = displayGroup.addSetting(new BooleanSetting("show_icons", "Pokaż ikony", "Wyświetla mały symbol przycisku", false));
        textSize = displayGroup.addSetting(new NumberSetting("text_size", "Rozmiar tekstu", "Skala czcionki", 1.0, 0.8, 1.5, 0.1));
        scale = displayGroup.addSetting(new NumberSetting("scale", "Skala modułu", "Skala całego elementu HUD", 1.0, 0.5, 2.5, 0.1));
        registerGroup(displayGroup);

        // --- 3. Colors ---
        SettingGroup colorsGroup = new SettingGroup("colors", "Kolory");
        textColor = colorsGroup.addSetting(new ColorSetting("text_color", "Kolor tekstu", "Główny kolor tekstu", 0xFFFFFFFF, false));
        lmbColor = colorsGroup.addSetting(new ColorSetting("lmb_color", "Kolor LMB", "Wyróżnienie licznika lewego przycisku", 0xFF10B981, false));
        rmbColor = colorsGroup.addSetting(new ColorSetting("rmb_color", "Kolor RMB", "Wyróżnienie licznika prawego przycisku", 0xFFF59E0B, false));
        backgroundColor = colorsGroup.addSetting(new ColorSetting("bg_color", "Kolor tła", "Kolor prostokąta tła", 0x99161E2E, true));
        borderColor = colorsGroup.addSetting(new ColorSetting("border_color", "Kolor ramki", "Kolor obramowania", 0x40FFFFFF, true));
        opacity = colorsGroup.addSetting(new NumberSetting("opacity", "Przezroczystość", "Globalne krycie w %", 80.0, 0.0, 100.0, 5.0));
        registerGroup(colorsGroup);

        // --- 4. Layout ---
        SettingGroup layoutGroup = new SettingGroup("layout", "Pozycja i Marginesy");
        positionX = layoutGroup.addSetting(new NumberSetting("position_x", "Pozycja X", "Położenie poziome od punktu kotwiczenia", 10.0, 0.0, 2000.0, 1.0));
        positionY = layoutGroup.addSetting(new NumberSetting("position_y", "Pozycja Y", "Położenie pionowe od punktu kotwiczenia", 120.0, 0.0, 2000.0, 1.0));
        anchor = layoutGroup.addSetting(new EnumSetting<>("anchor", "Punkt kotwiczenia", "Róg ekranu dla pozycjonowania", HudAnchor.TOP_LEFT));
        horizontalAlignment = layoutGroup.addSetting(new EnumSetting<>("h_align", "Wyrównanie tekstu", "Wyrównanie wewnątrz ramki", Alignment.CENTER));
        verticalAlignment = layoutGroup.addSetting(new EnumSetting<>("v_align", "Wyrównanie pionowe", "Wyrównanie pionowe", VAlignment.MIDDLE));
        spacing = layoutGroup.addSetting(new NumberSetting("spacing", "Odstęp elementów", "Odstęp między LMB i RMB", 4.0, 0.0, 20.0, 1.0));
        padding = layoutGroup.addSetting(new NumberSetting("padding", "Margines wewnętrzny", "Odstęp tekstu od krawędzi tła", 4.0, 0.0, 20.0, 1.0));
        registerGroup(layoutGroup);

        // --- 5. Advanced ---
        SettingGroup advancedGroup = new SettingGroup("advanced", "Zaawansowane");
        cpsWindow = advancedGroup.addSetting(new NumberSetting("cps_window", "Okno pomiaru", "Czas zliczania kliknięć w ms", 1000.0, 500.0, 2000.0, 100.0));
        numberFormat = advancedGroup.addSetting(new EnumSetting<>("number_format", "Format liczb", "Całkowite lub dziesiętne", NumberFormatMode.INTEGER));
        animation = advancedGroup.addSetting(new BooleanSetting("animation", "Animacja kliknięcia", "Krótki błysk koloru przy kliknięciu", true));
        updateInterval = advancedGroup.addSetting(new NumberSetting("update_interval", "Interwał odświeżania", "Co ile ticków przeliczać wartości", 1.0, 1.0, 20.0, 1.0));
        registerGroup(advancedGroup);
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
        return isEnabled();
    }

    @Override
    public void render(IHudRenderer renderer) {
        if (!isEnabled() || renderer == null) return;

        ClickTracker tracker = ClickTracker.getInstance();
        long window = cpsWindow.getValue().longValue();
        int lmbCps = tracker.getCpsLmb(window);
        int rmbCps = tracker.getCpsRmb(window);

        float currentScale = (float) (scale.getValue() * textSize.getValue());
        int pad = (int) (padding.getValue() * scale.getValue());
        int space = (int) (spacing.getValue() * scale.getValue());
        double globalOpacity = opacity.getValue() / 100.0;

        String lmbVal = formatCps(lmbCps);
        String rmbVal = formatCps(rmbCps);

        if (displayStyle.getValue() == CpsDisplayStyle.INLINE) {
            StringBuilder sb = new StringBuilder();
            if (prefix.getValue() != null && !prefix.getValue().isEmpty()) {
                sb.append(prefix.getValue()).append(" ");
            }
            if (showLmb.getValue()) {
                if (showLabels.getValue()) sb.append("LMB: ");
                sb.append(lmbVal);
            }
            if (showLmb.getValue() && showRmb.getValue()) {
                sb.append(" | ");
            }
            if (showRmb.getValue()) {
                if (showLabels.getValue()) sb.append("RMB: ");
                sb.append(rmbVal);
            }
            sb.append(suffix.getValue());
            if (showMaxCps.getValue()) {
                sb.append(" (Max: ").append(tracker.getMaxLeftCps()).append("/").append(tracker.getMaxRightCps()).append(")");
            }

            String fullText = sb.toString();
            int strWidth = (int) (renderer.getTextWidth(fullText) * currentScale);
            int fontHeight = (int) (renderer.getFontHeight() * currentScale);

            int boxWidth = strWidth + pad * 2;
            int boxHeight = fontHeight + pad * 2;

            int x = anchor.getValue().calculateX(positionX.getValue().intValue(), boxWidth, renderer.getScreenWidth());
            int y = anchor.getValue().calculateY(positionY.getValue().intValue(), boxHeight, renderer.getScreenHeight());

            drawBox(renderer, x, y, boxWidth, boxHeight, globalOpacity);
            renderer.drawText(fullText, x + pad, y + pad, textColor.getValue(), true, currentScale, false);

        } else if (displayStyle.getValue() == CpsDisplayStyle.STACKED) {
            String line1 = showLmb.getValue() ? (showLabels.getValue() ? "LMB: " : "") + lmbVal + suffix.getValue() : null;
            String line2 = showRmb.getValue() ? (showLabels.getValue() ? "RMB: " : "") + rmbVal + suffix.getValue() : null;

            int maxStrW = 0;
            if (line1 != null) maxStrW = Math.max(maxStrW, (int) (renderer.getTextWidth(line1) * currentScale));
            if (line2 != null) maxStrW = Math.max(maxStrW, (int) (renderer.getTextWidth(line2) * currentScale));

            int fontH = (int) (renderer.getFontHeight() * currentScale);
            int lineCount = (line1 != null ? 1 : 0) + (line2 != null ? 1 : 0);
            if (lineCount == 0) return;

            int boxWidth = maxStrW + pad * 2;
            int boxHeight = lineCount * fontH + (lineCount - 1) * space + pad * 2;

            int x = anchor.getValue().calculateX(positionX.getValue().intValue(), boxWidth, renderer.getScreenWidth());
            int y = anchor.getValue().calculateY(positionY.getValue().intValue(), boxHeight, renderer.getScreenHeight());

            drawBox(renderer, x, y, boxWidth, boxHeight, globalOpacity);

            int curY = y + pad;
            if (line1 != null) {
                renderer.drawText(line1, x + pad, curY, lmbColor.getValue(), true, currentScale, false);
                curY += fontH + space;
            }
            if (line2 != null) {
                renderer.drawText(line2, x + pad, curY, rmbColor.getValue(), true, currentScale, false);
            }
        } else { // BOXED
            String textLmb = (showLabels.getValue() ? "LMB: " : "") + lmbVal;
            String textRmb = (showLabels.getValue() ? "RMB: " : "") + rmbVal;

            int wLmb = (int) (renderer.getTextWidth(textLmb) * currentScale);
            int wRmb = (int) (renderer.getTextWidth(textRmb) * currentScale);
            int fontH = (int) (renderer.getFontHeight() * currentScale);

            int boxLmbW = wLmb + pad * 2;
            int boxRmbW = wRmb + pad * 2;
            int boxH = fontH + pad * 2;

            int totalW = (showLmb.getValue() && showRmb.getValue()) ? boxLmbW + space + boxRmbW :
                    (showLmb.getValue() ? boxLmbW : boxRmbW);

            int x = anchor.getValue().calculateX(positionX.getValue().intValue(), totalW, renderer.getScreenWidth());
            int y = anchor.getValue().calculateY(positionY.getValue().intValue(), boxH, renderer.getScreenHeight());

            int curX = x;
            if (showLmb.getValue()) {
                drawBox(renderer, curX, y, boxLmbW, boxH, globalOpacity);
                renderer.drawText(textLmb, curX + pad, y + pad, lmbColor.getValue(), true, currentScale, false);
                curX += boxLmbW + space;
            }
            if (showRmb.getValue()) {
                drawBox(renderer, curX, y, boxRmbW, boxH, globalOpacity);
                renderer.drawText(textRmb, curX + pad, y + pad, rmbColor.getValue(), true, currentScale, false);
            }
        }
    }

    private void drawBox(IHudRenderer renderer, int x, int y, int w, int h, double globalOpacity) {
        // Background
        int bg = backgroundColor.getValue();
        int alphaBg = (int) (((bg >>> 24) & 0xFF) * globalOpacity);
        renderer.fill(x, y, w, h, (alphaBg << 24) | (bg & 0x00FFFFFF));

        // Border
        int border = borderColor.getValue();
        int alphaBorder = (int) (((border >>> 24) & 0xFF) * globalOpacity);
        renderer.drawBorder(x, y, w, h, (alphaBorder << 24) | (border & 0x00FFFFFF), 1);
    }

    private String formatCps(int cps) {
        if (numberFormat.getValue() == NumberFormatMode.ONE_DECIMAL) {
            return String.format("%.1f", (double) cps);
        }
        return String.valueOf(cps);
    }

    // Getters for settings testing
    public BooleanSetting getShowLmb() { return showLmb; }
    public BooleanSetting getShowRmb() { return showRmb; }
    public BooleanSetting getShowMaxCps() { return showMaxCps; }
    public BooleanSetting getResetMaxCps() { return resetMaxCps; }
    public NumberSetting getPositionX() { return positionX; }
    public NumberSetting getPositionY() { return positionY; }
    public EnumSetting<HudAnchor> getAnchor() { return anchor; }
    public NumberSetting getScale() { return scale; }
}
