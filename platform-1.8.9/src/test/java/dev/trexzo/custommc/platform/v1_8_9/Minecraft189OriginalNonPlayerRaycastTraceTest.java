package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for V288 opt-in native raycast rejection reporting.
 * Synthetic source decisions cannot certify original GUI/server acceptance.
 */
final class Minecraft189OriginalNonPlayerRaycastTraceTest {
    @Test void startsUnavailableWithoutFabricatedOriginalEvidence() {
        final Minecraft189OriginalNonPlayerRaycastTrace trace =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        final Minecraft189OriginalNonPlayerRaycastTrace.Snapshot s =
                trace.snapshot();
        assertEquals(0, s.calls);
        assertEquals(0, s.validCallbacks);
        assertEquals("not-observed", s.decision);
        assertEquals(-1, s.nativeIndex);
        assertEquals(-1, s.nativeEntityCount);
    }

    @Test void genuineCrosshairMissIsNotAnAcceptedMob() {
        final Minecraft189OriginalNonPlayerRaycastTrace trace =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        trace.observe("original-crosshair-no-ray", -1, -1, -1, 3, false);
        final Minecraft189OriginalNonPlayerRaycastTrace.Snapshot s =
                trace.snapshot();
        assertEquals(1, s.calls);
        assertEquals(0, s.validCallbacks);
        assertEquals("original-crosshair-no-ray", s.decision);
        assertEquals(3, s.sourceKindCount);
    }

    @Test void originalNonEntityRayCannotCountAsLivingNonplayer() {
        final Minecraft189OriginalNonPlayerRaycastTrace trace =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        trace.observe("original-crosshair-not-entity-hit",
                -1, -1, -1, 4, false);
        assertEquals(0, trace.snapshot().validCallbacks);
        assertEquals("original-crosshair-not-entity-hit", trace.snapshot().decision);
    }

    @Test void originalWorldIdentityMismatchRemainsRejected() {
        final Minecraft189OriginalNonPlayerRaycastTrace trace =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        trace.observe("original-world-identity-changed",
                -1, -1, -1, 4, false);
        assertEquals(0, trace.snapshot().validCallbacks);
        assertEquals("original-world-identity-changed", trace.snapshot().decision);
    }

    @Test void loadedIndexAndKindArrayMismatchIsVisibleNotAccepted() {
        final Minecraft189OriginalNonPlayerRaycastTrace trace =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        trace.observe("original-ray-index-exceeds-kinds",
                4, -1, 5, 2, false);
        final Minecraft189OriginalNonPlayerRaycastTrace.Snapshot s =
                trace.snapshot();
        assertEquals(0, s.validCallbacks);
        assertEquals(4, s.nativeIndex);
        assertEquals(5, s.nativeEntityCount);
        assertEquals(2, s.sourceKindCount);
    }

    @Test void actualKindBitDecisionIsPreservedInRejectedEvidence() {
        final Minecraft189OriginalNonPlayerRaycastTrace trace =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        final int flags = Minecraft189WorldEntityKindState.PLAYER
                | Minecraft189WorldEntityKindState.LIVING;
        trace.observe("original-kind-not-living-nonplayer",
                1, flags, 3, 3, false);
        assertEquals(flags, trace.snapshot().nativeKindBits);
        assertEquals(0, trace.snapshot().validCallbacks);
    }

    @Test void acceptedNativeNonplayerHasExactActualIndexAndKindBits() {
        final Minecraft189OriginalNonPlayerRaycastTrace trace =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        final int pigBits = Minecraft189WorldEntityKindState.LIVING;
        trace.observe("original-living-nonplayer-ray-accepted",
                1, pigBits, 3, 3, true);
        final Minecraft189OriginalNonPlayerRaycastTrace.Snapshot s =
                trace.snapshot();
        assertEquals(1, s.calls);
        assertEquals(1, s.validCallbacks);
        assertEquals(1, s.nativeIndex);
        assertEquals(pigBits, s.nativeKindBits);
    }

    @Test void laterRejectionCannotBeMistakenForConsecutiveAcceptedTicks() {
        final Minecraft189OriginalNonPlayerRaycastTrace trace =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        trace.observe("original-living-nonplayer-ray-accepted",
                1, Minecraft189WorldEntityKindState.LIVING, 3, 3, true);
        trace.observe("original-crosshair-no-ray", -1, -1, -1, 3, false);
        assertEquals(2, trace.snapshot().calls);
        assertEquals(1, trace.snapshot().validCallbacks);
        assertEquals("original-crosshair-no-ray", trace.snapshot().decision);
    }

    @Test void absentSourceAndReflectionFailureNeverCountAsValid() {
        final Minecraft189OriginalNonPlayerRaycastTrace trace =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        trace.observe("original-world-source-not-observed", -1, -1, -1, -1,
                false);
        trace.observe("original-ray-reflection-unavailable", -1, -1, -1, 3,
                false);
        assertEquals(2, trace.snapshot().calls);
        assertEquals(0, trace.snapshot().validCallbacks);
        assertEquals("original-ray-reflection-unavailable",
                trace.snapshot().decision);
    }

    @Test void invalidDiagnosticDecisionIsRejectedBeforeAnyStateMutation() {
        final Minecraft189OriginalNonPlayerRaycastTrace trace =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        assertThrows(IllegalArgumentException.class,
                () -> trace.observe("", -1, -1, -1, -1, false));
        assertThrows(IllegalArgumentException.class,
                () -> trace.observe(null, -1, -1, -1, -1, false));
        assertEquals(0, trace.snapshot().calls);
    }

    @Test void existingSourceResetRevokesAllPriorNativeDiagnosticState() {
        final Minecraft189OriginalNonPlayerRaycastTrace trace =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        trace.observe("original-living-nonplayer-ray-accepted",
                1, Minecraft189WorldEntityKindState.LIVING, 3, 3, true);
        trace.reset();
        final Minecraft189OriginalNonPlayerRaycastTrace.Snapshot s =
                trace.snapshot();
        assertEquals(0, s.calls);
        assertEquals(0, s.validCallbacks);
        assertEquals(-1, s.nativeIndex);
        assertEquals("not-observed", s.decision);
    }

    @Test void offlineDiagnosticDoesNotPrintOrForgeOriginalPass() {
        final Minecraft189OriginalNonPlayerRaycastTrace trace =
                new Minecraft189OriginalNonPlayerRaycastTrace();
        trace.observe("original-living-nonplayer-ray-accepted",
                1, Minecraft189WorldEntityKindState.LIVING, 3, 3, true);
        assertFalse(trace.snapshot().toString().contains("PASS=YES"));
    }
}
