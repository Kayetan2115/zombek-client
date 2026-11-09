package pl.zombek.client.core.chat;

import java.util.*;

/**
 * Manages local chat tabs, tab switching, and unread counters.
 */
public class ChatTabManager {
    private static ChatTabManager instance;

    private final List<ChatTab> tabs = new ArrayList<>();
    private String activeTabId = "all";

    public ChatTabManager() {
        initDefaultTabs();
    }

    public static synchronized ChatTabManager getInstance() {
        if (instance == null) {
            instance = new ChatTabManager();
        }
        return instance;
    }

    public synchronized void initDefaultTabs() {
        tabs.clear();

        // 1. All (everything)
        tabs.add(new ChatTab("all", "Wszystko", 0xFFFFFFFF, "💬", 0, true, false));

        // 2. General (players)
        Set<ChatCategory> generalCats = EnumSet.of(ChatCategory.PLAYER_MESSAGES, ChatCategory.JOIN_MESSAGES, ChatCategory.LEAVE_MESSAGES);
        tabs.add(new ChatTab("general", "Ogólny", 0xFFAAAAAA, "👥", generalCats, 1, false, true));

        // 3. Private (/msg)
        Set<ChatCategory> privateCats = EnumSet.of(ChatCategory.PRIVATE_MESSAGES);
        tabs.add(new ChatTab("private", "Prywatne", 0xFFFF55FF, "✉", privateCats, 2, false, true));

        // 4. Party (Party >)
        Set<ChatCategory> partyCats = EnumSet.of(ChatCategory.PARTY_MESSAGES);
        tabs.add(new ChatTab("party", "Party", 0xFF55FFFF, "🎉", partyCats, 3, false, true));

        // 5. Team (Team >)
        Set<ChatCategory> teamCats = EnumSet.of(ChatCategory.TEAM_MESSAGES);
        tabs.add(new ChatTab("team", "Drużyna", 0xFF55FF55, "⚔", teamCats, 4, false, true));

        // 6. Guild (Guild >)
        Set<ChatCategory> guildCats = EnumSet.of(ChatCategory.GUILD_MESSAGES);
        tabs.add(new ChatTab("guild", "Gildia", 0xFF00AAAA, "🛡", guildCats, 5, false, true));

        // 7. System (System / commands / deaths / advancements)
        Set<ChatCategory> systemCats = EnumSet.of(ChatCategory.SYSTEM_MESSAGES, ChatCategory.COMMAND_FEEDBACK,
                ChatCategory.DEATH_MESSAGES, ChatCategory.ADVANCEMENT_MESSAGES, ChatCategory.OTHER_MESSAGES);
        tabs.add(new ChatTab("system", "System", 0xFFFFFF55, "⚙", systemCats, 6, false, true));

        activeTabId = "all";
    }

    public synchronized void switchTab(String tabId) {
        if (tabId == null || tabId.isEmpty()) return;
        for (ChatTab tab : tabs) {
            if (tab.getId().equalsIgnoreCase(tabId)) {
                this.activeTabId = tab.getId();
                tab.resetUnread();
                return;
            }
        }
    }

    public synchronized ChatTab getActiveTab() {
        for (ChatTab tab : tabs) {
            if (tab.getId().equalsIgnoreCase(activeTabId)) {
                return tab;
            }
        }
        return tabs.isEmpty() ? null : tabs.get(0);
    }

    public synchronized String getActiveTabId() {
        return activeTabId;
    }

    public synchronized void dispatchToTabs(ChatMessageEntry entry) {
        if (entry == null || entry.isHidden()) return;

        for (ChatTab tab : tabs) {
            if (tab.matches(entry)) {
                if (!tab.getId().equalsIgnoreCase(activeTabId)) {
                    tab.incrementUnread();
                }
            }
        }
    }

    public synchronized void addCustomTab(ChatTab tab) {
        if (tab != null) {
            tabs.add(tab);
            tabs.sort(Comparator.comparingInt(ChatTab::getOrder));
        }
    }

    public synchronized boolean removeTab(String tabId) {
        for (Iterator<ChatTab> it = tabs.iterator(); it.hasNext(); ) {
            ChatTab tab = it.next();
            if (tab.getId().equalsIgnoreCase(tabId) && tab.isRemovable()) {
                it.remove();
                if (activeTabId.equalsIgnoreCase(tabId)) {
                    activeTabId = "all";
                }
                return true;
            }
        }
        return false;
    }

    public synchronized List<ChatTab> getTabs() {
        return Collections.unmodifiableList(new ArrayList<>(tabs));
    }

    public static synchronized void resetForTesting() {
        instance = null;
    }
}
