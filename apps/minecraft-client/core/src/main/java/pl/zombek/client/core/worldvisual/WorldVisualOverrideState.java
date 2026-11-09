package pl.zombek.client.core.worldvisual;

import pl.zombek.client.core.worldvisual.fullbright.FullBrightMode;
import pl.zombek.client.core.worldvisual.time.TimeChangerPreset;
import pl.zombek.client.core.worldvisual.weather.WeatherPreset;

/**
 * Snapshot of the active visual overrides applied to the client renderer.
 */
public record WorldVisualOverrideState(
        boolean dynamicLightEnabled,
        int activeDynamicLightSources,
        boolean fullBrightEnabled,
        FullBrightMode fullBrightMode,
        double effectiveBrightness,
        boolean timeChangerEnabled,
        TimeChangerPreset timePreset,
        long visualTimeTicks,
        float visualSkyAngle,
        boolean weatherChangerEnabled,
        WeatherPreset weatherPreset,
        float visualRainGradient,
        float visualThunderGradient,
        long timestampNanos
) {
}
