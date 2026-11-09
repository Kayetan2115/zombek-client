package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class HudMultiSelectionManagerTest {

    private HudMultiSelectionManager selectionManager;

    @BeforeEach
    public void setUp() {
        selectionManager = HudMultiSelectionManager.getInstance();
        selectionManager.resetForTesting();
    }

    @AfterEach
    public void tearDown() {
        selectionManager.resetForTesting();
    }

    @Test
    public void testSingleSelect() {
        selectionManager.selectSingle("fps");
        assertEquals(1, selectionManager.getSelectedCount());
        assertTrue(selectionManager.isSelected("fps"));
        assertEquals("fps", selectionManager.getPrimarySelectedId());

        selectionManager.selectSingle("ping");
        assertEquals(1, selectionManager.getSelectedCount());
        assertFalse(selectionManager.isSelected("fps"));
        assertTrue(selectionManager.isSelected("ping"));
    }

    @Test
    public void testToggleSelection() {
        selectionManager.toggleSelection("fps");
        assertTrue(selectionManager.isSelected("fps"));

        selectionManager.toggleSelection("ping");
        assertEquals(2, selectionManager.getSelectedCount());
        assertTrue(selectionManager.isSelected("fps"));
        assertTrue(selectionManager.isSelected("ping"));

        // Toggle fps off
        selectionManager.toggleSelection("fps");
        assertEquals(1, selectionManager.getSelectedCount());
        assertFalse(selectionManager.isSelected("fps"));
        assertTrue(selectionManager.isSelected("ping"));
    }

    @Test
    public void testSelectAllAndClear() {
        selectionManager.selectAll(Arrays.asList("fps", "ping", "coordinates"));
        assertEquals(3, selectionManager.getSelectedCount());
        assertTrue(selectionManager.isSelected("fps"));
        assertTrue(selectionManager.isSelected("ping"));
        assertTrue(selectionManager.isSelected("coordinates"));

        selectionManager.clearSelection();
        assertEquals(0, selectionManager.getSelectedCount());
        assertNull(selectionManager.getPrimarySelectedId());
    }

    @Test
    public void testListenerNotification() {
        AtomicInteger count = new AtomicInteger(0);
        selectionManager.addListener(count::incrementAndGet);

        selectionManager.selectSingle("fps");
        assertEquals(1, count.get());

        selectionManager.toggleSelection("ping");
        assertEquals(2, count.get());

        selectionManager.clearSelection();
        assertEquals(3, count.get());
    }

    @Test
    public void testMarqueeSelection() {
        HudLayout layout = new HudLayout("test", "Układ");
        HudComponentState c1 = new HudComponentState("fps", 10, 10, 40, 20);
        c1.setVisible(true);
        HudComponentState c2 = new HudComponentState("ping", 60, 10, 40, 20);
        c2.setVisible(true);
        HudComponentState c3 = new HudComponentState("coordinates", 200, 200, 100, 30);
        c3.setVisible(true);

        layout.setComponentState(c1);
        layout.setComponentState(c2);
        layout.setComponentState(c3);

        // Box enclosing c1 and c2 (from 0,0 to 110,50)
        selectionManager.startMarquee(0, 0);
        assertTrue(selectionManager.isMarqueeActive());
        selectionManager.updateMarquee(110, 50);

        selectionManager.endMarquee(layout, 960, 540, false);
        assertFalse(selectionManager.isMarqueeActive());

        assertEquals(2, selectionManager.getSelectedCount());
        assertTrue(selectionManager.isSelected("fps"));
        assertTrue(selectionManager.isSelected("ping"));
        assertFalse(selectionManager.isSelected("coordinates"));
    }
}
