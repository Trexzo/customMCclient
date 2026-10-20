package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Shared, exact-reference original native source lease. Only the first
 * genuine host callback owns a getter stack; same-source recursion is
 * never a second tick, while any different (including null) nested
 * source revokes the outer call irreversibly until its finally exit.
 *
 * This intentionally does not implement ClickGUI focus semantics:
 * those differ between motion, jump, sprint and rotation hosts.
 */
class Minecraft189ExactSourceLease<S> {
    private boolean dispatching;
    private S source;
    private boolean revoked;

    boolean enter(final S original) {
        if (dispatching) {
            if (original != source) {
                revoked = true;
            }
            return false;
        }
        dispatching = true;
        source = original;
        revoked = false;
        return true;
    }

    boolean stillOwned() {
        return dispatching && !revoked;
    }

    void exit() {
        source = null;
        revoked = false;
        dispatching = false;
    }
}
