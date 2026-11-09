package pl.zombek.client.core.hud;

import org.junit.jupiter.api.Test;
import pl.zombek.client.core.hud.component.HudComponentModel;
import pl.zombek.client.core.hud.component.HudPriorityResolver;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HudPriorityResolverTest {

    @Test
    public void testPriorityConstants() {
        assertTrue(HudPriorityResolver.PRIORITY_CRITICAL > HudPriorityResolver.PRIORITY_HIGH);
        assertTrue(HudPriorityResolver.PRIORITY_HIGH > HudPriorityResolver.PRIORITY_MEDIUM);
        assertTrue(HudPriorityResolver.PRIORITY_MEDIUM > HudPriorityResolver.PRIORITY_STANDARD);
        assertTrue(HudPriorityResolver.PRIORITY_STANDARD > HudPriorityResolver.PRIORITY_LOW);
    }

    @Test
    public void testIntersectionDetection() {
        // [x1, y1, w1, h1] vs [x2, y2, w2, h2]
        // Overlapping rectangles
        assertTrue(HudPriorityResolver.intersects(10, 10, 50, 50, 30, 30, 50, 50));
        // Non-overlapping rectangles (separated horizontally)
        assertFalse(HudPriorityResolver.intersects(10, 10, 20, 20, 50, 10, 20, 20));
        // Non-overlapping rectangles (separated vertically)
        assertFalse(HudPriorityResolver.intersects(10, 10, 20, 20, 10, 50, 20, 20));
        // Touching borders only (no area overlap)
        assertFalse(HudPriorityResolver.intersects(0, 0, 10, 10, 10, 0, 10, 10));
    }

    @Test
    public void testCollisionResolutionTopAnchored() {
        int screenW = 800;
        int screenH = 600;

        // Two top-right elements occupying the exact same space
        HudComponentModel higherPriority = new HudComponentModel("comp_high", 100, 40, HudAnchor.TOP_RIGHT);
        higherPriority.setRenderPriority(HudPriorityResolver.PRIORITY_HIGH);
        higherPriority.setPosX(10);
        higherPriority.setPosY(10);

        HudComponentModel lowerPriority = new HudComponentModel("comp_low", 100, 40, HudAnchor.TOP_RIGHT);
        lowerPriority.setRenderPriority(HudPriorityResolver.PRIORITY_STANDARD);
        lowerPriority.setPosX(10);
        lowerPriority.setPosY(10);

        List<HudComponentModel> list = new ArrayList<>();
        list.add(lowerPriority);
        list.add(higherPriority);

        HudPriorityResolver.resolveCollisions(list, screenW, screenH, 4);

        // Higher priority should remain at posY = 10
        assertEquals(10, higherPriority.getPosY());

        // Lower priority should be shifted down below higher priority (10 + 40 + 4 = 54)
        assertTrue(lowerPriority.getPosY() >= 54);

        // Verify that bounding boxes no longer intersect
        int highX = higherPriority.getScreenX(screenW);
        int highY = higherPriority.getScreenY(screenH);
        int lowX = lowerPriority.getScreenX(screenW);
        int lowY = lowerPriority.getScreenY(screenH);

        assertFalse(HudPriorityResolver.intersects(highX, highY, 100, 40, lowX, lowY, 100, 40));
    }

    @Test
    public void testCollisionResolutionBottomAnchored() {
        int screenW = 800;
        int screenH = 600;

        HudComponentModel higherPriority = new HudComponentModel("comp_high", 80, 30, HudAnchor.BOTTOM_RIGHT);
        higherPriority.setRenderPriority(HudPriorityResolver.PRIORITY_HIGH);
        higherPriority.setPosX(5);
        higherPriority.setPosY(5);

        HudComponentModel lowerPriority = new HudComponentModel("comp_low", 80, 30, HudAnchor.BOTTOM_RIGHT);
        lowerPriority.setRenderPriority(HudPriorityResolver.PRIORITY_STANDARD);
        lowerPriority.setPosX(5);
        lowerPriority.setPosY(5);

        List<HudComponentModel> list = new ArrayList<>();
        list.add(higherPriority);
        list.add(lowerPriority);

        HudPriorityResolver.resolveCollisions(list, screenW, screenH, 2);

        // Higher priority remains intact
        assertEquals(5, higherPriority.getPosY());

        // Lower priority is shifted
        assertTrue(lowerPriority.getPosY() > 5);

        int highX = higherPriority.getScreenX(screenW);
        int highY = higherPriority.getScreenY(screenH);
        int lowX = lowerPriority.getScreenX(screenW);
        int lowY = lowerPriority.getScreenY(screenH);

        assertFalse(HudPriorityResolver.intersects(highX, highY, 80, 30, lowX, lowY, 80, 30));
    }

    @Test
    public void testNoCollisionWhenFarApart() {
        int screenW = 800;
        int screenH = 600;

        HudComponentModel leftComp = new HudComponentModel("left", 100, 40, HudAnchor.TOP_LEFT);
        leftComp.setRenderPriority(HudPriorityResolver.PRIORITY_HIGH);
        leftComp.setPosX(10);
        leftComp.setPosY(10);

        HudComponentModel rightComp = new HudComponentModel("right", 100, 40, HudAnchor.TOP_RIGHT);
        rightComp.setRenderPriority(HudPriorityResolver.PRIORITY_STANDARD);
        rightComp.setPosX(10);
        rightComp.setPosY(10);

        List<HudComponentModel> list = new ArrayList<>();
        list.add(leftComp);
        list.add(rightComp);

        HudPriorityResolver.resolveCollisions(list, screenW, screenH, 5);

        assertEquals(10, leftComp.getPosY());
        assertEquals(10, rightComp.getPosY());
    }
}
