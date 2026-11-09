package pl.zombek.client.core.data;

/**
 * Snapshot of an active item cooldown in Minecraft.
 */
public class CooldownItemData {

    private final String itemId;
    private final String displayName;
    private final float progress; // 0.0f (finished) to 1.0f (fresh/full)
    private final float remainingSeconds;
    private final Object itemHandle;

    public CooldownItemData(String itemId, String displayName, float progress, float remainingSeconds, Object itemHandle) {
        this.itemId = itemId != null ? itemId : "";
        this.displayName = displayName != null ? displayName : "";
        this.progress = Math.max(0.0f, Math.min(1.0f, progress));
        this.remainingSeconds = Math.max(0.0f, remainingSeconds);
        this.itemHandle = itemHandle;
    }

    public String getItemId() {
        return itemId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getProgress() {
        return progress;
    }

    public float getProgressPercentage() {
        return progress * 100.0f;
    }

    public float getRemainingSeconds() {
        return remainingSeconds;
    }

    public Object getItemHandle() {
        return itemHandle;
    }
}
