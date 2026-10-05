package dev.trexzo.custommc.core.setting;

import java.util.Objects;
import java.util.function.Predicate;

public final class Setting<T> {
    private final String id;
    private final T defaultValue;
    private final Predicate<T> validator;
    private T value;

    public Setting(
            final String id,
            final T defaultValue,
            final Predicate<T> validator) {
        this.id = requireId(id);
        this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
        this.validator = Objects.requireNonNull(validator, "validator");
        if (!validator.test(defaultValue)) {
            throw new IllegalArgumentException("default value rejected for " + id);
        }
        value = defaultValue;
    }

    public String id() {
        return id;
    }

    public T get() {
        return value;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public void set(final T next) {
        Objects.requireNonNull(next, "next");
        if (!validator.test(next)) {
            throw new IllegalArgumentException("invalid value for " + id);
        }
        value = next;
    }

    public void reset() {
        value = defaultValue;
    }

    private static String requireId(final String id) {
        Objects.requireNonNull(id, "id");
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException("setting id must not be blank");
        }
        return id;
    }
}
