package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.tracking.KeyTracker;

import static org.junit.jupiter.api.Assertions.*;

public class KeyTrackerTest {

    private KeyTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = KeyTracker.getInstance();
        tracker.resetAll();
    }

    @Test
    void testKeyboardStateTracking() {
        assertFalse(tracker.isKeyPressed(KeyTracker.KEY_W));
        assertFalse(tracker.isKeyPressed(KeyTracker.KEY_SPACE));

        tracker.setKeyPressed(KeyTracker.KEY_W, true);
        tracker.setKeyPressed(KeyTracker.KEY_SPACE, true);

        assertTrue(tracker.isKeyPressed(KeyTracker.KEY_W));
        assertTrue(tracker.isKeyPressed(KeyTracker.KEY_SPACE));
        assertFalse(tracker.isKeyPressed(KeyTracker.KEY_A));

        tracker.setKeyPressed(KeyTracker.KEY_W, false);
        assertFalse(tracker.isKeyPressed(KeyTracker.KEY_W));
        assertTrue(tracker.isKeyPressed(KeyTracker.KEY_SPACE));
    }

    @Test
    void testMouseButtonStateTracking() {
        assertFalse(tracker.isLeftMouseDown());
        assertFalse(tracker.isRightMouseDown());

        tracker.setMouseButtonPressed(KeyTracker.MOUSE_BUTTON_LEFT, true);
        assertTrue(tracker.isLeftMouseDown());
        assertTrue(tracker.isMouseButtonPressed(KeyTracker.MOUSE_BUTTON_LEFT));

        tracker.setMouseButtonPressed(KeyTracker.MOUSE_BUTTON_RIGHT, true);
        assertTrue(tracker.isRightMouseDown());

        tracker.setMouseButtonPressed(KeyTracker.MOUSE_BUTTON_LEFT, false);
        assertFalse(tracker.isLeftMouseDown());
        assertTrue(tracker.isRightMouseDown());
    }

    @Test
    void testFocusLostResetsAllKeys() {
        tracker.setKeyPressed(KeyTracker.KEY_W, true);
        tracker.setKeyPressed(KeyTracker.KEY_D, true);
        tracker.setMouseButtonPressed(KeyTracker.MOUSE_BUTTON_LEFT, true);

        tracker.onFocusLost();

        assertFalse(tracker.isKeyPressed(KeyTracker.KEY_W));
        assertFalse(tracker.isKeyPressed(KeyTracker.KEY_D));
        assertFalse(tracker.isLeftMouseDown());
        assertFalse(tracker.isRightMouseDown());
    }
}
