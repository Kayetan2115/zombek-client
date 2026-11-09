package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HudLayerManagerTest {

    private HudLayerManager layerManager;
    private HudLayout layout;

    @BeforeEach
    public void setUp() {
        layerManager = HudLayerManager.getInstance();
        layout = new HudLayout("test", "Układ Testowy");
        HudLayoutValidator.repairLayout(layout);
    }

    @Test
    public void testDefaultLayersExist() {
        assertNotNull(layout.getLayer(HudLayer.LAYER_BACKGROUND));
        assertNotNull(layout.getLayer(HudLayer.LAYER_MAIN_HUD));
        assertNotNull(layout.getLayer(HudLayer.LAYER_INFORMATION));
        assertNotNull(layout.getLayer(HudLayer.LAYER_NOTIFICATIONS));
    }

    @Test
    public void testCreateCustomLayer() {
        HudLayer custom = layerManager.createLayer(layout, "custom_layer", "Własna Warstwa", 600);
        assertNotNull(custom);
        assertEquals("custom_layer", custom.getLayerId());
        assertEquals(600, custom.getZIndexBase());
        assertNotNull(layout.getLayer("custom_layer"));
    }

    @Test
    public void testCannotDeleteSystemLayer() {
        assertFalse(layerManager.deleteLayer(layout, HudLayer.LAYER_MAIN_HUD));
        assertNotNull(layout.getLayer(HudLayer.LAYER_MAIN_HUD));
    }

    @Test
    public void testAssignComponentToLayer() {
        HudComponentState state = new HudComponentState("fps", 10, 10, 40, 20);
        layout.setComponentState(state);

        boolean assigned = layerManager.assignComponentToLayer(layout, "fps", HudLayer.LAYER_INFORMATION);
        assertTrue(assigned);
        assertEquals(HudLayer.LAYER_INFORMATION, state.getLayerId());
        assertTrue(state.getZIndex() >= 200, "Z-index should reflect information layer base (200)");
    }

    @Test
    public void testBringToFrontAndSendToBack() {
        HudComponentState c1 = new HudComponentState("fps", 10, 10, 40, 20);
        c1.setZIndex(10);
        HudComponentState c2 = new HudComponentState("ping", 10, 35, 40, 20);
        c2.setZIndex(20);

        layout.setComponentState(c1);
        layout.setComponentState(c2);

        layerManager.bringToFront(layout, "fps");
        assertTrue(c1.getZIndex() > c2.getZIndex(), "c1 should now have higher Z-index than c2");

        layerManager.sendToBack(layout, "fps");
        assertTrue(c1.getZIndex() < c2.getZIndex(), "c1 should now have lower Z-index than c2");
    }

    @Test
    public void testGetSortedComponents() {
        HudLayout cleanLayout = new HudLayout("clean", "Układ Czysty");
        HudComponentState c1 = new HudComponentState("fps", 10, 10, 40, 20);
        c1.setZIndex(50);
        HudComponentState c2 = new HudComponentState("ping", 10, 35, 40, 20);
        c2.setZIndex(10);
        HudComponentState c3 = new HudComponentState("coords", 10, 60, 40, 20);
        c3.setZIndex(30);

        cleanLayout.setComponentState(c1);
        cleanLayout.setComponentState(c2);
        cleanLayout.setComponentState(c3);

        List<HudComponentState> sorted = layerManager.getSortedComponents(cleanLayout);
        assertEquals(3, sorted.size());
        assertEquals("ping", sorted.get(0).getComponentId());
        assertEquals("coords", sorted.get(1).getComponentId());
        assertEquals("fps", sorted.get(2).getComponentId());
    }
}
