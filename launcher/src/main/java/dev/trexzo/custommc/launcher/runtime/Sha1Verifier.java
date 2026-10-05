package dev.trexzo.custommc.launcher.runtime;

import dev.trexzo.custommc.platform.RuntimeArtifact;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Objects;

public final class Sha1Verifier {
    public boolean matches(
            final Path path,
            final RuntimeArtifact artifact) throws IOException {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(artifact, "artifact");

        if (!Files.isRegularFile(path)
                || Files.size(path) != artifact.size()) {
            return false;
        }

        return digest(path).equals(artifact.sha1());
    }

    private static String digest(final Path path) throws IOException {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-1 unavailable", impossible);
        }

        final byte[] buffer = new byte[8192];
        try (InputStream input = Files.newInputStream(path)) {
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }

        final StringBuilder out = new StringBuilder(40);
        for (byte value : digest.digest()) {
            out.append(String.format(
                    Locale.ROOT,
                    "%02x",
                    Integer.valueOf(value & 0xff)));
        }
        return out.toString();
    }
}
