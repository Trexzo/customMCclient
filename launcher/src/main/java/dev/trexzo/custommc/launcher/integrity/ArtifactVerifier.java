package dev.trexzo.custommc.launcher.integrity;

import dev.trexzo.custommc.launcher.metadata.MinecraftLaunchTemplate;
import dev.trexzo.custommc.launcher.metadata.NativeArchive;
import dev.trexzo.custommc.launcher.metadata.ResolvedArtifact;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

public final class ArtifactVerifier {
    public ArtifactVerification verify(
            final ResolvedArtifact artifact)
            throws IOException {
        Objects.requireNonNull(artifact, "artifact");
        final Path path = artifact.path();

        if (!Files.isRegularFile(path)) {
            return new ArtifactVerification(
                    artifact,
                    ArtifactVerificationStatus.MISSING,
                    null,
                    null);
        }

        final long actualSize = Files.size(path);
        if (artifact.size() != null
                && artifact.size().longValue() != actualSize) {
            return new ArtifactVerification(
                    artifact,
                    ArtifactVerificationStatus.SIZE_MISMATCH,
                    Long.valueOf(actualSize),
                    null);
        }

        if (artifact.sha1() != null) {
            final String actualSha1 = sha1(path);
            if (!artifact.sha1().equals(actualSha1)) {
                return new ArtifactVerification(
                        artifact,
                        ArtifactVerificationStatus.SHA1_MISMATCH,
                        Long.valueOf(actualSize),
                        actualSha1);
            }

            return new ArtifactVerification(
                    artifact,
                    ArtifactVerificationStatus.VERIFIED,
                    Long.valueOf(actualSize),
                    actualSha1);
        }

        if (artifact.size() != null) {
            return new ArtifactVerification(
                    artifact,
                    ArtifactVerificationStatus.VERIFIED,
                    Long.valueOf(actualSize),
                    null);
        }

        return new ArtifactVerification(
                artifact,
                ArtifactVerificationStatus.PRESENT_UNVERIFIED,
                Long.valueOf(actualSize),
                null);
    }

    public LaunchIntegrityReport verify(
            final MinecraftLaunchTemplate template)
            throws IOException {
        Objects.requireNonNull(template, "template");
        final List<ArtifactVerification> verifications =
                new ArrayList<ArtifactVerification>();

        for (ResolvedArtifact artifact : template.classpath()) {
            verifications.add(verify(artifact));
        }
        for (NativeArchive nativeArchive
                : template.nativeArchives()) {
            verifications.add(
                    verify(nativeArchive.artifact()));
        }

        return new LaunchIntegrityReport(verifications);
    }

    private static String sha1(final Path path)
            throws IOException {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(
                    "SHA-1 is unavailable",
                    impossible);
        }

        final byte[] buffer = new byte[8192];
        try (InputStream input = Files.newInputStream(path)) {
            int read;
            while ((read = input.read(buffer)) >= 0) {
                if (read > 0) {
                    digest.update(buffer, 0, read);
                }
            }
        }

        return HexFormat.of().formatHex(digest.digest());
    }
}
