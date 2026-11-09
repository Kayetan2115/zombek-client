package pl.zombek.client.core.hud.editor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.modules.ModuleManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

/**
 * Manages active and staged HUD layouts, file persistence, and staging workflow.
 */
public class HudLayoutManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(HudLayoutManager.class);
    private static final HudLayoutManager INSTANCE = new HudLayoutManager();

    private HudLayout activeLayout = new HudLayout("default", "Domyślny Układ");
    private HudLayout stagedLayout = null;
    private Path layoutFilePath = null;

    private HudLayoutManager() {
    }

    public static HudLayoutManager getInstance() {
        return INSTANCE;
    }

    public synchronized void initialize(Path configDir) {
        if (configDir != null) {
            this.layoutFilePath = configDir.resolve("zombekclient").resolve("hud_layout.json");
            load();
        }
    }

    public synchronized void loadFromDisk() {
        load();
    }

    public synchronized void load() {
        if (layoutFilePath != null && Files.exists(layoutFilePath)) {
            activeLayout = HudLayoutSerializer.loadFromFile(layoutFilePath);
            LOGGER.info("Loaded HUD layout '{}' with {} components", activeLayout.getName(), activeLayout.getComponents().size());
        } else {
            // First time setup: run migration from modules and default descriptors
            activeLayout = new HudLayout("default", "Domyślny Układ");
            HudConfigMigrator.migrateFromModules(activeLayout, ModuleManager.getInstance());
            HudLayoutValidator.repairLayout(activeLayout);
            if (layoutFilePath != null) {
                try {
                    HudLayoutSerializer.saveToFile(activeLayout, layoutFilePath);
                    LOGGER.info("Initialized default HUD layout file at {}", layoutFilePath);
                } catch (IOException e) {
                    LOGGER.error("Failed to save initial HUD layout: {}", e.getMessage());
                }
            }
        }
        this.stagedLayout = activeLayout.cloneLayout();
    }

    /**
     * Starts an editing session by deep-cloning the active layout into the staged layout.
     */
    public synchronized void beginEditingSession() {
        if (activeLayout == null || activeLayout.getComponents().isEmpty()) {
            activeLayout = HudLayoutValidator.repairLayout(activeLayout != null ? activeLayout : new HudLayout("default", "Domyślny Układ"));
        }
        this.stagedLayout = activeLayout.cloneLayout();
    }

    /**
     * Returns true if the staged layout has differences compared to active layout.
     */
    public synchronized boolean hasUnsavedChanges() {
        if (stagedLayout == null || activeLayout == null) return false;
        HudLayout activeCopy = activeLayout.cloneLayout();
        HudLayout stagedCopy = stagedLayout.cloneLayout();
        activeCopy.setCreatedAt(0);
        activeCopy.setUpdatedAt(0);
        stagedCopy.setCreatedAt(0);
        stagedCopy.setUpdatedAt(0);
        String activeJson = HudLayoutSerializer.toJson(activeCopy);
        String stagedJson = HudLayoutSerializer.toJson(stagedCopy);
        return !activeJson.equals(stagedJson);
    }

    /**
     * Commits all staged layout changes to the active layout and persists to disk.
     */
    public synchronized boolean commitStagedChanges() {
        if (stagedLayout == null) return false;
        activeLayout = stagedLayout.cloneLayout();
        long now = System.currentTimeMillis();
        activeLayout.setUpdatedAt(now);
        stagedLayout.setUpdatedAt(now);

        if (layoutFilePath != null) {
            try {
                HudLayoutSerializer.saveToFile(activeLayout, layoutFilePath);
                LOGGER.info("Successfully persisted HUD layout to {}", layoutFilePath);
                return true;
            } catch (IOException e) {
                LOGGER.error("Failed to persist committed HUD layout: {}", e.getMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * Discards all modifications made during the editing session.
     */
    public synchronized void discardStagedChanges() {
        if (activeLayout != null) {
            this.stagedLayout = activeLayout.cloneLayout();
        }
    }

    /**
     * Resets the entire staged layout to factory defaults based on registered component descriptors.
     */
    public synchronized void resetToDefaults() {
        HudLayout defaults = new HudLayout("default", "Domyślny Układ");
        HudComponentRegistry registry = HudComponentRegistry.getInstance();
        for (HudComponentDescriptor desc : registry.getAllDescriptors()) {
            defaults.setComponentState(desc.createDefaultState());
        }
        this.stagedLayout = defaults;
    }

    /**
     * Resets a single component in the staged layout back to its descriptor defaults.
     */
    public synchronized void resetComponentToDefault(String componentId) {
        if (componentId == null || stagedLayout == null) return;
        HudComponentRegistry registry = HudComponentRegistry.getInstance();
        registry.getDescriptor(componentId).ifPresent(desc -> {
            stagedLayout.setComponentState(desc.createDefaultState());
        });
    }

    public synchronized HudLayout getActiveLayout() {
        return activeLayout;
    }

    public synchronized void setActiveLayout(HudLayout layout) {
        this.activeLayout = Objects.requireNonNull(layout, "layout cannot be null");
        this.stagedLayout = layout.cloneLayout();
    }

    public synchronized HudLayout getStagedLayout() {
        if (stagedLayout == null) {
            stagedLayout = activeLayout.cloneLayout();
        }
        return stagedLayout;
    }

    public synchronized Path getLayoutFilePath() {
        return layoutFilePath;
    }

    public synchronized void setLayoutFilePath(Path path) {
        this.layoutFilePath = path;
    }

    public synchronized void resetForTesting() {
        activeLayout = new HudLayout("default", "Domyślny Układ");
        HudLayoutValidator.repairLayout(activeLayout);
        stagedLayout = activeLayout.cloneLayout();
        layoutFilePath = null;
    }
}
