package pl.zombek.client.core.chat;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Immutable/managed representation of an incoming chat message entry.
 * Preserves reference to original game component for formatting, hover, and click events.
 */
public class ChatMessageEntry {
    private static final Pattern FORMATTING_PATTERN = Pattern.compile("(?i)§[0-9a-fk-or]");

    private final String id;
    private final long timestamp;
    private final String rawText;
    private final String cleanText;
    private String formattedText;
    private final String senderName;
    private final String senderUuid;
    private ChatCategory category;
    private boolean hidden;
    private boolean highlighted;
    private int highlightColorArgb;
    private boolean important;
    private final boolean system;
    private int repeatCount;
    private String assignedTabId;

    // Native text component reference (e.g. net.minecraft.text.Text or Component)
    private final transient Object nativeComponent;

    public ChatMessageEntry(String rawText, String senderName, String senderUuid, ChatCategory category,
                            boolean system, Object nativeComponent) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.rawText = rawText != null ? rawText : "";
        this.cleanText = stripFormatting(this.rawText).trim();
        this.formattedText = this.rawText;
        this.senderName = senderName;
        this.senderUuid = senderUuid;
        this.category = category != null ? category : ChatCategory.OTHER_MESSAGES;
        this.hidden = false;
        this.highlighted = false;
        this.highlightColorArgb = 0xFFFFFFFF;
        this.important = false;
        this.system = system;
        this.repeatCount = 1;
        this.assignedTabId = "all";
        this.nativeComponent = nativeComponent;
    }

    public ChatMessageEntry(String id, long timestamp, String rawText, String senderName, String senderUuid,
                            ChatCategory category, boolean hidden, boolean highlighted, int highlightColorArgb,
                            boolean important, boolean system, int repeatCount, String assignedTabId,
                            Object nativeComponent) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.timestamp = timestamp;
        this.rawText = rawText != null ? rawText : "";
        this.cleanText = stripFormatting(this.rawText).trim();
        this.formattedText = this.rawText;
        this.senderName = senderName;
        this.senderUuid = senderUuid;
        this.category = category != null ? category : ChatCategory.OTHER_MESSAGES;
        this.hidden = hidden;
        this.highlighted = highlighted;
        this.highlightColorArgb = highlightColorArgb;
        this.important = important;
        this.system = system;
        this.repeatCount = repeatCount > 0 ? repeatCount : 1;
        this.assignedTabId = assignedTabId != null ? assignedTabId : "all";
        this.nativeComponent = nativeComponent;
    }

    public static String stripFormatting(String input) {
        if (input == null) return "";
        return FORMATTING_PATTERN.matcher(input).replaceAll("");
    }

    public void incrementRepeat() {
        this.repeatCount++;
    }

    public String getId() { return id; }
    public long getTimestamp() { return timestamp; }
    public String getRawText() { return rawText; }
    public String getCleanText() { return cleanText; }
    public String getFormattedText() { return formattedText; }
    public void setFormattedText(String formattedText) { this.formattedText = formattedText; }
    public String getSenderName() { return senderName; }
    public String getSenderUuid() { return senderUuid; }
    public ChatCategory getCategory() { return category; }
    public void setCategory(ChatCategory category) { this.category = category; }
    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
    public boolean isHighlighted() { return highlighted; }
    public void setHighlighted(boolean highlighted) { this.highlighted = highlighted; }
    public int getHighlightColorArgb() { return highlightColorArgb; }
    public void setHighlightColorArgb(int highlightColorArgb) { this.highlightColorArgb = highlightColorArgb; }
    public boolean isImportant() { return important; }
    public void setImportant(boolean important) { this.important = important; }
    public boolean isSystem() { return system; }
    public int getRepeatCount() { return repeatCount; }
    public void setRepeatCount(int repeatCount) { this.repeatCount = repeatCount; }
    public String getAssignedTabId() { return assignedTabId; }
    public void setAssignedTabId(String assignedTabId) { this.assignedTabId = assignedTabId; }
    public Object getNativeComponent() { return nativeComponent; }
}
