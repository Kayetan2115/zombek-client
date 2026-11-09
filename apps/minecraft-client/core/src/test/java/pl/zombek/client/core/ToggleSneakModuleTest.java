package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.pvp.ToggleSneakModule;
import pl.zombek.client.core.movement.MovementIndicatorStyle;
import pl.zombek.client.core.movement.MovementToggleController;
import pl.zombek.client.core.movement.SneakActivationMode;

import static org.junit.jupiter.api.Assertions.*;

public class ToggleSneakModuleTest {

    private ToggleSneakModule module;

    @BeforeEach
    public void setUp() {
        module = new ToggleSneakModule();
        MovementToggleController.getInstance().resetAll();
    }

    @Test
    public void testModuleMetadata() {
        assertEquals("toggle_sneak", module.getId());
        assertEquals("Toggle Sneak", module.getName());
        assertEquals(ModuleCategory.PVP, module.getCategory());
        assertFalse(module.isEnabled());
        assertEquals(GLFW.GLFW_KEY_C, module.getKeybind());
    }

    @Test
    public void testSettingGroupsAndDefaults() {
        assertNotNull(module.getGroup("general"));
        assertNotNull(module.getGroup("behavior"));
        assertNotNull(module.getGroup("hud"));
        assertNotNull(module.getGroup("colors"));

        assertEquals(SneakActivationMode.TOGGLE_AND_HOLD, module.getActivationModeSetting().getValue());
        assertTrue(module.getDisableWhileFlyingSetting().getValue());
        assertTrue(module.getDisableWhileRidingSetting().getValue());
        assertTrue(module.getPreserveVanillaSneakSetting().getValue());
        assertEquals(MovementIndicatorStyle.BRACKETED, module.getIndicatorStyleSetting().getValue());
    }

    @Test
    public void testHandleKeyInput() {
        module.setEnabled(true);
        // Press sneak key
        module.handleKeyInput(true);
        assertTrue(MovementToggleController.getInstance().isSneakToggled());

        // Release sneak key
        module.handleKeyInput(false);
        assertTrue(MovementToggleController.getInstance().isSneakToggled());

        // Press again -> off
        module.handleKeyInput(true);
        assertFalse(MovementToggleController.getInstance().isSneakToggled());
    }

    @Test
    public void testRenderWithoutException() {
        module.setEnabled(true);
        MovementToggleController.getInstance().updateGameConditions(true, true, true, false, false, false);
        MovementToggleController.getInstance().onSneakKeyInput(true, SneakActivationMode.TOGGLE);

        TestHudRenderer renderer = new TestHudRenderer(1920, 1080);
        assertDoesNotThrow(() -> module.render(renderer));
        assertTrue(renderer.drawTextCallCount > 0);
        assertTrue(renderer.lastTextDrawn.contains("Sneak"));
    }
}
