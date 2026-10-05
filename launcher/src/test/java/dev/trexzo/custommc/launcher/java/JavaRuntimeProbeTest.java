package dev.trexzo.custommc.launcher.java;

import dev.trexzo.custommc.launcher.runtime.OperatingSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class JavaRuntimeProbeTest {
    @TempDir
    Path tempDir;

    @Test
    void parsesLegacyJava8VersionWithoutExecution()
            throws Exception {
        final Path home = createRuntime(
                "1.8.0_412",
                OperatingSystem.WINDOWS);

        final JavaRuntimeInspection inspection =
                new JavaRuntimeProbe().inspect(
                        home,
                        OperatingSystem.WINDOWS);

        assertTrue(inspection.usable());
        assertEquals(
                8,
                inspection.runtime().majorVersion());
        assertTrue(
                inspection.runtime()
                        .executable()
                        .endsWith("java.exe"));
    }

    @Test
    void parsesModernJavaVersionWithoutExecution()
            throws Exception {
        final Path home = createRuntime(
                "21.0.12",
                OperatingSystem.LINUX);

        final JavaRuntimeInspection inspection =
                new JavaRuntimeProbe().inspect(
                        home,
                        OperatingSystem.LINUX);

        assertTrue(inspection.usable());
        assertEquals(
                21,
                inspection.runtime().majorVersion());
    }

    @Test
    void invalidVersionIsReported() throws Exception {
        final Path home = createRuntime(
                "not-java",
                OperatingSystem.LINUX);

        final JavaRuntimeInspection inspection =
                new JavaRuntimeProbe().inspect(
                        home,
                        OperatingSystem.LINUX);

        assertFalse(inspection.usable());
        assertEquals(
                java.util.Collections.singletonList(
                        JavaRuntimeProblem.JAVA_VERSION_INVALID),
                inspection.problems());
    }

    private Path createRuntime(
            final String version,
            final OperatingSystem operatingSystem)
            throws Exception {
        final Path home =
                tempDir.resolve(
                        "runtime-"
                                + operatingSystem.name()
                                + "-"
                                + version.replace('.', '_'));
        Files.createDirectories(home.resolve("bin"));
        Files.write(
                home.resolve("release"),
                ("JAVA_VERSION=\"" + version + "\"\n")
                        .getBytes(StandardCharsets.UTF_8));

        final String executable =
                operatingSystem == OperatingSystem.WINDOWS
                        ? "java.exe"
                        : "java";
        Files.write(
                home.resolve("bin").resolve(executable),
                new byte[] {0});
        return home;
    }
}
