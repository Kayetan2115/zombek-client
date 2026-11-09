package pl.zombek.client.core.rendereffect.glint;

/**
 * Configuration per item category for enchantment glint.
 */
public class CategoryGlintConfig {
    private final boolean enabled;
    private final GlintMode mode;
    private final int primaryColor;
    private final int secondaryColor;

    public CategoryGlintConfig(boolean enabled, GlintMode mode, int primaryColor, int secondaryColor) {
        this.enabled = enabled;
        this.mode = mode != null ? mode : GlintMode.STATIC;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }

    public static CategoryGlintConfig defaultForCategory(GlintItemCategory category) {
        int color = switch (category) {
            case WEAPONS -> 0xFFFF2222; // Red
            case TOOLS -> 0xFF22FF22;   // Green
            case ARMOR -> 0xFF2288FF;   // Blue
            case BOOKS -> 0xFFFFDD00;   // Gold/Yellow
            case POTIONS -> 0xFFFF00FF; // Magenta
            case ELYTRA -> 0xFF00FFFF;  // Cyan
            case SHIELDS -> 0xFFFF8800; // Orange
            case OTHER -> 0xFF800080;   // Vanilla Purple
        };
        return new CategoryGlintConfig(false, GlintMode.STATIC, color, color);
    }

    public boolean isEnabled() {
        return enabled;
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
}
