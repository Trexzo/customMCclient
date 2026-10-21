package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Classifies why a genuine two-player source-native B attack MUST fail. */
final class Minecraft189SimultaneousBPriorityDiagnosticsTest {
    private static String diagnosis(boolean aligned, int count,
            int a, int b, boolean aliveA, boolean aliveB,
            double distanceA, double distanceB,
            boolean nearestB, boolean rotationB) {
        return Minecraft189SimultaneousTargetAcceptance.m274PriorityReason(
                aligned,count,a,b,aliveA,aliveB,distanceA,distanceB,
                nearestB,rotationB);
    }
    private static String normal(double a, double b) {
        return diagnosis(true,2,3,4,true,true,a,b,true,true);
    }

    @Test void bothRealAlivePlayersWithBCloserAndSelectedAreObserved() {
        assertEquals("original-both-eligible-B-priority-present-on-diagnostic-read",
                normal(3.25,2.2));
    }

    @Test void invalidOrMisalignedOriginalSourceIsNotEvidence() {
        assertEquals("original-world-snapshots-unavailable-or-misaligned",
                diagnosis(false,2,3,4,true,true,3.25,2.2,true,true));
    }

    @Test void thirdRealRemotePlayerDoesNotAuthorizeTwoTargetPriority() {
        assertEquals("original-remote-player-count-not-two",
                diagnosis(true,3,3,4,true,true,3.25,2.2,true,true));
    }

    @Test void missingOriginalAIdentityIsDistinctFromRange() {
        assertEquals("original-A-UUID-not-loaded",
                diagnosis(true,2,-1,4,false,true,Double.NaN,2.2,true,true));
    }

    @Test void missingOriginalBIdentityIsDistinctFromRange() {
        assertEquals("original-B-UUID-not-loaded",
                diagnosis(true,2,3,-1,true,false,3.25,Double.NaN,false,false));
    }

    @Test void originalADeadVetoIsExplicit() {
        assertEquals("original-A-not-living-and-alive",
                diagnosis(true,2,3,4,false,true,3.25,2.2,true,true));
    }

    @Test void originalBDeadVetoIsExplicit() {
        assertEquals("original-B-not-living-and-alive",
                diagnosis(true,2,3,4,true,false,3.25,2.2,true,true));
    }

    @Test void realAKnockbackOutsideThreePointFiveIsNotBothEligible() {
        assertEquals("original-A-outside-native-reach",normal(3.500001,2.2));
        assertEquals("original-A-outside-native-reach",normal(.99,2.2));
    }

    @Test void originalBTooFarOrTooNearIsNotEligible() {
        assertEquals("original-B-outside-native-reach",normal(3.25,3.500001));
        assertEquals("original-B-outside-native-reach",normal(3.25,.99));
    }

    @Test void actualBMustBeMoreThanPointTwelveCloserThanA() {
        assertEquals("original-B-not-0.12-closer-than-A",normal(2.32,2.2));
        assertEquals("original-B-not-0.12-closer-than-A",normal(2.1,2.2));
    }

    @Test void actualNearestOriginalPlayerMustBeB() {
        assertEquals("original-nearest-target-not-B",
                diagnosis(true,2,3,4,true,true,3.25,2.2,false,true));
    }

    @Test void nativeRotationSelectionMustAgreeWithB() {
        assertEquals("original-selected-rotation-not-B",
                diagnosis(true,2,3,4,true,true,3.25,2.2,true,false));
    }

    @Test void nanDistancesCannotBecomeNativePriorityWitnesses() {
        assertEquals("original-A-outside-native-reach",normal(Double.NaN,2.0));
        assertEquals("original-B-outside-native-reach",normal(3.0,Double.NaN));
    }

    @Test void infiniteDistancesCannotBecomeNativePriorityWitnesses() {
        assertEquals("original-A-outside-native-reach",
                normal(Double.POSITIVE_INFINITY,2.0));
        assertEquals("original-B-outside-native-reach",
                normal(3.0,Double.NEGATIVE_INFINITY));
    }

    @Test void exactlyValidThreePointFiveAndPointTwelveMarginNeedRealSelection() {
        assertEquals("original-both-eligible-B-priority-present-on-diagnostic-read",
                normal(3.5,3.0));
    }

    @Test void aTwoPlayerObservationDoesNotEmitAnOriginalPassMarker() {
        assertFalse(normal(3.5,2.0).contains("PASS"));
        assertFalse(normal(3.5,2.0).contains("CUSTOMMC_OFFICIAL"));
    }
}
