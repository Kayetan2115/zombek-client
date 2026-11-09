package pl.zombek.client.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.config.ModuleConfigManager;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.modules.impl.CoreTestModule;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ModuleConfigManagerTest {

    @TempDir
    Path tempDir;

    private ModuleManager manager;
    private ModuleConfigManager configManager;
    private CoreTestModule testModule;

    @BeforeEach
    void setUp() {
        manager = ModuleManager.getInstance();
        manager.clear();

        testModule = new CoreTestModule();
        manager.registerModule(testModule);

        configManager = new ModuleConfigManager(tempDir, manager);
    }

    @AfterEach
    void tearDown() {
        configManager.shutdown();
        manager.clear();
    }

    @Test
    void testSaveAndLoadConfiguration() throws IOException {
        testModule.setEnabled(true);
        testModule.setKeybind(GLFW.GLFW_KEY_X);
        testModule.getOpacity().setValue(45.0);
        testModule.getDisplayMode().setValue(CoreTestModule.TestDisplayMode.EXPANDED);
        testModule.getCustomText().setValue("ConfigSavedValue");

        configManager.saveNow();

        Path configFile = configManager.getConfigFile();
        assertTrue(Files.exists(configFile), "Configuration file should exist after save");

        String jsonContent = Files.readString(configFile, StandardCharsets.UTF_8);
        assertTrue(jsonContent.contains("\"schemaVersion\": 1"));
        assertTrue(jsonContent.contains("\"core_test_module\""));
        assertTrue(jsonContent.contains("\"ConfigSavedValue\""));

        testModule.setEnabled(false);
        testModule.setKeybind(0);
        testModule.getOpacity().setValue(80.0);
        testModule.getCustomText().setValue("Reset");

        configManager.load();

        assertTrue(testModule.isEnabled(), "Module enabled state should be restored");
        assertEquals(GLFW.GLFW_KEY_X, testModule.getKeybind(), "Keybind should be restored");
        assertEquals(45.0, testModule.getOpacity().getValue(), "Opacity should be restored");
        assertEquals(CoreTestModule.TestDisplayMode.EXPANDED, testModule.getDisplayMode().getValue());
        assertEquals("ConfigSavedValue", testModule.getCustomText().getValue());
    }

    @Test
    void testCorruptFileRecovery() throws IOException {
        Path configFile = configManager.getConfigFile();
        Files.createDirectories(configFile.getParent());
        Files.writeString(configFile, "{ invalid json corrupt content !!!", StandardCharsets.UTF_8);

        assertDoesNotThrow(() -> configManager.load(), "Corrupt file load must not throw an unhandled exception");

        Path backupFile = configFile.resolveSibling("modules.json.bak");
        assertTrue(Files.exists(backupFile), "Backup file should be created for corrupt config");
    }
}
