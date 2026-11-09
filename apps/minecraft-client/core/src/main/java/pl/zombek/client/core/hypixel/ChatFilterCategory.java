package pl.zombek.client.core.hypixel;

/**
 * Filterable categories of Minecraft chat messages on Hypixel Network.
 */
public enum ChatFilterCategory {
    PRIVATE_MESSAGES("Prywatne wiadomości (/msg, /r)"),
    TEAM_CHAT("Czat drużynowy (Team >)"),
    PARTY_CHAT("Czat party (Party >)"),
    GUILD_CHAT("Czat gildii (Guild >)"),
    OFFICER_CHAT("Czat oficerów (Officer >)"),
    SHOUT_MESSAGES("Krzyki ([SHOUT])"),
    SPECTATOR_CHAT("Czat obserwatorów ([SPECTATOR])"),
    LOBBY_STATUS("Wiadomości statusu lobby"),
    JOIN_MESSAGES("Dołączenia graczy do gry"),
    LEAVE_MESSAGES("Opuszczenia gry przez graczy"),
    SOUL_WELL("Ogłoszenia Soul Well"),
    MYSTERY_BOX("Ogłoszenia Mystery Box"),
    CUSTOM("Własna reguła użytkownika");

    private final String displayName;

    ChatFilterCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
