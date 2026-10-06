package dev.trexzo.custommc.bootstrap;

import java.util.Arrays;
import java.util.Objects;

public final class BootstrapInvocation {
    private final String targetMainClass;
    private final String[] targetArguments;

    private BootstrapInvocation(
            final String targetMainClass,
            final String[] targetArguments) {
        this.targetMainClass = targetMainClass;
        this.targetArguments = targetArguments;
    }

    public static BootstrapInvocation parse(
            final String[] arguments) {
        Objects.requireNonNull(
                arguments,
                "arguments");
        if (arguments.length == 0) {
            throw new IllegalArgumentException(
                    "missing target main class");
        }

        final String target =
                Objects.requireNonNull(
                        arguments[0],
                        "target main class")
                        .trim();
        if (target.isEmpty()) {
            throw new IllegalArgumentException(
                    "target main class must not be blank");
        }
        if (CustomMcBootstrapMain.class.getName()
                .equals(target)) {
            throw new IllegalArgumentException(
                    "bootstrap cannot target itself");
        }

        return new BootstrapInvocation(
                target,
                Arrays.copyOfRange(
                        arguments,
                        1,
                        arguments.length));
    }

    public String targetMainClass() {
        return targetMainClass;
    }

    public String[] targetArguments() {
        return targetArguments.clone();
    }
}
