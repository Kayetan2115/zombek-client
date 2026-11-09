package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.hud.HudAnchor;

import static org.junit.jupiter.api.Assertions.*;

class HudAlignmentManagerTest {

    private HudComponentDescriptor alignableDesc;
    private HudComponentDescriptor lockedDesc;

    @BeforeEach
    void setUp() {
        alignableDesc = HudComponentDescriptor.builder("test", "Test")
                .defaultPosition(50, 50)
                .defaultSize(100, 40)
                .defaultAnchor(HudAnchor.TOP_LEFT)
                .capabilities(HudComponentCapability.POSITION)
                .build();

        lockedDesc = HudComponentDescriptor.builder("locked", "Locked")
                .defaultPosition(50, 50)
                .defaultSize(100, 40)
                .defaultAnchor(HudAnchor.TOP_LEFT)
                .capabilities(HudComponentCapability.POSITION)
                .build();
    }

    @Test
    void testAlignLeftAndRight() {
        HudComponentState state = alignableDesc.createDefaultState();
        int virtW = 960;

        // Align Left (0)
        assertTrue(HudAlignmentManager.alignLeft(state, alignableDesc, virtW, 0));
        assertEquals(0, state.getPosX());

        // Align Left with 10px safe margin
        assertTrue(HudAlignmentManager.alignLeft(state, alignableDesc, virtW, 10));
        assertEquals(10, state.getPosX());

        // Align Right (960 - 100 = 860)
        assertTrue(HudAlignmentManager.alignRight(state, alignableDesc, virtW, 0));
        assertEquals(860, state.getPosX());

        // Align Right with 15px safe margin
        assertTrue(HudAlignmentManager.alignRight(state, alignableDesc, virtW, 15));
        assertEquals(845, state.getPosX());
    }

    @Test
    void testAlignTopAndBottom() {
        HudComponentState state = alignableDesc.createDefaultState();
        int virtH = 540;

        // Align Top (0)
        assertTrue(HudAlignmentManager.alignTop(state, alignableDesc, virtH, 0));
        assertEquals(0, state.getPosY());

        // Align Top with 10px safe margin
        assertTrue(HudAlignmentManager.alignTop(state, alignableDesc, virtH, 10));
        assertEquals(10, state.getPosY());

        // Align Bottom (540 - 40 = 500)
        assertTrue(HudAlignmentManager.alignBottom(state, alignableDesc, virtH, 0));
        assertEquals(500, state.getPosY());

        // Align Bottom with 20px safe margin
        assertTrue(HudAlignmentManager.alignBottom(state, alignableDesc, virtH, 20));
        assertEquals(480, state.getPosY());
    }

    @Test
    void testCenterHorizontallyAndVertically() {
        HudComponentState state = alignableDesc.createDefaultState();
        int virtW = 960;
        int virtH = 540;

        // Center X = (960 - 100) / 2 = 430
        assertTrue(HudAlignmentManager.centerHorizontally(state, alignableDesc, virtW));
        assertEquals(430, state.getPosX());

        // Center Y = (540 - 40) / 2 = 250
        assertTrue(HudAlignmentManager.centerVertically(state, alignableDesc, virtH));
        assertEquals(250, state.getPosY());

        // Center on Canvas
        assertTrue(HudAlignmentManager.centerOnCanvas(state, alignableDesc, virtW, virtH));
        assertEquals(430, state.getPosX());
        assertEquals(250, state.getPosY());
    }

    @Test
    void testLockedComponentCannotAlign() {
        HudComponentState state = lockedDesc.createDefaultState();
        state.setLocked(true);

        assertFalse(HudAlignmentManager.alignLeft(state, lockedDesc, 960, 0));
        assertEquals(50, state.getPosX());

        assertFalse(HudAlignmentManager.centerOnCanvas(state, lockedDesc, 960, 540));
        assertEquals(50, state.getPosX());
        assertEquals(50, state.getPosY());
    }
}
