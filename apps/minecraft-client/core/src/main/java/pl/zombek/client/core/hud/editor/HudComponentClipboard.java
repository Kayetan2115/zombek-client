package pl.zombek.client.core.hud.editor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manages copying, pasting, and duplicating HUD component configurations.
 */
public class HudComponentClipboard {
    private static final Logger LOGGER = LoggerFactory.getLogger(HudComponentClipboard.class);
    private static final HudComponentClipboard INSTANCE = new HudComponentClipboard();

    public enum PasteMode {
        IN_PLACE,
        NEAR_CURSOR,
        APPEARANCE_ONLY,
        POSITION_ONLY
    }

    private HudClipboardData clipboardData = null;

    private HudComponentClipboard() {
    }

    public static HudComponentClipboard getInstance() {
        return INSTANCE;
    }

    public synchronized void copy(Collection<HudComponentState> states, Collection<HudGroup> groups, String sourceProfileId) {
        HudClipboardData data = new HudClipboardData(sourceProfileId);
        if (states != null) {
            for (HudComponentState state : states) {
                data.addComponent(state);
            }
        }
        if (groups != null) {
            for (HudGroup group : groups) {
                data.addGroup(group);
            }
        }
        this.clipboardData = data;
        LOGGER.info("Copied {} components to HUD clipboard", data.getComponents().size());
    }

    public synchronized void copyComponents(HudLayout layout, Collection<String> componentIds) {
        if (layout == null || componentIds == null) return;
        List<HudComponentState> states = new ArrayList<>();
        List<HudGroup> groups = new ArrayList<>();
        for (String id : componentIds) {
            HudComponentState state = layout.getComponentState(id);
            if (state != null) {
                states.add(new HudComponentState(state));
                if (state.getGroupId() != null) {
                    HudGroup g = layout.getGroup(state.getGroupId());
                    if (g != null && !groups.contains(g)) {
                        groups.add(g);
                    }
                }
            }
        }
        copy(states, groups, layout.getLayoutId());
    }

    public synchronized boolean hasContent() {
        return clipboardData != null && !clipboardData.isEmpty();
    }

    public synchronized HudClipboardData getClipboardData() {
        return clipboardData;
    }

    public synchronized List<String> paste(HudLayout targetLayout, PasteMode mode, int cursorVirtualX, int cursorVirtualY) {
        if (!hasContent() || targetLayout == null) {
            return Collections.emptyList();
        }

        List<String> affectedIds = new ArrayList<>();
        Map<String, HudComponentState> copied = clipboardData.getComponents();

        // Calculate bounding box center of copied components for cursor placement
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (HudComponentState state : copied.values()) {
            minX = Math.min(minX, state.getPosX());
            minY = Math.min(minY, state.getPosY());
        }

        for (HudComponentState copiedState : copied.values()) {
            String compId = copiedState.getComponentId();
            HudComponentState targetState = targetLayout.getComponentState(compId);
            if (targetState == null) continue;

            if (mode == PasteMode.IN_PLACE) {
                targetState.setPosX(copiedState.getPosX() + 10);
                targetState.setPosY(copiedState.getPosY() + 10);
                targetState.setScale(copiedState.getScale());
                targetState.setAnchor(copiedState.getAnchor());
                targetState.setLayerId(copiedState.getLayerId());
                targetState.setVisible(true);
            } else if (mode == PasteMode.NEAR_CURSOR) {
                int offsetX = copiedState.getPosX() - minX;
                int offsetY = copiedState.getPosY() - minY;
                targetState.setPosX(Math.max(0, cursorVirtualX + offsetX));
                targetState.setPosY(Math.max(0, cursorVirtualY + offsetY));
                targetState.setScale(copiedState.getScale());
                targetState.setAnchor(copiedState.getAnchor());
                targetState.setVisible(true);
            } else if (mode == PasteMode.APPEARANCE_ONLY) {
                targetState.setScale(copiedState.getScale());
                targetState.setLayerId(copiedState.getLayerId());
            } else if (mode == PasteMode.POSITION_ONLY) {
                targetState.setPosX(copiedState.getPosX());
                targetState.setPosY(copiedState.getPosY());
                targetState.setAnchor(copiedState.getAnchor());
            }

            affectedIds.add(compId);
        }

        // Paste groups if target layout doesn't have them
        for (HudGroup g : clipboardData.getGroups()) {
            if (!targetLayout.getGroups().containsKey(g.getGroupId())) {
                targetLayout.addGroup(new HudGroup(g));
            }
        }

        LOGGER.info("Pasted {} components with mode {}", affectedIds.size(), mode);
        return affectedIds;
    }

    public synchronized List<String> pasteFromClipboard(HudLayout targetLayout, PasteMode mode, int cursorVirtualX, int cursorVirtualY) {
        return paste(targetLayout, mode, cursorVirtualX, cursorVirtualY);
    }

    public synchronized List<String> duplicate(HudLayout layout, Collection<String> componentIds) {
        return duplicateComponents(layout, componentIds, 15, 15);
    }

    public synchronized List<String> duplicateComponents(HudLayout layout, Collection<String> componentIds, int offsetX, int offsetY) {
        if (layout == null || componentIds == null || componentIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> duplicated = new ArrayList<>();
        for (String id : componentIds) {
            HudComponentState state = layout.getComponentState(id);
            if (state != null) {
                state.setPosX(state.getPosX() + offsetX);
                state.setPosY(state.getPosY() + offsetY);
                state.setVisible(true);
                duplicated.add(id);
            }
        }
        return duplicated;
    }

    public synchronized void clear() {
        this.clipboardData = null;
    }

    public synchronized void resetForTesting() {
        clear();
    }
}
