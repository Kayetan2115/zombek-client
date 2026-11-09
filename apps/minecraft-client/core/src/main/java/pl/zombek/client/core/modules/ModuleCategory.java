package pl.zombek.client.core.modules;

/**
 * Categorization for ZombekClient modules across all loaders.
 */
public enum ModuleCategory {
    PVP("PVP", "PvP / Combat", "Moduły wspomagające walkę i PvP", "⚔"),
    HUD("HUD", "HUD / Information", "Elementy interfejsu ekranowego i informacje", "📊"),
    VISUAL("VISUAL", "Visual", "Modyfikacje wizualne, oświetlenie i efekty graficzne", "👁"),
    SURVIVAL("SURVIVAL", "Survival / World", "Usprawnienia rozgrywki przetrwania i interakcji ze światem", "🌲"),
    OTHER("OTHER", "Social / Server", "Moduły społecznościowe, integracje serwerowe i narzędzia", "🌐");

    private final String id;
    private final String displayName;
    private final String description;
    private final String iconSymbol;

    ModuleCategory(String id, String displayName, String description, String iconSymbol) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.iconSymbol = iconSymbol;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getIconSymbol() {
        return iconSymbol;
    }

    public static ModuleCategory fromId(String id) {
        if (id == null) return OTHER;
        for (ModuleCategory cat : values()) {
            if (cat.id.equalsIgnoreCase(id) || cat.name().equalsIgnoreCase(id)) {
                return cat;
            }
        }
        return OTHER;
    }
}
