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
    void runtimeOverlayPrependsClasspathAndOverridesEntrypoint()
            throws Exception {
        final Path runtimeOne =
                tempDir.resolve("custom-runtime.jar");
        final Path runtimeTwo =
                tempDir.resolve("platform-runtime.jar");
        final Path client =
                tempDir.resolve("client.jar");

        final MinecraftLaunchTemplate template =
                new MinecraftLaunchTemplate(
                        "1.8.9",
                        "net.minecraft.client.main.Main",
                        "--username ${auth_player_name} "
                                + "--accessToken ${auth_access_token}",
                        "1.8",
                        Collections.singletonList(
                                new ResolvedArtifact(
                                        client,
                                        null,
                                        null)),
                        Collections.emptyList());

        final LaunchRuntimeOverlay overlay =
                new LaunchRuntimeOverlay(
                        Arrays.asList(
                                runtimeOne,
                                runtimeTwo),
                        "dev.trexzo.custommc.bootstrap.Main",
                        Arrays.asList(
                                "--target-main",
                                template.mainClass()));

        final LaunchRequest request =
                new LaunchRequest(
                        tempDir.resolve("java"),
                        tempDir.resolve("game"),
                        tempDir.resolve("assets"),
                        tempDir.resolve("natives"),
                        template,
                        new LaunchIdentity(
                                "Player",
                                "uuid",
                                "secret",
                                "{}",
                                "mojang"),
                        OperatingSystem.WINDOWS,
                        512,
                        1024,
                        overlay);

        final LaunchCommand command =
                new JvmLaunchCommandBuilder()
                        .build(request);

        final int classpathIndex =
                command.arguments()
                        .indexOf("-cp")
                        + 1;
        assertEquals(
                runtimeOne.toAbsolutePath().normalize()
                        + ";"
                        + runtimeTwo.toAbsolutePath().normalize()
                        + ";"
                        + client.toAbsolutePath().normalize(),
                command.arguments()
                        .get(classpathIndex));

        final int mainIndex =
                classpathIndex + 1;
        assertEquals(
                "dev.trexzo.custommc.bootstrap.Main",
                command.arguments()
                        .get(mainIndex));
        assertEquals(
                "--target-main",
                command.arguments()
                        .get(mainIndex + 1));
        assertEquals(
                template.mainClass(),
                command.arguments()
                        .get(mainIndex + 2));
        assertTrue(
                command.arguments()
                        .contains("secret"));
        assertFalse(
                command.redactedArguments()
                        .contains("secret"));
        assertTrue(
                command.redactedArguments()
                        .contains("<redacted>"));
    }

    @Test
    void runtimeOverlayIsImmutableAndRejectsAmbiguousInputs() {
        final Path runtime =
                tempDir.resolve("runtime.jar");
        final java.util.List<Path> classpath =
                new java.util.ArrayList<Path>();
        classpath.add(runtime);
        final java.util.List<String> arguments =
                new java.util.ArrayList<String>();
        arguments.add("--target-main");

        final LaunchRuntimeOverlay overlay =
                new LaunchRuntimeOverlay(
                        classpath,
                        " bootstrap.Main ",
                        arguments);

        classpath.clear();
        arguments.clear();

        assertEquals(
                Collections.singletonList(
                        runtime.toAbsolutePath().normalize()),
                overlay.classpathPrefix());
        assertEquals(
                "bootstrap.Main",
                overlay.mainClass("fallback.Main"));
        assertEquals(
                Collections.singletonList("--target-main"),
                overlay.mainArgumentsPrefix());
        assertFalse(overlay.empty());
        assertTrue(
                LaunchRuntimeOverlay.none()
                        .empty());
        assertEquals(
                "fallback.Main",
                LaunchRuntimeOverlay.none()
                        .mainClass("fallback.Main"));

        assertThrows(
                IllegalArgumentException.class,
                () -> new LaunchRuntimeOverlay(
                        Arrays.asList(
                                runtime,
                                runtime),
                        null,
                        Collections.emptyList()));
        assertThrows(
                IllegalArgumentException.class,
                () -> new LaunchRuntimeOverlay(
                        Collections.emptyList(),
                        " ",
                        Collections.emptyList()));
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
