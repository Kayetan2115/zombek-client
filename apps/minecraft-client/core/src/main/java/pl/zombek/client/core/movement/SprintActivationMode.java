package pl.zombek.client.core.movement;

/**
 * Activation modes for Toggle Sprint.
 */
public enum SprintActivationMode {
    TOGGLE("Toggle"),
    HOLD("Hold");

    private final String displayName;

    SprintActivationMode(String displayName) {
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
