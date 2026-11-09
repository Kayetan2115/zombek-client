package pl.zombek.client.core.modules.visual;

import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.rendereffect.particle.*;
import pl.zombek.client.core.settings.*;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Particle Changer Module granting deep customization of particle counts, scales,
 * category throttling, performance limits, and custom particle rules.
 */
public class ParticleChangerModule extends AbstractModule {
    public static final String ID = "particle_changer";
    private static ParticleChangerModule instance;

    // General
    private final EnumSetting<ParticlePreset> presetSetting;
    private final NumberSetting globalDensitySetting;
    private final NumberSetting globalScaleSetting;
    private final NumberSetting globalOpacitySetting;

    // Category Multipliers
    private final NumberSetting combatDensitySetting;
    private final NumberSetting environmentDensitySetting;
    private final NumberSetting magicDensitySetting;
    private final NumberSetting blocksDensitySetting;
    private final NumberSetting itemsDensitySetting;
    private final NumberSetting otherDensitySetting;

    // Performance
    private final NumberSetting maxParticleLimitSetting;
    private final NumberSetting maxDistanceCutoffSetting;

    // Custom Rules (Combat/Crit)
    private final NumberSetting critScaleSetting;
    private final NumberSetting critDensitySetting;
    private final BooleanSetting customCritColorSetting;
    private final ColorSetting critColorSetting;

    private final NumberSetting sharpScaleSetting;
    private final NumberSetting sharpDensitySetting;
    private final BooleanSetting customSharpColorSetting;
    private final ColorSetting sharpColorSetting;

    // Behavior
    private final BooleanSetting resetOnWorldChangeSetting;

    public ParticleChangerModule() {
        super(
                ID,
                "Particle Changer",
                "Personalizacja ilości, skali, barw i widoczności cząsteczek z ochroną wydajności.",
                ModuleCategory.VISUAL,
                GLFW.GLFW_KEY_UNKNOWN
        );
        instance = this;

        // 1. General
        SettingGroup generalGroup = new SettingGroup("general", "General");
        this.presetSetting = new EnumSetting<>(
                "preset", "Particle Preset", "Wybór profilu cząstek (Vanilla, Low, Medium, High, Reduced, Minimal, Custom)",
                ParticlePreset.VANILLA
        );
        this.globalDensitySetting = new NumberSetting(
                "global_density", "Global Density", "Mnożnik zagęszczenia cząsteczek (0.0x - 3.0x)", 1.0, 0.0, 3.0, 0.05, 2, "x"
        );
        this.globalScaleSetting = new NumberSetting(
                "global_scale", "Global Scale", "Mnożnik rozmiaru renderowanych cząsteczek", 1.0, 0.1, 3.0, 0.05, 2, "x"
        );
        this.globalOpacitySetting = new NumberSetting(
                "global_opacity", "Global Opacity", "Przezroczystość cząsteczek", 100.0, 10.0, 100.0, 5.0, 0, "%"
        );
        generalGroup.addSetting(presetSetting);
        generalGroup.addSetting(globalDensitySetting);
        generalGroup.addSetting(globalScaleSetting);
        generalGroup.addSetting(globalOpacitySetting);
        addSettingGroup(generalGroup);

        // 2. Categories
        SettingGroup categoryGroup = new SettingGroup("categories", "Category Densities");
        this.combatDensitySetting = new NumberSetting(
                "combat_density", "Combat Particles", "Mnożnik dla cząstek walki (krytyki, ciosy, zamachy)", 1.0, 0.0, 3.0, 0.05, 2, "x"
        );
        this.environmentDensitySetting = new NumberSetting(
                "environment_density", "Environment Particles", "Mnożnik dla deszczu, dymu, rozbryzgów wody", 1.0, 0.0, 3.0, 0.05, 2, "x"
        );
        this.magicDensitySetting = new NumberSetting(
                "magic_density", "Magic Particles", "Mnożnik dla mikstur, zaklęć, portali", 1.0, 0.0, 3.0, 0.05, 2, "x"
        );
        this.blocksDensitySetting = new NumberSetting(
                "blocks_density", "Block Particles", "Mnożnik dla niszczenia i sypania się bloków", 1.0, 0.0, 3.0, 0.05, 2, "x"
        );
        this.itemsDensitySetting = new NumberSetting(
                "items_density", "Item Particles", "Mnożnik dla pękających przedmiotów", 1.0, 0.0, 3.0, 0.05, 2, "x"
        );
        this.otherDensitySetting = new NumberSetting(
                "other_density", "Other Particles", "Mnożnik dla serc, nutek, wieśniaków i pozostałych", 1.0, 0.0, 3.0, 0.05, 2, "x"
        );
        categoryGroup.addSetting(combatDensitySetting);
        categoryGroup.addSetting(environmentDensitySetting);
        categoryGroup.addSetting(magicDensitySetting);
        categoryGroup.addSetting(blocksDensitySetting);
        categoryGroup.addSetting(itemsDensitySetting);
        categoryGroup.addSetting(otherDensitySetting);
        addSettingGroup(categoryGroup);

        // 3. Performance
        SettingGroup perfGroup = new SettingGroup("performance", "Performance Guards");
        this.maxParticleLimitSetting = new NumberSetting(
                "max_particles", "Max Active Particles", "Maksymalny twardy limit aktywnych cząstek na scenie", 2000.0, 50.0, 5000.0, 50.0, 0, ""
        );
        this.maxDistanceCutoffSetting = new NumberSetting(
                "max_distance", "Distance Cutoff", "Maksymalna odległość generowania cząstek od gracza", 64.0, 8.0, 128.0, 2.0, 0, "m"
        );
        perfGroup.addSetting(maxParticleLimitSetting);
        perfGroup.addSetting(maxDistanceCutoffSetting);
        addSettingGroup(perfGroup);

        // 4. Custom Rules
        SettingGroup rulesGroup = new SettingGroup("rules", "Custom Particle Rules");
        this.critScaleSetting = new NumberSetting("crit_scale", "Crit Particle Scale", "Skala cząstek ciosu krytycznego", 1.0, 0.2, 3.0, 0.1, 1, "x");
        this.critDensitySetting = new NumberSetting("crit_density", "Crit Particle Density", "Gęstość cząstek ciosu krytycznego", 1.0, 0.0, 3.0, 0.1, 1, "x");
        this.customCritColorSetting = new BooleanSetting("custom_crit_color", "Custom Crit Color", "Własny kolor cząstek krytyków", false);
        this.critColorSetting = new ColorSetting("crit_color", "Crit Color", "Kolor cząstek krytyka", 0xFFFFCC00);

        this.sharpScaleSetting = new NumberSetting("sharp_scale", "Sharpness Scale", "Skala cząstek ostrości (enchanted hit)", 1.0, 0.2, 3.0, 0.1, 1, "x");
        this.sharpDensitySetting = new NumberSetting("sharp_density", "Sharpness Density", "Gęstość cząstek ostrości", 1.0, 0.0, 3.0, 0.1, 1, "x");
        this.customSharpColorSetting = new BooleanSetting("custom_sharp_color", "Custom Sharpness Color", "Własny kolor cząstek ostrości", false);
        this.sharpColorSetting = new ColorSetting("sharp_color", "Sharpness Color", "Kolor cząstek ostrości", 0xFF00FFFF);

        rulesGroup.addSetting(critScaleSetting);
        rulesGroup.addSetting(critDensitySetting);
        rulesGroup.addSetting(customCritColorSetting);
        rulesGroup.addSetting(critColorSetting);
        rulesGroup.addSetting(sharpScaleSetting);
        rulesGroup.addSetting(sharpDensitySetting);
        rulesGroup.addSetting(customSharpColorSetting);
        rulesGroup.addSetting(sharpColorSetting);
        addSettingGroup(rulesGroup);

        // 5. Behavior
        SettingGroup behaviorGroup = new SettingGroup("behavior", "Behavior");
        this.resetOnWorldChangeSetting = new BooleanSetting(
                "reset_on_world_change", "Reset on World Change", "Przywróć domyślne cząsteczki przy zmianie świata lub rozłączeniu", false
        );
        behaviorGroup.addSetting(resetOnWorldChangeSetting);
        addSettingGroup(behaviorGroup);

        // Listeners
        this.presetSetting.addChangeListener(n -> {
            if (n != ParticlePreset.CUSTOM) {
                applyPresetValues(n);
            }
            syncToController();
        });

        this.globalDensitySetting.addChangeListener(n -> syncToController());
        this.globalScaleSetting.addChangeListener(n -> syncToController());
        this.globalOpacitySetting.addChangeListener(n -> syncToController());
        this.combatDensitySetting.addChangeListener(n -> syncToController());
        this.environmentDensitySetting.addChangeListener(n -> syncToController());
        this.magicDensitySetting.addChangeListener(n -> syncToController());
        this.blocksDensitySetting.addChangeListener(n -> syncToController());
        this.itemsDensitySetting.addChangeListener(n -> syncToController());
        this.otherDensitySetting.addChangeListener(n -> syncToController());
        this.maxParticleLimitSetting.addChangeListener(n -> syncToController());
        this.maxDistanceCutoffSetting.addChangeListener(n -> syncToController());
        this.critScaleSetting.addChangeListener(n -> syncToController());
        this.critDensitySetting.addChangeListener(n -> syncToController());
        this.customCritColorSetting.addChangeListener(n -> syncToController());
        this.critColorSetting.addChangeListener(n -> syncToController());
        this.sharpScaleSetting.addChangeListener(n -> syncToController());
        this.sharpDensitySetting.addChangeListener(n -> syncToController());
        this.customSharpColorSetting.addChangeListener(n -> syncToController());
        this.sharpColorSetting.addChangeListener(n -> syncToController());
    }

    public static ParticleChangerModule getInstance() {
        return instance;
    }

    private void applyPresetValues(ParticlePreset preset) {
        switch (preset) {
            case VANILLA -> {
                globalDensitySetting.setValue(1.0);
                combatDensitySetting.setValue(1.0);
                environmentDensitySetting.setValue(1.0);
                magicDensitySetting.setValue(1.0);
                blocksDensitySetting.setValue(1.0);
                itemsDensitySetting.setValue(1.0);
                otherDensitySetting.setValue(1.0);
                maxParticleLimitSetting.setValue(2000.0);
            }
            case LOW -> {
                globalDensitySetting.setValue(0.5);
                combatDensitySetting.setValue(0.7);
                environmentDensitySetting.setValue(0.3);
                magicDensitySetting.setValue(0.4);
                blocksDensitySetting.setValue(0.3);
                itemsDensitySetting.setValue(0.4);
                otherDensitySetting.setValue(0.3);
                maxParticleLimitSetting.setValue(1000.0);
            }
            case MEDIUM -> {
                globalDensitySetting.setValue(0.8);
                combatDensitySetting.setValue(1.0);
                environmentDensitySetting.setValue(0.7);
                magicDensitySetting.setValue(0.8);
                blocksDensitySetting.setValue(0.6);
                itemsDensitySetting.setValue(0.8);
                otherDensitySetting.setValue(0.6);
                maxParticleLimitSetting.setValue(1500.0);
            }
            case HIGH -> {
                globalDensitySetting.setValue(1.5);
                combatDensitySetting.setValue(1.5);
                environmentDensitySetting.setValue(1.2);
                magicDensitySetting.setValue(1.3);
                blocksDensitySetting.setValue(1.2);
                itemsDensitySetting.setValue(1.3);
                otherDensitySetting.setValue(1.2);
                maxParticleLimitSetting.setValue(3000.0);
            }
            case REDUCED_PARTICLES -> {
                globalDensitySetting.setValue(0.6);
                combatDensitySetting.setValue(1.0);
                environmentDensitySetting.setValue(0.1);
                magicDensitySetting.setValue(0.5);
                blocksDensitySetting.setValue(0.2);
                itemsDensitySetting.setValue(0.3);
                otherDensitySetting.setValue(0.2);
                maxParticleLimitSetting.setValue(800.0);
            }
            case MINIMAL_PARTICLES -> {
                globalDensitySetting.setValue(0.2);
                combatDensitySetting.setValue(0.5);
                environmentDensitySetting.setValue(0.0);
                magicDensitySetting.setValue(0.2);
                blocksDensitySetting.setValue(0.0);
                itemsDensitySetting.setValue(0.0);
                otherDensitySetting.setValue(0.0);
                maxParticleLimitSetting.setValue(300.0);
            }
            case CUSTOM -> {}
        }
    }

    public void syncToController() {
        if (!isEnabled()) {
            ParticleRenderController.getInstance().setActive(false);
            return;
        }

        Map<ParticleCategory, Float> catMap = new EnumMap<>(ParticleCategory.class);
        catMap.put(ParticleCategory.COMBAT, combatDensitySetting.getValue().floatValue());
        catMap.put(ParticleCategory.ENVIRONMENT, environmentDensitySetting.getValue().floatValue());
        catMap.put(ParticleCategory.MAGIC, magicDensitySetting.getValue().floatValue());
        catMap.put(ParticleCategory.BLOCKS, blocksDensitySetting.getValue().floatValue());
        catMap.put(ParticleCategory.ITEMS, itemsDensitySetting.getValue().floatValue());
        catMap.put(ParticleCategory.OTHER, otherDensitySetting.getValue().floatValue());

        Map<String, CustomParticleRule> rules = new HashMap<>();
        rules.put("crit", new CustomParticleRule(
                "crit", true,
                critDensitySetting.getValue().floatValue(),
                critScaleSetting.getValue().floatValue(),
                customCritColorSetting.getValue(),
                critColorSetting.getValue(),
                (float) (globalOpacitySetting.getValue() / 100.0)
        ));
        rules.put("enchanted_hit", new CustomParticleRule(
                "enchanted_hit", true,
                sharpDensitySetting.getValue().floatValue(),
                sharpScaleSetting.getValue().floatValue(),
                customSharpColorSetting.getValue(),
                sharpColorSetting.getValue(),
                (float) (globalOpacitySetting.getValue() / 100.0)
        ));

        ParticleVisualSettings settings = new ParticleVisualSettings(
                globalDensitySetting.getValue().floatValue(),
                globalScaleSetting.getValue().floatValue(),
                (float) (globalOpacitySetting.getValue() / 100.0),
                maxParticleLimitSetting.getValue().intValue(),
                maxDistanceCutoffSetting.getValue().floatValue(),
                catMap,
                rules
        );

        ParticleRenderController.getInstance().setActive(true);
        ParticleRenderController.getInstance().updateSettings(settings);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        syncToController();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        ParticleRenderController.getInstance().setActive(false);
    }

    public void onWorldChange() {
        if (resetOnWorldChangeSetting.getValue()) {
            presetSetting.setValue(ParticlePreset.VANILLA);
            applyPresetValues(ParticlePreset.VANILLA);
            syncToController();
        }
    }

    // Getters
    public EnumSetting<ParticlePreset> getPresetSetting() { return presetSetting; }
    public NumberSetting getGlobalDensitySetting() { return globalDensitySetting; }
    public NumberSetting getGlobalScaleSetting() { return globalScaleSetting; }
    public NumberSetting getGlobalOpacitySetting() { return globalOpacitySetting; }
    public NumberSetting getCombatDensitySetting() { return combatDensitySetting; }
    public NumberSetting getEnvironmentDensitySetting() { return environmentDensitySetting; }
    public NumberSetting getMagicDensitySetting() { return magicDensitySetting; }
    public NumberSetting getBlocksDensitySetting() { return blocksDensitySetting; }
    public NumberSetting getItemsDensitySetting() { return itemsDensitySetting; }
    public NumberSetting getOtherDensitySetting() { return otherDensitySetting; }
    public NumberSetting getMaxParticleLimitSetting() { return maxParticleLimitSetting; }
    public NumberSetting getMaxDistanceCutoffSetting() { return maxDistanceCutoffSetting; }
    public NumberSetting getCritScaleSetting() { return critScaleSetting; }
    public NumberSetting getCritDensitySetting() { return critDensitySetting; }
    public BooleanSetting getCustomCritColorSetting() { return customCritColorSetting; }
    public ColorSetting getCritColorSetting() { return critColorSetting; }
    public NumberSetting getSharpScaleSetting() { return sharpScaleSetting; }
    public NumberSetting getSharpDensitySetting() { return sharpDensitySetting; }
    public BooleanSetting getCustomSharpColorSetting() { return customSharpColorSetting; }
    public ColorSetting getSharpColorSetting() { return sharpColorSetting; }
    public BooleanSetting getResetOnWorldChangeSetting() { return resetOnWorldChangeSetting; }
}
