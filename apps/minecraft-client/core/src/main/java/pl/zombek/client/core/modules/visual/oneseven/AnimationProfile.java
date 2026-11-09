package pl.zombek.client.core.modules.visual.oneseven;

import java.util.Objects;

/**
 * Encapsulates a named configuration profile for 1.7 Visuals.
 */
public class AnimationProfile {
    private String name;
    private String description;
    private final AnimationSettings settings;

    public AnimationProfile(String name) {
        this(name, "", new AnimationSettings());
    }

    public AnimationProfile(String name, String description, AnimationSettings settings) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.description = description != null ? description : "";
        this.settings = settings != null ? settings.copy() : new AnimationSettings();
    }

    public AnimationProfile copy(String newName) {
        return new AnimationProfile(newName, this.description, this.settings.copy());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AnimationSettings getSettings() {
        return settings;
    }
}
