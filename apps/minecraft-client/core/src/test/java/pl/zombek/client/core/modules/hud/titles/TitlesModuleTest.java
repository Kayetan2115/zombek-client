package pl.zombek.client.core.modules.hud.titles;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.ModuleCategory;

import static org.junit.jupiter.api.Assertions.*;

public class TitlesModuleTest {

    private TitlesModule titlesModule;

    @BeforeEach
    public void setUp() {
        titlesModule = new TitlesModule();
        titlesModule.resetToDefaults();
    }

    @Test
    public void testModuleMetadata() {
        assertEquals("titles", titlesModule.getId());
        assertEquals("Titles & Action Bar", titlesModule.getName());
        assertEquals(ModuleCategory.HUD, titlesModule.getCategory());
        assertNotNull(titlesModule.getDescription());
    }

    @Test
    public void testSettingGroupsRegistered() {
        assertFalse(titlesModule.getSettingGroups().isEmpty(), "Titles module should have registered setting groups");
        assertTrue(titlesModule.getSettingGroups().size() >= 7);
    }

    @Test
    public void testOverrideVanillaConditions() {
        titlesModule.setEnabled(false);
        assertFalse(TitlesModule.shouldOverrideVanilla());
        assertFalse(TitlesModule.shouldOverrideActionBar());

        titlesModule.setEnabled(true);
        titlesModule.getEnableTitleSetting().setValue(true);
        titlesModule.getEnableActionBarSetting().setValue(true);

        assertTrue(TitlesModule.shouldOverrideVanilla());
        assertTrue(TitlesModule.shouldOverrideActionBar());

        titlesModule.getEnableActionBarSetting().setValue(false);
        assertTrue(TitlesModule.shouldOverrideVanilla());
        assertFalse(TitlesModule.shouldOverrideActionBar());
    }

    @Test
    public void testResetToDefaults() {
        titlesModule.getTitleScaleSetting().setValue(5.0);
        titlesModule.getOverrideFadeInSetting().setValue(40.0);

        titlesModule.resetToDefaults();

        assertEquals(3.0, titlesModule.getTitleScaleSetting().getValue().doubleValue(), 0.01);
        assertEquals(10.0, titlesModule.getOverrideFadeInSetting().getValue().doubleValue(), 0.01);
    }
}
