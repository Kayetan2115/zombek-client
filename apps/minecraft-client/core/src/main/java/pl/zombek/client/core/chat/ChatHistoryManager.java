package pl.zombek.client.core.chat;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Local chat history manager with in-memory ring-buffer, multi-criteria search,
 * privacy sanitization, and streamer mode protection.
 */
public class ChatHistoryManager {
    private static ChatHistoryManager instance;

    // Credential sanitization pattern: /login, /register, /l, /changepassword
    private static final Pattern CREDENTIAL_COMMAND_PATTERN = Pattern.compile(
            "(?i)^/(?:login|register|l|reg|changepassword|auth)\\s+.*"
    );

    private final List<ChatMessageEntry> messages = new ArrayList<>();
    private int maxMessages = 1000;

    // Privacy toggles
    private boolean privacyMode = false;
    private boolean streamerMode = false;
    private boolean excludePrivateMessages = false;
    private boolean clearOnDisconnect = true;

    public ChatHistoryManager() {}

    public static synchronized ChatHistoryManager getInstance() {
        if (instance == null) {
            instance = new ChatHistoryManager();
        }
        return instance;
    }

    /**
     * Appends message to history with credential sanitization and capacity capping.
     */
    public synchronized void addMessage(ChatMessageEntry entry) {
        if (entry == null) return;

        // Security: Never save authentication passwords
        if (CREDENTIAL_COMMAND_PATTERN.matcher(entry.getRawText()).matches()) {
            return;
        }

        // Privacy: Exclude private messages if requested
        if ((privacyMode || excludePrivateMessages) && entry.getCategory() == ChatCategory.PRIVATE_MESSAGES) {
            return;
        }

        messages.add(entry);

        // Ring-buffer enforcement
        while (messages.size() > maxMessages) {
            messages.remove(0);
        }
    }

    public synchronized List<ChatMessageEntry> getMessages() {
        return Collections.unmodifiableList(new ArrayList<>(messages));
    }

    public synchronized List<ChatMessageEntry> getMessagesForTab(ChatTab tab, boolean showHidden) {
        if (tab == null) return Collections.emptyList();
        List<ChatMessageEntry> result = new ArrayList<>();
        for (ChatMessageEntry msg : messages) {
            if (!showHidden && msg.isHidden()) {
                continue;
            }
            if (tab.matches(msg)) {
                result.add(msg);
            }
        }
        return result;
    }

    /**
     * Searches history by text query, sender, category, and time range.
     */
    public synchronized List<ChatMessageEntry> search(String query, String sender, ChatCategory category,
                                                      long fromEpochMillis, long toEpochMillis) {
        List<ChatMessageEntry> matches = new ArrayList<>();
        String lowerQuery = query != null && !query.trim().isEmpty() ? query.trim().toLowerCase(Locale.ROOT) : null;
        String lowerSender = sender != null && !sender.trim().isEmpty() ? sender.trim().toLowerCase(Locale.ROOT) : null;

        for (ChatMessageEntry entry : messages) {
            if (fromEpochMillis > 0 && entry.getTimestamp() < fromEpochMillis) continue;
            if (toEpochMillis > 0 && entry.getTimestamp() > toEpochMillis) continue;
            if (category != null && entry.getCategory() != category) continue;

            if (lowerSender != null) {
                if (entry.getSenderName() == null || !entry.getSenderName().toLowerCase(Locale.ROOT).contains(lowerSender)) {
                    continue;
                }
            }

            if (lowerQuery != null) {
                if (!entry.getCleanText().toLowerCase(Locale.ROOT).contains(lowerQuery)) {
                    continue;
                }
            }

            matches.add(entry);
        }
        return matches;
    }

    public synchronized void clearHistory() {
        messages.clear();
    }

    public synchronized void onDisconnect() {
        if (clearOnDisconnect) {
            clearHistory();
        }
    }

    /**
     * Exports history to a readable plaintext format with timestamps.
     */
    public synchronized String exportHistoryToText(TimestampFormat format) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ZombekClient Chat History Export ===\n");
        sb.append("Exported: ").append(new Date()).append("\n");
        sb.append("Total Messages: ").append(messages.size()).append("\n\n");

        for (ChatMessageEntry entry : messages) {
            String timeStr = format != null ? format.formatTime(entry.getTimestamp(), "HH:mm:ss") : "";
            if (!timeStr.isEmpty()) {
                sb.append("[").append(timeStr).append("] ");
            }
            if (streamerMode || privacyMode) {
                sb.append("[SENDER REDACTED]: ");
            } else if (entry.getSenderName() != null) {
                sb.append("<").append(entry.getSenderName()).append("> ");
            }
            sb.append(entry.getCleanText());
            if (entry.getRepeatCount() > 1) {
                sb.append(" ×").append(entry.getRepeatCount());
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public int getMaxMessages() { return maxMessages; }
    public synchronized void setMaxMessages(int maxMessages) {
        this.maxMessages = Math.max(50, maxMessages);
        while (messages.size() > this.maxMessages) {
            messages.remove(0);
        }
    }

    public boolean isPrivacyMode() { return privacyMode; }
    public void setPrivacyMode(boolean privacyMode) { this.privacyMode = privacyMode; }
    public boolean isStreamerMode() { return streamerMode; }
    public void setStreamerMode(boolean streamerMode) { this.streamerMode = streamerMode; }
    public boolean isExcludePrivateMessages() { return excludePrivateMessages; }
    public void setExcludePrivateMessages(boolean excludePrivateMessages) { this.excludePrivateMessages = excludePrivateMessages; }
    public boolean isClearOnDisconnect() { return clearOnDisconnect; }
    public void setClearOnDisconnect(boolean clearOnDisconnect) { this.clearOnDisconnect = clearOnDisconnect; }

    public static synchronized void resetForTesting() {
        instance = null;
    }
}
