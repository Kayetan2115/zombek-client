package pl.zombek.client.core;

import org.junit.jupiter.api.Test;
import pl.zombek.client.core.data.ArmorItemData;
import pl.zombek.client.core.data.ArmorSlot;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ArmorItemDataTest {

    @Test
    void testStandardDurabilityCalculation() {
        ArmorItemData chestplate = new ArmorItemData(
                ArmorSlot.CHESTPLATE,
                "diamond_chestplate",
                "Diamentowy Napierśnik",
                100, // 100 damage taken
                500, // 500 max durability
                true,
                false,
                Arrays.asList("Protection IV", "Unbreaking III"),
                null
        );

        assertEquals(ArmorSlot.CHESTPLATE, chestplate.getSlot());
        assertEquals("diamond_chestplate", chestplate.getItemId());
        assertEquals("Diamentowy Napierśnik", chestplate.getDisplayName());
        assertEquals(100, chestplate.getDamage());
        assertEquals(500, chestplate.getMaxDamage());
        assertTrue(chestplate.hasDurability());
        assertFalse(chestplate.isEmpty());
        assertEquals(400, chestplate.getRemainingDurability());
        assertEquals(80.0f, chestplate.getDurabilityPercentage(), 0.001f);
        assertEquals(2, chestplate.getEnchantments().size());
    }

    @Test
    void testNonDamageableItem() {
        ArmorItemData pumpkin = new ArmorItemData(
                ArmorSlot.HELMET,
                "carved_pumpkin",
                "Wyrzeźbiona Dynia",
                0,
                0,
                false,
                false,
                null,
                null
        );

        assertFalse(pumpkin.hasDurability());
        assertFalse(pumpkin.isEmpty());
        assertEquals(0, pumpkin.getRemainingDurability());
        assertEquals(100.0f, pumpkin.getDurabilityPercentage(), 0.001f);
    }

    @Test
    void testEmptySlot() {
        ArmorItemData emptySlot = ArmorItemData.empty(ArmorSlot.BOOTS);

        assertTrue(emptySlot.isEmpty());
        assertFalse(emptySlot.hasDurability());
        assertEquals(ArmorSlot.BOOTS, emptySlot.getSlot());
        assertEquals(0, emptySlot.getRemainingDurability());
        assertEquals(100.0f, emptySlot.getDurabilityPercentage(), 0.001f);
    }

    @Test
    void testExhaustedDurability() {
        ArmorItemData brokenHelmet = new ArmorItemData(
                ArmorSlot.HELMET,
                "iron_helmet",
                "Żelazny Hełm",
                165,
                165,
                true,
                false,
                null,
                null
        );

        assertEquals(0, brokenHelmet.getRemainingDurability());
        assertEquals(0.0f, brokenHelmet.getDurabilityPercentage(), 0.001f);
    }
}
