package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.visual.shinypots.ShinyPotsModule;

import static org.junit.jupiter.api.Assertions.*;

public class ShinyPotsModuleTest {

    private ShinyPotsModule module;

    @BeforeEach
    public void setUp() {
        module = new ShinyPotsModule();
    }

    @Test
    public void testModuleMetadata() {
        assertEquals("shiny_pots", module.getId());
        assertEquals("Shiny Pots", module.getName());
        assertEquals(ModuleCategory.VISUAL, module.getCategory());
        assertFalse(module.isEnabled());
    }

    @Test
    public void testSettingGroups() {
        assertNotNull(module.getGroup("general"));
        assertNotNull(module.getGroup("glint"));
        assertNotNull(module.getGroup("appearance"));
        assertNotNull(module.getGroup("visibility"));

        assertTrue(module.getRegularPotionsSetting().getValue());
        assertTrue(module.getSplashPotionsSetting().getValue());
        assertTrue(module.getLingeringPotionsSetting().getValue());
        assertTrue(module.getTippedArrowsSetting().getValue());

        assertTrue(module.getColoredPotionsSetting().getValue());
        assertTrue(module.getShowInInventorySetting().getValue());
    }
}
