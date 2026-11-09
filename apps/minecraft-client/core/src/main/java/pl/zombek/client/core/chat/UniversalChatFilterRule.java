package pl.zombek.client.core.chat;

import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Universal user-configurable chat filter rule with ReDoS guard.
 */
public class UniversalChatFilterRule {
    private String id;
    private String name;
    private boolean enabled;
    private ChatFilterMatchType matchType;
    private String patternString;
    private ChatCategory targetCategory;
    private ChatFilterAction action;
    private int priority;
    private int highlightColorArgb;
    private String targetTabId;

    // Transient compiled regex
    private transient Pattern compiledPattern;
    private transient boolean invalidRegex = false;

    public UniversalChatFilterRule(String name, ChatFilterMatchType matchType, String patternString,
                                   ChatCategory targetCategory, ChatFilterAction action, int priority) {
        this.id = UUID.randomUUID().toString();
        this.name = name != null ? name : "Reguła";
        this.enabled = true;
        this.matchType = matchType != null ? matchType : ChatFilterMatchType.CONTAINS;
        this.patternString = patternString != null ? patternString : "";
        this.targetCategory = targetCategory;
        this.action = action != null ? action : ChatFilterAction.HIDE_LOCALLY;
        this.priority = priority;
        this.highlightColorArgb = 0xFFFFFF00; // Default yellow highlight
        this.targetTabId = "all";
        compile();
    }

    public UniversalChatFilterRule(String id, String name, boolean enabled, ChatFilterMatchType matchType,
                                   String patternString, ChatCategory targetCategory, ChatFilterAction action,
                                   int priority, int highlightColorArgb, String targetTabId) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.name = name != null ? name : "Reguła";
        this.enabled = enabled;
        this.matchType = matchType != null ? matchType : ChatFilterMatchType.CONTAINS;
        this.patternString = patternString != null ? patternString : "";
        this.targetCategory = targetCategory;
        this.action = action != null ? action : ChatFilterAction.HIDE_LOCALLY;
        this.priority = priority;
        this.highlightColorArgb = highlightColorArgb;
        this.targetTabId = targetTabId != null ? targetTabId : "all";
        compile();
    }

    public void compile() {
        if (matchType == ChatFilterMatchType.REGEX && patternString != null && !patternString.isEmpty()) {
            if (patternString.length() > 100 || hasCatastrophicBacktrackingRisk(patternString)) {
                this.invalidRegex = true;
                this.compiledPattern = null;
                return;
            }
            try {
                this.compiledPattern = Pattern.compile("(?iu)" + patternString);
                this.invalidRegex = false;
            } catch (PatternSyntaxException e) {
                this.invalidRegex = true;
                this.compiledPattern = null;
            }
        } else {
            this.compiledPattern = null;
            this.invalidRegex = false;
        }
    }

    private boolean hasCatastrophicBacktrackingRisk(String regex) {
        return regex.contains("+)+") || regex.contains("*)*") || regex.contains("+)*") || regex.contains("*)+")
                || regex.contains("{") && regex.contains("}{");
    }

    public boolean matches(String cleanMessage, ChatCategory messageCategory) {
        if (!enabled || cleanMessage == null || patternString.isEmpty() || invalidRegex) {
            return false;
        }
        if (targetCategory != null && messageCategory != null && targetCategory != messageCategory) {
            return false;
        }

        switch (matchType) {
            case EXACT:
                return cleanMessage.equalsIgnoreCase(patternString);
            case CONTAINS:
                return cleanMessage.toLowerCase(Locale.ROOT).contains(patternString.toLowerCase(Locale.ROOT));
            case STARTS_WITH:
                return cleanMessage.toLowerCase(Locale.ROOT).startsWith(patternString.toLowerCase(Locale.ROOT));
            case ENDS_WITH:
                return cleanMessage.toLowerCase(Locale.ROOT).endsWith(patternString.toLowerCase(Locale.ROOT));
            case REGEX:
                return compiledPattern != null && compiledPattern.matcher(cleanMessage).find();
            default:
                return false;
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public ChatFilterMatchType getMatchType() { return matchType; }
    public void setMatchType(ChatFilterMatchType matchType) { this.matchType = matchType; compile(); }
    public String getPatternString() { return patternString; }
    public void setPatternString(String patternString) { this.patternString = patternString; compile(); }
    public ChatCategory getTargetCategory() { return targetCategory; }
    public void setTargetCategory(ChatCategory targetCategory) { this.targetCategory = targetCategory; }
    public ChatFilterAction getAction() { return action; }
    public void setAction(ChatFilterAction action) { this.action = action; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public int getHighlightColorArgb() { return highlightColorArgb; }
    public void setHighlightColorArgb(int highlightColorArgb) { this.highlightColorArgb = highlightColorArgb; }
    public String getTargetTabId() { return targetTabId; }
    public void setTargetTabId(String targetTabId) { this.targetTabId = targetTabId; }
    public boolean isInvalidRegex() { return invalidRegex; }
}
