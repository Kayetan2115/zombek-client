package pl.zombek.client.core.hypixel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Local chat message filter for Hypixel Network.
 * Evaluates category toggles and custom regex rules, preventing spam from appearing
 * in the client view without deleting original server messages or leaking data.
 */
public class HypixelChatFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(HypixelChatFilter.class);
    private static HypixelChatFilter instance;

    // Built-in Hypixel chat category patterns
    private static final Pattern PATTERN_PRIVATE = Pattern.compile("(?iu)^(?:from|to|\\[from|\\[to|od|do)[:\\s]+");
    private static final Pattern PATTERN_TEAM = Pattern.compile("(?iu)^Team\\s*>\\s*");
    private static final Pattern PATTERN_PARTY = Pattern.compile("(?iu)^Party\\s*>\\s*");
    private static final Pattern PATTERN_GUILD = Pattern.compile("(?iu)^Guild\\s*>\\s*");
    private static final Pattern PATTERN_OFFICER = Pattern.compile("(?iu)^Officer\\s*>\\s*");
    private static final Pattern PATTERN_SHOUT = Pattern.compile("(?iu)^\\[SHOUT\\]\\s*");
    private static final Pattern PATTERN_SPECTATOR = Pattern.compile("(?iu)^\\[SPECTATOR\\]\\s*");
    private static final Pattern PATTERN_LOBBY = Pattern.compile("(?iu)(?:joined the lobby|left the lobby|sending you to|connecting to|server restarting)");
    private static final Pattern PATTERN_JOIN = Pattern.compile("(?iu)(?:has joined \\(\\d+/\\d+\\)|dołączył do gry)");
    private static final Pattern PATTERN_LEAVE = Pattern.compile("(?iu)(?:has quit!|has left!|opuścił grę)");
    private static final Pattern PATTERN_SOUL_WELL = Pattern.compile("(?iu)(?:in the soul well!|soul well)");
    private static final Pattern PATTERN_MYSTERY_BOX = Pattern.compile("(?iu)(?:found a .* mystery box!|mystery box)");

    // Category Toggles
    private boolean enabled = true;
    private boolean filterPrivateMessages = false;
    private boolean filterTeamChat = false;
    private boolean filterPartyChat = false;
    private boolean filterGuildChat = false;
    private boolean filterOfficerChat = false;
    private boolean filterShoutMessages = false;
    private boolean filterSpectatorChat = false;
    private boolean filterLobbyStatusMessages = true;
    private boolean filterJoinMessages = false;
    private boolean filterLeaveMessages = false;
    private boolean filterSoulWellAnnouncements = true;
    private boolean filterMysteryBoxAnnouncements = true;

    // Custom Rules List
    private final List<ChatFilterRule> customRules = new ArrayList<>();

    // Statistics
    private int filteredCount = 0;

    public HypixelChatFilter() {}

    public static synchronized HypixelChatFilter getInstance() {
        if (instance == null) {
            instance = new HypixelChatFilter();
        }
        return instance;
    }

    /**
     * Determines whether an incoming chat message should be suppressed from local display.
     */
    public synchronized boolean shouldFilter(String rawMessage) {
        if (!enabled || rawMessage == null || rawMessage.trim().isEmpty()) {
            return false;
        }

        String clean = stripFormatting(rawMessage).trim();

        // 1. Check built-in categories
        if (filterPrivateMessages && PATTERN_PRIVATE.matcher(clean).find()) {
            incrementFiltered();
            return true;
        }

        // Universal ChatFilterManager synchronization
        pl.zombek.client.core.chat.ChatFilterManager universalFilter = pl.zombek.client.core.chat.ChatFilterManager.getInstance();
        if (universalFilter != null && universalFilter.isEnabled()) {
            pl.zombek.client.core.chat.ChatCategory cat = universalFilter.classifyMessage(clean, false);
            if (universalFilter.isCategoryHidden(cat)) {
                incrementFiltered();
                return true;
            }
        }
        if (filterTeamChat && PATTERN_TEAM.matcher(clean).find()) {
            incrementFiltered();
            return true;
        }
        if (filterPartyChat && PATTERN_PARTY.matcher(clean).find()) {
            incrementFiltered();
            return true;
        }
        if (filterGuildChat && PATTERN_GUILD.matcher(clean).find()) {
            incrementFiltered();
            return true;
        }
        if (filterOfficerChat && PATTERN_OFFICER.matcher(clean).find()) {
            incrementFiltered();
            return true;
        }
        if (filterShoutMessages && PATTERN_SHOUT.matcher(clean).find()) {
            incrementFiltered();
            return true;
        }
        if (filterSpectatorChat && PATTERN_SPECTATOR.matcher(clean).find()) {
            incrementFiltered();
            return true;
        }
        if (filterLobbyStatusMessages && PATTERN_LOBBY.matcher(clean).find()) {
            incrementFiltered();
            return true;
        }
        if (filterJoinMessages && PATTERN_JOIN.matcher(clean).find()) {
            incrementFiltered();
            return true;
        }
        if (filterLeaveMessages && PATTERN_LEAVE.matcher(clean).find()) {
            incrementFiltered();
            return true;
        }
        if (filterSoulWellAnnouncements && PATTERN_SOUL_WELL.matcher(clean).find()) {
            incrementFiltered();
            return true;
        }
        if (filterMysteryBoxAnnouncements && PATTERN_MYSTERY_BOX.matcher(clean).find()) {
            incrementFiltered();
            return true;
        }

        // 2. Check custom user rules
        for (ChatFilterRule rule : customRules) {
            if (rule.matches(clean)) {
                incrementFiltered();
                return true;
            }
        }

        return false;
    }

    private void incrementFiltered() {
        this.filteredCount++;
    }

    public synchronized void disableAllFilters() {
        this.filterPrivateMessages = false;
        this.filterTeamChat = false;
        this.filterPartyChat = false;
        this.filterGuildChat = false;
        this.filterOfficerChat = false;
        this.filterShoutMessages = false;
        this.filterSpectatorChat = false;
        this.filterLobbyStatusMessages = false;
        this.filterJoinMessages = false;
        this.filterLeaveMessages = false;
        this.filterSoulWellAnnouncements = false;
        this.filterMysteryBoxAnnouncements = false;
        for (ChatFilterRule rule : customRules) {
            rule.setEnabled(false);
        }
        LOGGER.info("[HypixelChatFilter] All chat filters disabled.");
    }

    public static String stripFormatting(String input) {
        if (input == null) return "";
        return input.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }

    public synchronized void addCustomRule(ChatFilterRule rule) {
        if (rule != null) {
            customRules.add(rule);
        }
    }

    public synchronized boolean removeCustomRule(String name) {
        return customRules.removeIf(r -> r.getName().equalsIgnoreCase(name));
    }

    public synchronized void clearCustomRules() {
        customRules.clear();
    }

    public synchronized void resetFilteredCount() {
        this.filteredCount = 0;
    }

    // Getters and Setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isFilterPrivateMessages() { return filterPrivateMessages; }
    public void setFilterPrivateMessages(boolean filter) { this.filterPrivateMessages = filter; }
    public boolean isFilterTeamChat() { return filterTeamChat; }
    public void setFilterTeamChat(boolean filter) { this.filterTeamChat = filter; }
    public boolean isFilterPartyChat() { return filterPartyChat; }
    public void setFilterPartyChat(boolean filter) { this.filterPartyChat = filter; }
    public boolean isFilterGuildChat() { return filterGuildChat; }
    public void setFilterGuildChat(boolean filter) { this.filterGuildChat = filter; }
    public boolean isFilterOfficerChat() { return filterOfficerChat; }
    public void setFilterOfficerChat(boolean filter) { this.filterOfficerChat = filter; }
    public boolean isFilterShoutMessages() { return filterShoutMessages; }
    public void setFilterShoutMessages(boolean filter) { this.filterShoutMessages = filter; }
    public boolean isFilterSpectatorChat() { return filterSpectatorChat; }
    public void setFilterSpectatorChat(boolean filter) { this.filterSpectatorChat = filter; }
    public boolean isFilterLobbyStatusMessages() { return filterLobbyStatusMessages; }
    public void setFilterLobbyStatusMessages(boolean filter) { this.filterLobbyStatusMessages = filter; }
    public boolean isFilterJoinMessages() { return filterJoinMessages; }
    public void setFilterJoinMessages(boolean filter) { this.filterJoinMessages = filter; }
    public boolean isFilterLeaveMessages() { return filterLeaveMessages; }
    public void setFilterLeaveMessages(boolean filter) { this.filterLeaveMessages = filter; }
    public boolean isFilterSoulWellAnnouncements() { return filterSoulWellAnnouncements; }
    public void setFilterSoulWellAnnouncements(boolean filter) { this.filterSoulWellAnnouncements = filter; }
    public boolean isFilterMysteryBoxAnnouncements() { return filterMysteryBoxAnnouncements; }
    public void setFilterMysteryBoxAnnouncements(boolean filter) { this.filterMysteryBoxAnnouncements = filter; }
    public int getFilteredCount() { return filteredCount; }
    public List<ChatFilterRule> getCustomRules() { return Collections.unmodifiableList(customRules); }
}
