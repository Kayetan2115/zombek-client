package pl.zombek.client.settings;

import java.util.Objects;

/**
 * Enumeration setting allowing cycle or selection across defined enum values.
 *
 * @param <E> enum type
 */
public class EnumSetting<E extends Enum<E>> extends Setting<E> {
    private final Class<E> enumClass;
    private final E[] values;

    public EnumSetting(String id, String name, String description, E defaultValue) {
        super(id, name, description, defaultValue);
        this.enumClass = defaultValue.getDeclaringClass();
        this.values = enumClass.getEnumConstants();
    }

    public Class<E> getEnumClass() {
        return enumClass;
    }

    public E[] getValues() {
        return values;
    }

    public int getSelectedIndex() {
        E curr = getValue();
        for (int i = 0; i < values.length; i++) {
            if (values[i] == curr) return i;
        }
        return 0;
    }

    public void cycleNext() {
        int next = (getSelectedIndex() + 1) % values.length;
        setValue(values[next]);
    }

    public void cyclePrevious() {
        int prev = (getSelectedIndex() - 1 + values.length) % values.length;
        setValue(values[prev]);
    }

    public String getFormattedValue() {
        return getValue().name();
    }

    @Override
    public String serialize() {
        return getValue().name();
    }

    @Override
    public boolean deserialize(String raw) {
        if (raw == null || raw.trim().isEmpty()) return false;
        String trimmed = raw.trim();
        for (E candidate : values) {
            if (candidate.name().equalsIgnoreCase(trimmed)) {
                return setValue(candidate);
            }
        }
        return false;
    }
}
