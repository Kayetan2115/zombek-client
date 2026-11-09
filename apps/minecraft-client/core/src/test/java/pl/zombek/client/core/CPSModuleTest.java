package pl.zombek.client.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.pvp.CPSModule;
import pl.zombek.client.core.tracking.ClickTracker;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CPSModuleTest {

    private CPSModule module;

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
        ClickTracker.getInstance().reset();
        module = new CPSModule();
    }

    @AfterEach
    void tearDown() {
        module.setEnabled(false);
        HudElementRegistry.getInstance().clear();
    }

    @Test
    void testModuleMetadataAndSettings() {
        assertEquals("cps", module.getId());
        assertEquals("CPS Counter", module.getName());
        assertEquals(ModuleCategory.PVP, module.getCategory());
        assertFalse(module.isEnabled());

        assertNotNull(module.getShowLmb());
        assertNotNull(module.getShowRmb());
        assertNotNull(module.getShowMaxCps());
        assertNotNull(module.getResetMaxCps());
        assertNotNull(module.getPositionX());
        assertNotNull(module.getPositionY());
        assertNotNull(module.getAnchor());
    }

    @Test
    void testRegistryLifecycle() {
        assertEquals(0, HudElementRegistry.getInstance().getElements().size());

        module.setEnabled(true);
        assertTrue(module.isEnabled());
        assertEquals(1, HudElementRegistry.getInstance().getElements().size());

        module.setEnabled(false);
        assertFalse(module.isEnabled());
        assertEquals(0, HudElementRegistry.getInstance().getElements().size());
    }

    @Test
    void testRenderOutputWithClicks() {
        module.setEnabled(true);
        MockHudRenderer renderer = new MockHudRenderer();

        ClickTracker.getInstance().recordLeftClick();
        ClickTracker.getInstance().recordLeftClick();
        ClickTracker.getInstance().recordRightClick();

        module.render(renderer);

        assertTrue(renderer.fillCalls > 0);
        assertTrue(renderer.borderCalls > 0);
        assertEquals(1, renderer.drawnTexts.size());

        String renderedLine = renderer.drawnTexts.get(0);
        assertTrue(renderedLine.contains("LMB: 2"));
        assertTrue(renderedLine.contains("RMB: 1"));
        assertTrue(renderedLine.contains("CPS"));
    }

    @Test
    void testResetMaxCpsSetting() {
        ClickTracker.getInstance().recordLeftClick();
        assertTrue(ClickTracker.getInstance().getMaxLeftCps() > 0);

        // Trigger setting toggle
        module.getResetMaxCps().setValue(true);

        assertEquals(0, ClickTracker.getInstance().getMaxLeftCps());
        assertFalse(module.getResetMaxCps().getValue(), "Reset switch should bounce back to false");
    }
}
