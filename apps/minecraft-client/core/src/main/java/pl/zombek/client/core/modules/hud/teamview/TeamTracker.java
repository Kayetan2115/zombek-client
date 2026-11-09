package pl.zombek.client.core.modules.hud.teamview;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Registry and cache for teammates, strictly limited to client-visible entities
 * and authorized synchronization.
 */
public class TeamTracker implements ITeamDirectionProvider {
    private static final TeamTracker INSTANCE = new TeamTracker();

    private final Set<String> manualMembers = new CopyOnWriteArraySet<>();
    private final Map<UUID, TrackedTeammate> visibleTeammates = new ConcurrentHashMap<>();

    private TeamTracker() {}

    public static TeamTracker getInstance() {
        return INSTANCE;
    }

    public void addManualMember(String usernameOrUuid) {
        if (usernameOrUuid != null && !usernameOrUuid.isBlank()) {
            manualMembers.add(usernameOrUuid.trim().toLowerCase());
        }
    }

    public void removeManualMember(String usernameOrUuid) {
        if (usernameOrUuid != null) {
            manualMembers.remove(usernameOrUuid.trim().toLowerCase());
        }
    }

    public void clearManualTeam() {
        manualMembers.clear();
    }

    public Set<String> getManualMembers() {
        return Collections.unmodifiableSet(manualMembers);
    }

    /**
     * Checks if a player qualifies as a teammate.
     */
    public boolean isTeammate(UUID uuid, String username, String theirTeam, String myTeam, TeamDetectionMode mode) {
        if (username == null) return false;

        boolean matchesManual = manualMembers.contains(username.toLowerCase())
                || (uuid != null && manualMembers.contains(uuid.toString().toLowerCase()));

        boolean matchesVanilla = (myTeam != null && !myTeam.isBlank() && theirTeam != null && myTeam.equalsIgnoreCase(theirTeam));

        return switch (mode) {
            case VANILLA_TEAMS -> matchesVanilla;
            case MANUAL_TEAM -> matchesManual;
            case BOTH -> matchesVanilla || matchesManual;
        };
    }

    public void updateTeammate(TeammateData data) {
        if (data != null && data.uuid() != null) {
            visibleTeammates.put(data.uuid(), new TrackedTeammate(data, System.currentTimeMillis()));
        }
    }

    public void removeTeammate(UUID uuid) {
        if (uuid != null) {
            visibleTeammates.remove(uuid);
        }
    }

    public void pruneStale(long maxAgeMs) {
        long now = System.currentTimeMillis();
        visibleTeammates.entrySet().removeIf(e -> now - e.getValue().lastSeenMs > maxAgeMs);
    }

    public void clear() {
        visibleTeammates.clear();
    }

    public List<TeammateData> getVisibleTeammates() {
        List<TeammateData> list = new ArrayList<>(visibleTeammates.size());
        for (TrackedTeammate entry : visibleTeammates.values()) {
            list.add(entry.data);
        }
        return list;
    }

    @Override
    public List<TeammateDirectionInfo> getTeammateDirections() {
        List<TeammateDirectionInfo> directions = new ArrayList<>();
        for (TrackedTeammate entry : visibleTeammates.values()) {
            TeammateData data = entry.data;
            directions.add(new TeammateDirectionInfo(
                    data.uuid(),
                    data.name(),
                    data.teamColorArgb(),
                    data.distanceBlocks(),
                    0.0 // Relative angle resolved at render-time against camera yaw
            ));
        }
        return directions;
    }

    private static class TrackedTeammate {
        final TeammateData data;
        final long lastSeenMs;

        TrackedTeammate(TeammateData data, long lastSeenMs) {
            this.data = data;
            this.lastSeenMs = lastSeenMs;
        }
    }
}
