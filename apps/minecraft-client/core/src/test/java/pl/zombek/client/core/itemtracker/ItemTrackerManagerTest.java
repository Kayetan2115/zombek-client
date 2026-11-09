package pl.zombek.client.core.itemtracker;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.inventory.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ItemTrackerManagerTest {

    private ItemTrackerManager manager;
    private FallbackInventoryDataProvider mockProvider;

    @BeforeEach
    public void setUp() {
        mockProvider = new FallbackInventoryDataProvider();
        mockProvider.setPlayerAvailable(true);
        InventoryDataProviderRegistry.getInstance().registerProvider(mockProvider);

        ZombekInventoryService.getInstance().resetForTesting();
        manager = ItemTrackerManager.getInstance();
        manager.resetForTesting();
        manager.startNewSession("test_world", true);
    }

    @AfterEach
    public void tearDown() {
        ZombekInventoryService.getInstance().resetForTesting();
        manager.resetForTesting();
        InventoryDataProviderRegistry.getInstance().resetForTesting();
    }

    @Test
    public void testSessionStateAndPausing() {
        ItemTrackingSession session = manager.getCurrentSession();
        assertNotNull(session);
        assertTrue(session.isActive());
        assertFalse(session.isPaused());

        manager.pauseTracking();
        assertTrue(session.isPaused());

        manager.resumeTracking();
        assertFalse(session.isPaused());
    }

    @Test
    public void testUnknownChangeConfidenceWhenSourceNotVerified() {
        // Initial inventory: 2 pearls
        List<InventoryItemData> initial = new ArrayList<>();
        initial.add(new InventoryItemData("minecraft:ender_pearl", "Ender Pearl", 2, 16, 0, InventorySlotSource.HOTBAR, "", null, false, false));
        mockProvider.setItems(initial);
        ZombekInventoryService.getInstance().getSnapshot(); // initial capture

        // Next: 4 pearls (+2 gained, but client has no verified action posted)
        List<InventoryItemData> next = new ArrayList<>();
        next.add(new InventoryItemData("minecraft:ender_pearl", "Ender Pearl", 4, 16, 0, InventorySlotSource.HOTBAR, "", null, false, false));
        mockProvider.setItems(next);
        ZombekInventoryService.getInstance().onClientTick(); // triggers change event

        List<ItemTrackEvent> events = manager.getCurrentSession().getHistory().getAllEvents();
        assertEquals(1, events.size());

        ItemTrackEvent event = events.get(0);
        assertEquals("minecraft:ender_pearl", event.getItemId());
        assertEquals(2, event.getDelta());
        assertEquals(ItemEventType.UNKNOWN_CHANGE, event.getEventType(), "Unverified change must be UNKNOWN_CHANGE!");
        assertEquals(EventConfidence.UNKNOWN, event.getConfidence(), "Confidence must be UNKNOWN and NEVER falsely CONFIRMED!");

        // Statistics check
        ItemTrackingStatistics stats = manager.getCurrentSession().getStatistics("minecraft:ender_pearl");
        assertNotNull(stats);
        assertEquals(2, stats.getTotalUnknownChanges());
        assertEquals(0, stats.getConfirmedCollected());
    }

    @Test
    public void testVerifiedActionPromotedToConfirmed() {
        // Initial inventory: 4 apples
        List<InventoryItemData> initial = new ArrayList<>();
        initial.add(new InventoryItemData("minecraft:golden_apple", "Golden Apple", 4, 64, 0, InventorySlotSource.HOTBAR, "", null, false, true));
        mockProvider.setItems(initial);
        ZombekInventoryService.getInstance().getSnapshot();

        // Player eats a golden apple: verified action posted
        manager.recordVerifiedAction("minecraft:golden_apple", -1, ItemEventType.CONSUMED);

        // Next: inventory count dropped to 3
        List<InventoryItemData> next = new ArrayList<>();
        next.add(new InventoryItemData("minecraft:golden_apple", "Golden Apple", 3, 64, 0, InventorySlotSource.HOTBAR, "", null, false, true));
        mockProvider.setItems(next);
        ZombekInventoryService.getInstance().onClientTick();

        List<ItemTrackEvent> events = manager.getCurrentSession().getHistory().getAllEvents();
        assertEquals(1, events.size());

        ItemTrackEvent event = events.get(0);
        assertEquals("minecraft:golden_apple", event.getItemId());
        assertEquals(-1, event.getDelta());
        assertEquals(ItemEventType.CONSUMED, event.getEventType());
        assertEquals(EventConfidence.CONFIRMED, event.getConfidence(), "Verified action must promote event to CONFIRMED!");

        ItemTrackingStatistics stats = manager.getCurrentSession().getStatistics("minecraft:golden_apple");
        assertNotNull(stats);
        assertEquals(1, stats.getTotalConsumed());
        assertEquals(1, stats.getConfirmedConsumed());
    }

    @Test
    public void testInternalTransferDoesNotEmitGainOrLoss() {
        // Initial: slot 0 has 16 obsidian
        List<InventoryItemData> initial = new ArrayList<>();
        initial.add(new InventoryItemData("minecraft:obsidian", "Obsidian", 16, 64, 0, InventorySlotSource.HOTBAR, "", null, true, false));
        mockProvider.setItems(initial);
        ZombekInventoryService.getInstance().getSnapshot();

        // Move to slot 15
        List<InventoryItemData> moved = new ArrayList<>();
        moved.add(new InventoryItemData("minecraft:obsidian", "Obsidian", 16, 64, 15, InventorySlotSource.MAIN_INVENTORY, "", null, true, false));
        mockProvider.setItems(moved);
        ZombekInventoryService.getInstance().onClientTick();

        List<ItemTrackEvent> events = manager.getCurrentSession().getHistory().getAllEvents();
        assertEquals(0, events.size(), "Internal slot movement must not generate gain or loss events!");
    }

    @Test
    public void testHistoryExportToJson() {
        // Initial baseline capture
        mockProvider.setItems(new ArrayList<>());
        ZombekInventoryService.getInstance().getSnapshot();

        manager.recordVerifiedAction("minecraft:arrow", 32, ItemEventType.COLLECTED);

        List<InventoryItemData> items = new ArrayList<>();
        items.add(new InventoryItemData("minecraft:arrow", "Arrow", 32, 64, 0, InventorySlotSource.HOTBAR, "", null, false, false));
        mockProvider.setItems(items);
        ZombekInventoryService.getInstance().onClientTick();

        String json = manager.getCurrentSession().getHistory().exportToJson();
        assertNotNull(json);
        assertTrue(json.contains("minecraft:arrow"));
        assertTrue(json.contains("COLLECTED"));
        assertTrue(json.contains("CONFIRMED"));
    }
}
