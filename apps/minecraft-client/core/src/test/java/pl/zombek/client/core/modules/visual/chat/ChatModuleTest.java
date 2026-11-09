package pl.zombek.client.core.modules.visual.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.ModuleCategory;

import static org.junit.jupiter.api.Assertions.*;

public class ChatModuleTest {

    private ChatModule chatModule;

    @BeforeEach
    public void setUp() {
        chatModule = new ChatModule();
        chatModule.resetToDefaults();
    }

    @Test
    public void testModuleMetadata() {
        assertEquals("chat", chatModule.getId());
        assertEquals("Chat", chatModule.getName());
        assertEquals(ModuleCategory.VISUAL, chatModule.getCategory());
        assertNotNull(chatModule.getDescription());
    }

    @Test
    public void testSettingGroupsRegistered() {
        assertFalse(chatModule.getSettingGroups().isEmpty(), "Chat module should have registered setting groups");
        assertTrue(chatModule.getSettingGroups().size() >= 8);
    }

    @Test
    public void testOverrideVanillaCondition() {
        chatModule.setEnabled(false);
        assertFalse(ChatModule.shouldOverrideVanilla());

        chatModule.setEnabled(true);
        assertTrue(ChatModule.shouldOverrideVanilla());
    }

    @Test
    public void testResetToDefaults() {
        chatModule.getChatWidthSetting().setValue(600.0);
        chatModule.getLineSpacingSetting().setValue(18.0);
        chatModule.getBackgroundOpacitySetting().setValue(0.9);

        chatModule.resetToDefaults();

        assertEquals(320.0, chatModule.getChatWidthSetting().getValue().doubleValue(), 0.01);
        assertEquals(10.0, chatModule.getLineSpacingSetting().getValue().doubleValue(), 0.01);
        assertEquals(0.7, chatModule.getBackgroundOpacitySetting().getValue().doubleValue(), 0.01);
    }
}
