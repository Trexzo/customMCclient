package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Test-only source geometry; production KillAura's FOV/ray/controller remain. */
final class Minecraft189OriginalFovWorldConvergenceTest {
    @Test void actualV179MojangStaleTargetWorldCannotArmAura() {
        // V179 exact genuine original crash: target remained at stale spawn
        // x -977.5 z -427.5 with host x -976.5 z -424.5, delta
        // (-1,-1,-3), and yaw had rotated to within 0 of stale target.
        assertFalse(Minecraft189RuntimeBridge
                .originalFovNegativeFixtureSourceAligned(-1.0D,-1.0D,-3.0D));
    }

    @Test void actualV175GenuineVerticalDriftCannotArmAura() {
        // Original World observed horizontal 2.0 but 3D 5.852...
        // and thus ~5.5 Y separation, making rear-player test invalid.
        assertFalse(Minecraft189RuntimeBridge
                .originalFovNegativeFixtureSourceAligned(0D,5.50D,2.0D));
    }

    @Test void trueOriginalServerAlignedTwoBlockRearSourcePasses() {
        assertTrue(Minecraft189RuntimeBridge
                .originalFovNegativeFixtureSourceAligned(0D,0D,2.0D));
        assertTrue(Minecraft189RuntimeBridge
                .originalFovNegativeFixtureSourceAligned(0D,0D,-2.0D));
        assertTrue(Minecraft189RuntimeBridge
                .originalFovNegativeFixtureSourceAligned(0.05D,0.15D,-2.05D));
    }

    @Test void incompleteNonfiniteAndDifferentDistancesRemainRejected() {
        for (double d : new double[] {Double.NaN,
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertFalse(Minecraft189RuntimeBridge
                    .originalFovNegativeFixtureSourceAligned(d,0D,2D));
            assertFalse(Minecraft189RuntimeBridge
                    .originalFovNegativeFixtureSourceAligned(0D,d,2D));
            assertFalse(Minecraft189RuntimeBridge
                    .originalFovNegativeFixtureSourceAligned(0D,0D,d));
        }
        assertFalse(Minecraft189RuntimeBridge
                .originalFovNegativeFixtureSourceAligned(0.4D,0D,2D));
        assertFalse(Minecraft189RuntimeBridge
                .originalFovNegativeFixtureSourceAligned(0D,0.5D,2D));
        assertFalse(Minecraft189RuntimeBridge
                .originalFovNegativeFixtureSourceAligned(0D,0D,3D));
    }
}
