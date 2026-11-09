package pl.zombek.client.core.info;

/**
 * Safely inspects and caches JVM runtime memory metrics.
 * Explicitly never invokes System.gc() to prevent game stutters.
 */
public class MemoryUsageTracker {
    public enum MemoryStatus {
        NORMAL,
        WARNING,
        CRITICAL
    }

    private long usedBytes;
    private long allocatedBytes;
    private long maxBytes;
    private double usagePercent;
    private long lastUpdateTime = 0L;
    private static final long UPDATE_THROTTLE_MS = 250L; // Update 4 times a second max

    public MemoryUsageTracker() {
        updateNow();
    }

    /**
     * Polls Runtime memory metrics with internal throttling to avoid overhead.
     */
    public synchronized void update() {
        long now = System.currentTimeMillis();
        if (now - lastUpdateTime >= UPDATE_THROTTLE_MS) {
            updateNow();
            lastUpdateTime = now;
        }
    }

    /**
     * Forces immediate update.
     */
    public synchronized void updateNow() {
        Runtime rt = Runtime.getRuntime();
        long total = rt.totalMemory();
        long free = rt.freeMemory();
        this.maxBytes = rt.maxMemory();
        this.allocatedBytes = total;
        this.usedBytes = Math.max(0L, total - free);
        this.usagePercent = maxBytes > 0 ? ((double) usedBytes * 100.0) / (double) maxBytes : 0.0;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public synchronized long getUsedBytes() {
        return usedBytes;
    }

    public synchronized long getAllocatedBytes() {
        return allocatedBytes;
    }

    public synchronized long getMaxBytes() {
        return maxBytes;
    }

    public synchronized double getUsagePercent() {
        return usagePercent;
    }

    public synchronized MemoryStatus getStatus(double warningPercent, double criticalPercent) {
        if (usagePercent >= criticalPercent) return MemoryStatus.CRITICAL;
        if (usagePercent >= warningPercent) return MemoryStatus.WARNING;
        return MemoryStatus.NORMAL;
    }

    /**
     * Formats used / max memory summary, e.g. "RAM: 3240 / 8192 MB (40%)".
     */
    public synchronized String formatSummary(MemoryUnit unit, boolean showPercent) {
        String usedStr = unit.format(usedBytes);
        String maxStr = unit.format(maxBytes);
        if (showPercent) {
            return String.format(java.util.Locale.US, "RAM: %s / %s (%.0f%%)", usedStr, maxStr, usagePercent);
        } else {
            return String.format(java.util.Locale.US, "RAM: %s / %s", usedStr, maxStr);
        }
    }
}
