package pl.zombek.client.core.bedwars;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BedwarsEventTrackerTest {
    private BedwarsEventTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new BedwarsEventTracker();
        tracker.clear();
    }

    @Test
    void testAddAndRetrieveEvents() {
        BedwarsEvent ev1 = new BedwarsEvent(BedwarsEventType.BED_DESTROYED, "RED BED BROKEN", "By Alex", 0xFFFF5555);
        tracker.addEvent(ev1);

        List<BedwarsEvent> visible = tracker.getVisibleEvents();
        assertEquals(1, visible.size());
        assertEquals("RED BED BROKEN", visible.get(0).getTitle());
    }

    @Test
    void testMaxEventsTrimming() {
        tracker.setMaxEvents(3);
        for (int i = 1; i <= 6; i++) {
            BedwarsEvent ev = new BedwarsEvent("ev_" + i, BedwarsEventType.PLAYER_ELIMINATED, "Event " + i, "", 0xFFFFFFFF, System.currentTimeMillis() + i, "☠");
            tracker.addEvent(ev);
        }

        List<BedwarsEvent> visible = tracker.getVisibleEvents();
        assertEquals(3, visible.size());
        // Head is newest event
        assertEquals("Event 6", visible.get(0).getTitle());
    }

    @Test
    void testDeduplicationSuppression() {
        BedwarsEvent ev1 = new BedwarsEvent("dedup_key", BedwarsEventType.FINAL_KILL, "Final Kill", "Desc", 0xFFFF5555, System.currentTimeMillis(), "⚔");
        BedwarsEvent ev2 = new BedwarsEvent("dedup_key", BedwarsEventType.FINAL_KILL, "Final Kill", "Desc", 0xFFFF5555, System.currentTimeMillis(), "⚔");

        tracker.addEvent(ev1);
        tracker.addEvent(ev2);

        assertEquals(1, tracker.getVisibleEvents().size());
    }

    @Test
    void testFadeAlphaCalculation() {
        BedwarsEvent ev = new BedwarsEvent(BedwarsEventType.BED_DESTROYED, "Title", "Desc", 0xFFFF5555);
        // Brand new event -> alpha 1.0f
        assertEquals(1.0f, ev.getFadeAlpha(10.0f), 0.05f);
    }
}
