package pl.zombek.client.core.movement;

/**
 * Activation modes for Toggle Sneak.
 */
public enum SneakActivationMode {
    TOGGLE("Toggle"),
    HOLD("Hold"),
    TOGGLE_AND_HOLD("Toggle + Physical Hold");

    private final String displayName;

    SneakActivationMode(String displayName) {
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
