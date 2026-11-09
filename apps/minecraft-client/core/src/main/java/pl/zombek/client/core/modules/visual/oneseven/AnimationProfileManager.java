package pl.zombek.client.core.modules.visual.oneseven;

import com.google.gson.*;

import java.util.*;

/**
 * Manages presets, custom profiles, persistence, and profile import/export for 1.7 Visuals.
 */
public class AnimationProfileManager {
    private static AnimationProfileManager instance;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final AnimationSettings currentSettings = new AnimationSettings();
    private final Map<String, AnimationProfile> customProfiles = new LinkedHashMap<>();

    public AnimationProfileManager() {
        // Initialize default presets
        applyPreset(AnimationPreset.MINECRAFT_1_7);
        customProfiles.put("Default Custom", new AnimationProfile("Default Custom", "Domyślny profil użytkownika", currentSettings.copy()));
    }

    public static synchronized AnimationProfileManager getInstance() {
        if (instance == null) {
            instance = new AnimationProfileManager();
        }
        return instance;
    }

    public AnimationSettings getCurrentSettings() {
        return currentSettings;
    }

    public Map<String, AnimationProfile> getCustomProfiles() {
        return Collections.unmodifiableMap(customProfiles);
    }

    public void applyPreset(AnimationPreset preset) {
        currentSettings.applyPresetDefaults(preset);
        ItemTransformManager.getInstance().applyPreset(preset);
    }

    public AnimationProfile saveCustomProfile(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Profile name cannot be empty");
        }
        String trimmed = name.trim();
        AnimationProfile profile = new AnimationProfile(trimmed, "Zapisany profil użytkownika", currentSettings.copy());
        customProfiles.put(trimmed, profile);
        currentSettings.setActivePreset(AnimationPreset.CUSTOM);
        currentSettings.setCustomProfileName(trimmed);
        return profile;
    }

    public boolean renameProfile(String oldName, String newName) {
        if (oldName == null || newName == null || newName.trim().isEmpty()) {
            return false;
        }
        String trimmedNew = newName.trim();
        AnimationProfile profile = customProfiles.remove(oldName);
        if (profile == null) {
            return false;
        }
        profile.setName(trimmedNew);
        customProfiles.put(trimmedNew, profile);
        if (oldName.equals(currentSettings.getCustomProfileName())) {
            currentSettings.setCustomProfileName(trimmedNew);
        }
        return true;
    }

    public AnimationProfile duplicateProfile(String sourceName, String newName) {
        if (sourceName == null || newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid profile names");
        }
        AnimationProfile source = customProfiles.get(sourceName);
        if (source == null) {
            throw new IllegalArgumentException("Source profile not found: " + sourceName);
        }
        String trimmedNew = newName.trim();
        AnimationProfile duplicate = source.copy(trimmedNew);
        customProfiles.put(trimmedNew, duplicate);
        return duplicate;
    }

    public boolean resetProfile(String name) {
        AnimationProfile profile = customProfiles.get(name);
        if (profile == null) {
            return false;
        }
        profile.getSettings().applyPresetDefaults(AnimationPreset.MINECRAFT_1_7);
        if (name.equals(currentSettings.getCustomProfileName())) {
            currentSettings.copyFrom(profile.getSettings());
        }
        return true;
    }

    public boolean deleteProfile(String name) {
        if (customProfiles.size() <= 1 && customProfiles.containsKey(name)) {
            return false; // Preserve at least one profile
        }
        return customProfiles.remove(name) != null;
    }

    public String exportProfile(String name) {
        AnimationProfile profile = customProfiles.get(name);
        AnimationSettings settingsToExport = profile != null ? profile.getSettings() : currentSettings;
        try {
            JsonObject root = new JsonObject();
            root.addProperty("profile_name", name != null ? name : "ExportedProfile");
            root.addProperty("export_timestamp", System.currentTimeMillis());
            settingsToExport.serializeToJson(root);
            ItemTransformManager.getInstance().serializeToJson(root);
            return GSON.toJson(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export profile JSON", e);
        }
    }

    public AnimationProfile importProfile(String jsonContent) {
        if (jsonContent == null || jsonContent.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON content cannot be empty");
        }
        try {
            JsonElement parsed = JsonParser.parseString(jsonContent);
            if (!parsed.isJsonObject()) {
                throw new IllegalArgumentException("Invalid profile JSON format: root must be an object");
            }
            JsonObject root = parsed.getAsJsonObject();
            if (root.has("schema_version") && root.get("schema_version").getAsInt() > AnimationSettings.SCHEMA_VERSION) {
                throw new IllegalArgumentException("Unsupported schema version: " + root.get("schema_version").getAsInt());
            }

            String profileName = root.has("profile_name") ? root.get("profile_name").getAsString() : "ImportedProfile";
            // Ensure unique name
            String baseName = profileName;
            int counter = 1;
            while (customProfiles.containsKey(profileName)) {
                profileName = baseName + " (" + counter++ + ")";
            }

            AnimationSettings settings = new AnimationSettings();
            settings.deserializeFromJson(root);

            if (root.has("item_categories")) {
                ItemTransformManager.getInstance().deserializeFromJson(root);
            }

            AnimationProfile profile = new AnimationProfile(profileName, "Zaimportowany profil użytkownika", settings);
            customProfiles.put(profileName, profile);
            currentSettings.copyFrom(settings);
            currentSettings.setActivePreset(AnimationPreset.CUSTOM);
            currentSettings.setCustomProfileName(profileName);
            return profile;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Malformed JSON: " + e.getMessage(), e);
        }
    }
}
