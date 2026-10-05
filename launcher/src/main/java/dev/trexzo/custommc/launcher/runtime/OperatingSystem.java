package dev.trexzo.custommc.launcher.runtime;

import java.util.Locale;
import java.util.Objects;

public enum OperatingSystem {
    WINDOWS,
    MACOS,
    LINUX;

    public static OperatingSystem current() {
        return detect(System.getProperty("os.name"));
    }

    public static OperatingSystem detect(final String osName) {
        final String normalized = Objects.requireNonNull(
                osName,
                "osName").toLowerCase(Locale.ROOT);

        if (normalized.contains("win")) {
            return WINDOWS;
        }
        if (normalized.contains("mac")
                || normalized.contains("darwin")) {
            return MACOS;
        }
        return LINUX;
    }
}
