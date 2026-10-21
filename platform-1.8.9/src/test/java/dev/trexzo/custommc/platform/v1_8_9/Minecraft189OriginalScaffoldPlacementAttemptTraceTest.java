package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * V294 source-only acceptance evidence. Real native Minecraft attempted
 * controller placement is never substituted by these synthetic counters.
 */
final class Minecraft189OriginalScaffoldPlacementAttemptTraceTest {
    @Test void cleanOriginalTraceHasNoFabricatedControllerOrSourceCall() {
        final Minecraft189OriginalScaffoldPlacementAttemptTrace.Snapshot s =
                new Minecraft189OriginalScaffoldPlacementAttemptTrace().snapshot();
        assertEquals(0, s.originalCalls);
        assertEquals(0, s.refused);
        assertEquals(0, s.completedControllerCalls);
        assertEquals(0, s.controllerAccepted);
        assertTrue(s.reasons.isEmpty());
        assertEquals("not-observed", s.lastReason);
    }

    @Test void nativeInterfaceFailureOnlyCountsAsRefusalNotControllerCall() {
        final Minecraft189OriginalScaffoldPlacementAttemptTrace t =
                new Minecraft189OriginalScaffoldPlacementAttemptTrace();
        t.started();
        t.rejected("original-source-interface-or-explicit-owner-missing");
        final Minecraft189OriginalScaffoldPlacementAttemptTrace.Snapshot s = t.snapshot();
        assertEquals(1, s.originalCalls);
        assertEquals(1, s.refused);
        assertEquals(0, s.completedControllerCalls);
        assertEquals(0, s.controllerAccepted);
    }

    @Test void missingPlayerWorldCursorOrItemDoesNotClaimAnyClick() {
        final Minecraft189OriginalScaffoldPlacementAttemptTrace t =
                new Minecraft189OriginalScaffoldPlacementAttemptTrace();
        t.started();
        t.rejected("original-player-world-cursor-or-item-context-unavailable");
        assertEquals(0, t.snapshot().completedControllerCalls);
        assertEquals(1, t.snapshot().reasons.get(
                "original-player-world-cursor-or-item-context-unavailable").intValue());
    }

    @Test void missingVanillaControllerRemainsDistinctFromRayRejection() {
        final Minecraft189OriginalScaffoldPlacementAttemptTrace t =
                new Minecraft189OriginalScaffoldPlacementAttemptTrace();
        t.started();
        t.rejected("original-vanilla-controller-interface-unavailable");
        t.started();
        t.rejected("original-source-cursor-face-ray-or-placement-candidate-rejected");
        assertEquals(1, t.snapshot().reasons.get(
                "original-vanilla-controller-interface-unavailable").intValue());
        assertEquals(1, t.snapshot().reasons.get(
                "original-source-cursor-face-ray-or-placement-candidate-rejected").intValue());
        assertEquals(0, t.snapshot().completedControllerCalls);
    }

    @Test void sourceEyeReachWorldLeaseFailuresRemainSeparate() {
        final Minecraft189OriginalScaffoldPlacementAttemptTrace t =
                new Minecraft189OriginalScaffoldPlacementAttemptTrace();
        t.started();
        t.rejected("original-eye-reach-or-world-lease-unavailable");
        assertEquals(1, t.snapshot().reasons.get(
                "original-eye-reach-or-world-lease-unavailable").intValue());
    }

    @Test void originalInventoryAndSelectedStoneGuardFailuresAreSeparated() {
        final Minecraft189OriginalScaffoldPlacementAttemptTrace t =
                new Minecraft189OriginalScaffoldPlacementAttemptTrace();
        t.started();
        t.rejected("original-selected-inventory-interface-unavailable");
        t.started();
        t.rejected("original-selected-hotbar-block-unavailable");
        assertEquals(2, t.snapshot().refused);
        assertEquals(2, t.snapshot().reasons.size());
        assertEquals(0, t.snapshot().controllerAccepted);
    }

    @Test void sourceGenerationAndHotbarRevalidationHaveDifferentReasons() {
        final Minecraft189OriginalScaffoldPlacementAttemptTrace t =
                new Minecraft189OriginalScaffoldPlacementAttemptTrace();
        t.started();
        t.rejected("original-same-tick-player-world-cursor-controller-or-inventory-changed");
        t.started();
        t.rejected("original-hotbar-slot-stack-or-item-changed");
        assertEquals(1, t.snapshot().reasons.get(
                "original-same-tick-player-world-cursor-controller-or-inventory-changed")
                .intValue());
        assertEquals(1, t.snapshot().reasons.get(
                "original-hotbar-slot-stack-or-item-changed").intValue());
    }

    @Test void originalNativeExceptionRemainsRejectedNotACompletedCall() {
        final Minecraft189OriginalScaffoldPlacementAttemptTrace t =
                new Minecraft189OriginalScaffoldPlacementAttemptTrace();
        t.started();
        t.rejected("original-native-exception-IllegalStateException");
        assertEquals(1, t.snapshot().refused);
        assertEquals(0, t.snapshot().completedControllerCalls);
        assertEquals(0, t.snapshot().controllerAccepted);
    }

    @Test void rejectedNativeControllerCallIsNotAnAcceptedPlacement() {
        final Minecraft189OriginalScaffoldPlacementAttemptTrace t =
                new Minecraft189OriginalScaffoldPlacementAttemptTrace();
        t.started();
        t.completed(false);
        assertEquals(1, t.snapshot().completedControllerCalls);
        assertEquals(0, t.snapshot().controllerAccepted);
        assertEquals(0, t.snapshot().refused);
        assertEquals("original-controller-rejected", t.snapshot().lastReason);
    }

    @Test void acceptedOriginalControllerOnlyRecordsActualCompletedCall() {
        final Minecraft189OriginalScaffoldPlacementAttemptTrace t =
                new Minecraft189OriginalScaffoldPlacementAttemptTrace();
        t.started();
        t.completed(true);
        assertEquals(1, t.snapshot().completedControllerCalls);
        assertEquals(1, t.snapshot().controllerAccepted);
        assertEquals("original-controller-accepted", t.snapshot().lastReason);
        assertFalse(t.snapshot().lastReason.contains("PASS=YES"));
    }

    @Test void histogramCountsEachActualRefusedOriginalCallExactlyOnce() {
        final Minecraft189OriginalScaffoldPlacementAttemptTrace t =
                new Minecraft189OriginalScaffoldPlacementAttemptTrace();
        for (int i = 0; i < 5; i++) {
            t.started();
            t.rejected(i % 2 == 0
                    ? "original-selected-hotbar-block-unavailable"
                    : "original-source-cursor-face-ray-or-placement-candidate-rejected");
        }
        t.started();
        t.completed(false);
        final Minecraft189OriginalScaffoldPlacementAttemptTrace.Snapshot s = t.snapshot();
        int total = 0;
        for (Map.Entry<String,Integer> e : s.reasons.entrySet())
            total += e.getValue();
        assertEquals(6, s.originalCalls);
        assertEquals(5, s.refused);
        assertEquals(5, total);
        assertEquals(1, s.completedControllerCalls);
        assertEquals(0, s.controllerAccepted);
    }

    @Test void snapshotIsImmutableAndCannotAlterOriginalHistory() {
        final Minecraft189OriginalScaffoldPlacementAttemptTrace t =
                new Minecraft189OriginalScaffoldPlacementAttemptTrace();
        t.started();
        t.rejected("original-selected-hotbar-block-unavailable");
        final Minecraft189OriginalScaffoldPlacementAttemptTrace.Snapshot old = t.snapshot();
        assertThrows(UnsupportedOperationException.class,
                () -> old.reasons.put("invented", 100));
        t.started();
        t.completed(true);
        assertEquals(1, old.originalCalls);
        assertEquals(0, old.completedControllerCalls);
        assertEquals(1, old.refused);
        assertEquals(2, t.snapshot().originalCalls);
    }

    @Test void invalidReasonCannotMutateExistingOriginalState() {
        final Minecraft189OriginalScaffoldPlacementAttemptTrace t =
                new Minecraft189OriginalScaffoldPlacementAttemptTrace();
        t.started();
        assertThrows(IllegalArgumentException.class, () -> t.rejected(""));
        assertThrows(IllegalArgumentException.class, () -> t.rejected(null));
        assertEquals(0, t.snapshot().refused);
        assertEquals(1, t.snapshot().originalCalls);
    }

    @Test void newOriginalWorldSessionClearsHistoricalAttemptTrace() {
        final Minecraft189OriginalScaffoldPlacementAttemptTrace t =
                new Minecraft189OriginalScaffoldPlacementAttemptTrace();
        t.started();
        t.rejected("original-selected-hotbar-block-unavailable");
        t.reset();
        assertEquals(0, t.snapshot().originalCalls);
        assertEquals(0, t.snapshot().refused);
        assertTrue(t.snapshot().reasons.isEmpty());
        assertEquals("not-observed", t.snapshot().lastReason);
    }
}
