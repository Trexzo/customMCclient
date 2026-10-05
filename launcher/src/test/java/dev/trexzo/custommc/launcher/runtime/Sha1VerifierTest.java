package dev.trexzo.custommc.launcher.runtime;

import dev.trexzo.custommc.platform.RuntimeArtifact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Sha1VerifierTest {
    @TempDir
    Path temp;

    @Test
    void verifiesSizeAndSha1() throws Exception {
        final byte[] bytes =
                "customMCclient".getBytes(StandardCharsets.UTF_8);
        final Path file = temp.resolve("artifact.bin");
        Files.write(file, bytes);

        final RuntimeArtifact expected = new RuntimeArtifact(
                "artifact",
                "a761b6efb7629e00f8265033036f974fc1a0e646",
                bytes.length);

        final Sha1Verifier verifier = new Sha1Verifier();
        assertTrue(verifier.matches(file, expected));

        final RuntimeArtifact wrongSize = new RuntimeArtifact(
                "artifact",
                expected.sha1(),
                bytes.length + 1L);
        assertFalse(verifier.matches(file, wrongSize));
    }
}
