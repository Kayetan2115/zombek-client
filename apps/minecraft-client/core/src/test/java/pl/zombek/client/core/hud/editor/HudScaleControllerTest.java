package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.hud.HudAnchor;

import static org.junit.jupiter.api.Assertions.*;

class HudScaleControllerTest {

    private HudComponentDescriptor scalableDesc;
    private HudComponentDescriptor fixedDesc;

    @BeforeEach
    void setUp() {
        scalableDesc = HudComponentDescriptor.builder("fps", "FPS")
                .defaultScale(1.0)
                .scaleRange(0.5, 2.5)
                .capabilities(HudComponentCapability.SCALE)
                .build();

        fixedDesc = HudComponentDescriptor.builder("fixed", "Fixed")
                .defaultScale(1.0)
                .capabilities(HudComponentCapability.POSITION)
                .build();
    }

    @Test
    void testScaleClampingAndRounding() {
        HudComponentState state = scalableDesc.createDefaultState();

        assertTrue(HudScaleController.setScale(state, scalableDesc, 1.3456));
        assertEquals(1.35, state.getScale(), 0.001);

        // Clamp to min scale (0.5)
        HudScaleController.setScale(state, scalableDesc, 0.1);
        assertEquals(0.5, state.getScale(), 0.001);

        // Clamp to max scale (2.5)
        HudScaleController.setScale(state, scalableDesc, 5.0);
        assertEquals(2.5, state.getScale(), 0.001);
    }

    @Test
    void testStepScale() {
        HudComponentState state = scalableDesc.createDefaultState();
        assertEquals(1.0, state.getScale(), 0.001);

        HudScaleController.stepScale(state, scalableDesc, 0.05);
        assertEquals(1.05, state.getScale(), 0.001);

        HudScaleController.stepScale(state, scalableDesc, -0.10);
        assertEquals(0.95, state.getScale(), 0.001);
    }

    @Test
    void testResetScale() {
        HudComponentState state = scalableDesc.createDefaultState();
        HudScaleController.setScale(state, scalableDesc, 2.0);
        assertEquals(2.0, state.getScale(), 0.001);

        HudScaleController.resetScale(state, scalableDesc);
        assertEquals(1.0, state.getScale(), 0.001);
    }

    @Test
    void testLockedOrUncapableRejectsScale() {
        HudComponentState lockedState = scalableDesc.createDefaultState();
        lockedState.setLocked(true);
        assertFalse(HudScaleController.setScale(lockedState, scalableDesc, 1.5));
        assertEquals(1.0, lockedState.getScale(), 0.001);

        HudComponentState uncapableState = fixedDesc.createDefaultState();
        assertFalse(HudScaleController.setScale(uncapableState, fixedDesc, 1.5));
        assertEquals(1.0, uncapableState.getScale(), 0.001);
    }
}
