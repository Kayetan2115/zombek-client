package pl.zombek.client.core.hud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central registry of active HUD elements displayed on the in-game screen.
 * Thread-safe and shared across all loader adapters.
 */
public class HudElementRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(HudElementRegistry.class);
    private static final HudElementRegistry INSTANCE = new HudElementRegistry();

    private final List<IHudElement> elements = new CopyOnWriteArrayList<>();

    private HudElementRegistry() {}

    public static HudElementRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Registers a HUD element to be drawn on every frame.
     */
    public void register(IHudElement element) {
        Objects.requireNonNull(element, "HUD element cannot be null");
        if (!elements.contains(element)) {
            elements.add(element);
            LOGGER.debug("Registered HUD element: {}", element.getElementId());
        }
    }

    /**
     * Unregisters a HUD element.
     */
    public void unregister(IHudElement element) {
        if (element != null) {
            elements.remove(element);
            LOGGER.debug("Unregistered HUD element: {}", element.getElementId());
        }
    }

    /**
     * Unregisters a HUD element by its identifier.
     */
    public void unregisterById(String elementId) {
        if (elementId != null) {
            elements.removeIf(e -> elementId.equalsIgnoreCase(e.getElementId()));
        }
    }

    /**
     * Returns an unmodifiable list of currently registered HUD elements.
     */
    public List<IHudElement> getElements() {
        return Collections.unmodifiableList(elements);
    }

    /**
     * Iterates through all registered HUD elements and renders visible ones.
     * Contains error boundary protection to prevent a single buggy element from breaking HUD rendering.
     *
     * @param renderer Platform renderer implementation
     */
    public void renderAll(IHudRenderer renderer) {
        if (renderer == null) return;
        for (IHudElement element : elements) {
            if (element != null && element.isVisible()) {
                try {
                    element.render(renderer);
                } catch (Throwable t) {
                    LOGGER.error("Failed to render HUD element '{}': {}",
                            element.getElementId(), t.getMessage(), t);
                }
            }
        }
    }

    /**
     * Clears all registered HUD elements (useful for testing and resets).
     */
    public void clear() {
        elements.clear();
    }
}
