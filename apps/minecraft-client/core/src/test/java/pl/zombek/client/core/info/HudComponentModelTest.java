package pl.zombek.client.core.info;

import org.junit.jupiter.api.Test;
import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.component.HudComponentModel;
import pl.zombek.client.core.hud.component.HudComponentStyle;

import static org.junit.jupiter.api.Assertions.*;

public class HudComponentModelTest {

    @Test
    public void testInitializationAndDefaults() {
        HudComponentModel model = new HudComponentModel("test_mod", 100, 40, HudAnchor.TOP_LEFT);
        assertEquals("test_mod", model.getModuleId());
        assertEquals(100, model.getWidth());
        assertEquals(40, model.getHeight());
        assertEquals(HudAnchor.TOP_LEFT, model.getAnchor());
        assertEquals(HudComponentStyle.CLASSIC, model.getStyle());
        assertEquals(1.0, model.getScale(), 0.001);
        assertTrue(model.isVisible());
    }

    @Test
    public void testEffectiveDimensionsWithScale() {
        HudComponentModel model = new HudComponentModel("test_scale", 100, 50, HudAnchor.TOP_LEFT);
        model.setScale(1.5);
        assertEquals(150, model.getEffectiveWidth());
        assertEquals(75, model.getEffectiveHeight());
    }

    @Test
    public void testAnchorCalculationsAndClamping() {
        HudComponentModel model = new HudComponentModel("test_anchor", 100, 50, HudAnchor.TOP_RIGHT);
        model.setPosX(10);
        model.setPosY(20);

        int screenW = 800;
        int screenH = 600;

        // TOP_RIGHT: 800 - 100 - 10 = 690
        assertEquals(690, model.getScreenX(screenW));
        assertEquals(20, model.getScreenY(screenH));

        // Test clamping to prevent pushing outside screen
        model.setPosX(1000);
        model.clampToScreen(screenW, screenH);
        assertTrue(model.getPosX() <= screenW - model.getEffectiveWidth());
    }
}
