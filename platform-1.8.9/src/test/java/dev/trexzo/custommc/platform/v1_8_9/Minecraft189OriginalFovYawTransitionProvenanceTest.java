package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Diagnostic classification only; no actual original-game PASS claims. */
final class Minecraft189OriginalFovYawTransitionProvenanceTest {
    private static String classify(float before, float after, boolean witness) {
        return Minecraft189RuntimeBridge.m284FovYawShiftCause(
                before, after, witness);
    }

    @Test void genuinePriorZeroToMinus180IsLargeNativeRotation() {
        assertEquals("original-native-yaw-changed-120-plus",
                classify(0.0F, -180.0F, true));
    }

    @Test void reverse180ToZeroIsAlsoLargeRotation() {
        assertEquals("original-native-yaw-changed-120-plus",
                classify(-180.0F, 0.0F, true));
    }

    @Test void wrappedYawAcrossPositiveNegative180IsSmallChange() {
        assertEquals("original-native-yaw-changed-under-120",
                classify(179.0F, -179.0F, true));
    }

    @Test void unchangedOriginalYawRemainsStable() {
        assertEquals("original-native-yaw-stable",
                classify(0.0F, 0.0F, true));
        assertEquals("original-native-yaw-stable",
                classify(12.0F, 12.2F, true));
    }

    @Test void smallButNonzeroRotationNeverLooksUnchanged() {
        assertEquals("original-native-yaw-changed-under-120",
                classify(0.0F, 45.0F, true));
    }

    @Test void noEarlierGenuineRearObservationCannotProveDrift() {
        assertEquals("original-native-rear-yaw-provenance-unavailable",
                classify(0.0F, -180.0F, false));
    }

    @Test void unknownOrNonfiniteYawDoesNotInventAngleEvidence() {
        assertEquals("original-native-rear-yaw-provenance-unavailable",
                classify(Float.NaN, -180.0F, true));
        assertEquals("original-native-rear-yaw-provenance-unavailable",
                classify(0.0F, Float.POSITIVE_INFINITY, true));
    }

    @Test void diagnosticResultCannotForgeAnOriginalAcceptanceMarker() {
        final String classification = classify(0.0F, -180.0F, true);
        assertFalse(classification.contains("PASS"));
        assertFalse(classification.contains("CUSTOMMC_OFFICIAL"));
    }
}
