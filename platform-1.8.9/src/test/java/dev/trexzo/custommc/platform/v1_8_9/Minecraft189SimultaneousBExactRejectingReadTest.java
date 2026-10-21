package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Same-source evidence provenance for the FIRST rejecting native B switch.
 * This is diagnostics only; the real B click still throws in production
 * acceptance unless the exact live native two-UUID gate admits it.
 */
final class Minecraft189SimultaneousBExactRejectingReadTest {
    private static String readReason(String cause,int clicked,int originalB) {
        return Minecraft189SimultaneousTargetAcceptance.m276FailureAtOriginalRead(
                cause, clicked, originalB);
    }
    private static final String VALID =
            "original-both-eligible-B-priority-present-on-diagnostic-read";

    @Test void unavailableSameReadReasonIsNeverAValidNativeWitness() {
        assertEquals("original-rejection-diagnostic-missing",
                readReason(null,8,8));
    }

    @Test void emptySameReadReasonIsNeverAValidNativeWitness() {
        assertEquals("original-rejection-diagnostic-missing",
                readReason("",8,8));
    }

    @Test void originalSnapshotMismatchStaysExplicitAndFirst() {
        final String evidence="original-world-snapshots-unavailable-or-misaligned"
                + " uuidCount=2 kindsCount=3";
        assertEquals(evidence,readReason(evidence,8,8));
    }

    @Test void missingOriginalAInTheRejectingNativeWorldIsPreserved() {
        assertEquals("original-A-UUID-not-loaded",
                readReason("original-A-UUID-not-loaded",8,8));
    }

    @Test void originalAWasDeadInTheRejectingWorldDoesNotGetReframed() {
        assertEquals("original-A-not-living-and-alive",
                readReason("original-A-not-living-and-alive",8,8));
    }

    @Test void rejectingSameReadNativeWorldBIsMissing() {
        assertEquals("original-B-UUID-not-loaded",
                readReason("original-B-UUID-not-loaded",8,-1));
    }

    @Test void rejectedNativeAOutOfReachIsNotRetrofittedBySecondRead() {
        final String cause="original-A-outside-native-reach"
                + " originalA3D=3.7 originalB3D=1.5";
        assertEquals(cause,readReason(cause,8,8));
    }

    @Test void rejectedNativeBOutOfReachMustStayRejected() {
        assertEquals("original-B-outside-native-reach",
                readReason("original-B-outside-native-reach",8,8));
    }

    @Test void nativeBIsNotCloseEnoughToA() {
        assertEquals("original-B-not-0.12-closer-than-A",
                readReason("original-B-not-0.12-closer-than-A",8,8));
    }

    @Test void realNearestNativeOwnerMismatchIsReportedFromRejection() {
        assertEquals("original-nearest-target-not-B",
                readReason("original-nearest-target-not-B",8,8));
    }

    @Test void realRotationOwnerMismatchIsReportedFromRejection() {
        assertEquals("original-selected-rotation-not-B",
                readReason("original-selected-rotation-not-B",8,8));
    }

    @Test void extraSourceRemotePlayerIsReportedFromRejectedRead() {
        assertEquals("original-remote-player-count-not-two remoteCount=3",
                readReason("original-remote-player-count-not-two remoteCount=3",8,8));
    }

    @Test void acceptedBOnSourceButWrongOriginalClickIndexStaysHardFailure() {
        final String result=readReason(VALID,8,7);
        assertTrue(result.startsWith("original-B-index-changed-before-click"));
        assertTrue(result.contains("clicked=8"));
        assertTrue(result.contains("originalB=7"));
        assertTrue(result.contains(VALID));
        assertFalse(result.contains("_PASS=YES"));
    }

    @Test void negativeClickIndexCanNeverBeOriginalB() {
        assertTrue(readReason(VALID,-1,7)
                .startsWith("original-B-index-changed-before-click"));
    }

    @Test void equalOriginalIndexDoesNotConvertAlreadyRejectedClickToPass() {
        final String result=readReason(VALID,7,7);
        assertTrue(result.startsWith(
                "original-reject-with-valid-source-needs-investigation"));
        assertFalse(result.contains("_PASS=YES"));
    }

    @Test void rejectedSameReadValuesArePreservedForLaterLogComparison() {
        final String evidence=VALID
                + " originalA3D=2.7 originalB3D=2.1"
                + " originalNearest=6 originalRotation=6 originalBIndex=6";
        final String result=readReason(evidence,6,6);
        assertTrue(result.contains("originalA3D=2.7"));
        assertTrue(result.contains("originalB3D=2.1"));
        assertTrue(result.contains("originalNearest=6"));
        assertTrue(result.contains("originalBIndex=6"));
    }

    @Test void secondReadDiagnosedAsValidCannotRewriteOriginalRejection() {
        final String first="original-A-outside-native-reach originalA3D=4.0";
        assertEquals(first,readReason(first,8,8));
        assertNotEquals(readReason(first,8,8),readReason(VALID,8,8));
    }
}
