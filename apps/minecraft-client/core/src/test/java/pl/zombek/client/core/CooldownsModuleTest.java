package pl.zombek.client.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.data.CooldownDataProviderRegistry;
import pl.zombek.client.core.data.CooldownItemData;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.pvp.CooldownsModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CooldownsModuleTest {

    private CooldownsModule module;

    static class MockHudRenderer implements IHudRenderer {
        final List<String> drawnTexts = new ArrayList<>();
        int fillCalls = 0;
        int borderCalls = 0;
        int itemRenderCalls = 0;

        @Override public void fill(int x, int y, int width, int height, int color) { fillCalls++; }
        @Override public void drawBorder(int x, int y, int width, int height, int color, int thickness) { borderCalls++; }
        @Override public void drawText(String text, int x, int y, int color, boolean shadow, float scale, boolean bold) { drawnTexts.add(text); }
        @Override public void renderItem(Object itemHandle, int x, int y, float scale) { itemRenderCalls++; }
        @Override public int getTextWidth(String text) { return text.length() * 6; }
        @Override public int getFontHeight() { return 9; }
        @Override public int getScreenWidth() { return 854; }
        @Override public int getScreenHeight() { return 480; }
    }

    @BeforeEach
    void setUp() {
        HudElementRegistry.getInstance().clear();
        CooldownDataProviderRegistry.reset();
        module = new CooldownsModule();
    }

    @AfterEach
    void tearDown() {
        module.setEnabled(false);
        HudElementRegistry.getInstance().clear();
        CooldownDataProviderRegistry.reset();
    }

    @Test
    void testModuleMetadataAndSettings() {
        assertEquals("cooldowns", module.getId());
        assertEquals("Cooldowns", module.getName());
        assertEquals(ModuleCategory.PVP, module.getCategory());
        assertFalse(module.isEnabled());

        assertNotNull(module.getShowItemIcon());
        assertNotNull(module.getShowItemName());
        assertNotNull(module.getShowRemainingTime());
        assertNotNull(module.getShowProgressBar());
        assertNotNull(module.getDisplayStyle());
        assertNotNull(module.getOrientation());
    }

    @Test
    void testRenderWithMockCooldowns() {
        CooldownDataProviderRegistry.setProvider(() -> Arrays.asList(
                new CooldownItemData("ender_pearl", "Perła Kresu", 0.75f, 15.0f, "mock_pearl"),
                new CooldownItemData("chorus_fruit", "Owoc Refrenu", 0.20f, 1.0f, "mock_chorus")
        ));

        module.setEnabled(true);
        MockHudRenderer renderer = new MockHudRenderer();

        module.render(renderer);

        assertTrue(renderer.fillCalls > 0);
        assertTrue(renderer.borderCalls > 0);
        assertEquals(2, renderer.itemRenderCalls);
        assertEquals(2, renderer.drawnTexts.size());
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("15.0s") || t.contains("Perła")));
    }
}
