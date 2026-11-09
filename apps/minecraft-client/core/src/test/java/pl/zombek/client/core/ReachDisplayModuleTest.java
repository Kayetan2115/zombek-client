package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.pvp.ReachDisplayModule;
import pl.zombek.client.core.modules.pvp.ReachDisplayStyle;
import pl.zombek.client.core.tracking.ReachTracker;

import static org.junit.jupiter.api.Assertions.*;

public class ReachDisplayModuleTest {
    private ReachDisplayModule module;

    @BeforeEach
    void setUp() {
        module = new ReachDisplayModule();
        ReachTracker.getInstance().resetStatistics();
    }

    @Test
    void testModuleMetadata() {
        assertEquals("reach-display", module.getId());
        assertEquals("Reach Display", module.getName());
        assertEquals(ModuleCategory.PVP, module.getCategory());
        assertFalse(module.isEnabled());
    }

    @Test
    void testSettingGroups() {
        assertNotNull(module.getGroup("general"));
        assertNotNull(module.getGroup("display"));
        assertNotNull(module.getGroup("behavior"));
        assertNotNull(module.getGroup("colors"));
        assertNotNull(module.getGroup("appearance"));
        assertNotNull(module.getGroup("layout"));

        assertTrue(module.getShowLastReachSetting().getValue());
        assertEquals(ReachDisplayStyle.COMPACT, module.getDisplayStyleSetting().getValue());
        assertEquals("blocks", module.getUnitSetting().getValue());
        assertEquals(HudAnchor.TOP_LEFT, module.getAnchorSetting().getValue());
    }

    @Test
    void testRenderWithoutException() {
        TestHudRenderer testRenderer = new TestHudRenderer(800, 600);
        module.setEnabled(true);

        ReachTracker.getInstance().recordObservedHit(3.24, 101, "Target1");

        assertDoesNotThrow(() -> module.render(testRenderer));
        assertTrue(testRenderer.drawTextCallCount > 0);
        assertTrue(testRenderer.lastTextDrawn.contains("3.24"));
    }

    @Test
    void testDetailedStyleRendering() {
        TestHudRenderer testRenderer = new TestHudRenderer(800, 600);
        module.setEnabled(true);
        module.getDisplayStyleSetting().setValue(ReachDisplayStyle.DETAILED);
        module.getShowAverageReachSetting().setValue(true);

        ReachTracker.getInstance().recordObservedHit(3.0, 1, "E1");
        ReachTracker.getInstance().recordObservedHit(4.0, 2, "E2");

        assertDoesNotThrow(() -> module.render(testRenderer));
        assertTrue(testRenderer.drawTextCallCount >= 2);
    }
}
