package dev.trexzo.custommc.launcher.integrity;

public enum ArtifactVerificationStatus {
    VERIFIED(true),
    PRESENT_UNVERIFIED(true),
    MISSING(false),
    SIZE_MISMATCH(false),
    SHA1_MISMATCH(false);

    private final boolean passes;

    ArtifactVerificationStatus(final boolean passes) {
        this.passes = passes;
    }

    public boolean passes() {
        return passes;
    }
}
