package pl.zombek.client.core.worldvisual;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.worldvisual.fullbright.FullBrightMode;
import pl.zombek.client.core.worldvisual.lighting.DynamicLightSource;
import pl.zombek.client.core.worldvisual.lighting.DynamicLightSourceType;
import pl.zombek.client.core.worldvisual.time.TimeChangerPreset;
import pl.zombek.client.core.worldvisual.weather.WeatherPreset;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ZombekWorldVisualControllerTest {

    private ZombekWorldVisualController controller;

    @BeforeEach
    public void setUp() {
        controller = ZombekWorldVisualController.getInstance();
        controller.resetAll();
    }

    @Test
    public void testCoordinatorSnapshot() {
        controller.getDynamicLightManager().setEnabled(true);
        controller.getDynamicLightManager().setUpdateIntervalTicks(1);
        controller.getDynamicLightManager().setPlatformProvider((x, y, z, m, o, d, e) -> {
            List<DynamicLightSource> l = new ArrayList<>();
            l.add(new DynamicLightSource("torch", DynamicLightSourceType.MAIN_HAND, x, y, z, 14, 0));
            return l;
        });
        controller.getDynamicLightManager().onClientTick(0, 64, 0, true, true, true, true);

        controller.getFullBrightController().setSmoothBrightness(false);
        controller.getFullBrightController().setEnabled(true);
        controller.getFullBrightController().setMode(FullBrightMode.FULL_BRIGHT);

        controller.getTimeVisualController().getTransitionManager().configure(false, 0);
        controller.getTimeVisualController().setEnabled(true);
        controller.getTimeVisualController().setPreset(TimeChangerPreset.NOON);

        controller.getWeatherVisualController().getTransitionManager().configure(false, 0);
        controller.getWeatherVisualController().setEnabled(true);
        controller.getWeatherVisualController().setPreset(WeatherPreset.CLEAR);

        WorldVisualOverrideState snapshot = controller.createSnapshot("minecraft:overworld", 18000L, 1.0f, 1.0f);

        assertTrue(snapshot.dynamicLightEnabled());
        assertEquals(1, snapshot.activeDynamicLightSources());
        assertTrue(snapshot.fullBrightEnabled());
        assertEquals(15.0, snapshot.effectiveBrightness(), 0.01);
        assertTrue(snapshot.timeChangerEnabled());
        assertEquals(6000L, snapshot.visualTimeTicks());
        assertTrue(snapshot.weatherChangerEnabled());
        assertEquals(0.0f, snapshot.visualRainGradient(), 0.01f);
    }

    @Test
    public void testLifecycleResets() {
        controller.getDynamicLightManager().getCache().configure(10, 20.0);
        controller.onWorldChange();
        assertEquals(0, controller.getDynamicLightManager().getActiveSources().size());

        controller.shutdown();
        assertFalse(controller.getFullBrightController().isEnabled());
        assertFalse(controller.getTimeVisualController().isEnabled());
        assertFalse(controller.getWeatherVisualController().isEnabled());
    }
}
