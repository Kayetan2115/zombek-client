package pl.zombek.client.core.modules.pvp.snaplook;

/**
 * Activation mode for Snaplook camera perspective switching.
 */
public enum SnaplookActivationMode {
    HOLD("Hold Mode (Przytrzymanie)"),
    TOGGLE("Toggle Mode (Przełącznik)"),
    TAP("Tap Mode (Szybkie Stuknięcie)");

    private final String displayName;

    SnaplookActivationMode(String displayName) {
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
