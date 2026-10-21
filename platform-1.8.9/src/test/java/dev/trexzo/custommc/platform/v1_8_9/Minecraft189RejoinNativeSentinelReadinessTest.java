package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Original-only diagnostic taxonomy. No method here authorizes a click,
 * changes player UUID ownership or replaces the four-real-sentinel gate.
 */
final class Minecraft189RejoinNativeSentinelReadinessTest {
    private static String reason(int baseline, int count,
            boolean sourceValid, boolean bLocked) {
        return Minecraft189RejoinTargetAcceptance.m272SentinelReadinessReason(
                baseline, count, sourceValid, bLocked);
    }

    @Test void unknownBaselineIsNotAWorldWitness() {
        assertEquals("native-baseline-unavailable",reason(-1,44,true,true));
    }

    @Test void nullNativeSourceIsNotAnArmorStandProof() {
        assertEquals("original-source-evidence-unavailable",reason(1,-1,false,true));
    }

    @Test void revokedOriginalSurvivorLockMustBeVisibleAsDiagnosis() {
        assertEquals("original-survivor-B-owner-not-locked",reason(4,9,true,false));
    }

    @Test void countDroppingBelowOriginalBaselineIsDistinguished() {
        assertEquals("native-nonplayer-count-decreased",reason(6,5,true,true));
    }

    @Test void noAddedNativeEntityStillWaits() {
        assertEquals("four-native-nonplayers-not-yet-observed",
                reason(20,20,true,true));
    }

    @Test void oneNewNativeEntityNeverCountsAsFour() {
        assertEquals("four-native-nonplayers-not-yet-observed",
                reason(20,21,true,true));
    }

    @Test void twoNewNativeEntitiesNeverCountAsFour() {
        assertEquals("four-native-nonplayers-not-yet-observed",
                reason(20,22,true,true));
    }

    @Test void threeNewNativeEntitiesNeverCountAsFour() {
        assertEquals("four-native-nonplayers-not-yet-observed",
                reason(20,23,true,true));
    }

    @Test void fourNewNativeNonplayersAreDiagnosticOnly() {
        assertEquals("four-native-nonplayers-observed",reason(20,24,true,true));
    }

    @Test void additionalNativeEntitiesDoNotChangeTheStrictFourThreshold() {
        assertEquals("four-native-nonplayers-observed",reason(20,30,true,true));
    }

    @Test void anObservedIncreaseWithUnavailableOriginalSnapshotIsNotAProof() {
        assertEquals("original-source-evidence-unavailable",
                reason(0,4,false,true));
    }

    @Test void realBLockMustStillBeHeldEvenWhenFourEntitiesAppear() {
        assertEquals("original-survivor-B-owner-not-locked",
                reason(10,14,true,false));
    }

    @Test void extremeCountsCannotOverflowOriginalFourThreshold() {
        assertEquals("four-native-nonplayers-not-yet-observed",
                reason(Integer.MAX_VALUE-2,Integer.MAX_VALUE,true,true));
    }

    @Test void rawWorldCountRejectsMissingOriginalUuidAndKinds() {
        assertEquals(-1,Minecraft189RejoinTargetAcceptance
                .m272RawNativeNonplayerCount(null,null));
    }

    @Test void diagnosticClassMustNotHaveAnIndependentNativePassMarker() {
        final String result=reason(0,4,true,true);
        assertNotEquals("CUSTOMMC_189_REJOIN_REAL_SENTINEL_NATIVE_OBSERVED_PASS=YES",
                result);
        assertFalse(result.contains("PASS"));
    }
}
