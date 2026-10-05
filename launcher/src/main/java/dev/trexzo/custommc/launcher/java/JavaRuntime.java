package dev.trexzo.custommc.launcher.java;

import java.nio.file.Path;
import java.util.Objects;

public final class JavaRuntime {
    private final Path home;
    private final Path executable;
    private final String version;
    private final int majorVersion;

    public JavaRuntime(
            final Path home,
            final Path executable,
            final String version,
            final int majorVersion) {
        if (majorVersion <= 0) {
            throw new IllegalArgumentException(
                    "majorVersion must be positive");
        }

        this.home = normalize(home, "home");
        this.executable = normalize(
                executable,
                "executable");
        this.version = Objects.requireNonNull(
                version,
                "version");
        this.majorVersion = majorVersion;
    }

    public Path home() {
        return home;
    }

    public Path executable() {
        return executable;
    }

    public String version() {
        return version;
    }

    public int majorVersion() {
        return majorVersion;
    }

    private static Path normalize(
            final Path path,
            final String name) {
        return Objects.requireNonNull(path, name)
                .toAbsolutePath()
                .normalize();
    }
}
