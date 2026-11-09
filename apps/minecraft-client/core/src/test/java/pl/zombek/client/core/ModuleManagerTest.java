package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.ModuleMetadata;
import pl.zombek.client.core.modules.ModuleManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ModuleManagerTest {
    private ModuleManager manager;

    static class DummyModule extends AbstractModule {
        DummyModule(String id, String name, String desc, ModuleCategory category) {
            super(new ModuleMetadata(id, name, desc, category));
        }
    }

    @BeforeEach
    void setUp() {
        manager = ModuleManager.getInstance();
        manager.clear();
    }

    @Test
    void testRegisterAndRetrieveModule() {
        DummyModule module = new DummyModule("test_pvp", "Test PvP Module", "Enhances combat", ModuleCategory.PVP);
        manager.registerModule(module);

        assertEquals(1, manager.getModuleCount());
        assertNotNull(manager.getModuleById("test_pvp"));
        assertEquals("Test PvP Module", manager.getModuleById("test_pvp").getName());
    }

    @Test
    void testRejectDuplicateModuleId() {
        DummyModule module1 = new DummyModule("dup_mod", "Duplicate One", "First", ModuleCategory.OTHER);
        DummyModule module2 = new DummyModule("dup_mod", "Duplicate Two", "Second", ModuleCategory.HUD);

        manager.registerModule(module1);
        assertThrows(IllegalArgumentException.class, () -> manager.registerModule(module2));
        assertEquals(1, manager.getModuleCount());
    }

    @Test
    void testFilterByCategory() {
        manager.registerModule(new DummyModule("pvp_1", "Sword Mod", "PvP tool", ModuleCategory.PVP));
        manager.registerModule(new DummyModule("hud_1", "Armor HUD", "HUD tool", ModuleCategory.HUD));
        manager.registerModule(new DummyModule("hud_2", "Keystrokes", "HUD tool", ModuleCategory.HUD));
        manager.registerModule(new DummyModule("vis_1", "Fullbright", "Visual tool", ModuleCategory.VISUAL));

        assertEquals(1, manager.getModulesByCategory(ModuleCategory.PVP).size());
        assertEquals(2, manager.getModulesByCategory(ModuleCategory.HUD).size());
        assertEquals(1, manager.getModulesByCategory(ModuleCategory.VISUAL).size());
        assertEquals(0, manager.getModulesByCategory(ModuleCategory.SURVIVAL).size());
        assertEquals(0, manager.getModulesByCategory(ModuleCategory.OTHER).size());
    }

    @Test
    void testSearchModules() {
        manager.registerModule(new DummyModule("cps_mod", "Clicks Per Second", "Measures mouse speed", ModuleCategory.HUD));
        manager.registerModule(new DummyModule("sprint", "Toggle Sprint", "Keeps sprinting on", ModuleCategory.SURVIVAL));
        manager.registerModule(new DummyModule("zoom", "Optic Zoom", "Magnifies view", ModuleCategory.VISUAL));

        List<AbstractModule> resultClicks = manager.searchModules("Clicks");
        assertEquals(1, resultClicks.size());
        assertEquals("cps_mod", resultClicks.get(0).getId());

        List<AbstractModule> resultDesc = manager.searchModules("mouse speed");
        assertEquals(1, resultDesc.size());

        List<AbstractModule> resultEmpty = manager.searchModules("");
        assertEquals(3, resultEmpty.size());

        List<AbstractModule> resultNonExistent = manager.searchModules("non_existent_xyz");
        assertTrue(resultNonExistent.isEmpty());
    }
}
