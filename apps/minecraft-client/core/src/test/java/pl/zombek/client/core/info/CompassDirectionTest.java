package pl.zombek.client.core.info;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CompassDirectionTest {

    @Test
    public void testMinecraftYawConversion() {
        // South is 0 in MC
        assertEquals(CompassDirection.S, CompassDirection.fromMinecraftYaw(0.0f));
        assertEquals(CompassDirection.S, CompassDirection.fromMinecraftYaw(10.0f));
        assertEquals(CompassDirection.S, CompassDirection.fromMinecraftYaw(350.0f));

        // SW is 45
        assertEquals(CompassDirection.SW, CompassDirection.fromMinecraftYaw(45.0f));

        // West is 90
        assertEquals(CompassDirection.W, CompassDirection.fromMinecraftYaw(90.0f));

        // NW is 135
        assertEquals(CompassDirection.NW, CompassDirection.fromMinecraftYaw(135.0f));

        // North is 180
        assertEquals(CompassDirection.N, CompassDirection.fromMinecraftYaw(180.0f));

        // NE is 225
        assertEquals(CompassDirection.NE, CompassDirection.fromMinecraftYaw(225.0f));

        // East is 270
        assertEquals(CompassDirection.E, CompassDirection.fromMinecraftYaw(270.0f));

        // SE is 315
        assertEquals(CompassDirection.SE, CompassDirection.fromMinecraftYaw(315.0f));
    }

    @Test
    public void testNegativeAndWrappingYaw() {
        assertEquals(CompassDirection.E, CompassDirection.fromMinecraftYaw(-90.0f)); // 270
        assertEquals(CompassDirection.N, CompassDirection.fromMinecraftYaw(-180.0f)); // 180
        assertEquals(CompassDirection.S, CompassDirection.fromMinecraftYaw(720.0f)); // 0
    }

    @Test
    public void testCardinalClassification() {
        assertTrue(CompassDirection.N.isCardinal());
        assertTrue(CompassDirection.S.isCardinal());
        assertTrue(CompassDirection.E.isCardinal());
        assertTrue(CompassDirection.W.isCardinal());

        assertFalse(CompassDirection.NE.isCardinal());
        assertFalse(CompassDirection.NW.isCardinal());
        assertFalse(CompassDirection.SE.isCardinal());
        assertFalse(CompassDirection.SW.isCardinal());
    }
}
