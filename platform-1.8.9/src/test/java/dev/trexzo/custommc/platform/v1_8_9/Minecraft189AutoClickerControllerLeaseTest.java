package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * One verified native target may reach one original controller argument.
 * These tests do not fabricate vanilla server damage or a native hit.
 */
final class Minecraft189AutoClickerControllerLeaseTest {
    private static final UUID PLAYER_A =
            UUID.fromString("10000000-0000-4000-8000-000000000001");
    private static final UUID PLAYER_B =
            UUID.fromString("20000000-0000-4000-8000-000000000002");

    @Test
    void oneShotIntentCannotBeReplayedOrRedirectedToOtherPlayer() {
        final Minecraft189AutoClickerControllerLease lease =
                new Minecraft189AutoClickerControllerLease();
        assertFalse(lease.pending());
        lease.arm(PLAYER_A, 10L);
        assertTrue(lease.pending());
        // Original World index A has become B before native clickMouse.
        assertNull(lease.consume(PLAYER_B, 10L, true, false, false));
        assertFalse(lease.pending());
        assertNull(lease.consume(PLAYER_A, 10L, true, false, false));

        lease.arm(PLAYER_A, 10L);
        final Minecraft189AutoClickerControllerLease.Intent accepted =
                lease.consume(PLAYER_A, 10L, true, false, false);
        assertNotNull(accepted);
        assertFalse(lease.pending());
        assertNull(lease.consume(PLAYER_A, 10L, true, false, false));
        assertTrue(accepted.permits(PLAYER_A, 10L, true, false, false));
        // Even after admission, the real controller's Entity can change.
        assertFalse(accepted.permits(PLAYER_B, 10L, true, false, false));
        assertFalse(accepted.permits(null, 10L, true, false, false));
    }

    @Test
    void originalModuleLifecycleAndPolicyChangesRevokeTheController() {
        final Minecraft189AutoClickerControllerLease lease =
                new Minecraft189AutoClickerControllerLease();
        lease.arm(PLAYER_A, 7L);
        assertNull(lease.consume(PLAYER_A, 8L, true, false, false));
        assertFalse(lease.pending());

        lease.arm(PLAYER_A, 8L);
        assertNull(lease.consume(PLAYER_A, 8L, false, false, false));
        lease.arm(PLAYER_A, 8L);
        assertNull(lease.consume(PLAYER_A, 8L, true, true, false));
        lease.arm(PLAYER_A, 8L);
        assertNull(lease.consume(PLAYER_A, 8L, true, false, true));

        lease.arm(PLAYER_A, 9L);
        final Minecraft189AutoClickerControllerLease.Intent intent =
                lease.consume(PLAYER_A, 9L, true, false, false);
        assertNotNull(intent);
        assertFalse(intent.permits(PLAYER_A, 10L, true, false, false));
        assertFalse(intent.permits(PLAYER_A, 9L, false, false, false));
        assertFalse(intent.permits(PLAYER_A, 9L, true, true, false));
        assertFalse(intent.permits(PLAYER_A, 9L, true, false, true));
    }

    @Test
    void clearingAndReplacingPendingOwnerCannotResurrectOldIntent() {
        final Minecraft189AutoClickerControllerLease lease =
                new Minecraft189AutoClickerControllerLease();
        lease.arm(PLAYER_A, 1L);
        lease.clear();
        assertFalse(lease.pending());
        assertNull(lease.consume(PLAYER_A, 1L, true, false, false));
        lease.arm(null, 1L);
        assertFalse(lease.pending());

        lease.arm(PLAYER_A, 2L);
        lease.arm(PLAYER_B, 2L);
        assertNull(lease.consume(PLAYER_A, 2L, true, false, false));
        assertFalse(lease.pending());
        lease.arm(PLAYER_B, 3L);
        final Minecraft189AutoClickerControllerLease.Intent replacement =
                lease.consume(PLAYER_B, 3L, true, false, false);
        assertNotNull(replacement);
        assertTrue(PLAYER_B.equals(replacement.targetUuid()));
    }
}
