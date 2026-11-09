package pl.zombek.client.core.modules.survival;

import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.BooleanSetting;
import pl.zombek.client.core.settings.EnumSetting;
import pl.zombek.client.core.settings.NumberSetting;
import pl.zombek.client.core.settings.SettingGroup;
import pl.zombek.client.core.uhc.UhcItemScalingController;
import pl.zombek.client.core.uhc.UhcScaleMode;

/**
 * UHC Overlay Module (True dropped item scaling).
 * Dynamically scales key dropped items on the ground (Golden Apples, Golden Heads, Ender Pearls, Arrows, etc.)
 * for rapid identification during looting, strictly preserving vanilla physics, bounding boxes, and server mechanics.
 */
public class UHCOverlayModule extends AbstractModule {
    private static UHCOverlayModule instance;

    // General
    private final SettingGroup groupGeneral;
    private final NumberSetting globalScaleSetting;
    private final EnumSetting<UhcScaleMode> scaleModeSetting;
    private final BooleanSetting resetDefaultsSetting;

    // Items
    private final SettingGroup groupItems;
    private final NumberSetting goldenApplesScaleSetting;
    private final NumberSetting goldenHeadsScaleSetting;
    private final NumberSetting enderPearlsScaleSetting;
    private final NumberSetting arrowsScaleSetting;
    private final NumberSetting healingItemsScaleSetting;
    private final NumberSetting blocksScaleSetting;
    private final NumberSetting bucketsScaleSetting;
    private final NumberSetting flintAndSteelScaleSetting;

    // Appearance
    private final SettingGroup groupAppearance;
    private final BooleanSetting preserveAnimationSetting;
    private final BooleanSetting preserveRotationSetting;
    private final BooleanSetting preserveLightingSetting;
    private final BooleanSetting preserveShadowsSetting;

    public UHCOverlayModule() {
        super(
                "uhc_overlay",
                "UHC Overlay",
                "Powiększanie wybranych przedmiotów leżących na ziemi (koxy, złote głowy, perły, strzały) dla łatwiejszego lootu w UHC.",
                ModuleCategory.SURVIVAL
        );
        instance = this;

        // General
        this.groupGeneral = new SettingGroup("general", "General");
        this.globalScaleSetting = new NumberSetting(
                "global_scale", "Global Scale",
                "Globalny mnożnik rozmiaru przedmiotów na ziemi.", 1.5, 0.5, 5.0, 0.1
        );
        this.scaleModeSetting = new EnumSetting<>(
                "scale_mode", "Scale Mode",
                "Tryb kalkulacji skali (indywidualny, globalny lub łączony).", UhcScaleMode.PER_ITEM
        );
        this.resetDefaultsSetting = new BooleanSetting(
                "reset_defaults", "Reset to Default",
                "Przywraca domyślne skale przedmiotów UHC.", false
        );
        this.groupGeneral.addSetting(globalScaleSetting);
        this.groupGeneral.addSetting(scaleModeSetting);
        this.groupGeneral.addSetting(resetDefaultsSetting);
        registerGroup(this.groupGeneral);

        // Items
        this.groupItems = new SettingGroup("items", "Per Item Scales");
        this.goldenApplesScaleSetting = new NumberSetting(
                "golden_apples_scale", "Golden Apples Scale",
                "Skala złotych jabłek (GApples / Koxy).", 2.0, 0.5, 5.0, 0.1
        );
        this.goldenHeadsScaleSetting = new NumberSetting(
                "golden_heads_scale", "Golden Heads Scale",
                "Skala złotych głów (Golden Heads).", 2.0, 0.5, 5.0, 0.1
        );
        this.enderPearlsScaleSetting = new NumberSetting(
                "ender_pearls_scale", "Ender Pearls Scale",
                "Skala pereł kresu (Ender Pearls).", 1.8, 0.5, 5.0, 0.1
        );
        this.arrowsScaleSetting = new NumberSetting(
                "arrows_scale", "Arrows Scale",
                "Skala strzał leżących na ziemi.", 1.5, 0.5, 5.0, 0.1
        );
        this.healingItemsScaleSetting = new NumberSetting(
                "healing_items_scale", "Healing Items Scale",
                "Skala mikstur leczniczych i regeneracji.", 1.6, 0.5, 5.0, 0.1
        );
        this.blocksScaleSetting = new NumberSetting(
                "blocks_scale", "Blocks Scale",
                "Skala klocków budowlanych (cobblestone, drewno).", 1.2, 0.5, 5.0, 0.1
        );
        this.bucketsScaleSetting = new NumberSetting(
                "buckets_scale", "Buckets Scale",
                "Skala wiader wody i lawy.", 1.4, 0.5, 5.0, 0.1
        );
        this.flintAndSteelScaleSetting = new NumberSetting(
                "flint_and_steel_scale", "Flint & Steel Scale",
                "Skala zapalniczki (Flint and Steel).", 1.4, 0.5, 5.0, 0.1
        );
        this.groupItems.addSetting(goldenApplesScaleSetting);
        this.groupItems.addSetting(goldenHeadsScaleSetting);
        this.groupItems.addSetting(enderPearlsScaleSetting);
        this.groupItems.addSetting(arrowsScaleSetting);
        this.groupItems.addSetting(healingItemsScaleSetting);
        this.groupItems.addSetting(blocksScaleSetting);
        this.groupItems.addSetting(bucketsScaleSetting);
        this.groupItems.addSetting(flintAndSteelScaleSetting);
        registerGroup(this.groupItems);

        // Appearance
        this.groupAppearance = new SettingGroup("appearance", "Rendering & Appearance");
        this.preserveAnimationSetting = new BooleanSetting(
                "preserve_animation", "Preserve Vanilla Animation",
                "Zachowaj natywną animację unoszenia i kołysania przedmiotu.", true
        );
        this.preserveRotationSetting = new BooleanSetting(
                "preserve_rotation", "Preserve Vanilla Rotation",
                "Zachowaj płynną rotację przedmiotu.", true
        );
        this.preserveLightingSetting = new BooleanSetting(
                "preserve_lighting", "Preserve Vanilla Lighting",
                "Zachowaj oryginalne cieniowanie i oświetlenie świata.", true
        );
        this.preserveShadowsSetting = new BooleanSetting(
                "preserve_shadows", "Preserve Shadows",
                "Renderuj cień przedmiotu.", true
        );
        this.groupAppearance.addSetting(preserveAnimationSetting);
        this.groupAppearance.addSetting(preserveRotationSetting);
        this.groupAppearance.addSetting(preserveLightingSetting);
        this.groupAppearance.addSetting(preserveShadowsSetting);
        registerGroup(this.groupAppearance);
    }

    public static UHCOverlayModule getInstance() {
        return instance;
    }

    @Override
    public void onInitialize() {
        // Init
    }

    @Override
    public void onEnable() {
        // Enabled
    }

    @Override
    public void onDisable() {
        // Disabled
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;

        if (resetDefaultsSetting.getValue()) {
            globalScaleSetting.setValue(1.5);
            scaleModeSetting.setValue(UhcScaleMode.PER_ITEM);
            goldenApplesScaleSetting.setValue(2.0);
            goldenHeadsScaleSetting.setValue(2.0);
            enderPearlsScaleSetting.setValue(1.8);
            arrowsScaleSetting.setValue(1.5);
            healingItemsScaleSetting.setValue(1.6);
            blocksScaleSetting.setValue(1.2);
            bucketsScaleSetting.setValue(1.4);
            flintAndSteelScaleSetting.setValue(1.4);
            preserveAnimationSetting.setValue(true);
            preserveRotationSetting.setValue(true);
            preserveLightingSetting.setValue(true);
            preserveShadowsSetting.setValue(true);
            UhcItemScalingController.getInstance().clearCustomRules();
            resetDefaultsSetting.setValue(false);
        }
    }

    // Getters for settings
    public NumberSetting getGlobalScaleSetting() { return globalScaleSetting; }
    public EnumSetting<UhcScaleMode> getScaleModeSetting() { return scaleModeSetting; }
    public NumberSetting getGoldenApplesScaleSetting() { return goldenApplesScaleSetting; }
    public NumberSetting getGoldenHeadsScaleSetting() { return goldenHeadsScaleSetting; }
    public NumberSetting getEnderPearlsScaleSetting() { return enderPearlsScaleSetting; }
    public NumberSetting getArrowsScaleSetting() { return arrowsScaleSetting; }
    public NumberSetting getHealingItemsScaleSetting() { return healingItemsScaleSetting; }
    public NumberSetting getBlocksScaleSetting() { return blocksScaleSetting; }
    public NumberSetting getBucketsScaleSetting() { return bucketsScaleSetting; }
    public NumberSetting getFlintAndSteelScaleSetting() { return flintAndSteelScaleSetting; }
    public BooleanSetting getPreserveAnimationSetting() { return preserveAnimationSetting; }
    public BooleanSetting getPreserveRotationSetting() { return preserveRotationSetting; }
    public BooleanSetting getPreserveLightingSetting() { return preserveLightingSetting; }
    public BooleanSetting getPreserveShadowsSetting() { return preserveShadowsSetting; }
}
