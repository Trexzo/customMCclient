package dev.trexzo.custommc.launcher.integrity;

import dev.trexzo.custommc.launcher.metadata.ResolvedArtifact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArtifactVerifierTest {
    @TempDir
    Path tempDir;

    @Test
    void verifiesKnownSha1AndSize() throws Exception {
        final Path file = tempDir.resolve("abc.bin");
        Files.write(
                file,
                "abc".getBytes(StandardCharsets.UTF_8));

        final ArtifactVerification result =
                new ArtifactVerifier().verify(
                        new ResolvedArtifact(
                                file,
                                "a9993e364706816aba3e25717850c26c9cd0d89d",
                                Long.valueOf(3L)));

        assertEquals(
                ArtifactVerificationStatus.VERIFIED,
                result.status());
        assertEquals(
                "a9993e364706816aba3e25717850c26c9cd0d89d",
                result.actualSha1());
        assertTrue(result.passes());
    }

    @Test
    void sizeMismatchFailsBeforeHashing() throws Exception {
        final Path file = tempDir.resolve("size.bin");
        Files.write(file, new byte[] {1, 2, 3});

        final ArtifactVerification result =
                new ArtifactVerifier().verify(
                        new ResolvedArtifact(
                                file,
                                null,
                                Long.valueOf(9L)));

        assertEquals(
                ArtifactVerificationStatus.SIZE_MISMATCH,
                result.status());
    }

    @Test
    void shaMismatchIsReported() throws Exception {
        final Path file = tempDir.resolve("hash.bin");
        Files.write(
                file,
                "abc".getBytes(StandardCharsets.UTF_8));

        final ArtifactVerification result =
                new ArtifactVerifier().verify(
                        new ResolvedArtifact(
                                file,
                                "0000000000000000000000000000000000000000",
                                null));

        assertEquals(
                ArtifactVerificationStatus.SHA1_MISMATCH,
                result.status());
    }

    @Test
    void presentArtifactWithoutMetadataIsExplicit()
            throws Exception {
        final Path file = tempDir.resolve("unknown.bin");
        Files.write(file, new byte[] {1});

        final ArtifactVerification result =
                new ArtifactVerifier().verify(
                        new ResolvedArtifact(
                                file,
                                null,
                                null));

        assertEquals(
                ArtifactVerificationStatus.PRESENT_UNVERIFIED,
                result.status());
        assertTrue(result.passes());
    }

    @Test
    void missingArtifactFails() throws Exception {
        final ArtifactVerification result =
                new ArtifactVerifier().verify(
                        new ResolvedArtifact(
                                tempDir.resolve("missing.bin"),
                                null,
                                null));

        assertEquals(
                ArtifactVerificationStatus.MISSING,
                result.status());
    }
}
