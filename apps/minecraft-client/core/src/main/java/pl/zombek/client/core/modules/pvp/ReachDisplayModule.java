package pl.zombek.client.core.modules.pvp;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;
import pl.zombek.client.core.tracking.ReachTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reach Display HUD Module.
 * Displays calculated attack reach distances to targets using exact AABB geometry.
 * Purely informative; does not alter combat mechanics or attack range.
 */
public class ReachDisplayModule extends AbstractModule implements IHudElement {
    private static ReachDisplayModule instance;

    // General Group
    private final BooleanSetting showLastReachSetting;
    private final BooleanSetting showAverageReachSetting;
    private final BooleanSetting showMaximumReachSetting;
    private final BooleanSetting showMinimumReachSetting;
    private final BooleanSetting showCurrentTargetSetting;
    private final BooleanSetting showObservedHitSetting;

    // Display Group
    private final EnumSetting<ReachDisplayStyle> displayStyleSetting;
    private final BooleanSetting showUnitSetting;
    private final StringSetting unitSetting;
    private final StringSetting prefixSetting;
    private final StringSetting suffixSetting;
    private final NumberSetting decimalPlacesSetting;
    private final NumberSetting scaleSetting;

    // Behavior Group
    private final BooleanSetting measureOnAttemptSetting;
    private final BooleanSetting trackObservedHitsSetting;
    private final BooleanSetting resetOnTargetChangeSetting;
    private final BooleanSetting resetOnWorldChangeSetting;
    private final NumberSetting maxHistorySetting;

    // Colors Group
    private final ColorSetting textColorSetting;
    private final ColorSetting normalReachColorSetting;
    private final ColorSetting warningColorSetting;
    private final ColorSetting hitColorSetting;
    private final ColorSetting backgroundColorSetting;
    private final ColorSetting borderColorSetting;

    // Appearance Group
    private final BooleanSetting showBackgroundSetting;
    private final BooleanSetting showBorderSetting;
    private final BooleanSetting showShadowSetting;
    private final NumberSetting borderThicknessSetting;
    private final NumberSetting paddingSetting;

    // Layout Group
    private final EnumSetting<HudAnchor> anchorSetting;
    private final NumberSetting offsetXSetting;
    private final NumberSetting offsetYSetting;

    public ReachDisplayModule() {
        super(
                "reach-display",
                "Reach Display",
                "Wyświetla rzeczywistą odległość ataku do przeciwnika na podstawie geometrii AABB (Lunar Client style).",
                ModuleCategory.PVP
        );
        instance = this;

        // 1. General Group
        SettingGroup generalGroup = new SettingGroup("general", "General");
        this.showLastReachSetting = new BooleanSetting("show_last", "Show Last Reach", "Wyświetla odległość ostatniego ataku", true);
        this.showAverageReachSetting = new BooleanSetting("show_average", "Show Average Reach", "Wyświetla średni zasięg trafień", false);
        this.showMaximumReachSetting = new BooleanSetting("show_max", "Show Maximum Reach", "Wyświetla maksymalny zasięg sesji", false);
        this.showMinimumReachSetting = new BooleanSetting("show_min", "Show Minimum Reach", "Wyświetla minimalny zasięg sesji", false);
        this.showCurrentTargetSetting = new BooleanSetting("show_current_target", "Show Current Target Distance", "Wyświetla bieżący dystans do bytu", false);
        this.showObservedHitSetting = new BooleanSetting("show_observed_hit", "Show Observed Hit Distance", "Wyświetla dystans potwierdzonego trafienia", true);
        generalGroup.addSetting(this.showLastReachSetting);
        generalGroup.addSetting(this.showAverageReachSetting);
        generalGroup.addSetting(this.showMaximumReachSetting);
        generalGroup.addSetting(this.showMinimumReachSetting);
        generalGroup.addSetting(this.showCurrentTargetSetting);
        generalGroup.addSetting(this.showObservedHitSetting);
        registerGroup(generalGroup);

        // 2. Display Group
        SettingGroup displayGroup = new SettingGroup("display", "Display");
        this.displayStyleSetting = new EnumSetting<>(
                "style", "Display Style", "Układ wyświetlania: Compact, Detailed lub Minimal",
                ReachDisplayStyle.COMPACT
        );
        this.showUnitSetting = new BooleanSetting("show_unit", "Show Unit", "Wyświetla jednostkę miary (blocks)", true);
        this.unitSetting = new StringSetting("unit", "Unit", "Nazwa jednostki", "blocks");
        this.prefixSetting = new StringSetting("prefix", "Prefix", "Tekst przed wartością", "");
        this.suffixSetting = new StringSetting("suffix", "Suffix", "Tekst za wartością", "");
        this.decimalPlacesSetting = new NumberSetting("decimal_places", "Decimal Places", "Miejsca po przecinku", 2.0, 0.0, 3.0, 1.0);
        this.scaleSetting = new NumberSetting("scale", "Scale", "Skala elementu HUD", 1.0, 0.5, 2.0, 0.1);
        displayGroup.addSetting(this.displayStyleSetting);
        displayGroup.addSetting(this.showUnitSetting);
        displayGroup.addSetting(this.unitSetting);
        displayGroup.addSetting(this.prefixSetting);
        displayGroup.addSetting(this.suffixSetting);
        displayGroup.addSetting(this.decimalPlacesSetting);
        displayGroup.addSetting(this.scaleSetting);
        registerGroup(displayGroup);

        // 3. Behavior Group
        SettingGroup behaviorGroup = new SettingGroup("behavior", "Behavior");
        this.measureOnAttemptSetting = new BooleanSetting("measure_on_attempt", "Measure on Attack Attempt", "Mierzy dystans przy każdej próbie ataku", true);
        this.trackObservedHitsSetting = new BooleanSetting("track_observed_hits", "Track Observed Hits", "Weryfikuje obserwowane trafienia", true);
        this.resetOnTargetChangeSetting = new BooleanSetting("reset_on_target_change", "Reset on Target Change", "Resetuje statystyki przy zmianie celu", false);
        this.resetOnWorldChangeSetting = new BooleanSetting("reset_on_world_change", "Reset on World Change", "Resetuje historię po zmianie świata", true);
        this.maxHistorySetting = new NumberSetting("max_history", "Maximum History Entries", "Maksymalna liczba pomiarów w historii", 50.0, 10.0, 200.0, 10.0);
        behaviorGroup.addSetting(this.measureOnAttemptSetting);
        behaviorGroup.addSetting(this.trackObservedHitsSetting);
        behaviorGroup.addSetting(this.resetOnTargetChangeSetting);
        behaviorGroup.addSetting(this.resetOnWorldChangeSetting);
        behaviorGroup.addSetting(this.maxHistorySetting);
        registerGroup(behaviorGroup);

        // 4. Colors Group
        SettingGroup colorsGroup = new SettingGroup("colors", "Colors");
        this.textColorSetting = new ColorSetting("text_color", "Text Color", "Kolor tekstu etykiet", 0xFFFFFFFF);
        this.normalReachColorSetting = new ColorSetting("normal_reach_color", "Normal Reach Color", "Kolor normalnego zasięgu (<= 3.0)", 0xFF55FF55);
        this.warningColorSetting = new ColorSetting("warning_color", "Warning Color", "Kolor granicznego zasięgu (3.0 - 3.5)", 0xFFFFAA00);
        this.hitColorSetting = new ColorSetting("hit_color", "Hit Color", "Kolor dalekiego zasięgu / trafienia (> 3.5)", 0xFFFF5555);
        this.backgroundColorSetting = new ColorSetting("background_color", "Background Color", "Kolor tła modułu", 0x99000000);
        this.borderColorSetting = new ColorSetting("border_color", "Border Color", "Kolor ramki modułu", 0x33FFFFFF);
        colorsGroup.addSetting(this.textColorSetting);
        colorsGroup.addSetting(this.normalReachColorSetting);
        colorsGroup.addSetting(this.warningColorSetting);
        colorsGroup.addSetting(this.hitColorSetting);
        colorsGroup.addSetting(this.backgroundColorSetting);
        colorsGroup.addSetting(this.borderColorSetting);
        registerGroup(colorsGroup);

        // 5. Appearance Group
        SettingGroup appearanceGroup = new SettingGroup("appearance", "Appearance");
        this.showBackgroundSetting = new BooleanSetting("show_background", "Show Background", "Rysuje tło pod tekstem", true);
        this.showBorderSetting = new BooleanSetting("show_border", "Show Border", "Rysuje ramkę wokół kafelka", true);
        this.showShadowSetting = new BooleanSetting("show_shadow", "Show Shadow", "Cień pod tekstem", true);
        this.borderThicknessSetting = new NumberSetting("border_thickness", "Border Thickness", "Grubość ramki", 1.0, 1.0, 4.0, 1.0);
        this.paddingSetting = new NumberSetting("padding", "Padding", "Wewnętrzny margines kafelka", 4.0, 2.0, 12.0, 1.0);
        appearanceGroup.addSetting(this.showBackgroundSetting);
        appearanceGroup.addSetting(this.showBorderSetting);
        appearanceGroup.addSetting(this.showShadowSetting);
        appearanceGroup.addSetting(this.borderThicknessSetting);
        appearanceGroup.addSetting(this.paddingSetting);
        registerGroup(appearanceGroup);

        // 6. Layout Group
        SettingGroup layoutGroup = new SettingGroup("layout", "Layout");
        this.anchorSetting = new EnumSetting<>(
                "anchor", "Screen Anchor", "Punkt zakotwiczenia na ekranie",
                HudAnchor.TOP_LEFT
        );
        this.offsetXSetting = new NumberSetting("offset_x", "Offset X", "Przesunięcie poziome", 10.0, 0.0, 1000.0, 5.0);
        this.offsetYSetting = new NumberSetting("offset_y", "Offset Y", "Przesunięcie pionowe", 180.0, 0.0, 1000.0, 5.0);
        layoutGroup.addSetting(this.anchorSetting);
        layoutGroup.addSetting(this.offsetXSetting);
        layoutGroup.addSetting(this.offsetYSetting);
        registerGroup(layoutGroup);
    }

    public static ReachDisplayModule getInstance() {
        return instance;
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
        if (!isEnabled()) {
            return;
        }

        ReachTracker tracker = ReachTracker.getInstance();
        tracker.setMaxHistoryEntries(this.maxHistorySetting.getValue().intValue());

        List<String> lines = buildLines(tracker);
        if (lines.isEmpty()) {
            lines.add(formatValue(0.0));
        }

        int padding = this.paddingSetting.getValue().intValue();
        int fontHeight = renderer.getFontHeight();
        int lineHeight = fontHeight + 2;

        int maxWidth = 0;
        for (String line : lines) {
            int w = renderer.getTextWidth(line);
            if (w > maxWidth) {
                maxWidth = w;
            }
        }

        int totalWidth = maxWidth + padding * 2;
        int totalHeight = lines.size() * lineHeight - 2 + padding * 2;

        HudAnchor anchor = this.anchorSetting.getValue();
        int startX = anchor.calculateX(renderer.getScreenWidth(), totalWidth, this.offsetXSetting.getValue().intValue());
        int startY = anchor.calculateY(renderer.getScreenHeight(), totalHeight, this.offsetYSetting.getValue().intValue());

        if (this.showBackgroundSetting.getValue()) {
            renderer.fill(startX, startY, totalWidth, totalHeight, this.backgroundColorSetting.getValue());
        }

        if (this.showBorderSetting.getValue()) {
            renderer.drawBorder(startX, startY, totalWidth, totalHeight,
                    this.borderColorSetting.getValue(), this.borderThicknessSetting.getValue().intValue());
        }

        float scale = this.scaleSetting.getValue().floatValue();
        boolean shadow = this.showShadowSetting.getValue();

        int curY = startY + padding;
        for (String line : lines) {
            int textColor = resolveLineColor(line, tracker);
            renderer.drawText(line, startX + padding, curY, textColor, shadow, scale, false);
            curY += lineHeight;
        }
    }

    private List<String> buildLines(ReachTracker tracker) {
        List<String> lines = new ArrayList<>();
        ReachDisplayStyle style = this.displayStyleSetting.getValue();

        if (style == ReachDisplayStyle.MINIMAL) {
            double dist = tracker.getLastReach();
            lines.add(formatValueRaw(dist));
            return lines;
        }

        if (style == ReachDisplayStyle.COMPACT) {
            double dist = this.showObservedHitSetting.getValue() && tracker.getLastObservedHitDistance() > 0.0
                    ? tracker.getLastObservedHitDistance()
                    : tracker.getLastReach();
            lines.add(formatValue(dist));
            return lines;
        }

        // Detailed style
        if (this.showLastReachSetting.getValue()) {
            lines.add("Reach: " + formatValue(tracker.getLastReach()));
        }
        if (this.showObservedHitSetting.getValue() && tracker.getLastObservedHitDistance() > 0.0) {
            lines.add("Hit: " + formatValue(tracker.getLastObservedHitDistance()));
        }
        if (this.showAverageReachSetting.getValue()) {
            lines.add("Avg: " + formatValue(tracker.getAverageReach()));
        }
        if (this.showMaximumReachSetting.getValue()) {
            lines.add("Max: " + formatValue(tracker.getMaximumReach()));
        }
        if (this.showMinimumReachSetting.getValue()) {
            lines.add("Min: " + formatValue(tracker.getMinimumReach()));
        }
        if (this.showCurrentTargetSetting.getValue() && tracker.getCurrentTargetDistance() > 0.0) {
            lines.add("Target: " + formatValue(tracker.getCurrentTargetDistance()));
        }

        return lines;
    }

    private String formatValue(double distance) {
        String num = formatValueRaw(distance);
        StringBuilder sb = new StringBuilder();
        if (!this.prefixSetting.getValue().isEmpty()) {
            sb.append(this.prefixSetting.getValue()).append(" ");
        }
        sb.append(num);
        if (this.showUnitSetting.getValue() && !this.unitSetting.getValue().isEmpty()) {
            sb.append(" ").append(this.unitSetting.getValue());
        }
        if (!this.suffixSetting.getValue().isEmpty()) {
            sb.append(" ").append(this.suffixSetting.getValue());
        }
        return sb.toString();
    }

    private String formatValueRaw(double distance) {
        int dec = this.decimalPlacesSetting.getValue().intValue();
        if (dec <= 0) {
            return String.format(Locale.ROOT, "%.0f", distance);
        } else if (dec == 1) {
            return String.format(Locale.ROOT, "%.1f", distance);
        } else if (dec == 2) {
            return String.format(Locale.ROOT, "%.2f", distance);
        } else {
            return String.format(Locale.ROOT, "%.3f", distance);
        }
    }

    private int resolveLineColor(String line, ReachTracker tracker) {
        double last = tracker.getLastReach();
        if (last > 3.5) {
            return this.hitColorSetting.getValue();
        } else if (last > 3.0) {
            return this.warningColorSetting.getValue();
        } else {
            return this.normalReachColorSetting.getValue();
        }
    }

    // Getters for settings
    public BooleanSetting getShowLastReachSetting() { return showLastReachSetting; }
    public BooleanSetting getShowAverageReachSetting() { return showAverageReachSetting; }
    public BooleanSetting getShowMaximumReachSetting() { return showMaximumReachSetting; }
    public BooleanSetting getShowMinimumReachSetting() { return showMinimumReachSetting; }
    public BooleanSetting getShowCurrentTargetSetting() { return showCurrentTargetSetting; }
    public BooleanSetting getShowObservedHitSetting() { return showObservedHitSetting; }
    public EnumSetting<ReachDisplayStyle> getDisplayStyleSetting() { return displayStyleSetting; }
    public BooleanSetting getShowUnitSetting() { return showUnitSetting; }
    public StringSetting getUnitSetting() { return unitSetting; }
    public NumberSetting getDecimalPlacesSetting() { return decimalPlacesSetting; }
    public NumberSetting getScaleSetting() { return scaleSetting; }
    public BooleanSetting getMeasureOnAttemptSetting() { return measureOnAttemptSetting; }
    public BooleanSetting getTrackObservedHitsSetting() { return trackObservedHitsSetting; }
    public ColorSetting getTextColorSetting() { return textColorSetting; }
    public ColorSetting getNormalReachColorSetting() { return normalReachColorSetting; }
    public ColorSetting getHitColorSetting() { return hitColorSetting; }
    public EnumSetting<HudAnchor> getAnchorSetting() { return anchorSetting; }
    public NumberSetting getOffsetXSetting() { return offsetXSetting; }
    public NumberSetting getOffsetYSetting() { return offsetYSetting; }
}
