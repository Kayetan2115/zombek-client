package pl.zombek.client.core.camera;

/**
 * Operating mode for the Hurt Cam damage tilt/shake customization.
 */
public enum HurtCamMode {
    VANILLA("Domyślny Minecraft (Vanilla)"),
    DISABLED("Wyłączony (Brak Przechylenia)"),
    REDUCED("Zredukowany (Subtelny 50%)"),
    CUSTOM("Własny (Niestandardowy)");

    private final String displayName;

    HurtCamMode(String displayName) {
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
