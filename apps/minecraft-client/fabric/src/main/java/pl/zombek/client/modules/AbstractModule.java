package pl.zombek.client.modules;

import net.minecraft.client.gui.DrawContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.settings.KeybindSetting;
import pl.zombek.client.settings.Setting;
import pl.zombek.client.settings.SettingGroup;

import java.util.*;

/**
 * Universal base class for all client modules.
 * Implements complete lifecycle management, safe error boundary containment,
 * typed settings registration, keybind association, and querying capabilities.
 */
public abstract class AbstractModule implements IClientModule {
    protected final Logger logger;
    private final ModuleMetadata metadata;
    private final KeybindSetting keybind;
    private final List<Setting<?>> settings = new ArrayList<>();
    private final List<SettingGroup> settingGroups = new ArrayList<>();
    private final Map<String, Setting<?>> settingsById = new LinkedHashMap<>();

    private boolean enabled = false;
    private boolean initialized = false;
    private boolean hasError = false;
    private String lastErrorMessage = null;

    public AbstractModule(ModuleMetadata metadata, int defaultKeybind) {
        this.metadata = Objects.requireNonNull(metadata, "Metadata cannot be null");
        this.logger = LoggerFactory.getLogger("ZombekClient/" + metadata.getId());
        this.keybind = new KeybindSetting("keybind", "Klawisz", "Skrót klawiszowy aktywujący moduł", defaultKeybind);
        registerSetting(this.keybind);
    }

    public AbstractModule(ModuleMetadata metadata) {
        this(metadata, KeybindSetting.KEY_NONE);
    }

    public AbstractModule(String id, String name, String description, ModuleCategory category, int defaultKeybind) {
        this(new ModuleMetadata(id, name, description, category), defaultKeybind);
    }

    public AbstractModule(String id, String name, String description, ModuleCategory category) {
        this(new ModuleMetadata(id, name, description, category), KeybindSetting.KEY_NONE);
    }

    public ModuleMetadata getMetadata() {
        return metadata;
    }

    public String getId() {
        return metadata.getId();
    }

    @Override
    public String getName() {
        return metadata.getName();
    }

    @Override
    public String getDescription() {
        return metadata.getDescription();
    }

    public ModuleCategory getCategory() {
        return metadata.getCategory();
    }

    public String getVersion() {
        return metadata.getVersion();
    }

    public String getAuthor() {
        return metadata.getAuthor();
    }

    public KeybindSetting getKeybindSetting() {
        return keybind;
    }

    public int getKeybind() {
        return keybind.getValue();
    }

    public void setKeybind(int keyCode) {
        keybind.setValue(keyCode);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public boolean hasError() {
        return hasError;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void clearError() {
        this.hasError = false;
        this.lastErrorMessage = null;
    }

    @Override
    public void setEnabled(boolean desired) {
        if (this.enabled == desired) {
            return;
        }
        if (desired) {
            safeEnable();
        } else {
            safeDisable();
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    // Lifecycle callbacks to be overridden by concrete modules
    public void onInitialize() {}
    @Override public void onEnable() {}
    @Override public void onDisable() {}
    public void onTick() {}
    public void onRender(DrawContext context, float delta) {}

    /**
     * Initializes module once on launcher/client startup.
     */
    public synchronized void initialize() {
        if (initialized) return;
        try {
            onInitialize();
            initialized = true;
        } catch (Throwable t) {
            hasError = true;
            lastErrorMessage = "Init failed: " + t.getMessage();
            logger.error("Failed to initialize module '{}': {}", getId(), t.getMessage(), t);
        }
    }

    /**
     * Safe enable wrapping user logic in an error boundary.
     */
    public synchronized void safeEnable() {
        if (enabled) return;
        try {
            clearError();
            enabled = true;
            onEnable();
            logger.info("Module '{}' enabled.", getId());
        } catch (Throwable t) {
            enabled = false;
            hasError = true;
            lastErrorMessage = "Enable error: " + t.getMessage();
            logger.error("Error while enabling module '{}': {}", getId(), t.getMessage(), t);
        }
    }

    /**
     * Safe disable wrapping user logic in an error boundary.
     */
    public synchronized void safeDisable() {
        if (!enabled) return;
        try {
            enabled = false;
            onDisable();
            logger.info("Module '{}' disabled.", getId());
        } catch (Throwable t) {
            hasError = true;
            lastErrorMessage = "Disable error: " + t.getMessage();
            logger.error("Error while disabling module '{}': {}", getId(), t.getMessage(), t);
        }
    }

    /**
     * Safe tick method called per client tick.
     */
    public void safeTick() {
        if (!enabled || hasError) return;
        try {
            onTick();
        } catch (Throwable t) {
            hasError = true;
            lastErrorMessage = "Tick error: " + t.getMessage();
            logger.error("Module '{}' crashed during onTick() and was safely disabled: {}", getId(), t.getMessage(), t);
            safeDisable();
        }
    }

    /**
     * Safe render method called during HUD/screen render phases.
     */
    public void safeRender(DrawContext context, float delta) {
        if (!enabled || hasError) return;
        try {
            onRender(context, delta);
        } catch (Throwable t) {
            hasError = true;
            lastErrorMessage = "Render error: " + t.getMessage();
            logger.error("Module '{}' crashed during onRender() and was safely disabled: {}", getId(), t.getMessage(), t);
            safeDisable();
        }
    }

    // Settings Registration and Access
    public <S extends Setting<?>> S registerSetting(S setting) {
        Objects.requireNonNull(setting, "Setting cannot be null");
        if (settingsById.containsKey(setting.getId())) {
            throw new IllegalArgumentException("Setting with ID '" + setting.getId() + "' already registered in module '" + getId() + "'");
        }
        settings.add(setting);
        settingsById.put(setting.getId(), setting);
        return setting;
    }

    public SettingGroup registerGroup(SettingGroup group) {
        Objects.requireNonNull(group, "SettingGroup cannot be null");
        settingGroups.add(group);
        for (Setting<?> s : group.getSettings()) {
            if (!settingsById.containsKey(s.getId())) {
                settings.add(s);
                settingsById.put(s.getId(), s);
            }
        }
        return group;
    }

    public List<Setting<?>> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    public List<SettingGroup> getSettingGroups() {
        return Collections.unmodifiableList(settingGroups);
    }

    public Setting<?> getSetting(String id) {
        return settingsById.get(id);
    }

    @SuppressWarnings("unchecked")
    public <T extends Setting<?>> T getSetting(String id, Class<T> type) {
        Setting<?> s = settingsById.get(id);
        if (s != null && type.isInstance(s)) {
            return (T) s;
        }
        return null;
    }

    public boolean hasSettings() {
        // More than just the default keybind setting
        return settings.size() > 1;
    }

    /**
     * Search filter match helper.
     */
    public boolean matchesQuery(String query) {
        if (query == null || query.trim().isEmpty()) return true;
        String q = query.trim().toLowerCase();
        return getId().contains(q) ||
                getName().toLowerCase().contains(q) ||
                getDescription().toLowerCase().contains(q) ||
                getCategory().getDisplayName().toLowerCase().contains(q);
    }

    @Override
    public String toString() {
        return "AbstractModule{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", category=" + getCategory() +
                ", enabled=" + enabled +
                '}';
    }
}
