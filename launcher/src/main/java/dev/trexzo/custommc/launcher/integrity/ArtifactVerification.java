package dev.trexzo.custommc.launcher.integrity;

import dev.trexzo.custommc.launcher.metadata.ResolvedArtifact;

import java.util.Objects;

public final class ArtifactVerification {
    private final ResolvedArtifact artifact;
    private final ArtifactVerificationStatus status;
    private final Long actualSize;
    private final String actualSha1;

    public ArtifactVerification(
            final ResolvedArtifact artifact,
            final ArtifactVerificationStatus status,
            final Long actualSize,
            final String actualSha1) {
        this.artifact = Objects.requireNonNull(
                artifact,
                "artifact");
        this.status = Objects.requireNonNull(
                status,
                "status");
        this.actualSize = actualSize;
        this.actualSha1 = actualSha1;
    }

    public ResolvedArtifact artifact() {
        return artifact;
    }

    public ArtifactVerificationStatus status() {
        return status;
    }

    public Long actualSize() {
        return actualSize;
    }

    public String actualSha1() {
        return actualSha1;
    }

    public boolean passes() {
        return status.passes();
    }
}
