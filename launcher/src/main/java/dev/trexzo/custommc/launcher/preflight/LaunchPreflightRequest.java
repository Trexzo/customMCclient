package dev.trexzo.custommc.launcher.preflight;

import dev.trexzo.custommc.launcher.command.LaunchIdentity;
import dev.trexzo.custommc.launcher.command.LaunchRuntimeOverlay;
import dev.trexzo.custommc.launcher.command.LaunchRuntimeOverlayResolver;
import dev.trexzo.custommc.launcher.java.JavaRuntime;
import dev.trexzo.custommc.launcher.metadata.MinecraftLaunchTemplate;
import dev.trexzo.custommc.launcher.runtime.MinecraftInstallation;
import dev.trexzo.custommc.launcher.runtime.RuntimeTarget;

import java.nio.file.Path;
import java.util.Objects;

public final class LaunchPreflightRequest {
    private final MinecraftInstallation installation;
    private final RuntimeTarget runtimeTarget;
    private final JavaRuntime javaRuntime;
    private final LaunchIdentity identity;
    private final Path gameDirectory;
    private final Path nativeStagingParent;
    private final int minimumMemoryMb;
    private final int maximumMemoryMb;
    private final LaunchRuntimeOverlay fixedRuntimeOverlay;
    private final LaunchRuntimeOverlayResolver runtimeOverlayResolver;

    public LaunchPreflightRequest(
            final MinecraftInstallation installation,
            final RuntimeTarget runtimeTarget,
            final JavaRuntime javaRuntime,
            final LaunchIdentity identity,
            final Path gameDirectory,
            final Path nativeStagingParent,
            final int minimumMemoryMb,
            final int maximumMemoryMb) {
        this(
                installation,
                runtimeTarget,
                javaRuntime,
                identity,
                gameDirectory,
                nativeStagingParent,
                minimumMemoryMb,
                maximumMemoryMb,
                LaunchRuntimeOverlay.none());
    }

    public LaunchPreflightRequest(
            final MinecraftInstallation installation,
            final RuntimeTarget runtimeTarget,
            final JavaRuntime javaRuntime,
            final LaunchIdentity identity,
            final Path gameDirectory,
            final Path nativeStagingParent,
            final int minimumMemoryMb,
            final int maximumMemoryMb,
            final LaunchRuntimeOverlay runtimeOverlay) {
        this(
                installation,
                runtimeTarget,
                javaRuntime,
                identity,
                gameDirectory,
                nativeStagingParent,
                minimumMemoryMb,
                maximumMemoryMb,
                Objects.requireNonNull(
                        runtimeOverlay,
                        "runtimeOverlay"),
                LaunchRuntimeOverlayResolver.fixed(
                        runtimeOverlay));
    }

    public LaunchPreflightRequest(
            final MinecraftInstallation installation,
            final RuntimeTarget runtimeTarget,
            final JavaRuntime javaRuntime,
            final LaunchIdentity identity,
            final Path gameDirectory,
            final Path nativeStagingParent,
            final int minimumMemoryMb,
            final int maximumMemoryMb,
            final LaunchRuntimeOverlayResolver runtimeOverlayResolver) {
        this(
                installation,
                runtimeTarget,
                javaRuntime,
                identity,
                gameDirectory,
                nativeStagingParent,
                minimumMemoryMb,
                maximumMemoryMb,
                null,
                Objects.requireNonNull(
                        runtimeOverlayResolver,
                        "runtimeOverlayResolver"));
    }

    private LaunchPreflightRequest(
            final MinecraftInstallation installation,
            final RuntimeTarget runtimeTarget,
            final JavaRuntime javaRuntime,
            final LaunchIdentity identity,
            final Path gameDirectory,
            final Path nativeStagingParent,
            final int minimumMemoryMb,
            final int maximumMemoryMb,
            final LaunchRuntimeOverlay fixedRuntimeOverlay,
            final LaunchRuntimeOverlayResolver runtimeOverlayResolver) {
        this.installation = Objects.requireNonNull(
                installation,
                "installation");
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
        this.fixedRuntimeOverlay = fixedRuntimeOverlay;
        this.runtimeOverlayResolver =
                Objects.requireNonNull(
                        runtimeOverlayResolver,
                        "runtimeOverlayResolver");

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

    public LaunchRuntimeOverlay runtimeOverlay() {
        if (fixedRuntimeOverlay == null) {
            throw new IllegalStateException(
                    "runtime overlay is template-aware");
        }
        return fixedRuntimeOverlay;
    }

    public LaunchRuntimeOverlay resolveRuntimeOverlay(
            final MinecraftLaunchTemplate template) {
        return Objects.requireNonNull(
                runtimeOverlayResolver.resolve(
                        Objects.requireNonNull(
                                template,
                                "template")),
                "resolved runtime overlay");
    }

    private static Path normalize(
            final Path path,
            final String name) {
        return Objects.requireNonNull(path, name)
                .toAbsolutePath()
                .normalize();
    }
}
