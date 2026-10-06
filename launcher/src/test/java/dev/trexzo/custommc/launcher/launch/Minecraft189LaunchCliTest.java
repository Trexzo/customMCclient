package dev.trexzo.custommc.launcher.launch;

import dev.trexzo.custommc.launcher.runtime.CpuArchitecture;
import dev.trexzo.custommc.launcher.runtime.OperatingSystem;
import dev.trexzo.custommc.launcher.runtime.RuntimeTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189LaunchCliTest {
    @TempDir
    Path tempDir;

    @Test
    void prepareBuildsCanonicalRequestAndReadsTokenFromEnvironment()
            throws Exception {
        final Path minecraftHome =
                tempDir.resolve("minecraft");
        final Path javaHome =
                javaRuntime();
        final Path overlay =
                tempDir.resolve("runtime-overlay");

        final Map<String, String> environment =
                new HashMap<String, String>();
        environment.put(
                Minecraft189LaunchCli.DEFAULT_ACCESS_TOKEN_ENV,
                "secret-token");

        final Minecraft189LaunchRequest request =
                Minecraft189LaunchCli.prepare(
                        new String[] {
                                "--minecraft-home",
                                minecraftHome.toString(),
                                "--java-home",
                                javaHome.toString(),
                                "--runtime-overlay",
                                overlay.toString(),
                                "--player-name",
                                "Player",
                                "--uuid",
                                "0123456789abcdef"
                        },
                        environment,
                        target());

        assertEquals(
                Minecraft189LaunchRequest.VERSION_ID,
                request.installation()
                        .versionId());
        assertEquals(
                minecraftHome.toAbsolutePath()
                        .normalize(),
                request.installation()
                        .home());
        assertEquals(
                minecraftHome.toAbsolutePath()
                        .normalize(),
                request.gameDirectory());
        assertEquals(
                minecraftHome.resolve(".custommc")
                        .resolve("native-staging")
                        .toAbsolutePath()
                        .normalize(),
                request.nativeStagingParent());
        assertEquals(
                512,
                request.minimumMemoryMb());
        assertEquals(
                2048,
                request.maximumMemoryMb());
        assertEquals(
                "secret-token",
                request.identity()
                        .accessToken());
        assertEquals(
                "{}",
                request.identity()
                        .userProperties());
        assertEquals(
                "mojang",
                request.identity()
                        .userType());
        assertEquals(
                overlay.resolve("bootstrap.jar")
                        .toAbsolutePath()
                        .normalize(),
                request.runtimeBundle()
                        .bootstrapArtifact());
    }

    @Test
    void explicitOptionalValuesOverrideDefaults()
            throws Exception {
        final Path minecraftHome =
                tempDir.resolve("minecraft");
        final Path game =
                tempDir.resolve("game");
        final Path staging =
                tempDir.resolve("staging");
        final Map<String, String> environment =
                new HashMap<String, String>();
        environment.put(
                "TOKEN_ALT",
                "alt-token");

        final Minecraft189LaunchRequest request =
                Minecraft189LaunchCli.prepare(
                        new String[] {
                                "--minecraft-home",
                                minecraftHome.toString(),
                                "--java-home",
                                javaRuntime().toString(),
                                "--runtime-overlay",
                                tempDir.resolve("overlay").toString(),
                                "--player-name",
                                "Player",
                                "--uuid",
                                "uuid",
                                "--access-token-env",
                                "TOKEN_ALT",
                                "--game-dir",
                                game.toString(),
                                "--native-staging",
                                staging.toString(),
                                "--min-mb",
                                "768",
                                "--max-mb",
                                "4096",
                                "--user-type",
                                "legacy",
                                "--user-properties",
                                "{x:1}"
                        },
                        environment,
                        target());

        assertEquals(
                game.toAbsolutePath().normalize(),
                request.gameDirectory());
        assertEquals(
                staging.toAbsolutePath().normalize(),
                request.nativeStagingParent());
        assertEquals(
                768,
                request.minimumMemoryMb());
        assertEquals(
                4096,
                request.maximumMemoryMb());
        assertEquals(
                "alt-token",
                request.identity()
                        .accessToken());
        assertEquals(
                "legacy",
                request.identity()
                        .userType());
        assertEquals(
                "{x:1}",
                request.identity()
                        .userProperties());
    }

    @Test
    void missingTokenEnvironmentUsesEmptyTokenRatherThanCommandLineSecret()
            throws Exception {
        final Minecraft189LaunchRequest request =
                Minecraft189LaunchCli.prepare(
                        minimumArguments(),
                        Collections.<String, String>emptyMap(),
                        target());

        assertEquals(
                "",
                request.identity()
                        .accessToken());
    }

    @Test
    void invalidOptionsAndMemoryAreRejectedBeforeLaunch()
            throws Exception {
        assertThrows(
                IllegalArgumentException.class,
                () -> Minecraft189LaunchCli.prepare(
                        new String[] {
                                "--wat",
                                "x"
                        },
                        Collections.<String, String>emptyMap(),
                        target()));

        final String[] duplicate =
                minimumArgumentsWith(
                        "--player-name",
                        "Other");
        assertThrows(
                IllegalArgumentException.class,
                () -> Minecraft189LaunchCli.prepare(
                        duplicate,
                        Collections.<String, String>emptyMap(),
                        target()));

        final String[] invalidMemory =
                minimumArgumentsWith(
                        "--min-mb",
                        "0");
        assertThrows(
                IllegalArgumentException.class,
                () -> Minecraft189LaunchCli.prepare(
                        invalidMemory,
                        Collections.<String, String>emptyMap(),
                        target()));

        final String[] invertedMemory =
                minimumArgumentsWith(
                        "--min-mb",
                        "4096",
                        "--max-mb",
                        "1024");
        assertThrows(
                IllegalArgumentException.class,
                () -> Minecraft189LaunchCli.prepare(
                        invertedMemory,
                        Collections.<String, String>emptyMap(),
                        target()));
    }

    @Test
    void unusableJavaHomeIsRejected()
            throws Exception {
        final String[] arguments =
                minimumArguments();
        for (int index = 0;
             index < arguments.length;
             index += 2) {
            if ("--java-home".equals(
                    arguments[index])) {
                arguments[index + 1] =
                        tempDir.resolve("missing-java")
                                .toString();
            }
        }

        final IllegalArgumentException failure =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> Minecraft189LaunchCli.prepare(
                                arguments,
                                Collections.<String, String>emptyMap(),
                                target()));

        assertTrue(
                failure.getMessage()
                        .contains(
                                "Java runtime is not usable"));
    }

    private String[] minimumArguments()
            throws Exception {
        return new String[] {
                "--minecraft-home",
                tempDir.resolve("minecraft").toString(),
                "--java-home",
                javaRuntime().toString(),
                "--runtime-overlay",
                tempDir.resolve("runtime-overlay").toString(),
                "--player-name",
                "Player",
                "--uuid",
                "uuid"
        };
    }

    private String[] minimumArgumentsWith(
            final String... suffix)
            throws Exception {
        final String[] base =
                minimumArguments();
        final String[] combined =
                new String[
                        base.length
                                + suffix.length];
        System.arraycopy(
                base,
                0,
                combined,
                0,
                base.length);
        System.arraycopy(
                suffix,
                0,
                combined,
                base.length,
                suffix.length);
        return combined;
    }

    private Path javaRuntime()
            throws Exception {
        final Path home =
                tempDir.resolve("java");
        Files.createDirectories(
                home.resolve("bin"));
        Files.write(
                home.resolve("release"),
                "JAVA_VERSION=\"1.8.0_412\"\n"
                        .getBytes(
                                StandardCharsets.UTF_8));
        Files.write(
                home.resolve("bin")
                        .resolve("java"),
                new byte[] {0});
        return home;
    }

    private static RuntimeTarget target() {
        return new RuntimeTarget(
                OperatingSystem.LINUX,
                CpuArchitecture.X64,
                "6.0");
    }
}
