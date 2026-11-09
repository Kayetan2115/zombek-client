package pl.zombek.client.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.data.TargetEntityType;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.visual.HitColorMode;
import pl.zombek.client.core.modules.visual.HitColorModule;
import pl.zombek.client.core.tracking.HitColorTracker;

import static org.junit.jupiter.api.Assertions.*;

public class HitColorModuleTest {

    private HitColorModule module;

    @BeforeEach
    void setUp() {
        HitColorTracker.getInstance().resetForTesting();
        module = new HitColorModule();
    }

    @AfterEach
    void tearDown() {
        if (module != null) {
            module.setEnabled(false);
        }
        HitColorTracker.getInstance().resetForTesting();
    }

    @Test
    void testModuleMetadataAndCategory() {
        assertEquals("hit-color", module.getId());
        assertEquals("Hit Color", module.getName());
        assertEquals(ModuleCategory.VISUAL, module.getCategory());
        assertFalse(module.isEnabled());
    }

    @Test
    void testSettingGroupsPresent() {
        assertNotNull(module.getGroup("general"));
        assertNotNull(module.getGroup("colors"));
        assertNotNull(module.getGroup("timing"));
        assertNotNull(module.getGroup("appearance"));
        assertNotNull(module.getGroup("advanced"));
    }

    @Test
    void testDisableClearsTracker() {
        module.setEnabled(true);
        HitColorTracker.getInstance().recordHit(10, TargetEntityType.PLAYER, true, true);
        assertEquals(1, HitColorTracker.getInstance().getActiveEffectCount());

        module.setEnabled(false);
        assertEquals(0, HitColorTracker.getInstance().getActiveEffectCount(), "Disabling module must clear active tracker effects");
    }
}
