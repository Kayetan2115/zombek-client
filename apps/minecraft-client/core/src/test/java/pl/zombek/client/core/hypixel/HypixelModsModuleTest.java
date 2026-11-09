package pl.zombek.client.core.hypixel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.TestHudRenderer;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.hypixel.HypixelModsModule;

import static org.junit.jupiter.api.Assertions.*;

public class HypixelModsModuleTest {
    private HypixelModsModule module;

    @BeforeEach
    void setUp() {
        module = new HypixelModsModule();
    }

    @Test
    void testModuleMetadata() {
        assertEquals("hypixel_mods", module.getId());
        assertEquals("Hypixel Mods", module.getName());
        assertEquals(ModuleCategory.OTHER, module.getCategory());
        assertEquals("hypixel_mods", module.getElementId());
    }

    @Test
    void testAllNineSettingGroupsRegistered() {
        assertEquals(9, module.getSettingGroups().size());
        assertNotNull(module.getGroupServerDetection());
        assertNotNull(module.getGroupSafetyMode());
        assertNotNull(module.getGroupChatCustomization());
        assertNotNull(module.getGroupChatFilters());
        assertNotNull(module.getGroupTablist());
        assertNotNull(module.getGroupAutocomplete());
        assertNotNull(module.getGroupLevelDisplay());
        assertNotNull(module.getGroupManualSuggestions());
        assertNotNull(module.getGroupNotifications());
    }

    @Test
    void testEnableDisableLifecycle() {
        assertFalse(module.isEnabled());
        module.setEnabled(true);
        assertTrue(module.isEnabled());
        assertTrue(module.isVisible());

        module.setEnabled(false);
        assertFalse(module.isEnabled());
        assertFalse(module.isVisible());
    }

    @Test
    void testRenderWithoutErrors() {
        module.setEnabled(true);
        TestHudRenderer renderer = new TestHudRenderer(1920, 1080);

        // Add a sample notification and manual suggestion
        HypixelManager.getInstance().getNotificationController().showNotification(new HypixelNotification(
                HypixelNotificationType.SERVER_DETECTION,
                "Test Title",
                "Test Message",
                0xFF55FF55,
                5.0f
        ));
        HypixelManager.getInstance().getManualSuggestions().triggerMatchEndSuggestion();

        assertDoesNotThrow(() -> module.render(renderer));
    }

    @Test
    void testApplySettingsSynchronization() {
        module.getAutoDetectSetting().setValue(false);
        module.getSafetyPresetSetting().setValue(SafetyPreset.CUSTOM);
        module.getEnableShortChannelsSetting().setValue(false);

        module.applySettingsToManagers();

        assertEquals(SafetyPreset.CUSTOM, HypixelManager.getInstance().getSafetyController().getPreset());
        assertFalse(HypixelManager.getInstance().getChatController().isEnableShortChannelNames());
    }
}
