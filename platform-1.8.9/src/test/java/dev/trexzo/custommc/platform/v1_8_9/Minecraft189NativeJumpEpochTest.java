package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Offline source-adapter tests for original mapped jump()/motionY callbacks.
 * Real Minecraft server/world verification is separately required.
 */
final class Minecraft189NativeJumpEpochTest {
    private static final class NativePlayer
            implements Minecraft189PlayerJumpControl, Minecraft189PlayerMotionControl {
        int jumps, reads, yWrites;
        double motionY = 0.42D;
        Runnable onJump, onYRead;
        @Override public void customMcJump() {
            jumps++;
            if (onJump != null) {
                final Runnable effect = onJump;
                onJump = null;
                effect.run();
            }
        }
        @Override public double customMcMotionY() {
            reads++;
            final double result = motionY;
            if (onYRead != null) {
                final Runnable effect = onYRead;
                onYRead = null;
                effect.run();
            }
            return result;
        }
        @Override public void customMcSetMotionY(double value) {
            yWrites++;
            motionY = value;
        }
        @Override public double customMcMotionX() { return 0D; }
        @Override public double customMcMotionZ() { return 0D; }
        @Override public void customMcSetMotionX(double value) { }
        @Override public void customMcSetMotionZ(double value) { }
    }

    private static Minecraft189PlayerMovementState.Snapshot state(boolean ground) {
        final Minecraft189PlayerMovementState src = new Minecraft189PlayerMovementState();
        src.update(ground, false, false);
        return src.snapshot();
    }

    @Test void highJumpRestartDuringOriginalJumpCannotQueueOldBoost() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule module = new Minecraft189HighJumpModule(input);
        final NativePlayer player = new NativePlayer();
        module.onEnable();
        input.key(LegacyKeyboardCodes.SPACE, true);
        player.onJump = () -> { module.onDisable(); module.onEnable(); };
        assertFalse(module.applyJump(player, state(true), false));
        assertEquals(1, player.jumps); // Original jump already happened.
        assertFalse(module.applyMotion(player, false));
        assertEquals(0, player.reads);
        assertEquals(0, player.yWrites);
        module.onDisable();
    }

    @Test void highJumpDisableDuringOriginalJumpNeverAppliesPendingVelocity() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule module = new Minecraft189HighJumpModule(input);
        final NativePlayer player = new NativePlayer();
        module.onEnable();
        input.key(LegacyKeyboardCodes.SPACE, true);
        player.onJump = module::onDisable;
        assertFalse(module.applyJump(player, state(true), false));
        assertEquals(1, player.jumps);
        assertFalse(module.applyMotion(player, false));
        assertEquals(0, player.yWrites);
    }

    @Test void highJumpMidOriginalJumpPolicyEditCancelsStaleBoost() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule module = new Minecraft189HighJumpModule(input);
        final NativePlayer player = new NativePlayer();
        module.onEnable();
        input.key(LegacyKeyboardCodes.SPACE, true);
        player.onJump = () -> module.requireMovementSetting().set(Boolean.TRUE);
        assertFalse(module.applyJump(player, state(true), false));
        assertEquals(1, player.jumps);
        assertFalse(module.applyMotion(player, false));
        assertEquals(0, player.yWrites);
        module.onDisable();
    }

    @Test void highJumpGetterOffOnCannotSetStaleVerticalVelocity() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule module = new Minecraft189HighJumpModule(input);
        final NativePlayer player = new NativePlayer();
        module.onEnable();
        input.key(LegacyKeyboardCodes.SPACE, true);
        assertTrue(module.applyJump(player, state(true), false));
        player.onYRead = () -> { module.onDisable(); module.onEnable(); };
        assertFalse(module.applyMotion(player, false));
        assertEquals(1, player.reads);
        assertEquals(0, player.yWrites);
        assertFalse(module.applyMotion(player, false)); // No stale pending credit.
        module.onDisable();
    }

    @Test void highJumpGetterVerticalSpeedGuiEditRevokesOldBoost() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule module = new Minecraft189HighJumpModule(input);
        final NativePlayer player = new NativePlayer();
        module.onEnable();
        input.key(LegacyKeyboardCodes.SPACE, true);
        assertTrue(module.applyJump(player, state(true), false));
        player.onYRead = () -> module.verticalSpeedSetting().set(1.20D);
        assertFalse(module.applyMotion(player, false));
        assertEquals(0, player.yWrites);
        assertFalse(module.applyMotion(player, false));
        module.onDisable();
    }

    @Test void highJumpValidNativeJumpStillAppliesConfiguredMotionOnce() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule module = new Minecraft189HighJumpModule(input);
        final NativePlayer player = new NativePlayer();
        module.onEnable();
        module.verticalSpeedSetting().set(1.1D);
        input.key(LegacyKeyboardCodes.SPACE, true);
        assertTrue(module.applyJump(player, state(true), false));
        assertTrue(module.applyMotion(player, false));
        assertEquals(1.10D, player.motionY, 0.00000001D);
        assertEquals(1, player.yWrites);
        assertFalse(module.applyMotion(player, false));
        assertEquals(1, player.yWrites);
        module.onDisable();
    }

    @Test void originalJumpCallbackReleasesSpaceNewPressIsFreshHighJumpEdge() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule module = new Minecraft189HighJumpModule(input);
        final NativePlayer player = new NativePlayer();
        module.onEnable();
        input.key(LegacyKeyboardCodes.SPACE, true);
        player.onJump = () -> input.key(LegacyKeyboardCodes.SPACE, false);
        assertTrue(module.applyJump(player, state(true), false));
        input.key(LegacyKeyboardCodes.SPACE, true);
        assertTrue(module.applyJump(player, state(true), false));
        assertEquals(2, player.jumps);
        module.onDisable();
    }

    @Test void airJumpOriginalCallbackReleasesSpaceAllowsNextPhysicalPress() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189AirJumpModule module = new Minecraft189AirJumpModule(input);
        final NativePlayer player = new NativePlayer();
        module.onEnable();
        input.key(LegacyKeyboardCodes.SPACE, true);
        player.onJump = () -> input.key(LegacyKeyboardCodes.SPACE, false);
        module.apply(player, state(false));
        assertEquals(1, player.jumps);
        input.key(LegacyKeyboardCodes.SPACE, true);
        module.apply(player, state(false));
        assertEquals(2, player.jumps);
        module.onDisable();
    }

    @Test void airJumpReentrantDisableEnableConsumesOldOriginalSpace() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189AirJumpModule module = new Minecraft189AirJumpModule(input);
        final NativePlayer player = new NativePlayer();
        module.onEnable();
        input.key(LegacyKeyboardCodes.SPACE, true);
        player.onJump = () -> { module.onDisable(); module.onEnable(); };
        module.apply(player, state(false));
        module.apply(player, state(false));
        assertEquals(1, player.jumps);
        input.key(LegacyKeyboardCodes.SPACE, false);
        module.apply(player, state(false));
        input.key(LegacyKeyboardCodes.SPACE, true);
        module.apply(player, state(false));
        assertEquals(2, player.jumps);
        module.onDisable();
    }

    @Test void airJumpOriginalCallbackCannotClobberNewOwnerPhysicalKey() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189AirJumpModule module = new Minecraft189AirJumpModule(input);
        final NativePlayer player = new NativePlayer();
        module.onEnable();
        input.key(LegacyKeyboardCodes.SPACE, true);
        player.onJump = () -> {
            module.onDisable();
            input.key(LegacyKeyboardCodes.SPACE, false);
            module.onEnable(); // Fresh owner samples actual RELEASED key.
            input.key(LegacyKeyboardCodes.SPACE, true);
        };
        module.apply(player, state(false));
        // New physically pressed Space AFTER re-enable belongs to the new
        // activation. The old callback must not erase its source edge.
        module.apply(player, state(false));
        assertEquals(2, player.jumps);
        module.onDisable();
    }

    @Test void guiFocusStillSwallowsOriginalAirJumpSpaceWithoutReplay() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189AirJumpModule module = new Minecraft189AirJumpModule(input);
        final NativePlayer player = new NativePlayer();
        module.onEnable();
        input.key(LegacyKeyboardCodes.SPACE, true);
        module.apply(player, state(false), true);
        module.apply(player, state(false), false);
        assertEquals(0, player.jumps);
        input.key(LegacyKeyboardCodes.SPACE, false);
        module.apply(player, state(false), false);
        input.key(LegacyKeyboardCodes.SPACE, true);
        module.apply(player, state(false), false);
        assertEquals(1, player.jumps);
        module.onDisable();
    }
}
