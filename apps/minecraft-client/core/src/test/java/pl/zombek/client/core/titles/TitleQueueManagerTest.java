package pl.zombek.client.core.titles;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TitleQueueManagerTest {

    private TitleQueueManager queueManager;

    @BeforeEach
    public void setUp() {
        queueManager = TitleQueueManager.getInstance();
        queueManager.onClearTitles();
        queueManager.getState().clearActionBar();
        queueManager.setPreserveVanillaTiming(true);
    }

    @Test
    public void testTitleAndSubtitleLifecycle() {
        queueManager.onTitleTimes(10, 40, 10);
        queueManager.onTitle("VICTORY!");
        queueManager.onSubtitle("All beds destroyed");

        ActiveTitleState state = queueManager.getState();
        assertNotNull(state);
        assertEquals("VICTORY!", state.getTitle());
        assertEquals("All beds destroyed", state.getSubtitle());
        assertTrue(state.isTitleActive());

        // During fade in (first 5 ticks)
        for (int i = 0; i < 5; i++) {
            queueManager.onClientTick();
        }
        float alphaFadeIn = state.getTitleAlpha();
        assertTrue(alphaFadeIn > 0.0f && alphaFadeIn <= 1.0f, "Alpha should be interpolating during fade-in");

        // During stay (tick until tick 20)
        for (int i = 0; i < 15; i++) {
            queueManager.onClientTick();
        }
        assertEquals(1.0f, state.getTitleAlpha(), 0.05f, "Alpha should be ~1.0 during stay phase");

        // After stay and fade out (tick another 50 ticks)
        for (int i = 0; i < 50; i++) {
            queueManager.onClientTick();
        }
        assertFalse(state.isTitleActive(), "Title should no longer be active after full duration");
    }

    @Test
    public void testIndependentActionBarLifecycle() {
        queueManager.onTitle("Title Only");
        queueManager.onActionBar("Defend your bed!");

        ActiveTitleState state = queueManager.getState();
        assertTrue(state.isTitleActive());
        assertTrue(state.isActionBarActive());

        // Clear titles, action bar must remain active!
        queueManager.onClearTitles();
        assertFalse(state.isTitleActive(), "Title should be cleared");
        assertTrue(state.isActionBarActive(), "Action Bar must remain active independently");

        // Tick down action bar
        for (int i = 0; i < 90; i++) {
            queueManager.onClientTick();
        }
        assertFalse(state.isActionBarActive(), "Action bar should expire after its own timer");
    }

    @Test
    public void testClientOverrideTimings() {
        queueManager.setPreserveVanillaTiming(false);
        queueManager.setOverrideFadeInTicks(5);
        queueManager.setOverrideStayTicks(100);
        queueManager.setOverrideFadeOutTicks(10);

        queueManager.onTitle("Long Stay Title");

        ActiveTitleState state = queueManager.getState();
        // Tick 20 ticks
        for (int i = 0; i < 20; i++) {
            queueManager.onClientTick();
        }

        assertTrue(state.isTitleActive(), "Title should remain active due to client override stay = 100");
    }
}
