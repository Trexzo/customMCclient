package dev.trexzo.custommc.platform.v1_8_9;

/**
 * One original EntityPlayerSP native sprint writer per host callback.
 *
 * WTap has first refusal. If it actually attempts a setSprinting(false),
 * even one that reenters a module and makes apply() return false, AutoSprint
 * must not overwrite that native result from the old movement snapshot.
 * A WTap no-op must still permit an independent AutoSprint write.
 *
 * This owns neither the original player identity nor GUI focus: the
 * SprintHostSourceLease continues to gate the REAL native setter.
 */
final class Minecraft189SprintWriteArbiter {
    enum Owner { W_TAP, AUTO_SPRINT }

    private long generation;
    private boolean active;
    private Owner writer;

    synchronized void enter() {
        ++generation;
        active = true;
        writer = null;
    }

    synchronized void exit() {
        ++generation;
        writer = null;
        active = false;
    }

    synchronized boolean ownedBy(final Owner owner) {
        return active && writer == owner;
    }

    private synchronized boolean claim(final Owner owner,
            final long token) {
        if (!active || token != generation || owner == null
                || (writer != null && writer != owner))
            return false;
        writer = owner;
        return true;
    }

    Minecraft189PlayerSprintControl guardedPlayer(
            final Minecraft189PlayerSprintControl original,
            final Owner owner) {
        if (original == null || owner == null) return null;
        final long token;
        synchronized (this) { token = generation; }
        return value -> {
            // Claim BEFORE the original native write: a nested callback,
            // failed setter, or OFF->ON module change cannot open another
            // writer lane or replay a stale wrapper in a new callback.
            if (claim(owner, token))
                original.customMcSetSprinting(value);
        };
    }
}
