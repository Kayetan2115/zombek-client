package pl.zombek.client.core.inventory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class InventorySnapshotServiceTest {

    private InventorySnapshotService service;

    @BeforeEach
    public void setUp() {
        service = new InventorySnapshotService();
    }

    @Test
    public void testSingleAndMultipleStackCounting() {
        List<InventoryItemData> items = new ArrayList<>();
        items.add(new InventoryItemData("minecraft:ender_pearl", "Ender Pearl", 16, 16, 0, InventorySlotSource.HOTBAR, "", null, false, false));
        items.add(new InventoryItemData("minecraft:ender_pearl", "Ender Pearl", 8, 16, 1, InventorySlotSource.HOTBAR, "", null, false, false));
        items.add(new InventoryItemData("minecraft:golden_apple", "Golden Apple", 4, 64, 9, InventorySlotSource.MAIN_INVENTORY, "", null, false, true));

        InventorySnapshot snapshot = new InventorySnapshot(System.currentTimeMillis(), "world_1", items);

        Set<InventorySlotSource> allSources = EnumSet.allOf(InventorySlotSource.class);
        assertEquals(24, snapshot.getItemCount("minecraft:ender_pearl", allSources));
        assertEquals(4, snapshot.getItemCount("minecraft:golden_apple", allSources));
        assertEquals(1, snapshot.getFullStacksCount("minecraft:ender_pearl", allSources)); // 24 / 16 = 1 full stack
    }

    @Test
    public void testSourceFiltering() {
        List<InventoryItemData> items = new ArrayList<>();
        items.add(new InventoryItemData("minecraft:arrow", "Arrow", 64, 64, 0, InventorySlotSource.HOTBAR, "", null, false, false));
        items.add(new InventoryItemData("minecraft:arrow", "Arrow", 64, 64, 9, InventorySlotSource.MAIN_INVENTORY, "", null, false, false));
        items.add(new InventoryItemData("minecraft:shield", "Shield", 1, 1, 40, InventorySlotSource.OFFHAND, "", null, false, false));
        items.add(new InventoryItemData("minecraft:diamond_chestplate", "Chestplate", 1, 1, 38, InventorySlotSource.ARMOR, "", null, false, false));

        InventorySnapshot snapshot = new InventorySnapshot(System.currentTimeMillis(), "world_1", items);

        // Only hotbar and offhand
        Set<InventorySlotSource> sources = EnumSet.of(InventorySlotSource.HOTBAR, InventorySlotSource.OFFHAND);
        assertEquals(64, snapshot.getItemCount("minecraft:arrow", sources));
        assertEquals(1, snapshot.getItemCount("minecraft:shield", sources));
        assertEquals(0, snapshot.getItemCount("minecraft:diamond_chestplate", sources));
    }

    @Test
    public void testSlotTransferDetectedWithoutFalseNetChange() {
        // Prev: 16 pearls in slot 0 (Hotbar)
        List<InventoryItemData> prevItems = new ArrayList<>();
        prevItems.add(new InventoryItemData("minecraft:ender_pearl", "Ender Pearl", 16, 16, 0, InventorySlotSource.HOTBAR, "", null, false, false));
        InventorySnapshot prevSnapshot = new InventorySnapshot(1000L, "world_1", prevItems);

        // Curr: moved same 16 pearls to slot 15 (Main Inventory)
        List<InventoryItemData> currItems = new ArrayList<>();
        currItems.add(new InventoryItemData("minecraft:ender_pearl", "Ender Pearl", 16, 16, 15, InventorySlotSource.MAIN_INVENTORY, "", null, false, false));
        InventorySnapshot currSnapshot = new InventorySnapshot(2000L, "world_1", currItems);

        Set<InventorySlotSource> sources = EnumSet.of(InventorySlotSource.HOTBAR, InventorySlotSource.MAIN_INVENTORY);
        SnapshotDiffResult diff = service.compare(prevSnapshot, currSnapshot, sources);

        assertFalse(diff.hasNetChanges(), "Total count did not change; must not report net change!");
        assertTrue(diff.isPureTransferOnly(), "Should be recognized as a pure internal transfer!");
        assertEquals(1, diff.getSlotTransfers().size());
        assertEquals("minecraft:ender_pearl", diff.getSlotTransfers().get(0).getItemId());
        assertEquals(0, diff.getSlotTransfers().get(0).getFromSlot());
        assertEquals(15, diff.getSlotTransfers().get(0).getToSlot());
    }

    @Test
    public void testTrueItemAcquisitionAndLoss() {
        // Prev: 4 golden apples in slot 0
        List<InventoryItemData> prevItems = new ArrayList<>();
        prevItems.add(new InventoryItemData("minecraft:golden_apple", "Golden Apple", 4, 64, 0, InventorySlotSource.HOTBAR, "", null, false, true));
        InventorySnapshot prevSnapshot = new InventorySnapshot(1000L, "world_1", prevItems);

        // Curr: now 6 golden apples (+2 picked up) and added 16 obsidian
        List<InventoryItemData> currItems = new ArrayList<>();
        currItems.add(new InventoryItemData("minecraft:golden_apple", "Golden Apple", 6, 64, 0, InventorySlotSource.HOTBAR, "", null, false, true));
        currItems.add(new InventoryItemData("minecraft:obsidian", "Obsidian", 16, 64, 1, InventorySlotSource.HOTBAR, "", null, true, false));
        InventorySnapshot currSnapshot = new InventorySnapshot(2000L, "world_1", currItems);

        Set<InventorySlotSource> sources = EnumSet.of(InventorySlotSource.HOTBAR);
        SnapshotDiffResult diff = service.compare(prevSnapshot, currSnapshot, sources);

        assertTrue(diff.hasNetChanges());
        assertEquals(2, diff.getNetChange("minecraft:golden_apple"));
        assertEquals(16, diff.getNetChange("minecraft:obsidian"));

        // Next: drop 3 golden apples (6 -> 3)
        List<InventoryItemData> droppedItems = new ArrayList<>();
        droppedItems.add(new InventoryItemData("minecraft:golden_apple", "Golden Apple", 3, 64, 0, InventorySlotSource.HOTBAR, "", null, false, true));
        droppedItems.add(new InventoryItemData("minecraft:obsidian", "Obsidian", 16, 64, 1, InventorySlotSource.HOTBAR, "", null, true, false));
        InventorySnapshot droppedSnapshot = new InventorySnapshot(3000L, "world_1", droppedItems);

        SnapshotDiffResult dropDiff = service.compare(currSnapshot, droppedSnapshot, sources);
        assertTrue(dropDiff.hasNetChanges());
        assertEquals(-3, dropDiff.getNetChange("minecraft:golden_apple"));
        assertEquals(0, dropDiff.getNetChange("minecraft:obsidian"));
    }
}
