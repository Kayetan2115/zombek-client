package pl.zombek.client.core.hypixel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HypixelServerDetectorTest {
    private HypixelServerDetector detector;

    @BeforeEach
    void setUp() {
        detector = new HypixelServerDetector();
        detector.reset();
    }

    @Test
    void testDetectStandardHypixelAddresses() {
        detector.onServerConnect("hypixel.net");
        assertEquals(HypixelServerStatus.HYPIXEL_DETECTED, detector.getStatus());
        assertTrue(detector.isHypixelConnected());

        detector.onServerConnect("mc.hypixel.net");
        assertEquals(HypixelServerStatus.HYPIXEL_DETECTED, detector.getStatus());
        assertTrue(detector.isHypixelConnected());

        detector.onServerConnect("mc.hypixel.net:25565");
        assertEquals(HypixelServerStatus.HYPIXEL_DETECTED, detector.getStatus());
        assertTrue(detector.isHypixelConnected());

        detector.onServerConnect("alpha.hypixel.net");
        assertEquals(HypixelServerStatus.HYPIXEL_DETECTED, detector.getStatus());
        assertTrue(detector.isHypixelConnected());
    }

    @Test
    void testDetectNonHypixelServer() {
        detector.onServerConnect("pvp.land");
        assertEquals(HypixelServerStatus.OTHER_SERVER, detector.getStatus());
        assertFalse(detector.isHypixelConnected());

        detector.onServerConnect("play.hypixel.fake.com");
        assertEquals(HypixelServerStatus.OTHER_SERVER, detector.getStatus());
        assertFalse(detector.isHypixelConnected());
    }

    @Test
    void testSingleplayerMode() {
        detector.onSingleplayerJoin();
        assertEquals(HypixelServerStatus.SINGLEPLAYER, detector.getStatus());
        assertFalse(detector.isHypixelConnected());
    }

    @Test
    void testDisconnectAndReconnect() {
        detector.onServerConnect("mc.hypixel.net");
        assertTrue(detector.isHypixelConnected());

        detector.onServerDisconnect();
        assertEquals(HypixelServerStatus.UNKNOWN_SERVER, detector.getStatus());
        assertFalse(detector.isHypixelConnected());

        detector.onServerConnect("mc.hypixel.net");
        assertEquals(HypixelServerStatus.HYPIXEL_DETECTED, detector.getStatus());
        assertTrue(detector.isHypixelConnected());
    }

    @Test
    void testManualCustomProfileOverride() {
        detector.setCustomProfileActive(true);
        assertEquals(HypixelServerStatus.CUSTOM_HYPIXEL_PROFILE, detector.getStatus());
        assertTrue(detector.isHypixelConnected());

        // Even with non-hypixel address, custom profile forces hypixel safety rules
        detector.onServerConnect("127.0.0.1:25565");
        assertEquals(HypixelServerStatus.CUSTOM_HYPIXEL_PROFILE, detector.getStatus());
        assertTrue(detector.isHypixelConnected());

        detector.setCustomProfileActive(false);
        assertEquals(HypixelServerStatus.OTHER_SERVER, detector.getStatus());
        assertFalse(detector.isHypixelConnected());
    }

    @Test
    void testNullOrEmptyAddressHandledSafely() {
        detector.onServerConnect(null);
        assertEquals(HypixelServerStatus.UNKNOWN_SERVER, detector.getStatus());

        detector.onServerConnect("   ");
        assertEquals(HypixelServerStatus.UNKNOWN_SERVER, detector.getStatus());
    }
}
