package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.visual.FreelookActivationMode;
import pl.zombek.client.core.modules.visual.FreelookCameraController;
import pl.zombek.client.core.modules.visual.FreelookModule;

import static org.junit.jupiter.api.Assertions.*;

public class FreelookModuleTest {
    private FreelookModule module;

    @BeforeEach
    void setUp() {
        module = new FreelookModule();
        FreelookCameraController.getInstance().reset();
    }

    @Test
    void testModuleMetadata() {
        assertEquals("freelook", module.getId());
        assertEquals("Freelook", module.getName());
        assertEquals(ModuleCategory.VISUAL, module.getCategory());
        assertFalse(module.isEnabled());
    }

    @Test
    void testSettingGroups() {
        assertNotNull(module.getGroup("general"));
        assertNotNull(module.getGroup("camera"));
        assertNotNull(module.getGroup("animation"));
        assertNotNull(module.getGroup("advanced"));

        assertEquals(FreelookActivationMode.HOLD, module.getActivationModeSetting().getValue());
        assertTrue(module.getSnapbackOnReleaseSetting().getValue());
        assertEquals(1.0, module.getHSensitivitySetting().getValue(), 0.01);
        assertEquals(90.0, module.getMaxPitchSetting().getValue(), 0.01);
    }

    @Test
    void testHoldModeKeyHandling() {
        module.setEnabled(true);
        module.getActivationModeSetting().setValue(FreelookActivationMode.HOLD);
        FreelookCameraController controller = FreelookCameraController.getInstance();

        // Key down -> starts freelook
        module.handleKeyInput(true, 180.0f, 10.0f);
        assertTrue(controller.isActive());
        assertEquals(180.0f, controller.getOriginalYaw(), 0.01f);

        // Key up -> stops freelook
        module.handleKeyInput(false, 180.0f, 10.0f);
        assertFalse(controller.isActive());
    }

    @Test
    void testToggleModeKeyHandling() {
        module.setEnabled(true);
        module.getActivationModeSetting().setValue(FreelookActivationMode.TOGGLE);
        FreelookCameraController controller = FreelookCameraController.getInstance();

        // First press -> starts freelook
        module.handleKeyInput(true, 90.0f, 0.0f);
        assertTrue(controller.isActive());

        // Release in toggle does nothing
        module.handleKeyInput(false, 90.0f, 0.0f);
        assertTrue(controller.isActive());

        // Second press -> stops freelook
        module.handleKeyInput(true, 90.0f, 0.0f);
        assertFalse(controller.isActive());
    }
}
