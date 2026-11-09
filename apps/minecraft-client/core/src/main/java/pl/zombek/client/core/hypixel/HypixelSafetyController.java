package pl.zombek.client.core.hypixel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Central authority enforcing Hypixel Fair Play safety policies.
 * Strictly prevents the activation of automated gameplay, network modification,
 * or unverified features when connected to Hypixel Network.
 */
public class HypixelSafetyController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HypixelSafetyController.class);
    private static HypixelSafetyController instance;

    public static final String UNOFFICIAL_DISCLAIMER = "Unofficial client modification. Use at your own risk.";

    private SafetyPreset currentPreset = SafetyPreset.STRICT_SAFETY;
    private final List<String> diagnosticLogs = new ArrayList<>();
    private static final int MAX_LOGS = 50;

    public HypixelSafetyController() {}

    public static synchronized HypixelSafetyController getInstance() {
        if (instance == null) {
            instance = new HypixelSafetyController();
        }
        return instance;
    }

    /**
     * Checks if a feature belonging to the specified safety category is permitted
     * under the current safety preset and server environment.
     */
    public synchronized boolean isFeatureAllowed(SafetyCategory category) {
        if (category == null) {
            logDiagnostic("Blocked feature with NULL safety category.");
            return false;
        }

        boolean onHypixel = HypixelServerDetector.getInstance().isHypixelConnected();

        // 1. Strict gameplay automation & network manipulation are ALWAYS blocked on all servers
        if (category == SafetyCategory.GAMEPLAY_AUTOMATION || category == SafetyCategory.NETWORK_MODIFICATION) {
            logDiagnostic("Blocked prohibited automation/network category: " + category.name());
            return false;
        }

        // 2. Unknown category is blocked on Hypixel and in Strict Safety
        if (category == SafetyCategory.UNKNOWN) {
            logDiagnostic("Blocked UNKNOWN safety category.");
            return false;
        }

        // 3. On Hypixel: only verified safe categories allowed
        if (onHypixel) {
            if (currentPreset == SafetyPreset.STRICT_SAFETY || currentPreset == SafetyPreset.CUSTOM) {
                boolean allowed = category.isAllowedOnHypixel();
                if (!allowed) {
                    logDiagnostic("Blocked non-Hypixel compliant category [" + category.name() + "] under preset " + currentPreset.name());
                }
                return allowed;
            }
        }

        // 4. Other servers preset still rejects gameplay automation (already checked above)
        return true;
    }

    /**
     * Returns an explicit explanation why a category is disallowed.
     */
    public String getBlockReason(SafetyCategory category) {
        if (category == null) return "Brak zadeklarowanej kategorii bezpieczeństwa.";
        switch (category) {
            case GAMEPLAY_AUTOMATION:
                return "Zablokowano: Automatyzacja działań gracza (AutoGG, AutoTip, makra) jest surowo wzbroniona przez zasady serwera.";
            case NETWORK_MODIFICATION:
                return "Zablokowano: Modyfikacja pakietów sieciowych lub odpytywanie API w trakcie gry jest niedozwolone.";
            case UNKNOWN:
                return "Zablokowano: Funkcja o niesprecyzowanym statusie bezpieczeństwa.";
            default:
                if (HypixelServerDetector.getInstance().isHypixelConnected() && !category.isAllowedOnHypixel()) {
                    return "Zablokowano: Funkcja niedozwolona w trybie Strict Safety na serwerze Hypixel.";
                }
                return "Funkcja dozwolona.";
        }
    }

    public synchronized void logDiagnostic(String message) {
        LOGGER.warn("[HypixelSafetyController] {}", message);
        diagnosticLogs.add(System.currentTimeMillis() + " - " + message);
        while (diagnosticLogs.size() > MAX_LOGS) {
            diagnosticLogs.remove(0);
        }
    }

    public synchronized SafetyPreset getPreset() {
        return currentPreset;
    }

    public synchronized void setPreset(SafetyPreset preset) {
        if (preset != null) {
            this.currentPreset = preset;
            LOGGER.info("[HypixelSafetyController] Safety preset switched to: {}", preset.name());
        }
    }

    public synchronized List<String> getDiagnosticLogs() {
        return Collections.unmodifiableList(new ArrayList<>(diagnosticLogs));
    }

    public synchronized void clearDiagnosticLogs() {
        diagnosticLogs.clear();
    }
}
