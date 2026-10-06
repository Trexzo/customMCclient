package dev.trexzo.custommc.launcher.command;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CustomMcBootstrapOverlay {
    public static final String BOOTSTRAP_MAIN_CLASS =
            "dev.trexzo.custommc.bootstrap.CustomMcBootstrapMain";
    public static final String RUNTIME_MARKER =
            "--custommc-runtime";

    private CustomMcBootstrapOverlay() {
    }

    public static LaunchRuntimeOverlay create(
            final Path bootstrapArtifact,
            final String targetMainClass) {
        final String target =
                targetMainClass(
                        targetMainClass);

        return new LaunchRuntimeOverlay(
                Collections.singletonList(
                        Objects.requireNonNull(
                                bootstrapArtifact,
                                "bootstrapArtifact")),
                BOOTSTRAP_MAIN_CLASS,
                Collections.singletonList(
                        target));
    }

    public static LaunchRuntimeOverlay createWithRuntime(
            final Path bootstrapArtifact,
            final List<Path> runtimeArtifacts,
            final String runtimeInitializerClass,
            final String targetMainClass) {
        Objects.requireNonNull(
                runtimeArtifacts,
                "runtimeArtifacts");

        final String initializer =
                className(
                        runtimeInitializerClass,
                        "runtimeInitializerClass");
        final String target =
                targetMainClass(
                        targetMainClass);

        final List<Path> classpath =
                new ArrayList<Path>();
        classpath.add(
                Objects.requireNonNull(
                        bootstrapArtifact,
                        "bootstrapArtifact"));
        for (Path artifact : runtimeArtifacts) {
            classpath.add(
                    Objects.requireNonNull(
                            artifact,
                            "runtimeArtifact"));
        }

        final List<String> prefix =
                new ArrayList<String>();
        prefix.add(RUNTIME_MARKER);
        prefix.add(initializer);
        prefix.add(target);

        return new LaunchRuntimeOverlay(
                classpath,
                BOOTSTRAP_MAIN_CLASS,
                prefix);
    }

    private static String targetMainClass(
            final String targetMainClass) {
        final String target =
                className(
                        targetMainClass,
                        "targetMainClass");
        if (BOOTSTRAP_MAIN_CLASS.equals(target)) {
            throw new IllegalArgumentException(
                    "bootstrap cannot target itself");
        }
        return target;
    }

    private static String className(
            final String value,
            final String name) {
        Objects.requireNonNull(value, name);

        final String result =
                value.trim();
        if (result.isEmpty()) {
            throw new IllegalArgumentException(
                    name + " must not be blank");
        }
        return result;
    }
}
