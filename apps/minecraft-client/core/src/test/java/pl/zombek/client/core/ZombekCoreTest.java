package pl.zombek.client.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.modules.impl.CoreTestModule;
import pl.zombek.client.core.platform.IPlatformAdapter;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ZombekCoreTest {

    @TempDir
    Path tempDir;

    static class MockPlatformAdapter implements IPlatformAdapter {
        private final Path dir;
        boolean menuOpened = false;

        MockPlatformAdapter(Path dir) {
            this.dir = dir;
        }

        @Override public String getLoaderName() { return "test-loader"; }
        @Override public String getLoaderVersion() { return "1.0.0"; }
        @Override public String getMinecraftVersion() { return "1.20.4"; }
        @Override public Path getGameDirectory() { return dir; }
        @Override public Path getConfigDirectory() { return dir.resolve("config").resolve("zombekclient"); }
        @Override public boolean isClient() { return true; }
        @Override public void openMenu() { menuOpened = true; }
    }

    @BeforeEach
    void setUp() {
        ModuleManager.getInstance().clear();
    }

    @AfterEach
    void tearDown() {
        if (ZombekCore.getInstance() != null) {
            ZombekCore.getInstance().shutdown();
        }
        ModuleManager.getInstance().clear();
    }

    @Test
    void testCoreInitialization() {
        MockPlatformAdapter platform = new MockPlatformAdapter(tempDir);
        ZombekCore core = ZombekCore.init(platform);

        assertNotNull(core);
        assertSame(platform, core.getPlatform());
        assertNotNull(core.getModuleManager());
        assertNotNull(core.getConfigManager());

        // Verify CoreTestModule was auto-registered
        assertNotNull(core.getModuleManager().getModuleById("core_test_module"));

        // Verify tick delegation
        CoreTestModule testMod = (CoreTestModule) core.getModuleManager().getModuleById("core_test_module");
        testMod.setEnabled(true);
        int initialTicks = testMod.getTickCount();

        core.onClientTick();
        core.onClientTick();
        assertEquals(initialTicks + 2, testMod.getTickCount());

        // Platform menu opening
        platform.openMenu();
        assertTrue(platform.menuOpened);
    }
}
