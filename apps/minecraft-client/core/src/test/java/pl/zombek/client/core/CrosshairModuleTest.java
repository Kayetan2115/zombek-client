package pl.zombek.client.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.data.CrosshairTargetRegistry;
import pl.zombek.client.core.data.CrosshairTargetType;
import pl.zombek.client.core.data.ICrosshairTargetProvider;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.visual.CrosshairColorResolver;
import pl.zombek.client.core.modules.visual.CrosshairModule;
import pl.zombek.client.core.modules.visual.CrosshairStyle;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class CrosshairModuleTest {
    private CrosshairModule module;

    static class MockHudRenderer implements IHudRenderer {
        int fillCalls = 0;
        int borderCalls = 0;

        @Override public void fill(int x, int y, int width, int height, int color) { fillCalls++; }
        @Override public void drawBorder(int x, int y, int width, int height, int color, int thickness) { borderCalls++; }
        @Override public void drawText(String text, int x, int y, int color, boolean shadow, float scale, boolean bold) {}
        @Override public int getTextWidth(String text) { return text.length() * 6; }
        @Override public int getFontHeight() { return 9; }
        @Override public int getScreenWidth() { return 854; }
        @Override public int getScreenHeight() { return 480; }
    }

    @BeforeEach
    void setUp() {
        HudElementRegistry.getInstance().clear();
        CrosshairTargetRegistry.reset();
        module = new CrosshairModule();
    }

    @AfterEach
    void tearDown() {
        if (module != null) {
            module.setEnabled(false);
        }
        CrosshairTargetRegistry.reset();
        HudElementRegistry.getInstance().clear();
    }

    @Test
    void testModuleMetadataAndCategory() {
        assertEquals("crosshair", module.getId());
        assertEquals("Crosshair", module.getName());
        assertEquals(ModuleCategory.HUD, module.getCategory());
        assertFalse(module.isEnabled());
    }

    @Test
    void testSettingGroupsPresent() {
        assertNotNull(module.getGroup("general"));
        assertNotNull(module.getGroup("size"));
        assertNotNull(module.getGroup("colors"));
        assertNotNull(module.getGroup("dynamic"));
        assertNotNull(module.getGroup("advanced"));
    }

    @Test
    void testVanillaCrosshairSuppressionLogic() {
        // When module is disabled -> should not hide vanilla crosshair
        assertFalse(CrosshairModule.shouldHideVanillaCrosshair());

        // When module is enabled -> should hide vanilla crosshair
        module.setEnabled(true);
        assertTrue(CrosshairModule.shouldHideVanillaCrosshair());

        // When HUD is hidden (F1) -> should not hide
        CrosshairTargetRegistry.setProvider(new ICrosshairTargetProvider() {
            @Override public CrosshairTargetType getTargetType() { return CrosshairTargetType.NONE; }
            @Override public float getAttackCooldownProgress() { return 1.0f; }
            @Override public boolean isSpectator() { return false; }
            @Override public boolean isHudHidden() { return true; }
            @Override public boolean hasOpenScreen() { return false; }
        });
        assertFalse(CrosshairModule.shouldHideVanillaCrosshair());

        // When in spectator and spectator setting is false
        module.getShowInSpectator().setValue(false);
        CrosshairTargetRegistry.setProvider(new ICrosshairTargetProvider() {
            @Override public CrosshairTargetType getTargetType() { return CrosshairTargetType.NONE; }
            @Override public float getAttackCooldownProgress() { return 1.0f; }
            @Override public boolean isSpectator() { return true; }
            @Override public boolean isHudHidden() { return false; }
            @Override public boolean hasOpenScreen() { return false; }
        });
        assertFalse(CrosshairModule.shouldHideVanillaCrosshair());
    }

    @Test
    void testDynamicColorResolution() {
        CrosshairColorResolver resolver = new CrosshairColorResolver(0xFFFFFFFF);
        int mainColor = 0xFFFFFFFF;
        int targetColor = 0xFFFF0000;
        int blockColor = 0xFF00FF00;
        int entityColor = 0xFFFF0055;

        // Target: ENTITY
        int resolved = resolver.resolveColor(
                CrosshairTargetType.ENTITY, 1.0f,
                true, true, false, false, 0,
                mainColor, targetColor, blockColor, entityColor, 1000L
        );
        assertEquals(entityColor, resolved);

        // Target: BLOCK
        resolved = resolver.resolveColor(
                CrosshairTargetType.BLOCK, 1.0f,
                true, true, false, false, 0,
                mainColor, targetColor, blockColor, entityColor, 1000L
        );
        assertEquals(blockColor, resolved);

        // Target: NONE
        resolved = resolver.resolveColor(
                CrosshairTargetType.NONE, 1.0f,
                true, true, false, false, 0,
                mainColor, targetColor, blockColor, entityColor, 1000L
        );
        assertEquals(mainColor, resolved);
    }

    @Test
    void testRenderAllStylesWithoutExceptions() {
        MockHudRenderer renderer = new MockHudRenderer();
        module.setEnabled(true);

        for (CrosshairStyle style : CrosshairStyle.values()) {
            module.getStyleSetting().setValue(style);
            module.render(renderer);
            assertTrue(renderer.fillCalls > 0 || renderer.borderCalls > 0, "Style " + style + " should render pixels");
        }
    }
}
