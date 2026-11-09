package pl.zombek.client.core.modules.hud.titles;

import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;
import pl.zombek.client.core.titles.*;

/**
 * Universal Titles Customization Module for ZombekClient.
 * Customizes Title, Subtitle, and Action Bar presentations, timing, and animations.
 */
public class TitlesModule extends AbstractModule implements IHudElement {
    private static TitlesModule instance;

    // General Group
    private final SettingGroup generalGroup;
    private final EnumSetting<TitlePreset> presetSetting;
    private final BooleanSetting preserveVanillaTimingSetting;
    private final BooleanSetting enableTitleSetting;
    private final BooleanSetting enableSubtitleSetting;
    private final BooleanSetting enableActionBarSetting;

    // Title Group
    private final SettingGroup titleGroup;
    private final NumberSetting titleScaleSetting;
    private final ColorSetting titleColorSetting;
    private final EnumSetting<TitleAlignment> titleAlignmentSetting;
    private final NumberSetting titlePosYSetting;
    private final BooleanSetting titleShadowSetting;
    private final BooleanSetting titleBackgroundSetting;

    // Subtitle Group
    private final SettingGroup subtitleGroup;
    private final NumberSetting subtitleScaleSetting;
    private final ColorSetting subtitleColorSetting;
    private final NumberSetting subtitleSpacingSetting;
    private final BooleanSetting subtitleShadowSetting;

    // Action Bar Group
    private final SettingGroup actionBarGroup;
    private final NumberSetting actionBarScaleSetting;
    private final ColorSetting actionBarColorSetting;
    private final NumberSetting actionBarPosYSetting;
    private final BooleanSetting actionBarBackgroundSetting;
    private final BooleanSetting actionBarShadowSetting;

    // Timing Group
    private final SettingGroup timingGroup;
    private final NumberSetting overrideFadeInSetting;
    private final NumberSetting overrideStaySetting;
    private final NumberSetting overrideFadeOutSetting;
    private final NumberSetting overrideActionBarStaySetting;

    // Animation Group
    private final SettingGroup animationGroup;
    private final EnumSetting<TitleAnimationStyle> animationStyleSetting;

    // Visibility Group
    private final SettingGroup visibilityGroup;
    private final BooleanSetting hideInGuiSetting;
    private final BooleanSetting hideInSpectatorSetting;

    // Preview Group
    private final SettingGroup previewGroup;
    private final BooleanSetting previewModeSetting;
    private final StringSetting previewTitleTextSetting;
    private final StringSetting previewSubtitleTextSetting;
    private final StringSetting previewActionBarTextSetting;

    public TitlesModule() {
        super("titles", "Titles & Action Bar", "Personalizacja tytułów ekranowych, napisów podtytułowych oraz Action Bara", ModuleCategory.HUD);
        instance = this;

        // 1. General Group
        this.generalGroup = new SettingGroup("general", "Ustawienia Ogólne");
        this.presetSetting = new EnumSetting<>("preset", "Preset Wyglądu", "Gotowy preset wizualny tytułów", TitlePreset.MODERN);
        this.preserveVanillaTimingSetting = new BooleanSetting("preserve_vanilla_timing", "Czas Vanilla", "Wykorzystaj czasy nadsyłane przez serwer gry", true);
        this.enableTitleSetting = new BooleanSetting("enable_title", "Włącz Tytuł Główny", "Wyświetla duży napis Title", true);
        this.enableSubtitleSetting = new BooleanSetting("enable_subtitle", "Włącz Podtytuł", "Wyświetla napis Subtitle", true);
        this.enableActionBarSetting = new BooleanSetting("enable_action_bar", "Włącz Action Bar", "Wyświetla komunikat nad paskiem ekwipunku", true);
        generalGroup.addSetting(presetSetting);
        generalGroup.addSetting(preserveVanillaTimingSetting);
        generalGroup.addSetting(enableTitleSetting);
        generalGroup.addSetting(enableSubtitleSetting);
        generalGroup.addSetting(enableActionBarSetting);
        registerGroup(generalGroup);

        // 2. Title Group
        this.titleGroup = new SettingGroup("title", "Tytuł Główny (Title)");
        this.titleScaleSetting = new NumberSetting("title_scale", "Skala Tytułu", "Rozmiar czcionki tytułu", 3.0, 1.0, 6.0, 0.5, "");
        this.titleColorSetting = new ColorSetting("title_color", "Kolor Tytułu", "Domyślny kolor tekstu tytułu", 0xFFFFFFFF);
        this.titleAlignmentSetting = new EnumSetting<>("title_alignment", "Wyrównanie Tytułu", "Położenie poziome tytułu", TitleAlignment.CENTER);
        this.titlePosYSetting = new NumberSetting("title_pos_y", "Przesunięcie Y", "Pozycja pionowa względem środka", -30.0, -150.0, 150.0, 5.0, "px");
        this.titleShadowSetting = new BooleanSetting("title_shadow", "Cień Tytułu", "Rysuje cień pod tekstem", true);
        this.titleBackgroundSetting = new BooleanSetting("title_background", "Tło Tytułu", "Rysuje kartę tła za tytułem", true);
        titleGroup.addSetting(titleScaleSetting);
        titleGroup.addSetting(titleColorSetting);
        titleGroup.addSetting(titleAlignmentSetting);
        titleGroup.addSetting(titlePosYSetting);
        titleGroup.addSetting(titleShadowSetting);
        titleGroup.addSetting(titleBackgroundSetting);
        registerGroup(titleGroup);

        // 3. Subtitle Group
        this.subtitleGroup = new SettingGroup("subtitle", "Podtytuł (Subtitle)");
        this.subtitleScaleSetting = new NumberSetting("subtitle_scale", "Skala Podtytułu", "Rozmiar czcionki podtytułu", 1.4, 0.8, 3.5, 0.1, "");
        this.subtitleColorSetting = new ColorSetting("subtitle_color", "Kolor Podtytułu", "Kolor tekstu podtytułu", 0xFFDDDDDD);
        this.subtitleSpacingSetting = new NumberSetting("subtitle_spacing", "Odstęp od Tytułu", "Pionowy odstęp w pikselach", 18.0, 5.0, 50.0, 1.0, "px");
        this.subtitleShadowSetting = new BooleanSetting("subtitle_shadow", "Cień Podtytułu", "Rysuje cień pod tekstem", true);
        subtitleGroup.addSetting(subtitleScaleSetting);
        subtitleGroup.addSetting(subtitleColorSetting);
        subtitleGroup.addSetting(subtitleSpacingSetting);
        subtitleGroup.addSetting(subtitleShadowSetting);
        registerGroup(subtitleGroup);

        // 4. Action Bar Group
        this.actionBarGroup = new SettingGroup("action_bar", "Pasek Komunikatów (Action Bar)");
        this.actionBarScaleSetting = new NumberSetting("action_bar_scale", "Skala Action Bara", "Skala tekstu paska", 1.0, 0.5, 2.0, 0.1, "");
        this.actionBarColorSetting = new ColorSetting("action_bar_color", "Kolor Action Bara", "Kolor komunikatu", 0xFFFFFFFF);
        this.actionBarPosYSetting = new NumberSetting("action_bar_pos_y", "Wysokość od Dołu", "Odstęp od dołu ekranu w pikselach", 68.0, 20.0, 200.0, 2.0, "px");
        this.actionBarBackgroundSetting = new BooleanSetting("action_bar_background", "Karta Tła", "Półprzezroczyste tło pod komunikatem", true);
        this.actionBarShadowSetting = new BooleanSetting("action_bar_shadow", "Cień Tekstu", "Cień tekstu komunikatu", true);
        actionBarGroup.addSetting(actionBarScaleSetting);
        actionBarGroup.addSetting(actionBarColorSetting);
        actionBarGroup.addSetting(actionBarPosYSetting);
        actionBarGroup.addSetting(actionBarBackgroundSetting);
        actionBarGroup.addSetting(actionBarShadowSetting);
        registerGroup(actionBarGroup);

        // 5. Timing Group
        this.timingGroup = new SettingGroup("timing", "Czas i Trwanie (Timing)");
        this.overrideFadeInSetting = new NumberSetting("override_fade_in", "Pojawianie się (ticki)", "Czas wejścia (20t = 1s)", 10.0, 0.0, 60.0, 1.0, "t");
        this.overrideStaySetting = new NumberSetting("override_stay", "Czas Trwania (ticki)", "Czas wyświetlania (20t = 1s)", 70.0, 10.0, 200.0, 5.0, "t");
        this.overrideFadeOutSetting = new NumberSetting("override_fade_out", "Zanikanie (ticki)", "Czas wyjścia (20t = 1s)", 20.0, 0.0, 60.0, 1.0, "t");
        this.overrideActionBarStaySetting = new NumberSetting("override_ab_stay", "Czas Action Bara (ticki)", "Czas wyświetlania paska", 60.0, 10.0, 200.0, 5.0, "t");
        timingGroup.addSetting(overrideFadeInSetting);
        timingGroup.addSetting(overrideStaySetting);
        timingGroup.addSetting(overrideFadeOutSetting);
        timingGroup.addSetting(overrideActionBarStaySetting);
        registerGroup(timingGroup);

        // 6. Animation Group
        this.animationGroup = new SettingGroup("animation", "Styl Animacji");
        this.animationStyleSetting = new EnumSetting<>("animation_style", "Animacja", "Typ animacji wejścia i wyjścia", TitleAnimationStyle.SLIDE);
        animationGroup.addSetting(animationStyleSetting);
        registerGroup(animationGroup);

        // 7. Visibility Group
        this.visibilityGroup = new SettingGroup("visibility", "Widoczność");
        this.hideInGuiSetting = new BooleanSetting("hide_in_gui", "Ukryj w Menu GUI", "Ukrywa tytuły, gdy otwarty jest ekwipunek/menu", true);
        this.hideInSpectatorSetting = new BooleanSetting("hide_in_spectator", "Ukryj w Trybie Widza", "Ukrywa tytuły dla spectatora", false);
        visibilityGroup.addSetting(hideInGuiSetting);
        visibilityGroup.addSetting(hideInSpectatorSetting);
        registerGroup(visibilityGroup);

        // 8. Preview Group
        this.previewGroup = new SettingGroup("preview", "Podgląd Interaktywny");
        this.previewModeSetting = new BooleanSetting("preview_mode", "Włącz Podgląd", "Wyświetla próbne tytuły w edytorze", false);
        this.previewTitleTextSetting = new StringSetting("preview_title", "Próbny Tytuł", "Tekst tytułu demonstracyjnego", "ZWYCIĘSTWO!");
        this.previewSubtitleTextSetting = new StringSetting("preview_subtitle", "Próbny Podtytuł", "Tekst podtytułu", "Wszyscy przeciwnicy zostali pokonani");
        this.previewActionBarTextSetting = new StringSetting("preview_action_bar", "Próbny Action Bar", "Komunikat paska", "+100 XP (Bonus Zwycięzcy)");
        previewGroup.addSetting(previewModeSetting);
        previewGroup.addSetting(previewTitleTextSetting);
        previewGroup.addSetting(previewSubtitleTextSetting);
        previewGroup.addSetting(previewActionBarTextSetting);
        registerGroup(previewGroup);

        // Wire preset change
        this.presetSetting.addChangeListener(p -> {
            CustomTitleRenderer.getInstance().applyPreset(p);
        });

        // Wire timing changes
        this.preserveVanillaTimingSetting.addChangeListener(preserve -> {
            TitleQueueManager.getInstance().setPreserveVanillaTiming(preserve);
        });
    }

    public static synchronized TitlesModule getInstance() {
        return instance;
    }

    public static boolean shouldOverrideVanilla() {
        return instance != null && instance.isEnabled();
    }

    public static boolean shouldOverrideActionBar() {
        return instance != null && instance.isEnabled() && instance.enableActionBarSetting.getValue();
    }

    @Override
    public void resetToDefaults() {
        super.resetToDefaults();
        CustomTitleRenderer.getInstance().applyPreset(TitlePreset.MODERN);
        TitleQueueManager.getInstance().setPreserveVanillaTiming(true);
        TitleQueueManager.getInstance().setOverrideFadeInTicks(10);
        TitleQueueManager.getInstance().setOverrideStayTicks(70);
        TitleQueueManager.getInstance().setOverrideFadeOutTicks(20);
        TitleQueueManager.getInstance().setOverrideActionBarStayTicks(60);
    }

    // IHudElement Preview Support
    @Override
    public String getElementId() { return "titles"; }
    @Override
    public boolean isVisible() { return isEnabled(); }

    @Override
    public void render(IHudRenderer renderer) {
        if (!isEnabled()) return;

        // Draw preview card in GUI
        int x = 20;
        int y = 50;
        int w = 340;
        int h = 110;

        renderer.fill(x, y, w, h, 0xAA111118);
        renderer.drawBorder(x, y, w, h, 0xFF445566, 1);
        renderer.drawText("§b[PREVIEW] Titles & Action Bar", x + 6, y + 6, 0xFFFFFFFF, true, 1.0f, false);

        // Preview title
        String previewTitle = previewTitleTextSetting.getValue();
        int titleW = renderer.getTextWidth(previewTitle);
        renderer.drawText(previewTitle, x + (w - titleW) / 2, y + 26, titleColorSetting.getValue(), true, 1.5f, true);

        // Preview subtitle
        String previewSub = previewSubtitleTextSetting.getValue();
        int subW = renderer.getTextWidth(previewSub);
        renderer.drawText(previewSub, x + (w - subW) / 2, y + 46, subtitleColorSetting.getValue(), true, 1.0f, false);

        // Preview action bar
        String previewAb = previewActionBarTextSetting.getValue();
        int abW = renderer.getTextWidth(previewAb);
        renderer.fill(x + (w - abW) / 2 - 4, y + 78, abW + 8, 14, 0x88000000);
        renderer.drawText(previewAb, x + (w - abW) / 2, y + 80, actionBarColorSetting.getValue(), true, 1.0f, false);
    }

    // Getters for Settings
    public EnumSetting<TitlePreset> getPresetSetting() { return presetSetting; }
    public BooleanSetting getPreserveVanillaTimingSetting() { return preserveVanillaTimingSetting; }
    public BooleanSetting getEnableTitleSetting() { return enableTitleSetting; }
    public BooleanSetting getEnableSubtitleSetting() { return enableSubtitleSetting; }
    public BooleanSetting getEnableActionBarSetting() { return enableActionBarSetting; }
    public NumberSetting getTitleScaleSetting() { return titleScaleSetting; }
    public ColorSetting getTitleColorSetting() { return titleColorSetting; }
    public EnumSetting<TitleAlignment> getTitleAlignmentSetting() { return titleAlignmentSetting; }
    public NumberSetting getTitlePosYSetting() { return titlePosYSetting; }
    public BooleanSetting getTitleShadowSetting() { return titleShadowSetting; }
    public BooleanSetting getTitleBackgroundSetting() { return titleBackgroundSetting; }
    public NumberSetting getSubtitleScaleSetting() { return subtitleScaleSetting; }
    public ColorSetting getSubtitleColorSetting() { return subtitleColorSetting; }
    public NumberSetting getSubtitleSpacingSetting() { return subtitleSpacingSetting; }
    public BooleanSetting getSubtitleShadowSetting() { return subtitleShadowSetting; }
    public NumberSetting getActionBarScaleSetting() { return actionBarScaleSetting; }
    public ColorSetting getActionBarColorSetting() { return actionBarColorSetting; }
    public NumberSetting getActionBarPosYSetting() { return actionBarPosYSetting; }
    public BooleanSetting getActionBarBackgroundSetting() { return actionBarBackgroundSetting; }
    public BooleanSetting getActionBarShadowSetting() { return actionBarShadowSetting; }
    public NumberSetting getOverrideFadeInSetting() { return overrideFadeInSetting; }
    public NumberSetting getOverrideStaySetting() { return overrideStaySetting; }
    public NumberSetting getOverrideFadeOutSetting() { return overrideFadeOutSetting; }
    public NumberSetting getOverrideActionBarStaySetting() { return overrideActionBarStaySetting; }
    public EnumSetting<TitleAnimationStyle> getAnimationStyleSetting() { return animationStyleSetting; }
    public BooleanSetting getHideInGuiSetting() { return hideInGuiSetting; }
    public BooleanSetting getHideInSpectatorSetting() { return hideInSpectatorSetting; }
    public BooleanSetting getPreviewModeSetting() { return previewModeSetting; }
    public StringSetting getPreviewTitleTextSetting() { return previewTitleTextSetting; }
    public StringSetting getPreviewSubtitleTextSetting() { return previewSubtitleTextSetting; }
    public StringSetting getPreviewActionBarTextSetting() { return previewActionBarTextSetting; }
}
