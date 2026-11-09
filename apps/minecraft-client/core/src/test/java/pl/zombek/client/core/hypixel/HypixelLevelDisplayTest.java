package pl.zombek.client.core.hypixel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HypixelLevelDisplayTest {
    private HypixelLevelDisplay levelDisplay;
    private HypixelServerDetector detector;
    private HypixelSafetyController safety;

    @BeforeEach
    void setUp() {
        levelDisplay = new HypixelLevelDisplay();
        detector = HypixelServerDetector.getInstance();
        detector.reset();
        safety = HypixelSafetyController.getInstance();
        safety.setPreset(SafetyPreset.STRICT_SAFETY);
    }

    @Test
    void testExtractLevelFromText() {
        assertEquals(120, levelDisplay.extractLevelFromText("§6[120★] §fSteve"));
        assertEquals(45, levelDisplay.extractLevelFromText("[45✫] Alex"));
        assertEquals(80, levelDisplay.extractLevelFromText("[80] Player"));
        assertNull(levelDisplay.extractLevelFromText("RegularPlayer"));
        assertNull(levelDisplay.extractLevelFromText(null));
    }

    @Test
    void testLevelDisplayLockedOnHypixelUnderStrictSafety() {
        detector.onServerConnect("mc.hypixel.net");
        assertTrue(detector.isHypixelConnected());
        safety.setPreset(SafetyPreset.STRICT_SAFETY);

        assertFalse(levelDisplay.isLevelDisplayPermitted());
        levelDisplay.setEnabled(true);
        assertFalse(levelDisplay.isEnabled()); // isEnabled() checks isLevelDisplayPermitted()
    }

    @Test
    void testLevelDisplayAllowedOnOtherServers() {
        detector.onServerConnect("pvp.land");
        assertFalse(detector.isHypixelConnected());

        assertTrue(levelDisplay.isLevelDisplayPermitted());
        levelDisplay.setEnabled(true);
        assertTrue(levelDisplay.isEnabled());
    }

    @Test
    void testPlayerLevelCaching() {
        levelDisplay.recordPlayerLevel("Steve", 150);
        assertEquals(150, levelDisplay.getPlayerLevel("steve"));
        assertNull(levelDisplay.getPlayerLevel("Unknown"));

        levelDisplay.clearCache();
        assertNull(levelDisplay.getPlayerLevel("steve"));
    }
}
