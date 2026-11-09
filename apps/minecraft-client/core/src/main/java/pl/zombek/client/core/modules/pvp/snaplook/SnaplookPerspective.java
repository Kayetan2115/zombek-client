package pl.zombek.client.core.modules.pvp.snaplook;

/**
 * Cross-loader representation of camera perspective.
 */
public enum SnaplookPerspective {
    FIRST_PERSON("Pierwsza Osoba (First Person)"),
    THIRD_PERSON_BACK("Trzecia Osoba Tył (Third Person Back)"),
    THIRD_PERSON_FRONT("Trzecia Osoba Przód (Third Person Front)");

    private final String displayName;

    SnaplookPerspective(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
