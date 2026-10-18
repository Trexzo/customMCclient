package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure predicates mirror the already-mandatory original M419 native source
 * evidence; the test never synthesizes or accepts an attackEntity call.
 * Full pinned Mojang graphical/dedicated-server CI is required separately.
 */
final class Minecraft189M419NativeControllerSourceDiagnosticTest {
    private static String reason(
            boolean hasEvidence, int a, int b, double da, double db,
            boolean selectedB, boolean aUuid, boolean bUuid,
            boolean bLocked, boolean controllerReady) {
        return Minecraft189RejoinTargetAcceptance.m419RejectionReason(
                hasEvidence, a, b, da, db, selectedB,
                aUuid, bUuid, bLocked, controllerReady);
    }

    @Test void genuineInteriorSourceCouldPassAllExistingMandatoryChecks() {
        assertNull(reason(true, 6, 1, 1.5D, Math.sqrt(5.0D),
                true, true, true, true, true));
        assertNull(reason(true, 6, 1, Math.hypot(1.5D, .9D),
                Math.sqrt(5.0D), true, true, true, true, true));
    }

    @Test void originalV159ControllerLowerReachFailureIsPreciselyIdentified() {
        final double originalDa = Math.hypot(.43D, .66D);
        assertTrue(originalDa < 1.0D);
        assertEquals("rejoined-A-outside-original-[1,3.25]-reach",
                reason(true, 8, 3, originalDa, Math.hypot(1.49D, 2.34D),
                        true, true, true, true, true));
    }

    @Test void originalV158VerticalMotionBreaksMandatoryThreeDMargin() {
        final double observedA = Math.sqrt(.06D*.06D + .89D*.89D + 2.04D*2.04D);
        final double observedB = Math.sqrt(1.0D + 2.0D*2.0D);
        assertTrue(observedA + .12D >= observedB);
        assertEquals("rejoined-A-not-0.12-closer-in-3D",
                reason(true, 8, 3, observedA, observedB,
                        true, true, true, true, true));
    }

    @Test void missingOrWrongNativeSnapshotFailsClosed() {
        assertEquals("source-evidence-unavailable",
                reason(false, -2, -2, Double.NaN, Double.NaN,
                        false, false, false, false, false));
        assertEquals("rejoined-A-missing",
                reason(true, -1, 3, 1.5D, 2.2D,
                        true, true, true, true, true));
        assertEquals("survivor-B-missing",
                reason(true, 8, -1, 1.5D, 2.2D,
                        true, true, true, true, true));
        assertEquals("rejoined-A-outside-original-[1,3.25]-reach",
                reason(true, 8, 3, Double.POSITIVE_INFINITY, 2.2D,
                        true, true, true, true, true));
        assertEquals("survivor-B-outside-original-[1,3.5]-reach",
                reason(true, 8, 3, 1.5D, 3.51D,
                        true, true, true, true, true));
    }

    @Test void exactNative3DMarginBoundaryStillFailsClosed() {
        assertEquals("rejoined-A-not-0.12-closer-in-3D",
                reason(true, 8, 3, 2.0D, 2.12D,
                        true, true, true, true, true));
        assertNull(reason(true, 8, 3, 2.0D, 2.13D,
                        true, true, true, true, true));
    }

    @Test void genuineSourceSelectionUuidLockAndControllerAreStillRequired() {
        final double a = 1.5D, b = 2.236D;
        assertEquals("survivor-B-not-source-selected",
                reason(true, 8, 3, a, b,
                        false, true, true, true, true));
        assertEquals("rejoined-A-original-UUID-mismatch",
                reason(true, 8, 3, a, b,
                        true, false, true, true, true));
        assertEquals("survivor-B-original-UUID-mismatch",
                reason(true, 8, 3, a, b,
                        true, true, false, true, true));
        assertEquals("survivor-B-not-locked-by-KillAura",
                reason(true, 8, 3, a, b,
                        true, true, true, false, true));
        assertEquals("original-controller-player-entity-null",
                reason(true, 8, 3, a, b,
                        true, true, true, true, false));
    }
    @Test void originalV196OutOfRangeBIsRejectedBeforeAnySyntheticClick() {
        // Original Mojang V196/V197 source telemetry, not invented range.
        final double a=1.7580159040626773;
        final double b=3.9382332372799493;
        final String reason=reason(true,6,1,a,b,
                false,true,true,false,true);
        assertEquals("survivor-B-outside-original-[1,3.5]-reach",reason);
        assertTrue(Minecraft189RejoinTargetAcceptance
                .m419MustVetoPendingSyntheticClick(true,4,reason,false));
    }

    @Test void genuinePhaseFourOriginalSourceBLockAndRayCanProceedToStrictController() {
        // This predicate NEVER grants a click: strict UUID/source World,
        // real PlayerControllerMP argument and B-only server score still
        // decide the outcome independently.
        assertNull(reason(true,6,1,1.5D,2.236D,
                true,true,true,true,true));
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m419MustVetoPendingSyntheticClick(true,4,null,true));
    }

    @Test void rejoinedAAsOriginalRayTargetMustFailClosedEvenIfOtherSourcesValid() {
        assertTrue(Minecraft189RejoinTargetAcceptance
                .m419MustVetoPendingSyntheticClick(true,4,null,false));
        assertTrue(Minecraft189RejoinTargetAcceptance
                .m419MustVetoPendingSyntheticClick(true,4,
                        "survivor-B-not-source-selected",true));
    }

    @Test void otherOriginalPhasesCannotBeChangedByM419TransientVeto() {
        for(int phase: new int[]{0,1,2,3,5,6}) {
            assertFalse(Minecraft189RejoinTargetAcceptance
                    .m419MustVetoPendingSyntheticClick(true,phase,
                            "rejoined-A-not-0.12-closer-in-3D",false));
        }
    }

    @Test void normalGameplayWithoutOriginalM419AcceptanceUnmodified() {
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m419MustVetoPendingSyntheticClick(false,4,
                        "survivor-B-outside-original-[1,3.5]-reach",false));
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m419MustVetoPendingSyntheticClick(false,4,null,false));
    }

    @Test void strictM419OriginalThreeDimensionalClosenessNotTurnedIntoPass() {
        final String reason=reason(true,6,1,2.468526924409685D,
                2.4281797437686112D,true,true,true,true,true);
        assertEquals("rejoined-A-not-0.12-closer-in-3D",reason);
        assertTrue(Minecraft189RejoinTargetAcceptance
                .m419MustVetoPendingSyntheticClick(true,4,reason,true));
    }

}
