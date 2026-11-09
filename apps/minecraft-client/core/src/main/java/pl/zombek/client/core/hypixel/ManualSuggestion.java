package pl.zombek.client.core.hypixel;

/**
 * Representation of a manual social message suggestion for the player.
 */
public class ManualSuggestion {
    private final String id;
    private final String title;
    private final String suggestedText;
    private final long createdAt;
    private final float durationSeconds;

    public ManualSuggestion(String id, String title, String suggestedText, float durationSeconds) {
        this.id = id;
        this.title = title;
        this.suggestedText = suggestedText;
        this.createdAt = System.currentTimeMillis();
        this.durationSeconds = durationSeconds;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSuggestedText() { return suggestedText; }
    public long getCreatedAt() { return createdAt; }
    public float getDurationSeconds() { return durationSeconds; }

    public boolean isExpired() {
        return (System.currentTimeMillis() - createdAt) > (long) (durationSeconds * 1000.0f);
    }
}
