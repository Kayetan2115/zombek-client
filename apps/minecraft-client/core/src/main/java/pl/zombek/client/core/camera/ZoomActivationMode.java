package pl.zombek.client.core.camera;

/**
 * Activation modes for the Zoom module.
 */
public enum ZoomActivationMode {
    HOLD("Przytrzymanie (Hold)"),
    TOGGLE("Przełącznik (Toggle)"),
    TOGGLE_HOLD("Przełącznik + Przytrzymanie (Toggle + Hold)");

    private final String displayName;

    ZoomActivationMode(String displayName) {
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
