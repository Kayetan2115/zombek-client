package pl.zombek.client.core.hypixel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HypixelNotificationControllerTest {
    private HypixelNotificationController controller;

    @BeforeEach
    void setUp() {
        controller = new HypixelNotificationController();
        controller.clear();
        controller.setEnabled(true);
        controller.setShowServerDetection(true);
        controller.setShowSafetyWarnings(true);
        controller.setShowManualSuggestions(true);
        controller.setShowModuleStatus(true);
        controller.setMaxVisibleNotifications(4);
    }

    @Test
    void testShowNotification() {
        assertEquals(0, controller.getVisibleNotifications().size());

        HypixelNotification notif = new HypixelNotification(
                HypixelNotificationType.SERVER_DETECTION,
                "Hypixel Wykryty",
                "Witaj na Hypixel!",
                0xFF55FF55,
                5.0f
        );
        controller.showNotification(notif);

        assertEquals(1, controller.getVisibleNotifications().size());
        assertEquals("Hypixel Wykryty", controller.getVisibleNotifications().get(0).getTitle());
    }

    @Test
    void testNotificationFadeAlpha() {
        HypixelNotification notif = new HypixelNotification(
                HypixelNotificationType.MODULE_STATUS,
                "Status",
                "Opis",
                0xFF55FFFF,
                5.0f
        );
        // Fresh notification should be positive alpha
        assertTrue(notif.getAlpha() >= 0.0f);
        assertTrue(notif.getAlpha() <= 1.0f);
    }

    @Test
    void testMaxVisibleNotificationsLimit() {
        controller.setMaxVisibleNotifications(3);

        for (int i = 0; i < 5; i++) {
            controller.showNotification(new HypixelNotification(
                    HypixelNotificationType.MODULE_STATUS,
                    "Notif " + i,
                    "Treść",
                    0xFF55FFFF,
                    5.0f
            ));
        }

        assertEquals(3, controller.getVisibleNotifications().size());
        assertEquals("Notif 4", controller.getVisibleNotifications().get(0).getTitle());
    }

    @Test
    void testNotificationTypeToggles() {
        controller.setShowSafetyWarnings(false);

        controller.showNotification(new HypixelNotification(
                HypixelNotificationType.SAFETY_WARNING,
                "Uwaga",
                "Zablokowano",
                0xFFFF5555,
                5.0f
        ));

        // Blocked by toggle
        assertEquals(0, controller.getVisibleNotifications().size());
    }

    @Test
    void testClearNotifications() {
        controller.showNotification(new HypixelNotification(
                HypixelNotificationType.MODULE_STATUS,
                "Title",
                "Msg",
                0xFF55FFFF,
                5.0f
        ));
        assertEquals(1, controller.getVisibleNotifications().size());

        controller.clear();
        assertEquals(0, controller.getVisibleNotifications().size());
    }
}
