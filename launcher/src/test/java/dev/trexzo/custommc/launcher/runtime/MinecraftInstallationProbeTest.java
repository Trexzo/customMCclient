package dev.trexzo.custommc.launcher.runtime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MinecraftInstallationProbeTest {
    @TempDir
    Path tempDir;

    @Test
    void completeSyntheticInstallationIsLaunchReady()
            throws Exception {
        final MinecraftInstallation installation =
                MinecraftInstallation.forVersion(
                        tempDir,
                        "1.8.9");

        Files.createDirectories(
                installation.versionJson().getParent());
        Files.createDirectories(
                installation.librariesDirectory());
        Files.createDirectories(
                installation.assetsDirectory());
        Files.write(
                installation.versionJson(),
                "{}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        Files.write(
                installation.versionJar(),
                new byte[] {0});

        final InstallationInspection inspection =
                new MinecraftInstallationProbe().inspect(
                        installation);

        assertTrue(inspection.launchReady());
        assertTrue(inspection.problems().isEmpty());
    }

    @Test
    void missingFilesAreReportedWithoutMutation()
            throws Exception {
        final MinecraftInstallation installation =
                MinecraftInstallation.forVersion(
                        tempDir,
                        "1.8.9");

        Files.createDirectories(
                installation.versionsDirectory());
        Files.createDirectories(
                installation.librariesDirectory());
        Files.createDirectories(
                installation.assetsDirectory());

        final InstallationInspection inspection =
                new MinecraftInstallationProbe().inspect(
                        installation);

        assertFalse(inspection.launchReady());
        assertEquals(
                java.util.Arrays.asList(
                        InstallationProblem.VERSION_JSON_MISSING,
                        InstallationProblem.VERSION_JAR_MISSING),
                inspection.problems());
    }
}
