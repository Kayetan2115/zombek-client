package pl.zombek.client.quilt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.ModuleMetadata;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.quilt.input.QuiltKeybindManager;
import pl.zombek.client.quilt.platform.QuiltPlatformAdapter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QuiltKeybindManagerTest {

    private QuiltKeybindManager manager;

    @BeforeEach
    public void setup() {
        ZombekCore.resetInstanceForTesting();
        ModuleManager.getInstance().clear();
        ZombekCore.init(new QuiltPlatformAdapter());
        manager = QuiltKeybindManager.getInstance();
        manager.setMenuKeyWasDown(false);
    }

    @AfterEach
    public void tearDown() {
        ZombekCore.resetInstanceForTesting();
        ModuleManager.getInstance().clear();
    }

    @Test
    public void testDefaultMenuKeyConstant() {
        assertEquals(GLFW.GLFW_KEY_RIGHT_SHIFT, QuiltKeybindManager.DEFAULT_OPEN_KEY,
                "ZombekClient menu default key on Quilt must be RIGHT SHIFT");
        assertNotNull(manager.getOpenMenuKeyBinding());
        assertEquals("key.zombekclient.open_menu", manager.getOpenMenuKeyBinding().getTranslationKey());
        assertEquals("key.categories.zombekclient", manager.getOpenMenuKeyBinding().getCategory());
    }

    @Test
    public void testConflictDetection() {
        AbstractModule mod1 = new AbstractModule(new ModuleMetadata("mod1", "Mod One", "Desc", ModuleCategory.PVP, "1.0", "Author")) {};
        AbstractModule mod2 = new AbstractModule(new ModuleMetadata("mod2", "Mod Two", "Desc", ModuleCategory.PVP, "1.0", "Author")) {};

        mod1.setKeybind(GLFW.GLFW_KEY_G);
        mod2.setKeybind(GLFW.GLFW_KEY_G);

        ZombekCore.getInstance().getModuleManager().registerModule(mod1);
        ZombekCore.getInstance().getModuleManager().registerModule(mod2);

        assertTrue(manager.hasConflict(GLFW.GLFW_KEY_G), "Key G should be detected as conflicting");
        List<AbstractModule> conflicts = manager.getConflictingModules(GLFW.GLFW_KEY_G);
        assertEquals(2, conflicts.size());

        assertFalse(manager.hasConflict(GLFW.GLFW_KEY_H), "Key H should have no conflict");
    }
}
