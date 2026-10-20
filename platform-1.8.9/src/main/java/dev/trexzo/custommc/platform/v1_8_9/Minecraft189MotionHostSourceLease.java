package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Source lease across the complete original EntityPlayerSP host motion
 * callback, rather than an individual movement module's native setter.
 * Nested different-player or newly focused-GUI callbacks revoke the
 * outer chain. A same-player callback never becomes an extra tick.
 */
final class Minecraft189MotionHostSourceLease {
    private boolean dispatching;
    private Minecraft189PlayerMotionControl player;
    private boolean guiAtEntry;
    private boolean revoked;

    boolean enter(final Minecraft189PlayerMotionControl source,
            final boolean guiFocused) {
        if (dispatching) {
            if (source != player || (!guiAtEntry && guiFocused)) {
                revoked = true;
            }
            return false;
        }
        dispatching = true;
        player = source;
        guiAtEntry = guiFocused;
        revoked = false;
        return true;
    }

    boolean stillOwned(final boolean guiFocused) {
        if (!dispatching) {
            return false;
        }
        if (!guiAtEntry && guiFocused) {
            revoked = true;
        }
        return !revoked;
    }

    void exit() {
        player = null;
        guiAtEntry = false;
        revoked = false;
        dispatching = false;
    }
}
