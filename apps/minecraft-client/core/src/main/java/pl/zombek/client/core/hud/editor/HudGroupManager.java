package pl.zombek.client.core.hud.editor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manages logical groups of HUD components, collective transformations, and group bounding boxes.
 */
public class HudGroupManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(HudGroupManager.class);
    private static final HudGroupManager INSTANCE = new HudGroupManager();

    public static class GroupBounds {
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        public GroupBounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }

        public boolean contains(int px, int py) {
            return px >= x && px <= x + width && py >= y && py <= y + height;
        }
    }

    private HudGroupManager() {
    }

    public static HudGroupManager getInstance() {
        return INSTANCE;
    }

    public synchronized HudGroup createGroup(HudLayout layout, String groupId, String name, Collection<String> componentIds) {
        if (layout == null || groupId == null || name == null) return null;
        String cleanId = groupId.trim().toLowerCase(Locale.ROOT);
        if (layout.getGroups().containsKey(cleanId)) {
            LOGGER.warn("Group '{}' already exists in layout.", cleanId);
            return null;
        }

        HudGroup group = new HudGroup(cleanId, name.trim());
        if (componentIds != null) {
            for (String compId : componentIds) {
                if (compId != null) {
                    group.addComponent(compId);
                    HudComponentState state = layout.getComponentState(compId);
                    if (state != null) {
                        state.setGroupId(cleanId);
                    }
                }
            }
        }
        layout.addGroup(group);
        LOGGER.info("Created group '{}' with {} components", cleanId, group.getComponentIds().size());
        return group;
    }

    public synchronized boolean renameGroup(HudLayout layout, String groupId, String newName) {
        if (layout == null || groupId == null || newName == null || newName.trim().isEmpty()) return false;
        HudGroup group = layout.getGroup(groupId.toLowerCase(Locale.ROOT));
        if (group == null) return false;
        group.setName(newName.trim());
        return true;
    }

    public synchronized boolean deleteGroup(HudLayout layout, String groupId) {
        if (layout == null || groupId == null) return false;
        String cleanId = groupId.toLowerCase(Locale.ROOT);
        HudGroup group = layout.getGroup(cleanId);
        if (group == null) return false;

        // Ungroup components without deleting them
        for (String compId : group.getComponentIds()) {
            HudComponentState state = layout.getComponentState(compId);
            if (state != null && cleanId.equals(state.getGroupId())) {
                state.setGroupId(null);
            }
        }
        layout.removeGroup(cleanId);
        LOGGER.info("Deleted and ungrouped group '{}'", cleanId);
        return true;
    }

    public synchronized boolean addComponentToGroup(HudLayout layout, String groupId, String componentId) {
        if (layout == null || groupId == null || componentId == null) return false;
        HudGroup group = layout.getGroup(groupId.toLowerCase(Locale.ROOT));
        if (group == null) return false;

        String cleanCompId = componentId.toLowerCase(Locale.ROOT);
        group.addComponent(cleanCompId);
        HudComponentState state = layout.getComponentState(cleanCompId);
        if (state != null) {
            state.setGroupId(group.getGroupId());
        }
        return true;
    }

    public synchronized boolean removeComponentFromGroup(HudLayout layout, String groupId, String componentId) {
        if (layout == null || groupId == null || componentId == null) return false;
        HudGroup group = layout.getGroup(groupId.toLowerCase(Locale.ROOT));
        if (group == null) return false;

        String cleanCompId = componentId.toLowerCase(Locale.ROOT);
        group.removeComponent(cleanCompId);
        HudComponentState state = layout.getComponentState(cleanCompId);
        if (state != null && group.getGroupId().equals(state.getGroupId())) {
            state.setGroupId(null);
        }
        return true;
    }

    public synchronized HudGroup duplicateGroup(HudLayout layout, String sourceGroupId, String newGroupId, String newName) {
        if (layout == null || sourceGroupId == null || newGroupId == null || newName == null) return null;
        HudGroup srcGroup = layout.getGroup(sourceGroupId.toLowerCase(Locale.ROOT));
        if (srcGroup == null) return null;

        HudGroup duplicate = new HudGroup(newGroupId.toLowerCase(Locale.ROOT), newName, srcGroup.getColor());
        duplicate.setComponentIds(new LinkedHashSet<>(srcGroup.getComponentIds()));
        duplicate.setLocked(srcGroup.isLocked());
        duplicate.setVisible(srcGroup.isVisible());
        layout.addGroup(duplicate);
        return duplicate;
    }

    public GroupBounds calculateGroupBounds(HudGroup group, HudLayout layout, int screenW, int screenH) {
        if (group == null || layout == null || group.getComponentIds().isEmpty()) {
            return null;
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int count = 0;

        for (String compId : group.getComponentIds()) {
            HudComponentState state = layout.getComponentState(compId);
            if (state != null && state.isVisible()) {
                int sx = state.calculateScreenX(screenW);
                int sy = state.calculateScreenY(screenH);
                int sw = state.getEffectiveWidth();
                int sh = state.getEffectiveHeight();

                minX = Math.min(minX, sx);
                minY = Math.min(minY, sy);
                maxX = Math.max(maxX, sx + sw);
                maxY = Math.max(maxY, sy + sh);
                count++;
            }
        }

        if (count == 0 || minX == Integer.MAX_VALUE) {
            return null;
        }

        int padding = 4;
        return new GroupBounds(
                Math.max(0, minX - padding),
                Math.max(0, minY - padding),
                (maxX - minX) + padding * 2,
                (maxY - minY) + padding * 2
        );
    }
}
