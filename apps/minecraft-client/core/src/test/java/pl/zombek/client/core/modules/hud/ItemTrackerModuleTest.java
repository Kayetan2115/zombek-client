package pl.zombek.client.core.modules.hud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.itemtracker.*;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.hud.itemtracker.ItemTrackerLayout;
import pl.zombek.client.core.modules.hud.itemtracker.ItemTrackerModule;
import pl.zombek.client.core.settings.BooleanSetting;
import pl.zombek.client.core.settings.EnumSetting;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ItemTrackerModuleTest {

    private ItemTrackerModule module;
    private ItemTrackerManager manager;

    @BeforeEach
    public void setUp() {
        module = new ItemTrackerModule();
        module.setEnabled(true);

        manager = ItemTrackerManager.getInstance();
        manager.startNewSession("test_world", false);
    }

    @Test
    public void testMetadataAndCategory() {
        assertEquals("item_tracker", module.getId());
        assertEquals("Item Tracker", module.getName());
        assertEquals(ModuleCategory.HUD, module.getCategory());
        assertTrue(module.isVisible());
        assertEquals(6, module.getSettingGroups().size()); // general, events, confidence, display, appearance, layout
    }

    @Test
    public void testPreviewFallbackWhenWorldNotLoaded() {
        List<String> lines = module.buildDisplayLines(null, false);
        assertFalse(lines.isEmpty());
        assertTrue(lines.get(0).contains("[PREVIEW]"));
    }

    @Test
    public void testFeedFilteringByEventType() {
        ItemTrackingSession session = manager.getCurrentSession();
        assertNotNull(session);

        // Record a collected event and a dropped event
        session.recordEvent(new ItemTrackEvent(
                1000L, "minecraft:diamond", "Diamond", 5,
                ItemEventType.COLLECTED, EventConfidence.CONFIRMED, null
        ));
        session.recordEvent(new ItemTrackEvent(
                2000L, "minecraft:dirt", "Dirt", -16,
                ItemEventType.DROPPED, EventConfidence.CONFIRMED, null
        ));

        List<String> linesAll = module.buildDisplayLines(manager, true);
        // Header + 2 events
        assertEquals(3, linesAll.size());

        // Now disable tracking dropped items
        BooleanSetting trackDropped = (BooleanSetting) module.getSetting("track_dropped");
        trackDropped.setValue(false);

        List<String> linesFiltered = module.buildDisplayLines(manager, true);
        // Header + only diamond collected event
        assertEquals(2, linesFiltered.size());
        assertTrue(linesFiltered.get(1).contains("Diamond"));
        assertFalse(linesFiltered.get(1).contains("Dirt"));
    }

    @Test
    public void testStatisticsLayout() {
        @SuppressWarnings("unchecked")
        EnumSetting<ItemTrackerLayout> layoutSetting = (EnumSetting<ItemTrackerLayout>) module.getSetting("layout");
        layoutSetting.setValue(ItemTrackerLayout.STATISTICS);

        ItemTrackingSession session = manager.getCurrentSession();
        assertNotNull(session);

        session.recordEvent(new ItemTrackEvent(
                1000L, "minecraft:golden_apple", "Golden Apple", 3,
                ItemEventType.COLLECTED, EventConfidence.CONFIRMED, null
        ));
        session.recordEvent(new ItemTrackEvent(
                2000L, "minecraft:golden_apple", "Golden Apple", -1,
                ItemEventType.CONSUMED, EventConfidence.CONFIRMED, null
        ));

        List<String> lines = module.buildDisplayLines(manager, true);
        // Header + Golden Apple stats
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("Stats"));
        assertTrue(lines.get(1).contains("Golden Apple"));
        assertTrue(lines.get(1).contains("+3"));
        assertTrue(lines.get(1).contains("-1"));
    }

    @Test
    public void testRestoreDefaults() {
        BooleanSetting trackDropped = (BooleanSetting) module.getSetting("track_dropped");
        trackDropped.setValue(false);
        assertFalse(trackDropped.getValue());

        trackDropped.reset();
        assertTrue(trackDropped.getValue());
    }
}
