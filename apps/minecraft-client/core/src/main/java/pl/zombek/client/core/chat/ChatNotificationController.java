package pl.zombek.client.core.chat;

import pl.zombek.client.core.friends.FriendEntry;
import pl.zombek.client.core.friends.NameTagFriendManager;

import java.util.*;

/**
 * Manages local notifications for mentions, direct messages, and keywords with audio cooldown.
 */
public class ChatNotificationController {
    private static ChatNotificationController instance;

    private boolean enableMentions = true;
    private boolean enableDirectMessageAlerts = true;
    private boolean enableKeywordAlerts = true;
    private boolean playSound = false;
    private int soundCooldownSeconds = 5;

    private final Set<String> customKeywords = new HashSet<>();
    private long lastSoundTimestamp = 0;
    private int totalUnreadNotifications = 0;

    public ChatNotificationController() {}

    public static synchronized ChatNotificationController getInstance() {
        if (instance == null) {
            instance = new ChatNotificationController();
        }
        return instance;
    }

    /**
     * Evaluates incoming message for mentions, keywords, and direct message alerts.
     * Returns true if notification should trigger.
     */
    public synchronized boolean evaluate(ChatMessageEntry entry, String clientPlayerName) {
        if (entry == null || entry.isHidden()) {
            return false;
        }

        boolean alert = false;
        String clean = entry.getCleanText().toLowerCase(Locale.ROOT);

        // 1. Direct message alert
        if (enableDirectMessageAlerts && entry.getCategory() == ChatCategory.PRIVATE_MESSAGES) {
            alert = true;
        }

        // 2. Mention by client player's name
        if (enableMentions && clientPlayerName != null && !clientPlayerName.trim().isEmpty()) {
            String lowerName = clientPlayerName.trim().toLowerCase(Locale.ROOT);
            if (clean.contains(lowerName)) {
                alert = true;
                entry.setHighlighted(true);
            }
        }

        // 3. Mention by local friend aliases
        if (enableMentions) {
            for (FriendEntry friend : NameTagFriendManager.getInstance().getAllFriends()) {
                String alias = friend.getAlias();
                if (alias != null && !alias.isEmpty() && clean.contains(alias.toLowerCase(Locale.ROOT))) {
                    alert = true;
                    entry.setHighlighted(true);
                    break;
                }
            }
        }

        // 4. Custom keywords
        if (enableKeywordAlerts && !customKeywords.isEmpty()) {
            for (String kw : customKeywords) {
                if (kw != null && !kw.isEmpty() && clean.contains(kw.toLowerCase(Locale.ROOT))) {
                    alert = true;
                    entry.setHighlighted(true);
                    break;
                }
            }
        }

        if (alert) {
            totalUnreadNotifications++;
            entry.setImportant(true);
        }

        return alert;
    }

    /**
     * Checks if notification sound can be played based on cooldown.
     */
    public synchronized boolean shouldPlaySound() {
        if (!playSound) return false;
        long now = System.currentTimeMillis();
        if (now - lastSoundTimestamp >= soundCooldownSeconds * 1000L) {
            lastSoundTimestamp = now;
            return true;
        }
        return false;
    }

    public synchronized void addKeyword(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            customKeywords.add(keyword.trim().toLowerCase(Locale.ROOT));
        }
    }

    public synchronized void removeKeyword(String keyword) {
        if (keyword != null) {
            customKeywords.remove(keyword.trim().toLowerCase(Locale.ROOT));
        }
    }

    public synchronized Set<String> getKeywords() {
        return Collections.unmodifiableSet(new HashSet<>(customKeywords));
    }

    public synchronized void clearKeywords() {
        customKeywords.clear();
    }

    public synchronized void resetUnread() {
        totalUnreadNotifications = 0;
    }

    public int getTotalUnreadNotifications() { return totalUnreadNotifications; }
    public boolean isEnableMentions() { return enableMentions; }
    public void setEnableMentions(boolean enableMentions) { this.enableMentions = enableMentions; }
    public boolean isEnableDirectMessageAlerts() { return enableDirectMessageAlerts; }
    public void setEnableDirectMessageAlerts(boolean enableDirectMessageAlerts) { this.enableDirectMessageAlerts = enableDirectMessageAlerts; }
    public boolean isEnableKeywordAlerts() { return enableKeywordAlerts; }
    public void setEnableKeywordAlerts(boolean enableKeywordAlerts) { this.enableKeywordAlerts = enableKeywordAlerts; }
    public boolean isPlaySound() { return playSound; }
    public void setPlaySound(boolean playSound) { this.playSound = playSound; }
    public int getSoundCooldownSeconds() { return soundCooldownSeconds; }
    public void setSoundCooldownSeconds(int soundCooldownSeconds) { this.soundCooldownSeconds = soundCooldownSeconds; }

    public static synchronized void resetForTesting() {
        instance = null;
    }
}
