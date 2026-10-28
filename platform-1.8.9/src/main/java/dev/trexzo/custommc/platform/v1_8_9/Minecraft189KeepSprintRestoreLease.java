package dev.trexzo.custommc.platform.v1_8_9;

/**
 * One-shot original-player KeepSprint restore scope, separate from the
 * physical LMB/WTap/AutoSprint host callback.
 *
 * A native getter may reenter synthetic combat, change the GUI, or start a
 * different original player callback. Any such transition revokes the old
 * restore rather than allowing a stale setSprinting(true) later.
 *
 * This lease never synthesizes a player, sprint bit, packet, or click.
 */
final class Minecraft189KeepSprintRestoreLease {
    private boolean restoring;
    private boolean revoked;
    private Minecraft189PlayerSprintControl original;
    private long generation;

    synchronized boolean enter(final Minecraft189PlayerSprintControl player) {
        if (restoring) {
            revoked = true;
            return false;
        }
        if (player == null) return false;
        ++generation;
        restoring = true;
        revoked = false;
        original = player;
        return true;
    }

    synchronized boolean stillOwned(final Minecraft189PlayerSprintControl player) {
        return restoring && !revoked && original == player;
    }

    synchronized void revoke() {
        if (restoring) revoked = true;
    }

    synchronized void exit() {
        ++generation;
        original = null;
        revoked = false;
        restoring = false;
    }
}
