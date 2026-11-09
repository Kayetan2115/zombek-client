package pl.zombek.client.core.playerinfo;

import pl.zombek.client.core.friends.FriendEntry;

import java.util.*;

/**
 * Immutable DTO providing unified player information across NameTags and Tab Editor.
 * Tracks explicit origin and availability for all fields (strict Fair Play).
 */
public final class PlayerInfoData {

    private final UUID uuid;
    private final String username;
    private final String displayName;
    private final String serverPrefix;
    private final String serverSuffix;
    private final String teamName;
    private final int teamColorArgb;

    private final int latency;
    private final DataAvailability latencyAvailability;

    private final String gameMode;
    private final DataAvailability gameModeAvailability;

    private final float health;
    private final float maxHealth;
    private final float absorption;
    private final DataAvailability healthAvailability;

    private final List<PlayerArmorData> armor;
    private final DataAvailability armorAvailability;

    private final String heldItem;
    private final DataAvailability heldItemAvailability;

    private final double distance;
    private final DataAvailability distanceAvailability;

    private final boolean isFriend;
    private final FriendEntry friendEntry;
    private final Object skinHandle;

    public PlayerInfoData(
            UUID uuid,
            String username,
            String displayName,
            String serverPrefix,
            String serverSuffix,
            String teamName,
            int teamColorArgb,
            int latency,
            DataAvailability latencyAvailability,
            String gameMode,
            DataAvailability gameModeAvailability,
            float health,
            float maxHealth,
            float absorption,
            DataAvailability healthAvailability,
            List<PlayerArmorData> armor,
            DataAvailability armorAvailability,
            String heldItem,
            DataAvailability heldItemAvailability,
            double distance,
            DataAvailability distanceAvailability,
            boolean isFriend,
            FriendEntry friendEntry,
            Object skinHandle) {
        this.uuid = uuid;
        this.username = username != null ? username : "Player";
        this.displayName = displayName != null ? displayName : this.username;
        this.serverPrefix = serverPrefix != null ? serverPrefix : "";
        this.serverSuffix = serverSuffix != null ? serverSuffix : "";
        this.teamName = teamName != null ? teamName : "";
        this.teamColorArgb = teamColorArgb != 0 ? teamColorArgb : 0xFFFFFFFF;
        this.latency = latency;
        this.latencyAvailability = latencyAvailability != null ? latencyAvailability : DataAvailability.UNKNOWN;
        this.gameMode = gameMode != null ? gameMode : "SURVIVAL";
        this.gameModeAvailability = gameModeAvailability != null ? gameModeAvailability : DataAvailability.UNKNOWN;
        this.health = health;
        this.maxHealth = maxHealth > 0 ? maxHealth : 20.0f;
        this.absorption = absorption;
        this.healthAvailability = healthAvailability != null ? healthAvailability : DataAvailability.UNKNOWN;
        this.armor = armor != null ? Collections.unmodifiableList(new ArrayList<>(armor)) : Collections.emptyList();
        this.armorAvailability = armorAvailability != null ? armorAvailability : DataAvailability.UNKNOWN;
        this.heldItem = heldItem != null ? heldItem : "";
        this.heldItemAvailability = heldItemAvailability != null ? heldItemAvailability : DataAvailability.UNKNOWN;
        this.distance = distance;
        this.distanceAvailability = distanceAvailability != null ? distanceAvailability : DataAvailability.UNKNOWN;
        this.isFriend = isFriend;
        this.friendEntry = friendEntry;
        this.skinHandle = skinHandle;
    }

    public UUID getUuid() { return uuid; }
    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public String getServerPrefix() { return serverPrefix; }
    public String getServerSuffix() { return serverSuffix; }
    public String getTeamName() { return teamName; }
    public int getTeamColorArgb() { return teamColorArgb; }

    public int getLatency() { return latency; }
    public DataAvailability getLatencyAvailability() { return latencyAvailability; }

    public String getGameMode() { return gameMode; }
    public DataAvailability getGameModeAvailability() { return gameModeAvailability; }

    public float getHealth() { return health; }
    public float getMaxHealth() { return maxHealth; }
    public float getAbsorption() { return absorption; }
    public DataAvailability getHealthAvailability() { return healthAvailability; }

    public List<PlayerArmorData> getArmor() { return armor; }
    public DataAvailability getArmorAvailability() { return armorAvailability; }

    public String getHeldItem() { return heldItem; }
    public DataAvailability getHeldItemAvailability() { return heldItemAvailability; }

    public double getDistance() { return distance; }
    public DataAvailability getDistanceAvailability() { return distanceAvailability; }

    public boolean isFriend() { return isFriend; }
    public FriendEntry getFriendEntry() { return friendEntry; }
    public Object getSkinHandle() { return skinHandle; }

    public String getEffectiveName(boolean enableAlias) {
        if (enableAlias && friendEntry != null && !friendEntry.getAlias().isBlank()) {
            return friendEntry.getAlias();
        }
        return displayName;
    }
}
