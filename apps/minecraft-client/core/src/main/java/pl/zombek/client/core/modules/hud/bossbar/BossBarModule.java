package pl.zombek.client.core.modules.hud.bossbar;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.bossbar.*;
import pl.zombek.client.core.hud.component.HudComponentModel;
import pl.zombek.client.core.hud.component.HudPriorityResolver;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

import java.util.List;

/**
 * Boss Bar Module providing extensive visual customization and smooth animation
 * for Minecraft Boss Bars (Ender Dragon, Wither, Raids, custom bosses).
 */
public class BossBarModule extends AbstractModule implements IHudElement {

    private static BossBarModule instance;

    private final HudComponentModel model;

    // General
    private BooleanSetting useCustomRendererSetting;
    private BooleanSetting showBossBarSetting;
    private BooleanSetting showBossNameSetting;
    private BooleanSetting showProgressBarSetting;
    private BooleanSetting showPercentageSetting;
    private NumberSetting maxBossBarsSetting;
    private BooleanSetting previewModeSetting;

    // Style
    private EnumSetting<BossBarStyle> bossBarStyleSetting;
    private NumberSetting barWidthSetting;
    private NumberSetting barHeightSetting;
    private NumberSetting cornerRadiusSetting;
    private NumberSetting borderThicknessSetting;

    // Colors
    private BooleanSetting useBossColorSetting;
    private ColorSetting customBarColorSetting;
    private ColorSetting textColorSetting;
    private ColorSetting bgColorSetting;
    private ColorSetting borderColorSetting;

    // Behavior
    private BooleanSetting smoothAnimationSetting;
    private NumberSetting animationSpeedSetting;
    private BooleanSetting hideInSpectatorSetting;
    private EnumSetting<StackDirection> stackDirectionSetting;
    private NumberSetting spacingSetting;

    // Layout
    private EnumSetting<HudAnchor> anchorSetting;
    private NumberSetting posXSetting;
    private NumberSetting posYSetting;
    private NumberSetting scaleSetting;

    public BossBarModule() {
        super(
                "boss_bar",
                "Boss Bar",
                "Zaawansowana personalizacja i stylizacja pasków bossów (Ender Dragon, Wither, custom boss bar).",
                ModuleCategory.HUD
        );
        this.model = new HudComponentModel("boss_bar", 182, 24, HudAnchor.TOP_CENTER);
        this.model.setRenderPriority(HudPriorityResolver.PRIORITY_CRITICAL);
        this.model.setPosX(0);
        this.model.setPosY(12);
        initSettings();
        instance = this;
    }

    public static BossBarModule getInstance() {
        return instance;
    }

    /**
     * Checks whether custom renderer takes over vanilla boss bar rendering.
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
        IBossBarDataProvider provider = BossBarDataProviderRegistry.getInstance().getProvider();
        return provider != null && provider.hasActiveBossBar();
    }

    protected void initSettings() {
        // --- Grupa General ---
        SettingGroup general = new SettingGroup("general", "Ogólne");
        useCustomRendererSetting = new BooleanSetting("use_custom_renderer", "Niestandardowy Renderer", "Zastępuje domyślny boss bar Minecrafta", true);
        showBossBarSetting = new BooleanSetting("show_boss_bar", "Włącz Wyświetlanie", "Główny przełącznik widoczności paska bossa", true);
        showBossNameSetting = new BooleanSetting("show_boss_name", "Pokaż Nazwę Bossa", "Wyświetla nazwę bossa nad paskiem", true);
        showProgressBarSetting = new BooleanSetting("show_progress_bar", "Pokaż Pasek Zdrowia", "Wyświetla pasek postępu/zdrowia", true);
        showPercentageSetting = new BooleanSetting("show_percentage", "Pokaż Procenty (%)", "Wyświetla wartość procentową obok nazwy", true);
        maxBossBarsSetting = new NumberSetting("max_boss_bars", "Maksymalna Liczba Pasków", "Limit jednocześnie wyświetlanych boss barów", 3.0, 1.0, 6.0, 1.0);
        previewModeSetting = new BooleanSetting("preview_mode", "Tryb Podglądu", "Pokazuje przykładowe paski bossów (Smok Kresu / Wither)", false);
        general.addSetting(useCustomRendererSetting);
        general.addSetting(showBossBarSetting);
        general.addSetting(showBossNameSetting);
        general.addSetting(showProgressBarSetting);
        general.addSetting(showPercentageSetting);
        general.addSetting(maxBossBarsSetting);
        general.addSetting(previewModeSetting);
        registerGroup(general);

        // --- Grupa Style ---
        SettingGroup style = new SettingGroup("style", "Styl");
        bossBarStyleSetting = new EnumSetting<>("boss_bar_style", "Styl Paska", "Wybór motywu wizualnego", BossBarStyle.MODERN);
        barWidthSetting = new NumberSetting("bar_width", "Szerokość Paska", "Szerokość paska w pikselach", 182.0, 100.0, 350.0, 2.0);
        barHeightSetting = new NumberSetting("bar_height", "Wysokość Paska", "Wysokość paska w pikselach", 7.0, 3.0, 16.0, 1.0);
        cornerRadiusSetting = new NumberSetting("corner_radius", "Zaokrąglenie Rogów", "Promień zaokrąglenia w pikselach", 2.0, 0.0, 6.0, 1.0);
        borderThicknessSetting = new NumberSetting("border_thickness", "Grubość Ramki", "Grubość obramowania paska", 1.0, 0.0, 3.0, 1.0);
        style.addSetting(bossBarStyleSetting);
        style.addSetting(barWidthSetting);
        style.addSetting(barHeightSetting);
        style.addSetting(cornerRadiusSetting);
        style.addSetting(borderThicknessSetting);
        registerGroup(style);

        // --- Grupa Colors ---
        SettingGroup colors = new SettingGroup("colors", "Kolory");
        useBossColorSetting = new BooleanSetting("use_boss_color", "Użyj Koloru Bossa", "Wykorzystuje wbudowany kolor paska z gry (różowy dla Smoka itp.)", true);
        customBarColorSetting = new ColorSetting("custom_bar_color", "Własny Kolor Paska", "Kolor paska, gdy wyłączono kolor wbudowany", 0xFFFF55FF);
        textColorSetting = new ColorSetting("text_color", "Kolor Tekstu Nazwy", "Kolor czcionki nazwy bossa", 0xFFFFFFFF);
        bgColorSetting = new ColorSetting("bg_color", "Kolor Tła Paska", "Kolor pustego toru paska postępu", 0x60000000);
        borderColorSetting = new ColorSetting("border_color", "Kolor Ramki", "Kolor obramowania paska", 0x40FFFFFF);
        colors.addSetting(useBossColorSetting);
        colors.addSetting(customBarColorSetting);
        colors.addSetting(textColorSetting);
        colors.addSetting(bgColorSetting);
        colors.addSetting(borderColorSetting);
        registerGroup(colors);

        // --- Grupa Behavior ---
        SettingGroup behavior = new SettingGroup("behavior", "Zachowanie");
        smoothAnimationSetting = new BooleanSetting("smooth_animation", "Płynna Animacja Postępu", "Stopniowa interpolacja zmian zdrowia bossa", true);
        animationSpeedSetting = new NumberSetting("animation_speed", "Prędkość Animacji", "Współczynnik szybkości interpolacji", 0.2, 0.05, 1.0, 0.05);
        hideInSpectatorSetting = new BooleanSetting("hide_in_spectator", "Ukryj w Trybie Widza", "Nie wyświetla paska podczas obserwowania", true);
        stackDirectionSetting = new EnumSetting<>("stack_direction", "Kierunek Układania", "Kierunek kolejnych pasków przy wielu bossach", StackDirection.DOWN);
        spacingSetting = new NumberSetting("spacing", "Odstęp między Paskami", "Odstęp w pikselach", 6.0, 2.0, 20.0, 1.0);
        behavior.addSetting(smoothAnimationSetting);
        behavior.addSetting(animationSpeedSetting);
        behavior.addSetting(hideInSpectatorSetting);
        behavior.addSetting(stackDirectionSetting);
        behavior.addSetting(spacingSetting);
        registerGroup(behavior);

        // --- Grupa Layout ---
        SettingGroup layout = new SettingGroup("layout", "Pozycja");
        anchorSetting = new EnumSetting<>("anchor", "Punkt Zakotwiczenia", "Kotwica ekranowa", HudAnchor.TOP_CENTER);
        posXSetting = new NumberSetting("pos_x", "Pozycja X", "Przesunięcie w poziomie", 0.0, -1000.0, 1000.0, 1.0);
        posYSetting = new NumberSetting("pos_y", "Pozycja Y", "Przesunięcie w pionie", 12.0, 0.0, 2000.0, 1.0);
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
        if (!isVisible() || !showBossBarSetting.getValue()) return;

        IBossBarDataProvider provider = BossBarDataProviderRegistry.getInstance().getProvider();
        boolean isPreview = previewModeSetting.getValue();

        List<BossBarData> bossBars = null;
        if (provider != null && provider.hasActiveBossBar()) {
            bossBars = provider.getActiveBossBars();
        }

        if ((bossBars == null || bossBars.isEmpty()) && isPreview) {
            FallbackBossBarDataProvider fallback = new FallbackBossBarDataProvider();
            fallback.setPreviewMode(true);
            bossBars = fallback.getActiveBossBars();
        }

        if (bossBars == null || bossBars.isEmpty()) {
            return;
        }

        model.setAnchor(anchorSetting.getValue());
        model.setPosX(posXSetting.getValue().intValue());
        model.setPosY(posYSetting.getValue().intValue());
        model.setScale(scaleSetting.getValue());

        CustomBossBarRenderer.render(
                renderer,
                model,
                bossBars,
                bossBarStyleSetting.getValue(),
                barWidthSetting.getValue().intValue(),
                barHeightSetting.getValue().intValue(),
                borderThicknessSetting.getValue().intValue(),
                cornerRadiusSetting.getValue().intValue(),
                showBossNameSetting.getValue(),
                showProgressBarSetting.getValue(),
                showPercentageSetting.getValue(),
                maxBossBarsSetting.getValue().intValue(),
                spacingSetting.getValue().intValue(),
                stackDirectionSetting.getValue(),
                useBossColorSetting.getValue(),
                customBarColorSetting.getValue(),
                textColorSetting.getValue(),
                bgColorSetting.getValue(),
                borderColorSetting.getValue(),
                isPreview
        );
    }

    public String getStatusDescription() {
        IBossBarDataProvider provider = BossBarDataProviderRegistry.getInstance().getProvider();
        if (provider != null && provider.hasActiveBossBar()) {
            List<BossBarData> bars = provider.getActiveBossBars();
            int count = bars != null ? bars.size() : 0;
            return count > 0 ? "Aktywne boss bary: " + count : "Brak aktywnego boss bara";
        }
        if (previewModeSetting.getValue()) {
            return "Status: Podgląd (Preview)";
        }
        return "Brak aktywnego boss bara";
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

    public BooleanSetting getShowBossBarSetting() {
        return showBossBarSetting;
    }

    public BooleanSetting getShowBossNameSetting() {
        return showBossNameSetting;
    }

    public BooleanSetting getShowProgressBarSetting() {
        return showProgressBarSetting;
    }

    public BooleanSetting getShowPercentageSetting() {
        return showPercentageSetting;
    }

    public NumberSetting getMaxBossBarsSetting() {
        return maxBossBarsSetting;
    }

    public EnumSetting<BossBarStyle> getBossBarStyleSetting() {
        return bossBarStyleSetting;
    }

    public NumberSetting getBarWidthSetting() {
        return barWidthSetting;
    }

    public NumberSetting getBarHeightSetting() {
        return barHeightSetting;
    }

    public BooleanSetting getSmoothAnimationSetting() {
        return smoothAnimationSetting;
    }

    public EnumSetting<StackDirection> getStackDirectionSetting() {
        return stackDirectionSetting;
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
