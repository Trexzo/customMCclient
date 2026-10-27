package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * V346: exact-source Native AutoBlock lease cancellation is bookkeeping,
 * not vanilla stopUsingItem or manual mouse control. Source-shaped Java
 * doubles cannot certify an actual Mojang attack or server-owned damage.
 */
final class Minecraft189AutoBlockPendingLeaseCleanupTest {
    @Test
    void guiVetoCancelsOnlyAbandonedOriginalSwordReservation() {
        final Minecraft189AutoBlockModule block = new Minecraft189AutoBlockModule();
        block.onEnable();
        try {
            final long token = block.beginNativeUse(81L);
            assertNotEquals(0L, token);
            assertTrue(block.cancelPendingNativeUse(token));
            assertFalse(block.cancelPendingNativeUse(token));
            assertFalse(block.completeNativeUse(token, true,
                    false, false, 81L));
            assertFalse(block.ownsBlock());
            // Still enabled for a genuine later valid sword use.
            final long fresh = block.beginNativeUse(82L);
            assertNotEquals(token, fresh);
            assertTrue(block.completeNativeUse(fresh, true,
                    false, false, 82L));
            assertTrue(block.ownsBlock());
        } finally {
            block.forgetForShutdown();
        }
    }

    @Test
    void staleFinallyCannotCancelNewerReentrantReservation() {
        final Minecraft189AutoBlockModule block = new Minecraft189AutoBlockModule();
        block.onEnable();
        try {
            final long stale = block.beginNativeUse(40L);
            final long fresh = block.beginNativeUse(41L);
            assertNotEquals(stale, fresh);
            assertFalse(block.cancelPendingNativeUse(stale));
            assertTrue(block.completeNativeUse(fresh, true,
                    false, false, 41L));
            assertTrue(block.ownsBlock());
        } finally {
            block.forgetForShutdown();
        }
    }

    @Test
    void cleanupAfterSuccessfulNativeSwordUseNeverUnownsBlock() {
        final Minecraft189AutoBlockModule block = new Minecraft189AutoBlockModule();
        block.onEnable();
        try {
            final long token = block.beginNativeUse(8L);
            assertTrue(block.completeNativeUse(token, true,
                    false, false, 8L));
            assertFalse(block.cancelPendingNativeUse(token));
            assertTrue(block.ownsBlock());
            // Manual right takes over without a synthetic release.
            assertFalse(block.shouldStop(false, true, true, true));
            assertFalse(block.ownsBlock());
        } finally {
            block.forgetForShutdown();
        }
    }

    @Test
    void cancelledTicketCannotCrossOffOnLifecycle() {
        final Minecraft189AutoBlockModule block = new Minecraft189AutoBlockModule();
        block.onEnable();
        final long old = block.beginNativeUse(17L);
        block.onDisable();
        block.onEnable();
        try {
            final long current = block.beginNativeUse(18L);
            assertFalse(block.cancelPendingNativeUse(old));
            assertTrue(block.completeNativeUse(current, true,
                    false, false, 18L));
            assertTrue(block.ownsBlock());
        } finally {
            block.forgetForShutdown();
        }
    }
}
