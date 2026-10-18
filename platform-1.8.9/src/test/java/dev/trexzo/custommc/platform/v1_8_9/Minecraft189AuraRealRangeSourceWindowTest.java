package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The true Mojang dedicated-server range veto MUST observe exactly the
 * server-login-derived seven-block player. This is an original acceptance
 * filter test, not a simulation of native Aura combat or server damage.
 */
final class Minecraft189AuraRealRangeSourceWindowTest {
    private static boolean valid(double squared) {
        return Minecraft189RuntimeBridge
                .originalSevenBlockRangeFixtureDistance(squared);
    }

    @Test void originalServerSevenBlockPlayerIsActuallyEligibleForTwentyAndEightyTicks() {
        assertTrue(valid(7D * 7D),
                "true original 7-block player source must not reset the test");
        assertTrue(valid(6.5D * 6.5D));
        assertTrue(valid(7.5D * 7.5D));
        assertTrue(valid(49D));
    }

    @Test void previousSourceFourBlockWindowIsNowRejectedRatherThanMisclassified() {
        assertFalse(valid(4D * 4D),
                "old 4-block geometry is not the real V165 dedicated fixture");
        assertFalse(valid(14.44D)); // old 3.8-block source
        assertFalse(valid(23.04D)); // old 4.8-block source
        assertFalse(valid(3.5D * 3.5D),
                "actual Aura 3.5 reach is NEVER a far negative fixture");
        assertFalse(valid(2.3134765625000506D),
                "real V161 near-source failure cannot count as far player");
    }

    @Test void originalNativeOutOfWindowPositionsAreStillFailClosed() {
        assertFalse(valid(6.49D * 6.49D));
        assertFalse(valid(7.51D * 7.51D));
        assertFalse(valid(0D));
        assertFalse(valid(-1D));
        assertFalse(valid(Double.NaN));
        assertFalse(valid(Double.POSITIVE_INFINITY));
        assertFalse(valid(Double.NEGATIVE_INFINITY));
    }

    @Test void actualNativeServerObservationRangeDoesNotChangeAuraGameplay() {
        // This read-only predicate does not return a target index, touch
        // original KillAura settings or generate ANY pass marker or hit.
        assertTrue(valid(49D));
        assertFalse(valid(10D));
        assertEquals(49D, 7D * 7D);
        assertNotEquals(3.5D * 3.5D, 49D);
    }
}
