package pl.zombek.client.core.data;

/**
 * Universal classification of entities for client visuals (Hit Color, Hitbox, etc.).
 */
public enum TargetEntityType {
    PLAYER("PLAYER", "Gracz"),
    HOSTILE("HOSTILE", "Wrogi mob"),
    PASSIVE("PASSIVE", "Pasywny mob"),
    NEUTRAL("NEUTRAL", "Neutralny mob"),
    PROJECTILE("PROJECTILE", "Pocisk"),
    VEHICLE("VEHICLE", "Pojazd"),
    OTHER("OTHER", "Inna encja");

    private final String id;
    private final String displayName;

    TargetEntityType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }
}
