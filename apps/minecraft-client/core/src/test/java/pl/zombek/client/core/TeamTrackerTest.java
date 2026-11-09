package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.hud.teamview.TeamDetectionMode;
import pl.zombek.client.core.modules.hud.teamview.TeamTracker;
import pl.zombek.client.core.modules.hud.teamview.TeammateData;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TeamTrackerTest {

    private TeamTracker tracker;

    @BeforeEach
    public void setUp() {
        tracker = TeamTracker.getInstance();
        tracker.clear();
        tracker.clearManualTeam();
    }

    @Test
    public void testManualTeamManagement() {
        tracker.addManualMember("Player1");
        tracker.addManualMember("Player2");

        assertTrue(tracker.getManualMembers().contains("player1"));
        assertTrue(tracker.getManualMembers().contains("player2"));
        assertFalse(tracker.getManualMembers().contains("player3"));

        tracker.removeManualMember("Player1");
        assertFalse(tracker.getManualMembers().contains("player1"));

        tracker.clearManualTeam();
        assertTrue(tracker.getManualMembers().isEmpty());
    }

    @Test
    public void testDetectionModes() {
        tracker.addManualMember("TeammateA");

        UUID idA = UUID.randomUUID();
        UUID idB = UUID.randomUUID();

        // 1. Manual team mode
        assertTrue(tracker.isTeammate(idA, "TeammateA", null, null, TeamDetectionMode.MANUAL_TEAM));
        assertFalse(tracker.isTeammate(idB, "EnemyB", null, null, TeamDetectionMode.MANUAL_TEAM));

        // 2. Vanilla team mode
        assertTrue(tracker.isTeammate(idB, "EnemyB", "RedTeam", "RedTeam", TeamDetectionMode.VANILLA_TEAMS));
        assertFalse(tracker.isTeammate(idB, "EnemyB", "BlueTeam", "RedTeam", TeamDetectionMode.VANILLA_TEAMS));

        // 3. Both mode
        assertTrue(tracker.isTeammate(idA, "TeammateA", "BlueTeam", "RedTeam", TeamDetectionMode.BOTH));
        assertTrue(tracker.isTeammate(idB, "EnemyB", "RedTeam", "RedTeam", TeamDetectionMode.BOTH));
        assertFalse(tracker.isTeammate(UUID.randomUUID(), "Stranger", "Green", "RedTeam", TeamDetectionMode.BOTH));
    }

    @Test
    public void testTrackVisibleTeammates() {
        UUID id = UUID.randomUUID();
        TeammateData data = new TeammateData(id, "TeammateA", "Red", 0xFF55FF55, 10.0, 64.0, 10.0, 15.0, true);

        tracker.updateTeammate(data);
        assertEquals(1, tracker.getVisibleTeammates().size());

        tracker.removeTeammate(id);
        assertEquals(0, tracker.getVisibleTeammates().size());
    }
}
