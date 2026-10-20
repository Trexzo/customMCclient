package dev.trexzo.custommc.platform.v1_8_9;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * Owns one original EntityPlayerSP sprint host callback across WTap
 * and AutoSprint rather than only one module's mapped setter.
 */
final class Minecraft189SprintHostSourceLease {
    private boolean dispatching;
    private Minecraft189PlayerSprintControl original;
    private boolean guiAtEntry;
    private boolean revoked;

    boolean enter(final Minecraft189PlayerSprintControl player,
            final boolean guiFocused) {
        if (dispatching) {
            if (player != original || (!guiAtEntry && guiFocused)) {
                revoked = true;
            }
            return false;
        }
        dispatching = true;
        original = player;
        guiAtEntry = guiFocused;
        revoked = false;
        return true;
    }

    boolean stillOwned(final boolean guiFocused) {
        if (!dispatching) return false;
        if (!guiAtEntry && guiFocused) revoked = true;
        return !revoked;
    }

    Minecraft189PlayerSprintControl guardedPlayer(
            final Minecraft189PlayerSprintControl player,
            final BooleanSupplier currentGuiFocused) {
        if (player == null) return null;
        Objects.requireNonNull(currentGuiFocused, "currentGuiFocused");
        return sprinting -> {
            if (stillOwned(currentGuiFocused.getAsBoolean())) {
                player.customMcSetSprinting(sprinting);
            }
        };
    }

    void exit() {
        original = null;
        guiAtEntry = false;
        revoked = false;
        dispatching = false;
    }
}
