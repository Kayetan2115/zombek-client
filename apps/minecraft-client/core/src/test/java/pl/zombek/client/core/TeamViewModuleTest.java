package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.hud.teamview.MarkerStyle;
import pl.zombek.client.core.modules.hud.teamview.TeamDetectionMode;
import pl.zombek.client.core.modules.hud.teamview.TeamTracker;
import pl.zombek.client.core.modules.hud.teamview.TeamViewModule;

import static org.junit.jupiter.api.Assertions.*;

public class TeamViewModuleTest {

    private TeamViewModule module;

    @BeforeEach
    public void setUp() {
        module = new TeamViewModule();
        TeamTracker.getInstance().clear();
    }

    @Test
    public void testModuleMetadata() {
        assertEquals("team_view", module.getId());
        assertEquals("Team View", module.getName());
        assertEquals(ModuleCategory.HUD, module.getCategory());
        assertFalse(module.isEnabled());
    }

    @Test
    public void testSettingGroupsAndDefaults() {
        assertNotNull(module.getGroup("general"));
        assertNotNull(module.getGroup("appearance"));
        assertNotNull(module.getGroup("visibility"));
        assertNotNull(module.getGroup("team"));
        assertNotNull(module.getGroup("integration"));

        assertEquals(TeamDetectionMode.BOTH, module.getDetectionModeSetting().getValue());
        assertTrue(module.getShowMarkersSetting().getValue());
        assertTrue(module.getShowNamesSetting().getValue());
        assertTrue(module.getShowDistanceSetting().getValue());
        assertTrue(module.getShowTeamColorSetting().getValue());

        assertEquals(MarkerStyle.ARROW_DOWN, module.getMarkerStyleSetting().getValue());
        assertEquals(64.0, module.getMaxDistanceSetting().getValue().doubleValue());
        assertFalse(module.getEnableServerIntegrationSetting().getValue());
    }
}
