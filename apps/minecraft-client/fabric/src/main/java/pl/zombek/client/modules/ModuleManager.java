package pl.zombek.client.modules;

import net.minecraft.client.gui.DrawContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.settings.Setting;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Central registry and lifecycle orchestrator for all ZombekClient modules.
 */
public class ModuleManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleManager.class);
    private static final ModuleManager INSTANCE = new ModuleManager();

    private final Map<String, AbstractModule> modulesById = new ConcurrentHashMap<>();
    private final List<AbstractModule> modules = new CopyOnWriteArrayList<>();
    private final List<Runnable> changeListeners = new CopyOnWriteArrayList<>();

    private ModuleManager() {
    }

    public static ModuleManager getInstance() {
        return INSTANCE;
    }

    /**
     * Registers a new module into the client framework.
     */
    public synchronized void registerModule(AbstractModule module) {
        Objects.requireNonNull(module, "Module cannot be null");
        String id = module.getId();
        if (modulesById.containsKey(id)) {
            throw new IllegalArgumentException("Module with ID '" + id + "' is already registered!");
        }

        modulesById.put(id, module);
        modules.add(module);

        // Initialize module
        module.initialize();

        // Listen for setting changes to trigger auto-saving
        for (Setting<?> setting : module.getSettings()) {
            setting.addChangeListener(v -> notifyChange());
        }

        LOGGER.info("Registered module: '{}' ({}) in category [{}]",
                module.getName(), module.getId(), module.getCategory());
    }

    public List<AbstractModule> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public AbstractModule getModuleById(String id) {
        if (id == null) return null;
        return modulesById.get(id.trim().toLowerCase());
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractModule> T getModule(Class<T> moduleClass) {
        for (AbstractModule module : modules) {
            if (moduleClass.isInstance(module)) {
                return (T) module;
            }
        }
        return null;
    }

    public List<AbstractModule> getModulesByCategory(ModuleCategory category) {
        Objects.requireNonNull(category, "Category cannot be null");
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }

    public List<AbstractModule> searchModules(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getModules();
        }
        String cleanQuery = query.trim().toLowerCase();
        return modules.stream()
                .filter(m -> m.matchesQuery(cleanQuery))
                .collect(Collectors.toList());
    }

    public int getModuleCount() {
        return modules.size();
    }

    public int getEnabledModuleCount() {
        return (int) modules.stream().filter(AbstractModule::isEnabled).count();
    }

    /**
     * Executes safe client tick on all enabled modules.
     */
    public void onTick() {
        for (AbstractModule module : modules) {
            if (module.isEnabled()) {
                module.safeTick();
            }
        }
    }

    /**
     * Executes safe rendering on all enabled modules.
     */
    public void onRender(DrawContext context, float delta) {
        for (AbstractModule module : modules) {
            if (module.isEnabled()) {
                module.safeRender(context, delta);
            }
        }
    }

    public void addChangeListener(Runnable listener) {
        if (listener != null) {
            changeListeners.add(listener);
        }
    }

    public void notifyChange() {
        for (Runnable listener : changeListeners) {
            try {
                listener.run();
            } catch (Exception e) {
                LOGGER.error("Error notifying change listener: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Unregisters all modules. Primarily intended for testing environments.
     */
    public synchronized void clear() {
        for (AbstractModule module : modules) {
            if (module.isEnabled()) {
                module.safeDisable();
            }
        }
        modulesById.clear();
        modules.clear();
        changeListeners.clear();
    }
}
