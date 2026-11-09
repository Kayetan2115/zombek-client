package pl.zombek.client.settings;

import java.util.Locale;

/**
 * Numeric slider setting supporting bounds, steps, and decimal precision.
 */
public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;
    private final int precision;
    private final String suffix;

    public NumberSetting(String id, String name, String description, double defaultValue, double min, double max, double step, int precision, String suffix) {
        super(id, name, description, defaultValue);
        if (min > max) {
            throw new IllegalArgumentException("Min cannot be greater than max (" + min + " > " + max + ")");
        }
        this.min = min;
        this.max = max;
        this.step = step > 0 ? step : 1.0;
        this.precision = Math.max(0, precision);
        this.suffix = suffix != null ? suffix : "";
        // Re-sanitize initial value according to constraints
        this.value = sanitize(defaultValue);
    }

    public NumberSetting(String id, String name, String description, double defaultValue, double min, double max, double step) {
        this(id, name, description, defaultValue, min, max, step, step < 1.0 ? 1 : 0, "");
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    public int getPrecision() {
        return precision;
    }

    public String getSuffix() {
        return suffix;
    }

    public int getIntValue() {
        return (int) Math.round(getValue());
    }

    public float getFloatValue() {
        return getValue().floatValue();
    }

    @Override
    protected Double sanitize(Double val) {
        if (val == null) return defaultValue;
        double clamped = Math.max(min, Math.min(max, val));
        if (step > 0) {
            double stepsCount = Math.round((clamped - min) / step);
            clamped = min + (stepsCount * step);
            // Re-clamp to guard against floating-point inaccuracies
            clamped = Math.max(min, Math.min(max, clamped));
        }
        if (precision >= 0) {
            double factor = Math.pow(10, precision);
            clamped = Math.round(clamped * factor) / factor;
        }
        return clamped;
    }

    public String getFormattedValue() {
        if (precision == 0) {
            return String.format(Locale.US, "%d%s", getIntValue(), suffix);
        }
        return String.format(Locale.US, "%." + precision + "f%s", getValue(), suffix);
    }

    @Override
    public String serialize() {
        return String.format(Locale.US, "%." + Math.max(1, precision) + "f", getValue());
    }

    @Override
    public boolean deserialize(String raw) {
        if (raw == null) return false;
        try {
            double parsed = Double.parseDouble(raw.trim());
            return setValue(parsed);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
