package dev.trexzo.custommc.launcher.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class Minecraft189RuntimeBundleTest {
    @TempDir
    Path tempDir;

    @Test
    void bundleProducesExactInitializedOverlayOrder() {
        final Path bootstrap =
                tempDir.resolve("bootstrap.jar");
        final Path core =
                tempDir.resolve("core.jar");
        final Path platformApi =
                tempDir.resolve("platform-api.jar");
        final Path platform189 =
                tempDir.resolve("platform-1.8.9.jar");

        final Minecraft189RuntimeBundle bundle =
                new Minecraft189RuntimeBundle(
                        bootstrap,
                        core,
                        platformApi,
                        platform189);

        assertEquals(
                Arrays.asList(
                        bootstrap.toAbsolutePath().normalize(),
                        core.toAbsolutePath().normalize(),
                        platformApi.toAbsolutePath().normalize(),
                        platform189.toAbsolutePath().normalize()),
                bundle.artifacts());

        final LaunchRuntimeOverlay overlay =
                bundle.overlay(
                        "net.minecraft.client.main.Main");

        assertEquals(
                bundle.artifacts(),
                overlay.classpathPrefix());
        assertEquals(
                CustomMcBootstrapOverlay.BOOTSTRAP_MAIN_CLASS,
                overlay.mainClass(
                        "ignored.Main"));
        assertEquals(
                Arrays.asList(
                        CustomMcBootstrapOverlay.RUNTIME_MARKER,
                        Minecraft189RuntimeBundle.RUNTIME_INITIALIZER_CLASS,
                        "net.minecraft.client.main.Main"),
                overlay.mainArgumentsPrefix());
    }

    @Test
    void bundleNormalizesPathsAndRejectsAliasedArtifacts() {
        final Path root =
                tempDir.resolve("runtime");

        final IllegalArgumentException failure =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new Minecraft189RuntimeBundle(
                                root.resolve("bootstrap.jar"),
                                root.resolve(".")
                                        .resolve("bootstrap.jar"),
                                root.resolve("platform-api.jar"),
                                root.resolve("platform-1.8.9.jar")));

        assertEquals(
                "runtime bundle artifacts must be distinct",
                failure.getMessage());
    }

    @Test
    void artifactViewsAreImmutable() {
        final Minecraft189RuntimeBundle bundle =
                new Minecraft189RuntimeBundle(
                        tempDir.resolve("bootstrap.jar"),
                        tempDir.resolve("core.jar"),
                        tempDir.resolve("platform-api.jar"),
                        tempDir.resolve("platform-1.8.9.jar"));

        assertThrows(
                UnsupportedOperationException.class,
                () -> bundle.artifacts().clear());
        assertThrows(
                UnsupportedOperationException.class,
                () -> bundle.runtimeArtifacts().clear());
    }
}
