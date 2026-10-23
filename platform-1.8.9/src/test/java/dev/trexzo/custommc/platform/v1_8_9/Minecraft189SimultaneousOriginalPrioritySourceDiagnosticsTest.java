package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** V310 acceptance-only source witness; never grants attack or a CI PASS. */
final class Minecraft189SimultaneousOriginalPrioritySourceDiagnosticsTest {
    private static String marker(final String callback, final double a,
            final double b) {
        return Minecraft189SimultaneousTargetAcceptance
                .v310OriginalPriorityDiagnostic(callback, 3, 7, a, b);
    }

    @Test void realOriginalNativeBSourceDistancesAndIndicesArePreserved() {
        assertEquals(
                "CUSTOMMC_189_SIMULTANEOUS_B_PRIORITY_NATIVE_SOURCE_DIAG"
                + " path=observe aIndex=3 bIndex=7"
                + " aDistance=3.25 bDistance=2.125 gapBCloser=1.125",
                marker("observe", 3.25D, 2.125D));
    }

    @Test void secondOriginalClickCallbackIsLabeledButDoesNotAuthorizeIt() {
        final String evidence = marker("original-click", 3.25D, 2.125D);
        assertTrue(evidence.contains("path=original-click"));
        assertFalse(evidence.contains("PASS=YES"));
        assertFalse(evidence.contains("_ORIGINAL_CONTROLLER_ATTEMPT"));
        assertFalse(evidence.contains("ATTACK_PASS"));
    }

    @Test void rejectedOrUnknownDistanceStillCannotBePresentedAsPass() {
        for (double[] pair : new double[][] {
                { Double.NaN, 2.0D },
                { 2.0D, Double.POSITIVE_INFINITY },
                { 2.0D, 3.0D },
                { 2.125D, 2.125D }
        }) {
            final String evidence = marker("observe", pair[0], pair[1]);
            assertTrue(evidence.contains("_DIAG"));
            assertFalse(evidence.contains("PASS=YES"));
            assertFalse(evidence.contains("VALID="));
        }
    }

    @Test void cannotConfuseSignedPriorityGapWithAbsoluteDistance() {
        final String positive = marker("observe", 2.81D, 2.5D);
        assertTrue(positive.contains("gapBCloser=0.31000000000000005"));
        final String negative = marker("observe", 2.5D, 2.81D);
        assertTrue(negative.contains("gapBCloser=-0.31000000000000005"));
    }
}
