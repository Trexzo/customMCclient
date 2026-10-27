package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * V338: HitSelect's native final dispatch must not borrow injury permission
 * across index reuse, target changes or mismatched World snapshots.
 *
 * These are original-source-shaped snapshots, NOT fabricated server success.
 */
final class Minecraft189HitSelectNativeOwnerTest {
    private static final UUID TARGET_A =
            UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID TARGET_B =
            UUID.fromString("00000000-0000-0000-0000-0000000000b2");
    private static final UUID LOCAL =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    private static Minecraft189WorldEntityUuidState.Snapshot ids(
            final UUID... values) {
        final Minecraft189WorldEntityUuidState state =
                new Minecraft189WorldEntityUuidState();
        assertTrue(state.update(values));
        return state.snapshot();
    }

    private static Minecraft189WorldEntityKindState.Snapshot kinds(
            final int... values) {
        final Minecraft189WorldEntityKindState state =
                new Minecraft189WorldEntityKindState();
        state.update(values);
        return state.snapshot();
    }

    private static Minecraft189WorldEntityCombatState.Snapshot hurt(
            final int... encoded) {
        final Minecraft189WorldEntityCombatState state =
                new Minecraft189WorldEntityCombatState();
        state.update(encoded);
        return state.snapshot();
    }

    @Test
    void exactOriginalPlayerUuidAndVerifiedHurtWindowAreRequired() {
        final Minecraft189HitSelectModule module = new Minecraft189HitSelectModule();
        module.onEnable();
        try {
            final int player = Minecraft189WorldEntityKindState.LIVING
                    | Minecraft189WorldEntityKindState.PLAYER;
            assertTrue(module.permitsOriginalOwner(1, TARGET_A,
                    ids(LOCAL, TARGET_A), kinds(7, player),
                    hurt(1, 3))); // alive, hurtTime=1
            assertFalse(module.permitsOriginalOwner(1, TARGET_A,
                    ids(LOCAL, TARGET_A), kinds(7, player),
                    hurt(1, 11))); // alive, hurtTime=5
            module.maxTargetHurtTicksSetting().set(5);
            assertTrue(module.permitsOriginalOwner(1, TARGET_A,
                    ids(LOCAL, TARGET_A), kinds(7, player),
                    hurt(1, 11)));
            assertFalse(module.permitsOriginalOwner(1, TARGET_A,
                    ids(LOCAL, TARGET_A), kinds(7, player),
                    hurt(1, 10))); // dead even inside the threshold
        } finally {
            module.onDisable();
        }
    }

    @Test
    void reusedNativeIndexCannotTransferEarlierTargetPermission() {
        final Minecraft189HitSelectModule module = new Minecraft189HitSelectModule();
        module.onEnable();
        try {
            final int player = Minecraft189WorldEntityKindState.LIVING
                    | Minecraft189WorldEntityKindState.PLAYER;
            final Minecraft189WorldEntityCombatState.Snapshot fresh =
                    hurt(1, 1); // current B is alive with 0 hurt ticks
            assertFalse(module.permitsOriginalOwner(1, TARGET_A,
                    ids(LOCAL, TARGET_B), kinds(7, player), fresh));
            assertTrue(module.permitsOriginalOwner(1, TARGET_B,
                    ids(LOCAL, TARGET_B), kinds(7, player), fresh));
            assertFalse(module.permitsOriginalOwner(1, null,
                    ids(LOCAL, TARGET_B), kinds(7, player), fresh));
        } finally {
            module.onDisable();
        }
    }

    @Test
    void unknownDeadNonPlayerOrLocalSlotCannotUseHitSelect() {
        final Minecraft189HitSelectModule module = new Minecraft189HitSelectModule();
        module.onEnable();
        try {
            assertFalse(module.permitsOriginalOwner(-1, TARGET_A,
                    ids(TARGET_A), kinds(3), hurt(1)));
            assertFalse(module.permitsOriginalOwner(1, TARGET_A,
                    ids(LOCAL, TARGET_A), kinds(7, 1), hurt(1, 1)));
            assertFalse(module.permitsOriginalOwner(1, TARGET_A,
                    ids(LOCAL, TARGET_A), kinds(7, 7), hurt(1, 1)));
            assertFalse(module.permitsOriginalOwner(1, TARGET_A,
                    ids(LOCAL, TARGET_A), kinds(7, 3), hurt(1, -1)));
            assertFalse(module.permitsOriginalOwner(1, TARGET_A,
                    ids(LOCAL, TARGET_A), kinds(7, 3), hurt(1, 0)));
        } finally {
            module.onDisable();
        }
    }

    @Test
    void unavailableOrDifferentLengthOriginalWorldSnapshotsFailShut() {
        final Minecraft189HitSelectModule module = new Minecraft189HitSelectModule();
        module.onEnable();
        try {
            final Minecraft189WorldEntityUuidState staleIds =
                    new Minecraft189WorldEntityUuidState();
            staleIds.clear();
            final Minecraft189WorldEntityKindState staleKinds =
                    new Minecraft189WorldEntityKindState();
            staleKinds.clear();
            final Minecraft189WorldEntityCombatState staleCombat =
                    new Minecraft189WorldEntityCombatState();
            staleCombat.clear();
            assertFalse(module.permitsOriginalOwner(1, TARGET_A,
                    staleIds.snapshot(), kinds(7, 3), hurt(1, 1)));
            assertFalse(module.permitsOriginalOwner(1, TARGET_A,
                    ids(LOCAL, TARGET_A), staleKinds.snapshot(), hurt(1, 1)));
            assertFalse(module.permitsOriginalOwner(1, TARGET_A,
                    ids(LOCAL, TARGET_A), kinds(7, 3), staleCombat.snapshot()));
            assertFalse(module.permitsOriginalOwner(1, TARGET_A,
                    ids(LOCAL, TARGET_A), kinds(7), hurt(1, 1)));
            assertFalse(module.permitsOriginalOwner(1, TARGET_A,
                    ids(LOCAL, TARGET_A), kinds(7, 3), hurt(1)));
        } finally {
            module.onDisable();
        }
    }

    @Test
    void disablingHitSelectPreservesExistingSyntheticModuleBehavior() {
        final Minecraft189HitSelectModule module = new Minecraft189HitSelectModule();
        assertFalse(module.active());
        assertTrue(module.permitsOriginalOwner(-1, null,
                null, null, null));
        module.onEnable();
        assertFalse(module.permitsOriginalOwner(-1, null,
                null, null, null));
        module.onDisable();
        assertTrue(module.permitsOriginalOwner(-1, null,
                null, null, null));
    }
}
