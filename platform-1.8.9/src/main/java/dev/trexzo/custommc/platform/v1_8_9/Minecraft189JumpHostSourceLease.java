package dev.trexzo.custommc.platform.v1_8_9;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * Single original native EntityPlayer.jump() stack owner across JumpReset,
 * LongJump, HighJump, LowHop, BunnyHop, AutoJump and AirJump.
 *
 * Per-module guards don't stop one mapped jump() reentering the host and
 * spending a DIFFERENT movement module's saved source edge. A different
 * original player or newly focused ClickGUI vetoes all remaining jumps.
 */
final class Minecraft189JumpHostSourceLease {
    private boolean dispatching;
    private Minecraft189PlayerJumpControl source;
    private boolean guiInitiallyFocused;
    private boolean revoked;

    boolean enter(final Minecraft189PlayerJumpControl player,
            final boolean guiFocused) {
        if (dispatching) {
            if (source != player || (!guiInitiallyFocused && guiFocused)) {
                revoked = true;
            }
            return false;
        }
        dispatching = true;
        source = player;
        guiInitiallyFocused = guiFocused;
        revoked = false;
        return true;
    }

    boolean stillOwned(final boolean guiFocused) {
        if (!dispatching) {
            return false;
        }
        if (!guiInitiallyFocused && guiFocused) {
            revoked = true;
        }
        return !revoked;
    }

    Minecraft189PlayerJumpControl guardedPlayer(
            final Minecraft189PlayerJumpControl player,
            final BooleanSupplier actualGuiFocused) {
        if (player == null) {
            return null;
        }
        Objects.requireNonNull(actualGuiFocused, "actualGuiFocused");
        return () -> {
            if (stillOwned(actualGuiFocused.getAsBoolean())) {
                player.customMcJump();
            }
        };
    }

    void exit() {
        dispatching = false;
        source = null;
        guiInitiallyFocused = false;
        revoked = false;
    }
}
