package pl.zombek.client.core.modules.hud.potions;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.component.HudComponentModel;
import pl.zombek.client.core.hud.component.HudPriorityResolver;
import pl.zombek.client.core.hud.potions.*;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Potion Effects Module displaying player's active status effects with duration,
 * amplifiers, category color coding, and expiration alerts.
 */
public class PotionEffectsModule extends AbstractModule implements IHudElement {

    private static PotionEffectsModule instance;

    private final HudComponentModel model;

    // General
    private BooleanSetting showIconsSetting;
    private BooleanSetting showNamesSetting;
    private BooleanSetting showAmplifierSetting;
    private BooleanSetting showDurationSetting;
    private BooleanSetting showInfiniteSetting;
    private EnumSetting<PotionSortMode> sortModeSetting;
    private BooleanSetting hideVanillaHudSetting;
    private BooleanSetting previewModeSetting;

    // Display
    private EnumSetting<PotionLayoutMode> layoutModeSetting;
    private NumberSetting iconSizeSetting;
    private NumberSetting rowSpacingSetting;
    private NumberSetting maxEffectsSetting;
    private BooleanSetting showDurationBarSetting;

    // Behavior
    private BooleanSetting highlightExpiringSetting;
    private NumberSetting warningTimeSetting;
    private BooleanSetting hideInSpectatorSetting;

    // Colors
    private ColorSetting beneficialColorSetting;
    private ColorSetting neutralColorSetting;
    private ColorSetting harmfulColorSetting;
    private ColorSetting expiringColorSetting;
    private ColorSetting textColorSetting;
    private ColorSetting bgColorSetting;

    // Layout
    private EnumSetting<HudAnchor> anchorSetting;
    private NumberSetting posXSetting;
    private NumberSetting posYSetting;
    private NumberSetting scaleSetting;

    public PotionEffectsModule() {
        super(
                "potion_effects",
                "Potion Effects",
                "Uniwersalny HUD aktywnych efektów mikstur i statusów gracza.",
                ModuleCategory.HUD
        );
        this.model = new HudComponentModel("potion_effects", 120, 80, HudAnchor.TOP_RIGHT);
        this.model.setRenderPriority(HudPriorityResolver.PRIORITY_MEDIUM);
        this.model.setPosX(4);
        this.model.setPosY(4);
        initSettings();
        instance = this;
    }

    public static PotionEffectsModule getInstance() {
        return instance;
    }

    public static boolean shouldHideVanilla() {
        return instance != null && instance.isEnabled() && !instance.hasError() && instance.hideVanillaHudSetting.getValue();
    }

    protected void initSettings() {
        // --- Grupa General ---
        SettingGroup general = new SettingGroup("general", "Ogólne");
        showIconsSetting = new BooleanSetting("show_icons", "Pokaż Ikony", "Wyświetla graficzne ikony efektów", true);
        showNamesSetting = new BooleanSetting("show_names", "Pokaż Nazwy", "Wyświetla nazwę statusu", true);
        showAmplifierSetting = new BooleanSetting("show_amplifier", "Pokaż Poziom", "Wyświetla poziom rzymski (np. II)", true);
        showDurationSetting = new BooleanSetting("show_duration", "Pokaż Czas", "Wyświetla pozostały czas trwania", true);
        showInfiniteSetting = new BooleanSetting("show_infinite", "Pokaż Efekty Stałe", "Wyświetlanie efektów o nieskończonym czasie", true);
        sortModeSetting = new EnumSetting<>("sort_mode", "Tryb Sortowania", "Porządkowanie listy efektów", PotionSortMode.DURATION_SHORTEST);
        hideVanillaHudSetting = new BooleanSetting("hide_vanilla_hud", "Ukryj Vanilla HUD", "Ukrywa domyślne ikony mikstur w prawym górnym rogu", true);
        previewModeSetting = new BooleanSetting("preview_mode", "Tryb Podglądu", "Wyświetla przykładowe efekty w celach konfiguracji", false);
        general.addSetting(showIconsSetting);
        general.addSetting(showNamesSetting);
        general.addSetting(showAmplifierSetting);
        general.addSetting(showDurationSetting);
        general.addSetting(showInfiniteSetting);
        general.addSetting(sortModeSetting);
        general.addSetting(hideVanillaHudSetting);
        general.addSetting(previewModeSetting);
        registerGroup(general);

        // --- Grupa Display ---
        SettingGroup display = new SettingGroup("display", "Wyświetlanie");
        layoutModeSetting = new EnumSetting<>("layout_mode", "Układ Efektów", "Styl prezentacji kafli", PotionLayoutMode.CLASSIC);
        iconSizeSetting = new NumberSetting("icon_size", "Rozmiar Ikony", "Wymiar ikony efektu w pikselach", 16.0, 10.0, 32.0, 1.0);
        rowSpacingSetting = new NumberSetting("row_spacing", "Odstęp między Efektami", "Pikselowy odstęp w pionie", 3.0, 1.0, 10.0, 1.0);
        maxEffectsSetting = new NumberSetting("max_effects", "Maksymalna Liczba Efektów", "Limit renderowanych statusów", 8.0, 1.0, 16.0, 1.0);
        showDurationBarSetting = new BooleanSetting("show_duration_bar", "Pasek Postępu Czasu", "Rysuje mini pasek ubywającego czasu", false);
        display.addSetting(layoutModeSetting);
        display.addSetting(iconSizeSetting);
        display.addSetting(rowSpacingSetting);
        display.addSetting(maxEffectsSetting);
        display.addSetting(showDurationBarSetting);
        registerGroup(display);

        // --- Grupa Behavior ---
        SettingGroup behavior = new SettingGroup("behavior", "Zachowanie");
        highlightExpiringSetting = new BooleanSetting("highlight_expiring", "Wyróżnij Kończące Się", "Zmienia kolor lub pulsowanie przy bliskim końcu efektu", true);
        warningTimeSetting = new NumberSetting("warning_time", "Czas Ostrzeżenia (sekundy)", "Próg czasowy alertu wygaśnięcia", 10.0, 3.0, 30.0, 1.0);
        hideInSpectatorSetting = new BooleanSetting("hide_in_spectator", "Ukryj w Trybie Widza", "Nie renderuje efektów podczas bycia obserwatorem", true);
        behavior.addSetting(highlightExpiringSetting);
        behavior.addSetting(warningTimeSetting);
        behavior.addSetting(hideInSpectatorSetting);
        registerGroup(behavior);

        // --- Grupa Colors ---
        SettingGroup colors = new SettingGroup("colors", "Kolory");
        beneficialColorSetting = new ColorSetting("beneficial_color", "Pozytywny (Buff)", "Kolor tekstu dla efektów korzystnych", 0xFF55FF55);
        neutralColorSetting = new ColorSetting("neutral_color", "Neutralny", "Kolor tekstu dla efektów neutralnych", 0xFFFFFFFF);
        harmfulColorSetting = new ColorSetting("harmful_color", "Negatywny (Debuff)", "Kolor tekstu dla efektów szkodliwych", 0xFFFF5555);
        expiringColorSetting = new ColorSetting("expiring_color", "Kończący Się", "Kolor alertu bliskiego końca", 0xFFFF3333);
        textColorSetting = new ColorSetting("text_color", "Domyślny Tekst", "Kolor czasu i dodatkowych opisów", 0xFFAAAAAA);
        bgColorSetting = new ColorSetting("bg_color", "Kolor Tła Kafli", "Tło pojedynczego wiersza/efektu", 0x60000000);
        colors.addSetting(beneficialColorSetting);
        colors.addSetting(neutralColorSetting);
        colors.addSetting(harmfulColorSetting);
        colors.addSetting(expiringColorSetting);
        colors.addSetting(textColorSetting);
        colors.addSetting(bgColorSetting);
        registerGroup(colors);

        // --- Grupa Layout ---
        SettingGroup layout = new SettingGroup("layout", "Pozycja");
        anchorSetting = new EnumSetting<>("anchor", "Punkt Zakotwiczenia", "Kotwica ekranowa", HudAnchor.TOP_RIGHT);
        posXSetting = new NumberSetting("pos_x", "Pozycja X", "Przesunięcie w poziomie", 4.0, 0.0, 4000.0, 1.0);
        posYSetting = new NumberSetting("pos_y", "Pozycja Y", "Przesunięcie w pionie", 4.0, 0.0, 4000.0, 1.0);
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

        IPotionEffectsDataProvider provider = PotionEffectsDataProviderRegistry.getInstance().getProvider();
        boolean isPreview = previewModeSetting.getValue();

        List<StatusEffectData> rawEffects = null;
        if (provider != null && provider.isPlayerAvailable()) {
            rawEffects = provider.getActiveEffects();
        }

        if ((rawEffects == null || rawEffects.isEmpty()) && isPreview) {
            FallbackPotionEffectsDataProvider fallback = new FallbackPotionEffectsDataProvider();
            fallback.setPreviewMode(true);
            rawEffects = fallback.getActiveEffects();
        }

        if (rawEffects == null || rawEffects.isEmpty()) {
            return;
        }

        // Filter and sort
        List<StatusEffectData> effects = new ArrayList<>();
        boolean showInf = showInfiniteSetting.getValue();
        for (StatusEffectData eff : rawEffects) {
            if (!showInf && eff.isInfinite()) continue;
            effects.add(eff);
        }

        sortEffects(effects, sortModeSetting.getValue());

        int limit = maxEffectsSetting.getValue().intValue();
        if (effects.size() > limit) {
            effects = effects.subList(0, limit);
        }

        int fontH = renderer.getFontHeight();
        int iconSize = iconSizeSetting.getValue().intValue();
        int spacing = rowSpacingSetting.getValue().intValue();
        PotionLayoutMode layoutMode = layoutModeSetting.getValue();

        int rowH = Math.max(iconSize, fontH * 2) + 4;
        if (layoutMode == PotionLayoutMode.ICONS_ONLY) {
            rowH = iconSize + 4;
        }

        int totalH = effects.size() * (rowH + spacing);
        int maxW = 110;

        model.setWidth(maxW);
        model.setHeight(Math.max(20, totalH));
        model.setAnchor(anchorSetting.getValue());
        model.setPosX(posXSetting.getValue().intValue());
        model.setPosY(posYSetting.getValue().intValue());
        model.setScale(scaleSetting.getValue());

        int screenW = renderer.getScreenWidth();
        int screenH = renderer.getScreenHeight();
        int startX = model.getScreenX(screenW);
        int startY = model.getScreenY(screenH);

        int warningSec = warningTimeSetting.getValue().intValue();
        boolean highlightExp = highlightExpiringSetting.getValue();

        for (int i = 0; i < effects.size(); i++) {
            StatusEffectData eff = effects.get(i);
            int itemY = startY + i * (rowH + spacing);

            // Draw card background
            int rowBg = bgColorSetting.getValue();
            renderer.fill(startX, itemY, maxW, rowH, rowBg);
            renderer.drawBorder(startX, itemY, maxW, rowH, 0x25FFFFFF, 1);

            int contentX = startX + 3;

            // Draw Icon placeholder / color badge
            if (showIconsSetting.getValue()) {
                renderer.fill(contentX, itemY + (rowH - iconSize) / 2, iconSize, iconSize, eff.getColor() | 0xFF000000);
                renderer.drawBorder(contentX, itemY + (rowH - iconSize) / 2, iconSize, iconSize, 0x50FFFFFF, 1);
                contentX += iconSize + 4;
            }

            if (layoutMode == PotionLayoutMode.ICONS_ONLY) {
                continue;
            }

            // Determine text color based on category and expiration
            boolean isExpiring = !eff.isInfinite() && eff.getDurationSeconds() <= warningSec;
            int nameColor;
            if (isExpiring && highlightExp) {
                nameColor = expiringColorSetting.getValue();
            } else {
                switch (eff.getCategory()) {
                    case HARMFUL:
                        nameColor = harmfulColorSetting.getValue();
                        break;
                    case NEUTRAL:
                        nameColor = neutralColorSetting.getValue();
                        break;
                    case BENEFICIAL:
                    default:
                        nameColor = beneficialColorSetting.getValue();
                        break;
                }
            }

            // Format title: Name + Amplifier
            StringBuilder titleBuilder = new StringBuilder();
            if (showNamesSetting.getValue()) {
                titleBuilder.append(eff.getName());
            }
            if (showAmplifierSetting.getValue() && eff.getAmplifier() > 0) {
                if (titleBuilder.length() > 0) titleBuilder.append(" ");
                titleBuilder.append(eff.getAmplifierRoman());
            }

            if (titleBuilder.length() > 0) {
                renderer.drawText(titleBuilder.toString(), contentX, itemY + 2, nameColor, true, 0.9f, true);
            }

            // Duration
            if (showDurationSetting.getValue()) {
                String durStr = eff.formatDuration();
                int durColor = isExpiring && highlightExp ? expiringColorSetting.getValue() : textColorSetting.getValue();
                renderer.drawText(durStr, contentX, itemY + rowH - fontH - 2, durColor, true, 0.85f, false);
            }
        }

        if (isPreview) {
            String badge = "§8[§cPREVIEW§8]";
            renderer.drawText(badge, startX + maxW - renderer.getTextWidth(badge) - 2, startY + totalH + 2, 0xFFAAAAAA, true, 0.75f, false);
        }
    }

    private void sortEffects(List<StatusEffectData> list, PotionSortMode mode) {
        switch (mode) {
            case DURATION_SHORTEST:
                list.sort(Comparator.comparingInt(StatusEffectData::getDurationTicks));
                break;
            case DURATION_LONGEST:
                list.sort(Comparator.comparingInt(StatusEffectData::getDurationTicks).reversed());
                break;
            case BENEFICIAL_FIRST:
                list.sort(Comparator.comparingInt((StatusEffectData e) -> e.getCategory().ordinal()));
                break;
            case HARMFUL_FIRST:
                list.sort(Comparator.comparingInt((StatusEffectData e) -> e.getCategory().ordinal()).reversed());
                break;
            case ALPHABETICAL:
                list.sort(Comparator.comparing(StatusEffectData::getName, String.CASE_INSENSITIVE_ORDER));
                break;
            case CUSTOM_ORDER:
            default:
                break;
        }
    }

    public String getStatusDescription() {
        IPotionEffectsDataProvider provider = PotionEffectsDataProviderRegistry.getInstance().getProvider();
        if (provider != null && provider.isPlayerAvailable()) {
            List<StatusEffectData> effs = provider.getActiveEffects();
            int count = effs != null ? effs.size() : 0;
            return count > 0 ? "Aktywne efekty: " + count : "Brak aktywnych efektów";
        }
        if (previewModeSetting.getValue()) {
            return "Status: Podgląd (Preview)";
        }
        return "Brak aktywnych efektów";
    }

    public HudComponentModel getModel() {
        return model;
    }

    public EnumSetting<PotionSortMode> getSortModeSetting() {
        return sortModeSetting;
    }

    public EnumSetting<PotionLayoutMode> getLayoutModeSetting() {
        return layoutModeSetting;
    }

    public BooleanSetting getPreviewModeSetting() {
        return previewModeSetting;
    }

    public BooleanSetting getHideVanillaHudSetting() {
        return hideVanillaHudSetting;
    }

    public BooleanSetting getShowIconsSetting() {
        return showIconsSetting;
    }

    public BooleanSetting getShowNamesSetting() {
        return showNamesSetting;
    }

    public BooleanSetting getShowAmplifierSetting() {
        return showAmplifierSetting;
    }

    public BooleanSetting getShowDurationSetting() {
        return showDurationSetting;
    }

    public BooleanSetting getHighlightExpiringSetting() {
        return highlightExpiringSetting;
    }

    public NumberSetting getWarningTimeSetting() {
        return warningTimeSetting;
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
