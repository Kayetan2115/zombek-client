package pl.zombek.client.core.tracking;

/**
 * Types of projectiles tracked in PvP Info.
 */
public enum ProjectileType {
    ARROW("Arrow"),
    SPECTRAL_ARROW("Spectral Arrow"),
    TRIDENT("Trident"),
    SNOWBALL("Snowball"),
    EGG("Egg"),
    FIREBALL("Fireball"),
    POTION("Potion"),
    FISHING_BOBBER("Fishing Bobber"),
    OTHER("Projectile");

    private final String displayName;

    ProjectileType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
