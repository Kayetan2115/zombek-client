package pl.zombek.client.core.modules;

import java.util.Objects;

/**
 * Immutable metadata identifying a module.
 */
public final class ModuleMetadata {
    private final String id;
    private final String name;
    private final String description;
    private final ModuleCategory category;
    private final String version;
    private final String author;

    public ModuleMetadata(String id, String name, String description, ModuleCategory category, String version, String author) {
        this.id = Objects.requireNonNull(id, "Module id cannot be null").trim().toLowerCase();
        this.name = Objects.requireNonNull(name, "Module name cannot be null").trim();
        this.description = description != null ? description : "";
        this.category = category != null ? category : ModuleCategory.OTHER;
        this.version = version != null ? version : "1.0.0";
        this.author = author != null ? author : "ZombekClient Team";
    }

    public ModuleMetadata(String id, String name, String description, ModuleCategory category) {
        this(id, name, description, category, "1.0.0", "ZombekClient Team");
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

    public ModuleCategory getCategory() {
        return category;
    }

    public String getVersion() {
        return version;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleMetadata that = (ModuleMetadata) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ModuleMetadata{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", version='" + version + '\'' +
                '}';
    }
}
