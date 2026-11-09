package pl.zombek.client.core.settings;

import java.util.regex.Pattern;

/**
 * Textual setting with optional length constraints and regex validation.
 */
public class StringSetting extends Setting<String> {
    private final int maxLength;
    private final Pattern pattern;

    public StringSetting(String id, String name, String description, String defaultValue, int maxLength, Pattern pattern) {
        super(id, name, description, defaultValue != null ? defaultValue : "");
        this.maxLength = maxLength > 0 ? maxLength : 256;
        this.pattern = pattern;
    }

    public StringSetting(String id, String name, String description, String defaultValue, int maxLength) {
        this(id, name, description, defaultValue, maxLength, null);
    }

    public StringSetting(String id, String name, String description, String defaultValue) {
        this(id, name, description, defaultValue, 256, null);
    }

    public int getMaxLength() {
        return maxLength;
    }

    @Override
    protected String sanitize(String val) {
        if (val == null) return "";
        if (val.length() > maxLength) {
            val = val.substring(0, maxLength);
        }
        if (pattern != null && !pattern.matcher(val).matches()) {
            return defaultValue;
        }
        return val;
    }

    @Override
    public String serialize() {
        return getValue();
    }

    @Override
    public boolean deserialize(String raw) {
        if (raw == null) return false;
        return setValue(raw);
    }
}
