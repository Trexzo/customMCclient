package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189KillAuraModuleTest {
    @Test
    void tracksBoundedYawAndClicksOnlyWithVanillaRayConfirmation() {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        final Minecraft189TargetRotationState target = targetAtTwoBlocks();
        final Minecraft189PlayerRotationState angle = new Minecraft189PlayerRotationState();
        final Minecraft189PlayerMovementState movement = new Minecraft189PlayerMovementState();
        final RotationPlayer player = new RotationPlayer();
        angle.update(30.0F, 15.0F);
        movement.update(true, false, true);
        aura.minCpsSetting().set(20);
        aura.maxCpsSetting().set(20);
        aura.onEnable();
        try {
            assertTrue(aura.aim(player, angle.snapshot(), target.snapshot(),
                    false, movement.snapshot()));
            assertEquals(20.0F, player.yaw, 0.00001F);
            assertEquals(5.0F, player.pitch, 0.00001F);
            assertFalse(aura.shouldClick(true, 0, angle.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false));
            angle.update(0.0F, 0.0F);
            assertTrue(aura.aim(player, angle.snapshot(), target.snapshot(),
                    false, movement.snapshot()));
            assertFalse(aura.shouldClick(false, 0, angle.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false));
            assertFalse(aura.shouldClick(true, 1, angle.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false));
            assertTrue(aura.shouldClick(true, 0, angle.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false));
            assertFalse(aura.shouldClick(true, 0, angle.snapshot(), target.snapshot(),
                    false, true, movement.snapshot(), false));
            assertTrue(aura.shouldClick(true, 0, angle.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false));
        } finally {
            aura.onDisable();
        }
    }

    @Test
    void aimingKeepsCurrentTickValidatedLockButSuspendClearsIt() {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        final Minecraft189TargetRotationState target = targetAtTwoBlocks();
        final Minecraft189PlayerRotationState rotation =
                new Minecraft189PlayerRotationState();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        rotation.update(0, 0);
        movement.update(true, false, true);
        aura.lockTargetSetting().set(true);
        aura.onEnable();
        try {
            final java.util.UUID id = java.util.UUID.randomUUID();
            aura.rememberSelectedTarget(id);
            assertEquals(id, aura.lockedTargetUuid());
            assertTrue(aura.aim(new RotationPlayer(), rotation.snapshot(),
                    target.snapshot(), false, movement.snapshot()));
            assertEquals(id, aura.lockedTargetUuid());
            aura.suspend();
            assertNull(aura.lockedTargetUuid());
        } finally {
            aura.onDisable();
        }
    }

    @Test
    void v45RejectedClickDropsCreditButKeepsGenuineUuidLockUntilDisable() {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        final Minecraft189TargetRotationState target = targetAtTwoBlocks();
        final Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        final Minecraft189PlayerMovementState movement = new Minecraft189PlayerMovementState();
        final RotationPlayer player = new RotationPlayer();
        final java.util.UUID genuineB = java.util.UUID.fromString(
                "9c6b2c0b-a653-3300-9829-2617908fae14");
        rotation.update(0, 0);
        movement.update(true, false, false);
        aura.minCpsSetting().set(10);
        aura.maxCpsSetting().set(10);
        aura.lockTargetSetting().set(Boolean.TRUE);
        aura.onEnable();
        try {
            aura.rememberSelectedTarget(genuineB);
            assertTrue(aura.aim(player, rotation.snapshot(), target.snapshot(),
                    false, movement.snapshot()));
            assertEquals(genuineB, aura.lockedTargetUuid());
            assertFalse(aura.shouldClick(true, 0, rotation.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false),
                    "first half-tick of 10 CPS earns no click");
            // V44's unconditional empty-ray check must not call suspend():
            // one missing native ray may not invalidate a genuine B lock.
            assertFalse(aura.shouldClick(false, -1, rotation.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false));
            assertEquals(genuineB, aura.lockedTargetUuid());
            assertFalse(aura.shouldClick(true, 0, rotation.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false),
                    "empty ray drops scheduler credit");
            // Actual crosshair candidate rejected by independent source-FOV
            // gate: invalidate pacing, NOT a still-source-verified UUID lock.
            aura.vetoSyntheticClick();
            assertEquals(genuineB, aura.lockedTargetUuid(),
                    "FOV-veto cannot erase source-verified B ownership");
            assertFalse(aura.shouldClick(true, 0, rotation.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false),
                    "FOV veto must not bank a delayed automatic click");
            assertTrue(aura.shouldClick(true, 0, rotation.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false),
                    "live native confirmation with valid geometry may recover");
            aura.suspend();
            assertNull(aura.lockedTargetUuid(),
                    "actual GUI/World/module suspension must still clear owner");
        } finally {
            aura.onDisable();
        }
    }

    @Test
    void genuineGuiPauseRetainsOnlyStickyUuidNotSchedulerCreditOrIndex() {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        final Minecraft189TargetRotationState target = targetAtTwoBlocks();
        final Minecraft189PlayerRotationState rotation =
                new Minecraft189PlayerRotationState();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        rotation.update(0.0F, 0.0F);
        movement.update(true, false, true);
        aura.lockTargetSetting().set(true);
        aura.minCpsSetting().set(10);
        aura.maxCpsSetting().set(10);
        aura.onEnable();
        final java.util.UUID survivorB = java.util.UUID.fromString(
                "9c6b2c0b-a653-3300-9829-2617908fae14");
        try {
            aura.rememberSelectedTarget(survivorB);
            assertTrue(aura.aim(new RotationPlayer(), rotation.snapshot(),
                    target.snapshot(), false, movement.snapshot()));
            assertFalse(aura.shouldClick(true, 0, rotation.snapshot(),
                    target.snapshot(), false, false, movement.snapshot(), false));
            aura.suspendForGui();
            assertEquals(survivorB, aura.lockedTargetUuid());
            // Cached index/aim must not authorize a synthetic click on
            // focus return, even if the original index happens to match.
            assertFalse(aura.shouldClick(true, 0, rotation.snapshot(),
                    target.snapshot(), false, false, movement.snapshot(), false));
            // A fresh source selector must revalidate B before original aim
            // and CPS credit are allowed to resume.
            assertTrue(aura.aim(new RotationPlayer(), rotation.snapshot(),
                    target.snapshot(), false, movement.snapshot()));
            assertFalse(aura.shouldClick(true, 0, rotation.snapshot(),
                    target.snapshot(), false, false, movement.snapshot(), false));
            assertTrue(aura.shouldClick(true, 0, rotation.snapshot(),
                    target.snapshot(), false, false, movement.snapshot(), false));
            aura.suspend();
            assertNull(aura.lockedTargetUuid(),
                    "genuine original-player/world-owner loss remains a hard reset");
        } finally {
            aura.onDisable();
        }
    }

    @Test
    void disabledAuraCannotRetainGuiPausedUuid() {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        aura.lockTargetSetting().set(true);
        aura.onEnable();
        aura.rememberSelectedTarget(java.util.UUID.randomUUID());
        assertNotNull(aura.lockedTargetUuid());
        aura.suspendForGui();
        assertNotNull(aura.lockedTargetUuid());
        aura.lockTargetSetting().set(false);
        assertNull(aura.lockedTargetUuid());
        aura.onDisable();
        aura.suspendForGui();
        assertNull(aura.lockedTargetUuid());
    }

    @Test
    void movementAndRangeGatesFailClosedAndDoNotBankClicks() {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        final Minecraft189TargetRotationState target = targetAtTwoBlocks();
        final Minecraft189PlayerRotationState angle = new Minecraft189PlayerRotationState();
        final Minecraft189PlayerMovementState movement = new Minecraft189PlayerMovementState();
        final RotationPlayer player = new RotationPlayer();
        angle.update(0, 0);
        movement.update(true, false, true);
        aura.minCpsSetting().set(10);
        aura.maxCpsSetting().set(10);
        aura.onEnable();
        try {
            aura.rangeSetting().set(1.5D);
            assertFalse(aura.aim(player, angle.snapshot(), target.snapshot(),
                    false, movement.snapshot()));
            aura.rangeSetting().set(3.0D);
            assertTrue(aura.aim(player, angle.snapshot(), target.snapshot(),
                    false, movement.snapshot()));
            assertFalse(aura.shouldClick(true, 0, angle.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false));
            movement.update(true, true, true);
            assertFalse(aura.shouldClick(true, 0, angle.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false));
            movement.update(true, false, true);
            assertFalse(aura.shouldClick(true, 0, angle.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false));
            assertTrue(aura.shouldClick(true, 0, angle.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false));
            aura.suspend();
            assertFalse(aura.shouldClick(true, 0, angle.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false));
        } finally {
            aura.onDisable();
        }
    }

    @Test
    void settingsAreRegisteredPersistentAndRemoved() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final Minecraft189KillAuraFeature feature = Minecraft189KillAuraFeature.install(
                modules, controller, new ModulePresentationRegistry(),
                new ModuleSettingRegistry(modules, settings),
                settings, new SettingPresentationRegistry());
        try {
            feature.module().rangeSetting().set(4.5D);
            assertEquals("4.5", settings.snapshotEncoded().get(
                    Minecraft189KillAuraModule.RANGE));
            assertThrows(IllegalArgumentException.class,
                    () -> feature.module().rangeSetting().set(9.0D));
            controller.enable(Minecraft189KillAuraModule.ID);
        } finally {
            feature.close();
        }
        assertNull(modules.find(Minecraft189KillAuraModule.ID));
        assertNull(settings.find(Minecraft189KillAuraModule.RANGE));
    }

    private static Minecraft189TargetRotationState targetAtTwoBlocks() {
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds = new Minecraft189WorldEntityKindState();
        final Minecraft189NearestPlayerTargetState selected =
                new Minecraft189NearestPlayerTargetState();
        final Minecraft189TargetRotationState target = new Minecraft189TargetRotationState();
        local.update(0, 0, 0);
        positions.update(new double[]{0, 0, 2});
        kinds.update(new int[]{Minecraft189WorldEntityKindState.LIVING
                | Minecraft189WorldEntityKindState.PLAYER});
        selected.update(local.snapshot(), positions.snapshot(), kinds.snapshot());
        target.update(local.snapshot(), selected.snapshot());
        return target;
    }

    private static final class RotationPlayer implements Minecraft189PlayerRotationControl {
        private float yaw;
        private float pitch;
        @Override public float customMcRotationYaw() { return yaw; }
        @Override public float customMcRotationPitch() { return pitch; }
        @Override public void customMcSetRotationYaw(float v) { yaw = v; }
        @Override public void customMcSetRotationPitch(float v) { pitch = v; }
    }
}
