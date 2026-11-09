package pl.zombek.client.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.data.HitboxEntityData;
import pl.zombek.client.core.data.TargetEntityType;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.visual.HitboxModule;

import static org.junit.jupiter.api.Assertions.*;

public class HitboxModuleTest {

    private HitboxModule module;

    @BeforeEach
    void setUp() {
        module = new HitboxModule();
    }

    @AfterEach
    void tearDown() {
        if (module != null) {
            module.setEnabled(false);
        }
    }

    @Test
    void testModuleMetadataAndCategory() {
        assertEquals("hitbox", module.getId());
        assertEquals("Hitbox", module.getName());
        assertEquals(ModuleCategory.VISUAL, module.getCategory());
        assertFalse(module.isEnabled());
    }

    @Test
    void testSettingGroupsPresent() {
        assertNotNull(module.getGroup("general"));
        assertNotNull(module.getGroup("appearance"));
        assertNotNull(module.getGroup("colors"));
        assertNotNull(module.getGroup("filters"));
        assertNotNull(module.getGroup("information"));
        assertNotNull(module.getGroup("advanced"));
    }

    @Test
    void testDistanceAndDeadFilters() {
        module.setEnabled(true);

        // Within 32m, alive
        HitboxEntityData valid = new HitboxEntityData(
                1, "Cow", TargetEntityType.PASSIVE,
                0, 0, 0, 1, 1, 1,
                0.8, 0, 0, 1, 15.0,
                false, false, false, false
        );
        assertTrue(module.shouldRenderEntity(valid));

        // Too far (50m > 32m)
        HitboxEntityData tooFar = new HitboxEntityData(
                2, "Cow", TargetEntityType.PASSIVE,
                0, 0, 0, 1, 1, 1,
                0.8, 0, 0, 1, 50.0,
                false, false, false, false
        );
        assertFalse(module.shouldRenderEntity(tooFar));

        // Dead entity
        HitboxEntityData dead = new HitboxEntityData(
                3, "Cow", TargetEntityType.PASSIVE,
                0, 0, 0, 1, 1, 1,
                0.8, 0, 0, 1, 10.0,
                false, false, true, false
        );
        assertFalse(module.shouldRenderEntity(dead));
    }

    @Test
    void testTargetedColorResolution() {
        module.setEnabled(true);

        HitboxEntityData targeted = new HitboxEntityData(
                1, "Enemy", TargetEntityType.PLAYER,
                0, 0, 0, 1, 1, 1,
                1.6, 0, 0, 1, 5.0,
                true, false, false, false
        );

        HitboxEntityData regular = new HitboxEntityData(
                2, "Friend", TargetEntityType.PLAYER,
                0, 0, 0, 1, 1, 1,
                1.6, 0, 0, 1, 5.0,
                false, false, false, false
        );

        int colorTargeted = module.resolveOutlineColor(targeted);
        int colorRegular = module.resolveOutlineColor(regular);

        assertNotEquals(colorTargeted, colorRegular, "Targeted entity should receive dedicated highlight color");
    }
}
