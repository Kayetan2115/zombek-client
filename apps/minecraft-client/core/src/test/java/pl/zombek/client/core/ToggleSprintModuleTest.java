package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.pvp.ToggleSprintModule;
import pl.zombek.client.core.movement.MovementIndicatorStyle;
import pl.zombek.client.core.movement.MovementToggleController;
import pl.zombek.client.core.movement.SprintActivationMode;

import static org.junit.jupiter.api.Assertions.*;

public class ToggleSprintModuleTest {

    private ToggleSprintModule module;

    @BeforeEach
    public void setUp() {
        module = new ToggleSprintModule();
        MovementToggleController.getInstance().resetAll();
    }

    @Test
    public void testModuleMetadata() {
        assertEquals("toggle_sprint", module.getId());
        assertEquals("Toggle Sprint", module.getName());
        assertEquals(ModuleCategory.PVP, module.getCategory());
        assertFalse(module.isEnabled());
        assertEquals(GLFW.GLFW_KEY_LEFT_ALT, module.getKeybind());
    }

    @Test
    public void testSettingGroupsAndDefaults() {
        assertNotNull(module.getGroup("general"));
        assertNotNull(module.getGroup("behavior"));
        assertNotNull(module.getGroup("hud"));
        assertNotNull(module.getGroup("appearance"));

        assertEquals(SprintActivationMode.TOGGLE, module.getActivationModeSetting().getValue());
        assertFalse(module.getAlwaysSprintSetting().getValue());
        assertTrue(module.getDisableWhileSneakingSetting().getValue());
        assertFalse(module.getDisableWhileUsingItemsSetting().getValue());
        assertTrue(module.getPreserveVanillaSprintSetting().getValue());
        assertEquals(MovementIndicatorStyle.BRACKETED, module.getIndicatorStyleSetting().getValue());
    }

    @Test
    public void testHandleKeyInput() {
        module.setEnabled(true);
        // Press sprint key
        module.handleKeyInput(true);
        assertTrue(MovementToggleController.getInstance().isSprintToggled());

        // Release sprint key
        module.handleKeyInput(false);
        assertTrue(MovementToggleController.getInstance().isSprintToggled());

        // Press again -> off
        module.handleKeyInput(true);
        assertFalse(MovementToggleController.getInstance().isSprintToggled());
    }

    @Test
    public void testRenderWithoutException() {
        module.setEnabled(true);
        MovementToggleController.getInstance().updateGameConditions(true, true, true, false, false, false);
        MovementToggleController.getInstance().onSprintKeyInput(true, SprintActivationMode.TOGGLE);

        TestHudRenderer renderer = new TestHudRenderer(1920, 1080);
        assertDoesNotThrow(() -> module.render(renderer));
        assertTrue(renderer.drawTextCallCount > 0);
        assertTrue(renderer.lastTextDrawn.contains("Sprint"));
    }
}
