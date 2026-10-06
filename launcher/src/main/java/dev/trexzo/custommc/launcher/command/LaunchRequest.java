package dev.trexzo.custommc.launcher.command;

import dev.trexzo.custommc.launcher.metadata.MinecraftLaunchTemplate;
import dev.trexzo.custommc.launcher.runtime.OperatingSystem;

import java.nio.file.Path;
import java.util.Objects;

public final class LaunchRequest {
    private final Path javaExecutable;
    private final Path gameDirectory;
    private final Path assetsDirectory;
    private final Path nativeDirectory;
    private final MinecraftLaunchTemplate template;
    private final LaunchIdentity identity;
    private final OperatingSystem operatingSystem;
    private final int minimumMemoryMb;
    private final int maximumMemoryMb;
    private final LaunchRuntimeOverlay runtimeOverlay;

    public LaunchRequest(
            final Path javaExecutable,
            final Path gameDirectory,
            final Path assetsDirectory,
            final Path nativeDirectory,
            final MinecraftLaunchTemplate template,
            final LaunchIdentity identity,
            final OperatingSystem operatingSystem,
            final int minimumMemoryMb,
            final int maximumMemoryMb) {
        this(
                javaExecutable,
                gameDirectory,
                assetsDirectory,
                nativeDirectory,
                template,
                identity,
                operatingSystem,
                minimumMemoryMb,
                maximumMemoryMb,
                LaunchRuntimeOverlay.none());
    }

    public LaunchRequest(
            final Path javaExecutable,
            final Path gameDirectory,
            final Path assetsDirectory,
            final Path nativeDirectory,
            final MinecraftLaunchTemplate template,
            final LaunchIdentity identity,
            final OperatingSystem operatingSystem,
            final int minimumMemoryMb,
            final int maximumMemoryMb,
            final LaunchRuntimeOverlay runtimeOverlay) {
        if (minimumMemoryMb <= 0) {
            throw new IllegalArgumentException(
                    "minimumMemoryMb must be positive");
        }
        if (maximumMemoryMb < minimumMemoryMb) {
            throw new IllegalArgumentException(
                    "maximumMemoryMb must be >= minimumMemoryMb");
        }

        this.javaExecutable = normalize(
                javaExecutable,
                "javaExecutable");
        this.gameDirectory = normalize(
                gameDirectory,
                "gameDirectory");
        this.assetsDirectory = normalize(
                assetsDirectory,
                "assetsDirectory");
        this.nativeDirectory = normalize(
                nativeDirectory,
                "nativeDirectory");
        this.template = Objects.requireNonNull(
                template,
                "template");
        this.identity = Objects.requireNonNull(
                identity,
                "identity");
        this.operatingSystem = Objects.requireNonNull(
                operatingSystem,
                "operatingSystem");
        this.minimumMemoryMb = minimumMemoryMb;
        this.maximumMemoryMb = maximumMemoryMb;
        this.runtimeOverlay = Objects.requireNonNull(
                runtimeOverlay,
                "runtimeOverlay");
    }

    public Path javaExecutable() {
        return javaExecutable;
    }

    public Path gameDirectory() {
        return gameDirectory;
    }

    public Path assetsDirectory() {
        return assetsDirectory;
    }

    public Path nativeDirectory() {
        return nativeDirectory;
    }

    public MinecraftLaunchTemplate template() {
        return template;
    }

    public LaunchIdentity identity() {
        return identity;
    }

    public OperatingSystem operatingSystem() {
        return operatingSystem;
    }

    public int minimumMemoryMb() {
        return minimumMemoryMb;
    }

    public int maximumMemoryMb() {
        return maximumMemoryMb;
    }

    public LaunchRuntimeOverlay runtimeOverlay() {
        return runtimeOverlay;
    }

    private static Path normalize(
            final Path path,
            final String name) {
        return Objects.requireNonNull(path, name)
                .toAbsolutePath()
                .normalize();
    }
}
