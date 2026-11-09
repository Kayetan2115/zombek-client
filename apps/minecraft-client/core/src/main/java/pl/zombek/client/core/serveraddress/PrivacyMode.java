package pl.zombek.client.core.serveraddress;

/**
 * Privacy protection modes for displaying server address information.
 */
public enum PrivacyMode {
    NORMAL("Normal"),
    HIDE_ADDRESS("Hide Address"),
    MASK_HOST("Mask Host"),
    HIDE_PORT("Hide Port"),
    SHOW_ALIAS_ONLY("Show Alias Only"),
    STREAMER_MODE("Streamer Mode");

    private final String displayName;

    PrivacyMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
