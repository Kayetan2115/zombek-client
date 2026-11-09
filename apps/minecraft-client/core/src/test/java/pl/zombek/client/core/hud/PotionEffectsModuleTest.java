package pl.zombek.client.core.hud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.TestHudRenderer;
import pl.zombek.client.core.hud.potions.*;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.hud.potions.PotionEffectsModule;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PotionEffectsModuleTest {

    private PotionEffectsModule module;
    private FallbackPotionEffectsDataProvider provider;

    @BeforeEach
    public void setup() {
        PotionEffectsDataProviderRegistry.getInstance().resetToDefault();
        provider = (FallbackPotionEffectsDataProvider) PotionEffectsDataProviderRegistry.getInstance().getProvider();
        provider.setPreviewMode(false);
        module = new PotionEffectsModule();
    }

    @Test
    public void testModuleMetadata() {
        assertEquals("potion_effects", module.getId());
        assertEquals("Potion Effects", module.getName());
        assertEquals(ModuleCategory.HUD, module.getCategory());
        assertEquals(5, module.getSettingGroups().size());
        assertFalse(module.isVisible());
        module.setEnabled(true);
        assertTrue(module.isVisible());
    }

    @Test
    public void testHideVanillaHudSetting() {
        module.setEnabled(true);
        module.getHideVanillaHudSetting().setValue(true);
        assertTrue(PotionEffectsModule.shouldHideVanilla());

        module.getHideVanillaHudSetting().setValue(false);
        assertFalse(PotionEffectsModule.shouldHideVanilla());
    }

    @Test
    public void testRenderEffectsWithDurationsAndAmplifiers() {
        List<StatusEffectData> list = new ArrayList<>();
        list.add(new StatusEffectData("speed", "Speed", 1, 1200, StatusEffectCategory.BENEFICIAL, 0x33EBFF));
        list.add(new StatusEffectData("strength", "Strength", 0, 400, StatusEffectCategory.BENEFICIAL, 0x932423));
        list.add(new StatusEffectData("poison", "Poison", 0, 100, StatusEffectCategory.HARMFUL, 0x4E9331));
        provider.setActiveEffects(list);

        module.setEnabled(true);
        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawTextCallCount > 0);
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("Speed II")));
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("Strength")));
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("Poison")));
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("1:00"))); // 1200 ticks = 60s
    }

    @Test
    public void testSortingModes() {
        List<StatusEffectData> list = new ArrayList<>();
        list.add(new StatusEffectData("jump_boost", "Jump Boost", 0, 1000, StatusEffectCategory.BENEFICIAL, 0));
        list.add(new StatusEffectData("regeneration", "Regeneration", 0, 200, StatusEffectCategory.BENEFICIAL, 0));
        list.add(new StatusEffectData("slowness", "Slowness", 0, 500, StatusEffectCategory.HARMFUL, 0));
        provider.setActiveEffects(list);

        module.setEnabled(true);

        for (PotionSortMode mode : PotionSortMode.values()) {
            module.getSortModeSetting().setValue(mode);
            TestHudRenderer renderer = new TestHudRenderer(800, 600);
            module.render(renderer);
            assertTrue(renderer.drawTextCallCount > 0, "Sort mode " + mode + " failed to render");
        }
    }

    @Test
    public void testLayoutModes() {
        List<StatusEffectData> list = new ArrayList<>();
        list.add(new StatusEffectData("haste", "Haste", 1, 1200, StatusEffectCategory.BENEFICIAL, 0));
        provider.setActiveEffects(list);

        module.setEnabled(true);

        for (PotionLayoutMode layout : PotionLayoutMode.values()) {
            module.getLayoutModeSetting().setValue(layout);
            TestHudRenderer renderer = new TestHudRenderer(800, 600);
            module.render(renderer);
            assertTrue(renderer.drawTextCallCount > 0 || layout == PotionLayoutMode.ICONS_ONLY,
                    "Layout mode " + layout + " failed");
        }
    }

    @Test
    public void testExpiringWarningHighlight() {
        List<StatusEffectData> list = new ArrayList<>();
        // 100 ticks = 5 seconds, under default 10s warning
        list.add(new StatusEffectData("speed", "Speed", 0, 100, StatusEffectCategory.BENEFICIAL, 0));
        provider.setActiveEffects(list);

        module.setEnabled(true);
        module.getHighlightExpiringSetting().setValue(true);
        module.getWarningTimeSetting().setValue(10.0);

        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawTextCallCount > 0);
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("Speed")));
    }

    @Test
    public void testPreviewModeFallback() {
        module.setEnabled(true);
        module.getPreviewModeSetting().setValue(true);

        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawTextCallCount > 0);
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("PREVIEW")));
    }
}
