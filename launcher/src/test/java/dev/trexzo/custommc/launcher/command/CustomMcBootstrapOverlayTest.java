package dev.trexzo.custommc.launcher.command;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

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
    void factoryRejectsBlankAndRecursiveTargets() {
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
    }
}
