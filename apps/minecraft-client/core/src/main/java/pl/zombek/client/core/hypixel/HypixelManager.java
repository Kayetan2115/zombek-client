package pl.zombek.client.core.hypixel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.bedwars.BedwarsGameDetector;
import pl.zombek.client.core.bedwars.BedwarsGameState;
import pl.zombek.client.core.bedwars.BedwarsManager;

import java.util.Locale;

/**
 * Central orchestrator for all Hypixel Mods subsystems:
 * server detection, safety governance, chat formatting, chat filters,
 * command autocomplete, level display, manual social suggestions, and HUD notifications.
 */
public class HypixelManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(HypixelManager.class);
    private static HypixelManager instance;

    private final HypixelServerDetector serverDetector;
    private final HypixelSafetyController safetyController;
    private final HypixelChatController chatController;
    private final HypixelChatFilter chatFilter;
    private final HypixelAutocomplete autocomplete;
    private final HypixelLevelDisplay levelDisplay;
    private final HypixelManualSuggestions manualSuggestions;
    private final HypixelNotificationController notificationController;

    private int tickCount = 0;
    private String lastKnownAddress = "";
    private boolean lastKnownHypixel = false;

    public HypixelManager() {
        this.serverDetector = HypixelServerDetector.getInstance();
        this.safetyController = HypixelSafetyController.getInstance();
        this.chatController = HypixelChatController.getInstance();
        this.chatFilter = HypixelChatFilter.getInstance();
        this.autocomplete = HypixelAutocomplete.getInstance();
        this.levelDisplay = HypixelLevelDisplay.getInstance();
        this.manualSuggestions = HypixelManualSuggestions.getInstance();
        this.notificationController = HypixelNotificationController.getInstance();
    }

    public static synchronized HypixelManager getInstance() {
        if (instance == null) {
            instance = new HypixelManager();
        }
        return instance;
    }

    /**
     * Called on each client game tick (20 ticks/sec).
     */
    public synchronized void onTick() {
        tickCount++;

        // Periodic check every 20 ticks (1 sec)
        if (tickCount % 20 == 0) {
            checkConnectionState();
            notificationController.pruneExpired();
        }

        // Integration with BedwarsHelper: check if match ending event occurred
        checkBedwarsGameEnding();
    }

    private void checkConnectionState() {
        IHypixelDataProvider provider = HypixelDataProviderRegistry.getProvider();
        if (provider == null) return;

        if (provider.isSingleplayer()) {
            if (serverDetector.getStatus() != HypixelServerStatus.SINGLEPLAYER) {
                serverDetector.onSingleplayerJoin();
            }
            return;
        }

        String currentAddress = provider.getCurrentServerAddress();
        if (currentAddress == null || currentAddress.isEmpty()) {
            if (serverDetector.getStatus() != HypixelServerStatus.UNKNOWN_SERVER && !serverDetector.isCustomProfileActive()) {
                serverDetector.onServerDisconnect();
                onHypixelStatusChanged(false);
            }
            lastKnownAddress = "";
            return;
        }

        if (!currentAddress.equalsIgnoreCase(lastKnownAddress)) {
            lastKnownAddress = currentAddress;
            serverDetector.onServerConnect(currentAddress);

            boolean isHypixel = serverDetector.isHypixelConnected();
            if (isHypixel != lastKnownHypixel) {
                onHypixelStatusChanged(isHypixel);
                lastKnownHypixel = isHypixel;
            }
        }
    }

    private void onHypixelStatusChanged(boolean isHypixel) {
        if (isHypixel) {
            notificationController.showNotification(new HypixelNotification(
                    HypixelNotificationType.SERVER_DETECTION,
                    "Hypixel Network wykryty",
                    "Aktywny tryb: Strict Safety (Fair Play)",
                    0xFF55FF55,
                    5.0f
            ));
        }
    }

    private void checkBedwarsGameEnding() {
        try {
            BedwarsManager bw = BedwarsManager.getInstance();
            if (bw != null) {
                BedwarsGameDetector detector = bw.getGameDetector();
                if (detector != null && detector.getCurrentState() == BedwarsGameState.GAME_ENDING) {
                    if (manualSuggestions.getActiveSuggestion() == null) {
                        manualSuggestions.triggerMatchEndSuggestion();
                    }
                }
            }
        } catch (Throwable ignored) {
            // Safe containment if BedwarsHelper is not loaded
        }
    }

    /**
     * Processes incoming chat message. Returns null if message is filtered out locally.
     * Otherwise returns formatted message string.
     */
    public synchronized String processIncomingChat(String rawMessage) {
        if (rawMessage == null) return null;

        // 1. Check if suppressed by chat filter
        if (chatFilter.shouldFilter(rawMessage)) {
            return null;
        }

        // 2. Check if suppressed as Guild MOTD
        if (chatController.isGuildMotd(rawMessage)) {
            return null;
        }

        // 3. Check for match ending or starting keywords to trigger manual suggestions
        String clean = rawMessage.replaceAll("§[0-9a-fk-orA-FK-OR]", "").toLowerCase(Locale.ROOT);
        if (clean.contains("victory!") || clean.contains("zwycięstwo!") || clean.contains("game over!")) {
            manualSuggestions.triggerMatchEndSuggestion();
        } else if (clean.contains("the game has begun") || clean.contains("gra została rozpoczęta")) {
            manualSuggestions.triggerMatchStartSuggestion();
        }

        // 4. Format message (short channels, timestamp)
        return chatController.formatChatMessage(rawMessage);
    }

    public synchronized void reset() {
        serverDetector.reset();
        chatFilter.resetFilteredCount();
        levelDisplay.clearCache();
        manualSuggestions.dismiss();
        notificationController.clear();
        safetyController.clearDiagnosticLogs();
        lastKnownAddress = "";
        lastKnownHypixel = false;
        tickCount = 0;
    }

    // Subsystem Getters
    public HypixelServerDetector getServerDetector() { return serverDetector; }
    public HypixelSafetyController getSafetyController() { return safetyController; }
    public HypixelChatController getChatController() { return chatController; }
    public HypixelChatFilter getChatFilter() { return chatFilter; }
    public HypixelAutocomplete getAutocomplete() { return autocomplete; }
    public HypixelLevelDisplay getLevelDisplay() { return levelDisplay; }
    public HypixelManualSuggestions getManualSuggestions() { return manualSuggestions; }
    public HypixelNotificationController getNotificationController() { return notificationController; }
}
