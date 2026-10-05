package dev.trexzo.custommc.launcher.preflight;

import dev.trexzo.custommc.launcher.command.LaunchIdentity;
import dev.trexzo.custommc.launcher.java.JavaRuntime;
import dev.trexzo.custommc.launcher.java.JavaRuntimeInspection;
import dev.trexzo.custommc.launcher.java.JavaRuntimeProbe;
import dev.trexzo.custommc.launcher.runtime.CpuArchitecture;
import dev.trexzo.custommc.launcher.runtime.MinecraftInstallation;
import dev.trexzo.custommc.launcher.runtime.OperatingSystem;
import dev.trexzo.custommc.launcher.runtime.RuntimeTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LaunchPreflightTest {
    @TempDir
    Path tempDir;

    @Test
    void preparesAndCleansCompleteNonExecutingLaunch()
            throws Exception {
        final MinecraftInstallation installation =
                createInstallation(true);
        final JavaRuntime javaRuntime =
                createJavaRuntime();

        final Path stagingParent =
                tempDir.resolve("native-staging");

        final LaunchPreflightRequest request =
                new LaunchPreflightRequest(
                        installation,
                        new RuntimeTarget(
                                OperatingSystem.LINUX,
                                CpuArchitecture.X64,
                                "6.0"),
                        javaRuntime,
                        new LaunchIdentity(
                                "Player",
                                "0123456789abcdef0123456789abcdef",
                                "secret-token",
                                "{}",
                                "mojang"),
                        tempDir.resolve("game"),
                        stagingParent,
                        512,
                        2048);

        final Path staged;
        try (LaunchPreflightResult result =
                new LaunchPreflight().prepare(request)) {
            staged = result.nativeDirectory();

            assertTrue(
                    result.integrityReport().passes());
            assertTrue(Files.isDirectory(staged));
            assertTrue(
                    result.command()
                            .arguments()
                            .contains("secret-token"));
            assertFalse(
                    result.command()
                            .redactedArguments()
                            .contains("secret-token"));
        }

        assertFalse(Files.exists(staged));
    }

    @Test
    void incompleteInstallationFailsBeforeStaging()
            throws Exception {
        final MinecraftInstallation installation =
                createInstallation(false);
        final JavaRuntime javaRuntime =
                createJavaRuntime();
        final Path stagingParent =
                tempDir.resolve("unused-staging");

        final LaunchPreflightRequest request =
                new LaunchPreflightRequest(
                        installation,
                        new RuntimeTarget(
                                OperatingSystem.LINUX,
                                CpuArchitecture.X64,
                                "6.0"),
                        javaRuntime,
                        new LaunchIdentity(
                                "Player",
                                "uuid",
                                "",
                                "{}",
                                "mojang"),
                        tempDir.resolve("game"),
                        stagingParent,
                        512,
                        1024);

        assertThrows(
                LaunchPreflightException.class,
                () -> new LaunchPreflight().prepare(request));

        assertFalse(Files.exists(stagingParent));
    }

    private MinecraftInstallation createInstallation(
            final boolean includeClient)
            throws Exception {
        final MinecraftInstallation installation =
                MinecraftInstallation.forVersion(
                        tempDir.resolve("minecraft"),
                        "1.8.9");

        Files.createDirectories(
                installation.versionJson().getParent());
        Files.createDirectories(
                installation.librariesDirectory());
        Files.createDirectories(
                installation.assetsDirectory());

        Files.write(
                installation.versionJson(),
                ("{"
                        + "\"id\":\"1.8.9\","
                        + "\"mainClass\":\"net.minecraft.client.main.Main\","
                        + "\"minecraftArguments\":\"--username ${auth_player_name} "
                        + "--accessToken ${auth_access_token}\","
                        + "\"assets\":\"1.8\","
                        + "\"libraries\":[]"
                        + "}")
                        .getBytes(StandardCharsets.UTF_8));

        if (includeClient) {
            Files.write(
                    installation.versionJar(),
                    new byte[] {1});
        }
        return installation;
    }

    private JavaRuntime createJavaRuntime()
            throws Exception {
        final Path home =
                tempDir.resolve("java-home");
        Files.createDirectories(home.resolve("bin"));
        Files.write(
                home.resolve("release"),
                "JAVA_VERSION=\"1.8.0_412\"\n"
                        .getBytes(StandardCharsets.UTF_8));
        Files.write(
                home.resolve("bin").resolve("java"),
                new byte[] {0});

        final JavaRuntimeInspection inspection =
                new JavaRuntimeProbe().inspect(
                        home,
                        OperatingSystem.LINUX);
        return inspection.runtime();
    }
}
