package pl.zombek.client.core.hud.editor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Manages multiple independent HUD layout profiles with quick switching,
 * automatic backups, and strict separation from gameplay settings.
 */
public class HudProfileManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(HudProfileManager.class);
    private static final HudProfileManager INSTANCE = new HudProfileManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final String DEFAULT_PROFILE_ID = "default";

    private Path profilesDir;
    private Path backupsDir;
    private Path metaFile;

    private String activeProfileId = DEFAULT_PROFILE_ID;
    private final Map<String, HudProfileMetadata> profilesMeta = new LinkedHashMap<>();
    private final Map<String, HudLayout> loadedProfiles = new HashMap<>();

    private HudProfileManager() {
        registerSystemDefaults();
    }

    public static HudProfileManager getInstance() {
        return INSTANCE;
    }

    private void registerSystemDefaults() {
        if (!profilesMeta.containsKey(DEFAULT_PROFILE_ID)) {
            profilesMeta.put(DEFAULT_PROFILE_ID, new HudProfileMetadata(DEFAULT_PROFILE_ID, "Domyślny Układ", "Podstawowy, zbalansowany układ HUD", true));
        }
        if (!profilesMeta.containsKey("pvp")) {
            profilesMeta.put("pvp", new HudProfileMetadata("pvp", "Profil PvP", "Układ zoptymalizowany pod pojedynki i walkę", false));
        }
        if (!profilesMeta.containsKey("survival")) {
            profilesMeta.put("survival", new HudProfileMetadata("survival", "Profil Survival", "Układ zoptymalizowany pod przetrwanie i eksplorację", false));
        }
        if (!profilesMeta.containsKey("uhc")) {
            profilesMeta.put("uhc", new HudProfileMetadata("uhc", "Profil UHC", "Układ pod turnieje Ultra Hardcore", false));
        }
        if (!profilesMeta.containsKey("building")) {
            profilesMeta.put("building", new HudProfileMetadata("building", "Profil Budowlany", "Układ pod precyzyjne budowanie", false));
        }
        if (!profilesMeta.containsKey("minimal")) {
            profilesMeta.put("minimal", new HudProfileMetadata("minimal", "Profil Minimalny", "Dyskretny interfejs z maksymalną widocznością gry", false));
        }
        if (!profilesMeta.containsKey("custom")) {
            profilesMeta.put("custom", new HudProfileMetadata("custom", "Profil Własny", "Układ w pełni spersonalizowany przez gracza", false));
        }
    }

    public synchronized void initialize(Path configDir) {
        if (configDir != null) {
            this.profilesDir = configDir.resolve("zombekclient").resolve("hud_profiles");
            this.backupsDir = profilesDir.resolve("backups");
            this.metaFile = profilesDir.resolve("profiles_meta.json");

            try {
                if (!Files.exists(profilesDir)) {
                    Files.createDirectories(profilesDir);
                }
                if (!Files.exists(backupsDir)) {
                    Files.createDirectories(backupsDir);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to initialize HUD profile directories: {}", e.getMessage());
            }

            loadMeta();
            ensureProfileFilesExist();

            // Synchronize active profile with HudLayoutManager
            HudLayout activeLayout = loadProfileLayout(activeProfileId);
            HudLayoutManager.getInstance().setActiveLayout(activeLayout);
            LOGGER.info("Initialized HudProfileManager with {} profiles, active: {}", profilesMeta.size(), activeProfileId);
        }
    }

    private void ensureProfileFilesExist() {
        if (profilesDir == null) return;
        for (HudProfileMetadata meta : profilesMeta.values()) {
            Path file = getProfilePath(meta.getId());
            if (!Files.exists(file)) {
                HudLayout layout = new HudLayout(meta.getId(), meta.getName());
                layout.setDescription(meta.getDescription());
                HudLayoutValidator.repairLayout(layout);
                try {
                    HudLayoutSerializer.saveToFile(layout, file);
                } catch (IOException e) {
                    LOGGER.error("Failed to write initial layout for profile {}: {}", meta.getId(), e.getMessage());
                }
            }
        }
    }

    public synchronized void loadMeta() {
        if (metaFile != null && Files.exists(metaFile)) {
            try (Reader reader = Files.newBufferedReader(metaFile, StandardCharsets.UTF_8)) {
                Map<String, Object> data = GSON.fromJson(reader, new TypeToken<Map<String, Object>>() {}.getType());
                if (data != null) {
                    if (data.containsKey("activeProfileId")) {
                        this.activeProfileId = String.valueOf(data.get("activeProfileId"));
                    }
                    if (data.containsKey("profiles")) {
                        String profilesJson = GSON.toJson(data.get("profiles"));
                        List<HudProfileMetadata> list = GSON.fromJson(profilesJson, new TypeToken<List<HudProfileMetadata>>() {}.getType());
                        if (list != null) {
                            for (HudProfileMetadata m : list) {
                                if (m.getId() != null) {
                                    profilesMeta.put(m.getId().toLowerCase(Locale.ROOT), m);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to load profiles_meta.json, restoring defaults: {}", e.getMessage());
            }
        }
        registerSystemDefaults();
        if (!profilesMeta.containsKey(activeProfileId)) {
            activeProfileId = DEFAULT_PROFILE_ID;
        }
    }

    public synchronized void saveMeta() {
        if (metaFile == null) return;
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("activeProfileId", activeProfileId);
            data.put("profiles", new ArrayList<>(profilesMeta.values()));

            Path temp = metaFile.resolveSibling("profiles_meta.json.tmp");
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                GSON.toJson(data, writer);
            }
            Files.move(temp, metaFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.error("Failed to save profiles_meta.json: {}", e.getMessage());
        }
    }

    public Path getProfilePath(String id) {
        if (profilesDir == null || id == null) return null;
        return profilesDir.resolve(id.toLowerCase(Locale.ROOT) + ".json");
    }

    public synchronized HudLayout loadProfileLayout(String profileId) {
        if (profileId == null) profileId = DEFAULT_PROFILE_ID;
        profileId = profileId.toLowerCase(Locale.ROOT);

        if (loadedProfiles.containsKey(profileId)) {
            return loadedProfiles.get(profileId).cloneLayout();
        }

        Path path = getProfilePath(profileId);
        HudLayout layout = null;
        if (path != null && Files.exists(path)) {
            layout = HudLayoutSerializer.loadFromFile(path);
        } else {
            HudProfileMetadata meta = profilesMeta.get(profileId);
            String name = meta != null ? meta.getName() : "Profil " + profileId;
            layout = new HudLayout(profileId, name);
            HudLayoutValidator.repairLayout(layout);
        }
        loadedProfiles.put(profileId, layout.cloneLayout());
        return layout;
    }

    /**
     * Activates a profile, immediately updating the active layout in the HUD.
     */
    public synchronized boolean activateProfile(String profileId) {
        if (profileId == null) return false;
        profileId = profileId.toLowerCase(Locale.ROOT);
        if (!profilesMeta.containsKey(profileId)) {
            LOGGER.warn("Cannot activate unknown profile: {}", profileId);
            return false;
        }

        // Backup current active profile before switching
        backupProfile(activeProfileId);

        this.activeProfileId = profileId;
        HudLayout newActive = loadProfileLayout(profileId);
        HudLayoutManager.getInstance().setActiveLayout(newActive);
        saveMeta();
        LOGGER.info("Activated HUD profile: {}", profileId);
        return true;
    }

    public synchronized boolean createProfile(String id, String name, String description, HudLayout template) {
        if (id == null || id.trim().isEmpty() || name == null || name.trim().isEmpty()) return false;
        String cleanId = id.trim().toLowerCase(Locale.ROOT);
        if (profilesMeta.containsKey(cleanId)) {
            LOGGER.warn("Profile with id '{}' already exists.", cleanId);
            return false;
        }

        HudLayout newLayout = template != null ? template.cloneLayout() : new HudLayout(cleanId, name);
        newLayout.setLayoutId(cleanId);
        newLayout.setName(name);
        newLayout.setDescription(description != null ? description : "");
        HudLayoutValidator.repairLayout(newLayout);

        HudProfileMetadata meta = new HudProfileMetadata(cleanId, name, description, false);
        meta.setComponentCount(newLayout.getComponents().size());
        profilesMeta.put(cleanId, meta);
        loadedProfiles.put(cleanId, newLayout.cloneLayout());

        Path path = getProfilePath(cleanId);
        if (path != null) {
            try {
                HudLayoutSerializer.saveToFile(newLayout, path);
            } catch (IOException e) {
                LOGGER.error("Failed to save new profile file: {}", e.getMessage());
            }
        }
        saveMeta();
        return true;
    }

    public synchronized boolean renameProfile(String id, String newName) {
        if (id == null || newName == null || newName.trim().isEmpty()) return false;
        String cleanId = id.toLowerCase(Locale.ROOT);
        HudProfileMetadata meta = profilesMeta.get(cleanId);
        if (meta == null) return false;

        meta.setName(newName.trim());
        meta.setUpdatedAt(System.currentTimeMillis());

        HudLayout layout = loadProfileLayout(cleanId);
        layout.setName(newName.trim());
        loadedProfiles.put(cleanId, layout);

        Path path = getProfilePath(cleanId);
        if (path != null) {
            try {
                HudLayoutSerializer.saveToFile(layout, path);
            } catch (IOException ignored) {
            }
        }
        saveMeta();
        return true;
    }

    public synchronized boolean duplicateProfile(String sourceId, String newId, String newName) {
        if (sourceId == null || newId == null || newName == null) return false;
        String src = sourceId.toLowerCase(Locale.ROOT);
        String target = newId.toLowerCase(Locale.ROOT);
        if (!profilesMeta.containsKey(src) || profilesMeta.containsKey(target)) return false;

        HudLayout sourceLayout = loadProfileLayout(src);
        HudProfileMetadata srcMeta = profilesMeta.get(src);
        String desc = srcMeta != null ? "Kopia profilu " + srcMeta.getName() : "Kopia profilu";
        return createProfile(target, newName, desc, sourceLayout);
    }

    public synchronized boolean deleteProfile(String id) {
        if (id == null) return false;
        String cleanId = id.toLowerCase(Locale.ROOT);

        // Protection: cannot delete default profile
        if (DEFAULT_PROFILE_ID.equals(cleanId)) {
            LOGGER.warn("Cannot delete default system profile.");
            return false;
        }

        // Protection: cannot delete active profile
        if (cleanId.equals(activeProfileId)) {
            LOGGER.warn("Cannot delete currently active profile '{}'. Switch to another profile first.", cleanId);
            return false;
        }

        HudProfileMetadata meta = profilesMeta.get(cleanId);
        if (meta != null && meta.isSystemProfile()) {
            LOGGER.warn("Cannot delete protected system profile '{}'.", cleanId);
            return false;
        }

        backupProfile(cleanId);

        profilesMeta.remove(cleanId);
        loadedProfiles.remove(cleanId);

        Path path = getProfilePath(cleanId);
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                LOGGER.error("Failed to delete profile file: {}", e.getMessage());
            }
        }
        saveMeta();
        return true;
    }

    public synchronized void backupProfile(String id) {
        if (id == null || backupsDir == null) return;
        String cleanId = id.toLowerCase(Locale.ROOT);
        Path src = getProfilePath(cleanId);
        if (src != null && Files.exists(src)) {
            try {
                String timestamp = String.valueOf(System.currentTimeMillis());
                Path backup = backupsDir.resolve(cleanId + "_" + timestamp + ".bak");
                Files.copy(src, backup, StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Created backup for profile '{}' at {}", cleanId, backup);
            } catch (IOException e) {
                LOGGER.warn("Failed to create backup for profile '{}': {}", cleanId, e.getMessage());
            }
        }
    }

    public synchronized void switchToNextProfile() {
        if (profilesMeta.isEmpty()) return;
        List<String> ids = new ArrayList<>(profilesMeta.keySet());
        int currentIndex = ids.indexOf(activeProfileId);
        int nextIndex = (currentIndex + 1) % ids.size();
        activateProfile(ids.get(nextIndex));
    }

    public synchronized void switchProfileNext() {
        switchToNextProfile();
    }

    public synchronized void switchToPreviousProfile() {
        if (profilesMeta.isEmpty()) return;
        List<String> ids = new ArrayList<>(profilesMeta.keySet());
        int currentIndex = ids.indexOf(activeProfileId);
        int prevIndex = (currentIndex - 1 + ids.size()) % ids.size();
        activateProfile(ids.get(prevIndex));
    }

    public synchronized String getActiveProfileId() {
        return activeProfileId;
    }

    public synchronized String getActiveProfileName() {
        HudProfileMetadata meta = getActiveProfileMetadata();
        return meta != null ? meta.getName() : activeProfileId;
    }

    public synchronized HudProfileMetadata getActiveProfileMetadata() {
        return profilesMeta.get(activeProfileId);
    }

    public synchronized Collection<HudProfileMetadata> getAllProfiles() {
        return Collections.unmodifiableCollection(profilesMeta.values());
    }

    public synchronized List<HudProfileMetadata> listProfiles() {
        return new ArrayList<>(profilesMeta.values());
    }

    public synchronized boolean hasProfile(String id) {
        if (id == null) return false;
        return profilesMeta.containsKey(id.toLowerCase(Locale.ROOT));
    }

    public synchronized void resetForTesting() {
        activeProfileId = DEFAULT_PROFILE_ID;
        profilesMeta.clear();
        loadedProfiles.clear();
        registerSystemDefaults();
        profilesDir = null;
        backupsDir = null;
        metaFile = null;
    }
}
