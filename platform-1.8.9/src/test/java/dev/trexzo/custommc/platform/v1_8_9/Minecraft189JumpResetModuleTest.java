package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189JumpResetModuleTest {
    @Test
    void partialNativeJumpExceptionStillCommitsCooldownBeforeAnyNextHurtEdge() {
        final Minecraft189JumpResetModule reset = new Minecraft189JumpResetModule();
        final Minecraft189PlayerMovementState movement = new Minecraft189PlayerMovementState();
        final Minecraft189PlayerHurtTimeState hurt = new Minecraft189PlayerHurtTimeState();
        final JumpCounter source = new JumpCounter();
        reset.cooldownSetting().set(3);
        reset.onEnable();
        try {
            movement.update(true, false, true);
            hurt.update(0);
            assertFalse(reset.apply(source, movement.snapshot(),
                    hurt.snapshot(), true, false));
            hurt.update(10);
            assertThrows(IllegalStateException.class,
                    () -> reset.apply(() -> {
                        source.jumps++;
                        throw new IllegalStateException(
                                "original Minecraft player jump failed after mutation");
                    }, movement.snapshot(), hurt.snapshot(), true, false));
            assertEquals(1, source.jumps,
                    "source jump can mutate real state before throwing");
            hurt.update(0);
            assertFalse(reset.apply(source, movement.snapshot(),
                    hurt.snapshot(), true, false));
            hurt.update(10);
            assertFalse(reset.apply(source, movement.snapshot(),
                    hurt.snapshot(), true, false),
                    "a new hurt edge while cooldown remains cannot replay");
            hurt.update(0);
            assertFalse(reset.apply(source, movement.snapshot(),
                    hurt.snapshot(), true, false));
            hurt.update(10);
            assertTrue(reset.apply(source, movement.snapshot(),
                    hurt.snapshot(), true, false),
                    "genuine new hurt after source cooldown is allowed");
            assertEquals(2, source.jumps);
        } finally { reset.onDisable(); }
    }

    @Test
    void reentrantOriginalJumpCannotSpendSecondFreshHurtEdgeEvenAtZeroCooldown() {
        final Minecraft189JumpResetModule reset = new Minecraft189JumpResetModule();
        final Minecraft189PlayerMovementState movement = new Minecraft189PlayerMovementState();
        final Minecraft189PlayerHurtTimeState hurt = new Minecraft189PlayerHurtTimeState();
        final JumpCounter source = new JumpCounter();
        reset.cooldownSetting().set(0);
        reset.onEnable();
        try {
            movement.update(true, false, true);
            hurt.update(0);
            reset.apply(source, movement.snapshot(), hurt.snapshot(), true, false);
            hurt.update(10);
            assertTrue(reset.apply(() -> {
                source.jumps++;
                hurt.update(11);
                assertFalse(reset.apply(source, movement.snapshot(),
                        hurt.snapshot(), true, false),
                        "original native method cannot dispatch another jump recursively");
            }, movement.snapshot(), hurt.snapshot(), true, false));
            assertEquals(1, source.jumps);
            assertFalse(reset.apply(source, movement.snapshot(),
                    hurt.snapshot(), true, false),
                    "nested real hurt edge must be consumed, never replayed");
            hurt.update(0);
            assertFalse(reset.apply(source, movement.snapshot(),
                    hurt.snapshot(), true, false));
            hurt.update(10);
            assertTrue(reset.apply(source, movement.snapshot(),
                    hurt.snapshot(), true, false),
                    "independent later hurt event remains valid at configured zero cooldown");
            assertEquals(2, source.jumps);
        } finally { reset.onDisable(); }
    }

    @Test
    void reentrantOriginalJumpThrowCannotLeaveModulePermanentlyBusy() {
        final Minecraft189JumpResetModule reset = new Minecraft189JumpResetModule();
        final Minecraft189PlayerMovementState movement = new Minecraft189PlayerMovementState();
        final Minecraft189PlayerHurtTimeState hurt = new Minecraft189PlayerHurtTimeState();
        final JumpCounter source = new JumpCounter();
        reset.cooldownSetting().set(0);
        reset.onEnable();
        try {
            movement.update(true, false, false);
            hurt.update(0);
            reset.apply(source, movement.snapshot(), hurt.snapshot(), true, false);
            hurt.update(10);
            assertThrows(IllegalStateException.class, () ->
                    reset.apply(() -> {
                        source.jumps++;
                        hurt.update(11);
                        assertFalse(reset.apply(source, movement.snapshot(),
                                hurt.snapshot(), true, false));
                        throw new IllegalStateException("native jump threw");
                    }, movement.snapshot(), hurt.snapshot(), true, false));
            assertEquals(1, source.jumps);
            assertFalse(reset.apply(source, movement.snapshot(),
                    hurt.snapshot(), true, false));
            hurt.update(0);
            reset.apply(source, movement.snapshot(), hurt.snapshot(), true, false);
            hurt.update(10);
            assertTrue(reset.apply(source, movement.snapshot(),
                    hurt.snapshot(), true, false),
                    "in-progress guard clears after original source exception");
            assertEquals(2, source.jumps);
        } finally { reset.onDisable(); }
    }

    @Test
    void oneJumpPerFreshLocalHurtEdgeWithCooldown() {
        final Minecraft189JumpResetModule module = new Minecraft189JumpResetModule();
        final Minecraft189PlayerMovementState movement = new Minecraft189PlayerMovementState();
        final Minecraft189PlayerHurtTimeState hurt = new Minecraft189PlayerHurtTimeState();
        final JumpCounter player = new JumpCounter();
        module.onEnable();
        movement.update(true, false, true);
        hurt.update(0);
        assertFalse(module.apply(player, movement.snapshot(), hurt.snapshot(), true, false));
        hurt.update(10);
        assertTrue(module.apply(player, movement.snapshot(), hurt.snapshot(), true, false));
        assertEquals(1, player.jumps);
        for (int n = 9; n >= 1; n--) {
            hurt.update(n);
            assertFalse(module.apply(player, movement.snapshot(), hurt.snapshot(), true, false));
        }
        hurt.update(10);
        assertTrue(module.apply(player, movement.snapshot(), hurt.snapshot(), true, false));
        assertEquals(2, player.jumps);
        module.onDisable();
    }

    @Test
    void suspensionAndFailedGatesConsumeTheEdgeWithoutReplay() {
        final Minecraft189JumpResetModule module = new Minecraft189JumpResetModule();
        final Minecraft189PlayerMovementState movement = new Minecraft189PlayerMovementState();
        final Minecraft189PlayerHurtTimeState hurt = new Minecraft189PlayerHurtTimeState();
        final JumpCounter player = new JumpCounter();
        module.cooldownSetting().set(0);
        module.onEnable();
        movement.update(true, false, true);
        hurt.update(0);
        module.apply(player, movement.snapshot(), hurt.snapshot(), true, false);
        hurt.update(10);
        assertFalse(module.apply(player, movement.snapshot(), hurt.snapshot(), true, true));
        assertFalse(module.apply(player, movement.snapshot(), hurt.snapshot(), true, false));
        hurt.update(0);
        module.apply(player, movement.snapshot(), hurt.snapshot(), true, false);
        movement.update(false, false, true);
        hurt.update(10);
        assertFalse(module.apply(player, movement.snapshot(), hurt.snapshot(), true, false));
        movement.update(true, false, true);
        assertFalse(module.apply(player, movement.snapshot(), hurt.snapshot(), true, false));
        hurt.update(0);
        module.apply(player, movement.snapshot(), hurt.snapshot(), true, false);
        hurt.update(10);
        assertFalse(module.apply(player, movement.snapshot(), hurt.snapshot(), false, false));
        assertFalse(module.apply(player, movement.snapshot(), hurt.snapshot(), true, false));
        hurt.update(0);
        module.apply(player, movement.snapshot(), hurt.snapshot(), true, false);
        movement.update(true, true, true);
        hurt.update(10);
        assertFalse(module.apply(player, movement.snapshot(), hurt.snapshot(), true, false));
        movement.update(true, false, true);
        assertFalse(module.apply(player, movement.snapshot(), hurt.snapshot(), true, false));
        assertEquals(0, player.jumps);
        module.onDisable();
    }

    @Test
    void zeroChanceAndSprintRequirementDoNotBankHitEvents() {
        final Minecraft189JumpResetModule module = new Minecraft189JumpResetModule();
        final Minecraft189PlayerMovementState movement = new Minecraft189PlayerMovementState();
        final Minecraft189PlayerHurtTimeState hurt = new Minecraft189PlayerHurtTimeState();
        final JumpCounter player = new JumpCounter();
        module.cooldownSetting().set(0);
        module.chanceSetting().set(0);
        module.onEnable();
        movement.update(true, false, true);
        hurt.update(0);
        module.apply(player, movement.snapshot(), hurt.snapshot(), true, false);
        hurt.update(10);
        assertFalse(module.apply(player, movement.snapshot(), hurt.snapshot(), true, false));
        module.chanceSetting().set(100);
        assertFalse(module.apply(player, movement.snapshot(), hurt.snapshot(), true, false));
        hurt.update(0);
        module.apply(player, movement.snapshot(), hurt.snapshot(), true, false);
        module.requireSprintSetting().set(true);
        movement.update(true, false, false);
        hurt.update(10);
        assertFalse(module.apply(player, movement.snapshot(), hurt.snapshot(), true, false));
        movement.update(true, false, true);
        assertFalse(module.apply(player, movement.snapshot(), hurt.snapshot(), true, false));
        hurt.update(0);
        module.apply(player, movement.snapshot(), hurt.snapshot(), true, false);
        hurt.update(10);
        assertTrue(module.apply(player, movement.snapshot(), hurt.snapshot(), true, false));
        assertEquals(1, player.jumps);
        module.onDisable();
    }

    @Test
    void settingsPersistAndFeatureCleansRegistrations() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final Minecraft189JumpResetFeature feature = Minecraft189JumpResetFeature.install(
                modules, controller, new ModulePresentationRegistry(),
                new ModuleSettingRegistry(modules, settings), settings,
                new SettingPresentationRegistry());
        try {
            assertEquals("5", settings.snapshotEncoded().get(
                    Minecraft189JumpResetModule.COOLDOWN_ID));
            feature.module().cooldownSetting().set(9);
            assertEquals("9", settings.snapshotEncoded().get(
                    Minecraft189JumpResetModule.COOLDOWN_ID));
            assertThrows(IllegalArgumentException.class,
                    () -> feature.module().cooldownSetting().set(21));
            feature.module().chanceSetting().set(75);
            assertEquals("75", settings.snapshotEncoded().get(
                    Minecraft189JumpResetModule.CHANCE_ID));
            feature.module().requireSprintSetting().set(true);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189JumpResetModule.SPRINT_ID));
            controller.enable(Minecraft189JumpResetModule.ID);
        } finally {
            feature.close();
        }
        assertNull(settings.find(Minecraft189JumpResetModule.COOLDOWN_ID));
        assertNull(modules.find(Minecraft189JumpResetModule.ID));
    }

    private static final class JumpCounter implements Minecraft189PlayerJumpControl {
        private int jumps;
        @Override public void customMcJump() { jumps++; }
    }
}
