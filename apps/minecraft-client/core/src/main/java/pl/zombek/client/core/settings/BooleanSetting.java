package pl.zombek.client.core.settings;

/**
 * Boolean toggle setting.
 */
public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String id, String name, String description, boolean defaultValue) {
        super(id, name, description, defaultValue);
    }

    public void toggle() {
        setValue(!getValue());
    }

    @Override
    public String serialize() {
        return String.valueOf(getValue());
    }

    @Override
    public boolean deserialize(String raw) {
        if (raw == null) return false;
        String trimmed = raw.trim();
        if ("true".equalsIgnoreCase(trimmed) || "1".equals(trimmed)) {
            return setValue(true);
        } else if ("false".equalsIgnoreCase(trimmed) || "0".equals(trimmed)) {
            return setValue(false);
        }
        return false;
    }
}
