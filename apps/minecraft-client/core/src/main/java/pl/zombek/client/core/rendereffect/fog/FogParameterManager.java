package pl.zombek.client.core.rendereffect.fog;

/**
 * Aggregates distance, color, and environment states into a coherent FogParameters instance.
 */
public class FogParameterManager {

    private final FogDistanceController distanceController;
    private final FogColorController colorController;
    private final FogEnvironmentManager environmentManager;
    private final FogProfileManager profileManager;

    public FogParameterManager(
            FogDistanceController distanceController,
            FogColorController colorController,
            FogEnvironmentManager environmentManager,
            FogProfileManager profileManager
    ) {
        this.distanceController = distanceController;
        this.colorController = colorController;
        this.environmentManager = environmentManager;
        this.profileManager = profileManager;
    }

    /**
     * Resolves complete fog parameters for the given rendering frame.
     */
    public FogParameters resolveParameters(
            float vanillaStart, float vanillaEnd,
            float vanillaR, float vanillaG, float vanillaB, float vanillaA,
            String dimensionId, FogEnvironmentType envType, boolean moduleEnabled
    ) {
        if (!moduleEnabled) {
            return FogParameters.vanilla(vanillaStart, vanillaEnd, vanillaR, vanillaG, vanillaB, vanillaA);
        }

        FogPreset preset = profileManager.getActivePreset();
        FogEnvironmentProfile envProfile = environmentManager.getProfile(envType != null ? envType : FogEnvironmentType.AIR);

        float[] distances = distanceController.computeDistances(vanillaStart, vanillaEnd, preset, envProfile);
        float[] colors = colorController.computeColor(vanillaR, vanillaG, vanillaB, vanillaA, dimensionId, envProfile);

        return new FogParameters(
                distances[0],
                distances[1],
                colors[0],
                colors[1],
                colors[2],
                colors[3],
                distanceController.getFogDensity(),
                true
        );
    }
}
