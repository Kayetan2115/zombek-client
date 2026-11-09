package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.pvp.PvPInfoModule;
import pl.zombek.client.core.modules.pvp.PvPInfoStyle;
import pl.zombek.client.core.tracking.ProjectileTracker;
import pl.zombek.client.core.tracking.ProjectileType;
import pl.zombek.client.core.tracking.PvPSessionManager;

import static org.junit.jupiter.api.Assertions.*;

public class PvPInfoModuleTest {
    private PvPInfoModule module;

    @BeforeEach
    void setUp() {
        module = new PvPInfoModule();
        PvPSessionManager.getInstance().resetSession();
    }

    @Test
    void testModuleMetadata() {
        assertEquals("pvp-info", module.getId());
        assertEquals("PvP Info", module.getName());
        assertEquals(ModuleCategory.PVP, module.getCategory());
        assertFalse(module.isEnabled());
    }

    @Test
    void testSettingGroups() {
        assertNotNull(module.getGroup("general"));
        assertNotNull(module.getGroup("display"));
        assertNotNull(module.getGroup("colors"));
        assertNotNull(module.getGroup("appearance"));
        assertNotNull(module.getGroup("layout"));

        assertTrue(module.getEnableMeleeSetting().getValue());
        assertTrue(module.getEnableProjectileSetting().getValue());
        assertTrue(module.getEnableHealthSetting().getValue());
        assertEquals(PvPInfoStyle.COMPACT, module.getStyleSetting().getValue());
        assertEquals(HudAnchor.TOP_LEFT, module.getAnchorSetting().getValue());
    }

    @Test
    void testRenderWithAllSections() {
        TestHudRenderer testRenderer = new TestHudRenderer(800, 600);
        module.setEnabled(true);

        PvPSessionManager.getInstance().getCurrentSession().recordAttackAttempt();
        PvPSessionManager.getInstance().getCurrentSession().recordObservedHit();
        PvPSessionManager.getInstance().getCurrentSession().updatePlayerHealth(18.0f, 0.0f);
        ProjectileTracker.getInstance().recordProjectileFired(ProjectileType.ARROW);

        assertDoesNotThrow(() -> module.render(testRenderer));
        assertTrue(testRenderer.drawTextCallCount > 0);
    }

    @Test
    void testResetSessionAction() {
        PvPSessionManager.getInstance().getCurrentSession().recordAttackAttempt();
        assertEquals(1, PvPSessionManager.getInstance().getCurrentSession().getMeleeAttacks());

        module.resetSession();
        assertEquals(0, PvPSessionManager.getInstance().getCurrentSession().getMeleeAttacks());
    }
}
