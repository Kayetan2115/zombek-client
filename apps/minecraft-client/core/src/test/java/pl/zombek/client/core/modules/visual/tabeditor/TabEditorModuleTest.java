package pl.zombek.client.core.modules.visual.tabeditor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.friends.FriendEntry;
import pl.zombek.client.core.friends.NameTagFriendManager;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.playerinfo.DataAvailability;
import pl.zombek.client.core.playerinfo.PlayerInfoData;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TabEditorModuleTest {

    private TabEditorModule module;

    @BeforeEach
    void setUp() {
        ModuleManager.getInstance().reset();
        module = new TabEditorModule();
        ModuleManager.getInstance().registerModule(module);
        NameTagFriendManager.getInstance().clearAll();
    }

    @AfterEach
    void tearDown() {
        ModuleManager.getInstance().reset();
        NameTagFriendManager.getInstance().clearAll();
    }

    @Test
    void testMetadata() {
        assertEquals("tab_editor", module.getId());
        assertEquals("Tab Editor", module.getName());
        assertEquals(ModuleCategory.VISUAL, module.getCategory());
        assertFalse(module.isEnabled());
    }

    @Test
    void testSettingGroups() {
        assertNotNull(module.getGroup("general"));
        assertNotNull(module.getGroup("header"));
        assertNotNull(module.getGroup("footer"));
        assertNotNull(module.getGroup("player_entry"));
        assertNotNull(module.getGroup("appearance"));
        assertNotNull(module.getGroup("grid"));
        assertNotNull(module.getGroup("sorting"));
        assertNotNull(module.getGroup("ping"));
        assertNotNull(module.getGroup("friends"));
        assertNotNull(module.getGroup("animations"));
        assertNotNull(module.getGroup("privacy"));
        assertNotNull(module.getGroup("position"));
    }

    @Test
    void testShouldOverrideVanilla() {
        assertFalse(TabEditorModule.shouldOverrideVanilla());
        module.setEnabled(true);
        assertTrue(TabEditorModule.shouldOverrideVanilla());
        module.getUseCustomTabSetting().setValue(false);
        assertFalse(TabEditorModule.shouldOverrideVanilla());
    }

    @Test
    void testHeaderFooterModeResolution() {
        // Original mode
        List<String> originalLines = TabHeaderFooterManager.resolveLines(
                HeaderFooterMode.ORIGINAL, "Server Line 1\nServer Line 2", "Custom", true
        );
        assertEquals(2, originalLines.size());
        assertEquals("Server Line 1", originalLines.get(0));

        // Custom mode
        List<String> customLines = TabHeaderFooterManager.resolveLines(
                HeaderFooterMode.CUSTOM, "Server Line 1", "MyCustomHeader", true
        );
        assertEquals(1, customLines.size());
        assertEquals("MyCustomHeader", customLines.get(0));

        // Hidden mode
        List<String> hiddenLines = TabHeaderFooterManager.resolveLines(
                HeaderFooterMode.HIDDEN, "Server Line 1", "Custom", true
        );
        assertTrue(hiddenLines.isEmpty());
    }

    @Test
    void testFriendHighlightInTab() {
        UUID friendUuid = UUID.randomUUID();
        FriendEntry friend = new FriendEntry(friendUuid, "RealNick", "BuddyAlias", 0xFF55FF55, "★", "", "");
        PlayerInfoData player = new PlayerInfoData(
                friendUuid, "RealNick", "RealNick", "", "", "Team", 0xFFFFFFFF,
                20, DataAvailability.AVAILABLE, "SURVIVAL", DataAvailability.AVAILABLE,
                20.0f, 20.0f, 0.0f, DataAvailability.AVAILABLE,
                Collections.emptyList(), DataAvailability.AVAILABLE, "", DataAvailability.AVAILABLE,
                10.0, DataAvailability.AVAILABLE, true, friend, null
        );

        String displayName = TabFriendHighlightManager.resolveDisplayName(player, true, "★");
        assertTrue(displayName.contains("★"));
        assertTrue(displayName.contains("BuddyAlias"));

        int color = TabFriendHighlightManager.resolveEntryColor(player, true, 0xFFFFFFFF, 0xFF55FF55);
        assertEquals(0xFF55FF55, color);
    }

    @Test
    void testRestoreDefaults() {
        module.getMaxColumnsSetting().setValue(6.0);
        module.getBackgroundOpacitySetting().setValue(0.9);
        module.resetToDefaults();

        assertEquals(4.0, module.getMaxColumnsSetting().getValue());
        assertEquals(0.6, module.getBackgroundOpacitySetting().getValue());
    }
}
