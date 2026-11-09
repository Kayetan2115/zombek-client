package pl.zombek.client.core.modules.visual.nametags;

import org.junit.jupiter.api.Test;
import pl.zombek.client.core.playerinfo.DataAvailability;
import pl.zombek.client.core.playerinfo.PlayerInfoData;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NameTagVisibilityRuleEvaluatorTest {

    private PlayerInfoData createSamplePlayer() {
        return new PlayerInfoData(
                UUID.randomUUID(), "TestPlayer", "TestPlayer", "", "", "Team", 0xFFFFFFFF,
                30, DataAvailability.AVAILABLE, "SURVIVAL", DataAvailability.AVAILABLE,
                20.0f, 20.0f, 0.0f, DataAvailability.AVAILABLE,
                Collections.emptyList(), DataAvailability.AVAILABLE, "", DataAvailability.AVAILABLE,
                15.0, DataAvailability.AVAILABLE, false, null, null
        );
    }

    @Test
    void testCategoryFiltering() {
        PlayerInfoData p = createSamplePlayer();

        // Target players disabled
        assertFalse(NameTagVisibilityRuleEvaluator.shouldRender(
                p, NameTagEntityCategory.PLAYERS,
                false, false, false, false, 10.0,
                false, true, true, true, 64.0, 0.0, true, true, true, true
        ));

        // Target players enabled
        assertTrue(NameTagVisibilityRuleEvaluator.shouldRender(
                p, NameTagEntityCategory.PLAYERS,
                false, false, false, false, 10.0,
                true, true, true, true, 64.0, 0.0, true, true, true, true
        ));
    }

    @Test
    void testFairPlaySneakingVisibility() {
        PlayerInfoData p = createSamplePlayer();

        // When player is sneaking, NameTag must NOT render if hideSneaking or respectVanillaVisibility is true
        assertFalse(NameTagVisibilityRuleEvaluator.shouldRender(
                p, NameTagEntityCategory.PLAYERS,
                true, false, false, false, 10.0,
                true, true, true, true, 64.0, 0.0, false, false, false, true
        ));

        // When respectVanillaVisibility is true, sneaking must also hide tag
        assertFalse(NameTagVisibilityRuleEvaluator.shouldRender(
                p, NameTagEntityCategory.PLAYERS,
                true, false, false, false, 10.0,
                true, true, true, true, 64.0, 0.0, false, false, false, false
        ));
    }

    @Test
    void testFairPlayInvisibilityEffect() {
        PlayerInfoData p = createSamplePlayer();

        // Invisible entities must NOT be revealed
        assertFalse(NameTagVisibilityRuleEvaluator.shouldRender(
                p, NameTagEntityCategory.PLAYERS,
                false, true, false, false, 10.0,
                true, true, true, true, 64.0, 0.0, false, false, true, false
        ));
    }

    @Test
    void testSpectatorAndGuiHiding() {
        PlayerInfoData p = createSamplePlayer();

        // Spectator hiding
        assertFalse(NameTagVisibilityRuleEvaluator.shouldRender(
                p, NameTagEntityCategory.PLAYERS,
                false, false, true, false, 10.0,
                true, true, true, true, 64.0, 0.0, false, true, false, false
        ));

        // GUI open hiding
        assertFalse(NameTagVisibilityRuleEvaluator.shouldRender(
                p, NameTagEntityCategory.PLAYERS,
                false, false, false, true, 10.0,
                true, true, true, true, 64.0, 0.0, true, false, false, false
        ));
    }

    @Test
    void testDistanceBoundaries() {
        PlayerInfoData p = createSamplePlayer();

        // Distance exceeds max
        assertFalse(NameTagVisibilityRuleEvaluator.shouldRender(
                p, NameTagEntityCategory.PLAYERS,
                false, false, false, false, 70.0,
                true, true, true, true, 64.0, 0.0, false, false, false, false
        ));

        // Distance below min
        assertFalse(NameTagVisibilityRuleEvaluator.shouldRender(
                p, NameTagEntityCategory.PLAYERS,
                false, false, false, false, 2.0,
                true, true, true, true, 64.0, 5.0, false, false, false, false
        ));

        // Valid distance
        assertTrue(NameTagVisibilityRuleEvaluator.shouldRender(
                p, NameTagEntityCategory.PLAYERS,
                false, false, false, false, 25.0,
                true, true, true, true, 64.0, 5.0, false, false, false, false
        ));
    }
}
