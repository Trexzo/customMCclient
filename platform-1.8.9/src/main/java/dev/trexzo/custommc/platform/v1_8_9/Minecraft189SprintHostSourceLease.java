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
    // A guarded setter captured during the previous native callback
    // cannot silently become authorized by a later callback with the
    // same or different original player reference.
    private long generation;

    boolean enter(final Minecraft189PlayerSprintControl player,
            final boolean guiFocused) {
        if (dispatching) {
            if (player != original || (!guiAtEntry && guiFocused)) {
                revoked = true;
            }
            return false;
        }
        ++generation;
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
        final long token = generation;
        return sprinting -> {
            if (token == generation && original == player
                    && stillOwned(currentGuiFocused.getAsBoolean())) {
                player.customMcSetSprinting(sprinting);
            }
        };
    }

    void exit() {
        ++generation;
        original = null;
        guiAtEntry = false;
        revoked = false;
        dispatching = false;
    }
}
