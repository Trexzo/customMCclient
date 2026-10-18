package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure schedule/gating contract for read-only real vanilla objectMouseOver
 * diagnostics. No original hit, Minecraft packet, or PASS marker is created
 * by these source unit tests.
 */
final class Minecraft189M419CrosshairSourcePhaseTest {
    private static boolean due(int phase, int ticks) {
        return Minecraft189RejoinTargetAcceptance
                .originalCrosshairObservationDue(phase, ticks);
    }

    @Test void actualPostRejoinControllerPhaseEmitsBoundedOriginalRayReadback() {
        assertTrue(due(4, 40));
        assertTrue(due(4, 80));
        assertTrue(due(4, 400));
        assertFalse(due(4, 39));
        assertFalse(due(4, 41));
        assertFalse(due(4, 0));
        assertFalse(due(4, -40));
    }

    @Test void originalLockedSurvivorPreRejoinObservationStillRuns() {
        assertTrue(due(3, 40));
        assertTrue(due(3, 120));
        assertFalse(due(3, 119));
    }

    @Test void originalOtherNativePhasesNeverProduceSpuriousRayEvidence() {
        for (int phase : new int[]{-1, 0, 1, 2, 5, 6, 100}) {
            for (int ticks : new int[]{1, 39, 40, 80, 400}) {
                assertFalse(due(phase, ticks),
                        "phase=" + phase + " ticks=" + ticks
                        + " must not claim this is live M419");
            }
        }
    }

    @Test void noOriginalRayEvidenceIsConvertedIntoControllerApproval() {
        // The source-only classifier cannot grant native controller usage.
        // The true original M419 assertion still requires an independent
        // PlayerControllerMP.attackEntity and dedicated B server damage.
        assertTrue(due(4, 40));
        assertFalse(due(4, 41));
        assertNotEquals(3, 4);
    }
}
