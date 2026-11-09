package pl.zombek.client.forge;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.core.config.ModuleConfigManager;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.modules.impl.CoreTestModule;
import pl.zombek.client.core.platform.IPlatformAdapter;
import pl.zombek.client.forge.integration.ForgePlatformAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ForgeCoreIntegrationTest {

    @TempDir
    Path tempDir;

    static class CustomForgeAdapter extends ForgePlatformAdapter {
        private final Path customConfigDir;

        CustomForgeAdapter(Path customConfigDir) {
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
    void testForgeCoreBootstrapAndModuleInteraction() throws IOException {
        CustomForgeAdapter adapter = new CustomForgeAdapter(tempDir.resolve("config").resolve("zombekclient"));
        ZombekCore core = ZombekCore.init(adapter);

        assertNotNull(core);
        assertEquals("forge", core.getPlatform().getLoaderName());

        // Verify CoreTestModule presence
        CoreTestModule testMod = (CoreTestModule) core.getModuleManager().getModuleById("core_test_module");
        assertNotNull(testMod);
        assertFalse(testMod.isEnabled());

        // Toggle module
        testMod.setEnabled(true);
        assertTrue(testMod.isEnabled());

        // Modify settings
        testMod.setKeybind(GLFW.GLFW_KEY_O);
        testMod.getOpacity().setValue(35.0);
        testMod.getDisplayMode().setValue(CoreTestModule.TestDisplayMode.COMPACT);
        testMod.getCustomText().setValue("ForgeCustomText");

        // Force save
        core.getConfigManager().saveNow();

        Path configFile = core.getConfigManager().getConfigFile();
        assertTrue(Files.exists(configFile), "modules.json must exist in Forge config directory");

        String content = Files.readString(configFile);
        assertTrue(content.contains("\"core_test_module\""));
        assertTrue(content.contains("\"ForgeCustomText\""));
        assertTrue(content.contains("\"COMPACT\""));

        // Reset and reload
        testMod.setEnabled(false);
        testMod.getCustomText().setValue("Reset");
        core.getConfigManager().load();

        assertTrue(testMod.isEnabled(), "Enabled state should be reloaded");
        assertEquals("ForgeCustomText", testMod.getCustomText().getValue(), "Custom text should be reloaded");
        assertEquals(CoreTestModule.TestDisplayMode.COMPACT, testMod.getDisplayMode().getValue());
    }
}
