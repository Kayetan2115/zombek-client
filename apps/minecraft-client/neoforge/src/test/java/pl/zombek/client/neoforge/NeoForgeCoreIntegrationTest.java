package pl.zombek.client.neoforge;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.modules.impl.CoreTestModule;
import pl.zombek.client.neoforge.integration.NeoForgePlatformAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class NeoForgeCoreIntegrationTest {

    @TempDir
    Path tempDir;

    static class CustomNeoForgeAdapter extends NeoForgePlatformAdapter {
        private final Path customConfigDir;

        CustomNeoForgeAdapter(Path customConfigDir) {
            this.customConfigDir = customConfigDir;
        }

        @Override
        public Path getConfigDirectory() {
            return customConfigDir;
        }
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
    void testNeoForgeCoreBootstrapAndModuleInteraction() throws IOException {
        CustomNeoForgeAdapter adapter = new CustomNeoForgeAdapter(tempDir.resolve("config").resolve("zombekclient"));
        ZombekCore core = ZombekCore.init(adapter);

        assertNotNull(core);
        assertEquals("neoforge", core.getPlatform().getLoaderName());

        // Verify CoreTestModule presence from common core
        CoreTestModule testMod = (CoreTestModule) core.getModuleManager().getModuleById("core_test_module");
        assertNotNull(testMod);
        assertFalse(testMod.isEnabled());

        // Toggle module
        testMod.setEnabled(true);
        assertTrue(testMod.isEnabled());

        // Modify settings
        testMod.setKeybind(GLFW.GLFW_KEY_P);
        testMod.getOpacity().setValue(85.0);
        testMod.getDisplayMode().setValue(CoreTestModule.TestDisplayMode.EXPANDED);
        testMod.getCustomText().setValue("NeoForgeClientTest");

        // Force save to disk
        core.getConfigManager().saveNow();

        Path configFile = core.getConfigManager().getConfigFile();
        assertTrue(Files.exists(configFile), "modules.json must exist in NeoForge config directory");

        String content = Files.readString(configFile);
        assertTrue(content.contains("\"core_test_module\""));
        assertTrue(content.contains("\"NeoForgeClientTest\""));
        assertTrue(content.contains("\"EXPANDED\""));

        // Reset state and reload from config
        testMod.setEnabled(false);
        testMod.getCustomText().setValue("Reset");
        core.getConfigManager().load();

        assertTrue(testMod.isEnabled(), "Enabled state should be reloaded from disk");
        assertEquals("NeoForgeClientTest", testMod.getCustomText().getValue(), "Custom text should be reloaded from disk");
        assertEquals(CoreTestModule.TestDisplayMode.EXPANDED, testMod.getDisplayMode().getValue());
    }
}
