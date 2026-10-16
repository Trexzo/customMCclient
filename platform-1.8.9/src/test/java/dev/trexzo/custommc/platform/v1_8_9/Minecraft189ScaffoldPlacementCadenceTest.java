package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A pure scheduling-policy unit fixture. No World, cursor, inventory or
 * controller method is faked to claim original-game placement.
 */
final class Minecraft189ScaffoldPlacementCadenceTest {
    @Test void requiresConfirmedReceiptAndSpacingBeforeSecondAttempt() {
        Minecraft189ScaffoldPlacementCadence p=new Minecraft189ScaffoldPlacementCadence();
        assertFalse(p.mayAttempt(42,100));
        assertTrue(p.arm(42,100,2,5));
        assertTrue(p.mayAttempt(42,100));
        assertTrue(p.controllerAttempt(42,100,true));
        assertTrue(p.pending());
        assertEquals(1,p.attemptedCount());
        assertFalse(p.mayAttempt(42,101),
                "one native controller attempt blocks all later attempts pending receipt");
        assertFalse(p.mayAttempt(42,104));
        assertTrue(p.receipt(42,104,true));
        assertFalse(p.pending());
        assertFalse(p.mayAttempt(42,108),
                "receipt confirmation itself starts a minimum pacing interval");
        assertTrue(p.mayAttempt(42,109));
        assertTrue(p.controllerAttempt(42,109,true));
        assertFalse(p.mayAttempt(42,110));
        assertTrue(p.receipt(42,112,true));
        assertTrue(p.stopped(),"two receipts exhaust max two placements");
        assertFalse(p.mayAttempt(42,200));
    }

    @Test void controllerVetoAndReceiptFailureNeverPermitRetries() {
        Minecraft189ScaffoldPlacementCadence a=new Minecraft189ScaffoldPlacementCadence();
        assertTrue(a.arm(1,5,8,3));
        assertFalse(a.controllerAttempt(1,5,false));
        assertTrue(a.stopped());
        assertFalse(a.mayAttempt(1,9));
        Minecraft189ScaffoldPlacementCadence b=new Minecraft189ScaffoldPlacementCadence();
        assertTrue(b.arm(2,10,8,3));
        assertTrue(b.controllerAttempt(2,10,true));
        assertFalse(b.receipt(2,11,false));
        assertTrue(b.stopped());
        assertFalse(b.mayAttempt(2,20));
    }

    @Test void disableStaleActivationBackwardTimeAndTimeoutFailClosed() {
        Minecraft189ScaffoldPlacementCadence p=new Minecraft189ScaffoldPlacementCadence();
        assertTrue(p.arm(7,100,8,2));
        assertFalse(p.mayAttempt(8,100),"wrong activation immediately invalidates policy");
        assertTrue(p.stopped());
        assertTrue(p.arm(8,200,8,2));
        assertTrue(p.controllerAttempt(8,200,true));
        assertFalse(p.mayAttempt(8,321),"unacknowledged receipt timeout must stop");
        assertTrue(p.stopped());
        assertTrue(p.arm(9,500,8,2));
        assertTrue(p.mayAttempt(9,501));
        assertFalse(p.mayAttempt(9,499),"reordered source ticks are not accepted");
        assertTrue(p.stopped());
        assertTrue(p.arm(10,600,8,2));
        p.invalidate();
        assertFalse(p.mayAttempt(10,601),"explicit module disable drops schedule");
    }

    @Test void exhaustedOrRejectedActivationCannotBeRearmedForMoreBlocks() {
        Minecraft189ScaffoldPlacementCadence p=new Minecraft189ScaffoldPlacementCadence();
        assertTrue(p.arm(11,0,1,2));
        assertTrue(p.controllerAttempt(11,0,true));
        assertTrue(p.receipt(11,1,true));
        assertTrue(p.stopped());
        assertFalse(p.arm(11,2,8,2),
                "same enable may not evade its verified one-block quota");
        assertTrue(p.arm(12,3,8,2),
                "only a strictly new module activation can arm a new policy");
        assertFalse(p.controllerAttempt(12,3,false));
        assertFalse(p.arm(12,4,8,2),
                "controller rejection cannot be rearmed on the same activation");
        assertFalse(p.arm(11,5,8,2),
                "older activation remains revoked even after the owner stopped");
        assertTrue(p.arm(13,6,1,2));
    }

    @Test void unsolicitedReceiptFailsShutWithoutControllerAction() {
        Minecraft189ScaffoldPlacementCadence p=new Minecraft189ScaffoldPlacementCadence();
        assertTrue(p.arm(21,10,2,2));
        assertFalse(p.receipt(21,10,true),
                "readback without an original admitted controller action is not authority");
        assertTrue(p.stopped());
        assertFalse(p.arm(21,11,2,2));
        assertTrue(p.arm(22,11,2,2));
    }


    /**
     * V53 groundwork only: three primitive controller acknowledgements and
     * receipts must form THREE sequential transactions, not three clicks
     * credited from a single receipt. This does not simulate native Minecraft
     * World, inventory, movement or dedicated-server placement.
     */
    @Test void threeVerifiedPrimitiveReceiptsNeedThreeSeparatedAdmissions() {
        Minecraft189ScaffoldPlacementCadence p =
                new Minecraft189ScaffoldPlacementCadence();
        assertTrue(p.arm(60,100,3,4));
        assertTrue(p.controllerAttempt(60,100,true));
        assertEquals(1,p.attemptedCount());
        assertFalse(p.mayAttempt(60,101),
                "a pending first readback forbids a second controller call");
        assertTrue(p.receipt(60,102,true));
        assertFalse(p.stopped());
        assertFalse(p.mayAttempt(60,105),
                "the first receipt starts a new four-tick gap");
        assertTrue(p.controllerAttempt(60,106,true));
        assertEquals(2,p.attemptedCount());
        assertFalse(p.mayAttempt(60,107));
        assertTrue(p.receipt(60,108,true));
        assertFalse(p.stopped(),
                "two acknowledged placements cannot exhaust a three-step quota");
        assertFalse(p.mayAttempt(60,111));
        assertTrue(p.controllerAttempt(60,112,true));
        assertEquals(3,p.attemptedCount());
        assertTrue(p.pending());
        assertFalse(p.mayAttempt(60,113),
                "a third controller acceptance is not a third positive receipt");
        assertTrue(p.receipt(60,114,true));
        assertFalse(p.pending());
        assertTrue(p.stopped(),
                "exactly three independent receipts close the activation");
        assertFalse(p.mayAttempt(60,116));
        assertFalse(p.arm(60,117,3,4),
                "same activation cannot restart another three-place batch");
    }

    @Test void secondReceiptTimeoutRevokesThirdStepForSameActivation() {
        Minecraft189ScaffoldPlacementCadence p =
                new Minecraft189ScaffoldPlacementCadence();
        assertTrue(p.arm(61,0,3,2));
        assertTrue(p.controllerAttempt(61,0,true));
        assertTrue(p.receipt(61,1,true));
        assertFalse(p.mayAttempt(61,2));
        assertTrue(p.controllerAttempt(61,3,true));
        assertFalse(p.mayAttempt(61,124),
                "unacknowledged second receipt must prevent third action");
        assertTrue(p.stopped());
        assertFalse(p.arm(61,125,3,2),
                "timeout cannot be repaired by re-arming the same generation");
        assertTrue(p.arm(62,126,3,2),
                "only a fresh explicit module activation may recover");
    }

    /** V59 four distinct original receipts, never four clicks from one ack. */
    @Test void fourPositiveReadbacksAreRequiredToExhaustFourthQuota() {
        Minecraft189ScaffoldPlacementCadence p =
                new Minecraft189ScaffoldPlacementCadence();
        assertTrue(p.arm(71, 100, 4, 4));
        for (int step = 1; step <= 4; step++) {
            final long tick = 100 + (step - 1) * 6L;
            assertTrue(p.controllerAttempt(71, tick, true),
                    "fresh source-native click quota=" + step);
            assertEquals(step, p.attemptedCount());
            assertFalse(p.mayAttempt(71, tick + 1),
                    "cannot double-click while native WorldClient receipt is pending");
            assertTrue(p.receipt(71, tick + 2, true),
                    "each controller admission requires its own positive debit");
            assertEquals(step == 4, p.stopped(),
                    "fourth receipt, not third, must terminate the activation");
        }
        assertFalse(p.arm(71, 130, 4, 4),
                "completed activation may not re-arm a fifth placement");
        assertTrue(p.arm(72, 131, 4, 4),
                "fresh explicitly enabled activation can start again");
    }

    @Test void thirdOriginalReadbackTimeoutRevokesFourthClick() {
        Minecraft189ScaffoldPlacementCadence p =
                new Minecraft189ScaffoldPlacementCadence();
        assertTrue(p.arm(74, 0, 4, 2));
        assertTrue(p.controllerAttempt(74, 0, true));
        assertTrue(p.receipt(74, 1, true));
        assertTrue(p.controllerAttempt(74, 3, true));
        assertTrue(p.receipt(74, 4, true));
        assertTrue(p.controllerAttempt(74, 6, true));
        assertFalse(p.mayAttempt(74, 127), "third receipt deadline=126");
        assertTrue(p.stopped(), "no fourth controller admission after timeout");
        assertEquals(0, p.attemptedCount(),
                "invalidation clears the source's stale transaction counter");
        assertFalse(p.arm(74, 128, 4, 2));
    }

    /** V67 primitive-only cadence: no original World or fake native click. */
    @Test void fiveUniqueReceiptsExhaustExactlyFifthQuotaAndCannotRearm() {
        Minecraft189ScaffoldPlacementCadence p =
                new Minecraft189ScaffoldPlacementCadence();
        assertTrue(p.arm(81, 100, 5, 4));
        for (int step = 1; step <= 5; step++) {
            final long tick = 100 + (step - 1) * 6L;
            assertTrue(p.mayAttempt(81, tick));
            assertTrue(p.controllerAttempt(81, tick, true));
            assertEquals(step, p.attemptedCount());
            assertFalse(p.mayAttempt(81, tick + 1),
                    "no next real click without a separate original receipt");
            assertTrue(p.receipt(81, tick + 2, true));
            assertEquals(step == 5, p.stopped(),
                    "fifth original readback, never fourth, must terminate quota");
        }
        assertFalse(p.mayAttempt(81, 200),
                "sixth click cannot be admitted after five native receipts");
        assertFalse(p.arm(81, 201, 5, 4),
                "same module activation may not rearm a second five-block batch");
        assertTrue(p.arm(82, 202, 5, 4),
                "new explicitly enabled activation can reset cadence safely");
    }

    @Test void fourthReadbackTimeoutPermanentlyVetoesFifthControllerAttempt() {
        Minecraft189ScaffoldPlacementCadence p =
                new Minecraft189ScaffoldPlacementCadence();
        assertTrue(p.arm(83, 0, 5, 2));
        for (int i = 0; i < 3; i++) {
            assertTrue(p.controllerAttempt(83, i * 3, true));
            assertTrue(p.receipt(83, i * 3 + 1, true));
        }
        assertTrue(p.controllerAttempt(83, 9, true));
        assertFalse(p.mayAttempt(83, 130),
                "the fourth pending original receipt times out before a fifth click");
        assertTrue(p.stopped());
        assertFalse(p.arm(83, 131, 5, 2),
                "timed-out activation cannot exploit a fresh quota");
    }

    @Test void rejectsInvalidConfigurationAndTickOverflow() {
        Minecraft189ScaffoldPlacementCadence p=new Minecraft189ScaffoldPlacementCadence();
        assertFalse(p.arm(0,1,2,2));
        assertFalse(p.arm(1,-1,2,2));
        assertFalse(p.arm(1,0,0,2));
        assertFalse(p.arm(1,0,9,2));
        assertFalse(p.arm(1,0,2,1));
        assertFalse(p.arm(1,0,2,101));
        assertFalse(p.arm(1,Long.MAX_VALUE-100,2,2));
        assertTrue(p.stopped());
    }
}
