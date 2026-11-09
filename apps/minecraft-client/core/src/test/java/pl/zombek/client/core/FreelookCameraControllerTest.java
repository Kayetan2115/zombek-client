package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.visual.FreelookActivationMode;
import pl.zombek.client.core.modules.visual.FreelookCameraController;
import pl.zombek.client.core.modules.visual.FreelookInterpolation;

import static org.junit.jupiter.api.Assertions.*;

public class FreelookCameraControllerTest {
    private FreelookCameraController controller;

    @BeforeEach
    void setUp() {
        controller = FreelookCameraController.getInstance();
        controller.reset();
    }

    @Test
    void testFreelookStartAndDecoupling() {
        assertFalse(controller.isActive());
        assertFalse(controller.isControllingCamera());

        controller.startFreelook(120.0f, 15.0f);

        assertTrue(controller.isActive());
        assertTrue(controller.isControllingCamera());
        assertEquals(120.0f, controller.getOriginalYaw(), 0.01f);
        assertEquals(15.0f, controller.getOriginalPitch(), 0.01f);
        assertEquals(120.0f, controller.getCameraYaw(), 0.01f);
        assertEquals(15.0f, controller.getCameraPitch(), 0.01f);
    }

    @Test
    void testMouseInputHandlingAndClamping() {
        controller.startFreelook(0.0f, 0.0f);

        // Turn right by 30 and down by 20
        controller.handleMouseInput(30.0f, 20.0f, 1.0f, 1.0f, false, false, 90.0f, 0.0f);
        assertEquals(30.0f, controller.getCameraYaw(), 0.01f);
        assertEquals(20.0f, controller.getCameraPitch(), 0.01f);
        assertEquals(0.0f, controller.getOriginalYaw(), 0.01f); // Body angle untouched

        // Pitch clamping at +90 and -90
        controller.handleMouseInput(0.0f, 100.0f, 1.0f, 1.0f, false, false, 90.0f, 0.0f);
        assertEquals(90.0f, controller.getCameraPitch(), 0.01f);

        controller.handleMouseInput(0.0f, -250.0f, 1.0f, 1.0f, false, false, 90.0f, 0.0f);
        assertEquals(-90.0f, controller.getCameraPitch(), 0.01f);
    }

    @Test
    void testInvertAndSensitivity() {
        controller.startFreelook(0.0f, 0.0f);

        // Inverted horizontal and 2x vertical sensitivity
        controller.handleMouseInput(10.0f, 10.0f, 1.0f, 2.0f, true, false, 90.0f, 0.0f);
        assertEquals(-10.0f, controller.getCameraYaw(), 0.01f);
        assertEquals(20.0f, controller.getCameraPitch(), 0.01f);
    }

    @Test
    void testSnapbackTransition() {
        controller.startFreelook(0.0f, 0.0f);
        controller.handleMouseInput(60.0f, 30.0f, 1.0f, 1.0f, false, false, 90.0f, 0.0f);

        // Stop freelook with snapback (100ms)
        controller.stopFreelook(true, 100L, FreelookInterpolation.LINEAR, 0.0f, 0.0f);

        assertFalse(controller.isActive());
        assertTrue(controller.isInSnapback());
        assertTrue(controller.isControllingCamera());

        // Update snapback halfway (50ms)
        long startTime = System.currentTimeMillis();
        controller.updateSnapback(startTime + 50L, 0.0f, 0.0f);
        assertTrue(controller.getCameraYaw() < 60.0f);
        assertTrue(controller.getCameraPitch() < 30.0f);

        // Update snapback finish (150ms)
        controller.updateSnapback(startTime + 150L, 0.0f, 0.0f);
        assertFalse(controller.isInSnapback());
        assertFalse(controller.isControllingCamera());
        assertEquals(0.0f, controller.getCameraYaw(), 0.01f);
        assertEquals(0.0f, controller.getCameraPitch(), 0.01f);
    }

    @Test
    void testReset() {
        controller.startFreelook(50.0f, 10.0f);
        assertTrue(controller.isActive());

        controller.reset();
        assertFalse(controller.isActive());
        assertFalse(controller.isInSnapback());
        assertFalse(controller.isControllingCamera());
    }
}
