package pl.zombek.client.core.hud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.TestHudRenderer;
import pl.zombek.client.core.hud.bossbar.*;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.hud.bossbar.BossBarModule;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class BossBarModuleTest {

    private BossBarModule module;
    private FallbackBossBarDataProvider provider;

    @BeforeEach
    public void setup() {
        BossBarDataProviderRegistry.getInstance().resetToDefault();
        provider = (FallbackBossBarDataProvider) BossBarDataProviderRegistry.getInstance().getProvider();
        provider.setPreviewMode(false);
        module = new BossBarModule();
    }

    @Test
    public void testModuleMetadata() {
        assertEquals("boss_bar", module.getId());
        assertEquals("Boss Bar", module.getName());
        assertEquals(ModuleCategory.HUD, module.getCategory());
        assertEquals(5, module.getSettingGroups().size());
        assertFalse(module.isVisible());
        module.setEnabled(true);
        assertTrue(module.isVisible());
    }

    @Test
    public void testNoRenderWhenNoBossBarAndPreviewOff() {
        module.setEnabled(true);
        module.getPreviewModeSetting().setValue(false);

        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertEquals(0, renderer.drawTextCallCount);
        assertFalse(BossBarModule.shouldOverrideVanilla());
    }

    @Test
    public void testRenderActiveBossBars() {
        List<BossBarData> list = new ArrayList<>();
        list.add(new BossBarData(UUID.randomUUID().toString(), "Smok Kresu", 0.75f, BossBarColor.PINK, BossBarOverlay.PROGRESS, false, false, false));
        provider.setActiveBossBars(list);

        module.setEnabled(true);
        assertTrue(BossBarModule.shouldOverrideVanilla());

        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawTextCallCount > 0);
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("Smok Kresu")));
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("75%")));
        assertTrue(renderer.fillCallCount > 0);
    }

    @Test
    public void testMultipleBossBarsWithStacking() {
        List<BossBarData> list = new ArrayList<>();
        list.add(new BossBarData(UUID.randomUUID().toString(), "Smok Kresu", 0.5f, BossBarColor.PINK, BossBarOverlay.PROGRESS, false, false, false));
        list.add(new BossBarData(UUID.randomUUID().toString(), "Wither", 0.3f, BossBarColor.PURPLE, BossBarOverlay.NOTCHED_6, false, false, false));
        provider.setActiveBossBars(list);

        module.setEnabled(true);
        module.getMaxBossBarsSetting().setValue(3.0);
        module.getStackDirectionSetting().setValue(StackDirection.DOWN);

        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("Smok Kresu")));
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("Wither")));
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("50%")));
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("30%")));
    }

    @Test
    public void testBossBarStyles() {
        List<BossBarData> list = new ArrayList<>();
        list.add(new BossBarData(UUID.randomUUID().toString(), "Raid Boss", 0.9f, BossBarColor.RED, BossBarOverlay.PROGRESS, false, false, false));
        provider.setActiveBossBars(list);

        module.setEnabled(true);

        for (BossBarStyle style : BossBarStyle.values()) {
            module.getBossBarStyleSetting().setValue(style);
            TestHudRenderer renderer = new TestHudRenderer(800, 600);
            module.render(renderer);
            assertTrue(renderer.drawTextCallCount > 0, "BossBarStyle " + style + " failed to render text");
            assertTrue(renderer.fillCallCount > 0, "BossBarStyle " + style + " failed to render fills");
        }
    }

    @Test
    public void testSmoothAnimationStep() {
        BossBarData bar = new BossBarData(UUID.randomUUID().toString(), "Anim Boss", 1.0f, BossBarColor.BLUE, BossBarOverlay.PROGRESS, false, false, false);
        assertEquals(1.0f, bar.getPercent());
        assertEquals(1.0f, bar.getAnimatedPercent());

        // Target changes to 0.5f
        bar.setPercent(0.5f);
        assertEquals(0.5f, bar.getPercent());

        // Animate step
        bar.updateAnimation(0.2f);
        assertTrue(bar.getAnimatedPercent() < 1.0f && bar.getAnimatedPercent() > 0.5f);
    }

    @Test
    public void testPreviewModeFallback() {
        module.setEnabled(true);
        module.getPreviewModeSetting().setValue(true);

        assertTrue(BossBarModule.shouldOverrideVanilla());
        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawTextCallCount > 0);
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("PREVIEW")));
    }
}
