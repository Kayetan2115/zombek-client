package pl.zombek.client.core.netgraph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.TestHudRenderer;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.hud.netgraph.NetGraphModule;

import static org.junit.jupiter.api.Assertions.*;

public class NetGraphModuleTest {

    private NetGraphModule module;
    private FallbackNetworkDataProvider fallbackNetProvider;

    @BeforeEach
    public void setup() {
        NetworkDataProviderRegistry.getInstance().resetForTesting();
        fallbackNetProvider = (FallbackNetworkDataProvider) NetworkDataProviderRegistry.getInstance().getProvider();
        module = new NetGraphModule();
    }

    @Test
    public void testMetadataAndSettingsGroups() {
        assertEquals("net_graph", module.getId());
        assertEquals("Net Graph", module.getName());
        assertEquals(ModuleCategory.HUD, module.getCategory());
        assertEquals(11, module.getSettingGroups().size());
    }

    @Test
    public void testResetToDefaults() {
        module.getMode().setValue(NetGraphMode.LATENCY);
        module.getGraphWidth().setValue(300.0);
        module.getPingColor().setValue(0xFFFF0000);

        module.resetToDefaults();

        assertEquals(NetGraphMode.COMBINED, module.getMode().getValue());
        assertEquals(200.0, module.getGraphWidth().getValue());
        assertEquals(0xFF55FF55, module.getPingColor().getValue());
    }

    @Test
    public void testSingleplayerRendering() {
        fallbackNetProvider.setSingleplayer(true);
        module.setEnabled(true);

        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("Singleplayer")));
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("N/A")));
        assertFalse(renderer.drawnTexts.stream().anyMatch(t -> t.contains("0 ms")));
    }

    @Test
    public void testMultiplayerRendering() {
        fallbackNetProvider.setSingleplayer(false);
        fallbackNetProvider.setStatus(ConnectionStatus.CONNECTED);
        fallbackNetProvider.setPingMs(34);
        fallbackNetProvider.setTrafficSupported(true);
        module.setEnabled(true);

        // Feed sample
        NetworkMetricsCollector.getInstance().recordPing(34);
        NetworkMetricsCollector.getInstance().recordIncomingBytes(15000);
        NetworkMetricsCollector.getInstance().recordIncomingPacket();

        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("Net Graph")));
        assertTrue(renderer.drawBorderCallCount > 0);
        assertTrue(renderer.fillCallCount > 0);
    }

    @Test
    public void testPreviewRendering() {
        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.renderPreview(renderer, 10, 10);

        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("Net Graph")));
        assertTrue(renderer.fillCallCount > 0);
    }
}
