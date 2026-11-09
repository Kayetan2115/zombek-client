package pl.zombek.client.core.friends;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Local Friend Manager for ZombekClient NameTags and Tab Editor.
 * Manages friend aliases, custom colors, badges, and local persistence.
 * Zero external network access, zero cloud synchronization, zero auto-invites.
 */
public final class NameTagFriendManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(NameTagFriendManager.class);
    private static final NameTagFriendManager INSTANCE = new NameTagFriendManager();

    public static NameTagFriendManager getInstance() {
        return INSTANCE;
    }

    private final Map<UUID, FriendEntry> friendsByUuid = new ConcurrentHashMap<>();
    private final Map<String, FriendEntry> friendsByUsername = new ConcurrentHashMap<>();
    private Path configDirectory = null;

    private NameTagFriendManager() {}

    public void setConfigDirectory(Path configDir) {
        this.configDirectory = configDir;
        loadFriends();
    }

    public synchronized void addFriend(FriendEntry friend) {
        if (friend == null) return;
        if (friend.getUuid() != null) {
            friendsByUuid.put(friend.getUuid(), friend);
        }
        if (!friend.getUsername().isBlank()) {
            friendsByUsername.put(friend.getUsername().toLowerCase(Locale.ROOT), friend);
        }
        saveFriends();
    }

    public synchronized void removeFriend(UUID uuid, String username) {
        if (uuid != null) {
            friendsByUuid.remove(uuid);
        }
        if (username != null && !username.isBlank()) {
            friendsByUsername.remove(username.toLowerCase(Locale.ROOT));
        }
        saveFriends();
    }

    public boolean isFriend(UUID uuid, String username) {
        if (uuid != null && friendsByUuid.containsKey(uuid)) {
            return true;
        }
        if (username != null && !username.isBlank()) {
            return friendsByUsername.containsKey(username.toLowerCase(Locale.ROOT));
        }
        return false;
    }

    public FriendEntry getFriend(UUID uuid, String username) {
        if (uuid != null) {
            FriendEntry entry = friendsByUuid.get(uuid);
            if (entry != null) return entry;
        }
        if (username != null && !username.isBlank()) {
            return friendsByUsername.get(username.toLowerCase(Locale.ROOT));
        }
        return null;
    }

    public List<FriendEntry> getAllFriends() {
        Set<FriendEntry> all = new LinkedHashSet<>(friendsByUuid.values());
        all.addAll(friendsByUsername.values());
        return new ArrayList<>(all);
    }

    public synchronized void clear() {
        friendsByUuid.clear();
        friendsByUsername.clear();
        saveFriends();
    }

    public synchronized void clearAll() {
        clear();
    }

    private File getFriendsFile() {
        if (configDirectory == null) return null;
        return configDirectory.resolve("friends.json").toFile();
    }

    public synchronized void loadFriends() {
        File file = getFriendsFile();
        if (file == null || !file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            com.google.gson.JsonElement json = com.google.gson.JsonParser.parseReader(reader);
            if (json != null && json.isJsonArray()) {
                friendsByUuid.clear();
                friendsByUsername.clear();
                for (com.google.gson.JsonElement el : json.getAsJsonArray()) {
                    if (el.isJsonObject()) {
                        com.google.gson.JsonObject obj = el.getAsJsonObject();
                        UUID uuid = null;
                        if (obj.has("uuid") && !obj.get("uuid").getAsString().isBlank()) {
                            try {
                                uuid = UUID.fromString(obj.get("uuid").getAsString());
                            } catch (Exception ignored) {}
                        }
                        String username = obj.has("username") ? obj.get("username").getAsString() : "";
                        String alias = obj.has("alias") ? obj.get("alias").getAsString() : "";
                        int color = obj.has("color") ? obj.get("color").getAsInt() : 0xFF55FFFF;
                        String icon = obj.has("icon") ? obj.get("icon").getAsString() : "★";
                        String prefix = obj.has("prefix") ? obj.get("prefix").getAsString() : "";
                        String notes = obj.has("notes") ? obj.get("notes").getAsString() : "";

                        FriendEntry entry = new FriendEntry(uuid, username, alias, color, icon, prefix, notes);
                        if (uuid != null) friendsByUuid.put(uuid, entry);
                        if (!username.isBlank()) friendsByUsername.put(username.toLowerCase(Locale.ROOT), entry);
                    }
                }
                LOGGER.info("[NameTagFriendManager] Loaded {} local friends from friends.json", friendsByUuid.size() + friendsByUsername.size());
            }
        } catch (Throwable t) {
            LOGGER.error("[NameTagFriendManager] Failed to load friends.json: {}", t.getMessage());
        }
    }

    public synchronized void saveFriends() {
        File file = getFriendsFile();
        if (file == null) return;

        try {
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
            for (FriendEntry entry : getAllFriends()) {
                com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
                obj.addProperty("uuid", entry.getUuid() != null ? entry.getUuid().toString() : "");
                obj.addProperty("username", entry.getUsername());
                obj.addProperty("alias", entry.getAlias());
                obj.addProperty("color", entry.getCustomColorArgb());
                obj.addProperty("icon", entry.getFriendMarkerIcon());
                obj.addProperty("prefix", entry.getPrefix());
                obj.addProperty("notes", entry.getNotes());
                arr.add(obj);
            }

            try (FileWriter writer = new FileWriter(file)) {
                new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(arr, writer);
            }
        } catch (Throwable t) {
            LOGGER.error("[NameTagFriendManager] Failed to save friends.json: {}", t.getMessage());
        }
    }
}
