package pl.zombek.client.neoforge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.ModuleMetadata;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.settings.KeybindSetting;
import pl.zombek.client.neoforge.input.NeoForgeKeybindManager;

import static org.junit.jupiter.api.Assertions.*;

public class NeoForgeKeybindManagerTest {

    static class DummyNeoForgeModule extends AbstractModule {
        DummyNeoForgeModule(String id, int key) {
            super(new ModuleMetadata(id, id, "desc", ModuleCategory.OTHER), key);
        }
    }

    @BeforeEach
    void setUp() {
        ModuleManager.getInstance().clear();
    }

    @Test
    void testDefaultMenuKey() {
        assertEquals(GLFW.GLFW_KEY_RIGHT_SHIFT, NeoForgeKeybindManager.DEFAULT_MENU_KEY);
    }

    @Test
    void testConflictDetectionOnNeoForge() {
        NeoForgeKeybindManager manager = NeoForgeKeybindManager.getInstance();

        DummyNeoForgeModule mod1 = new DummyNeoForgeModule("mod1", GLFW.GLFW_KEY_K);
        DummyNeoForgeModule mod2 = new DummyNeoForgeModule("mod2", GLFW.GLFW_KEY_K);
        DummyNeoForgeModule mod3 = new DummyNeoForgeModule("mod3", GLFW.GLFW_KEY_L);

        ModuleManager.getInstance().registerModule(mod1);
        ModuleManager.getInstance().registerModule(mod2);
        ModuleManager.getInstance().registerModule(mod3);

        assertTrue(manager.hasConflict(GLFW.GLFW_KEY_K));
        assertEquals(2, manager.getConflictingModules(GLFW.GLFW_KEY_K).size());

        assertFalse(manager.hasConflict(GLFW.GLFW_KEY_L));
        assertEquals(1, manager.getConflictingModules(GLFW.GLFW_KEY_L).size());

        assertFalse(manager.hasConflict(KeybindSetting.KEY_NONE));
        assertEquals(0, manager.getConflictingModules(KeybindSetting.KEY_NONE).size());
    }
}
