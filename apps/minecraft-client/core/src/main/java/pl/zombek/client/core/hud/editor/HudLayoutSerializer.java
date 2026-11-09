package pl.zombek.client.core.hud.editor;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * Handles JSON serialization, deserialization, schema migration, and corrupt-state recovery
 * for HUD layouts.
 */
public class HudLayoutSerializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HudLayoutSerializer.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Serializes a layout into a formatted JSON string.
     */
    public static String toJson(HudLayout layout) {
        if (layout == null) {
            layout = new HudLayout("default", "Domyślny Układ");
        }
        return GSON.toJson(layout);
    }

    /**
     * Deserializes a layout from a JSON string with error resilience.
     */
    public static HudLayout fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return HudLayoutValidator.repairLayout(new HudLayout("default", "Domyślny Układ"));
        }
        try {
            HudLayout layout = GSON.fromJson(json, HudLayout.class);
            return HudLayoutValidator.repairLayout(layout);
        } catch (Exception e) {
            LOGGER.error("Failed to parse HUD layout JSON, using repaired defaults: {}", e.getMessage());
            return HudLayoutValidator.repairLayout(new HudLayout("default", "Domyślny Układ"));
        }
    }

    /**
     * Loads a layout from a file. If file does not exist or is corrupt, returns a valid default layout.
     */
    public static HudLayout loadFromFile(Path filePath) {
        if (filePath == null || !Files.exists(filePath)) {
            LOGGER.info("No HUD layout file found at {}, initializing defaults.", filePath);
            return HudLayoutValidator.repairLayout(new HudLayout("default", "Domyślny Układ"));
        }

        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            HudLayout layout = GSON.fromJson(reader, HudLayout.class);
            return HudLayoutValidator.repairLayout(layout);
        } catch (Exception e) {
            LOGGER.warn("Corrupt HUD layout file detected at {}. Backing up and restoring defaults.", filePath, e);
            try {
                Path backup = filePath.resolveSibling(filePath.getFileName().toString() + ".corrupt." + System.currentTimeMillis());
                Files.copy(filePath, backup, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
            }
            return HudLayoutValidator.repairLayout(new HudLayout("default", "Domyślny Układ"));
        }
    }

    /**
     * Atomically saves a layout to disk using a temporary file.
     */
    public static void saveToFile(HudLayout layout, Path filePath) throws IOException {
        if (filePath == null) return;
        Path parent = filePath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        layout.setUpdatedAt(System.currentTimeMillis());
        Path tempFile = filePath.resolveSibling(filePath.getFileName().toString() + ".tmp");

        try (Writer writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            GSON.toJson(layout, writer);
        }

        try {
            Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
