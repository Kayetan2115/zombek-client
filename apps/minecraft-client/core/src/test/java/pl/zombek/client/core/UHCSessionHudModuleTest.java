package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.survival.UHCSessionHudModule;
import pl.zombek.client.core.uhc.*;

import static org.junit.jupiter.api.Assertions.*;

public class UHCSessionHudModuleTest {

    private UHCSessionHudModule module;

    @BeforeEach
    public void setUp() {
        module = new UHCSessionHudModule();
        UhcSessionTracker.getInstance().resetSession();
        UhcDataProviderRegistry.setProvider(new IUhcDataProvider() {
            @Override
            public boolean isPlayerInGame() { return true; }
            @Override
            public float getPlayerHealth() { return 18.5f; }
            @Override
            public float getPlayerMaxHealth() { return 20.0f; }
            @Override
            public float getPlayerAbsorption() { return 4.0f; }
            @Override
            public int getPlayerFoodLevel() { return 20; }
            @Override
            public float getPlayerSaturation() { return 5.0f; }
            @Override
            public int getItemCount(UhcTrackedItem item) {
                switch (item) {
                    case GOLDEN_APPLE: return 6;
                    case GOLDEN_HEAD: return 2;
                    case ARROW: return 64;
                    case ENDER_PEARL: return 4;
                    case FLINT_AND_STEEL: return 1;
                    case WATER_BUCKET: return 1;
                    default: return 0;
                }
            }
        });
    }

    @Test
    public void testModuleMetadata() {
        assertEquals("uhc_session_hud", module.getId());
        assertEquals("UHC Session HUD", module.getName());
        assertEquals(ModuleCategory.SURVIVAL, module.getCategory());
        assertFalse(module.isEnabled());
    }

    @Test
    public void testSettingGroupsAndDefaults() {
        assertNotNull(module.getGroup("general"));
        assertNotNull(module.getGroup("player"));
        assertNotNull(module.getGroup("items"));
        assertNotNull(module.getGroup("timers"));
        assertNotNull(module.getGroup("match"));
        assertNotNull(module.getGroup("behavior"));
        assertNotNull(module.getGroup("layout"));
        assertNotNull(module.getGroup("colors"));

        assertEquals(UhcMode.SOLO, module.getModeSetting().getValue());
        assertEquals(UhcLayoutStyle.COMPACT, module.getLayoutStyleSetting().getValue());
    }

    @Test
    public void testRenderOnTestHudRenderer() {
        module.setEnabled(true);
        assertTrue(module.isVisible());

        TestHudRenderer testRenderer = new TestHudRenderer();
        assertDoesNotThrow(() -> module.render(testRenderer));
        assertFalse(testRenderer.drawnTexts.isEmpty());
    }
}
