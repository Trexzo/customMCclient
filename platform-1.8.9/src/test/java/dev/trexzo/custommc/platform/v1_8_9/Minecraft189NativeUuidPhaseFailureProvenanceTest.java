package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * V280 deterministic diagnostics for the rejecting same-native-source read.
 * These do NOT replace the actual Mojang GUI / server acceptance tests.
 */
final class Minecraft189NativeUuidPhaseFailureProvenanceTest {
    private static final UUID A =
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID B =
            UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID EXTRA =
            UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static String sticky(int phase, UUID clicked, UUID lock,
            boolean available) {
        return Minecraft189LockedTargetAcceptance.m280WrongPhaseDiagnostic(
                phase, clicked, A, B, lock, 8, available ? 7 : -1,
                available ? 8 : -1, available ? 2.8 : Double.NaN,
                available ? 2.1 : Double.NaN,
                available ? 8 : -1, available ? 8 : -1, available);
    }

    private static String simultaneous(int phase, boolean owned, UUID clicked) {
        return Minecraft189SimultaneousTargetAcceptance
                .m280UnexpectedPhaseDiagnostic(phase, owned, clicked,
                        A, B, 8, 10, 10, 10, 8, 8, true, true);
    }

    @Test void stickyPhaseTwoEarlyBIsExplicit() {
        assertTrue(sticky(2, B, B, true)
                .contains("sameReadReason=B-click-before-A-retention"));
    }

    @Test void stickyPhaseOneEarlyBIsExplicit() {
        assertTrue(sticky(1, B, B, true)
                .contains("sameReadReason=B-click-before-A-retention"));
    }

    @Test void stickyPhaseThreeBeforeAHitDoesNotAuthorizeB() {
        assertTrue(sticky(3, B, B, true)
                .contains("sameReadReason=B-click-before-A-owned-attack"));
    }

    @Test void stickyNativeSameReadRetainsLockedSourceAndExactDistances() {
        final String diag = sticky(2, B, B, true);
        assertTrue(diag.contains("sameReadLockedUuid=" + B));
        assertTrue(diag.contains("sameReadExpectedA=" + A));
        assertTrue(diag.contains("sameReadExpectedB=" + B));
        assertTrue(diag.contains("sameReadA3D=2.8"));
        assertTrue(diag.contains("sameReadB3D=2.1"));
        assertTrue(diag.contains("sameReadNearest=8"));
        assertTrue(diag.contains("sameReadRotation=8"));
        assertTrue(diag.contains("sameReadAIndex=7"));
        assertTrue(diag.contains("sameReadBIndex=8"));
    }

    @Test void stickyWrongNativeUuidIsNotSilentlyTreatedAsAOrB() {
        assertTrue(sticky(2, EXTRA, EXTRA, true)
                .contains("sameReadReason=unexpected-clicked-UUID"));
    }

    @Test void stickySourceUnavailableNeverLooksLikeValidEvidence() {
        final String diag = sticky(2, B, null, false);
        assertTrue(diag.contains("sameReadReason=source-unavailable"));
        assertTrue(diag.contains("sameReadAvailable=false"));
        assertTrue(diag.contains("sameReadAIndex=-1"));
        assertTrue(diag.contains("sameReadA3D=NaN"));
    }

    @Test void stickyUnexpectedAOutsideItsPhaseIsDistinguished() {
        assertTrue(sticky(6, A, B, true)
                .contains("sameReadReason=A-click-outside-allowed-phase"));
    }

    @Test void simultaneousPhaseOneBWithoutAOwnedIsRootFailureCandidate() {
        final String diag = simultaneous(1, false, B);
        assertTrue(diag.contains(
                "sameReadReason=B-click-before-first-A-owned-attack"));
        assertTrue(diag.contains("sameReadFirstOwnedClick=false"));
        assertTrue(diag.contains("sameReadClickedUuid=" + B));
    }

    @Test void simultaneousPhaseTwoCannotOmitFirstAOwnership() {
        assertTrue(simultaneous(2, false, B)
                .contains("sameReadReason=phase-two-without-A-owned-attack"));
    }

    @Test void simultaneousUnknownClickedUuidIsExplicit() {
        assertTrue(simultaneous(1, true, EXTRA)
                .contains("sameReadReason=non-A-B-native-UUID"));
    }

    @Test void simultaneousWrongAOrBPhaseStaysDistinct() {
        assertTrue(simultaneous(0, false, A)
                .contains("sameReadReason=A-click-outside-allowed-phase"));
        assertTrue(simultaneous(0, false, B)
                .contains("sameReadReason=B-click-outside-allowed-phase"));
    }

    @Test void simultaneousReadRetainsOriginalSnapshotIndexesAndCounts() {
        final String diag = simultaneous(1, false, B);
        assertTrue(diag.contains("sameReadPhase=1"));
        assertTrue(diag.contains("sameReadClickedIndex=8"));
        assertTrue(diag.contains("sameReadUuidCount=10"));
        assertTrue(diag.contains("sameReadKindCount=10"));
        assertTrue(diag.contains("sameReadCombatCount=10"));
        assertTrue(diag.contains("sameReadNearest=8"));
        assertTrue(diag.contains("sameReadRotation=8"));
        assertTrue(diag.contains("sameReadClickedPlayer=true"));
        assertTrue(diag.contains("sameReadClickedAlive=true"));
    }

    @Test void diagnosticStringsNeverForgeOriginalPassMarkers() {
        assertFalse(sticky(2, B, B, true).contains("_PASS=YES"));
        assertFalse(simultaneous(1, false, B).contains("_PASS=YES"));
        assertFalse(sticky(2, B, B, false).contains("CUSTOMMC_OFFICIAL"));
        assertFalse(simultaneous(2, false, B).contains("CUSTOMMC_OFFICIAL"));
    }
}
