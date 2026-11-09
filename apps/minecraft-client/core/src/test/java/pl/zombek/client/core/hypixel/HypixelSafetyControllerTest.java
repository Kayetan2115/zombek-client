package pl.zombek.client.core.hypixel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HypixelSafetyControllerTest {
    private HypixelSafetyController controller;
    private HypixelServerDetector detector;

    @BeforeEach
    void setUp() {
        controller = new HypixelSafetyController();
        controller.clearDiagnosticLogs();
        controller.setPreset(SafetyPreset.STRICT_SAFETY);

        detector = HypixelServerDetector.getInstance();
        detector.reset();
    }

    @Test
    void testStrictSafetyOnHypixelPermitsAllowedCategories() {
        detector.onServerConnect("mc.hypixel.net");
        assertTrue(detector.isHypixelConnected());

        assertTrue(controller.isFeatureAllowed(SafetyCategory.COSMETIC));
        assertTrue(controller.isFeatureAllowed(SafetyCategory.LOCAL_UI));
        assertTrue(controller.isFeatureAllowed(SafetyCategory.LOCAL_NOTIFICATION));
        assertTrue(controller.isFeatureAllowed(SafetyCategory.LOCAL_AUTOCOMPLETE));
    }

    @Test
    void testStrictSafetyOnHypixelBlocksProhibitedCategories() {
        detector.onServerConnect("mc.hypixel.net");
        assertTrue(detector.isHypixelConnected());

        assertFalse(controller.isFeatureAllowed(SafetyCategory.GAMEPLAY_AUTOMATION));
        assertFalse(controller.isFeatureAllowed(SafetyCategory.NETWORK_MODIFICATION));
        assertFalse(controller.isFeatureAllowed(SafetyCategory.UNKNOWN));
        assertFalse(controller.isFeatureAllowed(null));
    }

    @Test
    void testProhibitedCategoriesBlockedEvenOnOtherServers() {
        detector.onServerConnect("pvp.land");
        assertFalse(detector.isHypixelConnected());

        controller.setPreset(SafetyPreset.OTHER_SERVERS);
        assertFalse(controller.isFeatureAllowed(SafetyCategory.GAMEPLAY_AUTOMATION));
        assertFalse(controller.isFeatureAllowed(SafetyCategory.NETWORK_MODIFICATION));
        assertTrue(controller.isFeatureAllowed(SafetyCategory.COSMETIC));
    }

    @Test
    void testBlockReasonMessages() {
        String autoReason = controller.getBlockReason(SafetyCategory.GAMEPLAY_AUTOMATION);
        assertTrue(autoReason.contains("Zablokowano"));
        assertTrue(autoReason.contains("Automatyzacja działań gracza"));

        String netReason = controller.getBlockReason(SafetyCategory.NETWORK_MODIFICATION);
        assertTrue(netReason.contains("Modyfikacja pakietów"));

        String nullReason = controller.getBlockReason(null);
        assertTrue(nullReason.contains("Brak"));
    }

    @Test
    void testDiagnosticLogging() {
        detector.onServerConnect("mc.hypixel.net");
        controller.isFeatureAllowed(SafetyCategory.GAMEPLAY_AUTOMATION);

        assertFalse(controller.getDiagnosticLogs().isEmpty());
        assertTrue(controller.getDiagnosticLogs().get(0).contains("GAMEPLAY_AUTOMATION"));

        controller.clearDiagnosticLogs();
        assertTrue(controller.getDiagnosticLogs().isEmpty());
    }
}
