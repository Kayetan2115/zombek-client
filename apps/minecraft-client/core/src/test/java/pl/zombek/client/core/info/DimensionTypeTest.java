package pl.zombek.client.core.info;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DimensionTypeTest {

    @Test
    public void testDimensionFromString() {
        assertEquals(DimensionType.OVERWORLD, DimensionType.fromString("minecraft:overworld"));
        assertEquals(DimensionType.OVERWORLD, DimensionType.fromString("overworld"));
        assertEquals(DimensionType.NETHER, DimensionType.fromString("minecraft:the_nether"));
        assertEquals(DimensionType.NETHER, DimensionType.fromString("the_nether"));
        assertEquals(DimensionType.END, DimensionType.fromString("minecraft:the_end"));
        assertEquals(DimensionType.END, DimensionType.fromString("the_end"));
        assertEquals(DimensionType.CUSTOM, DimensionType.fromString("custom_mod:twilight_forest"));
        assertEquals(DimensionType.OVERWORLD, DimensionType.fromString(null));
    }

    @Test
    public void testDisplayNames() {
        assertEquals("Overworld", DimensionType.OVERWORLD.getDisplayName());
        assertEquals("Nether", DimensionType.NETHER.getDisplayName());
        assertEquals("The End", DimensionType.END.getDisplayName());
        assertEquals("Inny", DimensionType.CUSTOM.getDisplayName());
    }
}
