package pl.zombek.client.core.modules.visual.oneseven;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ItemTransformManagerTest {

    private ItemTransformManager manager;

    @BeforeEach
    void setUp() {
        manager = ItemTransformManager.getInstance();
        manager.resetAllCategories();
    }

    @Test
    void testResolveItemCategory() {
        assertEquals(ItemCategory.SWORD, ItemCategory.fromItemId("minecraft:diamond_sword"));
        assertEquals(ItemCategory.SWORD, ItemCategory.fromItemId("minecraft:netherite_sword"));
        assertEquals(ItemCategory.AXE, ItemCategory.fromItemId("minecraft:iron_axe"));
        assertEquals(ItemCategory.PICKAXE, ItemCategory.fromItemId("minecraft:golden_pickaxe"));
        assertEquals(ItemCategory.BOW, ItemCategory.fromItemId("minecraft:bow"));
        assertEquals(ItemCategory.FISHING_ROD, ItemCategory.fromItemId("minecraft:fishing_rod"));
        assertEquals(ItemCategory.FOOD, ItemCategory.fromItemId("minecraft:golden_apple"));
        assertEquals(ItemCategory.POTION, ItemCategory.fromItemId("minecraft:potion"));
        assertEquals(ItemCategory.SHIELD, ItemCategory.fromItemId("minecraft:shield"));
        assertEquals(ItemCategory.OTHER, ItemCategory.fromItemId("minecraft:stick"));
    }

    @Test
    void testCategoryTransformModificationAndReset() {
        CategoryTransform transform = manager.getTransform(ItemCategory.SWORD);
        transform.setEnabled(true);
        transform.getOffset().setPosX(0.25f);
        transform.getOffset().setRotY(45.0f);

        assertEquals(0.25f, transform.getOffset().getPosX());
        assertEquals(45.0f, transform.getOffset().getRotY());

        manager.resetCategory(ItemCategory.SWORD);
        assertFalse(transform.isEnabled());
        assertEquals(0.0f, transform.getOffset().getPosX());
        assertEquals(0.0f, transform.getOffset().getRotY());
    }

    @Test
    void testCopyCategory() {
        CategoryTransform sword = manager.getTransform(ItemCategory.SWORD);
        sword.setEnabled(true);
        sword.getOffset().setPosY(-0.1f);

        manager.copyCategory(ItemCategory.SWORD, ItemCategory.AXE);
        CategoryTransform axe = manager.getTransform(ItemCategory.AXE);
        assertTrue(axe.isEnabled());
        assertEquals(-0.1f, axe.getOffset().getPosY());
    }

    @Test
    void testApplyPresetTransforms() {
        manager.applyPreset(AnimationPreset.MINECRAFT_1_7);
        CategoryTransform sword = manager.getTransform(ItemCategory.SWORD);
        assertTrue(sword.isEnabled());
        assertEquals(-5.0f, sword.getOffset().getRotX());
        assertEquals(-12.0f, sword.getOffset().getRotY());

        manager.applyPreset(AnimationPreset.MODERN);
        assertFalse(sword.isEnabled());
    }
}
