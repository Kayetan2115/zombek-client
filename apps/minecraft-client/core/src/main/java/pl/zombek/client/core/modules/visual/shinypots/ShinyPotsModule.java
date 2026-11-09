package pl.zombek.client.core.modules.visual.shinypots;

import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

/**
 * Shiny Pots Module.
 * Imparts an enchanting glint effect to potions and tipped arrows without modifying item NBT,
 * attributes, or actual game physics.
 */
public class ShinyPotsModule extends AbstractModule {
    private static ShinyPotsModule instance;

    // General
    private final SettingGroup groupGeneral;
    private final BooleanSetting regularPotionsSetting;
    private final BooleanSetting splashPotionsSetting;
    private final BooleanSetting lingeringPotionsSetting;
    private final BooleanSetting tippedArrowsSetting;

    // Glint
    private final SettingGroup groupGlint;
    private final BooleanSetting fullBoxGlintSetting;
    private final BooleanSetting coloredPotionsSetting;
    private final BooleanSetting glintBehindSetting;
    private final BooleanSetting renderEntireSlotSetting;
    private final NumberSetting glintStrengthSetting;
    private final NumberSetting glintSpeedSetting;
    private final NumberSetting glintOpacitySetting;

    // Appearance
    private final SettingGroup groupAppearance;
    private final ColorSetting customGlintColorSetting;
    private final BooleanSetting potionColorTintSetting;
    private final BooleanSetting slotBackgroundSetting;
    private final NumberSetting backgroundOpacitySetting;

    // Visibility
    private final SettingGroup groupVisibility;
    private final BooleanSetting showInInventorySetting;
    private final BooleanSetting showInHotbarSetting;
    private final BooleanSetting showInContainersSetting;
    private final BooleanSetting showInHandSetting;
    private final BooleanSetting showDroppedSetting;

    public ShinyPotsModule() {
        super(
                "shiny_pots",
                "Shiny Pots",
                "Nadaje miksturom i strzałom z miksturą efekt połysku enchantu (glint), inspirowany Lunar Client.",
                ModuleCategory.VISUAL
        );
        instance = this;

        // General
        this.groupGeneral = new SettingGroup("general", "General & Target Items");
        this.regularPotionsSetting = new BooleanSetting(
                "regular_potions", "Enable for Regular Potions",
                "Włącza efekt połysku dla zwykłych butelek z miksturami.", true
        );
        this.splashPotionsSetting = new BooleanSetting(
                "splash_potions", "Enable for Splash Potions",
                "Włącza efekt połysku dla mikstur miotanych.", true
        );
        this.lingeringPotionsSetting = new BooleanSetting(
                "lingering_potions", "Enable for Lingering Potions",
                "Włącza efekt połysku dla mikstur trwałych.", true
        );
        this.tippedArrowsSetting = new BooleanSetting(
                "tipped_arrows", "Enable for Tipped Arrows",
                "Włącza efekt połysku dla strzał nasyconych miksturami.", true
        );
        this.groupGeneral.addSetting(regularPotionsSetting);
        this.groupGeneral.addSetting(splashPotionsSetting);
        this.groupGeneral.addSetting(lingeringPotionsSetting);
        this.groupGeneral.addSetting(tippedArrowsSetting);
        registerGroup(this.groupGeneral);

        // Glint
        this.groupGlint = new SettingGroup("glint", "Glint Configuration");
        this.fullBoxGlintSetting = new BooleanSetting(
                "full_box_glint", "Full Box Glint",
                "Pokrywa cały kafelek slotu efektem połysku.", false
        );
        this.coloredPotionsSetting = new BooleanSetting(
                "colored_potions", "Colored Potions",
                "Koloruje połysk bazowym kolorem płynu mikstury.", true
        );
        this.glintBehindSetting = new BooleanSetting(
                "glint_behind", "Render Glint Behind Potion",
                "Renderuje warstwę połysku za teksturą butelki.", false
        );
        this.renderEntireSlotSetting = new BooleanSetting(
                "render_entire_slot", "Render Entire Slot",
                "Aplikuje efekt na pełne pole slotu GUI.", false
        );
        this.glintStrengthSetting = new NumberSetting(
                "glint_strength", "Glint Strength",
                "Intensywność połysku (mnożnik).", 1.0, 0.1, 3.0, 0.1
        );
        this.glintSpeedSetting = new NumberSetting(
                "glint_speed", "Glint Speed",
                "Szybkość animacji fali połysku.", 1.0, 0.1, 5.0, 0.1
        );
        this.glintOpacitySetting = new NumberSetting(
                "glint_opacity", "Glint Opacity",
                "Przezroczystość efektu połysku (0-100%).", 100.0, 10.0, 100.0, 5.0
        );
        this.groupGlint.addSetting(fullBoxGlintSetting);
        this.groupGlint.addSetting(coloredPotionsSetting);
        this.groupGlint.addSetting(glintBehindSetting);
        this.groupGlint.addSetting(renderEntireSlotSetting);
        this.groupGlint.addSetting(glintStrengthSetting);
        this.groupGlint.addSetting(glintSpeedSetting);
        this.groupGlint.addSetting(glintOpacitySetting);
        registerGroup(this.groupGlint);

        // Appearance
        this.groupAppearance = new SettingGroup("appearance", "Colors & Tinting");
        this.customGlintColorSetting = new ColorSetting(
                "custom_glint_color", "Custom Glint Color",
                "Własny kolor bazowy połysku (domyślnie fioletowy).", 0xFF800080
        );
        this.potionColorTintSetting = new BooleanSetting(
                "potion_color_tint", "Potion Color Tint",
                "Mieszaj kolor połysku z kolorem płynu.", true
        );
        this.slotBackgroundSetting = new BooleanSetting(
                "slot_background", "Slot Background",
                "Rysuj delikatne tło pod miksturą.", false
        );
        this.backgroundOpacitySetting = new NumberSetting(
                "background_opacity", "Background Opacity",
                "Przezroczystość tła slotu (0-100%).", 40.0, 0.0, 100.0, 5.0
        );
        this.groupAppearance.addSetting(customGlintColorSetting);
        this.groupAppearance.addSetting(potionColorTintSetting);
        this.groupAppearance.addSetting(slotBackgroundSetting);
        this.groupAppearance.addSetting(backgroundOpacitySetting);
        registerGroup(this.groupAppearance);

        // Visibility
        this.groupVisibility = new SettingGroup("visibility", "Visibility Contexts");
        this.showInInventorySetting = new BooleanSetting(
                "show_in_inventory", "Inventory",
                "Pokazuj połysk w głównym ekwipunku gracza.", true
        );
        this.showInHotbarSetting = new BooleanSetting(
                "show_in_hotbar", "Hotbar",
                "Pokazuj połysk na pasku podręcznym (hotbar).", true
        );
        this.showInContainersSetting = new BooleanSetting(
                "show_in_containers", "Containers",
                "Pokazuj połysk w skrzyniach, shulkerach i beczkach.", true
        );
        this.showInHandSetting = new BooleanSetting(
                "show_in_hand", "Held Items",
                "Pokazuj połysk na przedmiocie trzymanym w rękach.", true
        );
        this.showDroppedSetting = new BooleanSetting(
                "show_dropped", "Dropped Items",
                "Pokazuj połysk na miksturach leżących na ziemi.", true
        );
        this.groupVisibility.addSetting(showInInventorySetting);
        this.groupVisibility.addSetting(showInHotbarSetting);
        this.groupVisibility.addSetting(showInContainersSetting);
        this.groupVisibility.addSetting(showInHandSetting);
        this.groupVisibility.addSetting(showDroppedSetting);
        registerGroup(this.groupVisibility);
    }

    public static ShinyPotsModule getInstance() {
        return instance;
    }

    @Override
    public void onInitialize() {}

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    @Override
    public void onTick() {}

    // Getters for settings
    public BooleanSetting getRegularPotionsSetting() { return regularPotionsSetting; }
    public BooleanSetting getSplashPotionsSetting() { return splashPotionsSetting; }
    public BooleanSetting getLingeringPotionsSetting() { return lingeringPotionsSetting; }
    public BooleanSetting getTippedArrowsSetting() { return tippedArrowsSetting; }

    public BooleanSetting getFullBoxGlintSetting() { return fullBoxGlintSetting; }
    public BooleanSetting getColoredPotionsSetting() { return coloredPotionsSetting; }
    public BooleanSetting getGlintBehindSetting() { return glintBehindSetting; }
    public BooleanSetting getRenderEntireSlotSetting() { return renderEntireSlotSetting; }
    public NumberSetting getGlintStrengthSetting() { return glintStrengthSetting; }
    public NumberSetting getGlintSpeedSetting() { return glintSpeedSetting; }
    public NumberSetting getGlintOpacitySetting() { return glintOpacitySetting; }

    public ColorSetting getCustomGlintColorSetting() { return customGlintColorSetting; }
    public BooleanSetting getPotionColorTintSetting() { return potionColorTintSetting; }
    public BooleanSetting getSlotBackgroundSetting() { return slotBackgroundSetting; }
    public NumberSetting getBackgroundOpacitySetting() { return backgroundOpacitySetting; }

    public BooleanSetting getShowInInventorySetting() { return showInInventorySetting; }
    public BooleanSetting getShowInHotbarSetting() { return showInHotbarSetting; }
    public BooleanSetting getShowInContainersSetting() { return showInContainersSetting; }
    public BooleanSetting getShowInHandSetting() { return showInHandSetting; }
    public BooleanSetting getShowDroppedSetting() { return showDroppedSetting; }
}
