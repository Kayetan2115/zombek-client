package pl.zombek.client.core.bedwars;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.TestHudRenderer;
import pl.zombek.client.core.modules.hud.BedwarsHelperModule;
import pl.zombek.client.core.modules.hud.teamview.TeamViewModule;
import pl.zombek.client.core.modules.pvp.PvPInfoModule;
import pl.zombek.client.core.modules.survival.UHCSessionHudModule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BedwarsIntegrationTest {
    private BedwarsManager manager;

    @BeforeEach
    void setUp() {
        manager = BedwarsManager.getInstance();
        manager.reset();
    }

    @Test
    void testEndToEndMatchFlow() {
        // 1. Lobby & Waiting
        manager.onScoreboardUpdate(Arrays.asList(
                "BED WARS",
                "Waiting for players... (8/16)",
                "Map: Speedway",
                "Mode: 4v4v4v4"
        ));
        assertEquals(BedwarsGameState.WAITING, manager.getGameDetector().getCurrentState());
        assertEquals("Speedway", manager.getGameDetector().getDetectedMap());
        assertEquals(BedwarsMode.FOUR_V_FOUR_V_FOUR_V_FOUR, manager.getGameDetector().getDetectedMode());

        // 2. Game Start
        manager.onChatMessage("The game starts in 5 seconds!");
        manager.onScoreboardUpdate(Arrays.asList(
                "BED WARS",
                "Diamond II in 01:30",
                "R Red: ✓ (you)",
                "B Blue: ✓",
                "G Green: ✓",
                "Y Yellow: ✓"
        ));
        assertEquals(BedwarsGameState.BEDWARS_ACTIVE, manager.getGameDetector().getCurrentState());
        assertEquals(BedwarsTeamColor.RED, manager.getScoreboardParser().getPlayerTeamColor());

        // 3. Red destroys Blue Bed
        BedwarsDataProviderRegistry.setProvider(new IBedwarsDataProvider() {
            @Override public boolean isPlayerInGame() { return true; }
            @Override public String getPlayerName() { return "Steve"; }
            @Override public String getPlayerUuid() { return "uuid-steve"; }
            @Override public List<String> getTabListPlayerNames() { return Arrays.asList("Steve", "Alex", "Enemy1"); }
            @Override public BedwarsTeamColor getPlayerTeamColor() { return BedwarsTeamColor.RED; }
        });

        manager.onChatMessage("BED DESTRUCTION > Blue Bed was destroyed by Steve!");
        assertEquals(BedStatus.BED_DESTROYED, manager.getScoreboardParser().getParsedTeams().get(BedwarsTeamColor.BLUE).getBedStatus());
        assertEquals(1, manager.getSessionTracker().getConfirmedBedsBroken());

        // 4. Steve final kills Enemy1
        manager.onChatMessage("Enemy1 was knocked into the void by Steve. FINAL KILL!");
        assertEquals(1, manager.getSessionTracker().getConfirmedFinalKills());

        // 5. Victory
        manager.onChatMessage("VICTORY!");
        assertEquals(BedwarsGameState.GAME_ENDING, manager.getGameDetector().getCurrentState());
        assertEquals(1, manager.getSessionTracker().getConfirmedWins());
    }

    @Test
    void testCoexistenceWithOtherModules() {
        BedwarsHelperModule bedwars = new BedwarsHelperModule();
        UHCSessionHudModule uhc = new UHCSessionHudModule();
        TeamViewModule teamView = new TeamViewModule();
        PvPInfoModule pvpInfo = new PvPInfoModule();

        bedwars.setEnabled(true);
        uhc.setEnabled(true);
        teamView.setEnabled(true);
        pvpInfo.setEnabled(true);

        TestHudRenderer renderer = new TestHudRenderer(1920, 1080);

        // Rendering all modules together in the pipeline should succeed without collision
        assertDoesNotThrow(() -> {
            bedwars.render(renderer);
            uhc.render(renderer);
            pvpInfo.render(renderer);
        });
    }

    @Test
    void testTotalAlivePlayersCalculation() {
        manager.onScoreboardUpdate(Arrays.asList(
                "BED WARS",
                "R Red: 3",
                "B Blue: 2",
                "G Green: 0",
                "Y Yellow: [E]"
        ));

        // Red (3) + Blue (2) = 5 alive
        assertEquals(5, manager.getTotalAlivePlayers());
    }
}
