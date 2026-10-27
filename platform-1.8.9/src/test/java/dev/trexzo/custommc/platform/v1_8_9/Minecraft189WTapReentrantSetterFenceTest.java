package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * V343: original EntityPlayer.setSprinting can synchronously reenter the
 * mapped host. Never schedule a second WTap write or revive an old cycle.
 *
 * Java source doubles are NOT official Mojang 1.8.9 PvP proof.
 */
final class Minecraft189WTapReentrantSetterFenceTest {
    @Test
    void unchangedReentrantNativeContextRevokesOuterResetAndNeverDoubleWrites() {
        final Minecraft189WTapModule tap = new Minecraft189WTapModule();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        final ReentrantSprintPlayer player = new ReentrantSprintPlayer();
        tap.resetTicksSetting().set(4);
        tap.onEnable();
        try {
            movement.update(true, false, true);
            final int[] nestedCalls = {0};
            player.onNextWrite = () -> {
                nestedCalls[0]++;
                assertFalse(tap.apply(
                        player, movement.snapshot(), true, true));
            };

            // The original first native setter ran, but its pending reset
            // was invalidated when that setter reentered the same module.
            assertFalse(tap.apply(
                    player, movement.snapshot(), true, true));
            assertEquals(1, nestedCalls[0]);
            assertEquals(1, player.writes);
            assertFalse(tap.apply(
                    player, movement.snapshot(), true, true));
            assertEquals(1, player.writes);

            // The old held attack cannot replay; a genuine release and
            // subsequent press may initiate a NEW independent reset.
            assertFalse(tap.apply(
                    player, movement.snapshot(), false, true));
            assertTrue(tap.apply(
                    player, movement.snapshot(), true, true));
            assertEquals(2, player.writes);
        } finally {
            tap.onDisable();
        }
    }

    @Test
    void sneakingInsideOriginalSetterCannotRestoreMultiTickReset() {
        final Minecraft189WTapModule tap = new Minecraft189WTapModule();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        final ReentrantSprintPlayer player = new ReentrantSprintPlayer();
        tap.pauseWhileSneakingSetting().set(Boolean.TRUE);
        tap.resetTicksSetting().set(5);
        tap.onEnable();
        try {
            movement.update(true, false, true);
            player.onNextWrite = () -> {
                movement.update(true, true, true);
                assertFalse(tap.apply(
                        player, movement.snapshot(), true, true));
            };
            assertFalse(tap.apply(
                    player, movement.snapshot(), true, true));
            assertEquals(1, player.writes);

            movement.update(true, false, true);
            assertFalse(tap.apply(
                    player, movement.snapshot(), true, true));
            assertEquals(1, player.writes);
            assertFalse(tap.apply(
                    player, movement.snapshot(), false, true));
            assertTrue(tap.apply(
                    player, movement.snapshot(), true, true));
            assertEquals(2, player.writes);
        } finally {
            tap.onDisable();
        }
    }

    @Test
    void disappearingRealPlayerTargetInsideSetterCannotReviveOldReset() {
        final Minecraft189WTapModule tap = new Minecraft189WTapModule();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        final ReentrantSprintPlayer player = new ReentrantSprintPlayer();
        final Minecraft189PlayerPositionState local =
                new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        final Minecraft189NearestPlayerTargetState nearest =
                new Minecraft189NearestPlayerTargetState();
        local.update(0.0D, 64.0D, 0.0D);
        positions.update(new double[]{
                0.0D, 64.0D, 0.0D, 2.0D, 64.0D, 0.0D});
        kinds.update(new int[]{
                Minecraft189WorldEntityKindState.LIVING
                        | Minecraft189WorldEntityKindState.PLAYER
                        | Minecraft189WorldEntityKindState.LOCAL_PLAYER,
                Minecraft189WorldEntityKindState.LIVING
                        | Minecraft189WorldEntityKindState.PLAYER});
        nearest.update(local.snapshot(), positions.snapshot(),
                kinds.snapshot());
        assertTrue(nearest.snapshot().found());

        tap.requireNearbyPlayerSetting().set(Boolean.TRUE);
        tap.resetTicksSetting().set(4);
        tap.onEnable();
        try {
            movement.update(true, false, true);
            player.onNextWrite = () -> {
                nearest.clear();
                assertFalse(tap.apply(
                        player, movement.snapshot(), true, true,
                        nearest.snapshot()));
            };
            assertFalse(tap.apply(
                    player, movement.snapshot(), true, true,
                    nearest.snapshot()));
            assertEquals(1, player.writes);
            assertFalse(tap.apply(
                    player, movement.snapshot(), true, true,
                    nearest.snapshot()));
            assertEquals(1, player.writes);
        } finally {
            tap.onDisable();
        }
    }

    @Test
    void nativeCallbackReentryCannotIssueAnotherSetterDuringPendingMultiTick() {
        final Minecraft189WTapModule tap = new Minecraft189WTapModule();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        final ReentrantSprintPlayer player = new ReentrantSprintPlayer();
        tap.resetTicksSetting().set(4);
        tap.onEnable();
        try {
            movement.update(true, false, true);
            assertTrue(tap.apply(
                    player, movement.snapshot(), true, true));
            assertEquals(1, player.writes);

            player.onNextWrite = () -> assertFalse(tap.apply(
                    player, movement.snapshot(), true, true));
            assertFalse(tap.apply(
                    player, movement.snapshot(), true, true));
            assertEquals(2, player.writes);
            assertFalse(tap.apply(
                    player, movement.snapshot(), true, true));
            assertEquals(2, player.writes);
        } finally {
            tap.onDisable();
        }
    }

    @Test
    void failedNativeSetterReleasesReentryFenceForFutureRealPress() {
        final Minecraft189WTapModule tap = new Minecraft189WTapModule();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        final ReentrantSprintPlayer player = new ReentrantSprintPlayer();
        tap.onEnable();
        try {
            movement.update(true, false, true);
            player.failNextWrite = true;
            assertThrows(IllegalStateException.class, () ->
                    tap.apply(player, movement.snapshot(), true, true));
            assertEquals(1, player.writes);
            assertFalse(tap.apply(
                    player, movement.snapshot(), true, true));
            assertFalse(tap.apply(
                    player, movement.snapshot(), false, true));
            assertTrue(tap.apply(
                    player, movement.snapshot(), true, true));
            assertEquals(2, player.writes);
        } finally {
            tap.onDisable();
        }
    }

    private static final class ReentrantSprintPlayer
            implements Minecraft189PlayerSprintControl {
        private Runnable onNextWrite;
        private boolean failNextWrite;
        private int writes;

        @Override
        public void customMcSetSprinting(final boolean sprinting) {
            assertFalse(sprinting);
            writes++;
            if (failNextWrite) {
                failNextWrite = false;
                throw new IllegalStateException("original setter failed");
            }
            final Runnable callback = onNextWrite;
            onNextWrite = null;
            if (callback != null) callback.run();
        }
    }
}
