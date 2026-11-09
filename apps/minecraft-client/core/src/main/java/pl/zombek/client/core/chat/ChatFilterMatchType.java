package pl.zombek.client.core.chat;

/**
 * Matching algorithms for chat message filter rules.
 */
public enum ChatFilterMatchType {
    EXACT("Dokładne dopasowanie"),
    CONTAINS("Zawiera tekst"),
    STARTS_WITH("Zaczyna się od"),
    ENDS_WITH("Kończy się na"),
    REGEX("Bezpieczne wyrażenie regularne (RegEx)");

    private final String displayName;

    ChatFilterMatchType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
