package pl.zombek.client.core.hud.editor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Central registry of all editable HUD components available in ZombekClient.
 * Thread-safe and independent of loaders.
 */
public class HudComponentRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(HudComponentRegistry.class);
    private static final HudComponentRegistry INSTANCE = new HudComponentRegistry();

    private final Map<String, HudComponentDescriptor> descriptors = new ConcurrentHashMap<>();

    private HudComponentRegistry() {
    }

    public static HudComponentRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Registers a new HUD component descriptor.
     */
    public void register(HudComponentDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "Descriptor cannot be null");
        descriptors.put(descriptor.getId().toLowerCase(Locale.ROOT), descriptor);
        LOGGER.debug("Registered HUD component descriptor: {} ({})", descriptor.getId(), descriptor.getDisplayName());
    }

    /**
     * Unregisters a HUD component descriptor by identifier.
     */
    public void unregister(String id) {
        if (id != null) {
            descriptors.remove(id.toLowerCase(Locale.ROOT));
        }
    }

    /**
     * Retrieves a component descriptor by its identifier.
     */
    public Optional<HudComponentDescriptor> getDescriptor(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(descriptors.get(id.toLowerCase(Locale.ROOT)));
    }

    /**
     * Checks if a descriptor is registered.
     */
    public boolean hasDescriptor(String id) {
        if (id == null) return false;
        return descriptors.containsKey(id.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns an unmodifiable collection of all registered descriptors.
     */
    public Collection<HudComponentDescriptor> getAllDescriptors() {
        return Collections.unmodifiableCollection(descriptors.values());
    }

    /**
     * Returns all descriptors belonging to a given category.
     */
    public List<HudComponentDescriptor> getDescriptorsByCategory(String category) {
        if (category == null || category.equalsIgnoreCase("ALL") || category.equalsIgnoreCase("WSZYSTKIE")) {
            return new ArrayList<>(descriptors.values());
        }
        return descriptors.values().stream()
                .filter(d -> category.equalsIgnoreCase(d.getCategory()))
                .collect(Collectors.toList());
    }

    /**
     * Returns all descriptors belonging to a given component type.
     */
    public List<HudComponentDescriptor> getDescriptorsByType(HudComponentType type) {
        if (type == null) return new ArrayList<>(descriptors.values());
        return descriptors.values().stream()
                .filter(d -> d.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Returns all registered categories.
     */
    public Set<String> getCategories() {
        Set<String> categories = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (HudComponentDescriptor d : descriptors.values()) {
            categories.add(d.getCategory());
        }
        return categories;
    }

    /**
     * Total number of registered descriptors.
     */
    public int size() {
        return descriptors.size();
    }

    /**
     * Clears all registered descriptors (primarily for test resets).
     */
    public void resetForTesting() {
        descriptors.clear();
    }
}
