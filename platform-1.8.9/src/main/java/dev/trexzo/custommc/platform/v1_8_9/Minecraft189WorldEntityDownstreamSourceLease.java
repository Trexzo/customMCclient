package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Native loadedEntityList kinds/combat read lease, pinned to one exact
 * original Minecraft World source for the duration of mapped getters.
 *
 * Cross-callback World identity is independently validated against the
 * position-sampling source in Minecraft189HostRuntime.
 */
final class Minecraft189WorldEntityDownstreamSourceLease {
    private boolean dispatching;
    private Object originalWorld;
    private boolean revoked;

    boolean enter(final Object world) {
        if (dispatching) {
            if (world != originalWorld) {
                revoked = true;
            }
            return false;
        }
        dispatching = true;
        originalWorld = world;
        revoked = false;
        return true;
    }

    boolean stillOwned() {
        return dispatching && !revoked;
    }

    void exit() {
        originalWorld = null;
        revoked = false;
        dispatching = false;
    }
}
