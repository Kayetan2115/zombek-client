package pl.zombek.client.core.chat;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Visual chat tab definition for dividing incoming chat messages into local channels.
 */
public class ChatTab {
    private String id;
    private String displayName;
    private int colorArgb;
    private String icon;
    private Set<ChatCategory> allowedCategories;
    private String customSenderFilter;
    private int unreadCount;
    private int order;
    private boolean defaultTab;
    private boolean removable;

    public ChatTab(String id, String displayName, int colorArgb, String icon, int order, boolean defaultTab, boolean removable) {
        this.id = id;
        this.displayName = displayName;
        this.colorArgb = colorArgb;
        this.icon = icon != null ? icon : "";
        this.allowedCategories = EnumSet.allOf(ChatCategory.class);
        this.customSenderFilter = null;
        this.unreadCount = 0;
        this.order = order;
        this.defaultTab = defaultTab;
        this.removable = removable;
    }

    public ChatTab(String id, String displayName, int colorArgb, String icon, Set<ChatCategory> allowedCategories,
                   int order, boolean defaultTab, boolean removable) {
        this.id = id;
        this.displayName = displayName;
        this.colorArgb = colorArgb;
        this.icon = icon != null ? icon : "";
        this.allowedCategories = allowedCategories != null ? EnumSet.copyOf(allowedCategories) : EnumSet.allOf(ChatCategory.class);
        this.customSenderFilter = null;
        this.unreadCount = 0;
        this.order = order;
        this.defaultTab = defaultTab;
        this.removable = removable;
    }

    public boolean matches(ChatMessageEntry message) {
        if (message == null) return false;
        if ("all".equalsIgnoreCase(this.id)) {
            return true;
        }
        if (customSenderFilter != null && !customSenderFilter.trim().isEmpty()) {
            if (message.getSenderName() != null &&
                    message.getSenderName().toLowerCase(Locale.ROOT).contains(customSenderFilter.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return allowedCategories != null && allowedCategories.contains(message.getCategory());
    }

    public void incrementUnread() {
        this.unreadCount++;
    }

    public void resetUnread() {
        this.unreadCount = 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public int getColorArgb() { return colorArgb; }
    public void setColorArgb(int colorArgb) { this.colorArgb = colorArgb; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Set<ChatCategory> getAllowedCategories() { return allowedCategories; }
    public void setAllowedCategories(Set<ChatCategory> allowedCategories) { this.allowedCategories = allowedCategories; }
    public String getCustomSenderFilter() { return customSenderFilter; }
    public void setCustomSenderFilter(String customSenderFilter) { this.customSenderFilter = customSenderFilter; }
    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
    public boolean isDefaultTab() { return defaultTab; }
    public void setDefaultTab(boolean defaultTab) { this.defaultTab = defaultTab; }
    public boolean isRemovable() { return removable; }
    public void setRemovable(boolean removable) { this.removable = removable; }
}
