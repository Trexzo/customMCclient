package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Source owner for native Minecraft World.loadedEntityList data.
 * A different world or null nested callback revokes the former
 * visibility/UUID/XYZ read. No list position or UUID is fabricated.
 */
final class Minecraft189WorldEntityHostSourceLease {
    private boolean dispatching;
    private Minecraft189WorldEntityPositionsAccess original;
    private boolean revoked;

    boolean enter(final Minecraft189WorldEntityPositionsAccess world) {
        if (dispatching) {
            if (world != original) {
                revoked = true;
            }
            return false;
        }
        dispatching = true;
        original = world;
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
