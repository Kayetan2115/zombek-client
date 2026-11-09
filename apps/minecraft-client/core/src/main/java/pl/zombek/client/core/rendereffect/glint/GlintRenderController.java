package pl.zombek.client.core.rendereffect.glint;

import pl.zombek.client.core.modules.visual.shinypots.ShinyPotsContext;
import pl.zombek.client.core.modules.visual.shinypots.ShinyPotsController;

/**
 * Central controller resolving dynamic enchantment glint colors, animation states,
 * and seamless integration with Shiny Pots.
 */
public class GlintRenderController {
    private static final GlintRenderController INSTANCE = new GlintRenderController();

    private boolean active = false;
    private GlintSettings settings = GlintSettings.vanilla();
    private IGlintRenderAdapter adapter;

    private GlintRenderController() {
    }

    public static GlintRenderController getInstance() {
        return INSTANCE;
    }

    public void setAdapter(IGlintRenderAdapter adapter) {
        this.adapter = adapter;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        if (adapter != null) {
            adapter.onGlintSettingsUpdated();
        }
    }

    public void updateSettings(GlintSettings newSettings) {
        this.settings = newSettings != null ? newSettings : GlintSettings.vanilla();
        if (adapter != null) {
            adapter.onGlintSettingsUpdated();
        }
    }

    /**
     * Resolves the effective glint color for an item render pass.
     *
     * @param registryId Item identifier (e.g. "minecraft:diamond_sword", "minecraft:potion")
     * @param hasVanillaGlint Whether the item naturally has enchantment glint
     * @param location Render location context
     * @param timeMs Current frame timestamp in ms
     * @return 32-bit ARGB glint color, or 0 if no glint should be rendered
     */
    public int resolveGlintColor(
            String registryId,
            boolean hasVanillaGlint,
            GlintLocation location,
            long timeMs
    ) {
        // Check location support
        if (!settings.isLocationEnabled(location)) {
            return hasVanillaGlint ? GlintAnimationController.VANILLA_GLINT_ARGB : 0;
        }

        // Check Shiny Pots qualification
        ShinyPotsContext potsContext = mapLocationToPotsContext(location);
        boolean isShinyPot = ShinyPotsController.getInstance().shouldHaveGlint(registryId, potsContext);

        if (!active) {
            if (isShinyPot) {
                return ShinyPotsController.getInstance().resolveGlintColor(timeMs);
            }
            return hasVanillaGlint ? GlintAnimationController.VANILLA_GLINT_ARGB : 0;
        }

        // If neither natural glint nor shiny pot, don't render glint
        if (!hasVanillaGlint && !isShinyPot) {
            return 0;
        }

        // 1. Shiny Pots priority check
        ShinyPotsPriority priority = settings.getShinyPotsPriority();
        if (isShinyPot) {
            if (priority == ShinyPotsPriority.PRESERVE_SHINY_POTS) {
                return ShinyPotsController.getInstance().resolveGlintColor(timeMs);
            }
        }

        // 2. Check Category Override
        GlintItemCategory category = GlintItemCategory.fromRegistryId(registryId);
        CategoryGlintConfig catConfig = settings.getCategoryConfig(category);

        if (catConfig != null && catConfig.isEnabled()) {
            return GlintAnimationController.calculateColor(
                    catConfig.getMode(),
                    catConfig.getPrimaryColor(),
                    catConfig.getSecondaryColor(),
                    settings.getSpeed(),
                    settings.getOpacity(),
                    timeMs
            );
        }

        // If potion and priority is USE_POTION_SPECIFIC but no custom override, fall back to global
        if (isShinyPot && priority == ShinyPotsPriority.USE_POTION_SPECIFIC && catConfig != null && catConfig.isEnabled()) {
            return GlintAnimationController.calculateColor(
                    catConfig.getMode(),
                    catConfig.getPrimaryColor(),
                    catConfig.getSecondaryColor(),
                    settings.getSpeed(),
                    settings.getOpacity(),
                    timeMs
            );
        }

        // 3. Fallback to Global Glint Settings
        return GlintAnimationController.calculateColor(
                settings.getMode(),
                settings.getPrimaryColor(),
                settings.getSecondaryColor(),
                settings.getSpeed(),
                settings.getOpacity(),
                timeMs
        );
    }

    private ShinyPotsContext mapLocationToPotsContext(GlintLocation location) {
        if (location == null) return ShinyPotsContext.UNKNOWN;
        return switch (location) {
            case INVENTORY -> ShinyPotsContext.INVENTORY;
            case HOTBAR -> ShinyPotsContext.HOTBAR;
            case HELD_ITEMS -> ShinyPotsContext.HELD_ITEM;
            case DROPPED_ITEMS -> ShinyPotsContext.DROPPED_ITEM;
            case CONTAINERS -> ShinyPotsContext.CONTAINER;
            case ARMOR_RENDERING -> ShinyPotsContext.UNKNOWN;
        };
    }

    public void reset() {
        this.active = false;
        this.settings = GlintSettings.vanilla();
        if (adapter != null) {
            adapter.onGlintSettingsUpdated();
        }
    }

    public GlintSettings getSettings() {
        return settings;
    }
}
