package pl.zombek.client.core.hypixel;

/**
 * Categorization of client modifications by risk level and Hypixel policy compliance.
 */
public enum SafetyCategory {
    COSMETIC("Kosmetyczna", true, "Zmiany czysto wizualne, nieingerujące w zachowanie gry"),
    LOCAL_UI("Lokalne UI", true, "Lokalne elementy interfejsu i ukrywanie zbędnych elementów"),
    LOCAL_NOTIFICATION("Lokalne powiadomienie", true, "Powiadomienia ekranowe bazujące na widocznych danych"),
    LOCAL_AUTOCOMPLETE("Lokalne autocomplete", true, "Lokalne podpowiedzi tekstu bez wysyłania komend"),
    GAMEPLAY_AUTOMATION("Automatyzacja rozgrywki", false, "Automatyczne wykonywanie akcji przez klienta — ZABLOKOWANE"),
    NETWORK_MODIFICATION("Modyfikacja sieciowa", false, "Modyfikowanie pakietów lub zapytań API — ZABLOKOWANE"),
    UNKNOWN("Nieznana", false, "Funkcja o niesprecyzowanym statusie bezpieczeństwa — ZABLOKOWANE");

    private final String displayName;
    private final boolean allowedOnHypixel;
    private final String description;

    SafetyCategory(String displayName, boolean allowedOnHypixel, String description) {
        this.displayName = displayName;
        this.allowedOnHypixel = allowedOnHypixel;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAllowedOnHypixel() {
        return allowedOnHypixel;
    }

    public String getDescription() {
        return description;
    }
}
