package pl.zombek.client.core;

import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.ModuleMetadata;

import static org.junit.jupiter.api.Assertions.*;

public class ModuleLifecycleTest {

    static class LifecycleProbeModule extends AbstractModule {
        int initCount = 0;
        int enableCount = 0;
        int disableCount = 0;
        int tickCount = 0;
        boolean failOnTick = false;
        boolean failOnEnable = false;

        LifecycleProbeModule(String id) {
            super(new ModuleMetadata(id, "Probe", "Desc", ModuleCategory.OTHER));
        }

        @Override
        public void onInitialize() {
            initCount++;
        }

        @Override
        public void onEnable() {
            enableCount++;
            if (failOnEnable) {
                throw new RuntimeException("Simulated onEnable crash");
            }
        }

        @Override
        public void onDisable() {
            disableCount++;
        }

        @Override
        public void onTick() {
            tickCount++;
            if (failOnTick) {
                throw new IllegalStateException("Simulated onTick crash");
            }
        }
    }

    @Test
    void testNormalLifecycleFlow() {
        LifecycleProbeModule module = new LifecycleProbeModule("normal_probe");
        module.initialize();
        assertEquals(1, module.initCount);

        assertFalse(module.isEnabled());
        module.setEnabled(true);
        assertTrue(module.isEnabled());
        assertEquals(1, module.enableCount);

        module.setEnabled(true);
        assertEquals(1, module.enableCount);

        module.safeTick();
        module.safeTick();
        assertEquals(2, module.tickCount);

        module.setEnabled(false);
        assertFalse(module.isEnabled());
        assertEquals(1, module.disableCount);

        module.setEnabled(false);
        assertEquals(1, module.disableCount);
    }

    @Test
    void testErrorBoundaryOnEnableCrash() {
        LifecycleProbeModule module = new LifecycleProbeModule("crash_enable");
        module.initialize();
        module.failOnEnable = true;

        assertDoesNotThrow(() -> module.setEnabled(true));
        assertFalse(module.isEnabled(), "Module must remain disabled after an onEnable crash");
        assertTrue(module.hasError(), "Module must be marked with error");
        assertNotNull(module.getLastErrorMessage());
        assertTrue(module.getLastErrorMessage().contains("Simulated onEnable crash"));
    }

    @Test
    void testErrorBoundaryOnTickCrash() {
        LifecycleProbeModule module = new LifecycleProbeModule("crash_tick");
        module.initialize();
        module.setEnabled(true);
        assertTrue(module.isEnabled());

        module.failOnTick = true;
        assertDoesNotThrow(module::safeTick, "SafeTick must not propagate exceptions");
        assertFalse(module.isEnabled(), "Module must be auto-disabled after crashing during tick");
        assertTrue(module.hasError());
        assertNotNull(module.getLastErrorMessage());
        assertTrue(module.getLastErrorMessage().contains("Simulated onTick crash"));
    }
}
