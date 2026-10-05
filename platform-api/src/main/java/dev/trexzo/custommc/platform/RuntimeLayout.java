package dev.trexzo.custommc.platform;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class RuntimeLayout {
    private final Path root;
    private final Map<String, Path> artifacts;

    public RuntimeLayout(
            final Path root,
            final Map<String, Path> artifacts) {
        this.root = Objects.requireNonNull(root, "root").toAbsolutePath();
        this.artifacts = Collections.unmodifiableMap(
                new LinkedHashMap<String, Path>(
                        Objects.requireNonNull(artifacts, "artifacts")));
    }

    public Path root() {
        return root;
    }

    public Map<String, Path> artifacts() {
        return artifacts;
    }

    public Path requireArtifact(final String id) {
        final Path path = artifacts.get(Objects.requireNonNull(id, "id"));
        if (path == null) {
            throw new IllegalArgumentException("missing runtime artifact: " + id);
        }
        return path;
    }
}
