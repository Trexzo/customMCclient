package dev.trexzo.custommc.launcher.command;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CustomMcBootstrapOverlayTest {
    @Test
    void factoryBuildsExactBootstrapOverlayContract() {
        final Path artifact =
                java.nio.file.Paths.get(
                        "build/bootstrap.jar");

        final LaunchRuntimeOverlay overlay =
                CustomMcBootstrapOverlay.create(
                        artifact,
                        "net.minecraft.client.main.Main");

        assertEquals(
                1,
                overlay.classpathPrefix().size());
        assertEquals(
                artifact.toAbsolutePath().normalize(),
                overlay.classpathPrefix().get(0));
        assertTrue(overlay.overridesMainClass());
        assertEquals(
                CustomMcBootstrapOverlay.BOOTSTRAP_MAIN_CLASS,
                overlay.mainClass(
                        "ignored.Main"));
        assertEquals(
                java.util.Collections.singletonList(
                        "net.minecraft.client.main.Main"),
                overlay.mainArgumentsPrefix());
    }

    @Test
    void runtimeFactoryBuildsExplicitInitializerProtocolAndClasspathOrder() {
        final Path bootstrap =
                java.nio.file.Paths.get(
                        "build/bootstrap.jar");
        final Path core =
                java.nio.file.Paths.get(
                        "build/core.jar");
        final Path platform =
                java.nio.file.Paths.get(
                        "build/platform.jar");

        final LaunchRuntimeOverlay overlay =
                CustomMcBootstrapOverlay.createWithRuntime(
                        bootstrap,
                        Arrays.asList(
                                core,
                                platform),
                        " dev.trexzo.RuntimeInitializer ",
                        " net.minecraft.client.main.Main ");

        assertEquals(
                Arrays.asList(
                        bootstrap.toAbsolutePath().normalize(),
                        core.toAbsolutePath().normalize(),
                        platform.toAbsolutePath().normalize()),
                overlay.classpathPrefix());
        assertEquals(
                CustomMcBootstrapOverlay.BOOTSTRAP_MAIN_CLASS,
                overlay.mainClass(
                        "ignored.Main"));
        assertEquals(
                Arrays.asList(
                        CustomMcBootstrapOverlay.RUNTIME_MARKER,
                        "dev.trexzo.RuntimeInitializer",
                        "net.minecraft.client.main.Main"),
                overlay.mainArgumentsPrefix());
    }

    @Test
    void factoryRejectsBlankRecursiveAndDuplicateRuntimeInputs() {
        final Path artifact =
                java.nio.file.Paths.get(
                        "bootstrap.jar");

        assertThrows(
                IllegalArgumentException.class,
                () -> CustomMcBootstrapOverlay.create(
                        artifact,
                        " "));
        assertThrows(
                IllegalArgumentException.class,
                () -> CustomMcBootstrapOverlay.create(
                        artifact,
                        CustomMcBootstrapOverlay.BOOTSTRAP_MAIN_CLASS));
        assertThrows(
                IllegalArgumentException.class,
                () -> CustomMcBootstrapOverlay.createWithRuntime(
                        artifact,
                        java.util.Collections.<Path>emptyList(),
                        " ",
                        "net.minecraft.client.main.Main"));
        assertThrows(
                IllegalArgumentException.class,
                () -> CustomMcBootstrapOverlay.createWithRuntime(
                        artifact,
                        java.util.Collections.singletonList(
                                artifact),
                        "dev.trexzo.RuntimeInitializer",
                        "net.minecraft.client.main.Main"));
    }
}
