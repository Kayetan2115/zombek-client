package pl.zombek.client.core.hypixel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class HypixelManualSuggestionsTest {
    private HypixelManualSuggestions suggestions;
    private final AtomicReference<String> clipboardHolder = new AtomicReference<>(null);

    @BeforeEach
    void setUp() {
        suggestions = new HypixelManualSuggestions();
        suggestions.setEnabled(true);
        suggestions.setSuggestGg(true);
        suggestions.setSuggestGl(true);
        suggestions.dismiss();
        clipboardHolder.set(null);

        HypixelDataProviderRegistry.setProvider(new IHypixelDataProvider() {
            @Override public String getCurrentServerAddress() { return "mc.hypixel.net"; }
            @Override public boolean isSingleplayer() { return false; }
            @Override public void copyToClipboard(String text) { clipboardHolder.set(text); }
            @Override public void playSound(String soundId, float volume, float pitch) {}
            @Override public boolean isPlayerInGame() { return true; }
        });
    }

    @Test
    void testTriggerMatchEndGgSuggestion() {
        assertNull(suggestions.getActiveSuggestion());

        suggestions.triggerMatchEndSuggestion();
        ManualSuggestion active = suggestions.getActiveSuggestion();
        assertNotNull(active);
        assertEquals("GG", active.getSuggestedText());
        assertEquals("Koniec meczu", active.getTitle());
    }

    @Test
    void testTriggerMatchStartGlSuggestion() {
        suggestions.triggerMatchStartSuggestion();
        ManualSuggestion active = suggestions.getActiveSuggestion();
        assertNotNull(active);
        assertEquals("GL HF", active.getSuggestedText());
    }

    @Test
    void testCopyToClipboardBehavior() {
        suggestions.triggerMatchEndSuggestion();
        assertNotNull(suggestions.getActiveSuggestion());

        boolean success = suggestions.copyActiveSuggestionToClipboard();
        assertTrue(success);
        assertEquals("GG", clipboardHolder.get());
        assertNull(suggestions.getActiveSuggestion()); // Dismissed after copying
    }

    @Test
    void testDismissSuggestion() {
        suggestions.triggerMatchEndSuggestion();
        assertNotNull(suggestions.getActiveSuggestion());

        suggestions.dismiss();
        assertNull(suggestions.getActiveSuggestion());
    }

    @Test
    void testDisabledSuggestions() {
        suggestions.setEnabled(false);
        suggestions.triggerMatchEndSuggestion();
        assertNull(suggestions.getActiveSuggestion());
    }
}
