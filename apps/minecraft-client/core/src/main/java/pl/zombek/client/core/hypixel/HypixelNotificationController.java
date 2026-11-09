package pl.zombek.client.core.hypixel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manages and queues local HUD notification toasts for Hypixel events,
 * safety warnings, and manual suggestions.
 */
public class HypixelNotificationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HypixelNotificationController.class);
    private static HypixelNotificationController instance;

    private boolean enabled = true;
    private boolean showServerDetection = true;
    private boolean showSafetyWarnings = true;
    private boolean showManualSuggestions = true;
    private boolean showModuleStatus = true;
    private float notificationDuration = 5.0f;
    private boolean sound = true;
    private int maxVisibleNotifications = 4;

    private final List<HypixelNotification> activeNotifications = new ArrayList<>();

    public HypixelNotificationController() {}

    public static synchronized HypixelNotificationController getInstance() {
        if (instance == null) {
            instance = new HypixelNotificationController();
        }
        return instance;
    }

    /**
     * Dispatches a new notification to the active HUD toast stack.
     */
    public synchronized void showNotification(HypixelNotification notification) {
        if (!enabled || notification == null) {
            return;
        }

        // Filter based on notification type settings
        switch (notification.getType()) {
            case SERVER_DETECTION:
                if (!showServerDetection) return;
                break;
            case SAFETY_WARNING:
                if (!showSafetyWarnings) return;
                break;
            case MANUAL_SUGGESTION:
                if (!showManualSuggestions) return;
                break;
            case MODULE_STATUS:
                if (!showModuleStatus) return;
                break;
        }

        pruneExpired();

        // Optional sound feedback via platform adapter
        if (sound) {
            HypixelDataProviderRegistry.getProvider().playSound("minecraft:ui.toast.in", 0.8f, 1.0f);
        }

        activeNotifications.add(0, notification);
        while (activeNotifications.size() > maxVisibleNotifications) {
            activeNotifications.remove(activeNotifications.size() - 1);
        }

        LOGGER.info("[HypixelNotificationController] Notification displayed: [{}] {}", notification.getType(), notification.getTitle());
    }

    public synchronized void pruneExpired() {
        activeNotifications.removeIf(HypixelNotification::isExpired);
    }

    public synchronized List<HypixelNotification> getVisibleNotifications() {
        pruneExpired();
        return Collections.unmodifiableList(new ArrayList<>(activeNotifications));
    }

    public synchronized void clear() {
        activeNotifications.clear();
    }

    // Getters and Setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isShowServerDetection() { return showServerDetection; }
    public void setShowServerDetection(boolean show) { this.showServerDetection = show; }
    public boolean isShowSafetyWarnings() { return showSafetyWarnings; }
    public void setShowSafetyWarnings(boolean show) { this.showSafetyWarnings = show; }
    public boolean isShowManualSuggestions() { return showManualSuggestions; }
    public void setShowManualSuggestions(boolean show) { this.showManualSuggestions = show; }
    public boolean isShowModuleStatus() { return showModuleStatus; }
    public void setShowModuleStatus(boolean show) { this.showModuleStatus = show; }
    public float getNotificationDuration() { return notificationDuration; }
    public void setNotificationDuration(float duration) { this.notificationDuration = Math.max(1.0f, Math.min(15.0f, duration)); }
    public boolean isSound() { return sound; }
    public void setSound(boolean sound) { this.sound = sound; }
    public int getMaxVisibleNotifications() { return maxVisibleNotifications; }
    public void setMaxVisibleNotifications(int max) { this.maxVisibleNotifications = Math.max(1, Math.min(10, max)); }
}
