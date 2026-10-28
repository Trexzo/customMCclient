package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Per-real-UUID synthetic admission never fabricates vanilla mouse input. */
final class Minecraft189HitSelectRecoveryCycleTest {
    private static final UUID A = UUID.fromString(
            "e3542dd1-56f3-3ab4-82cb-2c015df72d8d");
    private static final UUID B = UUID.fromString(
            "9c6b2c0b-a653-3300-9829-2617908fae14");

    private static final class Source {
        final Minecraft189WorldEntityUuidState ids =
                new Minecraft189WorldEntityUuidState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        final Minecraft189WorldEntityCombatState combat =
                new Minecraft189WorldEntityCombatState();
        final Minecraft189HitSelectModule select =
                new Minecraft189HitSelectModule();

        Source() {
            ids.update(new UUID[]{A, B});
            kinds.update(new int[]{
                    Minecraft189WorldEntityKindState.LIVING
                            | Minecraft189WorldEntityKindState.PLAYER,
                    Minecraft189WorldEntityKindState.LIVING
                            | Minecraft189WorldEntityKindState.PLAYER});
            hurt(0, 0);
            select.onEnable();
        }

        void hurt(final int a, final int b) {
            combat.update(new int[]{1 | (a << 1), 1 | (b << 1)});
        }

        void observe(final int index) {
            select.observeOriginalOwner(index, ids.snapshot(),
                    kinds.snapshot(), combat.snapshot());
        }

        boolean allows(final int index, final UUID expected) {
            return select.permitsOriginalOwner(
                    index, expected, ids.snapshot(), kinds.snapshot(),
                    combat.snapshot());
        }

        void recovery() {
            select.requireObservedRecoverySetting().set(Boolean.TRUE);
        }
    }

    @Test
    void existingDefaultHurtGateIsUnchangedAndMinWindowIsOptional() {
        final Source s = new Source();
        assertFalse(s.select.requireObservedRecoverySetting().get());
        assertTrue(s.select.permits(0, s.combat.snapshot()));
        s.hurt(3, 0);
        assertFalse(s.select.permits(0, s.combat.snapshot()));
        s.select.maxTargetHurtTicksSetting().set(4);
        assertTrue(s.select.permits(0, s.combat.snapshot()));
        s.select.minTargetHurtTicksSetting().set(4);
        assertFalse(s.select.permits(0, s.combat.snapshot()));
        s.select.minTargetHurtTicksSetting().set(2);
        assertTrue(s.select.permits(0, s.combat.snapshot()));
        s.select.maxTargetHurtTicksSetting().set(1);
        assertFalse(s.select.permits(0, s.combat.snapshot()));
    }

    @Test
    void oneScheduledClickNeedsActualHurtRiseThenFallBeforeNext() {
        final Source s = new Source();
        s.recovery();
        s.observe(0);
        assertTrue(s.allows(0, A));
        s.select.noteScheduledOriginalClick(A);
        assertFalse(s.allows(0, A));
        // Unchanged 'ready' snapshots cannot rearm on an old ready tick.
        s.observe(0);
        assertFalse(s.allows(0, A));
        s.hurt(7, 0);
        s.observe(0);
        assertFalse(s.allows(0, A));
        s.hurt(2, 0);
        s.observe(0);
        assertFalse(s.allows(0, A));
        s.hurt(1, 0);
        s.observe(0);
        assertTrue(s.allows(0, A));
        s.select.noteScheduledOriginalClick(A);
        assertFalse(s.allows(0, A));
    }

    @Test
    void switchToNewOriginalUuidCannotBorrowPreviousTargetState() {
        final Source s = new Source();
        s.recovery();
        s.observe(0);
        s.select.noteScheduledOriginalClick(A);
        assertFalse(s.allows(0, A));
        s.observe(1);
        assertTrue(s.allows(1, B));
        assertFalse(s.allows(0, A));
        s.select.noteScheduledOriginalClick(B);
        assertFalse(s.allows(1, B));
        // Returning to a previously clicked A must not erase its
        // UUID-specific recovery state by briefly selecting B.
        s.observe(0);
        assertFalse(s.allows(0, A));
        s.hurt(5, 0);
        s.observe(0);
        assertFalse(s.allows(0, A));
        s.hurt(0, 0);
        s.observe(0);
        assertTrue(s.allows(0, A));
    }

    @Test
    void missingDuplicatedOrLocalOriginalUuidNeverProvidesRecovery() {
        final Source s = new Source();
        s.recovery();
        s.observe(0);
        s.select.noteScheduledOriginalClick(A);
        s.hurt(8, 0);
        s.ids.clear();
        s.observe(0);
        assertFalse(s.allows(0, A));
        s.ids.update(new UUID[]{A, A}); // Duplicate is rejected by native state.
        s.observe(0);
        assertFalse(s.allows(0, A));
        s.ids.update(new UUID[]{A, B});
        s.kinds.update(new int[]{
                Minecraft189WorldEntityKindState.LIVING
                        | Minecraft189WorldEntityKindState.PLAYER
                        | Minecraft189WorldEntityKindState.LOCAL_PLAYER,
                Minecraft189WorldEntityKindState.LIVING
                        | Minecraft189WorldEntityKindState.PLAYER});
        s.observe(0);
        assertFalse(s.allows(0, A));
        s.kinds.update(new int[]{3, 3});
        s.observe(0);
        assertFalse(s.allows(0, A));
        s.hurt(1, 0);
        s.observe(0);
        assertTrue(s.allows(0, A));
        assertFalse(s.allows(0, B));
        assertFalse(s.allows(-1, A));
        assertFalse(s.allows(8, A));
        assertFalse(s.allows(0, null));
    }

    @Test
    void disableGuiAndSourceRevocationErasePriorScheduledCredit() {
        final Source s = new Source();
        s.recovery();
        s.observe(0);
        s.select.noteScheduledOriginalClick(A);
        assertFalse(s.allows(0, A));
        s.select.suspendForGui();
        assertFalse(s.allows(0, A));
        s.observe(0);
        assertTrue(s.allows(0, A));
        s.select.noteScheduledOriginalClick(A);
        s.select.suspendForSource();
        assertFalse(s.allows(0, A));
        s.observe(0);
        assertTrue(s.allows(0, A));
        s.select.onDisable();
        // Disabled HitSelect does not add any synthetic-click permission.
        assertTrue(s.allows(-1, null)); // Other guards own the click.
        s.select.onEnable();
        assertFalse(s.allows(0, A));
        s.observe(0);
        assertTrue(s.allows(0, A));
    }

    @Test
    void nativeWorldReadReentryRevokesOldActivationAndChangedPolicy() {
        final Source s = new Source();
        s.recovery();
        s.observe(0);
        long token = s.select.nativeOwnerEpoch();
        assertTrue(s.allows(0, A));
        s.select.suspendForSource();
        assertTrue(s.select.nativeOwnerEpoch() != token);
        assertFalse(s.allows(0, A));
        s.observe(0);
        long fresh = s.select.nativeOwnerEpoch();
        assertTrue(s.allows(0, A));
        s.select.maxTargetHurtTicksSetting().set(3);
        s.observe(0);
        assertTrue(s.select.nativeOwnerEpoch() != fresh);
        assertTrue(s.allows(0, A));
        fresh = s.select.nativeOwnerEpoch();
        s.select.onDisable();
        s.select.onEnable();
        assertTrue(s.select.nativeOwnerEpoch() != fresh);
        assertFalse(s.allows(0, A));
    }

    @Test
    void indexOnlyLegacyCallerCannotBypassRecoveryMode() {
        final Source s = new Source();
        s.recovery();
        s.observe(0);
        assertFalse(s.select.permits(0, s.combat.snapshot()));
        assertTrue(s.allows(0, A));
        s.select.noteScheduledOriginalClick(B);
        assertTrue(s.allows(0, A));
        s.select.noteScheduledOriginalClick(A);
        assertFalse(s.allows(0, A));
        s.select.requireObservedRecoverySetting().set(Boolean.FALSE);
        s.observe(0);
        assertTrue(s.select.permits(0, s.combat.snapshot()));
    }
}
