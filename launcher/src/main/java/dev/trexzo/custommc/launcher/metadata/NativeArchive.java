package dev.trexzo.custommc.launcher.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class NativeArchive {
    private final ResolvedArtifact artifact;
    private final List<String> exclusions;

    public NativeArchive(
            final ResolvedArtifact artifact,
            final List<String> exclusions) {
        this.artifact = Objects.requireNonNull(
                artifact,
                "artifact");
        this.exclusions = Collections.unmodifiableList(
                new ArrayList<String>(
                        Objects.requireNonNull(
                                exclusions,
                                "exclusions")));
    }

    public ResolvedArtifact artifact() {
        return artifact;
    }

    public List<String> exclusions() {
        return exclusions;
    }
}
