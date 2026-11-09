package pl.zombek.client.core.info;

/**
 * Memory unit presentation options.
 */
public enum MemoryUnit {
    MB("MB"),
    GB("GB"),
    AUTO("Automatyczny (MB/GB)");

    private final String displayName;

    MemoryUnit(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Formats bytes to human readable memory string according to this unit.
     */
    public String format(long bytes) {
        double mb = bytes / (1024.0 * 1024.0);
        switch (this) {
            case GB:
                return String.format(java.util.Locale.US, "%.2f GB", mb / 1024.0);
            case AUTO:
                if (mb >= 1024.0) {
                    return String.format(java.util.Locale.US, "%.2f GB", mb / 1024.0);
                } else {
                    return String.format(java.util.Locale.US, "%.0f MB", mb);
                }
            case MB:
            default:
                return String.format(java.util.Locale.US, "%.0f MB", mb);
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}
