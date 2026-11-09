package pl.zombek.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;
import pl.zombek.client.keybind.KeybindManager;
import pl.zombek.client.modules.AbstractModule;
import pl.zombek.client.modules.ModuleCategory;
import pl.zombek.client.modules.ModuleMetadata;
import pl.zombek.client.modules.ModuleManager;

import static org.junit.jupiter.api.Assertions.*;

public class KeybindManagerTest {
    private ModuleManager moduleManager;
    private KeybindManager keybindManager;

    static class DummyKeybindModule extends AbstractModule {
        DummyKeybindModule(String id, int key) {
            super(new ModuleMetadata(id, id, "desc", ModuleCategory.OTHER), key);
        }
    }

    @BeforeEach
    void setUp() {
        moduleManager = ModuleManager.getInstance();
        moduleManager.clear();
        keybindManager = KeybindManager.getInstance();
    }

    @Test
    void testDefaultMenuKeyConstant() {
        assertEquals(GLFW.GLFW_KEY_RIGHT_SHIFT, KeybindManager.DEFAULT_MENU_KEY);
    }

    @Test
    void testConflictDetection() {
        DummyKeybindModule modA = new DummyKeybindModule("mod_a", GLFW.GLFW_KEY_K);
        DummyKeybindModule modB = new DummyKeybindModule("mod_b", GLFW.GLFW_KEY_K);
        DummyKeybindModule modC = new DummyKeybindModule("mod_c", GLFW.GLFW_KEY_L);

        moduleManager.registerModule(modA);
        moduleManager.registerModule(modB);
        moduleManager.registerModule(modC);

        assertTrue(keybindManager.hasConflict(GLFW.GLFW_KEY_K));
        assertEquals(2, keybindManager.getConflictingModules(GLFW.GLFW_KEY_K).size());

        assertFalse(keybindManager.hasConflict(GLFW.GLFW_KEY_L));
        assertEquals(1, keybindManager.getConflictingModules(GLFW.GLFW_KEY_L).size());

        assertFalse(keybindManager.hasConflict(pl.zombek.client.settings.KeybindSetting.KEY_NONE));
    }
}
