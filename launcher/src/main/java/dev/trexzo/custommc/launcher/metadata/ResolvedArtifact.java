package dev.trexzo.custommc.launcher.metadata;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

public final class ResolvedArtifact {
    private final Path path;
    private final String sha1;
    private final Long size;

    public ResolvedArtifact(
            final Path path,
            final String sha1,
            final Long size) {
        this.path = Objects.requireNonNull(
                path,
                "path").toAbsolutePath().normalize();

        if (sha1 != null
                && !sha1.matches("[0-9a-fA-F]{40}")) {
            throw new IllegalArgumentException(
                    "invalid SHA-1: " + sha1);
        }
        if (size != null && size.longValue() < 0L) {
            throw new IllegalArgumentException(
                    "artifact size must not be negative");
        }

        this.sha1 = sha1 == null
                ? null
                : sha1.toLowerCase(Locale.ROOT);
        this.size = size;
    }

    public Path path() {
        return path;
    }

    public String sha1() {
        return sha1;
    }

    public Long size() {
        return size;
    }
}
