package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Actual original Minecraft rejoin phase 3 may show A again before B
 * completes its mandatory 20 consecutive source-native locked snapshots.
 * Reject an A-owned synthetic action, never invent a B controller attack.
 */
final class Minecraft189RejoinPhaseThreeOwnerVetoTest {
    private static boolean deny(final int phase, final String reason,
            final boolean rayHitsB, final boolean aAndBLoaded) {
        return Minecraft189RejoinTargetAcceptance
                .m419MustVetoPendingSyntheticClick(
                        true, phase, reason, rayHitsB, aAndBLoaded);
    }

    @Test
    void wrongAOwnerAfterOriginalRejoinIsRejectedBeforeNativeAttack() {
        // Reproduces V350 official 1.8.9 crash: phase=3, A selected and
        // locked, B still live; no source-ray B or B-locked ownership.
        assertTrue(deny(3, "locked-owner-A", false, true));
        assertTrue(deny(3, "selected-owner-A", true, true));
        assertTrue(deny(3, null, false, true));
        // Do not reject actually admitted source B once its native
        // snapshot, selection and lock have all converged.
        assertFalse(deny(3, null, true, true));
    }

    @Test
    void preRejoinBOnlyObservationsAndExistingPhaseFourFenceRemain() {
        // Existing phase-3 B-only survivor clicks are not silently
        // suppressed and cannot be mistaken for post-rejoin evidence.
        assertFalse(deny(3, "A-not-yet-present", false, false));
        assertFalse(deny(3, null, true, false));
        // Phase 4 retains the original always-live source-identity fence.
        assertTrue(deny(4, "wrong-native-uuid", true, false));
        assertTrue(deny(4, null, false, false));
        assertFalse(deny(4, null, true, false));
        assertFalse(deny(2, "wrong-native-uuid", false, true));
        assertFalse(deny(5, "wrong-native-uuid", false, true));
    }

    @Test
    void unrelatedAndLegacyPredicateNeverFakeSuccessOrExpandPhaseThree() {
        // Existing overload used by independent historical M419 tests.
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m419MustVetoPendingSyntheticClick(
                        true, 3, "wrong-owner", false));
        assertTrue(Minecraft189RejoinTargetAcceptance
                .m419MustVetoPendingSyntheticClick(
                        true, 4, "wrong-owner", false));
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m419MustVetoPendingSyntheticClick(
                        false, 4, "wrong-owner", false, true));
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m419MustVetoPendingSyntheticClick(
                        false, 3, "wrong-owner", false, true));
    }
}
