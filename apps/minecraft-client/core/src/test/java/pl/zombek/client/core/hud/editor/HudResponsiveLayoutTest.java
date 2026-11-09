package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HudResponsiveLayoutTest {

    private HudResponsiveLayoutManager manager;
    private HudLayout layout;

    @BeforeEach
    public void setUp() {
        manager = HudResponsiveLayoutManager.getInstance();
        manager.setSimulateEnabled(false);
        manager.setSimulatedAspectRatio(HudAspectRatio.RATIO_16_9);

        layout = new HudLayout("test", "Układ Testowy");
    }

    @Test
    public void testAspectRatioDimensions() {
        manager.setSimulateEnabled(true);
        manager.setSimulatedAspectRatio(HudAspectRatio.RATIO_4_3);

        int baseW = 960;
        int baseH = 540;

        int simW = manager.getSimulatedWidth(baseW, baseH);
        int simH = manager.getSimulatedHeight(baseW, baseH);

        assertTrue(simW <= baseW);
        assertTrue(simH <= baseH);
    }

    @Test
    public void testValidationOutOfBounds() {
        // Component placed outside 960x540 screen bounds
        HudComponentState oobComp = new HudComponentState("fps", 1000, 600, 50, 20);
        oobComp.setVisible(true);
        layout.setComponentState(oobComp);

        List<LayoutValidationWarning> warnings = manager.validateLayout(layout, 960, 540);
        assertFalse(warnings.isEmpty());

        boolean hasOob = warnings.stream().anyMatch(w -> w.getType() == LayoutValidationWarning.WarningType.OUT_OF_BOUNDS);
        assertTrue(hasOob, "Expected OUT_OF_BOUNDS warning");
    }

    @Test
    public void testValidationOverlap() {
        // Two components at identical coordinates
        HudComponentState c1 = new HudComponentState("fps", 50, 50, 60, 20);
        c1.setVisible(true);
        HudComponentState c2 = new HudComponentState("ping", 50, 50, 60, 20);
        c2.setVisible(true);

        layout.setComponentState(c1);
        layout.setComponentState(c2);

        List<LayoutValidationWarning> warnings = manager.validateLayout(layout, 960, 540);
        assertFalse(warnings.isEmpty());

        boolean hasOverlap = warnings.stream().anyMatch(w -> w.getType() == LayoutValidationWarning.WarningType.OVERLAP);
        assertTrue(hasOverlap, "Expected OVERLAP warning");
    }
}
