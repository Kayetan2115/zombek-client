package pl.zombek.client.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.pvp.KeystrokesModule;
import pl.zombek.client.core.tracking.KeyTracker;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class KeystrokesModuleTest {

    private KeystrokesModule module;

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
        KeyTracker.getInstance().resetAll();
        module = new KeystrokesModule();
    }

    @AfterEach
    void tearDown() {
        module.setEnabled(false);
        HudElementRegistry.getInstance().clear();
    }

    @Test
    void testModuleMetadataAndSettings() {
        assertEquals("keystrokes", module.getId());
        assertEquals("Keystrokes", module.getName());
        assertEquals(ModuleCategory.PVP, module.getCategory());
        assertFalse(module.isEnabled());

        // Check essential settings presence
        assertNotNull(module.getShowW());
        assertNotNull(module.getShowA());
        assertNotNull(module.getShowS());
        assertNotNull(module.getShowD());
        assertNotNull(module.getShowLmb());
        assertNotNull(module.getShowRmb());
        assertNotNull(module.getShowSpace());
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
        assertTrue(HudElementRegistry.getInstance().getElements().contains(module));

        module.setEnabled(false);
        assertFalse(module.isEnabled());
        assertEquals(0, HudElementRegistry.getInstance().getElements().size());
    }

    @Test
    void testRenderOutput() {
        module.setEnabled(true);
        MockHudRenderer renderer = new MockHudRenderer();

        // Render default layout (W, A, S, D, LMB, RMB, SPACE)
        module.render(renderer);

        assertTrue(renderer.fillCalls > 0);
        assertTrue(renderer.borderCalls > 0);
        assertTrue(renderer.drawnTexts.contains("W"));
        assertTrue(renderer.drawnTexts.contains("A"));
        assertTrue(renderer.drawnTexts.contains("S"));
        assertTrue(renderer.drawnTexts.contains("D"));
        assertTrue(renderer.drawnTexts.contains("SPACE"));
    }
}
