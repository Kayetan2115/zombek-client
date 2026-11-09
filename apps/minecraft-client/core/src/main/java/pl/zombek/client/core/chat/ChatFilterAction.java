package pl.zombek.client.core.chat;

/**
 * Action executed when a chat message matches a filter rule.
 */
public enum ChatFilterAction {
    HIDE_LOCALLY("Ukryj lokalnie"),
    HIGHLIGHT("Wyróżnij wiadomość"),
    CHANGE_COLOR("Zmień kolor wyświetlania"),
    MOVE_TO_TAB("Przenieś do dedykowanej zakładki"),
    MARK_IMPORTANT("Oznacz jako ważną");

    private final String displayName;

    ChatFilterAction(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
