package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * V282 source-level investigation of native locked-target phase-2 failure.
 * Direct real selector/module logic only; never a simulated Mojang PASS.
 */
final class Minecraft189StickyUuidSourceTransitionTest {
    private static final UUID A = UUID.fromString(
            "11111111-1111-1111-1111-111111111111");
    private static final UUID B = UUID.fromString(
            "22222222-2222-2222-2222-222222222222");

    private static final class Source {
        final Minecraft189KillAuraModule aura =
                new Minecraft189KillAuraModule();
        final Minecraft189PlayerPositionState local =
                new Minecraft189PlayerPositionState();
        final Minecraft189PlayerRotationState rotation =
                new Minecraft189PlayerRotationState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        final Minecraft189WorldEntityCombatState combat =
                new Minecraft189WorldEntityCombatState();
        final Minecraft189WorldEntityUuidState identities =
                new Minecraft189WorldEntityUuidState();
        final Minecraft189NearestPlayerTargetState selected =
                new Minecraft189NearestPlayerTargetState();

        Source() {
            aura.lockTargetSetting().set(true);
            aura.onEnable();
            local.update(0, 0, 0);
            rotation.update(0, 0);
            positions.update(new double[]{0, 0, 2, 0, 0, 3});
            kinds.update(new int[]{3, 3});
            combat.update(new int[]{1, 1});
            identities.update(new UUID[]{A, B});
        }

        void positions(final double az, final double bz) {
            positions.update(new double[]{0, 0, az, 0, 0, bz});
        }

        void select() {
            Minecraft189KillAuraTargetSelector.select(
                    aura, local.snapshot(), rotation.snapshot(),
                    positions.snapshot(), kinds.snapshot(), combat.snapshot(),
                    null, null, identities.snapshot(), selected);
        }

        void expect(final int index, final UUID uuid) {
            assertTrue(selected.snapshot().found());
            assertEquals(index, selected.snapshot().entityIndex());
            assertEquals(uuid, aura.lockedTargetUuid());
        }
    }

    @Test void genuineAStaysLockedAfterBBecomesMuchCloserInReach() {
        final Source s = new Source();
        s.select();
        s.expect(0, A);
        s.positions(2.0D, 1.2D);
        for (int i = 0; i < 30; i++) {
            s.select();
            s.expect(0, A);
        }
    }

    @Test void changingLoadedEntityListOrderCannotConvertALockToB() {
        final Source s = new Source();
        s.select();
        s.expect(0, A);
        s.positions.update(new double[]{0, 0, 1.2, 0, 0, 2.0});
        s.identities.update(new UUID[]{B, A});
        for (int i = 0; i < 20; i++) {
            s.select();
            s.expect(1, A);
        }
    }

    @Test void bOnlyTakesOverAfterOriginalAExceedsActualReach() {
        final Source s = new Source();
        s.select();
        s.expect(0, A);
        s.positions(3.5D, 1.8D);
        s.select();
        s.expect(0, A);
        s.positions(3.50001D, 1.8D);
        s.select();
        s.expect(1, B);
    }

    @Test void originalADeathMakesBEligibleWithoutReusingAIndex() {
        final Source s = new Source();
        s.select();
        s.expect(0, A);
        s.positions(2.0D, 1.2D);
        s.combat.update(new int[]{0, 1});
        s.select();
        s.expect(1, B);
    }

    @Test void disqualifiedAOutsideCurrentFovReleasesLockToB() {
        final Source s = new Source();
        s.select();
        s.expect(0, A);
        // A moves due east, outside a strict 45-degree FOV;
        // B stays directly ahead and inside default reach.
        s.aura.fovSetting().set(45.0D);
        s.positions.update(new double[]{2, 0, 0, 0, 0, 1.5});
        s.select();
        s.expect(1, B);
    }

    @Test void missingIdentitySnapshotRevokesLockRatherThanGuessing() {
        final Source s = new Source();
        s.select();
        s.expect(0, A);
        s.identities.clear();
        s.select();
        assertFalse(s.selected.snapshot().available());
        assertNull(s.aura.lockedTargetUuid());
        s.positions(2.0D, 1.2D);
        s.identities.update(new UUID[]{A, B});
        s.select();
        s.expect(1, B);
    }

    @Test void misalignedNativeIdentityListCannotPreserveStaleLock() {
        final Source s = new Source();
        s.select();
        s.expect(0, A);
        s.identities.update(new UUID[]{A});
        s.select();
        assertFalse(s.selected.snapshot().available());
        assertNull(s.aura.lockedTargetUuid());
    }

    @Test void missingAimControlIsASourceRevocationNotALegitimateBPriorityProof() {
        final Source s = new Source();
        s.select();
        s.expect(0, A);
        assertFalse(s.aura.aim(null, s.rotation.snapshot(), null,
                false, null));
        assertNull(s.aura.lockedTargetUuid());
        // The B switch is allowed only after the module has explicitly
        // revoked the original lock; it does NOT satisfy native phase-2
        // original A-retention acceptance.
        s.positions(2.0D, 1.2D);
        s.select();
        s.expect(1, B);
    }

    @Test void disablingStickySettingReturnsToPlainNearestSelection() {
        final Source s = new Source();
        s.select();
        s.expect(0, A);
        s.aura.lockTargetSetting().set(false);
        s.positions(2.0D, 1.2D);
        s.select();
        assertEquals(1, s.selected.snapshot().entityIndex());
        assertNull(s.aura.lockedTargetUuid());
    }

    @Test void freshEmptyCombatSourceMustFailClosedUntilValidAgain() {
        final Source s = new Source();
        s.select();
        s.expect(0, A);
        s.combat.clear();
        s.select();
        assertFalse(s.selected.snapshot().available());
        assertNull(s.aura.lockedTargetUuid());
        s.combat.update(new int[]{1, 1});
        s.positions(2.0D, 1.2D);
        s.select();
        s.expect(1, B);
    }

    @Test void noScenarioFabricatesNativePassOrOriginalServerDamage() {
        final Source s = new Source();
        s.select();
        s.expect(0, A);
        assertFalse(s.selected.snapshot().toString()
                .contains("CUSTOMMC_OFFICIAL_189"));
    }
}
