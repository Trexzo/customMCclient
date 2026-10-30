package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * V364 deterministic original-UUID recovery state machine. These snapshots
 * are NOT a fabricated vanilla controller call or dedicated-server damage.
 * Real controller evidence is separately required by the native hook/CI.
 */
final class Minecraft189HitSelectControllerRecoveryTest {
    private static final UUID LOCAL = UUID.fromString(
            "00000000-0000-0000-0000-000000000001");
    private static final UUID A = UUID.fromString(
            "00000000-0000-0000-0000-0000000000a1");
    private static final UUID B = UUID.fromString(
            "00000000-0000-0000-0000-0000000000b2");

    private static final class Fixture {
        final Minecraft189HitSelectModule module = new Minecraft189HitSelectModule();
        final Minecraft189WorldEntityUuidState ids = new Minecraft189WorldEntityUuidState();
        final Minecraft189WorldEntityKindState kinds = new Minecraft189WorldEntityKindState();
        final Minecraft189WorldEntityCombatState combat = new Minecraft189WorldEntityCombatState();
        Fixture() {
            module.onEnable();
            module.requireObservedRecoverySetting().set(true);
            kinds.update(new int[]{7, 3});
            ids.update(new UUID[]{LOCAL, A});
            combat.update(new int[]{1, 1});
            observe();
        }
        void observe() {
            module.observeOriginalOwner(1, ids.snapshot(),
                    kinds.snapshot(), combat.snapshot());
        }
        boolean permits(UUID owner) {
            return module.permitsOriginalOwner(1, owner,
                    ids.snapshot(), kinds.snapshot(), combat.snapshot());
        }
        long epoch() { return module.nativeOwnerEpoch(); }
        boolean genuine(UUID scheduled, UUID controllerUuid, long epoch) {
            return module.noteOriginalNativeControllerAttack(
                    scheduled, controllerUuid, epoch,
                    module.minTargetHurtTicksSetting().get(),
                    module.maxTargetHurtTicksSetting().get());
        }
        void hurt(int encoded) { combat.update(new int[]{1, encoded}); observe(); }
        void target(UUID uuid) {
            ids.update(new UUID[]{LOCAL, uuid});
            observe();
        }
    }

    @Test void schedulerApprovalAndCriticalsOrGuiVetoDoNotSpendRecovery() {
        final Fixture f = new Fixture();
        assertTrue(f.permits(A));
        f.module.noteScheduledOriginalClick(A); // legacy no-op by design
        assertTrue(f.permits(A));
        f.observe(); // next scheduler callback without a controller attack
        assertTrue(f.permits(A));
        f.module.noteScheduledOriginalClick(A);
        assertTrue(f.permits(A));
    }

    @Test void exactGenuineControllerArgumentBeginsOneHurtRecoveryCycle() {
        final Fixture f = new Fixture();
        final long e = f.epoch();
        assertTrue(f.genuine(A, A, e));
        assertFalse(f.permits(A)); // WAIT_FOR_HURT
        assertFalse(f.genuine(A, A, e)); // same controller cannot double-spend
        f.hurt(11); // real observed hurtTime=5, above max=1
        assertFalse(f.permits(A)); // WAIT_FOR_READY
        f.hurt(3); // real observed hurtTime=1, within configured window
        assertTrue(f.permits(A)); // READY after actual rise and recovery
        assertTrue(f.genuine(A, A, f.epoch()));
        assertFalse(f.permits(A));
    }

    @Test void unrelatedManualControllerOrChangedRealEntityUuidCannotSpend() {
        final Fixture f = new Fixture();
        final long e = f.epoch();
        assertFalse(f.genuine(A, B, e));
        assertFalse(f.genuine(null, A, e));
        assertFalse(f.genuine(B, B, e));
        assertTrue(f.permits(A));
        assertTrue(f.genuine(A, A, e));
    }

    @Test void secondOriginalPlayerCannotBorrowFirstRecoveryLatch() {
        final Fixture f = new Fixture();
        assertTrue(f.genuine(A, A, f.epoch()));
        f.target(B);
        assertTrue(f.permits(B)); // new original UUID starts fresh
        assertFalse(f.permits(A)); // B now occupies index 1
        assertTrue(f.genuine(B, B, f.epoch()));
        f.target(A);
        assertFalse(f.permits(A)); // previous A still WAIT_FOR_HURT
        f.hurt(11);
        f.hurt(3);
        assertTrue(f.permits(A));
        f.target(B);
        assertFalse(f.permits(B)); // B has not observed own damage
    }

    @Test void offOnGuiOrSourceRevokeInvalidatesOldControllerIntent() {
        final Fixture f = new Fixture();
        final long e = f.epoch();
        f.module.onDisable();
        f.module.onEnable();
        f.observe();
        assertFalse(f.genuine(A, A, e));
        assertTrue(f.permits(A));
        final long guiEpoch = f.epoch();
        f.module.suspendForGui();
        f.observe();
        assertFalse(f.genuine(A, A, guiEpoch));
        assertTrue(f.permits(A));
        final long sourceEpoch = f.epoch();
        f.module.suspendForSource();
        f.observe();
        assertFalse(f.genuine(A, A, sourceEpoch));
    }

    @Test void originalHurtWindowChangedAfterNativeIntentCannotSpend() {
        final Fixture f = new Fixture();
        final long epoch = f.epoch();
        f.module.maxTargetHurtTicksSetting().set(4);
        assertFalse(f.module.noteOriginalNativeControllerAttack(
                A, A, epoch, 0, 1));
        f.observe();
        assertTrue(f.permits(A));
        assertTrue(f.genuine(A, A, f.epoch()));
        assertFalse(f.permits(A));
    }

    @Test void realControllerWithoutObservedHurtCannotClaimRecoveredAttack() {
        final Fixture f = new Fixture();
        assertTrue(f.genuine(A, A, f.epoch()));
        for (int i = 0; i < 10; ++i) {
            f.hurt(1); // real observed hurtTime=0 every tick
            assertFalse(f.permits(A));
        }
        // Do not manufacture server damage or claim a recovered hit.
        assertFalse(f.genuine(A, A, f.epoch()));
    }
}
