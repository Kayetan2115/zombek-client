package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class HudGroupManagerTest {

    private HudGroupManager groupManager;
    private HudLayout layout;

    @BeforeEach
    public void setUp() {
        groupManager = HudGroupManager.getInstance();
        layout = new HudLayout("test", "Układ Testowy");
        layout.setComponentState(new HudComponentState("fps", 10, 10, 40, 20));
        layout.setComponentState(new HudComponentState("ping", 10, 35, 40, 20));
        layout.setComponentState(new HudComponentState("cps", 10, 60, 40, 20));
    }

    @Test
    public void testCreateGroup() {
        HudGroup group = groupManager.createGroup(layout, "pvp_info", "Info PvP", Arrays.asList("fps", "ping"));
        assertNotNull(group);
        assertEquals("pvp_info", group.getGroupId());
        assertEquals("Info PvP", group.getName());
        assertEquals(2, group.getComponentIds().size());

        // Component states should have groupId updated
        assertEquals("pvp_info", layout.getComponentState("fps").getGroupId());
        assertEquals("pvp_info", layout.getComponentState("ping").getGroupId());
        assertNull(layout.getComponentState("cps").getGroupId());
    }

    @Test
    public void testRenameGroup() {
        groupManager.createGroup(layout, "grp1", "Nazwa 1", Collections.singleton("fps"));
        boolean renamed = groupManager.renameGroup(layout, "grp1", "Nowa Nazwa");
        assertTrue(renamed);
        assertEquals("Nowa Nazwa", layout.getGroup("grp1").getName());
    }

    @Test
    public void testDeleteGroupUngroupsComponents() {
        groupManager.createGroup(layout, "grp1", "Do Usunięcia", Arrays.asList("fps", "ping"));
        assertEquals("grp1", layout.getComponentState("fps").getGroupId());

        boolean deleted = groupManager.deleteGroup(layout, "grp1");
        assertTrue(deleted);
        assertNull(layout.getGroup("grp1"));
        assertNull(layout.getComponentState("fps").getGroupId(), "Component should be detached, not deleted");
        assertNull(layout.getComponentState("ping").getGroupId(), "Component should be detached, not deleted");
    }

    @Test
    public void testCalculateGroupBounds() {
        HudGroup group = groupManager.createGroup(layout, "bounds_grp", "Grupa", Arrays.asList("fps", "ping"));
        layout.getComponentState("fps").setVisible(true);
        layout.getComponentState("ping").setVisible(true);

        HudGroupManager.GroupBounds bounds = groupManager.calculateGroupBounds(group, layout, 960, 540);
        assertNotNull(bounds);
        assertTrue(bounds.getWidth() > 0);
        assertTrue(bounds.getHeight() > 0);
        assertTrue(bounds.contains(15, 15));
        assertTrue(bounds.contains(15, 40));
    }
}
