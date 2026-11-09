package pl.zombek.client.core.friends;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable model of a locally marked friend in ZombekClient.
 * Strictly local to the player's device; never transmitted to external APIs or servers.
 */
public final class FriendEntry {

    private final UUID uuid;
    private final String username;
    private final String alias;
    private final int customColorArgb;
    private final String friendMarkerIcon;
    private final String prefix;
    private final String notes;

    public FriendEntry(UUID uuid, String username, String alias, int customColorArgb, String friendMarkerIcon, String prefix, String notes) {
        this.uuid = uuid;
        this.username = username != null ? username : "";
        this.alias = alias != null ? alias : "";
        this.customColorArgb = customColorArgb;
        this.friendMarkerIcon = friendMarkerIcon != null ? friendMarkerIcon : "★";
        this.prefix = prefix != null ? prefix : "";
        this.notes = notes != null ? notes : "";
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getAlias() {
        return alias;
    }

    public int getCustomColorArgb() {
        return customColorArgb;
    }

    public int getColorArgb() {
        return customColorArgb;
    }

    public String getFriendMarkerIcon() {
        return friendMarkerIcon;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getNotes() {
        return notes;
    }

    public String getEffectiveDisplayName() {
        if (!alias.isBlank()) {
            return alias;
        }
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendEntry that = (FriendEntry) o;
        if (uuid != null && that.uuid != null) {
            return uuid.equals(that.uuid);
        }
        return username.equalsIgnoreCase(that.username);
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : username.toLowerCase().hashCode();
    }
}
