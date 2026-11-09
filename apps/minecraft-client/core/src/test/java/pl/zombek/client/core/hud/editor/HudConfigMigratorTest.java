package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.settings.EnumSetting;
import pl.zombek.client.core.settings.NumberSetting;

import static org.junit.jupiter.api.Assertions.*;

class HudConfigMigratorTest {

    private ModuleManager moduleManager;

    private static class DummyMovableModule extends AbstractModule {
        private final NumberSetting posX;
        private final NumberSetting posY;
        private final EnumSetting<HudAnchor> anchor;
        private final NumberSetting scale;

        public DummyMovableModule() {
            super("dummy_movable", "Dummy Movable", "Dummy description", ModuleCategory.HUD);
            this.posX = registerSetting(new NumberSetting("pos_x", "Position X", "X coordinate", 150, 0, 1000, 1));
            this.posY = registerSetting(new NumberSetting("pos_y", "Position Y", "Y coordinate", 85, 0, 1000, 1));
            this.anchor = registerSetting(new EnumSetting<>("anchor", "Anchor", "Screen anchor", HudAnchor.BOTTOM_RIGHT));
            this.scale = registerSetting(new NumberSetting("scale", "Scale", "Scale factor", 1.4, 0.5, 3.0, 0.1));
            setEnabled(true);
        }
    }

    @BeforeEach
    void setUp() {
        moduleManager = ModuleManager.getInstance();
        moduleManager.clear();
    }

    @Test
    void testNonDestructiveMigration() {
        DummyMovableModule dummy = new DummyMovableModule();
        moduleManager.registerModule(dummy);

        HudLayout targetLayout = new HudLayout("migrated", "Układ z migracji");
        HudConfigMigrator.migrateFromModules(targetLayout, moduleManager);

        HudComponentState state = targetLayout.getComponentState("dummy_movable");
        assertNotNull(state);
        assertEquals(150, state.getPosX());
        assertEquals(85, state.getPosY());
        assertEquals(HudAnchor.BOTTOM_RIGHT, state.getAnchor());
        assertEquals(1.4, state.getScale(), 0.001);
        assertTrue(state.isEnabled());
        assertTrue(state.isVisible());

        // Verify module settings are completely preserved and untouched
        assertEquals(150, dummy.posX.getValue());
        assertEquals(85, dummy.posY.getValue());
        assertEquals(HudAnchor.BOTTOM_RIGHT, dummy.anchor.getValue());
        assertEquals(1.4, dummy.scale.getValue());
    }
}
