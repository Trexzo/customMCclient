package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * V337: real mapped movement qualifiers must continue to own every WTap
 * multi-tick EntityPlayer.setSprinting(false) write.
 *
 * Test doubles do not certify original Mojang game/server authority.
 */
final class Minecraft189WTapContextLossTest {
    @Test
    void losingGroundCancelsRemainingNativeWritesAndRetainsCooldown() {
        final Minecraft189WTapModule tap = new Minecraft189WTapModule();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        final CounterPlayer player = new CounterPlayer();
        tap.requireGroundSetting().set(Boolean.TRUE);
        tap.resetTicksSetting().set(3);
        tap.cooldownTicksSetting().set(4);
        tap.onEnable();
        try {
            movement.update(true, false, true);
            assertTrue(tap.apply(player, movement.snapshot(), true, true));
            assertEquals(1, player.calls);

            movement.update(false, false, true);
            assertFalse(tap.apply(player, movement.snapshot(), true, true));
            assertEquals(1, player.calls);

            // No synthetic write may resume on landing while LMB is held.
            movement.update(true, false, true);
            assertFalse(tap.apply(player, movement.snapshot(), true, true));
            assertEquals(1, player.calls);

            // The original cooldown survives cancellation and blocks an
            // immediate fresh physical press.
            assertFalse(tap.apply(player, movement.snapshot(), false, true));
            assertFalse(tap.apply(player, movement.snapshot(), true, true));
            assertEquals(1, player.calls);
            assertFalse(tap.apply(player, movement.snapshot(), false, true));
            assertTrue(tap.apply(player, movement.snapshot(), true, true));
            assertEquals(2, player.calls);
        } finally {
            tap.onDisable();
        }
    }

    @Test
    void releasingForwardCancelsResetAndDoesNotBankHeldAttackEdge() {
        final Minecraft189WTapModule tap = new Minecraft189WTapModule();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        final CounterPlayer player = new CounterPlayer();
        tap.requireForwardSetting().set(Boolean.TRUE);
        tap.resetTicksSetting().set(5);
        tap.onEnable();
        try {
            movement.update(true, false, true);
            assertTrue(tap.apply(player, movement.snapshot(), true, true));
            assertEquals(1, player.calls);
            assertFalse(tap.apply(player, movement.snapshot(), true, false));
            assertFalse(tap.apply(player, movement.snapshot(), true, true));
            assertFalse(tap.apply(player, movement.snapshot(), true, true));
            assertEquals(1, player.calls);
            assertFalse(tap.apply(player, movement.snapshot(), false, true));
            assertTrue(tap.apply(player, movement.snapshot(), true, true));
            assertEquals(2, player.calls);
        } finally {
            tap.onDisable();
        }
    }

    @Test
    void bothRequirementsDisabledRetainLegacyMultiTickContinuation() {
        final Minecraft189WTapModule tap = new Minecraft189WTapModule();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        final CounterPlayer player = new CounterPlayer();
        tap.resetTicksSetting().set(3);
        tap.onEnable();
        try {
            movement.update(true, false, true);
            assertTrue(tap.apply(player, movement.snapshot(), true, true));
            movement.update(false, false, true);
            assertTrue(tap.apply(player, movement.snapshot(), true, false));
            assertTrue(tap.apply(player, movement.snapshot(), true, false));
            assertEquals(3, player.calls);
            assertFalse(tap.apply(player, movement.snapshot(), true, false));
        } finally {
            tap.onDisable();
        }
    }

    @Test
    void reentrantOriginalSetterLosingGroundRevokesOuterSprintWriteEpoch() {
        final Minecraft189WTapModule tap = new Minecraft189WTapModule();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        final ReentrantPlayer player = new ReentrantPlayer(tap, movement);
        tap.resetTicksSetting().set(4);
        tap.requireGroundSetting().set(Boolean.TRUE);
        tap.onEnable();
        try {
            movement.update(true, false, true);
            assertTrue(tap.apply(player, movement.snapshot(), true, true));
            assertEquals(1, player.calls);
            // The second native setter reenters this synchronized module
            // with a genuine changed grounded snapshot mid-dispatch.
            assertFalse(tap.apply(player, movement.snapshot(), true, true));
            assertEquals(2, player.calls);
            assertTrue(player.nestedVetoObserved);
            movement.update(true, false, true);
            assertFalse(tap.apply(player, movement.snapshot(), true, true));
            assertFalse(tap.apply(player, movement.snapshot(), true, true));
            assertEquals(2, player.calls);
            assertFalse(tap.apply(player, movement.snapshot(), false, true));
            assertTrue(tap.apply(player, movement.snapshot(), true, true));
            assertEquals(3, player.calls);
        } finally {
            tap.onDisable();
        }
    }

    private static class CounterPlayer implements Minecraft189PlayerSprintControl {
        int calls;

        @Override public void customMcSetSprinting(final boolean value) {
            calls++;
            assertFalse(value);
        }
    }

    private static final class ReentrantPlayer extends CounterPlayer {
        private final Minecraft189WTapModule tap;
        private final Minecraft189PlayerMovementState movement;
        boolean nestedVetoObserved;

        ReentrantPlayer(final Minecraft189WTapModule tap,
                final Minecraft189PlayerMovementState movement) {
            this.tap = tap;
            this.movement = movement;
        }

        @Override public void customMcSetSprinting(final boolean value) {
            super.customMcSetSprinting(value);
            if (calls == 2) {
                movement.update(false, false, true);
                nestedVetoObserved = !tap.apply(
                        this, movement.snapshot(), true, true);
            }
        }
    }
}
