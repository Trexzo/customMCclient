package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * V291 pure diagnostic classification of ALREADY-READ original snapshots.
 * The actual acceptance predicate and native UUID ownership are unchanged.
 */
final class Minecraft189LockedTargetInitialSourceDiagnosticTest {
    private static String reason(double a, double b, int ai, int bi,
            boolean found, int selected, boolean aim, int rotation) {
        return Minecraft189LockedTargetAcceptance.m291InitialAReason(
                a, b, ai, bi, found, selected, aim, rotation);
    }

    @Test void realEligibleSourceAndSelectionIsClassifiedWithoutForgedMarker() {
        assertEquals("original-initial-A-witness-eligible",
                reason(2, 3, 4, 5, true, 4, true, 4));
    }

    @Test void missingNativeAIndexIsNotAnEligibleOriginalIdentity() {
        assertEquals("original-UUID-source-index-unavailable",
                reason(2, 3, -1, 5, true, 5, true, 5));
    }

    @Test void duplicateOriginalNativeIndexIsNotTwoRemotePlayers() {
        assertEquals("original-UUID-source-index-unavailable",
                reason(2, 3, 5, 5, true, 5, true, 5));
    }

    @Test void bothActualSourceDistancesMustRemainWithinThreePointTwoFive() {
        assertEquals("original-initial-A-or-B-outside-3D-bounds",
                reason(0.999, 3, 0, 1, true, 0, true, 0));
        assertEquals("original-initial-A-or-B-outside-3D-bounds",
                reason(2, 3.250001, 0, 1, true, 0, true, 0));
    }

    @Test void exactBoundaryAndStrictPositiveGapAreRecognized() {
        assertEquals("original-initial-A-witness-eligible",
                reason(1, 3.25, 0, 1, true, 0, true, 0));
    }

    @Test void exactPointOneTwoGapCannotPassOriginalStrictMargin() {
        assertEquals("original-A-not-nearer-by-source-margin",
                reason(2.0, 2.12, 0, 1, true, 0, true, 0));
    }

    @Test void genuinelyNearerBNotAcceptedAsOriginalA() {
        assertEquals("original-A-not-nearer-by-source-margin",
                reason(3.0, 2.0, 0, 1, true, 0, true, 0));
    }

    @Test void nativeNearSelectionMustNameTheActualUuidAIndex() {
        assertEquals("original-initial-A-not-selected",
                reason(2, 3, 0, 1, true, 1, true, 0));
        assertEquals("original-initial-A-not-selected",
                reason(2, 3, 0, 1, false, -1, true, 0));
    }

    @Test void actualNativeRotationLeaseMustOwnSameAIndex() {
        assertEquals("original-initial-A-not-selected",
                reason(2, 3, 0, 1, true, 0, false, -1));
        assertEquals("original-initial-A-not-selected",
                reason(2, 3, 0, 1, true, 0, true, 1));
    }

    @Test void sourceIndexReorderDoesNotInvalidateRealAUuidSelection() {
        assertEquals("original-initial-A-witness-eligible",
                reason(2, 3, 9, 2, true, 9, true, 9));
    }

    @Test void nonfiniteSourceDistancesCannotInventOriginalRayEvidence() {
        assertEquals("original-distance-not-finite",
                reason(Double.NaN, 3, 0, 1, true, 0, true, 0));
        assertEquals("original-distance-not-finite",
                reason(2, Double.POSITIVE_INFINITY, 0, 1, true, 0, true, 0));
    }

    @Test void diagnosticLabelIsNotOfficialOriginalMojangSuccess() {
        final String status = reason(2, 3, 0, 1, true, 0, true, 0);
        assertFalse(status.contains("CUSTOMMC_OFFICIAL_189"));
        assertFalse(status.contains("PASS=YES"));
    }
}
