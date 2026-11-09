package pl.zombek.client.core.hud.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Priority and collision resolver for HUD components.
 * Prevents overlapping elements (e.g., Scoreboard overlapping Boss Bar or Potion Effects overlapping Saturation)
 * by adjusting layout offsets based on declared Render Priority.
 */
public final class HudPriorityResolver {

    public static final int PRIORITY_TAB_EDITOR = 120; // Tab List Overlay
    public static final int PRIORITY_TITLES = 115;     // Titles & Action Bar Overlay
    public static final int PRIORITY_CHAT = 110;       // Chat Overlay
    public static final int PRIORITY_CRITICAL = 100; // e.g. Boss Bar
    public static final int PRIORITY_HIGH = 75;     // e.g. Scoreboard
    public static final int PRIORITY_MEDIUM = 50;   // e.g. Potion Effects
    public static final int PRIORITY_CLOCK = 40;    // Clock
    public static final int PRIORITY_NET_GRAPH = 36; // Net Graph HUD
    public static final int PRIORITY_DAY_COUNTER = 35; // Day Counter
    public static final int PRIORITY_ITEM_COUNTER = 32; // Item Counter
    public static final int PRIORITY_PLAYTIME = 30; // Playtime
    public static final int PRIORITY_ITEM_TRACKER = 29; // Item Tracker
    public static final int PRIORITY_STOPWATCH = 28; // Stopwatch
    public static final int PRIORITY_STANDARD = 25; // e.g. Saturation, Coordinates
    public static final int PRIORITY_SERVER_ADDRESS = 24; // Server Address
    public static final int PRIORITY_LOW = 0;

    private HudPriorityResolver() {}

    /**
     * Resolves collisions among active components by adjusting their effective Y offsets.
     * Components with higher priority preserve their intended position.
     * Lower priority components are shifted down (or up, depending on anchor) to avoid overlapping.
     *
     * @param components   List of components to resolve
     * @param screenWidth  Current screen width
     * @param screenHeight Current screen height
     * @param spacing      Pixel spacing between stacked components
     */
    public static void resolveCollisions(List<HudComponentModel> components, int screenWidth, int screenHeight, int spacing) {
        if (components == null || components.size() <= 1 || screenWidth <= 0 || screenHeight <= 0) {
            return;
        }

        // Filter visible components
        List<HudComponentModel> active = new ArrayList<>();
        for (HudComponentModel model : components) {
            if (model != null && model.isVisible()) {
                active.add(model);
            }
        }

        if (active.size() <= 1) {
            return;
        }

        // Sort by render priority descending (highest priority first)
        active.sort(Comparator.comparingInt(HudComponentModel::getRenderPriority).reversed());

        // Placed bounding boxes: [x1, y1, x2, y2]
        List<int[]> placedBoxes = new ArrayList<>();

        for (HudComponentModel current : active) {
            int curW = current.getEffectiveWidth();
            int curH = current.getEffectiveHeight();
            int curX = current.getScreenX(screenWidth);
            int curY = current.getScreenY(screenHeight);

            boolean collided;
            int attempts = 0;
            final int maxAttempts = 20;

            do {
                collided = false;
                for (int[] box : placedBoxes) {
                    if (intersects(curX, curY, curW, curH, box[0], box[1], box[2], box[3])) {
                        collided = true;
                        // Shift current component to avoid collision
                        // If bottom-anchored, shift upwards on screen (increase posY offset)
                        // If top-anchored or center, shift downwards on screen (posY = box[1] + box[3] + spacing)
                        if (current.getAnchor().name().startsWith("BOTTOM")) {
                            current.setPosY(screenHeight - box[1] + spacing);
                        } else {
                            current.setPosY(box[1] + box[3] + spacing);
                        }
                        curY = current.getScreenY(screenHeight);
                        break;
                    }
                }
                attempts++;
            } while (collided && attempts < maxAttempts);

            current.clampToScreen(screenWidth, screenHeight);
            placedBoxes.add(new int[]{current.getScreenX(screenWidth), current.getScreenY(screenHeight), curW, curH});
        }
    }

    /**
     * Checks if two rectangles intersect.
     */
    public static boolean intersects(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }
}
