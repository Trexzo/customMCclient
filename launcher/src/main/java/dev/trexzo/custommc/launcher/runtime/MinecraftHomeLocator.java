package dev.trexzo.custommc.launcher.runtime;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

public final class MinecraftHomeLocator {
    public Path locate(
            final Path explicitOverride,
            final OperatingSystem operatingSystem,
            final Map<String, String> environment,
            final String userHome) {
        if (explicitOverride != null) {
            return explicitOverride.toAbsolutePath().normalize();
        }

        Objects.requireNonNull(operatingSystem, "operatingSystem");
        Objects.requireNonNull(environment, "environment");
        final String home = requireNonBlank(userHome, "userHome");

        if (operatingSystem == OperatingSystem.WINDOWS) {
            final String appData = environment.get("APPDATA");
            if (appData != null && !appData.trim().isEmpty()) {
                return Paths.get(appData)
                        .resolve(".minecraft")
                        .toAbsolutePath()
                        .normalize();
            }

            return Paths.get(home)
                    .resolve("AppData")
                    .resolve("Roaming")
                    .resolve(".minecraft")
                    .toAbsolutePath()
                    .normalize();
        }

        if (operatingSystem == OperatingSystem.MACOS) {
            return Paths.get(home)
                    .resolve("Library")
                    .resolve("Application Support")
                    .resolve("minecraft")
                    .toAbsolutePath()
                    .normalize();
        }

        return Paths.get(home)
                .resolve(".minecraft")
                .toAbsolutePath()
                .normalize();
    }

    public Path locateCurrent(final Path explicitOverride) {
        return locate(
                explicitOverride,
                OperatingSystem.current(),
                System.getenv(),
                System.getProperty("user.home"));
    }

    private static String requireNonBlank(
            final String value,
            final String name) {
        Objects.requireNonNull(value, name);
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    name + " must not be blank");
        }
        return value;
    }
}
