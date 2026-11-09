package pl.zombek.client.core.modules.visual.nametags;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.component.HudComponentModel;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.settings.*;

/**
 * Universal NameTags customization module for Minecraft.
 * Provides custom 3D in-game name rendering, local aliases, friend markers,
 * health, distance, team, and armor status without compromising Fair Play or vanilla visibility rules.
 */
public class NameTagsModule extends AbstractModule implements IHudElement {

    public static final String ID = "nametags";

    private final HudComponentModel model;

    // 1. General
    private final SettingGroup groupGeneral;
    private final BooleanSetting targetPlayersSetting;
    private final BooleanSetting targetLivingSetting;
    private final BooleanSetting targetOtherSetting;
    private final BooleanSetting useCustomRendererSetting;
    private final BooleanSetting preserveFormattingSetting;

    // 2. Player Information
    private final SettingGroup groupPlayerInfo;
    private final BooleanSetting showNameSetting;
    private final BooleanSetting showHealthSetting;
    private final BooleanSetting showAbsorptionSetting;
    private final BooleanSetting showDistanceSetting;
    private final BooleanSetting showTeamSetting;
    private final BooleanSetting showPingSetting;
    private final BooleanSetting showFriendMarkerSetting;
    private final BooleanSetting showArmorSetting;
    private final BooleanSetting showHeldItemSetting;

    // 3. Custom Name
    private final SettingGroup groupCustomName;
    private final BooleanSetting enableAliasSetting;
    private final BooleanSetting showCustomPrefixSetting;
    private final StringSetting customPrefixSetting;
    private final BooleanSetting showCustomSuffixSetting;
    private final StringSetting customSuffixSetting;
    private final StringSetting friendMarkerIconSetting;

    // 4. Health
    private final SettingGroup groupHealth;
    private final EnumSetting<HealthDisplayStyle> healthStyleSetting;
    private final ColorSetting healthColorSetting;
    private final ColorSetting lowHealthColorSetting;
    private final ColorSetting absorptionColorSetting;
    private final BooleanSetting maxHealthDisplaySetting;
    private final NumberSetting decimalPrecisionSetting;
    private final BooleanSetting smoothTransitionSetting;

    // 5. Distance
    private final SettingGroup groupDistance;
    private final EnumSetting<DistanceFormat> distanceFormatSetting;
    private final NumberSetting maxDisplayDistanceSetting;
    private final NumberSetting minDisplayDistanceSetting;

    // 6. Armor
    private final SettingGroup groupArmor;
    private final BooleanSetting showDurabilitySetting;
    private final BooleanSetting showArmorIconsSetting;
    private final BooleanSetting lowDurabilityWarningSetting;

    // 7. Appearance
    private final SettingGroup groupAppearance;
    private final EnumSetting<NameTagLayout> layoutSetting;
    private final NumberSetting scaleSetting;
    private final BooleanSetting dynamicScaleSetting;
    private final BooleanSetting constantScaleSetting;
    private final NumberSetting scaleMultiplierSetting;
    private final ColorSetting backgroundColorSetting;
    private final NumberSetting backgroundOpacitySetting;
    private final ColorSetting borderColorSetting;
    private final BooleanSetting showBorderSetting;
    private final NumberSetting borderThicknessSetting;
    private final BooleanSetting shadowSetting;
    private final NumberSetting spacingSetting;
    private final NumberSetting cornerRadiusSetting;

    // 8. Visibility
    private final SettingGroup groupVisibility;
    private final BooleanSetting respectVanillaVisibilitySetting;
    private final NumberSetting maxRenderDistanceSetting;
    private final BooleanSetting hideInGuiSetting;
    private final BooleanSetting hideInSpectatorSetting;
    private final BooleanSetting hideInvisibleSetting;
    private final BooleanSetting hideSneakingSetting;

    // 9. Position
    private final SettingGroup groupPosition;
    private final NumberSetting verticalOffsetSetting;
    private final NumberSetting horizontalOffsetSetting;
    private final NumberSetting nametagHeightSetting;

    public NameTagsModule() {
        super(ID, "NameTags", "Dostosowuje wygląd i informacje wyświetlane w NameTagach nad graczami i encjami z zachowaniem Fair Play.", ModuleCategory.VISUAL);

        this.model = new HudComponentModel(ID, 120, 24, HudAnchor.CENTER);
        this.model.setRenderPriority(0);

        // 1. General
        this.groupGeneral = new SettingGroup("general", "General");
        this.targetPlayersSetting = new BooleanSetting("target_players", "Target Players", "Dostosowuje NameTagi innych graczy.", true);
        this.targetLivingSetting = new BooleanSetting("target_living", "Target Living Entities", "Dostosowuje NameTagi stworzeń (zombie, zwierzęta itp.).", true);
        this.targetOtherSetting = new BooleanSetting("target_other", "Target Other Entities", "Dostosowuje NameTagi innych nazwanych encji.", false);
        this.useCustomRendererSetting = new BooleanSetting("use_custom_renderer", "Use Custom Renderer", "Zastępuje domyślny renderer NameTagów.", true);
        this.preserveFormattingSetting = new BooleanSetting("preserve_formatting", "Preserve Formatting", "Zachowuje oryginalne kody kolorów i formatowania.", true);

        this.groupGeneral.addSetting(targetPlayersSetting);
        this.groupGeneral.addSetting(targetLivingSetting);
        this.groupGeneral.addSetting(targetOtherSetting);
        this.groupGeneral.addSetting(useCustomRendererSetting);
        this.groupGeneral.addSetting(preserveFormattingSetting);

        // 2. Player Information
        this.groupPlayerInfo = new SettingGroup("player_info", "Player Information");
        this.showNameSetting = new BooleanSetting("show_name", "Show Name", "Wyświetla nazwę/nick gracza.", true);
        this.showHealthSetting = new BooleanSetting("show_health", "Show Health", "Wyświetla aktualny stan zdrowia (o ile znany klientowi).", true);
        this.showAbsorptionSetting = new BooleanSetting("show_absorption", "Show Absorption", "Wyświetla absorpcję/złote serca.", true);
        this.showDistanceSetting = new BooleanSetting("show_distance", "Show Distance", "Wyświetla odległość w metrach.", false);
        this.showTeamSetting = new BooleanSetting("show_team", "Show Team", "Wyświetla przynależność do drużyny.", true);
        this.showPingSetting = new BooleanSetting("show_ping", "Show Ping", "Wyświetla opóźnienie (ping z tablisty).", false);
        this.showFriendMarkerSetting = new BooleanSetting("show_friend_marker", "Show Friend Marker", "Wyświetla ikonę znajomego.", true);
        this.showArmorSetting = new BooleanSetting("show_armor", "Show Armor", "Wyświetla wskaźniki zbroi.", false);
        this.showHeldItemSetting = new BooleanSetting("show_held_item", "Show Held Item", "Wyświetla trzymany przedmiot.", false);

        this.groupPlayerInfo.addSetting(showNameSetting);
        this.groupPlayerInfo.addSetting(showHealthSetting);
        this.groupPlayerInfo.addSetting(showAbsorptionSetting);
        this.groupPlayerInfo.addSetting(showDistanceSetting);
        this.groupPlayerInfo.addSetting(showTeamSetting);
        this.groupPlayerInfo.addSetting(showPingSetting);
        this.groupPlayerInfo.addSetting(showFriendMarkerSetting);
        this.groupPlayerInfo.addSetting(showArmorSetting);
        this.groupPlayerInfo.addSetting(showHeldItemSetting);

        // 3. Custom Name
        this.groupCustomName = new SettingGroup("custom_name", "Custom Name & Friends");
        this.enableAliasSetting = new BooleanSetting("enable_alias", "Enable Local Aliases", "Stosuje lokalne aliasy nadane znajomym.", true);
        this.showCustomPrefixSetting = new BooleanSetting("show_custom_prefix", "Show Custom Prefix", "Wyświetla własny lokalny prefiks.", false);
        this.customPrefixSetting = new StringSetting("custom_prefix", "Custom Prefix", "Lokalny prefiks tekstowy.", "");
        this.showCustomSuffixSetting = new BooleanSetting("show_custom_suffix", "Show Custom Suffix", "Wyświetla własny lokalny sufiks.", false);
        this.customSuffixSetting = new StringSetting("custom_suffix", "Custom Suffix", "Lokalny sufiks tekstowy.", "");
        this.friendMarkerIconSetting = new StringSetting("friend_marker_icon", "Friend Marker Icon", "Symbol oznaczający znajomego.", "★");

        this.groupCustomName.addSetting(enableAliasSetting);
        this.groupCustomName.addSetting(showCustomPrefixSetting);
        this.groupCustomName.addSetting(customPrefixSetting);
        this.groupCustomName.addSetting(showCustomSuffixSetting);
        this.groupCustomName.addSetting(customSuffixSetting);
        this.groupCustomName.addSetting(friendMarkerIconSetting);

        // 4. Health
        this.groupHealth = new SettingGroup("health", "Health Display");
        this.healthStyleSetting = new EnumSetting<>("health_style", "Health Style", "Format prezentacji zdrowia.", HealthDisplayStyle.NUMERIC);
        this.healthColorSetting = new ColorSetting("health_color", "Health Color", "Kolor wskaźnika zdrowia.", 0xFF55FF55);
        this.lowHealthColorSetting = new ColorSetting("low_health_color", "Low Health Color", "Kolor niskiego zdrowia (<= 6 HP).", 0xFFFF5555);
        this.absorptionColorSetting = new ColorSetting("absorption_color", "Absorption Color", "Kolor absorpcji.", 0xFFFFAA00);
        this.maxHealthDisplaySetting = new BooleanSetting("max_health_display", "Show Max Health", "Pokazuje maksymalne zdrowie (/20).", false);
        this.decimalPrecisionSetting = new NumberSetting("decimal_precision", "Decimal Precision", "Miejsca po przecinku.", 1.0, 0.0, 2.0, 1.0);
        this.smoothTransitionSetting = new BooleanSetting("smooth_transition", "Smooth Transition", "Płynna zmiana wartości zdrowia.", true);

        this.groupHealth.addSetting(healthStyleSetting);
        this.groupHealth.addSetting(healthColorSetting);
        this.groupHealth.addSetting(lowHealthColorSetting);
        this.groupHealth.addSetting(absorptionColorSetting);
        this.groupHealth.addSetting(maxHealthDisplaySetting);
        this.groupHealth.addSetting(decimalPrecisionSetting);
        this.groupHealth.addSetting(smoothTransitionSetting);

        // 5. Distance
        this.groupDistance = new SettingGroup("distance", "Distance Display");
        this.distanceFormatSetting = new EnumSetting<>("distance_format", "Distance Format", "Format wyświetlania odległości.", DistanceFormat.BLOCKS);
        this.maxDisplayDistanceSetting = new NumberSetting("max_distance", "Max Display Distance", "Maksymalny dystans wyświetlania metrażu.", 64.0, 5.0, 128.0, 1.0);
        this.minDisplayDistanceSetting = new NumberSetting("min_distance", "Min Display Distance", "Minimalny dystans wyświetlania metrażu.", 0.0, 0.0, 32.0, 1.0);

        this.groupDistance.addSetting(distanceFormatSetting);
        this.groupDistance.addSetting(maxDisplayDistanceSetting);
        this.groupDistance.addSetting(minDisplayDistanceSetting);

        // 6. Armor
        this.groupArmor = new SettingGroup("armor", "Armor & Equipment");
        this.showDurabilitySetting = new BooleanSetting("show_durability", "Show Durability", "Pokazuje procent trwałości.", true);
        this.showArmorIconsSetting = new BooleanSetting("show_armor_icons", "Show Icons", "Renderuje ikony elementów zbroi.", true);
        this.lowDurabilityWarningSetting = new BooleanSetting("low_durability_warning", "Low Durability Warning", "Ostrzega o niskiej trwałości (<20%).", true);

        this.groupArmor.addSetting(showDurabilitySetting);
        this.groupArmor.addSetting(showArmorIconsSetting);
        this.groupArmor.addSetting(lowDurabilityWarningSetting);

        // 7. Appearance
        this.groupAppearance = new SettingGroup("appearance", "Appearance");
        this.layoutSetting = new EnumSetting<>("layout", "Layout Mode", "Styl układu NameTagu.", NameTagLayout.MODERN);
        this.scaleSetting = new NumberSetting("scale", "Scale", "Bazowa skala NameTagu.", 1.0, 0.5, 2.5, 0.05);
        this.dynamicScaleSetting = new BooleanSetting("dynamic_scale", "Dynamic Distance Scaling", "Płynnie skaluje NameTag z odległością.", true);
        this.constantScaleSetting = new BooleanSetting("constant_scale", "Constant Visual Size", "Utrzymuje stały rozmiar kątowy na ekranie.", false);
        this.scaleMultiplierSetting = new NumberSetting("scale_multiplier", "Scale Multiplier", "Mnożnik skali.", 1.0, 0.5, 3.0, 0.1);
        this.backgroundColorSetting = new ColorSetting("background_color", "Background Color", "Kolor tła NameTagu.", 0x000000);
        this.backgroundOpacitySetting = new NumberSetting("background_opacity", "Background Opacity", "Przezroczystość tła (%).", 60.0, 0.0, 100.0, 5.0);
        this.borderColorSetting = new ColorSetting("border_color", "Border Color", "Kolor obramowania.", 0x00000000);
        this.showBorderSetting = new BooleanSetting("show_border", "Show Border", "Czy renderować ramkę.", false);
        this.borderThicknessSetting = new NumberSetting("border_thickness", "Border Thickness", "Grubość ramki.", 1.0, 1.0, 4.0, 1.0);
        this.shadowSetting = new BooleanSetting("shadow", "Text Shadow", "Cień pod napisami.", true);
        this.spacingSetting = new NumberSetting("spacing", "Element Spacing", "Odstęp między elementami.", 3.0, 1.0, 10.0, 1.0);
        this.cornerRadiusSetting = new NumberSetting("corner_radius", "Corner Radius", "Zaokrąglenie rogów tła.", 0.0, 0.0, 8.0, 1.0);

        this.groupAppearance.addSetting(layoutSetting);
        this.groupAppearance.addSetting(scaleSetting);
        this.groupAppearance.addSetting(dynamicScaleSetting);
        this.groupAppearance.addSetting(constantScaleSetting);
        this.groupAppearance.addSetting(scaleMultiplierSetting);
        this.groupAppearance.addSetting(backgroundColorSetting);
        this.groupAppearance.addSetting(backgroundOpacitySetting);
        this.groupAppearance.addSetting(borderColorSetting);
        this.groupAppearance.addSetting(showBorderSetting);
        this.groupAppearance.addSetting(borderThicknessSetting);
        this.groupAppearance.addSetting(shadowSetting);
        this.groupAppearance.addSetting(spacingSetting);
        this.groupAppearance.addSetting(cornerRadiusSetting);

        // 8. Visibility
        this.groupVisibility = new SettingGroup("visibility", "Visibility & Rules");
        this.respectVanillaVisibilitySetting = new BooleanSetting("respect_vanilla_visibility", "Respect Vanilla Rules", "Rygorystycznie przestrzega reguł widoczności Minecraft.", true);
        this.maxRenderDistanceSetting = new NumberSetting("max_render_distance", "Max Render Distance", "Maksymalny zasięg renderowania (bloki).", 64.0, 8.0, 128.0, 4.0);
        this.hideInGuiSetting = new BooleanSetting("hide_in_gui", "Hide in GUI", "Ukrywa NameTagi gdy otwarte jest menu gry.", true);
        this.hideInSpectatorSetting = new BooleanSetting("hide_in_spectator", "Hide in Spectator", "Ukrywa NameTagi graczy w trybie Spectator.", true);
        this.hideInvisibleSetting = new BooleanSetting("hide_invisible", "Hide Invisible Entities", "Ukrywa NameTagi niewidzialnych encji.", true);
        this.hideSneakingSetting = new BooleanSetting("hide_sneaking", "Hide Sneaking Players", "Ukrywa NameTagi skradających się graczy.", true);

        this.groupVisibility.addSetting(respectVanillaVisibilitySetting);
        this.groupVisibility.addSetting(maxRenderDistanceSetting);
        this.groupVisibility.addSetting(hideInGuiSetting);
        this.groupVisibility.addSetting(hideInSpectatorSetting);
        this.groupVisibility.addSetting(hideInvisibleSetting);
        this.groupVisibility.addSetting(hideSneakingSetting);

        // 9. Position
        this.groupPosition = new SettingGroup("position", "Offset & Height");
        this.verticalOffsetSetting = new NumberSetting("vertical_offset", "Vertical Offset", "Pionowe przesunięcie NameTagu.", 0.0, -2.0, 2.0, 0.1);
        this.horizontalOffsetSetting = new NumberSetting("horizontal_offset", "Horizontal Offset", "Poziome przesunięcie NameTagu.", 0.0, -2.0, 2.0, 0.1);
        this.nametagHeightSetting = new NumberSetting("nametag_height", "Height Multiplier", "Wysokość nad głową encji.", 1.0, 0.5, 2.0, 0.1);

        this.groupPosition.addSetting(verticalOffsetSetting);
        this.groupPosition.addSetting(horizontalOffsetSetting);
        this.groupPosition.addSetting(nametagHeightSetting);

        registerGroup(groupGeneral);
        registerGroup(groupPlayerInfo);
        registerGroup(groupCustomName);
        registerGroup(groupHealth);
        registerGroup(groupDistance);
        registerGroup(groupArmor);
        registerGroup(groupAppearance);
        registerGroup(groupVisibility);
        registerGroup(groupPosition);
    }

    public static boolean shouldOverrideVanilla() {
        NameTagsModule mod = (NameTagsModule) ModuleManager.getInstance().getModuleById(ID);
        return mod != null && mod.isEnabled() && mod.getUseCustomRendererSetting().getValue();
    }

    @Override
    public String getElementId() {
        return ID;
    }

    @Override
    public boolean isVisible() {
        return isEnabled() && !hasError();
    }

    @Override
    public void render(IHudRenderer renderer) {
        if (!isVisible()) return;
        // In 2D HUD / Right Shift menu, renders an informative NameTag preview widget
        CustomNameTagRenderer.renderPreview(renderer, this, 10, renderer.getScreenHeight() / 2 - 20);
    }

    public HudComponentModel getHudModel() { return model; }

    public BooleanSetting getTargetPlayersSetting() { return targetPlayersSetting; }
    public BooleanSetting getTargetLivingSetting() { return targetLivingSetting; }
    public BooleanSetting getTargetOtherSetting() { return targetOtherSetting; }
    public BooleanSetting getUseCustomRendererSetting() { return useCustomRendererSetting; }
    public BooleanSetting getPreserveFormattingSetting() { return preserveFormattingSetting; }

    public BooleanSetting getShowNameSetting() { return showNameSetting; }
    public BooleanSetting getShowHealthSetting() { return showHealthSetting; }
    public BooleanSetting getShowAbsorptionSetting() { return showAbsorptionSetting; }
    public BooleanSetting getShowDistanceSetting() { return showDistanceSetting; }
    public BooleanSetting getShowTeamSetting() { return showTeamSetting; }
    public BooleanSetting getShowPingSetting() { return showPingSetting; }
    public BooleanSetting getShowFriendMarkerSetting() { return showFriendMarkerSetting; }
    public BooleanSetting getShowArmorSetting() { return showArmorSetting; }
    public BooleanSetting getShowHeldItemSetting() { return showHeldItemSetting; }

    public BooleanSetting getEnableAliasSetting() { return enableAliasSetting; }
    public BooleanSetting getShowCustomPrefixSetting() { return showCustomPrefixSetting; }
    public StringSetting getCustomPrefixSetting() { return customPrefixSetting; }
    public BooleanSetting getShowCustomSuffixSetting() { return showCustomSuffixSetting; }
    public StringSetting getCustomSuffixSetting() { return customSuffixSetting; }
    public StringSetting getFriendMarkerIconSetting() { return friendMarkerIconSetting; }

    public EnumSetting<HealthDisplayStyle> getHealthStyleSetting() { return healthStyleSetting; }
    public ColorSetting getHealthColorSetting() { return healthColorSetting; }
    public ColorSetting getLowHealthColorSetting() { return lowHealthColorSetting; }
    public ColorSetting getAbsorptionColorSetting() { return absorptionColorSetting; }
    public BooleanSetting getMaxHealthDisplaySetting() { return maxHealthDisplaySetting; }
    public NumberSetting getDecimalPrecisionSetting() { return decimalPrecisionSetting; }
    public BooleanSetting getSmoothTransitionSetting() { return smoothTransitionSetting; }

    public EnumSetting<DistanceFormat> getDistanceFormatSetting() { return distanceFormatSetting; }
    public NumberSetting getMaxDisplayDistanceSetting() { return maxDisplayDistanceSetting; }
    public NumberSetting getMinDisplayDistanceSetting() { return minDisplayDistanceSetting; }

    public BooleanSetting getShowDurabilitySetting() { return showDurabilitySetting; }
    public BooleanSetting getShowArmorIconsSetting() { return showArmorIconsSetting; }
    public BooleanSetting getLowDurabilityWarningSetting() { return lowDurabilityWarningSetting; }

    public EnumSetting<NameTagLayout> getLayoutSetting() { return layoutSetting; }
    public NumberSetting getScaleSetting() { return scaleSetting; }
    public BooleanSetting getDynamicScaleSetting() { return dynamicScaleSetting; }
    public BooleanSetting getConstantScaleSetting() { return constantScaleSetting; }
    public NumberSetting getScaleMultiplierSetting() { return scaleMultiplierSetting; }
    public ColorSetting getBackgroundColorSetting() { return backgroundColorSetting; }
    public NumberSetting getBackgroundOpacitySetting() { return backgroundOpacitySetting; }
    public ColorSetting getBorderColorSetting() { return borderColorSetting; }
    public BooleanSetting getShowBorderSetting() { return showBorderSetting; }
    public NumberSetting getBorderThicknessSetting() { return borderThicknessSetting; }
    public BooleanSetting getShadowSetting() { return shadowSetting; }
    public NumberSetting getSpacingSetting() { return spacingSetting; }
    public NumberSetting getCornerRadiusSetting() { return cornerRadiusSetting; }

    public BooleanSetting getRespectVanillaVisibilitySetting() { return respectVanillaVisibilitySetting; }
    public NumberSetting getMaxRenderDistanceSetting() { return maxRenderDistanceSetting; }
    public BooleanSetting getHideInGuiSetting() { return hideInGuiSetting; }
    public BooleanSetting getHideInSpectatorSetting() { return hideInSpectatorSetting; }
    public BooleanSetting getHideInvisibleSetting() { return hideInvisibleSetting; }
    public BooleanSetting getHideSneakingSetting() { return hideSneakingSetting; }

    public NumberSetting getVerticalOffsetSetting() { return verticalOffsetSetting; }
    public NumberSetting getHorizontalOffsetSetting() { return horizontalOffsetSetting; }
    public NumberSetting getNametagHeightSetting() { return nametagHeightSetting; }
}
