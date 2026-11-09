package pl.zombek.client.core.rendereffect.color;

/**
 * Platform/loader adapter for applying color grading transformations.
 */
public interface IColorGradingAdapter {
    void onColorSettingsUpdated(ColorSettings settings);
    void reset();
}
