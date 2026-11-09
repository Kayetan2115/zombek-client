package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.pvp.snaplook.*;

import static org.junit.jupiter.api.Assertions.*;

public class SnaplookControllerTest {

    private SnaplookController controller;
    private SnaplookModule module;
    private MockPerspectiveAdapter adapter;

    @BeforeEach
    public void setUp() {
        module = new SnaplookModule();
        controller = SnaplookController.getInstance();
        adapter = new MockPerspectiveAdapter(SnaplookPerspective.FIRST_PERSON);
        controller.setAdapter(adapter);
        controller.reset();
    }

    @Test
    public void testHoldMode() {
        module.setEnabled(true);
        module.getActivationModeSetting().setValue(SnaplookActivationMode.HOLD);
        module.getTargetPerspectiveSetting().setValue(SnaplookPerspective.THIRD_PERSON_BACK);

        controller.onKeyPressed();
        assertTrue(controller.isActive());
        assertEquals(SnaplookPerspective.THIRD_PERSON_BACK, adapter.getPerspective());

        controller.onKeyReleased();
        assertFalse(controller.isActive());
        assertEquals(SnaplookPerspective.FIRST_PERSON, adapter.getPerspective());
    }

    @Test
    public void testToggleMode() {
        module.setEnabled(true);
        module.getActivationModeSetting().setValue(SnaplookActivationMode.TOGGLE);
        module.getTargetPerspectiveSetting().setValue(SnaplookPerspective.THIRD_PERSON_FRONT);

        controller.onKeyPressed();
        assertTrue(controller.isActive());
        assertEquals(SnaplookPerspective.THIRD_PERSON_FRONT, adapter.getPerspective());

        controller.onKeyPressed();
        assertFalse(controller.isActive());
        assertEquals(SnaplookPerspective.FIRST_PERSON, adapter.getPerspective());
    }

    @Test
    public void testConflictWithManualF5Change() {
        module.setEnabled(true);
        module.getActivationModeSetting().setValue(SnaplookActivationMode.HOLD);
        module.getRespectManualChangesSetting().setValue(true);

        controller.onKeyPressed();
        assertTrue(controller.isActive());

        // Player presses F5 manually
        controller.onManualPerspectiveChanged(SnaplookPerspective.THIRD_PERSON_FRONT);
        assertFalse(controller.isActive());
        assertEquals(SnaplookPerspective.THIRD_PERSON_FRONT, controller.getPreviousPerspective());
    }

    @Test
    public void testResetsOnEvents() {
        module.setEnabled(true);
        controller.onKeyPressed();
        assertTrue(controller.isActive());

        controller.onWorldChange();
        assertFalse(controller.isActive());

        controller.onKeyPressed();
        assertTrue(controller.isActive());

        controller.onPlayerDeath();
        assertFalse(controller.isActive());
    }

    private static class MockPerspectiveAdapter implements ISnaplookPerspectiveAdapter {
        private SnaplookPerspective perspective;

        MockPerspectiveAdapter(SnaplookPerspective initial) {
            this.perspective = initial;
        }

        @Override
        public SnaplookPerspective getPerspective() {
            return perspective;
        }

        @Override
        public void setPerspective(SnaplookPerspective perspective) {
            this.perspective = perspective;
        }
    }
}
