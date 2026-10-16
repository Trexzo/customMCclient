package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Primitive-only non-reuse invariants. These never pretend to construct a
 * Minecraft World, create a native controller click or certify server blocks.
 */
final class Minecraft189ScaffoldConfirmedTargetsTest {
    @Test void thirdPositionCannotReuseFirstReceiptedTarget() {
        Minecraft189ScaffoldConfirmedTargets history =
                new Minecraft189ScaffoldConfirmedTargets();
        assertFalse(history.contains(11, 99, 20));
        assertTrue(history.recordReceipt(11, 99, 20)); // A
        assertTrue(history.recordReceipt(12, 99, 20)); // B
        assertEquals(2, history.count());
        assertTrue(history.contains(11, 99, 20),
                "A-B-A reuse must be rejected even if A was not the last target");
        assertFalse(history.recordReceipt(11, 99, 20));
        assertEquals(2, history.count(),
                "duplicate readback cannot increase confirmed transaction quota");
        assertTrue(history.recordReceipt(13, 99, 20));
        assertEquals(3, history.count());
    }

    @Test void fiveReceiptedTargetsMustRetainEveryPriorHistoricalVeto() {
        final Minecraft189ScaffoldConfirmedTargets history =
                new Minecraft189ScaffoldConfirmedTargets();
        for (int i = 0; i < 5; i++)
            assertTrue(history.recordReceipt(200 + i, 99, -40),
                    "each real controller source target has one confirmed receipt");
        assertEquals(5, history.count());
        for (int i = 0; i < 5; i++) {
            assertTrue(history.contains(200 + i, 99, -40),
                    "even a long-past receipted coordinate must be retained");
            assertFalse(history.recordReceipt(200 + i, 99, -40),
                    "five-step history must veto old A/B/C/D/E reuse");
            assertEquals(5, history.count());
        }
        history.reset();
        assertEquals(0, history.count());
        assertFalse(history.contains(200, 99, -40),
                "only a fresh activation clears the prior World target history");
    }

    @Test void exactThreeDimensionalCoordinateRequiredForDuplicate() {
        Minecraft189ScaffoldConfirmedTargets history =
                new Minecraft189ScaffoldConfirmedTargets();
        assertTrue(history.recordReceipt(5, 99, -1));
        assertFalse(history.contains(5, 100, -1));
        assertFalse(history.contains(5, 99, 0));
        assertFalse(history.contains(-5, 99, -1));
        assertTrue(history.recordReceipt(5, 100, -1));
        assertTrue(history.recordReceipt(5, 99, 0));
        assertEquals(3, history.count());
    }

    @Test void cannotExceedEightVerifiedOriginalTargets() {
        Minecraft189ScaffoldConfirmedTargets history =
                new Minecraft189ScaffoldConfirmedTargets();
        for (int i = 0; i < Minecraft189ScaffoldConfirmedTargets.LIMIT; i++)
            assertTrue(history.recordReceipt(100 + i, 99, -20));
        assertEquals(8, history.count());
        assertFalse(history.recordReceipt(108, 99, -20),
                "ninth controller receipt cannot silently extend a bounded activation");
        for (int i = 0; i < 8; i++)
            assertTrue(history.contains(100 + i, 99, -20));
        assertEquals(8, history.count());
    }

    @Test void originalReceiptsAndNegativeCoordinatesResetOnInvalidate() {
        Minecraft189ScaffoldConfirmedTargets history =
                new Minecraft189ScaffoldConfirmedTargets();
        assertTrue(history.recordReceipt(Integer.MIN_VALUE, 1, Integer.MAX_VALUE));
        assertTrue(history.recordReceipt(0, 0, 0));
        history.reset();
        assertEquals(0, history.count());
        assertFalse(history.contains(Integer.MIN_VALUE, 1, Integer.MAX_VALUE));
        assertFalse(history.contains(0, 0, 0));
        assertTrue(history.recordReceipt(Integer.MIN_VALUE, 1, Integer.MAX_VALUE),
                "a fresh activation can reuse world coordinates after full invalidation");
        assertEquals(1, history.count());
        history.reset();
        history.reset();
        assertEquals(0, history.count());
    }

    @Test void duplicateReceiptDoesNotMutatePreviousHistoryOrEvictAnyTarget() {
        Minecraft189ScaffoldConfirmedTargets history =
                new Minecraft189ScaffoldConfirmedTargets();
        assertTrue(history.recordReceipt(1, 99, 1));
        assertTrue(history.recordReceipt(2, 99, 1));
        assertFalse(history.recordReceipt(1, 99, 1));
        assertTrue(history.contains(2, 99, 1));
        assertTrue(history.recordReceipt(3, 99, 1));
        assertEquals(3, history.count());
    }
}
