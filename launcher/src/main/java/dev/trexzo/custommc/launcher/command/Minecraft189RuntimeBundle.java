package dev.trexzo.custommc.launcher.command;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import dev.trexzo.custommc.launcher.metadata.MinecraftLaunchTemplate;

public final class Minecraft189RuntimeBundle
        implements LaunchRuntimeOverlayResolver {
    public static final String RUNTIME_INITIALIZER_CLASS =
            "dev.trexzo.custommc.platform.v1_8_9."
                    + "Minecraft189BootstrapInitializer";

    private final Path bootstrapArtifact;
    private final Path coreArtifact;
    private final Path platformApiArtifact;
    private final Path asmArtifact;
    private final Path platform189Artifact;

    public static Minecraft189RuntimeBundle fromDirectory(
            final Path directory) {
        final Path root =
                normalize(
                        directory,
                        "directory");
        return new Minecraft189RuntimeBundle(
                root.resolve("bootstrap.jar"),
                root.resolve("core.jar"),
                root.resolve("platform-api.jar"),
                root.resolve("asm.jar"),
                root.resolve("platform-1.8.9.jar"));
    }

    public Minecraft189RuntimeBundle(
            final Path bootstrapArtifact,
            final Path coreArtifact,
            final Path platformApiArtifact,
            final Path asmArtifact,
            final Path platform189Artifact) {
        this.bootstrapArtifact =
                normalize(
                        bootstrapArtifact,
                        "bootstrapArtifact");
        this.coreArtifact =
                normalize(
                        coreArtifact,
                        "coreArtifact");
        this.platformApiArtifact =
                normalize(
                        platformApiArtifact,
                        "platformApiArtifact");
        this.asmArtifact =
                normalize(
                        asmArtifact,
                        "asmArtifact");
        this.platform189Artifact =
                normalize(
                        platform189Artifact,
                        "platform189Artifact");

        final Set<Path> distinct =
                new LinkedHashSet<Path>(
                        artifacts());
        if (distinct.size() != 5) {
            throw new IllegalArgumentException(
                    "runtime bundle artifacts must be distinct");
        }
    }

    public Path bootstrapArtifact() {
        return bootstrapArtifact;
    }

    public Path coreArtifact() {
        return coreArtifact;
    }

    public Path platformApiArtifact() {
        return platformApiArtifact;
    }

    public Path asmArtifact() {
        return asmArtifact;
    }

    public Path platform189Artifact() {
        return platform189Artifact;
    }

    public List<Path> artifacts() {
        return Collections.unmodifiableList(
                Arrays.asList(
                        bootstrapArtifact,
                        coreArtifact,
                        platformApiArtifact,
                        asmArtifact,
                        platform189Artifact));
    }

    public List<Path> runtimeArtifacts() {
        return Collections.unmodifiableList(
                Arrays.asList(
                        coreArtifact,
                        platformApiArtifact,
                        asmArtifact,
                        platform189Artifact));
    }

    @Override
    public LaunchRuntimeOverlay resolve(
            final MinecraftLaunchTemplate template) {
        return overlay(
                Objects.requireNonNull(
                        template,
                        "template")
                        .mainClass());
    }

    public LaunchRuntimeOverlay overlay(
            final String targetMainClass) {
        return CustomMcBootstrapOverlay.createWithRuntime(
                bootstrapArtifact,
                runtimeArtifacts(),
                RUNTIME_INITIALIZER_CLASS,
                targetMainClass);
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
