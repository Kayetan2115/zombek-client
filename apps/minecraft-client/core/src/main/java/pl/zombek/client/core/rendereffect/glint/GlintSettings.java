package pl.zombek.client.core.rendereffect.glint;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Snapshot of glint coloring settings.
 */
public class GlintSettings {
    private final GlintMode mode;
    private final int primaryColor;
    private final int secondaryColor;
    private final float speed;
    private final float opacity;
    private final ShinyPotsPriority shinyPotsPriority;
    private final Set<GlintLocation> enabledLocations;
    private final Map<GlintItemCategory, CategoryGlintConfig> categoryConfigs;

    public GlintSettings(
            GlintMode mode,
            int primaryColor,
            int secondaryColor,
            float speed,
            float opacity,
            ShinyPotsPriority shinyPotsPriority,
            Set<GlintLocation> enabledLocations,
            Map<GlintItemCategory, CategoryGlintConfig> categoryConfigs
    ) {
        this.mode = mode != null ? mode : GlintMode.VANILLA;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.speed = speed;
        this.opacity = opacity;
        this.shinyPotsPriority = shinyPotsPriority != null ? shinyPotsPriority : ShinyPotsPriority.PRESERVE_SHINY_POTS;
        if (enabledLocations != null) {
            Set<GlintLocation> set = EnumSet.noneOf(GlintLocation.class);
            set.addAll(enabledLocations);
            this.enabledLocations = Collections.unmodifiableSet(set);
        } else {
            this.enabledLocations = Collections.emptySet();
        }

        Map<GlintItemCategory, CategoryGlintConfig> catMap = new EnumMap<>(GlintItemCategory.class);
        if (categoryConfigs != null) {
            catMap.putAll(categoryConfigs);
        }
        this.categoryConfigs = Collections.unmodifiableMap(catMap);
    }

    public static GlintSettings vanilla() {
        return new GlintSettings(
                GlintMode.VANILLA,
                0xFF8040CC,
                0xFF8040CC,
                1.0f,
                1.0f,
                ShinyPotsPriority.PRESERVE_SHINY_POTS,
                EnumSet.allOf(GlintLocation.class),
                Collections.emptyMap()
        );
    }

    public GlintMode getMode() {
        return mode;
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public int getSecondaryColor() {
        return secondaryColor;
    }

    public float getSpeed() {
        return speed;
    }

    public float getOpacity() {
        return opacity;
    }

    public ShinyPotsPriority getShinyPotsPriority() {
        return shinyPotsPriority;
    }

    public Set<GlintLocation> getEnabledLocations() {
        return enabledLocations;
    }

    public boolean isLocationEnabled(GlintLocation location) {
        return location == null || enabledLocations.contains(location);
    }

    public Map<GlintItemCategory, CategoryGlintConfig> getCategoryConfigs() {
        return categoryConfigs;
    }

    public CategoryGlintConfig getCategoryConfig(GlintItemCategory category) {
        return categoryConfigs.get(category);
    }
}
