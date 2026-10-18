package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Original World readback is diagnostic only; the independent vanilla
 * controller argument, UUID, ray, 3D distance and damage gates remain.
 */
final class Minecraft189M419NativeReadbackStabilityTest {
    @Test void realV176AndV178NativeControllerCrashesAreNotReady() {
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m419StableNativeSourceSample(null,
                        2.3491730716924386, 2.4281797437686015));
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m419StableNativeSourceSample(null,
                        2.3400644431964785, 2.4281797437686015));
    }

    @Test void strictWorldBufferMustHavePresentSelectedLockedB() {
        assertTrue(Minecraft189RejoinTargetAcceptance
                .m419StableNativeSourceSample(null, 1.6D, 2.1D));
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m419StableNativeSourceSample(
                        "survivor-B-not-locked-by-KillAura", 1.6D, 2.1D));
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m419StableNativeSourceSample(
                        "survivor-B-not-source-selected", 1.6D, 2.1D));
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m419StableNativeSourceSample(null, 2.2D, 2.4D));
        assertTrue(Minecraft189RejoinTargetAcceptance
                .m419StableNativeSourceSample(null, 2.0D, 2.4D));
    }

    @Test void unknownSourceAndOutOfReachCanNeverProduceStableEvent() {
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m419StableNativeSourceSample(null, Double.NaN, 2.4D));
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m419StableNativeSourceSample(null, 1.1D, Double.POSITIVE_INFINITY));
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m419StableNativeSourceSample(null, 0.95D, 2.4D));
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m419StableNativeSourceSample(null, 3.26D, 3.50D));
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m419StableNativeSourceSample(null, 1.1D, 3.51D));
    }
}
