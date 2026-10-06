package dev.trexzo.custommc.bootstrap;

import java.util.Objects;

public final class BootstrapContext {
    private final String targetMainClass;
    private final String[] targetArguments;

    public BootstrapContext(
            final String targetMainClass,
            final String[] targetArguments) {
        this.targetMainClass =
                requireClassName(
                        targetMainClass,
                        "targetMainClass");
        this.targetArguments =
                Objects.requireNonNull(
                        targetArguments,
                        "targetArguments")
                        .clone();
    }

    public String targetMainClass() {
        return targetMainClass;
    }

    public String[] targetArguments() {
        return targetArguments.clone();
    }

    static String requireClassName(
            final String value,
            final String name) {
        Objects.requireNonNull(value, name);
        final String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(
                    name + " must not be blank");
        }
        return trimmed;
    }
}
