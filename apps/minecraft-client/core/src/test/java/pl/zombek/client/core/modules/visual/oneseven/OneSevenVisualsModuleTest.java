package pl.zombek.client.core.modules.visual.oneseven;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.ModuleCategory;

import static org.junit.jupiter.api.Assertions.*;

public class OneSevenVisualsModuleTest {

    private OneSevenVisualsModule module;

    @BeforeEach
    void setUp() {
        module = new OneSevenVisualsModule();
    }

    @Test
    void testModuleMetadata() {
        assertEquals("one_seven_visuals", module.getId());
        assertEquals("1.7 Visuals", module.getName());
        assertEquals(ModuleCategory.VISUAL, module.getCategory());
        assertFalse(module.isEnabled());
    }

    @Test
    void testSettingGroupsRegistration() {
        // Must contain all 13 required groups
        assertNotNull(module.getGroup("presets"));
        assertNotNull(module.getGroup("blocking"));
        assertNotNull(module.getGroup("blockhit"));
        assertNotNull(module.getGroup("swing"));
        assertNotNull(module.getGroup("sneak"));
        assertNotNull(module.getGroup("item_positions"));
        assertNotNull(module.getGroup("fishing_rod"));
        assertNotNull(module.getGroup("bow"));
        assertNotNull(module.getGroup("eating_drinking"));
        assertNotNull(module.getGroup("item_switching"));
        assertNotNull(module.getGroup("damage_visuals"));
        assertNotNull(module.getGroup("health"));
        assertNotNull(module.getGroup("resource_pack"));
    }

    @Test
    void testLifecycleAndReset() {
        module.setEnabled(true);
        assertTrue(module.isEnabled());
        module.onTick();

        module.setEnabled(false);
        assertFalse(module.isEnabled());
        assertFalse(BlockingAnimationController.getInstance().isBlockingActive());
    }
}
