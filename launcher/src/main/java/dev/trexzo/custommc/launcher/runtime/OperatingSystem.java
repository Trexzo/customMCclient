package dev.trexzo.custommc.launcher.runtime;

import java.util.Locale;
import java.util.Objects;

public enum OperatingSystem {
    WINDOWS("windows"),
    MACOS("osx"),
    LINUX("linux");

    private final String metadataName;

    OperatingSystem(final String metadataName) {
        this.metadataName = metadataName;
    }

    public String metadataName() {
        return metadataName;
    }

    public static OperatingSystem current() {
        return detect(System.getProperty("os.name"));
    }

    public static OperatingSystem detect(final String osName) {
        final String normalized = Objects.requireNonNull(
                osName,
                "osName").toLowerCase(Locale.ROOT);

        if (normalized.contains("mac")
                || normalized.contains("darwin")) {
            return MACOS;
        }
        if (normalized.contains("win")) {
            return WINDOWS;
        }
        return LINUX;
    }
}
