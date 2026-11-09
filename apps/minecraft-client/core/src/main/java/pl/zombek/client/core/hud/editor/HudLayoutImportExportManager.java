package pl.zombek.client.core.hud.editor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Manages versioned JSON import and export of HUD layouts with strict security validation,
 * path traversal guards, sanitization, and automatic backup.
 */
public class HudLayoutImportExportManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(HudLayoutImportExportManager.class);
    private static final HudLayoutImportExportManager INSTANCE = new HudLayoutImportExportManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB limit

    public enum ImportMode {
        REPLACE,
        MERGE
    }

    private HudLayoutImportExportManager() {
    }

    public static HudLayoutImportExportManager getInstance() {
        return INSTANCE;
    }

    public synchronized boolean exportLayout(HudLayout layout, java.io.File targetFile, String description) {
        if (targetFile == null) return false;
        try {
            return exportLayout(layout, targetFile.toPath());
        } catch (IOException e) {
            LOGGER.error("Failed to export layout: {}", e.getMessage());
            return false;
        }
    }

    public synchronized ImportResult importLayout(java.io.File sourceFile, HudLayout targetLayout, ImportMode mode) {
        if (sourceFile == null) return new ImportResult(false, 0, null, null, "Brak pliku");
        return importLayout(sourceFile.toPath(), targetLayout, mode);
    }

    public synchronized boolean exportLayout(HudLayout layout, Path targetFile) throws IOException {
        if (layout == null || targetFile == null) return false;
        assertSafePath(targetFile);

        HudLayout exportData = layout.cloneLayout();
        sanitizePrivateData(exportData);

        Path parent = targetFile.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        Path temp = targetFile.resolveSibling(targetFile.getFileName().toString() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            GSON.toJson(exportData, writer);
        }
        Files.move(temp, targetFile, StandardCopyOption.REPLACE_EXISTING);
        LOGGER.info("Exported HUD layout '{}' to {}", layout.getName(), targetFile);
        return true;
    }

    public synchronized boolean exportSelected(HudLayout layout, Collection<String> componentIds, Path targetFile) throws IOException {
        if (layout == null || componentIds == null || targetFile == null) return false;
        assertSafePath(targetFile);

        HudLayout exportData = new HudLayout("export_" + System.currentTimeMillis(), "Eksportowane elementy");
        for (String id : componentIds) {
            HudComponentState state = layout.getComponentState(id);
            if (state != null) {
                exportData.setComponentState(new HudComponentState(state));
            }
        }
        sanitizePrivateData(exportData);

        return exportLayout(exportData, targetFile);
    }

    public synchronized ImportResult importLayout(Path sourceFile, HudLayout targetLayout, ImportMode mode) {
        if (sourceFile == null || !Files.exists(sourceFile) || targetLayout == null) {
            return new ImportResult(false, 0, null, null, "Plik źródłowy nie istnieje lub layout docelowy jest null.");
        }

        try {
            assertSafePath(sourceFile);
            long size = Files.size(sourceFile);
            if (size > MAX_FILE_SIZE_BYTES) {
                return new ImportResult(false, 0, null, null, "Rozmiar pliku przekracza dopuszczalny limit 5 MB (" + size + " bajtów).");
            }
        } catch (Exception e) {
            return new ImportResult(false, 0, null, null, "Błąd bezpieczeństwa ścieżki: " + e.getMessage());
        }

        // Auto backup target profile before import
        HudProfileManager.getInstance().backupProfile(targetLayout.getLayoutId());

        HudLayout importedLayout;
        try (Reader reader = Files.newBufferedReader(sourceFile, StandardCharsets.UTF_8)) {
            importedLayout = GSON.fromJson(reader, HudLayout.class);
            if (importedLayout == null) {
                return new ImportResult(false, 0, null, null, "Nieprawidłowy plik JSON (pusta struktura).");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to parse import JSON: {}", e.getMessage());
            return new ImportResult(false, 0, null, null, "Błąd parsowania pliku JSON: " + e.getMessage());
        }

        // Schema version check
        if (importedLayout.getSchemaVersion() > 1) {
            return new ImportResult(false, 0, null, null, "Nieobsługiwana nowsza wersja schematu HUD: " + importedLayout.getSchemaVersion());
        }

        List<String> imported = new ArrayList<>();
        List<String> unknown = new ArrayList<>();
        HudComponentRegistry registry = HudComponentRegistry.getInstance();

        if (mode == ImportMode.REPLACE) {
            // In replace mode, hide all current components and overwrite with imported layout
            for (HudComponentState cur : targetLayout.getComponents().values()) {
                cur.setVisible(false);
            }
        }

        for (Map.Entry<String, HudComponentState> entry : importedLayout.getComponents().entrySet()) {
            String compId = entry.getKey();
            HudComponentState impState = entry.getValue();

            if (registry.hasDescriptor(compId)) {
                HudComponentState state = targetLayout.getComponentState(compId);
                if (state == null) {
                    state = registry.getDescriptor(compId).orElseThrow().createDefaultState();
                    targetLayout.setComponentState(state);
                }
                state.setPosX(Math.max(0, impState.getPosX()));
                state.setPosY(Math.max(0, impState.getPosY()));
                state.setWidth(Math.max(10, impState.getWidth()));
                state.setHeight(Math.max(10, impState.getHeight()));
                state.setScale(Math.max(0.2, Math.min(3.0, impState.getScale())));
                state.setAnchor(impState.getAnchor());
                state.setVisible(impState.isVisible());
                state.setLayerId(impState.getLayerId());
                state.setGroupId(impState.getGroupId());
                imported.add(compId);
            } else {
                unknown.add(compId);
            }
        }

        // Merge groups
        if (importedLayout.getGroups() != null) {
            for (HudGroup g : importedLayout.getGroups().values()) {
                targetLayout.addGroup(new HudGroup(g));
            }
        }

        // Merge layers
        if (importedLayout.getLayers() != null) {
            for (HudLayer l : importedLayout.getLayers().values()) {
                targetLayout.addLayer(new HudLayer(l));
            }
        }

        HudLayoutValidator.repairLayout(targetLayout);
        LOGGER.info("Imported {} components ({} unknown/skipped) into layout '{}'", imported.size(), unknown.size(), targetLayout.getName());
        return new ImportResult(true, imported.size(), imported, unknown, "Pomyślnie zaimportowano " + imported.size() + " komponentów.");
    }

    private void sanitizePrivateData(HudLayout layout) {
        // Strip tokens, account secrets, and server IPs from customProps
        for (HudComponentState state : layout.getComponents().values()) {
            if (state.getCustomProps() != null) {
                state.getCustomProps().remove("accessToken");
                state.getCustomProps().remove("token");
                state.getCustomProps().remove("refreshToken");
                state.getCustomProps().remove("serverIp");
                state.getCustomProps().remove("account");
            }
        }
    }

    private void assertSafePath(Path path) throws SecurityException {
        if (path == null) throw new SecurityException("Ścieżka nie może być pusta.");
        String pathStr = path.toString();
        if (pathStr.contains("..") || pathStr.contains("\0")) {
            throw new SecurityException("Wykryto niedozwoloną sekwencję path traversal w ścieżce: " + pathStr);
        }
    }
}
