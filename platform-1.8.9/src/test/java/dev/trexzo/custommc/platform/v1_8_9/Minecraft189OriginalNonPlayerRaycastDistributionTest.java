package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Native-source diagnostic aggregation only, not original GUI/server proof. */
final class Minecraft189OriginalNonPlayerRaycastDistributionTest {
    private static final String ACCEPT = "original-living-nonplayer-ray-accepted";

    private static void accepted(final Minecraft189OriginalNonPlayerRaycastTrace t) {
        t.observe(ACCEPT, 1, Minecraft189WorldEntityKindState.LIVING,
                2, 2, true);
    }

    private static void reject(final Minecraft189OriginalNonPlayerRaycastTrace t,
            final String reason) {
        t.observe(reason, -1, -1, -1, 2, false);
    }

    @Test void missingOriginalRayAccumulatesRatherThanBeingHiddenByLastDecision() {
        final Minecraft189OriginalNonPlayerRaycastTrace t =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        reject(t, "original-crosshair-no-ray");
        accepted(t);
        reject(t, "original-crosshair-no-ray");
        final Minecraft189OriginalNonPlayerRaycastTrace.Snapshot s = t.snapshot();
        assertEquals(3, s.calls);
        assertEquals(1, s.validCallbacks);
        assertEquals(2, s.missRayCount);
        assertEquals("original-crosshair-no-ray", s.decision);
    }

    @Test void nonEntityRayTypeNeverIncrementsRealMobAcceptCount() {
        final Minecraft189OriginalNonPlayerRaycastTrace t =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        reject(t, "original-crosshair-not-entity-hit");
        final Minecraft189OriginalNonPlayerRaycastTrace.Snapshot s = t.snapshot();
        assertEquals(1, s.wrongRayTypeCount);
        assertEquals(0, s.validCallbacks);
    }

    @Test void oldWorldAndMissingLoadedIdentityAreSeparateReasons() {
        final Minecraft189OriginalNonPlayerRaycastTrace t =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        reject(t, "original-world-identity-changed");
        reject(t, "original-ray-not-in-loaded-entities");
        final Minecraft189OriginalNonPlayerRaycastTrace.Snapshot s = t.snapshot();
        assertEquals(1, s.worldMismatchCount);
        assertEquals(1, s.identityMissingCount);
    }

    @Test void mismatchedKindArrayAndInvalidEntityKindStayDifferent() {
        final Minecraft189OriginalNonPlayerRaycastTrace t =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        reject(t, "original-ray-index-exceeds-kinds");
        reject(t, "original-kind-not-living-nonplayer");
        final Minecraft189OriginalNonPlayerRaycastTrace.Snapshot s = t.snapshot();
        assertEquals(1, s.kindIndexMismatchCount);
        assertEquals(1, s.invalidKindCount);
    }

    @Test void absentNativeWorldAndKindInputUseSourceUnavailableCategory() {
        final Minecraft189OriginalNonPlayerRaycastTrace t =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        reject(t, "original-world-source-not-observed");
        reject(t, "native-input-or-kind-source-unavailable");
        assertEquals(2, t.snapshot().unavailableSourceCount);
    }

    @Test void reflectionFailureAndOtherOriginalRejectionsRemainDistinct() {
        final Minecraft189OriginalNonPlayerRaycastTrace t =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        reject(t, "original-ray-reflection-unavailable");
        reject(t, "original-ray-is-local-player");
        assertEquals(1, t.snapshot().reflectionUnavailableCount);
        assertEquals(1, t.snapshot().otherRejections);
    }

    @Test void rawNativeRayStreakTracksConsecutiveActualAcceptedEvaluations() {
        final Minecraft189OriginalNonPlayerRaycastTrace t =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        accepted(t);
        accepted(t);
        accepted(t);
        Minecraft189OriginalNonPlayerRaycastTrace.Snapshot s = t.snapshot();
        assertEquals(3, s.currentAcceptedRayStreak);
        assertEquals(3, s.maximumAcceptedRayStreak);
        assertEquals(3, s.validCallbacks);
        reject(t, "original-crosshair-no-ray");
        accepted(t);
        s = t.snapshot();
        assertEquals(1, s.currentAcceptedRayStreak);
        assertEquals(3, s.maximumAcceptedRayStreak);
        assertEquals(4, s.validCallbacks);
    }

    @Test void acceptedStreakIsRawNotAnIdentityOrTwentyTickCertification() {
        final Minecraft189OriginalNonPlayerRaycastTrace t =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        // The original bridge, NOT this diagnostic, still enforces the
        // same loaded entity identity and strict consecutive 20 + 80.
        for (int i = 0; i < 25; i++) {
            t.observe(ACCEPT, i % 2, Minecraft189WorldEntityKindState.LIVING,
                    2, 2, true);
        }
        assertEquals(25, t.snapshot().maximumAcceptedRayStreak);
        assertFalse(t.snapshot().toString().contains("PASS=YES"));
    }

    @Test void everyRejectedObservationIsAccountedForByExactlyOneCategory() {
        final Minecraft189OriginalNonPlayerRaycastTrace t =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        final String[] reasons = {
            "original-crosshair-no-ray",
            "original-crosshair-not-entity-hit",
            "original-world-identity-changed",
            "original-ray-not-in-loaded-entities",
            "original-ray-index-exceeds-kinds",
            "original-kind-not-living-nonplayer",
            "original-ray-reflection-unavailable",
            "native-input-or-kind-source-unavailable",
            "original-ray-is-local-player",
            "original-world-source-not-observed",
            "original-ray-entity-null"
        };
        for (final String reason : reasons) reject(t, reason);
        accepted(t);
        final Minecraft189OriginalNonPlayerRaycastTrace.Snapshot s = t.snapshot();
        final int rejected = s.missRayCount + s.wrongRayTypeCount
                + s.worldMismatchCount + s.identityMissingCount
                + s.kindIndexMismatchCount + s.invalidKindCount
                + s.reflectionUnavailableCount + s.unavailableSourceCount
                + s.otherRejections;
        assertEquals(reasons.length, rejected);
        assertEquals(s.calls, rejected + s.validCallbacks);
    }

    @Test void resetClearsPriorOriginalWindowAndAllDiagnosticHighWaterMarks() {
        final Minecraft189OriginalNonPlayerRaycastTrace t =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        for (int i = 0; i < 4; i++) accepted(t);
        reject(t, "original-crosshair-no-ray");
        t.reset();
        final Minecraft189OriginalNonPlayerRaycastTrace.Snapshot s = t.snapshot();
        assertEquals(0, s.calls);
        assertEquals(0, s.validCallbacks);
        assertEquals(0, s.currentAcceptedRayStreak);
        assertEquals(0, s.maximumAcceptedRayStreak);
        assertEquals(0, s.missRayCount);
        assertEquals(0, s.otherRejections);
        assertEquals("not-observed", s.decision);
    }

    @Test void mutableOriginalSourceValuesDoNotChangeRecordedSnapshot() {
        final Minecraft189OriginalNonPlayerRaycastTrace t =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        accepted(t);
        final Minecraft189OriginalNonPlayerRaycastTrace.Snapshot old = t.snapshot();
        reject(t, "original-crosshair-no-ray");
        assertEquals(1, old.currentAcceptedRayStreak);
        assertEquals(0, old.missRayCount);
        assertEquals(0, t.snapshot().currentAcceptedRayStreak);
        assertEquals(1, t.snapshot().missRayCount);
    }
}
