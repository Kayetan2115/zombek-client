package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.hud.HudAnchor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HudComponentRegistryTest {

    private HudComponentRegistry registry;

    @BeforeEach
    void setUp() {
        registry = HudComponentRegistry.getInstance();
        registry.resetForTesting();
    }

    @Test
    void testRegisterAndRetrieve() {
        HudComponentDescriptor desc = HudComponentDescriptor.builder("test_elem", "Test Elem")
                .category("PvP")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(15, 25)
                .defaultSize(80, 20)
                .defaultAnchor(HudAnchor.TOP_RIGHT)
                .capabilities(HudComponentCapability.POSITION, HudComponentCapability.SCALE)
                .build();

        registry.register(desc);

        assertEquals(1, registry.size());
        assertTrue(registry.hasDescriptor("test_elem"));
        assertTrue(registry.hasDescriptor("TEST_ELEM")); // Case-insensitive

        Optional<HudComponentDescriptor> retrieved = registry.getDescriptor("test_elem");
        assertTrue(retrieved.isPresent());
        assertEquals("Test Elem", retrieved.get().getDisplayName());
        assertEquals("PvP", retrieved.get().getCategory());
        assertEquals(HudComponentType.SCREEN_SPACE, retrieved.get().getType());
        assertEquals(15, retrieved.get().getDefaultX());
        assertEquals(25, retrieved.get().getDefaultY());
        assertEquals(HudAnchor.TOP_RIGHT, retrieved.get().getDefaultAnchor());
        assertTrue(retrieved.get().hasCapability(HudComponentCapability.POSITION));
        assertTrue(retrieved.get().hasCapability(HudComponentCapability.SCALE));
        assertFalse(retrieved.get().hasCapability(HudComponentCapability.Z_INDEX));
    }

    @Test
    void testFilterByCategoryAndType() {
        registry.register(HudComponentDescriptor.builder("c1", "Comp 1").category("PvP").type(HudComponentType.SCREEN_SPACE).build());
        registry.register(HudComponentDescriptor.builder("c2", "Comp 2").category("PvP").type(HudComponentType.WORLD_SPACE).build());
        registry.register(HudComponentDescriptor.builder("c3", "Comp 3").category("HUD").type(HudComponentType.SCREEN_SPACE).build());

        List<HudComponentDescriptor> pvpList = registry.getDescriptorsByCategory("PvP");
        assertEquals(2, pvpList.size());

        List<HudComponentDescriptor> hudList = registry.getDescriptorsByCategory("HUD");
        assertEquals(1, hudList.size());

        List<HudComponentDescriptor> allList = registry.getDescriptorsByCategory("ALL");
        assertEquals(3, allList.size());

        List<HudComponentDescriptor> worldTypes = registry.getDescriptorsByType(HudComponentType.WORLD_SPACE);
        assertEquals(1, worldTypes.size());
        assertEquals("c2", worldTypes.get(0).getId());
    }

    @Test
    void testUnregister() {
        HudComponentDescriptor desc = HudComponentDescriptor.builder("temp", "Temp").build();
        registry.register(desc);
        assertTrue(registry.hasDescriptor("temp"));

        registry.unregister("temp");
        assertFalse(registry.hasDescriptor("temp"));
        assertEquals(0, registry.size());
    }
}
