package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Pins one genuine original EntityPlayerSP position source across its
 * separate X, Y and Z native getter calls before any shared publication.
 */
final class Minecraft189PositionHostSourceLease {
    private boolean dispatching;
    private Minecraft189PlayerPositionAccess original;
    private boolean revoked;

    boolean enter(final Minecraft189PlayerPositionAccess player) {
        if (dispatching) {
            if (player != original) {
                revoked = true;
            }
            return false;
        }
        dispatching = true;
        original = player;
        revoked = false;
        return true;
    }

    boolean stillOwned() {
        return dispatching && !revoked;
    }

    void exit() {
        original = null;
        revoked = false;
        dispatching = false;
    }
}
