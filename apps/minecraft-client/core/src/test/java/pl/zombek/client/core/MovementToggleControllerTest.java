package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.movement.MovementIndicatorStyle;
import pl.zombek.client.core.movement.MovementToggleController;
import pl.zombek.client.core.movement.SneakActivationMode;
import pl.zombek.client.core.movement.SprintActivationMode;

import static org.junit.jupiter.api.Assertions.*;

public class MovementToggleControllerTest {

    private MovementToggleController controller;

    @BeforeEach
    public void setUp() {
        controller = MovementToggleController.getInstance();
        controller.resetAll();
        controller.updateGameConditions(true, true, true, false, false, false);
    }

    @Test
    public void testSprintToggleMode() {
        assertFalse(controller.isSprintToggled());

        // Press down once -> toggled on
        controller.onSprintKeyInput(true, SprintActivationMode.TOGGLE);
        assertTrue(controller.isSprintToggled());
        assertTrue(controller.isSprintActive(true, SprintActivationMode.TOGGLE, false, true, true, true));

        // Release key -> still toggled on
        controller.onSprintKeyInput(false, SprintActivationMode.TOGGLE);
        assertTrue(controller.isSprintToggled());
        assertTrue(controller.isSprintActive(true, SprintActivationMode.TOGGLE, false, true, true, true));

        // Press down again -> toggled off
        controller.onSprintKeyInput(true, SprintActivationMode.TOGGLE);
        assertFalse(controller.isSprintToggled());
        assertFalse(controller.isSprintActive(true, SprintActivationMode.TOGGLE, false, true, true, true));
    }

    @Test
    public void testSprintHoldMode() {
        assertFalse(controller.isSprintKeyHeld());

        // Press down -> active
        controller.onSprintKeyInput(true, SprintActivationMode.HOLD);
        assertTrue(controller.isSprintKeyHeld());
        assertTrue(controller.isSprintActive(true, SprintActivationMode.HOLD, false, true, true, true));

        // Release -> inactive
        controller.onSprintKeyInput(false, SprintActivationMode.HOLD);
        assertFalse(controller.isSprintKeyHeld());
        assertFalse(controller.isSprintActive(true, SprintActivationMode.HOLD, false, true, true, true));
    }

    @Test
    public void testAlwaysSprint() {
        // Even when not toggled or held, alwaysSprint activates when moving forward and allowed
        assertFalse(controller.isSprintToggled());
        assertTrue(controller.isSprintActive(true, SprintActivationMode.TOGGLE, true, true, true, true));

        // If not moving forward, sprint should not be active
        controller.updateGameConditions(false, true, true, false, false, false);
        assertFalse(controller.isSprintActive(true, SprintActivationMode.TOGGLE, true, true, true, true));
    }

    @Test
    public void testSprintGameConditions() {
        controller.onSprintKeyInput(true, SprintActivationMode.TOGGLE);
        controller.onSprintKeyInput(false, SprintActivationMode.TOGGLE);
        assertTrue(controller.isSprintToggled());

        // Cannot sprint (e.g. hunger <= 6, blindness)
        controller.updateGameConditions(true, false, true, false, false, false);
        assertFalse(controller.isSprintActive(true, SprintActivationMode.TOGGLE, false, true, true, true));

        // Using item when disableWhileUsingItems is true
        controller.updateGameConditions(true, true, true, true, false, false);
        assertFalse(controller.isSprintActive(true, SprintActivationMode.TOGGLE, false, true, true, true));
        // Allowed when disableWhileUsingItems is false
        assertTrue(controller.isSprintActive(true, SprintActivationMode.TOGGLE, false, true, true, false));
    }

    @Test
    public void testSneakToggleAndHoldModes() {
        // Toggle mode
        controller.onSneakKeyInput(true, SneakActivationMode.TOGGLE);
        assertTrue(controller.isSneakToggled());
        assertTrue(controller.isSneakActive(true, SneakActivationMode.TOGGLE, true, true, true));

        controller.onSneakKeyInput(false, SneakActivationMode.TOGGLE);
        assertTrue(controller.isSneakToggled());

        controller.onSneakKeyInput(true, SneakActivationMode.TOGGLE);
        assertFalse(controller.isSneakToggled());

        // Hold mode
        controller.onSneakKeyInput(true, SneakActivationMode.HOLD);
        assertTrue(controller.isSneakKeyHeld());
        assertTrue(controller.isSneakActive(true, SneakActivationMode.HOLD, true, true, true));

        controller.onSneakKeyInput(false, SneakActivationMode.HOLD);
        assertFalse(controller.isSneakKeyHeld());
        assertFalse(controller.isSneakActive(true, SneakActivationMode.HOLD, true, true, true));

        // Toggle and Hold mode
        controller.onSneakKeyInput(true, SneakActivationMode.TOGGLE_AND_HOLD);
        assertTrue(controller.isSneakToggled());
        assertTrue(controller.isSneakKeyHeld());
        assertTrue(controller.isSneakActive(true, SneakActivationMode.TOGGLE_AND_HOLD, true, true, true));
    }

    @Test
    public void testSneakConditionsAndSafetyResets() {
        controller.onSneakKeyInput(true, SneakActivationMode.TOGGLE);
        assertTrue(controller.isSneakActive(true, SneakActivationMode.TOGGLE, true, true, true));

        // Flying disables sneak if configured
        controller.updateGameConditions(true, true, true, false, true, false);
        assertFalse(controller.isSneakActive(true, SneakActivationMode.TOGGLE, true, true, true));

        // Riding disables sneak if configured
        controller.updateGameConditions(true, true, true, false, false, true);
        assertFalse(controller.isSneakActive(true, SneakActivationMode.TOGGLE, true, true, true));

        // Safety resets
        controller.updateGameConditions(true, true, true, false, false, false);
        assertTrue(controller.isSneakActive(true, SneakActivationMode.TOGGLE, true, true, true));

        controller.onDeathOrRespawn();
        assertFalse(controller.isSneakToggled());
        assertFalse(controller.isSprintToggled());

        controller.onSprintKeyInput(true, SprintActivationMode.TOGGLE);
        assertTrue(controller.isSprintToggled());
        controller.onWorldChange();
        assertFalse(controller.isSprintToggled());

        controller.onSprintKeyInput(true, SprintActivationMode.HOLD);
        assertTrue(controller.isSprintKeyHeld());
        controller.onFocusLost();
        assertFalse(controller.isSprintKeyHeld());
    }

    @Test
    public void testIndicatorStatusFormatting() {
        controller.onSprintKeyInput(true, SprintActivationMode.TOGGLE);

        String bracketed = controller.getSprintStatusText(MovementIndicatorStyle.BRACKETED, true, SprintActivationMode.TOGGLE, false);
        assertEquals("[Sprint (Toggled)]", bracketed);

        String compact = controller.getSprintStatusText(MovementIndicatorStyle.COMPACT, true, SprintActivationMode.TOGGLE, false);
        assertEquals("Sprint", compact);

        String detailed = controller.getSprintStatusText(MovementIndicatorStyle.DETAILED, true, SprintActivationMode.TOGGLE, false);
        assertEquals("Sprint: Toggled", detailed);

        String textOnly = controller.getSprintStatusText(MovementIndicatorStyle.TEXT_ONLY, true, SprintActivationMode.TOGGLE, false);
        assertEquals("SPRINT", textOnly);

        controller.onSneakKeyInput(true, SneakActivationMode.TOGGLE);
        String sneakBracketed = controller.getSneakStatusText(MovementIndicatorStyle.BRACKETED, true, SneakActivationMode.TOGGLE);
        assertEquals("[Sneak (Toggled)]", sneakBracketed);
    }
}
