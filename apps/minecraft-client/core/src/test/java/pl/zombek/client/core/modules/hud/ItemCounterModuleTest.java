package pl.zombek.client.core.modules.hud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.inventory.InventoryItemData;
import pl.zombek.client.core.inventory.InventorySlotSource;
import pl.zombek.client.core.inventory.InventorySnapshot;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.hud.itemcounter.ItemCounterModule;
import pl.zombek.client.core.modules.hud.itemcounter.ItemCounterModule.DisplayItemEntry;
import pl.zombek.client.core.settings.BooleanSetting;
import pl.zombek.client.core.settings.NumberSetting;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ItemCounterModuleTest {

    private ItemCounterModule module;

    @BeforeEach
    public void setUp() {
        module = new ItemCounterModule();
        module.setEnabled(true);
    }

    @Test
    public void testMetadataAndCategory() {
        assertEquals("item_counter", module.getId());
        assertEquals("Item Counter", module.getName());
        assertEquals(ModuleCategory.HUD, module.getCategory());
        assertTrue(module.isVisible());
        assertEquals(6, module.getSettingGroups().size()); // general, sources, tracking, alerts, appearance, layout
    }

    @Test
    public void testCountingAndSourceFiltering() {
        List<InventoryItemData> items = new ArrayList<>();
        // Hotbar has 8 pearls
        items.add(new InventoryItemData("minecraft:ender_pearl", "Ender Pearl", 8, 16, 0, InventorySlotSource.HOTBAR, null, null, false, false));
        // Main inventory has 16 pearls
        items.add(new InventoryItemData("minecraft:ender_pearl", "Ender Pearl", 16, 16, 10, InventorySlotSource.MAIN_INVENTORY, null, null, false, false));
        // Offhand has 1 golden apple
        items.add(new InventoryItemData("minecraft:golden_apple", "Golden Apple", 1, 64, 40, InventorySlotSource.OFFHAND, null, null, false, true));
        // Armor has 1 helmet
        items.add(new InventoryItemData("minecraft:diamond_helmet", "Diamond Helmet", 1, 1, 39, InventorySlotSource.ARMOR, null, null, false, false));

        InventorySnapshot snapshot = new InventorySnapshot(1000L, "test_world", items);

        // Track only ender pearls and golden apples
        ((pl.zombek.client.core.settings.StringSetting) module.getSetting("watched_items")).setValue("minecraft:ender_pearl,minecraft:golden_apple");

        // By default, hotbar, main, and offhand are enabled
        List<DisplayItemEntry> entries = module.buildDisplayEntries(snapshot, true);
        assertEquals(2, entries.size());

        DisplayItemEntry pearls = entries.stream().filter(e -> e.itemId.equals("minecraft:ender_pearl")).findFirst().orElse(null);
        assertNotNull(pearls);
        assertEquals(24, pearls.count); // 8 + 16

        // Disable main inventory source
        BooleanSetting countMainSetting = (BooleanSetting) module.getSetting("count_main_inventory");
        countMainSetting.setValue(false);

        List<DisplayItemEntry> entriesOnlyHotbar = module.buildDisplayEntries(snapshot, true);
        DisplayItemEntry pearlsOnlyHotbar = entriesOnlyHotbar.stream().filter(e -> e.itemId.equals("minecraft:ender_pearl")).findFirst().orElse(null);
        assertNotNull(pearlsOnlyHotbar);
        assertEquals(8, pearlsOnlyHotbar.count); // Only hotbar
    }

    @Test
    public void testQuantityAlertThresholds() {
        List<InventoryItemData> items = new ArrayList<>();
        items.add(new InventoryItemData("minecraft:golden_apple", "Golden Apple", 2, 64, 0, InventorySlotSource.HOTBAR, null, null, false, true));
        items.add(new InventoryItemData("minecraft:arrow", "Arrow", 5, 64, 1, InventorySlotSource.HOTBAR, null, null, false, false));
        items.add(new InventoryItemData("minecraft:cobblestone", "Cobblestone", 64, 64, 2, InventorySlotSource.HOTBAR, null, null, true, false));

        InventorySnapshot snapshot = new InventorySnapshot(1000L, "test_world", items);

        module.addWatchedItem("minecraft:golden_apple");
        module.addWatchedItem("minecraft:arrow");
        module.addWatchedItem("minecraft:cobblestone");

        NumberSetting warnSetting = (NumberSetting) module.getSetting("warning_threshold");
        NumberSetting critSetting = (NumberSetting) module.getSetting("critical_threshold");
        warnSetting.setValue(5.0);
        critSetting.setValue(2.0);

        List<DisplayItemEntry> entries = module.buildDisplayEntries(snapshot, true);

        DisplayItemEntry apples = entries.stream().filter(e -> e.itemId.equals("minecraft:golden_apple")).findFirst().orElseThrow();
        assertTrue(apples.critical, "Apples count (2) <= critical threshold (2) should be critical");

        DisplayItemEntry arrows = entries.stream().filter(e -> e.itemId.equals("minecraft:arrow")).findFirst().orElseThrow();
        assertTrue(arrows.warning, "Arrows count (5) <= warning threshold (5) should be warning");
        assertFalse(arrows.critical, "Arrows count (5) > critical threshold (2) should not be critical");

        DisplayItemEntry cobble = entries.stream().filter(e -> e.itemId.equals("minecraft:cobblestone")).findFirst().orElseThrow();
        assertFalse(cobble.warning, "Cobblestone count (64) should not be warning");
        assertFalse(cobble.critical, "Cobblestone count (64) should not be critical");
    }

    @Test
    public void testPreviewFallbackWhenWorldNotLoaded() {
        List<DisplayItemEntry> entries = module.buildDisplayEntries(null, false);
        assertFalse(entries.isEmpty(), "Preview entries should be returned when world is not loaded");
        assertEquals(3, entries.size());
        assertEquals("minecraft:ender_pearl", entries.get(0).itemId);
        assertEquals(12, entries.get(0).count);
    }

    @Test
    public void testWatchedItemManagement() {
        module.addWatchedItem("minecraft:totem_of_undying");
        assertTrue(module.getWatchedItemIds().contains("minecraft:totem_of_undying"));

        module.removeWatchedItem("minecraft:totem_of_undying");
        assertFalse(module.getWatchedItemIds().contains("minecraft:totem_of_undying"));
    }

    @Test
    public void testRestoreDefaults() {
        BooleanSetting countHotbar = (BooleanSetting) module.getSetting("count_hotbar");
        countHotbar.setValue(false);
        assertFalse(countHotbar.getValue());

        countHotbar.reset();
        assertTrue(countHotbar.getValue(), "Default for count_hotbar should be true");
    }
}
