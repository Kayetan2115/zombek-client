package pl.zombek.client.core.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Universal chat filter manager executing category toggles and priority-ordered custom rules.
 * Keeps hidden messages locally and tracks suppression metrics without altering network packets.
 */
public class ChatFilterManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatFilterManager.class);
    private static ChatFilterManager instance;

    // Common message patterns across Minecraft servers
    private static final Pattern PATTERN_JOIN = Pattern.compile("(?iu)(?:joined the game|dołączył do gry|has joined)");
    private static final Pattern PATTERN_LEAVE = Pattern.compile("(?iu)(?:left the game|opuścił grę|has left|has quit)");
    private static final Pattern PATTERN_DEATH = Pattern.compile("(?iu)(?:was slain by|drowned|hit the ground too hard|blew up|burned to death|zginął|został zabity)");
    private static final Pattern PATTERN_ADVANCEMENT = Pattern.compile("(?iu)(?:has made the advancement|has completed the challenge|has reached the goal|zdobył osiągnięcie)");
    private static final Pattern PATTERN_PRIVATE = Pattern.compile("(?iu)^(?:from|to|\\[from|\\[to|od|do|wisp to|whisper from)[:\\s]+");
    private static final Pattern PATTERN_TEAM = Pattern.compile("(?iu)^(?:Team|Drużyna)\\s*>\\s*");
    private static final Pattern PATTERN_PARTY = Pattern.compile("(?iu)^Party\\s*>\\s*");
    private static final Pattern PATTERN_GUILD = Pattern.compile("(?iu)^Guild\\s*>\\s*");

    // Master toggles
    private boolean enabled = true;
    private boolean paused = false;

    // Category Hide Toggles
    private final Map<ChatCategory, Boolean> categoryHiddenMap = new EnumMap<>(ChatCategory.class);

    // Custom Rules List (sorted by priority descending)
    private final List<UniversalChatFilterRule> rules = new ArrayList<>();

    // Hidden metrics
    private int hiddenMessagesCount = 0;

    public ChatFilterManager() {
        initDefaults();
    }

    public static synchronized ChatFilterManager getInstance() {
        if (instance == null) {
            instance = new ChatFilterManager();
        }
        return instance;
    }

    public synchronized void initDefaults() {
        categoryHiddenMap.clear();
        for (ChatCategory cat : ChatCategory.values()) {
            categoryHiddenMap.put(cat, false);
        }
        rules.clear();
        hiddenMessagesCount = 0;
    }

    /**
     * Automatically classifies raw message text into an appropriate ChatCategory if not already set.
     */
    public ChatCategory classifyMessage(String cleanText, boolean system) {
        if (cleanText == null || cleanText.isEmpty()) {
            return ChatCategory.OTHER_MESSAGES;
        }

        if (PATTERN_PRIVATE.matcher(cleanText).find()) {
            return ChatCategory.PRIVATE_MESSAGES;
        }
        if (PATTERN_PARTY.matcher(cleanText).find()) {
            return ChatCategory.PARTY_MESSAGES;
        }
        if (PATTERN_GUILD.matcher(cleanText).find()) {
            return ChatCategory.GUILD_MESSAGES;
        }
        if (PATTERN_TEAM.matcher(cleanText).find()) {
            return ChatCategory.TEAM_MESSAGES;
        }
        if (PATTERN_JOIN.matcher(cleanText).find()) {
            return ChatCategory.JOIN_MESSAGES;
        }
        if (PATTERN_LEAVE.matcher(cleanText).find()) {
            return ChatCategory.LEAVE_MESSAGES;
        }
        if (PATTERN_DEATH.matcher(cleanText).find()) {
            return ChatCategory.DEATH_MESSAGES;
        }
        if (PATTERN_ADVANCEMENT.matcher(cleanText).find()) {
            return ChatCategory.ADVANCEMENT_MESSAGES;
        }
        if (system) {
            return ChatCategory.SYSTEM_MESSAGES;
        }
        return ChatCategory.PLAYER_MESSAGES;
    }

    /**
     * Evaluates message against active category filters and custom rules.
     * Updates message properties (hidden, highlighted, color, targetTab, important).
     */
    public synchronized boolean evaluateMessage(ChatMessageEntry entry) {
        if (!enabled || paused || entry == null) {
            return false;
        }

        ChatCategory category = entry.getCategory();

        // 1. Check category toggle
        if (categoryHiddenMap.getOrDefault(category, false)) {
            entry.setHidden(true);
            hiddenMessagesCount++;
            return true;
        }

        // 2. Check custom rules in priority order
        for (UniversalChatFilterRule rule : rules) {
            if (!rule.isEnabled()) continue;

            if (rule.matches(entry.getCleanText(), category)) {
                switch (rule.getAction()) {
                    case HIDE_LOCALLY:
                        entry.setHidden(true);
                        hiddenMessagesCount++;
                        return true;
                    case HIGHLIGHT:
                        entry.setHighlighted(true);
                        entry.setHighlightColorArgb(rule.getHighlightColorArgb());
                        break;
                    case CHANGE_COLOR:
                        entry.setHighlightColorArgb(rule.getHighlightColorArgb());
                        break;
                    case MOVE_TO_TAB:
                        if (rule.getTargetTabId() != null && !rule.getTargetTabId().isEmpty()) {
                            entry.setAssignedTabId(rule.getTargetTabId());
                        }
                        break;
                    case MARK_IMPORTANT:
                        entry.setImportant(true);
                        break;
                }
            }
        }

        return entry.isHidden();
    }

    public synchronized void setCategoryHidden(ChatCategory category, boolean hidden) {
        categoryHiddenMap.put(category, hidden);
    }

    public synchronized boolean isCategoryHidden(ChatCategory category) {
        return categoryHiddenMap.getOrDefault(category, false);
    }

    public synchronized void addRule(UniversalChatFilterRule rule) {
        if (rule != null) {
            rules.add(rule);
            rules.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        }
    }

    public synchronized boolean removeRule(String ruleId) {
        boolean removed = rules.removeIf(r -> r.getId().equals(ruleId));
        return removed;
    }

    public synchronized List<UniversalChatFilterRule> getRules() {
        return Collections.unmodifiableList(new ArrayList<>(rules));
    }

    public synchronized void clearRules() {
        rules.clear();
    }

    public synchronized int getHiddenMessagesCount() {
        return hiddenMessagesCount;
    }

    public synchronized void resetHiddenMessagesCount() {
        hiddenMessagesCount = 0;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isPaused() { return paused; }
    public void setPaused(boolean paused) { this.paused = paused; }

    public static synchronized void resetForTesting() {
        instance = null;
    }
}
