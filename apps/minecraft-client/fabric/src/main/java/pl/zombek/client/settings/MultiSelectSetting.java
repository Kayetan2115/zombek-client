package pl.zombek.client.settings;

import java.util.*;

/**
 * Multiple-choice setting allowing users to toggle several options from a fixed list.
 */
public class MultiSelectSetting extends Setting<Set<String>> {
    private final List<String> availableOptions;

    public MultiSelectSetting(String id, String name, String description, List<String> availableOptions, Set<String> defaultSelected) {
        super(id, name, description, new LinkedHashSet<>(defaultSelected != null ? defaultSelected : Collections.emptySet()));
        this.availableOptions = Collections.unmodifiableList(new ArrayList<>(availableOptions != null ? availableOptions : Collections.emptyList()));
        // Ensure initial value only contains valid options
        this.value = sanitize(defaultValue);
    }

    public List<String> getAvailableOptions() {
        return availableOptions;
    }

    public boolean isSelected(String option) {
        return getValue().contains(option);
    }

    public void toggle(String option) {
        if (!availableOptions.contains(option)) return;
        Set<String> copy = new LinkedHashSet<>(getValue());
        if (copy.contains(option)) {
            copy.remove(option);
        } else {
            copy.add(option);
        }
        setValue(copy);
    }

    @Override
    protected Set<String> sanitize(Set<String> val) {
        if (val == null) return new LinkedHashSet<>();
        Set<String> filtered = new LinkedHashSet<>();
        for (String item : val) {
            if (availableOptions.contains(item)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    @Override
    public String serialize() {
        return String.join(",", getValue());
    }

    @Override
    public boolean deserialize(String raw) {
        if (raw == null) return false;
        Set<String> result = new LinkedHashSet<>();
        if (!raw.trim().isEmpty()) {
            String[] parts = raw.split(",");
            for (String part : parts) {
                String item = part.trim();
                if (availableOptions.contains(item)) {
                    result.add(item);
                }
            }
        }
        return setValue(result);
    }
}
