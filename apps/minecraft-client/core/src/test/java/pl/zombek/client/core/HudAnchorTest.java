package pl.zombek.client.core;

import org.junit.jupiter.api.Test;
import pl.zombek.client.core.hud.HudAnchor;

import static org.junit.jupiter.api.Assertions.*;

public class HudAnchorTest {

    @Test
    void testTopLeftAnchor() {
        HudAnchor anchor = HudAnchor.TOP_LEFT;
        // offset (10, 20), element size (80, 50), screen (800, 600)
        assertEquals(10, anchor.calculateX(10, 80, 800));
        assertEquals(20, anchor.calculateY(20, 50, 600));
    }

    @Test
    void testTopRightAnchor() {
        HudAnchor anchor = HudAnchor.TOP_RIGHT;
        // screenWidth (800) - width (80) - offsetX (10) = 710
        assertEquals(710, anchor.calculateX(10, 80, 800));
        assertEquals(20, anchor.calculateY(20, 50, 600));
    }

    @Test
    void testBottomLeftAnchor() {
        HudAnchor anchor = HudAnchor.BOTTOM_LEFT;
        assertEquals(10, anchor.calculateX(10, 80, 800));
        // screenHeight (600) - height (50) - offsetY (30) = 520
        assertEquals(520, anchor.calculateY(30, 50, 600));
    }

    @Test
    void testBottomRightAnchor() {
        HudAnchor anchor = HudAnchor.BOTTOM_RIGHT;
        assertEquals(710, anchor.calculateX(10, 80, 800));
        assertEquals(520, anchor.calculateY(30, 50, 600));
    }

    @Test
    void testScreenBoundsClamp() {
        HudAnchor anchor = HudAnchor.TOP_LEFT;
        // Offset beyond screen
        assertEquals(750, anchor.calculateX(900, 50, 800));
        assertEquals(550, anchor.calculateY(900, 50, 600));
    }
}
