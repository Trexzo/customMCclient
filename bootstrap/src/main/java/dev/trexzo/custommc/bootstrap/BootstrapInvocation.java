package dev.trexzo.custommc.bootstrap;

import java.util.Arrays;
import java.util.Objects;

public final class BootstrapInvocation {
    public static final String RUNTIME_MARKER =
            "--custommc-runtime";

    private final String runtimeInitializerClass;
    private final String targetMainClass;
    private final String[] targetArguments;

    private BootstrapInvocation(
            final String runtimeInitializerClass,
            final String targetMainClass,
            final String[] targetArguments) {
        this.runtimeInitializerClass = runtimeInitializerClass;
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

        if (RUNTIME_MARKER.equals(
                arguments[0])) {
            if (arguments.length < 3) {
                throw new IllegalArgumentException(
                        "runtime bootstrap requires initializer and target main");
            }

            final String initializer =
                    BootstrapContext.requireClassName(
                            arguments[1],
                            "runtime initializer class");
            final String target =
                    targetClass(arguments[2]);

            return new BootstrapInvocation(
                    initializer,
                    target,
                    Arrays.copyOfRange(
                            arguments,
                            3,
                            arguments.length));
        }

        return new BootstrapInvocation(
                null,
                targetClass(arguments[0]),
                Arrays.copyOfRange(
                        arguments,
                        1,
                        arguments.length));
    }

    public boolean hasRuntimeInitializer() {
        return runtimeInitializerClass != null;
    }

    public String runtimeInitializerClass() {
        return runtimeInitializerClass;
    }

    public String targetMainClass() {
        return targetMainClass;
    }

    public String[] targetArguments() {
        return targetArguments.clone();
    }

    public BootstrapContext context() {
        return new BootstrapContext(
                targetMainClass,
                targetArguments);
    }

    private static String targetClass(
            final String value) {
        final String target =
                BootstrapContext.requireClassName(
                        value,
                        "target main class");
        if (CustomMcBootstrapMain.class.getName()
                .equals(target)) {
            throw new IllegalArgumentException(
                    "bootstrap cannot target itself");
        }
        return target;
    }
}
