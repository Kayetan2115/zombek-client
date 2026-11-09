package pl.zombek.client.core.modules.pvp;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;
import pl.zombek.client.core.tracking.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * PvP Info HUD Module.
 * Displays melee, projectile, and health statistics for PvP sessions (Lunar Client style).
 * Gathers genuine in-game metrics without estimating or faking missing server data.
 */
public class PvPInfoModule extends AbstractModule implements IHudElement {
    private static PvPInfoModule instance;

    // General Group
    private final BooleanSetting enableMeleeSetting;
    private final BooleanSetting enableProjectileSetting;
    private final BooleanSetting enableHealthSetting;
    private final BooleanSetting autoSessionSetting;
    private final BooleanSetting resetOnDeathSetting;
    private final BooleanSetting resetOnWorldChangeSetting;
    private final NumberSetting sessionTimeoutSetting;

    // Display Group
    private final EnumSetting<PvPInfoStyle> styleSetting;
    private final BooleanSetting showTitlesSetting;
    private final BooleanSetting compactLabelsSetting;
    private final NumberSetting scaleSetting;

    // Colors Group
    private final ColorSetting textColorSetting;
    private final ColorSetting titleColorSetting;
    private final ColorSetting valueColorSetting;
    private final ColorSetting accentColorSetting;
    private final ColorSetting backgroundColorSetting;
    private final ColorSetting borderColorSetting;

    // Appearance Group
    private final BooleanSetting showBackgroundSetting;
    private final BooleanSetting showBorderSetting;
    private final BooleanSetting showShadowSetting;
    private final NumberSetting borderThicknessSetting;
    private final NumberSetting paddingSetting;
    private final NumberSetting spacingSetting;

    // Layout Group
    private final EnumSetting<HudAnchor> anchorSetting;
    private final NumberSetting offsetXSetting;
    private final NumberSetting offsetYSetting;

    public PvPInfoModule() {
        super(
                "pvp-info",
                "PvP Info",
                "Zbiera statystyki sesji PvP: walka wręcz, celność, pociski i stan zdrowia (Lunar Client style).",
                ModuleCategory.PVP
        );
        instance = this;

        // 1. General Group
        SettingGroup generalGroup = new SettingGroup("general", "General");
        this.enableMeleeSetting = new BooleanSetting("enable_melee", "Enable Melee", "Sekcja statystyk walki wręcz", true);
        this.enableProjectileSetting = new BooleanSetting("enable_projectile", "Enable Projectile", "Sekcja statystyk pocisków", true);
        this.enableHealthSetting = new BooleanSetting("enable_health", "Enable Health", "Sekcja zdrowia gracza i celu", true);
        this.autoSessionSetting = new BooleanSetting("auto_session", "Automatic Session", "Automatyczne rozpoczynanie sesji przy walce", true);
        this.resetOnDeathSetting = new BooleanSetting("reset_on_death", "Reset on Death", "Resetowanie sesji po śmierci gracza", false);
        this.resetOnWorldChangeSetting = new BooleanSetting("reset_on_world_change", "Reset on World Change", "Resetowanie sesji przy zmianie świata", true);
        this.sessionTimeoutSetting = new NumberSetting("session_timeout", "Session Timeout (min)", "Limit bezczynności sesji w minutach", 10.0, 1.0, 60.0, 1.0);
        generalGroup.addSetting(this.enableMeleeSetting);
        generalGroup.addSetting(this.enableProjectileSetting);
        generalGroup.addSetting(this.enableHealthSetting);
        generalGroup.addSetting(this.autoSessionSetting);
        generalGroup.addSetting(this.resetOnDeathSetting);
        generalGroup.addSetting(this.resetOnWorldChangeSetting);
        generalGroup.addSetting(this.sessionTimeoutSetting);
        registerGroup(generalGroup);

        // 2. Display Group
        SettingGroup displayGroup = new SettingGroup("display", "Display");
        this.styleSetting = new EnumSetting<>(
                "style", "Display Style", "Styl prezentacji panelu statystyk",
                PvPInfoStyle.COMPACT
        );
        this.showTitlesSetting = new BooleanSetting("show_titles", "Show Section Titles", "Nagłówki sekcji MELEE/PROJECTILE/HEALTH", true);
        this.compactLabelsSetting = new BooleanSetting("compact_labels", "Compact Labels", "Krótsze nazwy statystyk", false);
        this.scaleSetting = new NumberSetting("scale", "Scale", "Skala kafelka HUD", 1.0, 0.5, 2.0, 0.1);
        displayGroup.addSetting(this.styleSetting);
        displayGroup.addSetting(this.showTitlesSetting);
        displayGroup.addSetting(this.compactLabelsSetting);
        displayGroup.addSetting(this.scaleSetting);
        registerGroup(displayGroup);

        // 3. Colors Group
        SettingGroup colorsGroup = new SettingGroup("colors", "Colors");
        this.textColorSetting = new ColorSetting("text_color", "Text Color", "Kolor tekstu etykiet", 0xFFE0E0E0);
        this.titleColorSetting = new ColorSetting("title_color", "Title Color", "Kolor nagłówków sekcji", 0xFFFFAA00);
        this.valueColorSetting = new ColorSetting("value_color", "Value Color", "Kolor wartości liczbowych", 0xFF55FFFF);
        this.accentColorSetting = new ColorSetting("accent_color", "Accent Color", "Kolor akcentów i alertów", 0xFFFF5555);
        this.backgroundColorSetting = new ColorSetting("background_color", "Background Color", "Kolor tła panelu", 0xAA000000);
        this.borderColorSetting = new ColorSetting("border_color", "Border Color", "Kolor obramowania", 0x33FFFFFF);
        colorsGroup.addSetting(this.textColorSetting);
        colorsGroup.addSetting(this.titleColorSetting);
        colorsGroup.addSetting(this.valueColorSetting);
        colorsGroup.addSetting(this.accentColorSetting);
        colorsGroup.addSetting(this.backgroundColorSetting);
        colorsGroup.addSetting(this.borderColorSetting);
        registerGroup(colorsGroup);

        // 4. Appearance Group
        SettingGroup appearanceGroup = new SettingGroup("appearance", "Appearance");
        this.showBackgroundSetting = new BooleanSetting("show_background", "Show Background", "Rysuje tło pod statystykami", true);
        this.showBorderSetting = new BooleanSetting("show_border", "Show Border", "Rysuje obramowanie panelu", true);
        this.showShadowSetting = new BooleanSetting("show_shadow", "Show Shadow", "Cień pod tekstem", true);
        this.borderThicknessSetting = new NumberSetting("border_thickness", "Border Thickness", "Grubość ramki", 1.0, 1.0, 4.0, 1.0);
        this.paddingSetting = new NumberSetting("padding", "Padding", "Margines wewnętrzny panelu", 4.0, 2.0, 12.0, 1.0);
        this.spacingSetting = new NumberSetting("spacing", "Spacing", "Odstęp między liniami", 2.0, 0.0, 6.0, 1.0);
        appearanceGroup.addSetting(this.showBackgroundSetting);
        appearanceGroup.addSetting(this.showBorderSetting);
        appearanceGroup.addSetting(this.showShadowSetting);
        appearanceGroup.addSetting(this.borderThicknessSetting);
        appearanceGroup.addSetting(this.paddingSetting);
        appearanceGroup.addSetting(this.spacingSetting);
        registerGroup(appearanceGroup);

        // 5. Layout Group
        SettingGroup layoutGroup = new SettingGroup("layout", "Layout");
        this.anchorSetting = new EnumSetting<>(
                "anchor", "Screen Anchor", "Punkt zakotwiczenia na ekranie",
                HudAnchor.TOP_LEFT
        );
        this.offsetXSetting = new NumberSetting("offset_x", "Offset X", "Przesunięcie poziome", 10.0, 0.0, 1000.0, 5.0);
        this.offsetYSetting = new NumberSetting("offset_y", "Offset Y", "Przesunięcie pionowe", 210.0, 0.0, 1000.0, 5.0);
        layoutGroup.addSetting(this.anchorSetting);
        layoutGroup.addSetting(this.offsetXSetting);
        layoutGroup.addSetting(this.offsetYSetting);
        registerGroup(layoutGroup);
    }

    public static PvPInfoModule getInstance() {
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

    /**
     * Resets current PvP session statistics.
     */
    public void resetSession() {
        PvPSessionManager.getInstance().resetSession();
    }

    @Override
    public void render(IHudRenderer renderer) {
        if (!isEnabled()) {
            return;
        }

        PvPSession session = PvPSessionManager.getInstance().getCurrentSession();
        ReachTracker reachTracker = ReachTracker.getInstance();
        ProjectileTracker projectileTracker = ProjectileTracker.getInstance();
        ComboTracker comboTracker = ComboTracker.getInstance();

        List<StatLine> lines = new ArrayList<>();
        boolean compact = this.compactLabelsSetting.getValue();

        // 1. MELEE SECTION
        if (this.enableMeleeSetting.getValue()) {
            if (this.showTitlesSetting.getValue()) {
                lines.add(new StatLine("MELEE", "", true));
            }
            lines.add(new StatLine(compact ? "Att:" : "Attacks:", String.valueOf(session.getMeleeAttacks()), false));
            lines.add(new StatLine(compact ? "Hits:" : "Observed Hits:", String.valueOf(session.getObservedHits()), false));
            lines.add(new StatLine(compact ? "Acc:" : "Accuracy:", String.format(Locale.ROOT, "%.1f%%", session.getEstimatedHitRate()), false));
            int combo = comboTracker.getCurrentCombo();
            int maxCombo = Math.max(session.getHighestCombo(), comboTracker.getBestCombo());
            lines.add(new StatLine(compact ? "Cmb:" : "Combo:", combo + " (Max: " + maxCombo + ")", false));

            double avgReach = reachTracker.getAverageReach();
            if (avgReach > 0.0) {
                lines.add(new StatLine(compact ? "Rch:" : "Avg Reach:", String.format(Locale.ROOT, "%.2f b", avgReach), false));
            }
        }

        // 2. PROJECTILE SECTION
        if (this.enableProjectileSetting.getValue()) {
            if (this.showTitlesSetting.getValue()) {
                lines.add(new StatLine("PROJECTILE", "", true));
            }
            int fired = projectileTracker.getProjectilesFired();
            int hits = projectileTracker.getObservedHits();
            lines.add(new StatLine(compact ? "Fired:" : "Fired:", String.valueOf(fired), false));
            if (hits > 0) {
                lines.add(new StatLine(compact ? "Hits:" : "Hits:", hits + " (" + String.format(Locale.ROOT, "%.0f%%", projectileTracker.getHitRate()) + ")", false));
            } else {
                lines.add(new StatLine(compact ? "Hits:" : "Hits:", "0", false));
            }
            if (fired > 0) {
                lines.add(new StatLine(compact ? "Type:" : "Last:", projectileTracker.getLastProjectileType().getDisplayName(), false));
            }
        }

        // 3. HEALTH SECTION
        if (this.enableHealthSetting.getValue()) {
            if (this.showTitlesSetting.getValue()) {
                lines.add(new StatLine("HEALTH", "", true));
            }
            float hp = session.getPlayerHealth();
            float abs = session.getPlayerAbsorption();
            String hpStr = String.format(Locale.ROOT, "%.1f", hp);
            if (abs > 0.0f) {
                hpStr += " (+" + String.format(Locale.ROOT, "%.1f", abs) + ")";
            }
            lines.add(new StatLine(compact ? "HP:" : "Health:", hpStr, false));

            if (session.getDamageReceived() > 0.0f) {
                lines.add(new StatLine(compact ? "Dmg:" : "Dmg Taken:", String.format(Locale.ROOT, "%.1f", session.getDamageReceived()), false));
            }

            if (session.hasValidTargetHealth()) {
                lines.add(new StatLine(compact ? "Tgt:" : "Target HP:", String.format(Locale.ROOT, "%.1f", session.getTargetHealth()), false));
            } else {
                lines.add(new StatLine(compact ? "Tgt:" : "Target HP:", "N/A", false));
            }
        }

        if (lines.isEmpty()) {
            return;
        }

        int padding = this.paddingSetting.getValue().intValue();
        int spacing = this.spacingSetting.getValue().intValue();
        int fontHeight = renderer.getFontHeight();
        int lineHeight = fontHeight + spacing;

        int maxWidth = 0;
        for (StatLine sl : lines) {
            String full = sl.isTitle ? sl.label : (sl.label + " " + sl.value);
            int w = renderer.getTextWidth(full);
            if (w > maxWidth) {
                maxWidth = w;
            }
        }

        int totalWidth = maxWidth + padding * 2;
        int totalHeight = lines.size() * lineHeight - spacing + padding * 2;

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
        for (StatLine sl : lines) {
            if (sl.isTitle) {
                renderer.drawText(sl.label, startX + padding, curY, this.titleColorSetting.getValue(), shadow, scale, true);
            } else {
                int labelWidth = renderer.getTextWidth(sl.label + " ");
                renderer.drawText(sl.label, startX + padding, curY, this.textColorSetting.getValue(), shadow, scale, false);
                renderer.drawText(sl.value, startX + padding + labelWidth, curY, this.valueColorSetting.getValue(), shadow, scale, false);
            }
            curY += lineHeight;
        }
    }

    private static class StatLine {
        final String label;
        final String value;
        final boolean isTitle;

        StatLine(String label, String value, boolean isTitle) {
            this.label = label;
            this.value = value;
            this.isTitle = isTitle;
        }
    }

    // Getters for settings
    public BooleanSetting getEnableMeleeSetting() { return enableMeleeSetting; }
    public BooleanSetting getEnableProjectileSetting() { return enableProjectileSetting; }
    public BooleanSetting getEnableHealthSetting() { return enableHealthSetting; }
    public BooleanSetting getAutoSessionSetting() { return autoSessionSetting; }
    public BooleanSetting getResetOnDeathSetting() { return resetOnDeathSetting; }
    public BooleanSetting getResetOnWorldChangeSetting() { return resetOnWorldChangeSetting; }
    public NumberSetting getSessionTimeoutSetting() { return sessionTimeoutSetting; }
    public EnumSetting<PvPInfoStyle> getStyleSetting() { return styleSetting; }
    public ColorSetting getTextColorSetting() { return textColorSetting; }
    public ColorSetting getTitleColorSetting() { return titleColorSetting; }
    public ColorSetting getValueColorSetting() { return valueColorSetting; }
    public ColorSetting getAccentColorSetting() { return accentColorSetting; }
    public EnumSetting<HudAnchor> getAnchorSetting() { return anchorSetting; }
    public NumberSetting getOffsetXSetting() { return offsetXSetting; }
    public NumberSetting getOffsetYSetting() { return offsetYSetting; }
}
