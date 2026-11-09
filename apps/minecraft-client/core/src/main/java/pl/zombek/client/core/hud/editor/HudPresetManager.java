package pl.zombek.client.core.hud.editor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.hud.HudAnchor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Manages built-in layout templates and user-created custom presets.
 */
public class HudPresetManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(HudPresetManager.class);
    private static final HudPresetManager INSTANCE = new HudPresetManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final String PRESET_DEFAULT = "default";
    public static final String PRESET_MINIMAL = "minimal";
    public static final String PRESET_PVP = "pvp";
    public static final String PRESET_SURVIVAL = "survival";
    public static final String PRESET_UHC = "uhc";
    public static final String PRESET_BUILDING = "building";
    public static final String PRESET_PERFORMANCE = "performance";
    public static final String PRESET_STREAMER = "streamer";

    private Path presetsDir;
    private final Map<String, HudPreset> presets = new LinkedHashMap<>();

    private HudPresetManager() {
        registerSystemPresets();
    }

    public static HudPresetManager getInstance() {
        return INSTANCE;
    }

    public synchronized void initialize(Path configDir) {
        if (configDir != null) {
            this.presetsDir = configDir.resolve("zombekclient").resolve("hud_presets");
            try {
                if (!Files.exists(presetsDir)) {
                    Files.createDirectories(presetsDir);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to create presets directory: {}", e.getMessage());
            }
            loadCustomPresets();
        }
    }

    private void registerSystemPresets() {
        // 1. DEFAULT
        HudPreset def = new HudPreset(PRESET_DEFAULT, "Domyślny", "Zbalansowany uniwersalny układ dla każdego gracza", true);
        addTemplateState(def, "coordinates", 10, 10, HudAnchor.TOP_LEFT, 1.0, true);
        addTemplateState(def, "fps", 10, 44, HudAnchor.TOP_LEFT, 1.0, true);
        addTemplateState(def, "ping", 10, 66, HudAnchor.TOP_LEFT, 1.0, true);
        addTemplateState(def, "armor_status", 10, 10, HudAnchor.BOTTOM_LEFT, 1.0, true);
        presets.put(PRESET_DEFAULT, def);

        // 2. MINIMAL
        HudPreset min = new HudPreset(PRESET_MINIMAL, "Minimalny", "Tylko niezbędne wskaźniki i maksymalna przestrzeń gry", true);
        addTemplateState(min, "coordinates", 8, 8, HudAnchor.TOP_LEFT, 0.85, true);
        addTemplateState(min, "fps", 8, 32, HudAnchor.TOP_LEFT, 0.85, true);
        presets.put(PRESET_MINIMAL, min);

        // 3. PVP
        HudPreset pvp = new HudPreset(PRESET_PVP, "PvP", "Kompletny zestaw modułów do walki i pojedynków", true);
        addTemplateState(pvp, "keystrokes", 10, 10, HudAnchor.BOTTOM_RIGHT, 1.0, true);
        addTemplateState(pvp, "cps", 10, 120, HudAnchor.BOTTOM_RIGHT, 1.0, true);
        addTemplateState(pvp, "armor_status", 10, 10, HudAnchor.BOTTOM_LEFT, 1.0, true);
        addTemplateState(pvp, "combo_counter", 0, 80, HudAnchor.CENTER, 1.0, true);
        addTemplateState(pvp, "reach_display", 0, 100, HudAnchor.CENTER, 1.0, true);
        addTemplateState(pvp, "ping", 10, 10, HudAnchor.TOP_RIGHT, 1.0, true);
        addTemplateState(pvp, "fps", 10, 32, HudAnchor.TOP_RIGHT, 1.0, true);
        presets.put(PRESET_PVP, pvp);

        // 4. SURVIVAL
        HudPreset sur = new HudPreset(PRESET_SURVIVAL, "Survival", "Informacje o świecie, czasie, inwentarzu i statusie", true);
        addTemplateState(sur, "coordinates", 10, 10, HudAnchor.TOP_LEFT, 1.0, true);
        addTemplateState(sur, "direction_hud", 0, 10, HudAnchor.TOP_CENTER, 1.0, true);
        addTemplateState(sur, "item_counter", 10, 10, HudAnchor.BOTTOM_LEFT, 1.0, true);
        addTemplateState(sur, "item_tracker", 10, 70, HudAnchor.BOTTOM_LEFT, 1.0, true);
        addTemplateState(sur, "potion_effects", 10, 10, HudAnchor.TOP_RIGHT, 1.0, true);
        addTemplateState(sur, "saturation", 10, 110, HudAnchor.BOTTOM_RIGHT, 1.0, true);
        addTemplateState(sur, "clock", 10, 36, HudAnchor.TOP_LEFT, 1.0, true);
        addTemplateState(sur, "day_counter", 10, 56, HudAnchor.TOP_LEFT, 1.0, true);
        presets.put(PRESET_SURVIVAL, sur);

        // 5. UHC
        HudPreset uhc = new HudPreset(PRESET_UHC, "UHC", "Układ do turniejów Ultra Hardcore z czasem i pancerzem", true);
        addTemplateState(uhc, "coordinates", 10, 10, HudAnchor.TOP_LEFT, 0.9, true);
        addTemplateState(uhc, "armor_status", 10, 10, HudAnchor.BOTTOM_LEFT, 1.0, true);
        addTemplateState(uhc, "potion_effects", 10, 10, HudAnchor.TOP_RIGHT, 0.9, true);
        addTemplateState(uhc, "saturation", 10, 80, HudAnchor.BOTTOM_RIGHT, 1.0, true);
        addTemplateState(uhc, "stopwatch", 10, 40, HudAnchor.TOP_LEFT, 1.0, true);
        addTemplateState(uhc, "fps", 10, 32, HudAnchor.TOP_RIGHT, 0.9, true);
        addTemplateState(uhc, "ping", 10, 54, HudAnchor.TOP_RIGHT, 0.9, true);
        presets.put(PRESET_UHC, uhc);

        // 6. BUILDING
        HudPreset bld = new HudPreset(PRESET_BUILDING, "Budowlany", "Kierunki świata, koordynaty, zliczanie bloków i stoper", true);
        addTemplateState(bld, "coordinates", 10, 10, HudAnchor.TOP_LEFT, 1.0, true);
        addTemplateState(bld, "direction_hud", 0, 12, HudAnchor.TOP_CENTER, 1.0, true);
        addTemplateState(bld, "item_counter", 10, 10, HudAnchor.BOTTOM_LEFT, 1.0, true);
        addTemplateState(bld, "fps", 10, 10, HudAnchor.TOP_RIGHT, 1.0, true);
        addTemplateState(bld, "stopwatch", 10, 34, HudAnchor.TOP_RIGHT, 1.0, true);
        presets.put(PRESET_BUILDING, bld);

        // 7. PERFORMANCE
        HudPreset perf = new HudPreset(PRESET_PERFORMANCE, "Wydajność", "Monitorowanie FPS, Pingu, RAM i wykres aktywności sieci", true);
        addTemplateState(perf, "fps", 10, 10, HudAnchor.TOP_RIGHT, 1.0, true);
        addTemplateState(perf, "ping", 10, 32, HudAnchor.TOP_RIGHT, 1.0, true);
        addTemplateState(perf, "memory_usage", 10, 54, HudAnchor.TOP_RIGHT, 1.0, true);
        addTemplateState(perf, "net_graph", 10, 80, HudAnchor.TOP_RIGHT, 1.0, true);
        presets.put(PRESET_PERFORMANCE, perf);

        // 8. STREAMER
        HudPreset strm = new HudPreset(PRESET_STREAMER, "Streamer", "Układ bezpieczny dla transmisji, chroniący dane serwera", true);
        addTemplateState(strm, "fps", 10, 10, HudAnchor.TOP_RIGHT, 1.0, true);
        addTemplateState(strm, "armor_status", 10, 10, HudAnchor.BOTTOM_LEFT, 1.0, true);
        addTemplateState(strm, "keystrokes", 10, 10, HudAnchor.BOTTOM_RIGHT, 1.0, true);
        presets.put(PRESET_STREAMER, strm);
    }

    private void addTemplateState(HudPreset preset, String id, int x, int y, HudAnchor anchor, double scale, boolean visible) {
        HudComponentState state = new HudComponentState(id);
        state.setPosX(x);
        state.setPosY(y);
        state.setAnchor(anchor);
        state.setScale(scale);
        state.setVisible(visible);
        preset.addComponent(state);
    }

    public synchronized void loadCustomPresets() {
        if (presetsDir == null || !Files.exists(presetsDir)) return;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(presetsDir, "*.json")) {
            for (Path file : stream) {
                try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    HudPreset p = GSON.fromJson(reader, HudPreset.class);
                    if (p != null && p.getId() != null && !p.isSystemPreset()) {
                        presets.put(p.getId().toLowerCase(Locale.ROOT), p);
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to read custom preset file {}: {}", file, e.getMessage());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to list preset files: {}", e.getMessage());
        }
    }

    public synchronized PresetApplyResult applyPreset(HudLayout targetLayout, String presetId) {
        return applyPreset(presetId, targetLayout);
    }

    public synchronized PresetApplyResult applyPreset(String presetId, HudLayout targetLayout) {
        if (presetId == null || targetLayout == null) {
            return new PresetApplyResult(false, presetId, null, null, "Nieprawidłowe argumenty");
        }
        HudPreset preset = presets.get(presetId.toLowerCase(Locale.ROOT));
        if (preset == null) {
            return new PresetApplyResult(false, presetId, null, null, "Nie znaleziono presetu: " + presetId);
        }

        List<String> changed = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        HudComponentRegistry registry = HudComponentRegistry.getInstance();

        // 1. Hide all components first
        for (HudComponentState state : targetLayout.getComponents().values()) {
            state.setVisible(false);
        }

        // 2. Apply template configurations
        for (Map.Entry<String, HudComponentState> entry : preset.getComponents().entrySet()) {
            String compId = entry.getKey();
            HudComponentState templateState = entry.getValue();

            if (registry.hasDescriptor(compId)) {
                HudComponentState targetState = targetLayout.getComponentState(compId);
                if (targetState == null) {
                    targetState = registry.getDescriptor(compId).orElseThrow().createDefaultState();
                    targetLayout.setComponentState(targetState);
                }
                targetState.setPosX(templateState.getPosX());
                targetState.setPosY(templateState.getPosY());
                targetState.setAnchor(templateState.getAnchor());
                targetState.setScale(templateState.getScale());
                targetState.setVisible(templateState.isVisible());
                changed.add(compId);
            } else {
                missing.add(compId);
            }
        }

        LOGGER.info("Applied HUD preset '{}': {} updated, {} missing", preset.getName(), changed.size(), missing.size());
        return new PresetApplyResult(true, presetId, changed, missing, "Pomyślnie zastosowano preset " + preset.getName());
    }

    public synchronized boolean createPresetFromLayout(String id, String name, String description, HudLayout source) {
        if (id == null || name == null || source == null) return false;
        String cleanId = id.trim().toLowerCase(Locale.ROOT);
        if (presets.containsKey(cleanId) && presets.get(cleanId).isSystemPreset()) {
            LOGGER.warn("Cannot overwrite system preset '{}'.", cleanId);
            return false;
        }

        HudPreset custom = new HudPreset(cleanId, name, description, false);
        for (HudComponentState state : source.getComponents().values()) {
            if (state.isVisible()) {
                custom.addComponent(new HudComponentState(state));
            }
        }
        presets.put(cleanId, custom);

        if (presetsDir != null) {
            Path file = presetsDir.resolve(cleanId + ".json");
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                GSON.toJson(custom, writer);
                LOGGER.info("Saved custom preset to {}", file);
            } catch (IOException e) {
                LOGGER.error("Failed to write custom preset file: {}", e.getMessage());
            }
        }
        return true;
    }

    public synchronized boolean deleteCustomPreset(String id) {
        if (id == null) return false;
        String cleanId = id.toLowerCase(Locale.ROOT);
        HudPreset preset = presets.get(cleanId);
        if (preset == null || preset.isSystemPreset()) {
            LOGGER.warn("Cannot delete non-existent or system preset '{}'.", cleanId);
            return false;
        }

        presets.remove(cleanId);
        if (presetsDir != null) {
            Path file = presetsDir.resolve(cleanId + ".json");
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                LOGGER.error("Failed to delete custom preset file: {}", e.getMessage());
            }
        }
        return true;
    }

    public synchronized HudPreset getPreset(String id) {
        if (id == null) return null;
        return presets.get(id.toLowerCase(Locale.ROOT));
    }

    public synchronized Collection<HudPreset> getAllPresets() {
        return Collections.unmodifiableCollection(presets.values());
    }

    public synchronized void resetForTesting() {
        presets.clear();
        registerSystemPresets();
        presetsDir = null;
    }
}
