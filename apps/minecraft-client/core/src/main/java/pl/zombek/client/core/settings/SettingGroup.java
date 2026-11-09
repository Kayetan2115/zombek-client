package pl.zombek.client.core.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * Logical container grouping related settings together for hierarchical presentation.
 */
public class SettingGroup {
    private final String id;
    private final String name;
    private final String description;
    private final List<Setting<?>> settings = new ArrayList<>();
    private BooleanSupplier visibilityPredicate = () -> true;

    public SettingGroup(String id, String name, String description) {
        this.id = Objects.requireNonNull(id, "Group id cannot be null").trim();
        this.name = Objects.requireNonNull(name, "Group name cannot be null").trim();
        this.description = description != null ? description : "";
    }

    public SettingGroup(String id, String name) {
        this(id, name, "");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public SettingGroup add(Setting<?> setting) {
        if (setting != null && !settings.contains(setting)) {
            settings.add(setting);
        }
        return this;
    }

    public <S extends Setting<?>> S addSetting(S setting) {
        add(setting);
        return setting;
    }

    public List<Setting<?>> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    public boolean isVisible() {
        return visibilityPredicate == null || visibilityPredicate.getAsBoolean();
    }

    public SettingGroup visibleWhen(BooleanSupplier predicate) {
        this.visibilityPredicate = predicate;
        return this;
    }
}
