package pl.zombek.client.core.modules.visual;

import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.rendereffect.glint.*;
import pl.zombek.client.core.settings.*;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Glint Colorizer Module enabling custom enchantment glint colors, rainbow animations,
 * per-item category overrides, and seamless Shiny Pots integration.
 */
public class GlintColorizerModule extends AbstractModule {
    public static final String ID = "glint_colorizer";
    private static GlintColorizerModule instance;

    // General
    private final EnumSetting<GlintMode> modeSetting;
    private final NumberSetting glintSpeedSetting;
    private final NumberSetting glintOpacitySetting;

    // Colors
    private final ColorSetting primaryColorSetting;
    private final ColorSetting secondaryColorSetting;

    // Categories
    private final BooleanSetting categoryOverridesSetting;
    private final ColorSetting weaponsColorSetting;
    private final ColorSetting toolsColorSetting;
    private final ColorSetting armorColorSetting;
    private final ColorSetting booksColorSetting;
    private final ColorSetting potionsColorSetting;
    private final ColorSetting elytraColorSetting;
    private final ColorSetting shieldsColorSetting;

    // Locations
    private final BooleanSetting showInInventorySetting;
    private final BooleanSetting showInHotbarSetting;
    private final BooleanSetting showInHandSetting;
    private final BooleanSetting showDroppedSetting;
    private final BooleanSetting showInContainersSetting;
    private final BooleanSetting showOnArmorSetting;

    // Integration
    private final EnumSetting<ShinyPotsPriority> shinyPotsPrioritySetting;

    // Behavior
    private final BooleanSetting resetOnWorldChangeSetting;

    public GlintColorizerModule() {
        super(
                ID,
                "Glint Colorizer",
                "Personalizacja kolorów i animacji glintu zaklęć na przedmiotach i zbrojach.",
                ModuleCategory.VISUAL,
                GLFW.GLFW_KEY_UNKNOWN
        );
        instance = this;

        // 1. General
        SettingGroup generalGroup = new SettingGroup("general", "General");
        this.modeSetting = new EnumSetting<>(
                "mode", "Glint Mode", "Styl animacji i generowania barw (Vanilla, Static, Rainbow, Gradient, Pulse, Custom)",
                GlintMode.STATIC
        );
        this.glintSpeedSetting = new NumberSetting(
                "glint_speed", "Animation Speed", "Szybkość animacji tęczy, przejścia lub pulsacji", 1.0, 0.1, 5.0, 0.1, 1, "x"
        );
        this.glintOpacitySetting = new NumberSetting(
                "glint_opacity", "Glint Opacity", "Przezroczystość nałożonego efektu zaklęcia", 100.0, 10.0, 100.0, 5.0, 0, "%"
        );
        generalGroup.addSetting(modeSetting);
        generalGroup.addSetting(glintSpeedSetting);
        generalGroup.addSetting(glintOpacitySetting);
        addSettingGroup(generalGroup);

        // 2. Colors
        SettingGroup colorsGroup = new SettingGroup("colors", "Colors");
        this.primaryColorSetting = new ColorSetting(
                "primary_color", "Primary Color", "Główny kolor glintu zaklęć", 0xFF8040CC
        );
        this.secondaryColorSetting = new ColorSetting(
                "secondary_color", "Secondary Color", "Drugi kolor używany do animacji gradientu", 0xFF00FFFF
        );
        colorsGroup.addSetting(primaryColorSetting);
        colorsGroup.addSetting(secondaryColorSetting);
        addSettingGroup(colorsGroup);

        // 3. Category Overrides
        SettingGroup categoryGroup = new SettingGroup("categories", "Category Overrides");
        this.categoryOverridesSetting = new BooleanSetting(
                "category_overrides", "Enable Category Colors", "Używaj odrębnych kolorów dla poszczególnych typów ekwipunku", false
        );
        this.weaponsColorSetting = new ColorSetting(
                "weapons_color", "Weapons Glint", "Kolor glintu dla broni (miecze, topory, łuki)", 0xFFFF2222
        );
        this.toolsColorSetting = new ColorSetting(
                "tools_color", "Tools Glint", "Kolor glintu dla narzędzi (kilofy, łopaty)", 0xFF22FF22
        );
        this.armorColorSetting = new ColorSetting(
                "armor_color", "Armor Glint", "Kolor glintu dla elementów pancerza", 0xFF2288FF
        );
        this.booksColorSetting = new ColorSetting(
                "books_color", "Books Glint", "Kolor glintu dla zaklętych ksiąg", 0xFFFFDD00
        );
        this.potionsColorSetting = new ColorSetting(
                "potions_color", "Potions Glint", "Kolor glintu dla mikstur", 0xFFFF00FF
        );
        this.elytraColorSetting = new ColorSetting(
                "elytra_color", "Elytra Glint", "Kolor glintu dla elytry", 0xFF00FFFF
        );
        this.shieldsColorSetting = new ColorSetting(
                "shields_color", "Shields Glint", "Kolor glintu dla tarcz", 0xFFFF8800
        );
        categoryGroup.addSetting(categoryOverridesSetting);
        categoryGroup.addSetting(weaponsColorSetting);
        categoryGroup.addSetting(toolsColorSetting);
        categoryGroup.addSetting(armorColorSetting);
        categoryGroup.addSetting(booksColorSetting);
        categoryGroup.addSetting(potionsColorSetting);
        categoryGroup.addSetting(elytraColorSetting);
        categoryGroup.addSetting(shieldsColorSetting);
        addSettingGroup(categoryGroup);

        // 4. Locations
        SettingGroup locationGroup = new SettingGroup("locations", "Locations");
        this.showInInventorySetting = new BooleanSetting("show_inventory", "Show in Inventory", "Wyświetlaj w ekwipunku gracza", true);
        this.showInHotbarSetting = new BooleanSetting("show_hotbar", "Show in Hotbar", "Wyświetlaj na pasku podręcznym", true);
        this.showInHandSetting = new BooleanSetting("show_hand", "Show in Hand", "Wyświetlaj w dłoniach w pierwszej i trzeciej osobie", true);
        this.showDroppedSetting = new BooleanSetting("show_dropped", "Show Dropped", "Wyświetlaj na przedmiotach leżących na ziemi", true);
        this.showInContainersSetting = new BooleanSetting("show_containers", "Show in Containers", "Wyświetlaj w skrzyniach i innych pojemnikach", true);
        this.showOnArmorSetting = new BooleanSetting("show_armor", "Show on Armor", "Wyświetlaj na modelach założonej zbroi", true);
        locationGroup.addSetting(showInInventorySetting);
        locationGroup.addSetting(showInHotbarSetting);
        locationGroup.addSetting(showInHandSetting);
        locationGroup.addSetting(showDroppedSetting);
        locationGroup.addSetting(showInContainersSetting);
        locationGroup.addSetting(showOnArmorSetting);
        addSettingGroup(locationGroup);

        // 5. Shiny Pots Integration
        SettingGroup integrationGroup = new SettingGroup("integration", "Shiny Pots Integration");
        this.shinyPotsPrioritySetting = new EnumSetting<>(
                "shiny_pots_priority", "Shiny Pots Priority",
                "Priorytet kolorowania pomiędzy modułem Glint Colorizer a Shiny Pots",
                ShinyPotsPriority.PRESERVE_SHINY_POTS
        );
        integrationGroup.addSetting(shinyPotsPrioritySetting);
        addSettingGroup(integrationGroup);

        // 6. Behavior
        SettingGroup behaviorGroup = new SettingGroup("behavior", "Behavior");
        this.resetOnWorldChangeSetting = new BooleanSetting(
                "reset_on_world_change", "Reset on World Change", "Przywróć domyślny glint przy zmianie świata lub rozłączeniu", false
        );
        behaviorGroup.addSetting(resetOnWorldChangeSetting);
        addSettingGroup(behaviorGroup);

        // Listeners
        this.modeSetting.addChangeListener(n -> syncToController());
        this.glintSpeedSetting.addChangeListener(n -> syncToController());
        this.glintOpacitySetting.addChangeListener(n -> syncToController());
        this.primaryColorSetting.addChangeListener(n -> syncToController());
        this.secondaryColorSetting.addChangeListener(n -> syncToController());
        this.categoryOverridesSetting.addChangeListener(n -> syncToController());
        this.weaponsColorSetting.addChangeListener(n -> syncToController());
        this.toolsColorSetting.addChangeListener(n -> syncToController());
        this.armorColorSetting.addChangeListener(n -> syncToController());
        this.booksColorSetting.addChangeListener(n -> syncToController());
        this.potionsColorSetting.addChangeListener(n -> syncToController());
        this.elytraColorSetting.addChangeListener(n -> syncToController());
        this.shieldsColorSetting.addChangeListener(n -> syncToController());
        this.showInInventorySetting.addChangeListener(n -> syncToController());
        this.showInHotbarSetting.addChangeListener(n -> syncToController());
        this.showInHandSetting.addChangeListener(n -> syncToController());
        this.showDroppedSetting.addChangeListener(n -> syncToController());
        this.showInContainersSetting.addChangeListener(n -> syncToController());
        this.showOnArmorSetting.addChangeListener(n -> syncToController());
        this.shinyPotsPrioritySetting.addChangeListener(n -> syncToController());
    }

    public static GlintColorizerModule getInstance() {
        return instance;
    }

    public void syncToController() {
        if (!isEnabled()) {
            GlintRenderController.getInstance().setActive(false);
            return;
        }

        Set<GlintLocation> locations = EnumSet.noneOf(GlintLocation.class);
        if (showInInventorySetting.getValue()) locations.add(GlintLocation.INVENTORY);
        if (showInHotbarSetting.getValue()) locations.add(GlintLocation.HOTBAR);
        if (showInHandSetting.getValue()) locations.add(GlintLocation.HELD_ITEMS);
        if (showDroppedSetting.getValue()) locations.add(GlintLocation.DROPPED_ITEMS);
        if (showInContainersSetting.getValue()) locations.add(GlintLocation.CONTAINERS);
        if (showOnArmorSetting.getValue()) locations.add(GlintLocation.ARMOR_RENDERING);

        boolean overrides = categoryOverridesSetting.getValue();
        Map<GlintItemCategory, CategoryGlintConfig> catConfigs = new EnumMap<>(GlintItemCategory.class);
        catConfigs.put(GlintItemCategory.WEAPONS, new CategoryGlintConfig(overrides, modeSetting.getValue(), weaponsColorSetting.getValue(), secondaryColorSetting.getValue()));
        catConfigs.put(GlintItemCategory.TOOLS, new CategoryGlintConfig(overrides, modeSetting.getValue(), toolsColorSetting.getValue(), secondaryColorSetting.getValue()));
        catConfigs.put(GlintItemCategory.ARMOR, new CategoryGlintConfig(overrides, modeSetting.getValue(), armorColorSetting.getValue(), secondaryColorSetting.getValue()));
        catConfigs.put(GlintItemCategory.BOOKS, new CategoryGlintConfig(overrides, modeSetting.getValue(), booksColorSetting.getValue(), secondaryColorSetting.getValue()));
        catConfigs.put(GlintItemCategory.POTIONS, new CategoryGlintConfig(overrides, modeSetting.getValue(), potionsColorSetting.getValue(), secondaryColorSetting.getValue()));
        catConfigs.put(GlintItemCategory.ELYTRA, new CategoryGlintConfig(overrides, modeSetting.getValue(), elytraColorSetting.getValue(), secondaryColorSetting.getValue()));
        catConfigs.put(GlintItemCategory.SHIELDS, new CategoryGlintConfig(overrides, modeSetting.getValue(), shieldsColorSetting.getValue(), secondaryColorSetting.getValue()));
        catConfigs.put(GlintItemCategory.OTHER, new CategoryGlintConfig(false, modeSetting.getValue(), primaryColorSetting.getValue(), secondaryColorSetting.getValue()));

        GlintSettings settings = new GlintSettings(
                modeSetting.getValue(),
                primaryColorSetting.getValue(),
                secondaryColorSetting.getValue(),
                glintSpeedSetting.getValue().floatValue(),
                (float) (glintOpacitySetting.getValue() / 100.0),
                shinyPotsPrioritySetting.getValue(),
                locations,
                catConfigs
        );

        GlintRenderController.getInstance().setActive(true);
        GlintRenderController.getInstance().updateSettings(settings);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        syncToController();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        GlintRenderController.getInstance().setActive(false);
    }

    public void onWorldChange() {
        if (resetOnWorldChangeSetting.getValue()) {
            modeSetting.setValue(GlintMode.VANILLA);
            syncToController();
        }
    }

    // Getters
    public EnumSetting<GlintMode> getModeSetting() { return modeSetting; }
    public NumberSetting getGlintSpeedSetting() { return glintSpeedSetting; }
    public NumberSetting getGlintOpacitySetting() { return glintOpacitySetting; }
    public ColorSetting getPrimaryColorSetting() { return primaryColorSetting; }
    public ColorSetting getSecondaryColorSetting() { return secondaryColorSetting; }
    public BooleanSetting getCategoryOverridesSetting() { return categoryOverridesSetting; }
    public ColorSetting getWeaponsColorSetting() { return weaponsColorSetting; }
    public ColorSetting getToolsColorSetting() { return toolsColorSetting; }
    public ColorSetting getArmorColorSetting() { return armorColorSetting; }
    public ColorSetting getBooksColorSetting() { return booksColorSetting; }
    public ColorSetting getPotionsColorSetting() { return potionsColorSetting; }
    public ColorSetting getElytraColorSetting() { return elytraColorSetting; }
    public ColorSetting getShieldsColorSetting() { return shieldsColorSetting; }
    public BooleanSetting getShowInInventorySetting() { return showInInventorySetting; }
    public BooleanSetting getShowInHotbarSetting() { return showInHotbarSetting; }
    public BooleanSetting getShowInHandSetting() { return showInHandSetting; }
    public BooleanSetting getShowDroppedSetting() { return showDroppedSetting; }
    public BooleanSetting getShowInContainersSetting() { return showInContainersSetting; }
    public BooleanSetting getShowOnArmorSetting() { return showOnArmorSetting; }
    public EnumSetting<ShinyPotsPriority> getShinyPotsPrioritySetting() { return shinyPotsPrioritySetting; }
    public BooleanSetting getResetOnWorldChangeSetting() { return resetOnWorldChangeSetting; }
}
