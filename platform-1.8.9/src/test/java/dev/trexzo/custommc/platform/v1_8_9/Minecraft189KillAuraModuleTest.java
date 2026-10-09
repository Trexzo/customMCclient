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
            assertFalse(aura.shouldClick(true, angle.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false));
            angle.update(0.0F, 0.0F);
            assertTrue(aura.aim(player, angle.snapshot(), target.snapshot(),
                    false, movement.snapshot()));
            assertFalse(aura.shouldClick(false, angle.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false));
            assertTrue(aura.shouldClick(true, angle.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false));
            assertFalse(aura.shouldClick(true, angle.snapshot(), target.snapshot(),
                    false, true, movement.snapshot(), false));
            assertTrue(aura.shouldClick(true, angle.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false));
        } finally {
            aura.onDisable();
        }
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
            assertFalse(aura.shouldClick(true, angle.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false));
            movement.update(true, true, true);
            assertFalse(aura.shouldClick(true, angle.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false));
            movement.update(true, false, true);
            assertFalse(aura.shouldClick(true, angle.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false));
            assertTrue(aura.shouldClick(true, angle.snapshot(), target.snapshot(),
                    false, false, movement.snapshot(), false));
            aura.suspend();
            assertFalse(aura.shouldClick(true, angle.snapshot(), target.snapshot(),
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
