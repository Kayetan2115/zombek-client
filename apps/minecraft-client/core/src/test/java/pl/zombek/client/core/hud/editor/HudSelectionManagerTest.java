package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.hud.HudAnchor;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class HudSelectionManagerTest {

    private HudSelectionManager selectionManager;

    @BeforeEach
    void setUp() {
        selectionManager = HudSelectionManager.getInstance();
        selectionManager.resetForTesting();
    }

    @Test
    void testBasicSelectionAndListeners() {
        AtomicReference<String> selected = new AtomicReference<>(null);
        selectionManager.addSelectionListener(selected::set);

        assertNull(selectionManager.getSelectedComponentId());
        assertFalse(selectionManager.hasSelection());

        selectionManager.select("fps");
        assertEquals("fps", selectionManager.getSelectedComponentId());
        assertEquals("fps", selected.get());
        assertTrue(selectionManager.isSelected("FPS"));

        selectionManager.clearSelection();
        assertNull(selectionManager.getSelectedComponentId());
        assertNull(selected.get());
    }

    @Test
    void testCanvasHitDisambiguationCycling() {
        HudLayout layout = new HudLayout("test", "Test Layout");

        // Element A at (10, 10, width: 50, height: 50), anchor TOP_LEFT, zIndex 0
        HudComponentState compA = new HudComponentState("comp_a");
        compA.setPosX(10);
        compA.setPosY(10);
        compA.setWidth(50);
        compA.setHeight(50);
        compA.setAnchor(HudAnchor.TOP_LEFT);
        compA.setZIndex(0);
        layout.setComponentState(compA);

        // Element B at (20, 20, width: 50, height: 50), anchor TOP_LEFT, zIndex 1 (topmost)
        HudComponentState compB = new HudComponentState("comp_b");
        compB.setPosX(20);
        compB.setPosY(20);
        compB.setWidth(50);
        compB.setHeight(50);
        compB.setAnchor(HudAnchor.TOP_LEFT);
        compB.setZIndex(1);
        layout.setComponentState(compB);

        int screenW = 800;
        int screenH = 600;

        // Click on (30, 30), which is inside BOTH compA (10..60, 10..60) and compB (20..70, 20..70)
        // First click: should select topmost (comp_b, highest z-index)
        boolean hit1 = selectionManager.selectAt(30, 30, layout, screenW, screenH);
        assertTrue(hit1);
        assertEquals("comp_b", selectionManager.getSelectedComponentId());

        // Second click on overlapping area: should cycle to comp_a!
        boolean hit2 = selectionManager.selectAt(30, 30, layout, screenW, screenH);
        assertTrue(hit2);
        assertEquals("comp_a", selectionManager.getSelectedComponentId());

        // Third click on overlapping area: should cycle back to comp_b!
        boolean hit3 = selectionManager.selectAt(30, 30, layout, screenW, screenH);
        assertTrue(hit3);
        assertEquals("comp_b", selectionManager.getSelectedComponentId());

        // Click on empty space (200, 200) -> should clear selection
        boolean hitEmpty = selectionManager.selectAt(200, 200, layout, screenW, screenH);
        assertFalse(hitEmpty);
        assertNull(selectionManager.getSelectedComponentId());
    }
}
