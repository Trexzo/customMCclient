package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * V285 exercises actual opt-in diagnostic storage, never original GUI/server.
 * Production Aura aim, source owners, and attack admission are unchanged.
 */
final class Minecraft189OriginalFovAuraAimTraceTest {
    private static void record(Minecraft189OriginalFovAuraAimTrace trace,
            float yaw, boolean targetAvailable, int index, float targetYaw,
            boolean returnedTrue) {
        trace.observe(true, yaw, targetAvailable, index, targetYaw, returnedTrue);
    }

    @Test void noSourceObservationHasNoEvidence() {
        final Minecraft189OriginalFovAuraAimTrace trace =
                new Minecraft189OriginalFovAuraAimTrace();
        final Minecraft189OriginalFovAuraAimTrace.Snapshot s = trace.snapshot();
        assertEquals(0, s.callbacks);
        assertEquals(0, s.eligibleCallbacks);
        assertTrue(Float.isNaN(s.lastPreAimYaw));
        assertTrue(Float.isNaN(s.lastEligiblePreAimYaw));
        assertTrue(Float.isNaN(s.lastEligibleSelectedYaw));
        assertEquals(-1, s.lastEligibleSelectedIndex);
    }

    @Test void rejectedRearPlayerCannotBeMisrecordedAsAimAdmission() {
        final Minecraft189OriginalFovAuraAimTrace trace =
                new Minecraft189OriginalFovAuraAimTrace();
        record(trace, 0.0F, false, -1, Float.NaN, false);
        final Minecraft189OriginalFovAuraAimTrace.Snapshot s = trace.snapshot();
        assertEquals(1, s.callbacks);
        assertEquals(0, s.eligibleCallbacks);
        assertEquals(0.0F, s.lastPreAimYaw);
        assertTrue(Float.isNaN(s.lastEligiblePreAimYaw));
    }

    @Test void exactNativeAimAdmissionRetainsPreAimAndSelectedSource() {
        final Minecraft189OriginalFovAuraAimTrace trace =
                new Minecraft189OriginalFovAuraAimTrace();
        record(trace, -170.0F, true, 5, -180.0F, true);
        final Minecraft189OriginalFovAuraAimTrace.Snapshot s = trace.snapshot();
        assertEquals(1, s.callbacks);
        assertEquals(1, s.eligibleCallbacks);
        assertEquals(-170.0F, s.lastEligiblePreAimYaw);
        assertEquals(-180.0F, s.lastEligibleSelectedYaw);
        assertEquals(5, s.lastEligibleSelectedIndex);
    }

    @Test void laterRejectedCallbackDoesNotErasePriorAimParticipation() {
        final Minecraft189OriginalFovAuraAimTrace trace =
                new Minecraft189OriginalFovAuraAimTrace();
        record(trace, -120.0F, true, 1, -180.0F, true);
        record(trace, 0.0F, false, -1, Float.NaN, false);
        final Minecraft189OriginalFovAuraAimTrace.Snapshot s = trace.snapshot();
        assertEquals(2, s.callbacks);
        assertEquals(1, s.eligibleCallbacks);
        assertEquals(0.0F, s.lastPreAimYaw);
        assertEquals(-120.0F, s.lastEligiblePreAimYaw);
        assertEquals(-180.0F, s.lastEligibleSelectedYaw);
        assertEquals(1, s.lastEligibleSelectedIndex);
    }

    @Test void repeatedSelectedCallbacksRetainMostRecentOriginalSource() {
        final Minecraft189OriginalFovAuraAimTrace trace =
                new Minecraft189OriginalFovAuraAimTrace();
        record(trace, -150.0F, true, 4, -180.0F, true);
        record(trace, -160.0F, true, 3, -179.0F, true);
        final Minecraft189OriginalFovAuraAimTrace.Snapshot s = trace.snapshot();
        assertEquals(2, s.callbacks);
        assertEquals(2, s.eligibleCallbacks);
        assertEquals(-160.0F, s.lastEligiblePreAimYaw);
        assertEquals(-179.0F, s.lastEligibleSelectedYaw);
        assertEquals(3, s.lastEligibleSelectedIndex);
    }

    @Test void absentNativeYawIsRecordedAsUnavailableNotInventedZero() {
        final Minecraft189OriginalFovAuraAimTrace trace =
                new Minecraft189OriginalFovAuraAimTrace();
        trace.observe(false, 0.0F, false, -1, Float.NaN, false);
        assertTrue(Float.isNaN(trace.snapshot().lastPreAimYaw));
    }

    @Test void missingSelectedSourceNeverInventsAValidIndex() {
        final Minecraft189OriginalFovAuraAimTrace trace =
                new Minecraft189OriginalFovAuraAimTrace();
        record(trace, -170.0F, false, 42, 0.0F, true);
        final Minecraft189OriginalFovAuraAimTrace.Snapshot s = trace.snapshot();
        assertEquals(1, s.eligibleCallbacks);
        assertEquals(-1, s.lastEligibleSelectedIndex);
        assertTrue(Float.isNaN(s.lastEligibleSelectedYaw));
    }

    @Test void malformedAnglesRemainUnavailableNotFinite() {
        final Minecraft189OriginalFovAuraAimTrace trace =
                new Minecraft189OriginalFovAuraAimTrace();
        trace.observe(true, Float.POSITIVE_INFINITY, true, 3,
                Float.NaN, true);
        final Minecraft189OriginalFovAuraAimTrace.Snapshot s = trace.snapshot();
        assertTrue(Float.isNaN(s.lastPreAimYaw));
        assertTrue(Float.isNaN(s.lastEligiblePreAimYaw));
        assertTrue(Float.isNaN(s.lastEligibleSelectedYaw));
        assertEquals(3, s.lastEligibleSelectedIndex);
    }

    @Test void originalWorldAcceptanceResetRevokesEveryDiagnosticCounter() {
        final Minecraft189OriginalFovAuraAimTrace trace =
                new Minecraft189OriginalFovAuraAimTrace();
        record(trace, -170.0F, true, 5, -180.0F, true);
        trace.reset();
        final Minecraft189OriginalFovAuraAimTrace.Snapshot s = trace.snapshot();
        assertEquals(0, s.callbacks);
        assertEquals(0, s.eligibleCallbacks);
        assertEquals(-1, s.lastEligibleSelectedIndex);
        assertTrue(Float.isNaN(s.lastPreAimYaw));
        assertTrue(Float.isNaN(s.lastEligiblePreAimYaw));
    }

    @Test void traceDataCannotProduceARealMojangPassMarker() {
        final Minecraft189OriginalFovAuraAimTrace trace =
                new Minecraft189OriginalFovAuraAimTrace();
        record(trace, -170.0F, true, 5, -180.0F, true);
        assertFalse(trace.snapshot().toString().contains("PASS=YES"));
    }
}
