package dev.trexzo.custommc.launcher.natives;

import dev.trexzo.custommc.launcher.integrity.ArtifactVerifier;
import dev.trexzo.custommc.launcher.metadata.NativeArchive;
import dev.trexzo.custommc.launcher.metadata.ResolvedArtifact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NativeStagerTest {
    @TempDir
    Path tempDir;

    @Test
    void stagesNativeFilesAndHonorsExclusions()
            throws Exception {
        final Path archive = tempDir.resolve("native.jar");
        try (OutputStream output =
                Files.newOutputStream(archive);
             ZipOutputStream zip =
                new ZipOutputStream(output)) {
            writeEntry(
                    zip,
                    "META-INF/MANIFEST.MF",
                    "ignored");
            writeEntry(
                    zip,
                    "native.dll",
                    "dll");
            writeEntry(
                    zip,
                    "nested/libnative.so",
                    "so");
        }

        final NativeArchive nativeArchive =
                new NativeArchive(
                        new ResolvedArtifact(
                                archive,
                                null,
                                null),
                        Collections.singletonList(
                                "META-INF/"));

        final Path staging =
                new NativeStager(
                        new ArtifactVerifier())
                        .stage(
                                Collections.singletonList(
                                        nativeArchive),
                                tempDir.resolve("staging"));

        assertTrue(
                Files.isRegularFile(
                        staging.resolve("native.dll")));
        assertTrue(
                Files.isRegularFile(
                        staging.resolve(
                                "nested/libnative.so")));
        assertFalse(
                Files.exists(
                        staging.resolve(
                                "META-INF/MANIFEST.MF")));
    }

    @Test
    void rejectsZipSlipAndCleansFailedStage()
            throws Exception {
        final Path archive = tempDir.resolve("evil.jar");
        try (OutputStream output =
                Files.newOutputStream(archive);
             ZipOutputStream zip =
                new ZipOutputStream(output)) {
            writeEntry(
                    zip,
                    "../escape.dll",
                    "bad");
        }

        final Path parent = tempDir.resolve("staging");
        final NativeArchive nativeArchive =
                new NativeArchive(
                        new ResolvedArtifact(
                                archive,
                                null,
                                null),
                        Collections.<String>emptyList());

        assertThrows(
                NativeStagingException.class,
                () -> new NativeStager(
                        new ArtifactVerifier())
                        .stage(
                                Collections.singletonList(
                                        nativeArchive),
                                parent));

        assertFalse(
                Files.exists(
                        parent.resolve("escape.dll")));
    }

    private static void writeEntry(
            final ZipOutputStream zip,
            final String name,
            final String contents)
            throws Exception {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(
                contents.getBytes(
                        StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
