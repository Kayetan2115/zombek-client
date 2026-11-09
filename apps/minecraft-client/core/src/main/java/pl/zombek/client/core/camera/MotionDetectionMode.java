package pl.zombek.client.core.camera;

/**
 * Detection sources for calculating camera and scene motion.
 */
public enum MotionDetectionMode {
    CAMERA_ROTATION("Rotacja Kamery (Yaw/Pitch)"),
    MOUSE_DELTA("Delta Myszy (Raw Mouse Input)"),
    COMPOSITE("Hybrydowy (Rotacja + Pozycja)");

    private final String displayName;

    MotionDetectionMode(String displayName) {
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
