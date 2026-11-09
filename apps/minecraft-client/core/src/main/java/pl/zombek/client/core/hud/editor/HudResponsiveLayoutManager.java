package pl.zombek.client.core.hud.editor;

import java.util.*;

/**
 * Manages responsive layout testing, simulated aspect ratios (16:9, 16:10, 21:9, 32:9, 4:3),
 * and layout validation warnings (out-of-bounds, overlaps, safe-area violations).
 */
public class HudResponsiveLayoutManager {
    private static final HudResponsiveLayoutManager INSTANCE = new HudResponsiveLayoutManager();

    private HudAspectRatio simulatedAspectRatio = HudAspectRatio.RATIO_16_9;
    private boolean simulateEnabled = false;

    private HudResponsiveLayoutManager() {
    }

    public static HudResponsiveLayoutManager getInstance() {
        return INSTANCE;
    }

    public HudAspectRatio getSimulatedAspectRatio() {
        return simulatedAspectRatio;
    }

    public HudAspectRatio getActiveRatio() {
        return simulatedAspectRatio;
    }

    public void setSimulatedAspectRatio(HudAspectRatio simulatedAspectRatio) {
        this.simulatedAspectRatio = simulatedAspectRatio != null ? simulatedAspectRatio : HudAspectRatio.RATIO_16_9;
    }

    public void setActiveRatio(HudAspectRatio simulatedAspectRatio) {
        setSimulatedAspectRatio(simulatedAspectRatio);
    }

    public void cycleAspectRatio() {
        HudAspectRatio[] values = HudAspectRatio.values();
        int idx = (simulatedAspectRatio.ordinal() + 1) % values.length;
        this.simulatedAspectRatio = values[idx];
    }

    public List<LayoutValidationWarning> validateLayout(HudLayout layout) {
        return validateLayout(layout, 960, 540);
    }

    public boolean isSimulateEnabled() {
        return simulateEnabled;
    }

    public void setSimulateEnabled(boolean simulateEnabled) {
        this.simulateEnabled = simulateEnabled;
    }

    /**
     * Calculates simulated viewport dimension for previewing responsive adaptation.
     */
    public int getSimulatedWidth(int baseWidth, int baseHeight) {
        if (!simulateEnabled) return baseWidth;
        int simW = (int) Math.round(baseHeight * simulatedAspectRatio.getRatio());
        return Math.min(baseWidth, simW);
    }

    public int getSimulatedHeight(int baseWidth, int baseHeight) {
        if (!simulateEnabled) return baseHeight;
        int simH = (int) Math.round(baseWidth / simulatedAspectRatio.getRatio());
        return Math.min(baseHeight, simH);
    }

    /**
     * Validates a layout against bounds, safe-area margins, and overlapping elements.
     */
    public List<LayoutValidationWarning> validateLayout(HudLayout layout, int screenW, int screenH) {
        if (layout == null) return Collections.emptyList();
        List<LayoutValidationWarning> warnings = new ArrayList<>();

        List<HudComponentState> visibleComps = new ArrayList<>();
        for (HudComponentState state : layout.getComponents().values()) {
            if (state.isVisible()) {
                visibleComps.add(state);
            }
        }

        // 1. Out of Bounds & Safe Area Check
        int safeMargin = 8;
        for (HudComponentState state : visibleComps) {
            int sx = state.calculateRawScreenX(screenW);
            int sy = state.calculateRawScreenY(screenH);
            int sw = state.getEffectiveWidth();
            int sh = state.getEffectiveHeight();

            if (sx < 0 || sy < 0 || sx + sw > screenW || sy + sh > screenH) {
                warnings.add(new LayoutValidationWarning(
                        state.getComponentId(),
                        LayoutValidationWarning.WarningType.OUT_OF_BOUNDS,
                        LayoutValidationWarning.Severity.ERROR,
                        "Komponent '" + state.getComponentId() + "' wykracza poza obszar ekranu (" + sx + ", " + sy + ")."
                ));
            } else if (sx < safeMargin || sy < safeMargin || sx + sw > screenW - safeMargin || sy + sh > screenH - safeMargin) {
                warnings.add(new LayoutValidationWarning(
                        state.getComponentId(),
                        LayoutValidationWarning.WarningType.SAFE_AREA_VIOLATION,
                        LayoutValidationWarning.Severity.WARNING,
                        "Komponent '" + state.getComponentId() + "' znajduje się w strefie marginesu bezpiecznego."
                ));
            }

            // Scale bounds
            if (state.getScale() < 0.25 || state.getScale() > 3.0) {
                warnings.add(new LayoutValidationWarning(
                        state.getComponentId(),
                        LayoutValidationWarning.WarningType.INVALID_SCALE,
                        LayoutValidationWarning.Severity.WARNING,
                        "Skala komponentu '" + state.getComponentId() + "' (" + state.getScale() + ") jest poza zakresem zalecanym (0.25x - 3.00x)."
                ));
            }
        }

        // 2. Overlap Check (two visible components overlapping with the same Z-Index)
        for (int i = 0; i < visibleComps.size(); i++) {
            HudComponentState a = visibleComps.get(i);
            int ax = a.calculateScreenX(screenW);
            int ay = a.calculateScreenY(screenH);
            int aw = a.getEffectiveWidth();
            int ah = a.getEffectiveHeight();

            for (int j = i + 1; j < visibleComps.size(); j++) {
                HudComponentState b = visibleComps.get(j);
                if (a.getZIndex() == b.getZIndex()) {
                    int bx = b.calculateScreenX(screenW);
                    int by = b.calculateScreenY(screenH);
                    int bw = b.getEffectiveWidth();
                    int bh = b.getEffectiveHeight();

                    // Check overlap
                    if (!(ax + aw <= bx || ax >= bx + bw || ay + ah <= by || ay >= by + bh)) {
                        warnings.add(new LayoutValidationWarning(
                                a.getComponentId(),
                                LayoutValidationWarning.WarningType.OVERLAP,
                                LayoutValidationWarning.Severity.INFO,
                                "Komponent '" + a.getComponentId() + "' nakłada się na '" + b.getComponentId() + "' na tej samej warstwie Z=" + a.getZIndex() + "."
                        ));
                    }
                }
            }
        }

        return warnings;
    }

    public synchronized void resetForTesting() {
        simulatedAspectRatio = HudAspectRatio.RATIO_16_9;
        simulateEnabled = false;
    }
}
