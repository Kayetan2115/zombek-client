package pl.zombek.client.core.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Universal abstract base class for all module settings across all loaders.
 *
 * @param <T> value type
 */
public abstract class Setting<T> {
    private final String id;
    private final String name;
    private final String description;
    protected final T defaultValue;
    protected T value;

    private BooleanSupplier visibilityPredicate = () -> true;
    private Predicate<T> validator;
    private final List<Consumer<T>> changeListeners = new ArrayList<>();

    protected Setting(String id, String name, String description, T defaultValue) {
        this.id = Objects.requireNonNull(id, "Setting id cannot be null").trim();
        this.name = Objects.requireNonNull(name, "Setting name cannot be null").trim();
        this.description = description != null ? description : "";
        this.defaultValue = Objects.requireNonNull(defaultValue, "Default value cannot be null");
        this.value = defaultValue;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public T getValue() {
        return value;
    }

    public boolean setValue(T newValue) {
        if (newValue == null) {
            return false;
        }
        if (validator != null && !validator.test(newValue)) {
            return false;
        }
        T sanitized = sanitize(newValue);
        if (Objects.equals(this.value, sanitized)) {
            return true;
        }
        this.value = sanitized;
        notifyListeners(sanitized);
        return true;
    }

    public void reset() {
        setValue(defaultValue);
    }

    public boolean isVisible() {
        return visibilityPredicate == null || visibilityPredicate.getAsBoolean();
    }

    @SuppressWarnings("unchecked")
    public <S extends Setting<T>> S visibleWhen(BooleanSupplier predicate) {
        this.visibilityPredicate = predicate;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public <S extends Setting<T>> S setValidator(Predicate<T> validator) {
        this.validator = validator;
        return (S) this;
    }

    public Setting<T> addChangeListener(Consumer<T> listener) {
        if (listener != null) {
            this.changeListeners.add(listener);
        }
        return this;
    }

    protected void notifyListeners(T newValue) {
        for (Consumer<T> listener : changeListeners) {
            try {
                listener.accept(newValue);
            } catch (Exception e) {
                System.err.printf("[Setting:%s] Exception in change listener: %s%n", id, e.getMessage());
            }
        }
    }

    protected T sanitize(T val) {
        return val;
    }

    public abstract String serialize();
    public abstract boolean deserialize(String raw);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
