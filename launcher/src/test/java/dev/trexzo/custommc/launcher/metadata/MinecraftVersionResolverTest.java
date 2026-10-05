package dev.trexzo.custommc.launcher.metadata;

import dev.trexzo.custommc.launcher.runtime.CpuArchitecture;
import dev.trexzo.custommc.launcher.runtime.MinecraftInstallation;
import dev.trexzo.custommc.launcher.runtime.OperatingSystem;
import dev.trexzo.custommc.launcher.runtime.RuntimeTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class MinecraftVersionResolverTest {
    private static final String CLIENT_SHA =
            "0123456789abcdef0123456789abcdef01234567";
    private static final String COMMON_SHA =
            "1111111111111111111111111111111111111111";
    private static final String NATIVE_SHA =
            "2222222222222222222222222222222222222222";

    @TempDir
    Path tempDir;

    @Test
    void resolvesLegacyClasspathNativesRulesAndIntegrity()
            throws Exception {
        final MinecraftInstallation installation =
                installationWithMetadata(
                        "{"
                        + "\"id\":\"1.8.9\","
                        + "\"mainClass\":\"net.minecraft.client.main.Main\","
                        + "\"minecraftArguments\":\"--username ${auth_player_name}\","
                        + "\"assets\":\"1.8\","
                        + "\"downloads\":{\"client\":{"
                        + "\"sha1\":\"" + CLIENT_SHA + "\","
                        + "\"size\":8461484}},"
                        + "\"libraries\":["
                        + "{\"name\":\"com.example:common:1.0\","
                        + "\"downloads\":{\"artifact\":{"
                        + "\"path\":\"com/example/common/1.0/common-1.0.jar\","
                        + "\"sha1\":\"" + COMMON_SHA + "\","
                        + "\"size\":10}}},"
                        + "{\"name\":\"org.lwjgl.lwjgl:lwjgl-platform:2.9.4\","
                        + "\"rules\":[{\"action\":\"allow\",\"os\":{\"name\":\"windows\"}}],"
                        + "\"natives\":{\"windows\":\"natives-windows-${arch}\"},"
                        + "\"extract\":{\"exclude\":[\"META-INF/\"]},"
                        + "\"downloads\":{\"classifiers\":{"
                        + "\"natives-windows-64\":{"
                        + "\"path\":\"org/lwjgl/lwjgl/lwjgl-platform/2.9.4/lwjgl-platform-2.9.4-natives-windows-64.jar\","
                        + "\"sha1\":\"" + NATIVE_SHA + "\","
                        + "\"size\":20}}}},"
                        + "{\"name\":\"com.example:linux-only:1.0\","
                        + "\"rules\":[{\"action\":\"allow\",\"os\":{\"name\":\"linux\"}}]}"
                        + "]}");

        final MinecraftLaunchTemplate template =
                new MinecraftVersionResolver().resolve(
                        installation,
                        new RuntimeTarget(
                                OperatingSystem.WINDOWS,
                                CpuArchitecture.X64,
                                "10.0"));

        assertEquals("1.8.9", template.versionId());
        assertEquals(
                "net.minecraft.client.main.Main",
                template.mainClass());
        assertEquals("1.8", template.assetIndexId());

        assertEquals(2, template.classpath().size());
        assertEquals(
                COMMON_SHA,
                template.classpath().get(0).sha1());
        assertEquals(
                installation.versionJar()
                        .toAbsolutePath()
                        .normalize(),
                template.classpath().get(1).path());
        assertEquals(
                CLIENT_SHA,
                template.classpath().get(1).sha1());

        assertEquals(1, template.nativeArchives().size());
        assertEquals(
                NATIVE_SHA,
                template.nativeArchives()
                        .get(0)
                        .artifact()
                        .sha1());
        assertEquals(
                java.util.Collections.singletonList(
                        "META-INF/"),
                template.nativeArchives()
                        .get(0)
                        .exclusions());
    }

    @Test
    void laterMatchingRuleOverridesEarlierRule()
            throws Exception {
        final MinecraftInstallation installation =
                installationWithMetadata(
                        "{"
                        + "\"id\":\"1.8.9\","
                        + "\"mainClass\":\"Main\","
                        + "\"minecraftArguments\":\"--demo\","
                        + "\"assets\":\"1.8\","
                        + "\"libraries\":[{"
                        + "\"name\":\"com.example:blocked:1.0\","
                        + "\"rules\":["
                        + "{\"action\":\"allow\"},"
                        + "{\"action\":\"disallow\",\"os\":{\"name\":\"windows\"}}"
                        + "]}]}");

        final MinecraftLaunchTemplate template =
                new MinecraftVersionResolver().resolve(
                        installation,
                        new RuntimeTarget(
                                OperatingSystem.WINDOWS,
                                CpuArchitecture.X64,
                                "10.0"));

        assertEquals(1, template.classpath().size());
    }

    @Test
    void artifactPathCannotEscapeLibrariesRoot()
            throws Exception {
        final MinecraftInstallation installation =
                installationWithMetadata(
                        "{"
                        + "\"id\":\"1.8.9\","
                        + "\"mainClass\":\"Main\","
                        + "\"minecraftArguments\":\"--demo\","
                        + "\"assets\":\"1.8\","
                        + "\"libraries\":[{"
                        + "\"name\":\"com.example:bad:1.0\","
                        + "\"downloads\":{\"artifact\":{"
                        + "\"path\":\"../escape.jar\"}}"
                        + "}]}");

        assertThrows(
                VersionMetadataException.class,
                () -> new MinecraftVersionResolver().resolve(
                        installation,
                        new RuntimeTarget(
                                OperatingSystem.LINUX,
                                CpuArchitecture.X64,
                                "6.0")));
    }

    @Test
    void metadataVersionMustMatchInstallationDirectory()
            throws Exception {
        final MinecraftInstallation installation =
                installationWithMetadata(
                        "{"
                        + "\"id\":\"1.8.8\","
                        + "\"mainClass\":\"Main\","
                        + "\"minecraftArguments\":\"--demo\","
                        + "\"assets\":\"1.8\","
                        + "\"libraries\":[]"
                        + "}");

        assertThrows(
                VersionMetadataException.class,
                () -> new MinecraftVersionResolver().resolve(
                        installation,
                        new RuntimeTarget(
                                OperatingSystem.LINUX,
                                CpuArchitecture.X64,
                                "6.0")));
    }

    private MinecraftInstallation installationWithMetadata(
            final String json)
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
                json.getBytes(StandardCharsets.UTF_8));
        Files.write(
                installation.versionJar(),
                new byte[] {0});

        return installation;
    }
}
