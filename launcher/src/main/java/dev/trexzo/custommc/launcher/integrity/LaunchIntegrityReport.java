package dev.trexzo.custommc.launcher.integrity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class LaunchIntegrityReport {
    private final List<ArtifactVerification> verifications;

    public LaunchIntegrityReport(
            final List<ArtifactVerification> verifications) {
        this.verifications = Collections.unmodifiableList(
                new ArrayList<ArtifactVerification>(
                        Objects.requireNonNull(
                                verifications,
                                "verifications")));
    }

    public List<ArtifactVerification> verifications() {
        return verifications;
    }

    public boolean passes() {
        for (ArtifactVerification verification : verifications) {
            if (!verification.passes()) {
                return false;
            }
        }
        return true;
    }

    public boolean allCryptographicallyVerified() {
        if (verifications.isEmpty()) {
            return false;
        }

        for (ArtifactVerification verification : verifications) {
            if (verification.status()
                    != ArtifactVerificationStatus.VERIFIED
                    || verification.artifact().sha1() == null) {
                return false;
            }
        }
        return true;
    }
}
