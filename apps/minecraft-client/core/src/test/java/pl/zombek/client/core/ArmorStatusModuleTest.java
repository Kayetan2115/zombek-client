package pl.zombek.client.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.data.ArmorDataProviderRegistry;
import pl.zombek.client.core.data.ArmorItemData;
import pl.zombek.client.core.data.ArmorSlot;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.pvp.ArmorStatusModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ArmorStatusModuleTest {

    private ArmorStatusModule module;

    static class MockHudRenderer implements IHudRenderer {
        final List<String> drawnTexts = new ArrayList<>();
        int fillCalls = 0;
        int borderCalls = 0;
        int itemRenderCalls = 0;

        @Override public void fill(int x, int y, int width, int height, int color) { fillCalls++; }
        @Override public void drawBorder(int x, int y, int width, int height, int color, int thickness) { borderCalls++; }
        @Override public void drawText(String text, int x, int y, int color, boolean shadow, float scale, boolean bold) { drawnTexts.add(text); }
        @Override public void renderItem(Object itemHandle, int x, int y, float scale) { itemRenderCalls++; }
        @Override public int getTextWidth(String text) { return text.length() * 6; }
        @Override public int getFontHeight() { return 9; }
        @Override public int getScreenWidth() { return 854; }
        @Override public int getScreenHeight() { return 480; }
    }

    @BeforeEach
    void setUp() {
        HudElementRegistry.getInstance().clear();
        ArmorDataProviderRegistry.reset();
        module = new ArmorStatusModule();
    }

    @AfterEach
    void tearDown() {
        module.setEnabled(false);
        HudElementRegistry.getInstance().clear();
        ArmorDataProviderRegistry.reset();
    }

    @Test
    void testModuleMetadataAndSettings() {
        assertEquals("armor_status", module.getId());
        assertEquals("Armor Status", module.getName());
        assertEquals(ModuleCategory.PVP, module.getCategory());
        assertFalse(module.isEnabled());

        assertNotNull(module.getShowHelmet());
        assertNotNull(module.getShowChestplate());
        assertNotNull(module.getShowLeggings());
        assertNotNull(module.getShowBoots());
        assertNotNull(module.getHideEmptySlots());
        assertNotNull(module.getLayoutStyle());
        assertNotNull(module.getSlotOrder());
    }

    @Test
    void testDurabilityColorThresholdResolution() {
        // High durability (> 60%) -> 0xFF55FF55
        assertEquals(0xFF55FF55, module.resolveDurabilityColor(85.0f));
        assertEquals(0xFF55FF55, module.resolveDurabilityColor(60.0f));

        // Medium durability (30% - 59%) -> 0xFFFFFF55
        assertEquals(0xFFFFFF55, module.resolveDurabilityColor(50.0f));
        assertEquals(0xFFFFFF55, module.resolveDurabilityColor(30.0f));

        // Low durability (16% - 29%) -> 0xFFFF5555
        assertEquals(0xFFFF5555, module.resolveDurabilityColor(25.0f));

        // Critical durability (<= 15%) -> 0xFFAA0000
        assertEquals(0xFFAA0000, module.resolveDurabilityColor(10.0f));
        assertEquals(0xFFAA0000, module.resolveDurabilityColor(0.0f));
    }

    @Test
    void testRenderWithMockArmor() {
        // Register mock equipped armor
        ArmorDataProviderRegistry.setProvider(() -> Arrays.asList(
                new ArmorItemData(ArmorSlot.HELMET, "diamond_helmet", "Diamentowy Hełm", 50, 363, true, false, null, "stack_helmet"),
                new ArmorItemData(ArmorSlot.CHESTPLATE, "diamond_chestplate", "Diamentowy Napierśnik", 200, 528, true, false, null, "stack_chest"),
                new ArmorItemData(ArmorSlot.LEGGINGS, "diamond_leggings", "Diamentowe Nogawice", 100, 495, true, false, null, "stack_legs"),
                new ArmorItemData(ArmorSlot.BOOTS, "diamond_boots", "Diamentowe Buty", 300, 429, true, false, null, "stack_boots")
        ));

        module.setEnabled(true);
        MockHudRenderer renderer = new MockHudRenderer();

        module.render(renderer);

        assertTrue(renderer.fillCalls > 0);
        assertTrue(renderer.borderCalls > 0);
        assertEquals(4, renderer.itemRenderCalls);
        assertEquals(4, renderer.drawnTexts.size());
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("%")));
    }
}
