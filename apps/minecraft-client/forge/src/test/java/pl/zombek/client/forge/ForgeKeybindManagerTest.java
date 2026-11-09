package pl.zombek.client.forge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.ModuleMetadata;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.settings.KeybindSetting;
import pl.zombek.client.forge.input.ForgeKeybindManager;

import static org.junit.jupiter.api.Assertions.*;

public class ForgeKeybindManagerTest {

    static class DummyForgeModule extends AbstractModule {
        DummyForgeModule(String id, int key) {
            super(new ModuleMetadata(id, id, "desc", ModuleCategory.OTHER), key);
        }
    }

    @BeforeEach
    void setUp() {
        ModuleManager.getInstance().clear();
    }

    @Test
    void testDefaultMenuKey() {
        assertEquals(GLFW.GLFW_KEY_RIGHT_SHIFT, ForgeKeybindManager.DEFAULT_MENU_KEY);
    }

    @Test
    void testConflictDetectionOnForge() {
        ForgeKeybindManager manager = ForgeKeybindManager.getInstance();

        DummyForgeModule mod1 = new DummyForgeModule("mod1", GLFW.GLFW_KEY_K);
        DummyForgeModule mod2 = new DummyForgeModule("mod2", GLFW.GLFW_KEY_K);
        DummyForgeModule mod3 = new DummyForgeModule("mod3", GLFW.GLFW_KEY_L);

        ModuleManager.getInstance().registerModule(mod1);
        ModuleManager.getInstance().registerModule(mod2);
        ModuleManager.getInstance().registerModule(mod3);

        assertTrue(manager.hasConflict(GLFW.GLFW_KEY_K));
        assertEquals(2, manager.getConflictingModules(GLFW.GLFW_KEY_K).size());

        assertFalse(manager.hasConflict(GLFW.GLFW_KEY_L));
        assertEquals(1, manager.getConflictingModules(GLFW.GLFW_KEY_L).size());

        assertFalse(manager.hasConflict(KeybindSetting.KEY_NONE));
    }
}
