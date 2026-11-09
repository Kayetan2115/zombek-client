package pl.zombek.client.core.chat;

import java.util.List;

/**
 * Orchestrates incoming chat message processing: classification, filtering,
 * repeated message deduplication (e.g. Hello! ×3), timestamps, notifications, and tab dispatch.
 */
public class ChatMessageManager {
    private static ChatMessageManager instance;

    private boolean enableRepeatedMessageGrouping = true;
    private long repeatedMessageWindowMs = 15000L; // 15 seconds

    private boolean enableSenderGrouping = false;
    private long senderGroupWindowMs = 5000L; // 5 seconds

    private TimestampFormat timestampFormat = TimestampFormat.HH_MM;
    private String customTimestampFormat = "HH:mm";
    private boolean showTimestamps = true;

    public ChatMessageManager() {}

    public static synchronized ChatMessageManager getInstance() {
        if (instance == null) {
            instance = new ChatMessageManager();
        }
        return instance;
    }

    /**
     * Processes incoming message through the entire ZombekClient chat pipeline.
     */
    public synchronized ChatMessageEntry processIncoming(String rawText, String senderName, String senderUuid,
                                                         boolean system, Object nativeComponent, String clientPlayerName) {
        if (rawText == null || rawText.isEmpty()) {
            return null;
        }

        ChatFilterManager filterMgr = ChatFilterManager.getInstance();
        ChatCategory category = filterMgr.classifyMessage(ChatMessageEntry.stripFormatting(rawText).trim(), system);

        ChatMessageEntry entry = new ChatMessageEntry(rawText, senderName, senderUuid, category, system, nativeComponent);

        // 1. Run Filters (may mark hidden, highlighted, change color, move to tab)
        filterMgr.evaluateMessage(entry);

        // 2. Run Notifications (mentions, keywords, direct messages)
        ChatNotificationController.getInstance().evaluate(entry, clientPlayerName);

        // 3. Repeated Message Deduplication (e.g. "Hello! ×3")
        if (enableRepeatedMessageGrouping && !entry.isHidden()) {
            ChatHistoryManager history = ChatHistoryManager.getInstance();
            List<ChatMessageEntry> existing = history.getMessages();
            if (!existing.isEmpty()) {
                ChatMessageEntry last = existing.get(existing.size() - 1);
                long diff = entry.getTimestamp() - last.getTimestamp();
                if (diff <= repeatedMessageWindowMs && last.getCleanText().equals(entry.getCleanText())
                        && ObjectsEquals(last.getSenderName(), entry.getSenderName())) {
                    last.incrementRepeat();
                    return last;
                }
            }
        }

        // 4. Dispatch to tabs
        ChatTabManager.getInstance().dispatchToTabs(entry);

        // 5. Store in history buffer
        ChatHistoryManager.getInstance().addMessage(entry);

        return entry;
    }

    private boolean ObjectsEquals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public String formatTimestamp(long epochMillis) {
        if (!showTimestamps || timestampFormat == TimestampFormat.NONE) {
            return "";
        }
        return timestampFormat.formatTime(epochMillis, customTimestampFormat);
    }

    public boolean isEnableRepeatedMessageGrouping() { return enableRepeatedMessageGrouping; }
    public void setEnableRepeatedMessageGrouping(boolean enableRepeatedMessageGrouping) {
        this.enableRepeatedMessageGrouping = enableRepeatedMessageGrouping;
    }
    public long getRepeatedMessageWindowMs() { return repeatedMessageWindowMs; }
    public void setRepeatedMessageWindowMs(long repeatedMessageWindowMs) {
        this.repeatedMessageWindowMs = repeatedMessageWindowMs;
    }
    public boolean isEnableSenderGrouping() { return enableSenderGrouping; }
    public void setEnableSenderGrouping(boolean enableSenderGrouping) {
        this.enableSenderGrouping = enableSenderGrouping;
    }
    public long getSenderGroupWindowMs() { return senderGroupWindowMs; }
    public void setSenderGroupWindowMs(long senderGroupWindowMs) {
        this.senderGroupWindowMs = senderGroupWindowMs;
    }
    public TimestampFormat getTimestampFormat() { return timestampFormat; }
    public void setTimestampFormat(TimestampFormat timestampFormat) {
        this.timestampFormat = timestampFormat != null ? timestampFormat : TimestampFormat.HH_MM;
    }
    public String getCustomTimestampFormat() { return customTimestampFormat; }
    public void setCustomTimestampFormat(String customTimestampFormat) {
        this.customTimestampFormat = customTimestampFormat;
    }
    public boolean isShowTimestamps() { return showTimestamps; }
    public void setShowTimestamps(boolean showTimestamps) {
        this.showTimestamps = showTimestamps;
    }

    public static synchronized void resetForTesting() {
        instance = null;
    }
}
