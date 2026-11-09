package pl.zombek.client.quilt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.modules.impl.CoreTestModule;
import pl.zombek.client.quilt.config.QuiltConfigHelper;
import pl.zombek.client.quilt.platform.QuiltPlatformAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class QuiltCoreIntegrationTest {

    @TempDir
    Path tempConfigDir;

    @BeforeEach
    public void setup() {
        QuiltConfigHelper.setConfigDirectoryOverride(tempConfigDir.resolve("config").resolve("zombekclient"));
        ZombekCore.resetInstanceForTesting();
        ModuleManager.getInstance().clear();
    }

    @AfterEach
    public void tearDown() {
        QuiltConfigHelper.setConfigDirectoryOverride(null);
        ZombekCore.resetInstanceForTesting();
        ModuleManager.getInstance().clear();
    }

    @Test
    public void testCoreIntegrationAndPersistence() throws IOException {
        QuiltPlatformAdapter adapter = new QuiltPlatformAdapter();
        ZombekCore core = ZombekCore.init(adapter);

        assertNotNull(core);
        assertEquals("quilt", core.getPlatform().getLoaderName());

        // Verify CoreTestModule presence
        CoreTestModule testMod = (CoreTestModule) core.getModuleManager().getModuleById("core_test_module");
        assertNotNull(testMod, "CoreTestModule must be registered in ModuleManager");
        assertFalse(testMod.isEnabled());

        // Toggle module
        testMod.setEnabled(true);
        assertTrue(testMod.isEnabled());

        // Modify settings
        testMod.setKeybind(GLFW.GLFW_KEY_K);
        testMod.getOpacity().setValue(60.0);
        testMod.getDisplayMode().setValue(CoreTestModule.TestDisplayMode.EXPANDED);
        testMod.getCustomText().setValue("QuiltClientTest");

        // Force save to disk
        core.getConfigManager().saveNow();

        Path configFile = core.getConfigManager().getConfigFile();
        assertTrue(Files.exists(configFile), "modules.json must exist in Quilt config directory");

        String content = Files.readString(configFile);
        assertTrue(content.contains("\"core_test_module\""), "JSON should contain core_test_module");
        assertTrue(content.contains("\"QuiltClientTest\""), "JSON should contain custom text");
        assertTrue(content.contains("\"EXPANDED\""), "JSON should contain EXPANDED display mode");

        // Reset state and reload from config
        testMod.setEnabled(false);
        testMod.getCustomText().setValue("Reset");
        core.getConfigManager().load();

        assertTrue(testMod.isEnabled(), "Enabled state should be reloaded from disk");
        assertEquals("QuiltClientTest", testMod.getCustomText().getValue(), "Custom text should be reloaded from disk");
        assertEquals(CoreTestModule.TestDisplayMode.EXPANDED, testMod.getDisplayMode().getValue(), "DisplayMode should be reloaded");
    }
}
