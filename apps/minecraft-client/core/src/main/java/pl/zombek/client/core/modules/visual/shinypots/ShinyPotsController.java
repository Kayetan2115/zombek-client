package pl.zombek.client.core.modules.visual.shinypots;

/**
 * Controller deciding whether an item should render with the enchantment glint effect,
 * resolving glint speed, strength, and colors.
 */
public class ShinyPotsController {
    private static final ShinyPotsController INSTANCE = new ShinyPotsController();

    private ShinyPotsController() {}

    public static ShinyPotsController getInstance() {
        return INSTANCE;
    }

    /**
     * Determines whether the given item qualifies for Shiny Pots enchantment glint.
     *
     * @param registryId item identifier (e.g. "minecraft:potion", "minecraft:splash_potion")
     * @param context rendering location
     * @return true if the item should display glint
     */
    public boolean shouldHaveGlint(String registryId, ShinyPotsContext context) {
        ShinyPotsModule module = ShinyPotsModule.getInstance();
        if (module == null || !module.isEnabled()) {
            return false;
        }

        if (registryId == null) {
            return false;
        }

        // Check context visibility
        if (!isContextAllowed(module, context)) {
            return false;
        }

        PotionType type = PotionType.fromRegistryId(registryId);
        return switch (type) {
            case REGULAR -> module.getRegularPotionsSetting().getValue();
            case SPLASH -> module.getSplashPotionsSetting().getValue();
            case LINGERING -> module.getLingeringPotionsSetting().getValue();
            case TIPPED_ARROW -> module.getTippedArrowsSetting().getValue();
            case OTHER -> false;
        };
    }

    public boolean shouldHaveGlint(String registryId) {
        return shouldHaveGlint(registryId, ShinyPotsContext.UNKNOWN);
    }

    public boolean isContextAllowed(ShinyPotsModule module, ShinyPotsContext context) {
        return switch (context) {
            case INVENTORY -> module.getShowInInventorySetting().getValue();
            case HOTBAR -> module.getShowInHotbarSetting().getValue();
            case CONTAINER -> module.getShowInContainersSetting().getValue();
            case HELD_ITEM -> module.getShowInHandSetting().getValue();
            case DROPPED_ITEM -> module.getShowDroppedSetting().getValue();
            case UNKNOWN -> true;
        };
    }

    /**
     * Resolves effective glint color with time-based pulsating alpha.
     */
    public int resolveGlintColor(long currentTimeMs) {
        ShinyPotsModule module = ShinyPotsModule.getInstance();
        if (module == null) return 0xFF800080;

        int baseColor = module.getCustomGlintColorSetting().getValue() & 0x00FFFFFF;
        float speed = module.getGlintSpeedSetting().getValue().floatValue();
        float opacitySetting = module.getGlintOpacitySetting().getValue().floatValue() / 100.0f;

        // Sine pulsation
        double pulse = 0.8 + 0.2 * Math.sin((currentTimeMs / 200.0) * speed);
        int alpha = (int) Math.min(255, Math.max(20, pulse * opacitySetting * 255.0));

        return (alpha << 24) | baseColor;
    }
}
