package pl.zombek.client.core.hypixel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Manages manual social suggestions (GG, GL, customized templates).
 * Strictly compliant with Fair Play rules: NEVER types into chat, NEVER sends
 * commands, and NEVER simulates user keypresses.
 * Offers a discrete HUD card and one-click clipboard copying.
 */
public class HypixelManualSuggestions {
    private static final Logger LOGGER = LoggerFactory.getLogger(HypixelManualSuggestions.class);
    private static HypixelManualSuggestions instance;

    private boolean enabled = true;
    private boolean suggestGg = true;
    private boolean suggestGl = true;
    private String customTemplates = "GG!;Dobra gra!;Dzięki za mecz!";
    private float notificationDuration = 6.0f;
    private boolean copyToClipboardButton = true;

    private ManualSuggestion activeSuggestion = null;

    public HypixelManualSuggestions() {}

    public static synchronized HypixelManualSuggestions getInstance() {
        if (instance == null) {
            instance = new HypixelManualSuggestions();
        }
        return instance;
    }

    /**
     * Called when a match concludes (e.g. from BedwarsHelper or chat victory announcement).
     */
    public synchronized void triggerMatchEndSuggestion() {
        if (!enabled || !suggestGg) {
            return;
        }

        this.activeSuggestion = new ManualSuggestion(
                "match_end_gg",
                "Koniec meczu",
                "GG",
                notificationDuration
        );

        LOGGER.info("[HypixelManualSuggestions] Match end detected. Formed manual suggestion: 'GG'");

        // Forward to notification controller as well
        HypixelNotificationController.getInstance().showNotification(new HypixelNotification(
                HypixelNotificationType.MANUAL_SUGGESTION,
                "Sugestia: GG",
                "Naciśnij [Kopiuj] aby wkleić na czat",
                0xFF55FF55,
                notificationDuration
        ));
    }

    /**
     * Called when a match commences.
     */
    public synchronized void triggerMatchStartSuggestion() {
        if (!enabled || !suggestGl) {
            return;
        }

        this.activeSuggestion = new ManualSuggestion(
                "match_start_gl",
                "Start gry",
                "GL HF",
                notificationDuration
        );

        LOGGER.info("[HypixelManualSuggestions] Match start detected. Formed manual suggestion: 'GL HF'");
    }

    /**
     * Copies the active suggestion's text into system clipboard without typing or sending.
     */
    public synchronized boolean copyActiveSuggestionToClipboard() {
        if (activeSuggestion == null || activeSuggestion.isExpired()) {
            return false;
        }

        String text = activeSuggestion.getSuggestedText();
        HypixelDataProviderRegistry.getProvider().copyToClipboard(text);
        LOGGER.info("[HypixelManualSuggestions] Suggested text copied to clipboard: '{}'", text);
        dismiss();
        return true;
    }

    public synchronized void dismiss() {
        this.activeSuggestion = null;
    }

    public synchronized ManualSuggestion getActiveSuggestion() {
        if (activeSuggestion != null && activeSuggestion.isExpired()) {
            activeSuggestion = null;
        }
        return activeSuggestion;
    }

    public List<String> getParsedCustomTemplates() {
        if (customTemplates == null || customTemplates.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(customTemplates.split(";"));
    }

    // Getters and Setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isSuggestGg() { return suggestGg; }
    public void setSuggestGg(boolean suggestGg) { this.suggestGg = suggestGg; }
    public boolean isSuggestGl() { return suggestGl; }
    public void setSuggestGl(boolean suggestGl) { this.suggestGl = suggestGl; }
    public String getCustomTemplates() { return customTemplates; }
    public void setCustomTemplates(String templates) { this.customTemplates = templates; }
    public float getNotificationDuration() { return notificationDuration; }
    public void setNotificationDuration(float duration) { this.notificationDuration = Math.max(1.0f, Math.min(15.0f, duration)); }
    public boolean isCopyToClipboardButton() { return copyToClipboardButton; }
    public void setCopyToClipboardButton(boolean copyButton) { this.copyToClipboardButton = copyButton; }
}
