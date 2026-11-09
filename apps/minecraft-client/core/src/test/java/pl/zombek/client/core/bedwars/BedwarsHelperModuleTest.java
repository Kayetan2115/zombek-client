package pl.zombek.client.core.bedwars;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.TestHudRenderer;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.hud.BedwarsHelperModule;

import static org.junit.jupiter.api.Assertions.*;

public class BedwarsHelperModuleTest {
    private BedwarsHelperModule module;

    @BeforeEach
    void setUp() {
        module = new BedwarsHelperModule();
        BedwarsManager.getInstance().reset();
    }

    @Test
    void testModuleMetadata() {
        assertEquals("bedwars_helper", module.getId());
        assertEquals("Bedwars Helper", module.getName());
        assertEquals(ModuleCategory.HUD, module.getCategory());
        assertEquals("bedwars_helper", module.getElementId());
    }

    @Test
    void testAllNineSettingGroupsRegistered() {
        assertNotNull(module.getGroupGeneral());
        assertNotNull(module.getGroupGameInfo());
        assertNotNull(module.getGroupTeamBoard());
        assertNotNull(module.getGroupMyTeam());
        assertNotNull(module.getGroupSessionStats());
        assertNotNull(module.getGroupEventFeed());
        assertNotNull(module.getGroupTimers());
        assertNotNull(module.getGroupParser());
        assertNotNull(module.getGroupAppearance());
    }

    @Test
    void testDefaultSettings() {
        assertTrue(module.getAutoDetectBedwarsSetting().getValue());
        assertTrue(module.getAutoEnableOverlaySetting().getValue());
        assertFalse(module.getRequireHighConfidenceSetting().getValue());
        assertFalse(module.getManualModeSetting().getValue());
        assertEquals(BedwarsLayout.COMPACT, module.getLayoutStyleSetting().getValue());
        assertEquals(TeamBoardLayout.COMPACT, module.getTeamLayoutSetting().getValue());
    }

    @Test
    void testVisibilityEvaluation() {
        module.setEnabled(true);

        // Outside bedwars without manual mode -> invisible
        BedwarsManager.getInstance().getGameDetector().setCurrentState(BedwarsGameState.NOT_IN_BEDWARS);
        assertFalse(module.isVisible());

        // With manual mode active -> visible
        module.getManualModeSetting().setValue(true);
        assertTrue(module.isVisible());

        // When bedwars match is active -> visible
        module.getManualModeSetting().setValue(false);
        BedwarsManager.getInstance().getGameDetector().setCurrentState(BedwarsGameState.BEDWARS_ACTIVE);
        BedwarsManager.getInstance().getGameDetector().setConfidence(DetectionConfidence.HIGH);
        assertTrue(module.isVisible());

        // When module is disabled -> invisible
        module.setEnabled(false);
        assertFalse(module.isVisible());
    }

    @Test
    void testRenderDoesNotThrowAndCallsRenderer() {
        module.setEnabled(true);
        module.getManualModeSetting().setValue(true);

        TestHudRenderer renderer = new TestHudRenderer(1920, 1080);
        assertDoesNotThrow(() -> module.render(renderer));

        assertTrue(renderer.fillCallCount > 0);
        assertTrue(renderer.drawBorderCallCount > 0);
        assertTrue(renderer.drawTextCallCount > 0);
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("BED WARS")));
    }
}
