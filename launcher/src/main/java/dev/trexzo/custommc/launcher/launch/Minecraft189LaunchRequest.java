package dev.trexzo.custommc.launcher.launch;

import dev.trexzo.custommc.launcher.command.LaunchIdentity;
import dev.trexzo.custommc.launcher.command.Minecraft189RuntimeBundle;
import dev.trexzo.custommc.launcher.java.JavaRuntime;
import dev.trexzo.custommc.launcher.preflight.LaunchPreflightRequest;
import dev.trexzo.custommc.launcher.runtime.MinecraftInstallation;
import dev.trexzo.custommc.launcher.runtime.RuntimeTarget;

import java.nio.file.Path;
import java.util.Objects;

public final class Minecraft189LaunchRequest {
    public static final String VERSION_ID = "1.8.9";

    private final MinecraftInstallation installation;
    private final RuntimeTarget runtimeTarget;
    private final JavaRuntime javaRuntime;
    private final LaunchIdentity identity;
    private final Path gameDirectory;
    private final Path nativeStagingParent;
    private final int minimumMemoryMb;
    private final int maximumMemoryMb;
    private final Minecraft189RuntimeBundle runtimeBundle;

    public Minecraft189LaunchRequest(
            final MinecraftInstallation installation,
            final RuntimeTarget runtimeTarget,
            final JavaRuntime javaRuntime,
            final LaunchIdentity identity,
            final Path gameDirectory,
            final Path nativeStagingParent,
            final int minimumMemoryMb,
            final int maximumMemoryMb,
            final Minecraft189RuntimeBundle runtimeBundle) {
        this.installation = Objects.requireNonNull(
                installation,
                "installation");
        if (!VERSION_ID.equals(
                installation.versionId())) {
            throw new IllegalArgumentException(
                    "Minecraft 1.8.9 launch requires version "
                            + VERSION_ID
                            + ": "
                            + installation.versionId());
        }

        this.runtimeTarget = Objects.requireNonNull(
                runtimeTarget,
                "runtimeTarget");
        this.javaRuntime = Objects.requireNonNull(
                javaRuntime,
                "javaRuntime");
        this.identity = Objects.requireNonNull(
                identity,
                "identity");
        this.gameDirectory = normalize(
                gameDirectory,
                "gameDirectory");
        this.nativeStagingParent = normalize(
                nativeStagingParent,
                "nativeStagingParent");
        this.runtimeBundle = Objects.requireNonNull(
                runtimeBundle,
                "runtimeBundle");

        if (minimumMemoryMb <= 0) {
            throw new IllegalArgumentException(
                    "minimumMemoryMb must be positive");
        }
        if (maximumMemoryMb < minimumMemoryMb) {
            throw new IllegalArgumentException(
                    "maximumMemoryMb must be >= minimumMemoryMb");
        }

        this.minimumMemoryMb = minimumMemoryMb;
        this.maximumMemoryMb = maximumMemoryMb;
    }

    public MinecraftInstallation installation() {
        return installation;
    }

    public RuntimeTarget runtimeTarget() {
        return runtimeTarget;
    }

    public JavaRuntime javaRuntime() {
        return javaRuntime;
    }

    public LaunchIdentity identity() {
        return identity;
    }

    public Path gameDirectory() {
        return gameDirectory;
    }

    public Path nativeStagingParent() {
        return nativeStagingParent;
    }

    public int minimumMemoryMb() {
        return minimumMemoryMb;
    }

    public int maximumMemoryMb() {
        return maximumMemoryMb;
    }

    public Minecraft189RuntimeBundle runtimeBundle() {
        return runtimeBundle;
    }

    public LaunchPreflightRequest preflightRequest() {
        return new LaunchPreflightRequest(
                installation,
                runtimeTarget,
                javaRuntime,
                identity,
                gameDirectory,
                nativeStagingParent,
                minimumMemoryMb,
                maximumMemoryMb,
                runtimeBundle);
    }

    private static Path normalize(
            final Path path,
            final String name) {
        return Objects.requireNonNull(
                path,
                name)
                .toAbsolutePath()
                .normalize();
    }
}
