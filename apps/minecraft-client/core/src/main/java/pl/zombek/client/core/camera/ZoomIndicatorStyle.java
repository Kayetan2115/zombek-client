package pl.zombek.client.core.camera;

/**
 * Visual presentation style for the on-screen Zoom HUD indicator.
 */
public enum ZoomIndicatorStyle {
    BADGE("Zaokrąglona Plakietka (Badge)"),
    COMPACT("Kompaktowy (Compact)"),
    TEXT_ONLY("Tylko Tekst (Text Only)");

    private final String displayName;

    ZoomIndicatorStyle(String displayName) {
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
