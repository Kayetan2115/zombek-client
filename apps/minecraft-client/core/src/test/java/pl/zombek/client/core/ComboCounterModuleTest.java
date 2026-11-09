package pl.zombek.client.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.pvp.ComboCounterModule;
import pl.zombek.client.core.tracking.ComboTracker;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ComboCounterModuleTest {

    private ComboCounterModule module;

    static class MockHudRenderer implements IHudRenderer {
        final List<String> drawnTexts = new ArrayList<>();
        int fillCalls = 0;
        int borderCalls = 0;

        @Override public void fill(int x, int y, int width, int height, int color) { fillCalls++; }
        @Override public void drawBorder(int x, int y, int width, int height, int color, int thickness) { borderCalls++; }
        @Override public void drawText(String text, int x, int y, int color, boolean shadow, float scale, boolean bold) { drawnTexts.add(text); }
        @Override public int getTextWidth(String text) { return text.length() * 6; }
        @Override public int getFontHeight() { return 9; }
        @Override public int getScreenWidth() { return 854; }
        @Override public int getScreenHeight() { return 480; }
    }

    @BeforeEach
    void setUp() {
        HudElementRegistry.getInstance().clear();
        ComboTracker.getInstance().resetAll();
        module = new ComboCounterModule();
    }

    @AfterEach
    void tearDown() {
        module.setEnabled(false);
        HudElementRegistry.getInstance().clear();
        ComboTracker.getInstance().resetAll();
    }

    @Test
    void testModuleMetadataAndSettings() {
        assertEquals("combo_counter", module.getId());
        assertEquals("Combo Counter", module.getName());
        assertEquals(ModuleCategory.PVP, module.getCategory());
        assertFalse(module.isEnabled());

        assertNotNull(module.getResetOnTargetChange());
        assertNotNull(module.getResetOnMiss());
        assertNotNull(module.getResetOnDeath());
        assertNotNull(module.getResetTimeoutMs());
    }

    @Test
    void testMilestoneColorResolution() {
        // Tier 1 (1-3) -> 0xFFFFFFFF
        assertEquals(0xFFFFFFFF, module.resolveComboColor(1));
        assertEquals(0xFFFFFFFF, module.resolveComboColor(3));

        // Tier 2 (4-6) -> 0xFF38BDF8
        assertEquals(0xFF38BDF8, module.resolveComboColor(4));
        assertEquals(0xFF38BDF8, module.resolveComboColor(6));

        // Tier 3 (7-9) -> 0xFFF59E0B
        assertEquals(0xFFF59E0B, module.resolveComboColor(7));
        assertEquals(0xFFF59E0B, module.resolveComboColor(9));

        // Tier 4 (10+) -> 0xFFEF4444
        assertEquals(0xFFEF4444, module.resolveComboColor(10));
        assertEquals(0xFFEF4444, module.resolveComboColor(25));
    }

    @Test
    void testRenderWithCombo() {
        ComboTracker.getInstance().recordHit(100, true);

        module.setEnabled(true);
        MockHudRenderer renderer = new MockHudRenderer();

        module.render(renderer);

        assertTrue(renderer.fillCalls > 0);
        assertTrue(renderer.borderCalls > 0);
        assertFalse(renderer.drawnTexts.isEmpty());
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("1")));
    }
}
