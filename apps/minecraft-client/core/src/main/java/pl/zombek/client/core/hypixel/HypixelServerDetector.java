package pl.zombek.client.core.hypixel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Detects whether the Minecraft client is currently connected to the Hypixel Network.
 * Operates purely on locally available connection metadata (server address string)
 * without performing DNS queries, ping tests, or chat command probing.
 */
public class HypixelServerDetector {
    private static final Logger LOGGER = LoggerFactory.getLogger(HypixelServerDetector.class);
    private static HypixelServerDetector instance;

    private HypixelServerStatus currentStatus = HypixelServerStatus.UNKNOWN_SERVER;
    private String currentServerAddress = "";
    private boolean customProfileActive = false;
    private long connectionTime = 0L;

    public HypixelServerDetector() {}

    public static synchronized HypixelServerDetector getInstance() {
        if (instance == null) {
            instance = new HypixelServerDetector();
        }
        return instance;
    }

    /**
     * Called when the client connects to a server.
     */
    public synchronized void onServerConnect(String rawAddress) {
        if (rawAddress == null || rawAddress.trim().isEmpty()) {
            this.currentServerAddress = "";
            this.currentStatus = HypixelServerStatus.UNKNOWN_SERVER;
            return;
        }

        this.currentServerAddress = rawAddress.trim();
        this.connectionTime = System.currentTimeMillis();

        if (customProfileActive) {
            this.currentStatus = HypixelServerStatus.CUSTOM_HYPIXEL_PROFILE;
            LOGGER.info("[HypixelServerDetector] Connected with manual custom Hypixel profile: {}", currentServerAddress);
            return;
        }

        if (isHypixelAddress(this.currentServerAddress)) {
            this.currentStatus = HypixelServerStatus.HYPIXEL_DETECTED;
            LOGGER.info("[HypixelServerDetector] Hypixel Network detected via address: {}", currentServerAddress);
        } else {
            this.currentStatus = HypixelServerStatus.OTHER_SERVER;
            LOGGER.info("[HypixelServerDetector] Connected to external server: {}", currentServerAddress);
        }
    }

    /**
     * Called when the client enters a singleplayer world.
     */
    public synchronized void onSingleplayerJoin() {
        this.currentServerAddress = "local_singleplayer";
        this.connectionTime = System.currentTimeMillis();
        this.currentStatus = HypixelServerStatus.SINGLEPLAYER;
        LOGGER.info("[HypixelServerDetector] Singleplayer world entered.");
    }

    /**
     * Called when the client disconnects or connection is lost.
     */
    public synchronized void onServerDisconnect() {
        LOGGER.info("[HypixelServerDetector] Disconnected from server (was: {}). Resetting state.", currentServerAddress);
        this.currentServerAddress = "";
        this.connectionTime = 0L;
        if (customProfileActive) {
            this.currentStatus = HypixelServerStatus.CUSTOM_HYPIXEL_PROFILE;
        } else {
            this.currentStatus = HypixelServerStatus.UNKNOWN_SERVER;
        }
    }

    /**
     * Called on world/instance dimension transition within the same server.
     */
    public synchronized void onWorldChange() {
        // Preserves server status, updates timestamp
        if (currentStatus == HypixelServerStatus.UNKNOWN_SERVER && !currentServerAddress.isEmpty()) {
            onServerConnect(currentServerAddress);
        }
    }

    /**
     * Checks if the given address string matches Hypixel hostnames or subdomains.
     */
    public static boolean isHypixelAddress(String address) {
        if (address == null) return false;
        String clean = address.toLowerCase(Locale.ROOT).trim();
        // Remove port if present: e.g. "mc.hypixel.net:25565" -> "mc.hypixel.net"
        int colonIdx = clean.indexOf(':');
        if (colonIdx > 0) {
            clean = clean.substring(0, colonIdx);
        }

        return clean.equals("hypixel.net")
                || clean.equals("mc.hypixel.net")
                || clean.endsWith(".hypixel.net");
    }

    public synchronized void setCustomProfileActive(boolean active) {
        this.customProfileActive = active;
        if (active) {
            this.currentStatus = HypixelServerStatus.CUSTOM_HYPIXEL_PROFILE;
        } else if (currentServerAddress.isEmpty()) {
            this.currentStatus = HypixelServerStatus.UNKNOWN_SERVER;
        } else if (currentServerAddress.equals("local_singleplayer")) {
            this.currentStatus = HypixelServerStatus.SINGLEPLAYER;
        } else {
            this.currentStatus = isHypixelAddress(currentServerAddress)
                    ? HypixelServerStatus.HYPIXEL_DETECTED
                    : HypixelServerStatus.OTHER_SERVER;
        }
    }

    public synchronized boolean isHypixelConnected() {
        return currentStatus.isHypixelEnvironment();
    }

    public synchronized HypixelServerStatus getStatus() {
        return currentStatus;
    }

    public synchronized String getCurrentServerAddress() {
        return currentServerAddress;
    }

    public synchronized boolean isCustomProfileActive() {
        return customProfileActive;
    }

    public synchronized long getConnectionDurationSeconds() {
        if (connectionTime <= 0L) return 0L;
        return Math.max(0L, (System.currentTimeMillis() - connectionTime) / 1000L);
    }

    public synchronized void reset() {
        this.currentStatus = HypixelServerStatus.UNKNOWN_SERVER;
        this.currentServerAddress = "";
        this.customProfileActive = false;
        this.connectionTime = 0L;
    }
}
