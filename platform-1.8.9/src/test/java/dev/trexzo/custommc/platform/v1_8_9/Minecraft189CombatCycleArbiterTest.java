package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * V341: cross-module action-cycle ownership, not a substitute for actual
 * Mojang 1.8.9 native controller, source UUID or server damage.
 */
final class Minecraft189CombatCycleArbiterTest {
    @Test
    void reentrantNativeCallbackCannotClaimAnotherPrimaryAction() {
        final Minecraft189CombatCycleArbiter state =
                new Minecraft189CombatCycleArbiter();
        final long outer = state.enter();
        assertNotEquals(0L, outer);
        assertEquals(0L, state.enter());
        assertFalse(state.claimPrimary(0L,
                Minecraft189CombatCycleArbiter.Primary.ROD));
        assertTrue(state.claimPrimary(outer,
                Minecraft189CombatCycleArbiter.Primary.POTION));
        assertEquals(Minecraft189CombatCycleArbiter.Phase.POTION, state.phase());
        assertFalse(state.claimPrimary(outer,
                Minecraft189CombatCycleArbiter.Primary.ROD));
        assertFalse(state.claimPrimary(outer,
                Minecraft189CombatCycleArbiter.Primary.MELEE));
        assertFalse(state.claimReblock(outer));
        state.leave(0L);
        assertEquals(Minecraft189CombatCycleArbiter.Phase.POTION, state.phase());
        state.leave(outer);
        assertEquals(Minecraft189CombatCycleArbiter.Phase.QUIESCENT,
                state.phase());
    }

    @Test
    void rodCannotDoubleAttackOrBeginSwordBlock() {
        final Minecraft189CombatCycleArbiter state =
                new Minecraft189CombatCycleArbiter();
        final long tick = state.enter();
        assertTrue(state.claimPrimary(tick,
                Minecraft189CombatCycleArbiter.Primary.ROD));
        assertFalse(state.claimPrimary(tick,
                Minecraft189CombatCycleArbiter.Primary.POTION));
        assertFalse(state.claimPrimary(tick,
                Minecraft189CombatCycleArbiter.Primary.MELEE));
        assertFalse(state.claimReblock(tick));
        state.leave(tick);
    }

    @Test
    void meleeAttemptCanReblockOnceButNeverClaimServerHit() {
        final Minecraft189CombatCycleArbiter state =
                new Minecraft189CombatCycleArbiter();
        final long tick = state.enter();
        assertTrue(state.claimPrimary(tick,
                Minecraft189CombatCycleArbiter.Primary.MELEE));
        assertEquals(Minecraft189CombatCycleArbiter.Phase.MELEE_ATTEMPT,
                state.phase());
        assertTrue(state.claimReblock(tick));
        assertEquals(Minecraft189CombatCycleArbiter.Phase.REBLOCK_ATTEMPT,
                state.phase());
        assertFalse(state.claimReblock(tick));
        assertFalse(state.claimPrimary(tick,
                Minecraft189CombatCycleArbiter.Primary.MELEE));
        state.leave(tick);
        final long next = state.enter();
        assertNotEquals(tick, next);
        assertFalse(state.claimReblock(next));
        state.leave(next);
    }

    @Test
    void oldCleanupCannotRevokeNewCycleAfterLifecycleChange() {
        final Minecraft189CombatCycleArbiter state =
                new Minecraft189CombatCycleArbiter();
        final long old = state.enter();
        state.revoke();
        final long newToken = state.enter();
        assertNotEquals(old, newToken);
        state.leave(old);
        assertEquals(Minecraft189CombatCycleArbiter.Phase.EVALUATING,
                state.phase());
        assertTrue(state.claimPrimary(newToken,
                Minecraft189CombatCycleArbiter.Primary.MELEE));
        state.leave(newToken);
        assertEquals(Minecraft189CombatCycleArbiter.Phase.QUIESCENT,
                state.phase());
    }

    @Test
    void nativeThrowableFinallyReturnsToIdleWithoutBankingActions() {
        final Minecraft189CombatCycleArbiter state =
                new Minecraft189CombatCycleArbiter();
        final long token = state.enter();
        try {
            assertTrue(state.claimPrimary(token,
                    Minecraft189CombatCycleArbiter.Primary.POTION));
            throw new IllegalStateException("original right click failure");
        } catch (IllegalStateException expected) {
            assertEquals("original right click failure", expected.getMessage());
        } finally {
            state.leave(token);
        }
        final long later = state.enter();
        assertNotEquals(0L, later);
        assertFalse(state.claimReblock(later));
        assertTrue(state.claimPrimary(later,
                Minecraft189CombatCycleArbiter.Primary.ROD));
        state.leave(later);
    }

    @Test
    void wTapAndKeepSprintRemainMutuallyExclusiveWhileCycleIsOwned() {
        final Minecraft189CombatCycleArbiter state =
                new Minecraft189CombatCycleArbiter();
        final Minecraft189KeepSprintModule keep =
                new Minecraft189KeepSprintModule();
        final Minecraft189WTapModule tap = new Minecraft189WTapModule();
        final Minecraft189PlayerMovementState move =
                new Minecraft189PlayerMovementState();
        move.update(true, false, true);
        keep.onEnable();
        tap.onEnable();
        try {
            final long cycle = state.enter();
            assertTrue(state.claimPrimary(cycle,
                    Minecraft189CombatCycleArbiter.Primary.MELEE));
            assertFalse(keep.shouldRestore(move.snapshot(),
                    true, true, true, tap.active(), false),
                    "WTap owns sprint reset when both modules are enabled");
            assertEquals(Minecraft189CombatCycleArbiter.Phase.MELEE_ATTEMPT,
                    state.phase());
            tap.onDisable();
            assertTrue(keep.shouldRestore(move.snapshot(),
                    true, true, true, tap.active(), false));
            assertFalse(state.claimPrimary(cycle,
                    Minecraft189CombatCycleArbiter.Primary.ROD));
            state.leave(cycle);
        } finally {
            tap.onDisable();
            keep.onDisable();
        }
    }

    @Test
    void meleeReblockRemainsSubjectToRealManualRightClickOwnership() {
        final Minecraft189CombatCycleArbiter state =
                new Minecraft189CombatCycleArbiter();
        final Minecraft189AutoBlockModule block =
                new Minecraft189AutoBlockModule();
        block.onEnable();
        try {
            final long cycle = state.enter();
            assertTrue(state.claimPrimary(cycle,
                    Minecraft189CombatCycleArbiter.Primary.MELEE));
            assertFalse(block.mayStart(
                    true, true, false, true, false, false),
                    "manual right-click owns native item use");
            assertTrue(block.mayStart(
                    true, true, false, false, false, false));
            assertTrue(state.claimReblock(cycle));
            assertFalse(state.claimReblock(cycle));
            state.leave(cycle);
            // State machine does not grant sword permission on a rod tick.
            final long rod = state.enter();
            assertTrue(state.claimPrimary(rod,
                    Minecraft189CombatCycleArbiter.Primary.ROD));
            assertFalse(state.claimReblock(rod));
            state.leave(rod);
        } finally {
            block.onDisable();
        }
    }
}
