package dev.trexzo.custommc.launcher.runtime;

import java.nio.file.Path;
import java.util.Objects;

public final class MinecraftInstallation {
    private final Path home;
    private final String versionId;
    private final Path versionsDirectory;
    private final Path librariesDirectory;
    private final Path assetsDirectory;
    private final Path versionJson;
    private final Path versionJar;

    private MinecraftInstallation(
            final Path home,
            final String versionId) {
        this.home = Objects.requireNonNull(
                home,
                "home").toAbsolutePath().normalize();
        this.versionId = requireNonBlank(
                versionId,
                "versionId");

        versionsDirectory = this.home.resolve("versions");
        librariesDirectory = this.home.resolve("libraries");
        assetsDirectory = this.home.resolve("assets");

        final Path versionDirectory =
                versionsDirectory.resolve(this.versionId);
        versionJson = versionDirectory.resolve(
                this.versionId + ".json");
        versionJar = versionDirectory.resolve(
                this.versionId + ".jar");
    }

    public static MinecraftInstallation forVersion(
            final Path home,
            final String versionId) {
        return new MinecraftInstallation(home, versionId);
    }

    public Path home() {
        return home;
    }

    public String versionId() {
        return versionId;
    }

    public Path versionsDirectory() {
        return versionsDirectory;
    }

    public Path librariesDirectory() {
        return librariesDirectory;
    }

    public Path assetsDirectory() {
        return assetsDirectory;
    }

    public Path versionJson() {
        return versionJson;
    }

    public Path versionJar() {
        return versionJar;
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
