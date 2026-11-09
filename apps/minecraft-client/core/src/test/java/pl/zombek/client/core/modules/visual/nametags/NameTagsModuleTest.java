package pl.zombek.client.core.modules.visual.nametags;

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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NameTagsModuleTest {

    private NameTagsModule module;

    @BeforeEach
    void setUp() {
        ModuleManager.getInstance().reset();
        module = new NameTagsModule();
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
        assertEquals("nametags", module.getId());
        assertEquals("NameTags", module.getName());
        assertEquals(ModuleCategory.VISUAL, module.getCategory());
        assertFalse(module.isEnabled());
    }

    @Test
    void testSettingGroups() {
        assertNotNull(module.getGroup("general"));
        assertNotNull(module.getGroup("player_info"));
        assertNotNull(module.getGroup("custom_name"));
        assertNotNull(module.getGroup("health"));
        assertNotNull(module.getGroup("distance"));
        assertNotNull(module.getGroup("armor"));
        assertNotNull(module.getGroup("appearance"));
        assertNotNull(module.getGroup("visibility"));
        assertNotNull(module.getGroup("position"));
    }

    @Test
    void testShouldOverrideVanilla() {
        assertFalse(NameTagsModule.shouldOverrideVanilla());
        module.setEnabled(true);
        assertTrue(NameTagsModule.shouldOverrideVanilla());
        module.getUseCustomRendererSetting().setValue(false);
        assertFalse(NameTagsModule.shouldOverrideVanilla());
    }

    @Test
    void testFormatNameTagWithFriendAndHealth() {
        UUID uuid = UUID.randomUUID();
        FriendEntry friend = new FriendEntry(uuid, "CoolPlayer", "Buddy", 0xFF55FF55, "★", "", "");
        NameTagFriendManager.getInstance().addFriend(friend);

        PlayerInfoData data = new PlayerInfoData(
                uuid, "CoolPlayer", "CoolPlayer", "[PRO] ", "", "GreenTeam", 0xFF55FF55,
                25, DataAvailability.AVAILABLE, "SURVIVAL", DataAvailability.AVAILABLE,
                18.0f, 20.0f, 2.0f, DataAvailability.AVAILABLE,
                Collections.emptyList(), DataAvailability.AVAILABLE, "Diamond Sword", DataAvailability.AVAILABLE,
                10.0, DataAvailability.AVAILABLE, true, friend, null
        );

        module.getShowDistanceSetting().setValue(true);
        module.getShowPingSetting().setValue(true);

        CustomNameTagRenderer.NameTagFormattedContent formatted =
                CustomNameTagRenderer.formatNameTag(data, module);

        assertNotNull(formatted);
        assertTrue(formatted.mainLine.contains("★"));
        assertTrue(formatted.mainLine.contains("Buddy"));
        assertTrue(formatted.mainLine.contains("18.0"));
        assertTrue(formatted.mainLine.contains("10m"));
        assertTrue(formatted.mainLine.contains("25ms"));
        assertTrue(formatted.isFriend);
    }

    @Test
    void testRestoreDefaults() {
        module.getScaleMultiplierSetting().setValue(2.5);
        module.getHealthStyleSetting().setValue(HealthDisplayStyle.HEARTS);
        module.resetToDefaults();

        assertEquals(1.0, module.getScaleMultiplierSetting().getValue());
        assertEquals(HealthDisplayStyle.NUMERIC, module.getHealthStyleSetting().getValue());
    }
}
