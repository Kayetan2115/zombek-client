package pl.zombek.client.core.rendereffect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.rendereffect.glint.*;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GlintRenderControllerTest {

    private GlintRenderController controller;

    @BeforeEach
    public void setup() {
        controller = GlintRenderController.getInstance();
        controller.reset();
        controller.setActive(true);
    }

    @Test
    public void testStaticGlintColorCalculation() {
        GlintSettings settings = new GlintSettings(
                GlintMode.STATIC,
                0xFFFF0000, // Red
                0x00000000,
                1.0f,
                1.0f,
                ShinyPotsPriority.PRESERVE_SHINY_POTS,
                EnumSet.allOf(GlintLocation.class),
                new HashMap<>()
        );
        controller.updateSettings(settings);

        int resolved = controller.resolveGlintColor(
                "minecraft:diamond_sword",
                true,
                GlintLocation.INVENTORY,
                1000L
        );

        // Alpha = 255, Red = 255, Green = 0, Blue = 0
        assertEquals(0xFFFF0000, resolved, "Should resolve to full red static glint");
    }

    @Test
    public void testRainbowAnimationCyclesHue() {
        int color1 = GlintAnimationController.calculateColor(
                GlintMode.RAINBOW, 0xFFFFFFFF, 0xFFFFFFFF, 1.0f, 1.0f, 0L
        );
        int color2 = GlintAnimationController.calculateColor(
                GlintMode.RAINBOW, 0xFFFFFFFF, 0xFFFFFFFF, 1.0f, 1.0f, 1500L
        );

        assertNotEquals(color1, color2, "Rainbow animation should advance hues over time");
        assertEquals(0xFF000000, color1 & 0xFF000000, "Alpha should be full opacity");
    }

    @Test
    public void testCategoryOverrides() {
        Map<GlintItemCategory, CategoryGlintConfig> catMap = new HashMap<>();
        catMap.put(GlintItemCategory.WEAPONS, new CategoryGlintConfig(true, GlintMode.STATIC, 0xFFFF1111, 0));
        catMap.put(GlintItemCategory.TOOLS, new CategoryGlintConfig(true, GlintMode.STATIC, 0xFF11FF11, 0));

        GlintSettings settings = new GlintSettings(
                GlintMode.STATIC,
                0xFF8040CC, // Global purple
                0,
                1.0f,
                1.0f,
                ShinyPotsPriority.PRESERVE_SHINY_POTS,
                EnumSet.allOf(GlintLocation.class),
                catMap
        );
        controller.updateSettings(settings);

        int swordColor = controller.resolveGlintColor("minecraft:netherite_sword", true, GlintLocation.HOTBAR, 100L);
        int pickaxeColor = controller.resolveGlintColor("minecraft:diamond_pickaxe", true, GlintLocation.HOTBAR, 100L);
        int bookColor = controller.resolveGlintColor("minecraft:enchanted_book", true, GlintLocation.HOTBAR, 100L);

        assertEquals(0xFFFF1111, swordColor, "Weapons should have custom red glint");
        assertEquals(0xFF11FF11, pickaxeColor, "Tools should have custom green glint");
        assertEquals(0xFF8040CC, bookColor, "Books without override should fall back to global glint");
    }

    @Test
    public void testLocationFiltering() {
        // Only Hotbar allowed
        GlintSettings settings = new GlintSettings(
                GlintMode.STATIC,
                0xFFFF0000,
                0,
                1.0f,
                1.0f,
                ShinyPotsPriority.PRESERVE_SHINY_POTS,
                EnumSet.of(GlintLocation.HOTBAR),
                new HashMap<>()
        );
        controller.updateSettings(settings);

        int inHotbar = controller.resolveGlintColor("minecraft:bow", true, GlintLocation.HOTBAR, 100L);
        int inContainer = controller.resolveGlintColor("minecraft:bow", true, GlintLocation.CONTAINERS, 100L);

        assertEquals(0xFFFF0000, inHotbar);
        assertEquals(GlintAnimationController.VANILLA_GLINT_ARGB, inContainer, "Disabled locations fallback to vanilla glint");
    }
}
