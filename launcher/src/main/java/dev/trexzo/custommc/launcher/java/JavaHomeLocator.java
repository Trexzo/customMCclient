package dev.trexzo.custommc.launcher.java;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

public final class JavaHomeLocator {
    public Path locate(
            final Path explicitOverride,
            final Map<String, String> environment,
            final String currentJavaHome) {
        if (explicitOverride != null) {
            return explicitOverride
                    .toAbsolutePath()
                    .normalize();
        }

        Objects.requireNonNull(environment, "environment");
        final String javaHome =
                environment.get("JAVA_HOME");
        if (javaHome != null
                && !javaHome.trim().isEmpty()) {
            return Paths.get(javaHome)
                    .toAbsolutePath()
                    .normalize();
        }

        if (currentJavaHome == null
                || currentJavaHome.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "no Java home candidate available");
        }

        return Paths.get(currentJavaHome)
                .toAbsolutePath()
                .normalize();
    }

    public Path locateCurrent(final Path explicitOverride) {
        return locate(
                explicitOverride,
                System.getenv(),
                System.getProperty("java.home"));
    }
}
