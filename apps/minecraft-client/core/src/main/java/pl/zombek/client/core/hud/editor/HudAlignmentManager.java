package pl.zombek.client.core.hud.editor;

/**
 * Alignment tools for positioning selected HUD components relative to virtual canvas boundaries.
 */
public class HudAlignmentManager {

    public static boolean alignLeft(HudComponentState state, HudComponentDescriptor desc, int virtW, int safeMarginX) {
        if (!canAlign(state, desc)) return false;
        int effW = state.getEffectiveWidth();
        int targetX = Math.max(0, safeMarginX);
        state.setPosX(state.getAnchor().screenXToOffsetX(targetX, effW, virtW));
        return true;
    }

    public static boolean alignRight(HudComponentState state, HudComponentDescriptor desc, int virtW, int safeMarginX) {
        if (!canAlign(state, desc)) return false;
        int effW = state.getEffectiveWidth();
        int targetX = Math.max(0, virtW - effW - safeMarginX);
        state.setPosX(state.getAnchor().screenXToOffsetX(targetX, effW, virtW));
        return true;
    }

    public static boolean alignTop(HudComponentState state, HudComponentDescriptor desc, int virtH, int safeMarginY) {
        if (!canAlign(state, desc)) return false;
        int effH = state.getEffectiveHeight();
        int targetY = Math.max(0, safeMarginY);
        state.setPosY(state.getAnchor().screenYToOffsetY(targetY, effH, virtH));
        return true;
    }

    public static boolean alignBottom(HudComponentState state, HudComponentDescriptor desc, int virtH, int safeMarginY) {
        if (!canAlign(state, desc)) return false;
        int effH = state.getEffectiveHeight();
        int targetY = Math.max(0, virtH - effH - safeMarginY);
        state.setPosY(state.getAnchor().screenYToOffsetY(targetY, effH, virtH));
        return true;
    }

    public static boolean centerHorizontally(HudComponentState state, HudComponentDescriptor desc, int virtW) {
        if (!canAlign(state, desc)) return false;
        int effW = state.getEffectiveWidth();
        int targetX = Math.max(0, (virtW - effW) / 2);
        state.setPosX(state.getAnchor().screenXToOffsetX(targetX, effW, virtW));
        return true;
    }

    public static boolean centerVertically(HudComponentState state, HudComponentDescriptor desc, int virtH) {
        if (!canAlign(state, desc)) return false;
        int effH = state.getEffectiveHeight();
        int targetY = Math.max(0, (virtH - effH) / 2);
        state.setPosY(state.getAnchor().screenYToOffsetY(targetY, effH, virtH));
        return true;
    }

    public static boolean centerOnCanvas(HudComponentState state, HudComponentDescriptor desc, int virtW, int virtH) {
        boolean h = centerHorizontally(state, desc, virtW);
        boolean v = centerVertically(state, desc, virtH);
        return h && v;
    }

    private static boolean canAlign(HudComponentState state, HudComponentDescriptor desc) {
        if (state == null || desc == null) return false;
        if (state.isLocked()) return false;
        if (desc.getType() == HudComponentType.WORLD_SPACE) return false;
        return desc.hasCapability(HudComponentCapability.POSITION);
    }
}
