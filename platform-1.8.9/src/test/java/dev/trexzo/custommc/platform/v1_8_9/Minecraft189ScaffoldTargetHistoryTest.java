package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure primitive-only target transaction regression. No fabricated
 * WorldClient, original controller calls or server-place PASS markers.
 */
final class Minecraft189ScaffoldTargetHistoryTest {
    @Test void nonConsecutiveReplayIsAlwaysRejected() {
        Minecraft189ScaffoldTargetHistory p = new Minecraft189ScaffoldTargetHistory();
        assertFalse(p.contains(1, 99, 0));
        assertTrue(p.recordConfirmed(1, 99, 0));
        assertTrue(p.recordConfirmed(2, 99, 0));
        assertEquals(2, p.size());
        assertTrue(p.contains(1, 99, 0));
        assertFalse(p.recordConfirmed(1, 99, 0),
                "A->B->A cannot count third click as a new placement");
        assertEquals(2, p.size());
        assertTrue(p.recordConfirmed(3, 99, 0),
                "unique C remains eligible after the rejected duplicate");
        assertEquals(3, p.size());
    }

    @Test void supportsExactlyEightDistinctConfirmedTargetsThenStops() {
        Minecraft189ScaffoldTargetHistory p = new Minecraft189ScaffoldTargetHistory();
        for (int x=1; x<=8; x++) {
            assertTrue(p.recordConfirmed(x, 99, 0));
            assertEquals(x, p.size());
        }
        assertFalse(p.recordConfirmed(9, 99, 0), "capacity must fail shut");
        assertFalse(p.recordConfirmed(3, 99, 0));
        assertEquals(8, p.size());
        for (int x=1; x<=8; x++) assertTrue(p.contains(x, 99, 0));
        assertFalse(p.contains(9, 99, 0));
    }

    @Test void coordinateTriplesDoNotAliasOnAxisOrSign() {
        Minecraft189ScaffoldTargetHistory p = new Minecraft189ScaffoldTargetHistory();
        assertTrue(p.recordConfirmed(-20, 64, 0));
        assertFalse(p.contains(20, 64, 0));
        assertFalse(p.contains(-20, 65, 0));
        assertFalse(p.contains(-20, 64, -1));
        assertTrue(p.recordConfirmed(20, 64, 0));
        assertTrue(p.recordConfirmed(-20, 65, 0));
        assertTrue(p.recordConfirmed(-20, 64, -1));
        assertEquals(4,p.size());
    }

    @Test void revokingActivationDropsAllPriorCoordinates() {
        Minecraft189ScaffoldTargetHistory p = new Minecraft189ScaffoldTargetHistory();
        assertTrue(p.recordConfirmed(1, 99, 0));
        assertTrue(p.recordConfirmed(2, 99, 0));
        p.clear();
        assertEquals(0, p.size());
        assertFalse(p.contains(1, 99, 0));
        assertTrue(p.recordConfirmed(1, 99, 0),
                "fresh explicit module enable creates an independent lease");
        p.clear();
        p.clear();
        assertEquals(0, p.size());
    }
}
