package pl.zombek.client.core.modules.pvp;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;
import pl.zombek.client.core.tracking.ComboTracker;

/**
 * Combo Counter HUD Module.
 * Displays consecutive hits landed on opponents with milestone color thresholds,
 * animations, best combo tracking, combo timer, and configurable reset policies.
 */
public class ComboCounterModule extends AbstractModule implements IHudElement {

    public enum DisplayStyle {
        STANDARD("Standardowy (COMBO: 3)"),
        COMPACT("Kompaktowy (3)"),
        BOXED("W ramce"),
        DETAILED("Szczegółowy (Combo + Best)");

        private final String displayName;
        DisplayStyle(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    public enum NumberFormatMode {
        RAW("3"),
        WITH_PREFIX("COMBO: 3"),
        WITH_SUFFIX("3 Hits"),
        FULL("COMBO: 3 Hits");

        private final String displayName;
        NumberFormatMode(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    // General Group
    private final BooleanSetting showLabel;
    private final BooleanSetting showCurrentCombo;
    private final BooleanSetting showBestCombo;
    private final BooleanSetting showTimer;

    // Display Group
    private final EnumSetting<DisplayStyle> displayStyle;
    private final EnumSetting<NumberFormatMode> formatMode;
    private final StringSetting prefix;
    private final StringSetting suffix;
    private final NumberSetting textSize;
    private final NumberSetting scale;

    // Colors Group
    private final ColorSetting textColor;
    private final ColorSetting counterColor;
    private final ColorSetting backgroundColor;
    private final ColorSetting borderColor;
    private final NumberSetting opacity;

    // Milestones Group
    private final BooleanSetting enableMilestones;
    private final NumberSetting tier1Threshold;
    private final NumberSetting tier2Threshold;
    private final NumberSetting tier3Threshold;
    private final NumberSetting tier4Threshold;
    private final ColorSetting tier1Color;
    private final ColorSetting tier2Color;
    private final ColorSetting tier3Color;
    private final ColorSetting tier4Color;

    // Animation Group
    private final BooleanSetting enableAnimation;
    private final NumberSetting scalePulse;
    private final NumberSetting animationDurationMs;

    // Behavior Group
    private final BooleanSetting resetOnTargetChange;
    private final BooleanSetting resetOnMiss;
    private final BooleanSetting resetOnDeath;
    private final NumberSetting resetTimeoutMs;

    // Layout Group
    private final NumberSetting positionX;
    private final NumberSetting positionY;
    private final EnumSetting<HudAnchor> anchor;
    private final NumberSetting padding;
    private final BooleanSetting showBackground;
    private final BooleanSetting showBorder;
    private final NumberSetting borderThickness;
    private final BooleanSetting showShadow;

    public ComboCounterModule() {
        super("combo_counter", "Combo Counter", "Wyświetla liczbę kolejnych skutecznych trafień w przeciwnika.", ModuleCategory.PVP);

        // --- 1. General ---
        SettingGroup generalGroup = new SettingGroup("general", "Ogólne");
        showLabel = generalGroup.addSetting(new BooleanSetting("show_label", "Pokaż etykietę", "Wyświetla napis COMBO", true));
        showCurrentCombo = generalGroup.addSetting(new BooleanSetting("show_current", "Pokaż bieżące combo", "Wyświetla aktualny licznik uderzeń", true));
        showBestCombo = generalGroup.addSetting(new BooleanSetting("show_best", "Pokaż rekord", "Wyświetla najwyższy wynik combo w sesji", false));
        showTimer = generalGroup.addSetting(new BooleanSetting("show_timer", "Pokaż czas trwania", "Wyświetla czas trwania aktywnej serii", false));
        registerGroup(generalGroup);

        // --- 2. Display ---
        SettingGroup displayGroup = new SettingGroup("display", "Prezentacja");
        displayStyle = displayGroup.addSetting(new EnumSetting<>("display_style", "Styl", "Format wizualny licznika", DisplayStyle.STANDARD));
        formatMode = displayGroup.addSetting(new EnumSetting<>("format_mode", "Format liczby", "Format etykiet i suffiksu", NumberFormatMode.FULL));
        prefix = displayGroup.addSetting(new StringSetting("prefix", "Prefiks", "Tekst przed liczbą", "COMBO: ", 20));
        suffix = displayGroup.addSetting(new StringSetting("suffix", "Sufiks", "Tekst za liczbą", " Hits", 20));
        textSize = displayGroup.addSetting(new NumberSetting("text_size", "Wielkość tekstu", "Rozmiar czcionki", 1.0, 0.8, 1.5, 0.1));
        scale = displayGroup.addSetting(new NumberSetting("scale", "Skala", "Globalne powiększenie modułu", 1.0, 0.5, 2.5, 0.1));
        registerGroup(displayGroup);

        // --- 3. Colors ---
        SettingGroup colorsGroup = new SettingGroup("colors", "Kolory");
        textColor = colorsGroup.addSetting(new ColorSetting("text_color", "Kolor etykiety", "Kolor napisu COMBO", 0xFFFFFFFF, false));
        counterColor = colorsGroup.addSetting(new ColorSetting("counter_color", "Domyślny kolor licznika", "Kolor liczby trafień", 0xFF10B981, false));
        backgroundColor = colorsGroup.addSetting(new ColorSetting("bg_color", "Kolor tła", "Tło modułu", 0x80000000, true));
        borderColor = colorsGroup.addSetting(new ColorSetting("border_color", "Kolor obramowania", "Obramowanie modułu", 0x40FFFFFF, true));
        opacity = colorsGroup.addSetting(new NumberSetting("opacity", "Przezroczystość", "Krycie tła w %", 80.0, 0.0, 100.0, 5.0));
        registerGroup(colorsGroup);

        // --- 4. Milestones ---
        SettingGroup milestonesGroup = new SettingGroup("milestones", "Wyróżnienia Trafień");
        enableMilestones = milestonesGroup.addSetting(new BooleanSetting("enable_milestones", "Włącz progi kolorów", "Zmienia kolor licznika przy wyższych seriach", true));
        tier1Threshold = milestonesGroup.addSetting(new NumberSetting("t1_threshold", "Próg 1 (początkowy)", "Początkowa seria trafień", 1.0, 1.0, 5.0, 1.0));
        tier2Threshold = milestonesGroup.addSetting(new NumberSetting("t2_threshold", "Próg 2 (dobry)", "Średnia seria trafień", 4.0, 2.0, 10.0, 1.0));
        tier3Threshold = milestonesGroup.addSetting(new NumberSetting("t3_threshold", "Próg 3 (wysoki)", "Wysoka seria trafień", 7.0, 5.0, 20.0, 1.0));
        tier4Threshold = milestonesGroup.addSetting(new NumberSetting("t4_threshold", "Próg 4 (mistrzowski)", "Mistrzowska seria trafień", 10.0, 8.0, 50.0, 1.0));
        tier1Color = milestonesGroup.addSetting(new ColorSetting("t1_color", "Kolor Progu 1", "Domyślny kolor 1-3 hits", 0xFFFFFFFF, false));
        tier2Color = milestonesGroup.addSetting(new ColorSetting("t2_color", "Kolor Progu 2", "Kolor 4-6 hits (Aqua)", 0xFF38BDF8, false));
        tier3Color = milestonesGroup.addSetting(new ColorSetting("t3_color", "Kolor Progu 3", "Kolor 7-9 hits (Złoty)", 0xFFF59E0B, false));
        tier4Color = milestonesGroup.addSetting(new ColorSetting("t4_color", "Kolor Progu 4", "Kolor 10+ hits (Ognisty Czerwony)", 0xFFEF4444, false));
        registerGroup(milestonesGroup);

        // --- 5. Animation ---
        SettingGroup animationGroup = new SettingGroup("animation", "Animacja Trafień");
        enableAnimation = animationGroup.addSetting(new BooleanSetting("enable_anim", "Animacja uderzenia", "Pulsacja licznika przy zarejestrowanym trafieniu", true));
        scalePulse = animationGroup.addSetting(new NumberSetting("pulse_scale", "Mnożnik pulsu", "Maksymalne powiększenie w ułamku sekundy", 1.25, 1.05, 1.5, 0.05));
        animationDurationMs = animationGroup.addSetting(new NumberSetting("anim_duration", "Czas trwania pulsu", "Czas trwania animacji w milisekundach", 150.0, 50.0, 500.0, 25.0, "ms"));
        registerGroup(animationGroup);

        // --- 6. Behavior ---
        SettingGroup behaviorGroup = new SettingGroup("behavior", "Zachowanie i Reset");
        resetOnTargetChange = behaviorGroup.addSetting(new BooleanSetting("reset_target_change", "Reset przy zmianie celu", "Zeruje combo po uderzeniu innego przeciwnika", true));
        resetOnMiss = behaviorGroup.addSetting(new BooleanSetting("reset_miss", "Reset przy pudle", "Zeruje combo po naciśnięciu ataku w powietrze", false));
        resetOnDeath = behaviorGroup.addSetting(new BooleanSetting("reset_death", "Reset po śmierci", "Zeruje licznik po zrespieniu gracza", true));
        resetTimeoutMs = behaviorGroup.addSetting(new NumberSetting("timeout_ms", "Czas wygaśnięcia", "Maksymalna przerwa między trafieniami przed resetem", 2000.0, 500.0, 5000.0, 100.0, "ms"));
        registerGroup(behaviorGroup);

        // --- 7. Layout ---
        SettingGroup layoutGroup = new SettingGroup("layout", "Układ i Pozycja");
        positionX = layoutGroup.addSetting(new NumberSetting("position_x", "Pozycja X", "Przesunięcie w poziomie od punktu kotwiczenia", 10.0, 0.0, 2000.0, 1.0));
        positionY = layoutGroup.addSetting(new NumberSetting("position_y", "Pozycja Y", "Przesunięcie w pionie od punktu kotwiczenia", 50.0, 0.0, 2000.0, 1.0));
        anchor = layoutGroup.addSetting(new EnumSetting<>("anchor", "Punkt kotwiczenia", "Róg ekranu dla pozycjonowania", HudAnchor.TOP_LEFT));
        padding = layoutGroup.addSetting(new NumberSetting("padding", "Margines wewnętrzny", "Wewnętrzny padding ramki", 4.0, 0.0, 20.0, 1.0));
        showBackground = layoutGroup.addSetting(new BooleanSetting("show_bg", "Pokaż tło", "Renderowanie tła prostokąta", true));
        showBorder = layoutGroup.addSetting(new BooleanSetting("show_border", "Pokaż obramowanie", "Renderowanie konturu ramki", true));
        borderThickness = layoutGroup.addSetting(new NumberSetting("border_thickness", "Grubość ramki", "Szerokość linii ramki w px", 1.0, 1.0, 4.0, 1.0));
        showShadow = layoutGroup.addSetting(new BooleanSetting("show_shadow", "Cień tekstu", "Renderowanie cienia pod literami", true));
        registerGroup(layoutGroup);
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
    public void onTick() {
        ComboTracker.getInstance().onTick(resetTimeoutMs.getValue().longValue());
    }

    @Override
    public void render(IHudRenderer renderer) {
        if (!isVisible() || renderer == null) return;

        ComboTracker tracker = ComboTracker.getInstance();
        int current = tracker.getCurrentCombo();
        int best = tracker.getBestCombo();

        // Calculate dynamic animation pulse
        float animScale = 1.0f;
        if (enableAnimation.getValue() && tracker.isComboActive()) {
            long now = System.nanoTime();
            long elapsedNanos = now - tracker.getLastHitTimeNanos();
            long durationNanos = animationDurationMs.getValue().longValue() * 1_000_000L;
            if (elapsedNanos < durationNanos && durationNanos > 0) {
                float progress = (float) elapsedNanos / durationNanos;
                float maxPulse = scalePulse.getValue().floatValue();
                animScale = maxPulse - (maxPulse - 1.0f) * progress;
            }
        }

        float s = scale.getValue().floatValue() * animScale;
        float textScale = textSize.getValue().floatValue() * s;
        int pad = padding.getValue().intValue();

        String mainText = buildComboText(current);
        String subText = buildSubText(best, tracker.getComboDurationMs());

        int textWidth = (int) (renderer.getTextWidth(mainText) * textScale);
        if (!subText.isEmpty()) {
            int subW = (int) (renderer.getTextWidth(subText) * (textScale * 0.85f));
            if (subW > textWidth) textWidth = subW;
        }

        int fontHeight = (int) (renderer.getFontHeight() * textScale);
        int totalHeight = pad * 2 + fontHeight;
        if (!subText.isEmpty()) {
            totalHeight += (int) (renderer.getFontHeight() * (textScale * 0.85f)) + 2;
        }
        int totalWidth = pad * 2 + textWidth;

        int baseX = anchor.getValue().calculateX(positionX.getValue().intValue(), totalWidth, renderer.getScreenWidth());
        int baseY = anchor.getValue().calculateY(positionY.getValue().intValue(), totalHeight, renderer.getScreenHeight());

        // Background & Border
        if (showBackground.getValue()) {
            int bg = applyOpacity(backgroundColor.getValue(), opacity.getValue().floatValue() / 100.0f);
            renderer.fill(baseX, baseY, totalWidth, totalHeight, bg);
        }
        if (showBorder.getValue()) {
            renderer.drawBorder(baseX, baseY, totalWidth, totalHeight, borderColor.getValue(), borderThickness.getValue().intValue());
        }

        // Color resolution
        int dynamicColor = resolveComboColor(current);

        int textX = baseX + pad;
        int textY = baseY + pad;

        renderer.drawText(mainText, textX, textY, dynamicColor, showShadow.getValue(), textScale, true);

        if (!subText.isEmpty()) {
            int subY = textY + fontHeight + 2;
            renderer.drawText(subText, textX, subY, textColor.getValue(), showShadow.getValue(), textScale * 0.85f, false);
        }
    }

    private String buildComboText(int current) {
        NumberFormatMode mode = formatMode.getValue();
        DisplayStyle style = displayStyle.getValue();

        if (style == DisplayStyle.COMPACT) {
            return String.valueOf(current);
        }

        switch (mode) {
            case RAW:
                return String.valueOf(current);
            case WITH_PREFIX:
                return prefix.getValue() + current;
            case WITH_SUFFIX:
                return current + suffix.getValue();
            case FULL:
            default:
                return (showLabel.getValue() ? prefix.getValue() : "") + current + suffix.getValue();
        }
    }

    private String buildSubText(int best, long durationMs) {
        StringBuilder sb = new StringBuilder();
        if (showBestCombo.getValue()) {
            sb.append("Best: ").append(best);
        }
        if (showTimer.getValue() && durationMs > 0) {
            if (sb.length() > 0) sb.append(" | ");
            double sec = durationMs / 1000.0;
            sb.append(String.format("%.1fs", sec));
        }
        return sb.toString();
    }

    public int resolveComboColor(int combo) {
        if (!enableMilestones.getValue()) {
            return counterColor.getValue();
        }

        int t4 = tier4Threshold.getValue().intValue();
        int t3 = tier3Threshold.getValue().intValue();
        int t2 = tier2Threshold.getValue().intValue();

        if (combo >= t4) {
            return tier4Color.getValue();
        } else if (combo >= t3) {
            return tier3Color.getValue();
        } else if (combo >= t2) {
            return tier2Color.getValue();
        } else {
            return tier1Color.getValue();
        }
    }

    private int applyOpacity(int color, float alphaFactor) {
        int alpha = (color >> 24) & 0xFF;
        int newAlpha = Math.round(alpha * alphaFactor);
        return (newAlpha << 24) | (color & 0x00FFFFFF);
    }

    // Getters for settings
    public BooleanSetting getResetOnTargetChange() { return resetOnTargetChange; }
    public BooleanSetting getResetOnMiss() { return resetOnMiss; }
    public BooleanSetting getResetOnDeath() { return resetOnDeath; }
    public NumberSetting getResetTimeoutMs() { return resetTimeoutMs; }
}
