package pl.zombek.client.core.info;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.TestHudRenderer;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.hud.info.PingModule;

import static org.junit.jupiter.api.Assertions.*;

public class PingModuleTest {

    private PingModule module;
    private InfoHudDataProviderRegistry.FallbackInfoDataProvider fallbackProvider;

    @BeforeEach
    public void setup() {
        InfoHudDataProviderRegistry.getInstance().resetToDefault();
        fallbackProvider = (InfoHudDataProviderRegistry.FallbackInfoDataProvider) InfoHudDataProviderRegistry.getInstance().getProvider();
        module = new PingModule();
    }

    @Test
    public void testMetadataAndSettingsGroups() {
        assertEquals("ping", module.getId());
        assertEquals("Ping (Latency)", module.getName());
        assertEquals(ModuleCategory.HUD, module.getCategory());
        assertEquals(4, module.getSettingGroups().size());
    }

    @Test
    public void testSingleplayerShowsNA() {
        fallbackProvider.setSingleplayer(true);
        fallbackProvider.setPing(-1);
        module.setEnabled(true);

        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("N/A")));
        assertFalse(renderer.drawnTexts.stream().anyMatch(t -> t.contains("0 ms")));
    }

    @Test
    public void testHideInSingleplayerOption() {
        fallbackProvider.setSingleplayer(true);
        module.getHideInSingleplayer().setValue(true);
        module.setEnabled(true);

        TestHudRenderer renderer = new TestHudRenderer();
        module.render(renderer);

        assertEquals(0, renderer.drawTextCallCount);
    }

    @Test
    public void testMultiplayerRendering() {
        fallbackProvider.setSingleplayer(false);
        fallbackProvider.setPing(35);
        module.setEnabled(true);

        TestHudRenderer renderer = new TestHudRenderer();
        module.render(renderer);

        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("35 ms")));
    }
}
