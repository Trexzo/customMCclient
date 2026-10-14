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
