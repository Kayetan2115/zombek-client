package pl.zombek.client.core.netgraph;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.TestHudRenderer;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.core.hud.component.HudPriorityResolver;
import pl.zombek.client.core.lifecycle.IClientModule;
import pl.zombek.client.core.modules.hud.info.PingModule;
import pl.zombek.client.core.modules.hud.netgraph.NetGraphModule;
import pl.zombek.client.core.platform.IPlatformAdapter;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class Etap8B7IntegrationTest {

    private ZombekCore core;

    @BeforeEach
    public void setup() {
        ZombekCore.resetInstanceForTesting();
        core = ZombekCore.init(new TestPlatformAdapter());
    }

    @AfterEach
    public void teardown() {
        ZombekCore.resetInstanceForTesting();
    }

    @Test
    public void testModuleRegistrationAndPriority() {
        IClientModule module = core.getModuleManager().getModuleById(NetGraphModule.ID);
        assertNotNull(module);
        assertTrue(module instanceof NetGraphModule);
        assertEquals("Net Graph", module.getName());

        assertEquals(36, HudPriorityResolver.PRIORITY_NET_GRAPH);
    }

    @Test
    public void testSharedPingIntegration() {
        PingModule pingModule = (PingModule) core.getModuleManager().getModuleById("ping");
        assertNotNull(pingModule);

        FallbackNetworkDataProvider netProvider = new FallbackNetworkDataProvider();
        netProvider.setStatus(ConnectionStatus.CONNECTED);
        netProvider.setPingMs(42);
        netProvider.setTrafficSupported(true);
        NetworkDataProviderRegistry.getInstance().registerProvider(netProvider);

        // Tick core
        core.onClientTick();

        // Check that LatencyTracker captured ping
        assertEquals(42, NetworkMetricsCollector.getInstance().getLatencyTracker().getCurrentPing());
        assertEquals(42, NetworkMetricsCollector.getInstance().getLatencyTracker().getLastKnownPing());
    }

    @Test
    public void testSessionResetOnDisconnect() {
        NetworkMetricsCollector.getInstance().recordPing(35);
        NetworkMetricsCollector.getInstance().recordIncomingBytes(5000);

        core.onClientTick();

        // World leave
        NetworkMetricsCollector.getInstance().onWorldLeave();
        assertEquals(ConnectionStatus.DISCONNECTED, NetworkMetricsCollector.getInstance().getStatusTracker().getCurrentStatus());
    }

    @Test
    public void testFullRenderPipeline() {
        NetGraphModule netGraph = (NetGraphModule) core.getModuleManager().getModuleById(NetGraphModule.ID);
        netGraph.setEnabled(true);

        TestHudRenderer renderer = new TestHudRenderer(1920, 1080);
        netGraph.render(renderer);

        assertTrue(renderer.fillCallCount > 0);
    }

    private static class TestPlatformAdapter implements IPlatformAdapter {
        @Override
        public String getLoaderName() { return "Test"; }
        @Override
        public String getLoaderVersion() { return "1.0.0"; }
        @Override
        public String getMinecraftVersion() { return "1.20.4"; }
        @Override
        public void openMenu() {}

        @Override
        public boolean isClient() { return true; }

        @Override
        public Path getGameDirectory() { return Path.of("."); }

        @Override
        public Path getConfigDirectory() {
            try {
                return java.nio.file.Files.createTempDirectory("zombek_etap8b7_test");
            } catch (Exception e) {
                return Path.of(".");
            }
        }
    }
}
