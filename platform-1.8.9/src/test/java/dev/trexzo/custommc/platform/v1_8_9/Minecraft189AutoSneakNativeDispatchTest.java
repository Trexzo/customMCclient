package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189AutoSneakNativeDispatchTest {
    private static Minecraft189PlayerMovementState.Snapshot snapshot(
            final boolean ground, final boolean sneaking,
            final boolean sprinting) {
        final Minecraft189PlayerMovementState state =
                new Minecraft189PlayerMovementState();
        state.update(ground, sneaking, sprinting);
        return state.snapshot();
    }

    private static final class NativePlayer
            implements Minecraft189PlayerSneakControl {
        int calls;
        boolean sneaking;
        boolean throwAfterWrite;
        Runnable onNativeWrite;

        @Override
        public void customMcSetSneaking(final boolean value) {
            calls++;
            if (calls > 8) {
                throw new AssertionError("recursive native sneak setter");
            }
            if (onNativeWrite != null) onNativeWrite.run();
            sneaking = value;
            if (throwAfterWrite) {
                throw new IllegalStateException("mapped native setter failed");
            }
        }
    }

    @Test
    void genuineEnabledMovementMakesOneNativeSneakWrite() {
        final Minecraft189AutoSneakModule module =
                new Minecraft189AutoSneakModule();
        final NativePlayer player = new NativePlayer();
        module.onEnable();
        module.apply(player, snapshot(true, false, false),
                true, true, false);
        assertEquals(1, player.calls);
        assertTrue(player.sneaking);
        module.apply(player, snapshot(true, true, false),
                true, true, false);
        assertEquals(1, player.calls);
    }

    @Test
    void reentrantOriginalSetterCannotWriteSamePlayerTwice() {
        final Minecraft189AutoSneakModule module =
                new Minecraft189AutoSneakModule();
        final NativePlayer player = new NativePlayer();
        final Minecraft189PlayerMovementState.Snapshot original =
                snapshot(true, false, false);
        module.onEnable();
        player.onNativeWrite = () -> module.apply(
                player, original, true, true, false);
        module.apply(player, original, true, true, false);
        assertEquals(1, player.calls);
        assertTrue(player.sneaking);
    }

    @Test
    void reentrantCallbackCannotSneakDifferentNativePlayer() {
        final Minecraft189AutoSneakModule module =
                new Minecraft189AutoSneakModule();
        final NativePlayer playerA = new NativePlayer();
        final NativePlayer playerB = new NativePlayer();
        final Minecraft189PlayerMovementState.Snapshot original =
                snapshot(true, false, false);
        module.onEnable();
        playerA.onNativeWrite = () -> module.apply(
                playerB, original, true, true, false);
        module.apply(playerA, original, true, true, false);
        assertEquals(1, playerA.calls);
        assertEquals(0, playerB.calls);
        assertFalse(playerB.sneaking);
    }

    @Test
    void nativeCallbackOffOnDoesNotBypassSameStackGuard() {
        final Minecraft189AutoSneakModule module =
                new Minecraft189AutoSneakModule();
        final NativePlayer player = new NativePlayer();
        final Minecraft189PlayerMovementState.Snapshot original =
                snapshot(true, false, false);
        module.onEnable();
        player.onNativeWrite = () -> {
            module.onDisable();
            module.onEnable();
            module.apply(player, original, true, true, false);
        };
        module.apply(player, original, true, true, false);
        assertEquals(1, player.calls);
        assertTrue(module.active());
        assertTrue(player.sneaking);
    }

    @Test
    void nativeSettingEditWithinCallbackCannotReplaySneak() {
        final Minecraft189AutoSneakModule module =
                new Minecraft189AutoSneakModule();
        final NativePlayer player = new NativePlayer();
        final Minecraft189PlayerMovementState.Snapshot original =
                snapshot(true, false, false);
        module.requireForwardSetting().set(Boolean.TRUE);
        module.onEnable();
        player.onNativeWrite = () -> {
            module.requireForwardSetting().set(Boolean.FALSE);
            module.apply(player, original, false, false, false);
        };
        module.apply(player, original, true, true, false);
        assertEquals(1, player.calls);
        assertFalse(module.requireForwardSetting().get());
    }

    @Test
    void thrownNativeSetterReleasesGuardForLaterRealCallback() {
        final Minecraft189AutoSneakModule module =
                new Minecraft189AutoSneakModule();
        final NativePlayer player = new NativePlayer();
        final Minecraft189PlayerMovementState.Snapshot original =
                snapshot(true, false, false);
        module.onEnable();
        player.throwAfterWrite = true;
        assertThrows(IllegalStateException.class,
                () -> module.apply(player, original, true, true, false));
        assertEquals(1, player.calls);
        assertTrue(player.sneaking); // Setter changed real bit before throw.
        player.throwAfterWrite = false;
        player.sneaking = false; // External vanilla state change.
        module.apply(player, original, true, true, false);
        assertEquals(2, player.calls);
        assertTrue(player.sneaking);
    }

    @Test
    void thrownReentrantSetterStillClearsGuardWithoutUnsneak() {
        final Minecraft189AutoSneakModule module =
                new Minecraft189AutoSneakModule();
        final NativePlayer player = new NativePlayer();
        final Minecraft189PlayerMovementState.Snapshot original =
                snapshot(true, false, false);
        module.onEnable();
        player.throwAfterWrite = true;
        player.onNativeWrite = () -> module.apply(
                player, original, true, true, false);
        assertThrows(IllegalStateException.class,
                () -> module.apply(player, original, true, true, false));
        assertEquals(1, player.calls);
        assertTrue(player.sneaking);
        player.onNativeWrite = null;
        player.throwAfterWrite = false;
        player.sneaking = false;
        module.apply(player, original, true, true, false);
        assertEquals(2, player.calls);
    }

    @Test
    void originalClickGuiFocusNeverDispatchesNativeSneak() {
        final Minecraft189AutoSneakModule module =
                new Minecraft189AutoSneakModule();
        final NativePlayer player = new NativePlayer();
        module.onEnable();
        module.apply(player, snapshot(true, false, false),
                true, true, true);
        assertEquals(0, player.calls);
        assertFalse(player.sneaking);
    }

    @Test
    void missingMappedMovementNeverInventsSourceSneak() {
        final Minecraft189AutoSneakModule module =
                new Minecraft189AutoSneakModule();
        final NativePlayer player = new NativePlayer();
        module.onEnable();
        final Minecraft189PlayerMovementState unavailable =
                new Minecraft189PlayerMovementState();
        module.apply(player, unavailable.snapshot(),
                true, true, false);
        assertEquals(0, player.calls);
    }

    @Test
    void physicalSneakAlreadyActiveNeverForcesAnotherWrite() {
        final Minecraft189AutoSneakModule module =
                new Minecraft189AutoSneakModule();
        final NativePlayer player = new NativePlayer();
        player.sneaking = true;
        module.onEnable();
        module.apply(player, snapshot(true, true, false),
                true, true, false);
        assertEquals(0, player.calls);
        assertTrue(player.sneaking);
    }

    @Test
    void allFourOptInPoliciesRetainOriginalPlayerAuthority() {
        final Minecraft189AutoSneakModule module =
                new Minecraft189AutoSneakModule();
        final NativePlayer player = new NativePlayer();
        module.onEnable();
        module.groundOnlySetting().set(Boolean.TRUE);
        module.apply(player, snapshot(false, false, false),
                true, true, false);
        module.groundOnlySetting().set(Boolean.FALSE);
        module.pauseSprintingSetting().set(Boolean.TRUE);
        module.apply(player, snapshot(true, false, true),
                true, true, false);
        module.pauseSprintingSetting().set(Boolean.FALSE);
        module.requireMovementSetting().set(Boolean.TRUE);
        module.apply(player, snapshot(true, false, false),
                false, false, false);
        module.requireMovementSetting().set(Boolean.FALSE);
        module.requireForwardSetting().set(Boolean.TRUE);
        module.apply(player, snapshot(true, false, false),
                false, true, false);
        assertEquals(0, player.calls);
        assertFalse(player.sneaking);
        module.apply(player, snapshot(true, false, false),
                true, true, false);
        assertEquals(1, player.calls);
    }

    @Test
    void disabledModuleNeverStartsAnotherNativeWrite() {
        final Minecraft189AutoSneakModule module =
                new Minecraft189AutoSneakModule();
        final NativePlayer player = new NativePlayer();
        module.onEnable();
        module.onDisable();
        module.apply(player, snapshot(true, false, false),
                true, true, false);
        assertEquals(0, player.calls);
    }

    @Test
    void compatibilityOverloadDoesNotInventForwardKey() {
        final Minecraft189AutoSneakModule module =
                new Minecraft189AutoSneakModule();
        final NativePlayer player = new NativePlayer();
        module.onEnable();
        module.requireForwardSetting().set(Boolean.TRUE);
        module.apply(player, snapshot(true, false, false), true);
        assertEquals(0, player.calls);
    }
}
