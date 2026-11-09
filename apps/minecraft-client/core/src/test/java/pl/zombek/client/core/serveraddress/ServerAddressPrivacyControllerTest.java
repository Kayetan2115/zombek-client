package pl.zombek.client.core.serveraddress;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ServerAddressPrivacyControllerTest {

    @BeforeEach
    public void setUp() {
        ServerAliasManager.getInstance().clear();
    }

    @AfterEach
    public void tearDown() {
        ServerAliasManager.getInstance().clear();
    }

    @Test
    public void testFormatHostNormal() {
        String host = "play.example.net";
        assertEquals("play.example.net", ServerAddressPrivacyController.formatHost(host, PrivacyMode.NORMAL, false));
    }

    @Test
    public void testFormatHostHiddenAndStreamer() {
        String host = "play.example.net";
        assertEquals("[HIDDEN]", ServerAddressPrivacyController.formatHost(host, PrivacyMode.HIDE_ADDRESS, false));
        assertEquals("[HIDDEN]", ServerAddressPrivacyController.formatHost(host, PrivacyMode.STREAMER_MODE, false));
        assertEquals("[HIDDEN]", ServerAddressPrivacyController.formatHost(host, PrivacyMode.NORMAL, true)); // Streamer toggle enabled
    }

    @Test
    public void testFormatHostMasked() {
        String host = "play.example.net";
        String masked = ServerAddressPrivacyController.formatHost(host, PrivacyMode.MASK_HOST, false);
        assertEquals("pla****.net", masked);

        String shortHost = "mc.gg";
        String maskedShort = ServerAddressPrivacyController.maskHostString(shortHost);
        assertEquals("mc****", maskedShort);

        String tinyHost = "123";
        assertEquals("****", ServerAddressPrivacyController.maskHostString(tinyHost));
    }

    @Test
    public void testFormatHostUnavailable() {
        assertEquals("UNAVAILABLE", ServerAddressPrivacyController.formatHost(null, PrivacyMode.NORMAL, false));
        assertEquals("UNAVAILABLE", ServerAddressPrivacyController.formatHost("", PrivacyMode.NORMAL, false));
    }

    @Test
    public void testFormatWorldNameSingleplayer() {
        assertEquals("[HIDDEN]", ServerAddressPrivacyController.formatWorldName("Survival_S3", true, false));
        assertEquals("[HIDDEN]", ServerAddressPrivacyController.formatWorldName("Survival_S3", false, true));
        assertEquals("Survival_S3", ServerAddressPrivacyController.formatWorldName("Survival_S3", false, false));
        assertEquals("Singleplayer World", ServerAddressPrivacyController.formatWorldName(null, false, false));
    }

    @Test
    public void testServerAliasManager() {
        ServerAliasManager aliasManager = ServerAliasManager.getInstance();
        assertFalse(aliasManager.hasAlias("play.hypixel.net"));

        aliasManager.setAlias("play.hypixel.net:25565", "My Fav Server", 0xFFAAAA);
        assertTrue(aliasManager.hasAlias("play.hypixel.net"));

        ServerAliasManager.AliasEntry entry = aliasManager.getAlias("play.hypixel.net");
        assertNotNull(entry);
        assertEquals("My Fav Server", entry.getAlias());
        assertEquals(0xFFAAAA, entry.getCustomColor());

        aliasManager.removeAlias("play.hypixel.net");
        assertFalse(aliasManager.hasAlias("play.hypixel.net"));
    }

    @Test
    public void testCopyToClipboardDoesNotCrash() {
        // In headless or non-headless test environments, shouldn't throw an unhandled exception
        assertDoesNotThrow(() -> ServerAddressPrivacyController.copyToClipboard("test.address"));
        assertFalse(ServerAddressPrivacyController.copyToClipboard(null));
        assertFalse(ServerAddressPrivacyController.copyToClipboard(""));
    }
}
