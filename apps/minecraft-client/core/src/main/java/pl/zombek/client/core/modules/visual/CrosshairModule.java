package pl.zombek.client.core.modules.visual;

import pl.zombek.client.core.data.CrosshairTargetRegistry;
import pl.zombek.client.core.data.CrosshairTargetType;
import pl.zombek.client.core.data.ICrosshairTargetProvider;
import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

/**
 * Customizable in-game crosshair module with dynamic targeting, styles, and smooth transitions.
 * Replaces vanilla Minecraft crosshair when enabled.
 */
public class CrosshairModule extends AbstractModule implements IHudElement {
    private static CrosshairModule instance;

    // General
    private final EnumSetting<CrosshairStyle> styleSetting;
    private final BooleanSetting showCenterDot;
    private final BooleanSetting showOutline;
    private final BooleanSetting showBackground;
    private final BooleanSetting showInSpectator;
    private final BooleanSetting hideInMenus;

    // Size
    private final NumberSetting widthSetting;
    private final NumberSetting heightSetting;
    private final NumberSetting thicknessSetting;
    private final NumberSetting gapSetting;
    private final NumberSetting dotSizeSetting;
    private final NumberSetting outlineThicknessSetting;
    private final NumberSetting circleRadiusSetting;
    private final NumberSetting circleThicknessSetting;
    private final NumberSetting squareWidthSetting;
    private final NumberSetting squareHeightSetting;
    private final NumberSetting squareThicknessSetting;
    private final BooleanSetting squareFilledSetting;

    // Colors
    private final ColorSetting mainColorSetting;
    private final ColorSetting outlineColorSetting;
    private final ColorSetting dotColorSetting;
    private final ColorSetting targetColorSetting;
    private final ColorSetting blockColorSetting;
    private final ColorSetting entityColorSetting;
    private final ColorSetting backgroundColorSetting;
    private final NumberSetting opacitySetting;

    // Dynamic
    private final BooleanSetting dynamicColorSetting;
    private final BooleanSetting targetDetectionSetting;
    private final BooleanSetting attackReadyColorSetting;
    private final BooleanSetting colorAnimationSetting;
    private final NumberSetting animationDurationSetting;

    // Advanced
    private final NumberSetting scaleSetting;
    private final NumberSetting rotationSetting;
    private final NumberSetting offsetXSetting;
    private final NumberSetting offsetYSetting;

    // State & color resolver
    private final CrosshairColorResolver colorResolver;

    public CrosshairModule() {
        super("crosshair", "Crosshair", "Własny, rozbudowany celownik z wieloma stylami i dynamiczną zmianą kolorów", ModuleCategory.HUD);
        instance = this;

        // --- General ---
        SettingGroup generalGroup = new SettingGroup("general", "General");
        this.styleSetting = generalGroup.addSetting(new EnumSetting<>(
                "style",
                "Styl celownika",
                "Wybierz kształt celownika",
                CrosshairStyle.CLASSIC_CROSS
        ));
        this.showCenterDot = generalGroup.addSetting(new BooleanSetting(
                "show_center_dot",
                "Centralna kropka",
                "Rysuje punkt w samym środku celownika",
                false
        ));
        this.showOutline = generalGroup.addSetting(new BooleanSetting(
                "show_outline",
                "Obramowanie",
                "Rysuje kontrastowy obrys wokół celownika",
                true
        ));
        this.showBackground = generalGroup.addSetting(new BooleanSetting(
                "show_background",
                "Tło celownika",
                "Wyświetla przyciemnione pole pod celownikiem",
                false
        ));
        this.showInSpectator = generalGroup.addSetting(new BooleanSetting(
                "show_in_spectator",
                "Widoczny w trybie Spectator",
                "Wyświetlaj celownik również podczas obserwowania",
                false
        ));
        this.hideInMenus = generalGroup.addSetting(new BooleanSetting(
                "hide_in_menus",
                "Ukrywaj w menu",
                "Automatycznie ukryj celownik przy otwartym ekranie/GUI",
                true
        ));
        registerGroup(generalGroup);

        // --- Size ---
        SettingGroup sizeGroup = new SettingGroup("size", "Wymiary");
        this.widthSetting = sizeGroup.addSetting(new NumberSetting("width", "Długość pozioma", "Długość poziomych ramion krzyżyka", 5.0, 1.0, 50.0, 1.0, "px"));
        this.heightSetting = sizeGroup.addSetting(new NumberSetting("height", "Długość pionowa", "Długość pionowych ramion krzyżyka", 5.0, 1.0, 50.0, 1.0, "px"));
        this.thicknessSetting = sizeGroup.addSetting(new NumberSetting("thickness", "Grubość linii", "Grubość ramion celownika", 1.0, 1.0, 10.0, 1.0, "px"));
        this.gapSetting = sizeGroup.addSetting(new NumberSetting("gap", "Odstęp od środka", "Przerwa pomiędzy środkiem a ramionami", 3.0, 0.0, 50.0, 1.0, "px"));
        this.dotSizeSetting = sizeGroup.addSetting(new NumberSetting("dot_size", "Rozmiar kropki", "Średnica centralnego punktu", 2.0, 1.0, 10.0, 1.0, "px"));
        this.outlineThicknessSetting = sizeGroup.addSetting(new NumberSetting("outline_thickness", "Grubość obramowania", "Grubość ciemnego obrysu", 1.0, 1.0, 5.0, 1.0, "px"));
        this.circleRadiusSetting = sizeGroup.addSetting(new NumberSetting("circle_radius", "Promień okręgu", "Promień pierścienia dla stylu Circle", 5.0, 2.0, 50.0, 1.0, "px"));
        this.circleThicknessSetting = sizeGroup.addSetting(new NumberSetting("circle_thickness", "Grubość okręgu", "Grubość pierścienia", 1.0, 1.0, 5.0, 1.0, "px"));
        this.squareWidthSetting = sizeGroup.addSetting(new NumberSetting("square_width", "Szerokość kwadratu", "Szerokość dla stylu Square", 6.0, 2.0, 50.0, 1.0, "px"));
        this.squareHeightSetting = sizeGroup.addSetting(new NumberSetting("square_height", "Wysokość kwadratu", "Wysokość dla stylu Square", 6.0, 2.0, 50.0, 1.0, "px"));
        this.squareThicknessSetting = sizeGroup.addSetting(new NumberSetting("square_thickness", "Grubość kwadratu", "Grubość linii kwadratu", 1.0, 1.0, 5.0, 1.0, "px"));
        this.squareFilledSetting = sizeGroup.addSetting(new BooleanSetting("square_filled", "Wypełnienie kwadratu", "Wypełnij wnętrze kwadratu kolorem", false));
        registerGroup(sizeGroup);

        // --- Colors ---
        SettingGroup colorsGroup = new SettingGroup("colors", "Kolory");
        this.mainColorSetting = colorsGroup.addSetting(new ColorSetting("main_color", "Główny kolor", "Podstawowy kolor celownika", 0xFFFFFFFF));
        this.outlineColorSetting = colorsGroup.addSetting(new ColorSetting("outline_color", "Kolor obramowania", "Kolor ciemnego obrysu", 0xFF000000));
        this.dotColorSetting = colorsGroup.addSetting(new ColorSetting("dot_color", "Kolor kropki", "Kolor centralnego punktu", 0xFFFFFFFF));
        this.targetColorSetting = colorsGroup.addSetting(new ColorSetting("target_color", "Kolor celu", "Kolor podczas gotowości / cooldownu", 0xFFFF5555));
        this.blockColorSetting = colorsGroup.addSetting(new ColorSetting("block_color", "Kolor celu: Blok", "Kolor po najechaniu na blok", 0xFF55FF55));
        this.entityColorSetting = colorsGroup.addSetting(new ColorSetting("entity_color", "Kolor celu: Encja", "Kolor po najechaniu na wrogą encję / gracza", 0xFFFF5555));
        this.backgroundColorSetting = colorsGroup.addSetting(new ColorSetting("bg_color", "Kolor tła", "Kolor przyciemnionego pola tła", 0x40000000));
        this.opacitySetting = colorsGroup.addSetting(new NumberSetting("opacity", "Przezroczystość", "Globalna nieprzezroczystość celownika", 100.0, 10.0, 100.0, 1.0, "%"));
        registerGroup(colorsGroup);

        // --- Dynamic ---
        SettingGroup dynamicGroup = new SettingGroup("dynamic", "Dynamiczny Crosshair");
        this.dynamicColorSetting = dynamicGroup.addSetting(new BooleanSetting("dynamic_color", "Dynamiczny kolor", "Zmieniaj kolor celownika w zależności od celu", true));
        this.targetDetectionSetting = dynamicGroup.addSetting(new BooleanSetting("target_detection", "Wykrywanie celowania", "Rozróżniaj celowanie w encje, bloki lub próżnię", true));
        this.attackReadyColorSetting = dynamicGroup.addSetting(new BooleanSetting("attack_ready_color", "Sygnalizuj gotowość ataku", "Zmieniaj odcień w trakcie odnawiania ataku", false));
        this.colorAnimationSetting = dynamicGroup.addSetting(new BooleanSetting("color_animation", "Płynna animacja przejścia", "Płynne blendowanie kolorów pomiędzy stanami", true));
        this.animationDurationSetting = dynamicGroup.addSetting(new NumberSetting("anim_duration", "Czas animacji", "Czas trwania przejścia kolorów", 150.0, 50.0, 1000.0, 10.0, "ms"));
        registerGroup(dynamicGroup);

        // --- Advanced ---
        SettingGroup advancedGroup = new SettingGroup("advanced", "Zaawansowane");
        this.scaleSetting = advancedGroup.addSetting(new NumberSetting("scale", "Skala", "Mnożnik rozmiaru całego celownika", 1.0, 0.5, 3.0, 0.1, "x"));
        this.rotationSetting = advancedGroup.addSetting(new NumberSetting("rotation", "Rotacja", "Kąt obrotu celownika", 0.0, 0.0, 360.0, 5.0, "°"));
        this.offsetXSetting = advancedGroup.addSetting(new NumberSetting("offset_x", "Przesunięcie X", "Korekta pozycji poziomej od środka ekranu", 0.0, -200.0, 200.0, 1.0, "px"));
        this.offsetYSetting = advancedGroup.addSetting(new NumberSetting("offset_y", "Przesunięcie Y", "Korekta pozycji pionowej od środka ekranu", 0.0, -200.0, 200.0, 1.0, "px"));
        registerGroup(advancedGroup);

        this.colorResolver = new CrosshairColorResolver(mainColorSetting.getValue());
    }

    public static CrosshairModule getInstance() {
        return instance;
    }

    @Override
    public String getElementId() {
        return getId();
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
    public boolean isVisible() {
        if (!isEnabled() || hasError()) {
            return false;
        }
        ICrosshairTargetProvider provider = CrosshairTargetRegistry.getProvider();
        if (provider.isHudHidden()) {
            return false;
        }
        if (hideInMenus.getValue() && provider.hasOpenScreen()) {
            return false;
        }
        if (provider.isSpectator() && !showInSpectator.getValue()) {
            return false;
        }
        return true;
    }

    /**
     * Determines whether the vanilla Minecraft crosshair should be suppressed.
     * Safe against crashes and respects spectator / menu states.
     */
    public static boolean shouldHideVanillaCrosshair() {
        CrosshairModule mod = getInstance();
        if (mod == null || !mod.isEnabled() || mod.hasError()) {
            return false;
        }
        ICrosshairTargetProvider provider = CrosshairTargetRegistry.getProvider();
        if (provider.isHudHidden()) {
            return false;
        }
        if (provider.isSpectator() && !mod.showInSpectator.getValue()) {
            return false;
        }
        return true;
    }

    @Override
    public void render(IHudRenderer renderer) {
        if (!isVisible() || renderer == null) {
            return;
        }

        ICrosshairTargetProvider provider = CrosshairTargetRegistry.getProvider();
        CrosshairTargetType targetType = provider.getTargetType();
        float attackCooldown = provider.getAttackCooldownProgress();
        long now = System.currentTimeMillis();

        // 1. Resolve effective color
        int activeColor = colorResolver.resolveColor(
                targetType,
                attackCooldown,
                dynamicColorSetting.getValue(),
                targetDetectionSetting.getValue(),
                attackReadyColorSetting.getValue(),
                colorAnimationSetting.getValue(),
                animationDurationSetting.getValue().longValue(),
                mainColorSetting.getValue(),
                targetColorSetting.getValue(),
                blockColorSetting.getValue(),
                entityColorSetting.getValue(),
                now
        );

        // Apply global opacity
        float opacityFactor = (float) (opacitySetting.getValue() / 100.0);
        int finalColor = applyOpacity(activeColor, opacityFactor);
        int finalOutlineColor = applyOpacity(outlineColorSetting.getValue(), opacityFactor);
        int finalDotColor = applyOpacity(dotColorSetting.getValue(), opacityFactor);
        int finalBgColor = applyOpacity(backgroundColorSetting.getValue(), opacityFactor);

        // 2. Center coordinates + offset
        int screenWidth = renderer.getScreenWidth();
        int screenHeight = renderer.getScreenHeight();
        int cx = (screenWidth / 2) + offsetXSetting.getValue().intValue();
        int cy = (screenHeight / 2) + offsetYSetting.getValue().intValue();

        // 3. Dynamic gap expansion if dynamic cross style
        int gap = gapSetting.getValue().intValue();
        if (styleSetting.getValue() == CrosshairStyle.DYNAMIC_CROSS) {
            gap += Math.round((1.0f - attackCooldown) * 4.0f);
        }

        // 4. Procedural render
        CrosshairRenderer.render(
                renderer,
                cx,
                cy,
                styleSetting.getValue(),
                finalColor,
                finalOutlineColor,
                finalDotColor,
                finalBgColor,
                showCenterDot.getValue(),
                showOutline.getValue(),
                showBackground.getValue(),
                widthSetting.getValue().intValue(),
                heightSetting.getValue().intValue(),
                thicknessSetting.getValue().intValue(),
                gap,
                dotSizeSetting.getValue().intValue(),
                outlineThicknessSetting.getValue().intValue(),
                circleRadiusSetting.getValue().intValue(),
                circleThicknessSetting.getValue().intValue(),
                squareWidthSetting.getValue().intValue(),
                squareHeightSetting.getValue().intValue(),
                squareThicknessSetting.getValue().intValue(),
                squareFilledSetting.getValue(),
                scaleSetting.getValue().floatValue()
        );
    }

    private static int applyOpacity(int argb, float opacityFactor) {
        int a = (argb >> 24) & 0xFF;
        int newA = Math.round(a * Math.min(1.0f, Math.max(0.0f, opacityFactor)));
        return (newA << 24) | (argb & 0x00FFFFFF);
    }

    // Getters for settings
    public EnumSetting<CrosshairStyle> getStyleSetting() {
        return styleSetting;
    }

    public BooleanSetting getShowCenterDot() {
        return showCenterDot;
    }

    public BooleanSetting getShowOutline() {
        return showOutline;
    }

    public BooleanSetting getShowInSpectator() {
        return showInSpectator;
    }

    public BooleanSetting getHideInMenus() {
        return hideInMenus;
    }

    public NumberSetting getWidthSetting() {
        return widthSetting;
    }

    public NumberSetting getHeightSetting() {
        return heightSetting;
    }

    public NumberSetting getThicknessSetting() {
        return thicknessSetting;
    }

    public NumberSetting getGapSetting() {
        return gapSetting;
    }

    public ColorSetting getMainColorSetting() {
        return mainColorSetting;
    }

    public BooleanSetting getDynamicColorSetting() {
        return dynamicColorSetting;
    }
}
