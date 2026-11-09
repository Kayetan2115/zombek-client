package pl.zombek.client.core.hypixel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.TestHudRenderer;
import pl.zombek.client.core.modules.hud.BedwarsHelperModule;
import pl.zombek.client.core.modules.hypixel.HypixelModsModule;
import pl.zombek.client.core.modules.pvp.KeystrokesModule;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class HypixelIntegrationTest {
    private HypixelManager manager;
    private final AtomicReference<String> clipboardHolder = new AtomicReference<>(null);

    @BeforeEach
    void setUp() {
        manager = HypixelManager.getInstance();
        manager.reset();
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
    void testEndToEndHypixelFlow() {
        // 1. Connection check on tick
        for (int i = 0; i < 20; i++) {
            manager.onTick();
        }
        assertTrue(manager.getServerDetector().isHypixelConnected());
        assertEquals(HypixelServerStatus.HYPIXEL_DETECTED, manager.getServerDetector().getStatus());
        assertEquals(SafetyPreset.STRICT_SAFETY, manager.getSafetyController().getPreset());

        // 2. Incoming chat formatting
        String formatted = manager.processIncomingChat("Party > Steve: Let's play 4v4!");
        assertEquals("[P] Steve: Let's play 4v4!", formatted);

        // 3. Spam filtering
        String filtered = manager.processIncomingChat("Alex found a 5-Star Mystery Box in the lobby!");
        assertNull(filtered); // Filtered out

        // 4. Match ending triggers manual GG suggestion
        manager.processIncomingChat("VICTORY!");
        ManualSuggestion active = manager.getManualSuggestions().getActiveSuggestion();
        assertNotNull(active);
        assertEquals("GG", active.getSuggestedText());

        // 5. Copy to clipboard
        boolean copied = manager.getManualSuggestions().copyActiveSuggestionToClipboard();
        assertTrue(copied);
        assertEquals("GG", clipboardHolder.get());
    }

    @Test
    void testCoexistenceWithBedwarsHelperAndHudModules() {
        HypixelModsModule hypixel = new HypixelModsModule();
        BedwarsHelperModule bedwars = new BedwarsHelperModule();
        KeystrokesModule keystrokes = new KeystrokesModule();

        hypixel.setEnabled(true);
        bedwars.setEnabled(true);
        keystrokes.setEnabled(true);

        TestHudRenderer renderer = new TestHudRenderer(1920, 1080);

        assertDoesNotThrow(() -> {
            hypixel.render(renderer);
            bedwars.render(renderer);
            keystrokes.render(renderer);
        });
    }
}
