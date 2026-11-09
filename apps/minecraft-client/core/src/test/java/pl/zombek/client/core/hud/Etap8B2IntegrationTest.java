package pl.zombek.client.core.hud;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.zombek.client.core.TestHudRenderer;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.core.hud.bossbar.BossBarDataProviderRegistry;
import pl.zombek.client.core.hud.potions.PotionEffectsDataProviderRegistry;
import pl.zombek.client.core.hud.saturation.SaturationDataProviderRegistry;
import pl.zombek.client.core.hud.scoreboard.ScoreboardDataProviderRegistry;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.modules.hud.bossbar.BossBarModule;
import pl.zombek.client.core.modules.hud.potions.PotionEffectsModule;
import pl.zombek.client.core.modules.hud.saturation.SaturationModule;
import pl.zombek.client.core.modules.hud.scoreboard.ScoreboardModule;
import pl.zombek.client.core.platform.IPlatformAdapter;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class Etap8B2IntegrationTest {

    @TempDir
    Path tempDir;

    static class MockPlatformAdapter implements IPlatformAdapter {
        private final Path dir;

        MockPlatformAdapter(Path dir) {
            this.dir = dir;
        }

        @Override public String getLoaderName() { return "test-loader"; }
        @Override public String getLoaderVersion() { return "1.0.0"; }
        @Override public String getMinecraftVersion() { return "1.20.4"; }
        @Override public Path getGameDirectory() { return dir; }
        @Override public Path getConfigDirectory() { return dir.resolve("config").resolve("zombekclient"); }
        @Override public boolean isClient() { return true; }
        @Override public void openMenu() {}
    }

    @BeforeEach
    public void setup() {
        ZombekCore.resetInstanceForTesting();
        HudElementRegistry.getInstance().clear();
        ScoreboardDataProviderRegistry.getInstance().resetToDefault();
        PotionEffectsDataProviderRegistry.getInstance().resetToDefault();
        SaturationDataProviderRegistry.getInstance().resetToDefault();
        BossBarDataProviderRegistry.getInstance().resetToDefault();
        ModuleManager.getInstance().clear();
    }

    @AfterEach
    public void tearDown() {
        HudElementRegistry.getInstance().clear();
        ZombekCore.resetInstanceForTesting();
        ModuleManager.getInstance().clear();
    }

    @Test
    public void testZombekCoreRegistration() {
        ZombekCore core = ZombekCore.init(new MockPlatformAdapter(tempDir));

        ModuleManager mm = core.getModuleManager();
        assertNotNull(mm.getModuleById("scoreboard"));
        assertNotNull(mm.getModuleById("potion_effects"));
        assertNotNull(mm.getModuleById("saturation"));
        assertNotNull(mm.getModuleById("boss_bar"));

        assertEquals(ModuleCategory.HUD, mm.getModuleById("scoreboard").getCategory());
        assertEquals(ModuleCategory.HUD, mm.getModuleById("potion_effects").getCategory());
        assertEquals(ModuleCategory.HUD, mm.getModuleById("saturation").getCategory());
        assertEquals(ModuleCategory.HUD, mm.getModuleById("boss_bar").getCategory());
    }

    @Test
    public void testAllFourModulesCoexistenceAndRendering() {
        ScoreboardModule scoreboard = new ScoreboardModule();
        PotionEffectsModule potions = new PotionEffectsModule();
        SaturationModule saturation = new SaturationModule();
        BossBarModule bossBar = new BossBarModule();

        // Enable preview mode on all for full rendering test
        scoreboard.getPreviewModeSetting().setValue(true);
        potions.getPreviewModeSetting().setValue(true);
        saturation.getPreviewModeSetting().setValue(true);
        bossBar.getPreviewModeSetting().setValue(true);

        scoreboard.setEnabled(true);
        potions.setEnabled(true);
        saturation.setEnabled(true);
        bossBar.setEnabled(true);

        assertEquals(4, HudElementRegistry.getInstance().getElements().size());

        TestHudRenderer renderer = new TestHudRenderer(1920, 1080);
        HudElementRegistry.getInstance().renderAll(renderer);

        assertTrue(renderer.drawTextCallCount > 10);
        assertTrue(renderer.fillCallCount > 5);

        // Turn off one by one
        scoreboard.setEnabled(false);
        assertEquals(3, HudElementRegistry.getInstance().getElements().size());

        potions.setEnabled(false);
        assertEquals(2, HudElementRegistry.getInstance().getElements().size());

        saturation.setEnabled(false);
        assertEquals(1, HudElementRegistry.getInstance().getElements().size());

        bossBar.setEnabled(false);
        assertEquals(0, HudElementRegistry.getInstance().getElements().size());
    }
}
