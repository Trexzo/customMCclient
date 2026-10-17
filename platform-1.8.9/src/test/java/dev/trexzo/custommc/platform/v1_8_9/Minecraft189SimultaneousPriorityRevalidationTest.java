package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Exact callback-decision regression for V134's genuine phase-2 UUID crash.
 * Full original Minecraft production owner checks and both live UUIDs are
 * re-read in verifyOwnedClick before this side-effect-free predicate.
 */
final class Minecraft189SimultaneousPriorityRevalidationTest {
    private static final UUID A = UUID.fromString(
            "2bb9b3f7-e881-4d37-9822-b5cfe0300656");
    private static final UUID B = UUID.fromString(
            "c581d44b-7bdf-4af4-adc6-94a620dc17fb");
    private static final UUID FOREIGN = UUID.fromString(
            "4b634c88-f5f0-470f-bbcb-9317c5c74a47");

    private static boolean allow(final int phase, final boolean aHit,
            final UUID clicked, final int clickedIndex,
            final int nativeAIndex, final boolean bothAliveASelected) {
        return Minecraft189SimultaneousTargetAcceptance
                .phaseTwoFirstReturnPermitted(phase, aHit, clicked, A,
                        clickedIndex, nativeAIndex, bothAliveASelected);
    }

    @Test void actualTwoPlayerPriorityBounceMayKeepAWithoutClaimingB() {
        // A was already hit, native world first preferred B. After actual
        // movement/knockback, the genuine nearest/rotation returned to A,
        // while B is still living, loaded, distinct and within 3.5 blocks.
        assertTrue(allow(2, true, A, 7, 7, true));
        // A moves between native indices after a loadedEntityList reorder.
        assertTrue(allow(2, true, A, 2, 2, true));
        // B may be selected next, but only the separate actual native
        // B attack branch may emit the B-owned marker or phase advancement.
        assertFalse(allow(2, true, B, 2, 2, true));
    }

    @Test void nativePriorityWitnessMustBeFreshAndComplete() {
        assertFalse(allow(2, true, A, 7, 7, false),
                "absence of both live eligible players cannot license bounce");
        assertFalse(allow(2, true, A, 7, 6, true),
                "reordered original source index must agree exactly");
        assertFalse(allow(2, true, A, -1, -1, true),
                "an unknown player index is never a target owner");
        assertFalse(allow(2, true, A, 7, -1, true),
                "missing actual native source-A entity must veto");
        assertFalse(allow(2, true, FOREIGN, 7, 7, true),
                "a third or spoofed native UUID cannot acquire either phase");
        assertFalse(allow(2, true, null, 7, 7, true));
    }

    @Test void noPrematurePassBeforeAHitOrOutsideBWaitPhase() {
        assertFalse(allow(0, true, A, 7, 7, true));
        assertFalse(allow(1, true, A, 7, 7, true));
        assertFalse(allow(3, true, A, 7, 7, true),
                "post-B success must rely on previously certified branch");
        assertFalse(allow(2, false, A, 7, 7, true),
                "cannot invent prior server-owned A damage");
    }
}
