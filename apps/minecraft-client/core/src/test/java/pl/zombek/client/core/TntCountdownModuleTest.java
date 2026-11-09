package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.pvp.tnt.TntCountdownModule;
import pl.zombek.client.core.modules.pvp.tnt.TntTracker;

import static org.junit.jupiter.api.Assertions.*;

public class TntCountdownModuleTest {

    private TntCountdownModule module;

    @BeforeEach
    public void setUp() {
        module = new TntCountdownModule();
        TntTracker.getInstance().clear();
    }

    @Test
    public void testModuleMetadata() {
        assertEquals("tnt_countdown", module.getId());
        assertEquals("TNT Countdown", module.getName());
        assertEquals(ModuleCategory.PVP, module.getCategory());
        assertFalse(module.isEnabled());
    }

    @Test
    public void testSettingGroups() {
        assertNotNull(module.getGroup("general"));
        assertNotNull(module.getGroup("appearance"));
        assertNotNull(module.getGroup("visibility"));
        assertNotNull(module.getGroup("position"));

        assertTrue(module.getShowCountdownSetting().getValue());
        assertTrue(module.getDynamicColorSetting().getValue());
        assertEquals(48.0, module.getMaxDistanceSetting().getValue().doubleValue());
    }

    @Test
    public void testTrackerIntegration() {
        TntTracker tracker = TntTracker.getInstance();
        tracker.updateTnt(101, 10.0, 64.0, 10.0, 80, 5.0, true);
        tracker.updateTnt(102, 12.0, 64.0, 12.0, 30, 8.0, true);

        assertEquals(2, tracker.getActiveCount());

        tracker.removeTnt(101);
        assertEquals(1, tracker.getActiveCount());

        tracker.clear();
        assertEquals(0, tracker.getActiveCount());
    }
}
