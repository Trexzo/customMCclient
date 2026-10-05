package dev.trexzo.custommc.core.setting;

import java.util.Objects;
import java.util.function.Predicate;

public final class Setting<T> {
    private final String id;
    private final T defaultValue;
    private final Predicate<T> validator;
    private final SettingCodec<T> codec;
    private T value;

    public Setting(
            final String id,
            final T defaultValue,
            final Predicate<T> validator) {
        this(id, defaultValue, validator, null);
    }

    public Setting(
            final String id,
            final T defaultValue,
            final Predicate<T> validator,
            final SettingCodec<T> codec) {
        this.id = requireId(id);
        this.defaultValue = Objects.requireNonNull(
                defaultValue,
                "defaultValue");
        this.validator = Objects.requireNonNull(validator, "validator");
        this.codec = codec;

        if (!validator.test(defaultValue)) {
            throw new IllegalArgumentException(
                    "default value rejected for " + id);
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

    public boolean isPersistent() {
        return codec != null;
    }

    public void set(final T next) {
        Objects.requireNonNull(next, "next");
        if (!validator.test(next)) {
            throw new IllegalArgumentException(
                    "invalid value for " + id);
        }
        value = next;
    }

    public T decode(final String encoded) {
        final SettingCodec<T> persistentCodec = requireCodec();
        final T decoded = Objects.requireNonNull(
                persistentCodec.decode(
                        Objects.requireNonNull(encoded, "encoded")),
                "decoded");

        if (!validator.test(decoded)) {
            throw new IllegalArgumentException(
                    "decoded value rejected for " + id);
        }
        return decoded;
    }

    public String encode() {
        return requireCodec().encode(value);
    }

    public void reset() {
        value = defaultValue;
    }

    private SettingCodec<T> requireCodec() {
        if (codec == null) {
            throw new IllegalStateException(
                    "setting is not persistent: " + id);
        }
        return codec;
    }

    private static String requireId(final String id) {
        Objects.requireNonNull(id, "id");
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "setting id must not be blank");
        }
        return id;
    }
}
