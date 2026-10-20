package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189ScaffoldKeyboardWalkDiagnosticsGateTest {
    private static boolean sample(int r, int a, int quota) {
        return Minecraft189ScaffoldKeyboardWalkDiagnosticsGate
                .waitingForOriginalNextController(r, a, quota);
    }
    @Test void secondPlacementOriginalWindowForAllQuotas() {
        for (int q=2; q<=6; q++) assertTrue(sample(1,1,q));
    }
    @Test void fifthPlacementRequiresFourReceiptsAndFiveOrSixQuota() {
        for (int q=2; q<=6; q++) assertEquals(q>=5,sample(4,4,q));
    }
    @Test void sixthPlacementRequiresFiveReceiptsAndSixQuota() {
        for (int q=2; q<=6; q++) assertEquals(q==6,sample(5,5,q));
    }
    @Test void neverObservePreReceipt() {
        for (int q=2; q<=6; q++) assertFalse(sample(0,0,q));
    }
    @Test void neverObserveAfterCompletedQuota() {
        for (int q=2; q<=6; q++) assertFalse(sample(q,q,q));
    }
    @Test void pendingUnreceiptedOriginalControllerNeverObserves() {
        for (int q=2; q<=6; q++) for (int i=0; i<q; i++)
            assertFalse(sample(i,i+1,q));
    }
    @Test void rewoundSourceAttemptNeverObserves() {
        for (int q=2; q<=6; q++) for (int i=1; i<q; i++)
            assertFalse(sample(i,i-1,q));
    }
    @Test void unnecessaryMiddleObservationsAreExcluded() {
        for (int q=2; q<=6; q++) for (int i=2; i<Math.min(q,4); i++)
            assertFalse(sample(i,i,q));
    }
    @Test void invalidOriginalQuotaNeverObserves() {
        for (int q:new int[]{-1,0,1,7,Integer.MAX_VALUE})
            assertFalse(sample(1,1,q));
    }
    @Test void staleOrMismatchedGenuineCountsNeverObserve() {
        assertFalse(sample(-1,-1,6));
        assertFalse(sample(4,3,6));
        assertFalse(sample(4,5,6));
        assertFalse(sample(5,4,6));
        assertFalse(sample(5,6,6));
    }
}
