package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.pvp.snaplook.SnaplookActivationMode;
import pl.zombek.client.core.modules.pvp.snaplook.SnaplookModule;
import pl.zombek.client.core.modules.pvp.snaplook.SnaplookPerspective;

import static org.junit.jupiter.api.Assertions.*;

public class SnaplookModuleTest {

    private SnaplookModule module;

    @BeforeEach
    public void setUp() {
        module = new SnaplookModule();
    }

    @Test
    public void testModuleMetadata() {
        assertEquals("snaplook", module.getId());
        assertEquals("Snaplook", module.getName());
        assertEquals(ModuleCategory.PVP, module.getCategory());
        assertEquals(GLFW.GLFW_KEY_V, module.getKeybind());
        assertFalse(module.isEnabled());
    }

    @Test
    public void testSettingGroupsAndDefaults() {
        assertNotNull(module.getGroup("general"));
        assertNotNull(module.getGroup("camera"));
        assertNotNull(module.getGroup("behavior"));

        assertEquals(SnaplookActivationMode.HOLD, module.getActivationModeSetting().getValue());
        assertEquals(SnaplookPerspective.THIRD_PERSON_BACK, module.getTargetPerspectiveSetting().getValue());
        assertTrue(module.getRestorePreviousPerspectiveSetting().getValue());
        assertTrue(module.getRespectManualChangesSetting().getValue());
        assertTrue(module.getResetOnWorldChangeSetting().getValue());
    }
}
