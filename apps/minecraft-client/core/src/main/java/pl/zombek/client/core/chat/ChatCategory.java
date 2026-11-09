package pl.zombek.client.core.chat;

/**
 * Universal classification categories for Minecraft chat messages.
 */
public enum ChatCategory {
    PLAYER_MESSAGES("Gracze", "Standardowe wiadomości graczy na czacie"),
    SYSTEM_MESSAGES("System", "Komunikaty systemowe gry i serwera"),
    PRIVATE_MESSAGES("Prywatne", "Prywatne wiadomości (/msg, /tell, /r)"),
    TEAM_MESSAGES("Drużyna", "Czat drużynowy (Team >)"),
    PARTY_MESSAGES("Party", "Czat party (Party >)"),
    GUILD_MESSAGES("Gildia", "Czat gildii (Guild >)"),
    JOIN_MESSAGES("Dołączenia", "Komunikaty o dołączeniu graczy do gry"),
    LEAVE_MESSAGES("Opuszczenia", "Komunikaty o wyjściu graczy z gry"),
    DEATH_MESSAGES("Śmierci", "Wiadomości o śmierci graczy"),
    ADVANCEMENT_MESSAGES("Osiągnięcia", "Powiadomienia o zdobytych osiągnięciach"),
    COMMAND_FEEDBACK("Komendy", "Odpowiedzi na wykonane komendy"),
    OTHER_MESSAGES("Inne", "Pozostałe niesklasyfikowane wiadomości");

    private final String displayName;
    private final String description;

    ChatCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
