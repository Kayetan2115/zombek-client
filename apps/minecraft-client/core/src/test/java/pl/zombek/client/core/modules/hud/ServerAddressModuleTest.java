package pl.zombek.client.core.modules.hud;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.hud.serveraddress.ServerAddressLayout;
import pl.zombek.client.core.modules.hud.serveraddress.ServerAddressModule;
import pl.zombek.client.core.serveraddress.PrivacyMode;
import pl.zombek.client.core.serveraddress.ServerAliasManager;
import pl.zombek.client.core.serveraddress.ServerConnectionData;
import pl.zombek.client.core.settings.BooleanSetting;
import pl.zombek.client.core.settings.EnumSetting;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerAddressModuleTest {

    private ServerAddressModule module;

    @BeforeEach
    public void setUp() {
        ServerAliasManager.getInstance().clear();
        module = new ServerAddressModule();
        module.setEnabled(true);
    }

    @AfterEach
    public void tearDown() {
        ServerAliasManager.getInstance().clear();
    }

    @Test
    public void testMetadataAndCategory() {
        assertEquals("server_address", module.getId());
        assertEquals("Server Address", module.getName());
        assertEquals(ModuleCategory.HUD, module.getCategory());
        assertTrue(module.isVisible());
        assertEquals(5, module.getSettingGroups().size()); // general, display, privacy, appearance, layout
    }

    @Test
    public void testFullAddressDisplayIsOffByDefault() {
        BooleanSetting showAddr = (BooleanSetting) module.getSetting("show_server_address");
        assertFalse(showAddr.getValue(), "Full server address display must be OFF by default for privacy!");
    }

    @Test
    public void testSingleplayerNeverShowsFakeIp() {
        ServerConnectionData spData = ServerConnectionData.singleplayer("Hardcore_World_1", "minecraft:overworld");

        // By default hide_world_name is true
        List<String> linesDefault = module.buildDisplayLines(spData, true);
        assertEquals(1, linesDefault.size());
        assertEquals("Singleplayer", linesDefault.get(0));
        assertFalse(linesDefault.get(0).contains("127.0.0.1"), "Singleplayer must never display a fake IP!");
        assertFalse(linesDefault.get(0).contains("localhost"), "Singleplayer must never display localhost!");

        // In detailed layout without hiding world name
        @SuppressWarnings("unchecked")
        EnumSetting<ServerAddressLayout> layoutSetting = (EnumSetting<ServerAddressLayout>) module.getSetting("layout");
        layoutSetting.setValue(ServerAddressLayout.DETAILED);

        BooleanSetting hideWorld = (BooleanSetting) module.getSetting("hide_world_name");
        hideWorld.setValue(false);

        BooleanSetting showDim = (BooleanSetting) module.getSetting("show_dimension");
        showDim.setValue(true);

        List<String> detailedLines = module.buildDisplayLines(spData, true);
        assertTrue(detailedLines.stream().anyMatch(l -> l.contains("SINGLEPLAYER")));
        assertTrue(detailedLines.stream().anyMatch(l -> l.contains("Hardcore_World_1")));
        assertTrue(detailedLines.stream().anyMatch(l -> l.contains("minecraft:overworld")));
        assertTrue(detailedLines.stream().noneMatch(l -> l.contains("127.0.0.1")));
    }

    @Test
    public void testMultiplayerAddressDisplayAndMasking() {
        ServerConnectionData mpData = ServerConnectionData.multiplayer("PlayMC", "play.example.net", 25565, "minecraft:overworld");

        // Default privacy mode is MASK_HOST
        List<String> lines = module.buildDisplayLines(mpData, true);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("pla****.net"));
        assertFalse(lines.get(0).contains("play.example.net"));
    }

    @Test
    public void testCustomAliasSubstitution() {
        ServerConnectionData mpData = ServerConnectionData.multiplayer("Secret SMP", "smp.private-server.org", 25565, "minecraft:overworld");
        ServerAliasManager.getInstance().setAlias("smp.private-server.org", "Friends SMP", 0xFFFFFF);

        List<String> lines = module.buildDisplayLines(mpData, true);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("Friends SMP"));
        assertFalse(lines.get(0).contains("smp.private-server.org"));
    }

    @Test
    public void testPreviewFallbackWhenDisconnected() {
        List<String> lines = module.buildDisplayLines(null, false);
        assertFalse(lines.isEmpty());
        assertTrue(lines.get(0).contains("[PREVIEW]"));
    }

    @Test
    public void testRestoreDefaults() {
        @SuppressWarnings("unchecked")
        EnumSetting<PrivacyMode> privacySetting = (EnumSetting<PrivacyMode>) module.getSetting("privacy_mode");
        privacySetting.setValue(PrivacyMode.NORMAL);
        assertEquals(PrivacyMode.NORMAL, privacySetting.getValue());

        privacySetting.reset();
        assertEquals(PrivacyMode.MASK_HOST, privacySetting.getValue());
    }
}
