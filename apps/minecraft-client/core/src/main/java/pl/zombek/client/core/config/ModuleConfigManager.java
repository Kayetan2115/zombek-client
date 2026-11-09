package pl.zombek.client.core.config;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.ClientConstants;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.settings.Setting;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Handles JSON serialization and persistence for module states, keybinds, and settings across all loaders.
 * Supports atomic file writes, debouncing to prevent disk spam, schema versioning,
 * and automatic corrupt file recovery.
 */
public class ModuleConfigManager implements IClientConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleConfigManager.class);
    private static final int CURRENT_SCHEMA_VERSION = 1;
    private static final long DEFAULT_DEBOUNCE_MS = 300L;

    private final Path configDir;
    private final Path configFile;
    private final ModuleManager moduleManager;
    private final Gson gson;
    private final ScheduledExecutorService debounceExecutor;
    private ScheduledFuture<?> pendingSaveTask;
    private final Object saveLock = new Object();

    private JsonObject rawRootObject = new JsonObject();

    public ModuleConfigManager(Path configDir, ModuleManager moduleManager) {
        this.configDir = configDir;
        this.configFile = configDir.resolve("modules.json");
        this.moduleManager = moduleManager;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.debounceExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ZombekClient-ConfigSaver");
            t.setDaemon(true);
            return t;
        });

        this.moduleManager.addChangeListener(this::scheduleSave);
    }

    public ModuleConfigManager(Path configDir) {
        this(configDir, ModuleManager.getInstance());
    }

    @Override
    public String getConfigName() {
        return "modules.json";
    }

    public Path getConfigFile() {
        return configFile;
    }

    public Path getConfigDir() {
        return configDir;
    }

    public void scheduleSave() {
        synchronized (saveLock) {
            if (pendingSaveTask != null && !pendingSaveTask.isDone()) {
                pendingSaveTask.cancel(false);
            }
            pendingSaveTask = debounceExecutor.schedule(this::saveNow, DEFAULT_DEBOUNCE_MS, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void save() {
        saveNow();
    }

    public void saveNow() {
        synchronized (saveLock) {
            try {
                if (!Files.exists(configDir)) {
                    Files.createDirectories(configDir);
                }

                JsonObject root = rawRootObject != null ? rawRootObject.deepCopy() : new JsonObject();
                root.addProperty("schemaVersion", CURRENT_SCHEMA_VERSION);
                root.addProperty("clientVersion", ClientConstants.CLIENT_VERSION);

                JsonObject modulesNode = root.has("modules") && root.get("modules").isJsonObject()
                        ? root.getAsJsonObject("modules")
                        : new JsonObject();

                for (AbstractModule module : moduleManager.getModules()) {
                    JsonObject modNode = modulesNode.has(module.getId()) && modulesNode.get(module.getId()).isJsonObject()
                            ? modulesNode.getAsJsonObject(module.getId())
                            : new JsonObject();

                    modNode.addProperty("enabled", module.isEnabled());
                    modNode.addProperty("keybind", module.getKeybind());

                    JsonObject settingsNode = modNode.has("settings") && modNode.get("settings").isJsonObject()
                            ? modNode.getAsJsonObject("settings")
                            : new JsonObject();

                    for (Setting<?> setting : module.getSettings()) {
                        settingsNode.addProperty(setting.getId(), setting.serialize());
                    }

                    modNode.add("settings", settingsNode);
                    modulesNode.add(module.getId(), modNode);
                }

                root.add("modules", modulesNode);
                this.rawRootObject = root;

                Path tempFile = configFile.resolveSibling(configFile.getFileName() + ".tmp");
                try (Writer writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                    gson.toJson(root, writer);
                    writer.flush();
                }

                try {
                    Files.move(tempFile, configFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(tempFile, configFile, StandardCopyOption.REPLACE_EXISTING);
                }

                LOGGER.info("Saved ZombekClient module configuration to {}", configFile);
            } catch (Exception e) {
                LOGGER.error("Failed to save module configuration: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void load() {
        synchronized (saveLock) {
            if (!Files.exists(configFile)) {
                LOGGER.info("Configuration file '{}' does not exist yet. Using defaults.", configFile);
                return;
            }

            try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
                JsonElement element = JsonParser.parseReader(reader);
                if (!element.isJsonObject()) {
                    throw new JsonParseException("Expected root JSON object in " + configFile);
                }

                JsonObject root = element.getAsJsonObject();
                this.rawRootObject = root;

                if (!root.has("modules") || !root.get("modules").isJsonObject()) {
                    LOGGER.warn("Configuration missing 'modules' object in {}", configFile);
                    return;
                }

                JsonObject modulesNode = root.getAsJsonObject("modules");

                // Etap 8A6 Migration: If "uhc_overlay" contains legacy session settings and "uhc_session_hud" does not exist,
                // migrate previous HUD session settings to "uhc_session_hud".
                if (modulesNode.has("uhc_overlay") && !modulesNode.has("uhc_session_hud")) {
                    JsonElement elem = modulesNode.get("uhc_overlay");
                    if (elem.isJsonObject()) {
                        JsonObject overlayObj = elem.getAsJsonObject();
                        if (overlayObj.has("settings") && overlayObj.get("settings").isJsonObject()) {
                            JsonObject s = overlayObj.getAsJsonObject("settings");
                            if (s.has("show_player_section") || s.has("show_timers_section") || s.has("uhc_mode")) {
                                modulesNode.add("uhc_session_hud", overlayObj.deepCopy());
                                modulesNode.remove("uhc_overlay");
                                LOGGER.info("Migrated legacy UHC session HUD configuration from 'uhc_overlay' to 'uhc_session_hud'.");
                            }
                        }
                    }
                }

                for (Map.Entry<String, JsonElement> entry : modulesNode.entrySet()) {
                    String moduleId = entry.getKey();
                    if (!entry.getValue().isJsonObject()) continue;

                    JsonObject modNode = entry.getValue().getAsJsonObject();
                    AbstractModule module = moduleManager.getModuleById(moduleId);
                    if (module == null) {
                        continue;
                    }

                    if (modNode.has("keybind") && modNode.get("keybind").isJsonPrimitive()) {
                        module.setKeybind(modNode.get("keybind").getAsInt());
                    }

                    if (modNode.has("settings") && modNode.get("settings").isJsonObject()) {
                        JsonObject settingsNode = modNode.getAsJsonObject("settings");
                        for (Map.Entry<String, JsonElement> settingEntry : settingsNode.entrySet()) {
                            String settingId = settingEntry.getKey();
                            Setting<?> setting = module.getSetting(settingId);
                            if (setting != null && settingEntry.getValue().isJsonPrimitive()) {
                                setting.deserialize(settingEntry.getValue().getAsString());
                            }
                        }
                    }

                    if (modNode.has("enabled") && modNode.get("enabled").isJsonPrimitive()) {
                        boolean enabled = modNode.get("enabled").getAsBoolean();
                        module.setEnabled(enabled);
                    }
                }

                LOGGER.info("Successfully loaded module configuration from {}", configFile);
            } catch (Exception e) {
                LOGGER.error("Error reading configuration file '{}'. Creating backup and loading defaults: {}",
                        configFile, e.getMessage(), e);
                backupCorruptFile();
            }
        }
    }

    private void backupCorruptFile() {
        try {
            Path backup = configFile.resolveSibling(configFile.getFileName() + ".bak");
            Files.copy(configFile, backup, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.warn("Corrupt configuration backed up to {}", backup);
        } catch (IOException e) {
            LOGGER.error("Failed to create backup for corrupt configuration: {}", e.getMessage(), e);
        }
    }

    public void shutdown() {
        saveNow();
        debounceExecutor.shutdown();
        try {
            if (!debounceExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                debounceExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            debounceExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
