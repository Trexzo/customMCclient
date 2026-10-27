package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * V342: real COMBINED module implementations in one deterministic sequence.
 *
 * These are source-shaped local test doubles, not original Mojang/server
 * acceptance. Native controller UUID and server-owned damage must still
 * be demonstrated by the official binary CI separately.
 */
final class Minecraft189CombatModulesTandemTest {
    private static final UUID SELF =
            UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID A =
            UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID B =
            UUID.fromString("00000000-0000-0000-0000-0000000000b2");

    private static Minecraft189WorldEntityUuidState.Snapshot ids(UUID remote) {
        final Minecraft189WorldEntityUuidState world =
                new Minecraft189WorldEntityUuidState();
        assertTrue(world.update(new UUID[]{SELF, remote}));
        return world.snapshot();
    }

    private static Minecraft189WorldEntityKindState.Snapshot kinds() {
        final Minecraft189WorldEntityKindState world =
                new Minecraft189WorldEntityKindState();
        world.update(new int[]{
                Minecraft189WorldEntityKindState.LIVING
                        | Minecraft189WorldEntityKindState.PLAYER
                        | Minecraft189WorldEntityKindState.LOCAL_PLAYER,
                Minecraft189WorldEntityKindState.LIVING
                        | Minecraft189WorldEntityKindState.PLAYER
        });
        return world.snapshot();
    }

    private static Minecraft189WorldEntityCombatState.Snapshot hurt(int time) {
        final Minecraft189WorldEntityCombatState world =
                new Minecraft189WorldEntityCombatState();
        world.update(new int[]{1, 1 | (time << 1)});
        return world.snapshot();
    }

    @Test
    void hitSelectWtapSprintVelocityAndAutoBlockCoordinateInOneMeleeCycle() {
        final Minecraft189CombatCycleArbiter lane =
                new Minecraft189CombatCycleArbiter();
        final Minecraft189HitSelectModule hit =
                new Minecraft189HitSelectModule();
        final Minecraft189WTapModule wtap = new Minecraft189WTapModule();
        final Minecraft189KeepSprintModule keep =
                new Minecraft189KeepSprintModule();
        final Minecraft189VelocityModule velocity =
                new Minecraft189VelocityModule();
        final Minecraft189AutoBlockModule block =
                new Minecraft189AutoBlockModule();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        final SprintRecorder sprint = new SprintRecorder();
        hit.onEnable();
        wtap.onEnable();
        keep.onEnable();
        velocity.onEnable();
        block.onEnable();
        try {
            movement.update(true, false, true);
            assertTrue(hit.permitsOriginalOwner(
                    1, A, ids(A), kinds(), hurt(0)));
            final long token = lane.enter();
            assertTrue(lane.claimPrimary(token,
                    Minecraft189CombatCycleArbiter.Primary.MELEE));
            assertTrue(wtap.apply(sprint, movement.snapshot(), true, true));
            assertEquals(1, sprint.calls);
            assertFalse(keep.shouldRestore(
                    movement.snapshot(), true, true, true,
                    wtap.active(), false));
            assertEquals(2.0D, velocity.adjustHorizontal(2.0D, 5.0D));
            // AutoBlock is a separate sword-use contract, and manual
            // right-click must not be stolen by this eligible melee.
            assertFalse(block.mayStart(
                    true, true, false, true, false, false));
            assertTrue(block.mayStart(
                    true, true, false, false, false, false));
            assertTrue(lane.claimReblock(token));
            assertTrue(block.completeNativeUse(
                    block.beginNativeUse(50L), true, false, false, 50L));
            assertTrue(block.ownsBlock());
            assertFalse(block.shouldStop(false, true, true, true));
            assertFalse(block.ownsBlock());
            assertFalse(lane.claimPrimary(token,
                    Minecraft189CombatCycleArbiter.Primary.ROD));
            lane.leave(token);
        } finally {
            block.onDisable();
            velocity.onDisable();
            keep.onDisable();
            wtap.onDisable();
            hit.onDisable();
        }
    }

    @Test
    void indexReuseInvalidatesHitSelectWhileRodPotPreemptAnyReblock() {
        final Minecraft189HitSelectModule hit =
                new Minecraft189HitSelectModule();
        final Minecraft189CombatCycleArbiter lane =
                new Minecraft189CombatCycleArbiter();
        hit.onEnable();
        try {
            assertFalse(hit.permitsOriginalOwner(
                    1, A, ids(B), kinds(), hurt(0)),
                    "B cannot spend the previous original A hurt window");
            final long first = lane.enter();
            assertTrue(lane.claimPrimary(first,
                    Minecraft189CombatCycleArbiter.Primary.POTION));
            assertFalse(lane.claimPrimary(first,
                    Minecraft189CombatCycleArbiter.Primary.ROD));
            assertFalse(lane.claimReblock(first));
            lane.leave(first);

            final long next = lane.enter();
            assertTrue(lane.claimPrimary(next,
                    Minecraft189CombatCycleArbiter.Primary.ROD));
            assertFalse(lane.claimPrimary(next,
                    Minecraft189CombatCycleArbiter.Primary.MELEE));
            assertFalse(lane.claimReblock(next));
            lane.leave(next);

            final long clean = lane.enter();
            assertFalse(lane.claimReblock(clean));
            assertEquals(Minecraft189CombatCycleArbiter.Phase.EVALUATING,
                    lane.phase());
            lane.leave(clean);
        } finally {
            hit.onDisable();
        }
    }

    @Test
    void alreadyOwnedAutoBlockAndNewSourceEpochDoNotCrossActionCycles() {
        final Minecraft189AutoBlockModule block =
                new Minecraft189AutoBlockModule();
        final Minecraft189CombatCycleArbiter lane =
                new Minecraft189CombatCycleArbiter();
        block.onEnable();
        try {
            final long old = lane.enter();
            assertTrue(lane.claimPrimary(old,
                    Minecraft189CombatCycleArbiter.Primary.MELEE));
            assertTrue(lane.claimReblock(old));
            final long sword = block.beginNativeUse(31L);
            assertFalse(block.completeNativeUse(
                    sword, true, false, false, 32L));
            assertFalse(block.ownsBlock());
            lane.revoke();
            final long fresh = lane.enter();
            lane.leave(old); // Stale native finally must not end new cycle.
            assertEquals(Minecraft189CombatCycleArbiter.Phase.EVALUATING,
                    lane.phase());
            assertFalse(lane.claimReblock(fresh));
            assertTrue(lane.claimPrimary(fresh,
                    Minecraft189CombatCycleArbiter.Primary.MELEE));
            assertTrue(lane.claimReblock(fresh));
            assertTrue(block.completeNativeUse(
                    block.beginNativeUse(32L),
                    true, false, false, 32L));
            assertTrue(block.ownsBlock());
            lane.leave(fresh);
        } finally {
            block.forgetForShutdown();
        }
    }

    private static final class SprintRecorder
            implements Minecraft189PlayerSprintControl {
        int calls;

        @Override
        public void customMcSetSprinting(final boolean sprinting) {
            assertFalse(sprinting);
            calls++;
        }
    }
}
