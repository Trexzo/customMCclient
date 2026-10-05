package dev.trexzo.custommc.launcher.command;

import dev.trexzo.custommc.launcher.metadata.MinecraftLaunchTemplate;
import dev.trexzo.custommc.launcher.metadata.ResolvedArtifact;
import dev.trexzo.custommc.launcher.runtime.OperatingSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class JvmLaunchCommandBuilderTest {
    @TempDir
    Path tempDir;

    @Test
    void buildsShellFreeCommandAndRedactsAccessToken()
            throws Exception {
        final Path library =
                tempDir.resolve("libs with spaces")
                        .resolve("one.jar");
        final Path client =
                tempDir.resolve("client.jar");

        final List<ResolvedArtifact> classpath =
                Arrays.asList(
                        new ResolvedArtifact(
                                library,
                                null,
                                null),
                        new ResolvedArtifact(
                                client,
                                null,
                                null));

        final MinecraftLaunchTemplate template =
                new MinecraftLaunchTemplate(
                        "1.8.9",
                        "net.minecraft.client.main.Main",
                        "--username ${auth_player_name} "
                                + "--gameDir ${game_directory} "
                                + "--assetsDir ${assets_root} "
                                + "--assetIndex ${assets_index_name} "
                                + "--uuid ${auth_uuid} "
                                + "--accessToken ${auth_access_token} "
                                + "--userProperties ${user_properties} "
                                + "--userType ${user_type}",
                        "1.8",
                        classpath,
                        Collections.emptyList());

        final LaunchRequest request =
                new LaunchRequest(
                        tempDir.resolve("java"),
                        tempDir.resolve("game dir"),
                        tempDir.resolve("assets"),
                        tempDir.resolve("natives"),
                        template,
                        new LaunchIdentity(
                                "Player",
                                "0123456789abcdef0123456789abcdef",
                                "super-secret-token",
                                "{}",
                                "mojang"),
                        OperatingSystem.LINUX,
                        512,
                        2048);

        final LaunchCommand command =
                new JvmLaunchCommandBuilder().build(
                        request);

        assertEquals(
                request.javaExecutable().toString(),
                command.arguments().get(0));
        assertTrue(
                command.arguments().contains(
                        request.gameDirectory().toString()));
        assertTrue(
                command.arguments().contains(
                        "super-secret-token"));
        assertFalse(
                command.redactedArguments().contains(
                        "super-secret-token"));
        assertTrue(
                command.redactedArguments().contains(
                        "<redacted>"));

        final int classpathIndex =
                command.arguments().indexOf("-cp") + 1;
        assertEquals(
                library.toAbsolutePath().normalize()
                        + ":"
                        + client.toAbsolutePath().normalize(),
                command.arguments().get(classpathIndex));
    }

    @Test
    void rejectsInvalidMemoryRange() {
        final MinecraftLaunchTemplate template =
                new MinecraftLaunchTemplate(
                        "1.8.9",
                        "Main",
                        "--demo",
                        "1.8",
                        Collections.singletonList(
                                new ResolvedArtifact(
                                        tempDir.resolve("client.jar"),
                                        null,
                                        null)),
                        Collections.emptyList());

        assertThrows(
                IllegalArgumentException.class,
                () -> new LaunchRequest(
                        tempDir.resolve("java"),
                        tempDir,
                        tempDir,
                        tempDir,
                        template,
                        new LaunchIdentity(
                                "Player",
                                "uuid",
                                "",
                                "{}",
                                "mojang"),
                        OperatingSystem.LINUX,
                        2048,
                        1024));
    }
}
