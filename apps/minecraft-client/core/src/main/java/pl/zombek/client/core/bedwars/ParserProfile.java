package pl.zombek.client.core.bedwars;

/**
 * Available parser profiles for parsing Bedwars scoreboard and chat formats.
 */
public enum ParserProfile {
    HYPIXEL_ENGLISH("Hypixel English"),
    GENERIC_BEDWARS("Generic Bedwars"),
    CUSTOM("Własny profil");

    private final String displayName;

    ParserProfile(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
