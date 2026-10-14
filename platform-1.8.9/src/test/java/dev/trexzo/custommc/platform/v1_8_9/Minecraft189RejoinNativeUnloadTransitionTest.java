package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * V40: the original Minecraft client's old A entity may remain in its native
 * list briefly after the genuine server disconnects A, while B has already
 * become the correctly locked and selected target. A B-owned callback during
 * this phase is not a wrong-target attack. This unit test does NOT certify
 * that A has unloaded or replace the 15 original-client A-absent snapshots.
 */
final class Minecraft189RejoinNativeUnloadTransitionTest {
    private static final UUID A =
            UUID.fromString("e3542dd1-56f3-3ab4-82cb-2c015df72d8d");
    private static final UUID B =
            UUID.fromString("9c6b2c0b-a653-3300-9829-2617908fae14");

    private static boolean allow(int phase, boolean aStillLoaded,
            boolean selectedB, boolean bInRange, UUID target, UUID locked) {
        return Minecraft189RejoinTargetAcceptance
                .phaseTwoOwnedSurvivorWhileNativeAStillLoaded(
                        phase, aStillLoaded, selectedB, bInRange,
                        target, locked, B);
    }

    @Test void originalBAlreadyLockedDuringAUnloadDoesNotCrashAcceptance() {
        assertTrue(allow(2, true, true, true, B, B),
                "native A may linger while real selected and locked B is valid");
    }

    @Test void cannotUseTransitionToSkipRealAAbsentFifteenSnapshotGate() {
        assertFalse(allow(1, true, true, true, B, B));
        assertFalse(allow(3, true, true, true, B, B));
        assertFalse(allow(4, true, true, true, B, B));
        assertFalse(allow(2, false, true, true, B, B),
                "real A-absent phase uses the existing strict B-only gate");
    }

    @Test void wrongUuidOrUnlockedSelectionStillFailsClosed() {
        assertFalse(allow(2, true, true, true, A, B));
        assertFalse(allow(2, true, true, true, B, A));
        assertFalse(allow(2, true, true, true, null, B));
        assertFalse(allow(2, true, true, true, B, null));
        assertFalse(allow(2, true, false, true, B, B));
        assertFalse(allow(2, true, true, false, B, B));
    }
}
