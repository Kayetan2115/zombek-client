package pl.zombek.client.core.playerinfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.friends.FriendEntry;
import pl.zombek.client.core.friends.NameTagFriendManager;
import pl.zombek.client.core.modules.hud.teamview.TeamDetectionMode;
import pl.zombek.client.core.modules.hud.teamview.TeamTracker;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central Player Information Service providing cached and enriched player and entity metadata.
 * Single source of truth for NameTags and Tab Editor.
 * Zero external network queries, zero fake assumptions.
 */
public final class ZombekPlayerInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZombekPlayerInfoService.class);
    private static final ZombekPlayerInfoService INSTANCE = new ZombekPlayerInfoService();

    public static ZombekPlayerInfoService getInstance() {
        return INSTANCE;
    }

    private final Map<UUID, PlayerInfoData> playerCacheByUuid = new ConcurrentHashMap<>();
    private final Map<String, PlayerInfoData> playerCacheByUsername = new ConcurrentHashMap<>();
    private volatile long lastRefreshTime = 0L;
    private static final long CACHE_TTL_MS = 250L; // 4 times per second refresh limit

    private ZombekPlayerInfoService() {}

    public synchronized List<PlayerInfoData> getPlayers() {
        long now = System.currentTimeMillis();
        if (now - lastRefreshTime > CACHE_TTL_MS || playerCacheByUuid.isEmpty()) {
            refreshCache();
        }
        return new ArrayList<>(playerCacheByUuid.values());
    }

    public PlayerInfoData getPlayer(UUID uuid, String username) {
        if (uuid != null) {
            PlayerInfoData p = playerCacheByUuid.get(uuid);
            if (p != null) return p;
        }
        if (username != null && !username.isBlank()) {
            PlayerInfoData p = playerCacheByUsername.get(username.toLowerCase(Locale.ROOT));
            if (p != null) return p;
        }

        // Query platform provider on-demand
        IPlayerInfoDataProvider provider = PlayerInfoDataProviderRegistry.getInstance().getProvider();
        PlayerInfoData raw = provider.getPlayerInfo(uuid, username);
        return enrichPlayerData(raw);
    }

    public PlayerInfoData getEntityInfo(Object entityHandle) {
        if (entityHandle == null) return null;
        IPlayerInfoDataProvider provider = PlayerInfoDataProviderRegistry.getInstance().getProvider();
        PlayerInfoData raw = provider.getEntityInfo(entityHandle);
        return enrichPlayerData(raw);
    }

    public synchronized void refreshCache() {
        IPlayerInfoDataProvider provider = PlayerInfoDataProviderRegistry.getInstance().getProvider();
        List<PlayerInfoData> players = provider.getConnectedPlayers();

        playerCacheByUuid.clear();
        playerCacheByUsername.clear();

        for (PlayerInfoData raw : players) {
            PlayerInfoData enriched = enrichPlayerData(raw);
            if (enriched.getUuid() != null) {
                playerCacheByUuid.put(enriched.getUuid(), enriched);
            }
            if (!enriched.getUsername().isBlank()) {
                playerCacheByUsername.put(enriched.getUsername().toLowerCase(Locale.ROOT), enriched);
            }
        }
        lastRefreshTime = System.currentTimeMillis();
    }

    private PlayerInfoData enrichPlayerData(PlayerInfoData raw) {
        if (raw == null) return null;

        // Check Friend Status
        FriendEntry friend = NameTagFriendManager.getInstance().getFriend(raw.getUuid(), raw.getUsername());
        boolean isFriend = (friend != null);

        // Check Team Status from TeamTracker if available
        String team = raw.getTeamName();
        int teamColor = raw.getTeamColorArgb();
        if (team.isEmpty()) {
            boolean isTeammate = TeamTracker.getInstance().isTeammate(
                    raw.getUuid(), raw.getUsername(), "", "", TeamDetectionMode.MANUAL_TEAM
            );
            if (isTeammate) {
                team = "ManualTeam";
                teamColor = 0xFF55FF55;
            }
        }

        return new PlayerInfoData(
                raw.getUuid(),
                raw.getUsername(),
                raw.getDisplayName(),
                raw.getServerPrefix(),
                raw.getServerSuffix(),
                team,
                teamColor,
                raw.getLatency(),
                raw.getLatencyAvailability(),
                raw.getGameMode(),
                raw.getGameModeAvailability(),
                raw.getHealth(),
                raw.getMaxHealth(),
                raw.getAbsorption(),
                raw.getHealthAvailability(),
                raw.getArmor(),
                raw.getArmorAvailability(),
                raw.getHeldItem(),
                raw.getHeldItemAvailability(),
                raw.getDistance(),
                raw.getDistanceAvailability(),
                isFriend,
                friend,
                raw.getSkinHandle()
        );
    }

    public synchronized void onClientTick() {
        // Opportunistic refresh check on tick
        long now = System.currentTimeMillis();
        if (now - lastRefreshTime > CACHE_TTL_MS) {
            refreshCache();
        }
    }

    public synchronized void clearCache() {
        onWorldLeave();
    }

    public synchronized void onWorldLeave() {
        LOGGER.info("[ZombekPlayerInfoService] Player left world. Clearing player info cache.");
        playerCacheByUuid.clear();
        playerCacheByUsername.clear();
        lastRefreshTime = 0L;
    }

    public synchronized void onWorldJoin(String worldId) {
        LOGGER.info("[ZombekPlayerInfoService] Player joined world: {}. Resetting player info cache.", worldId);
        playerCacheByUuid.clear();
        playerCacheByUsername.clear();
        lastRefreshTime = 0L;
    }

    public synchronized void resetForTesting() {
        playerCacheByUuid.clear();
        playerCacheByUsername.clear();
        lastRefreshTime = 0L;
    }
}
