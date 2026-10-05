package dev.trexzo.custommc.platform;

import java.util.Objects;

public final class RuntimeArtifact {
    private final String id;
    private final String sha1;
    private final long size;

    public RuntimeArtifact(
            final String id,
            final String sha1,
            final long size) {
        this.id = requireText(id, "id");
        this.sha1 = requireSha1(sha1);
        if (size < 0L) {
            throw new IllegalArgumentException("size must be non-negative");
        }
        this.size = size;
    }

    public String id() {
        return id;
    }

    public String sha1() {
        return sha1;
    }

    public long size() {
        return size;
    }

    private static String requireText(
            final String value,
            final String name) {
        Objects.requireNonNull(value, name);
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private static String requireSha1(final String value) {
        requireText(value, "sha1");
        if (!value.matches("[0-9a-fA-F]{40}")) {
            throw new IllegalArgumentException("invalid SHA-1: " + value);
        }
        return value.toLowerCase(java.util.Locale.ROOT);
    }
}
