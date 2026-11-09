package pl.zombek.client.core.worldvisual;

import pl.zombek.client.core.worldvisual.fullbright.FullBrightController;
import pl.zombek.client.core.worldvisual.lighting.DynamicLightManager;
import pl.zombek.client.core.worldvisual.time.WorldTimeVisualController;
import pl.zombek.client.core.worldvisual.weather.WeatherVisualController;

/**
 * Central coordinator for all client-side world visual rendering overrides.
 * Enforces strict separation between true world state and visual override state.
 * Manages deterministic render priority:
 * 1. Base Minecraft world state
 * 2. Weather override
 * 3. Time of day / celestial override
 * 4. Dynamic ambient lighting
 * 5. Full Bright post-processing
 */
public class ZombekWorldVisualController {
    private static final ZombekWorldVisualController INSTANCE = new ZombekWorldVisualController();

    private final DynamicLightManager dynamicLightManager = DynamicLightManager.getInstance();
    private final FullBrightController fullBrightController = FullBrightController.getInstance();
    private final WorldTimeVisualController timeVisualController = WorldTimeVisualController.getInstance();
    private final WeatherVisualController weatherVisualController = WeatherVisualController.getInstance();

    private ZombekWorldVisualController() {}

    public static ZombekWorldVisualController getInstance() {
        return INSTANCE;
    }

    public DynamicLightManager getDynamicLightManager() {
        return dynamicLightManager;
    }

    public FullBrightController getFullBrightController() {
        return fullBrightController;
    }

    public WorldTimeVisualController getTimeVisualController() {
        return timeVisualController;
    }

    public WeatherVisualController getWeatherVisualController() {
        return weatherVisualController;
    }

    /**
     * Ticked by central core client tick.
     */
    public void onClientTick(double cameraX, double cameraY, double cameraZ) {
        pl.zombek.client.core.modules.visual.LightingModule mod = pl.zombek.client.core.modules.visual.LightingModule.getInstance();
        boolean mh = mod != null && mod.getMainHandLightSetting().getValue();
        boolean oh = mod != null && mod.getOffhandLightSetting().getValue();
        boolean di = mod != null && mod.getDroppedItemLightSetting().getValue();
        boolean ent = mod != null && mod.getEntityLightSetting().getValue();
        onClientTick(cameraX, cameraY, cameraZ, mh, oh, di, ent);
    }

    /**
     * Ticked by central core client tick with explicit lighting flags.
     */
    public void onClientTick(double cameraX, double cameraY, double cameraZ,
                             boolean includeMainHand, boolean includeOffhand,
                             boolean includeDroppedItems, boolean includeEntities) {
        dynamicLightManager.onClientTick(cameraX, cameraY, cameraZ,
                includeMainHand, includeOffhand, includeDroppedItems, includeEntities);
        fullBrightController.updateAnimation();
        timeVisualController.getTransitionManager().update();
        weatherVisualController.getTransitionManager().update();
    }

    /**
     * Creates an immutable state snapshot for inspection and debugging.
     */
    public WorldVisualOverrideState createSnapshot(String dimensionKey, long trueWorldTime, float trueRain, float trueThunder) {
        return new WorldVisualOverrideState(
                dynamicLightManager.isEnabled(),
                dynamicLightManager.getActiveSources().size(),
                fullBrightController.isEnabled(),
                fullBrightController.getMode(),
                fullBrightController.getCurrentBrightness(),
                timeVisualController.isEnabled(),
                timeVisualController.getPreset(),
                timeVisualController.getVisualTimeOfDay(trueWorldTime, dimensionKey),
                timeVisualController.getVisualSkyAngle(0.0f, dimensionKey),
                weatherVisualController.isEnabled(),
                weatherVisualController.getPreset(),
                weatherVisualController.getVisualRainGradient(trueRain, dimensionKey, true),
                weatherVisualController.getVisualThunderGradient(trueThunder, dimensionKey),
                System.nanoTime()
        );
    }

    /**
     * Invoked when connecting or disconnecting from a world or server.
     */
    public void onWorldChange() {
        dynamicLightManager.clear();
        fullBrightController.updateAnimation();
    }

    /**
     * Invoked when travelling between dimensions (Nether, End, Overworld).
     */
    public void onDimensionChange(String newDimensionKey) {
        dynamicLightManager.clear();
    }

    /**
     * Safe reset for testing or lifecycle shutdown.
     */
    public void resetAll() {
        dynamicLightManager.resetForTesting();
        fullBrightController.resetForTesting();
        timeVisualController.resetForTesting();
        weatherVisualController.resetForTesting();
    }

    public void shutdown() {
        resetAll();
    }
}
