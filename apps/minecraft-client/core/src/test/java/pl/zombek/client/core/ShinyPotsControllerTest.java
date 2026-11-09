package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.visual.shinypots.PotionType;
import pl.zombek.client.core.modules.visual.shinypots.ShinyPotsContext;
import pl.zombek.client.core.modules.visual.shinypots.ShinyPotsController;
import pl.zombek.client.core.modules.visual.shinypots.ShinyPotsModule;

import static org.junit.jupiter.api.Assertions.*;

public class ShinyPotsControllerTest {

    private ShinyPotsController controller;
    private ShinyPotsModule module;

    @BeforeEach
    public void setUp() {
        module = new ShinyPotsModule();
        controller = ShinyPotsController.getInstance();
    }

    @Test
    public void testPotionTypeExtraction() {
        assertEquals(PotionType.REGULAR, PotionType.fromRegistryId("minecraft:potion"));
        assertEquals(PotionType.SPLASH, PotionType.fromRegistryId("minecraft:splash_potion"));
        assertEquals(PotionType.LINGERING, PotionType.fromRegistryId("minecraft:lingering_potion"));
        assertEquals(PotionType.TIPPED_ARROW, PotionType.fromRegistryId("minecraft:tipped_arrow"));
        assertEquals(PotionType.OTHER, PotionType.fromRegistryId("minecraft:diamond_sword"));
    }

    @Test
    public void testShouldHaveGlintWhenEnabled() {
        module.setEnabled(true);

        assertTrue(controller.shouldHaveGlint("minecraft:potion"));
        assertTrue(controller.shouldHaveGlint("minecraft:splash_potion"));
        assertTrue(controller.shouldHaveGlint("minecraft:lingering_potion"));
        assertTrue(controller.shouldHaveGlint("minecraft:tipped_arrow"));
        assertFalse(controller.shouldHaveGlint("minecraft:apple"));

        // Disable splash potions
        module.getSplashPotionsSetting().setValue(false);
        assertFalse(controller.shouldHaveGlint("minecraft:splash_potion"));
        assertTrue(controller.shouldHaveGlint("minecraft:potion"));
    }

    @Test
    public void testContextFiltering() {
        module.setEnabled(true);
        module.getShowInContainersSetting().setValue(false);

        assertTrue(controller.shouldHaveGlint("minecraft:potion", ShinyPotsContext.INVENTORY));
        assertFalse(controller.shouldHaveGlint("minecraft:potion", ShinyPotsContext.CONTAINER));
    }

    @Test
    public void testResolveGlintColor() {
        module.setEnabled(true);
        int color = controller.resolveGlintColor(1000L);
        assertNotEquals(0, color);
    }
}
