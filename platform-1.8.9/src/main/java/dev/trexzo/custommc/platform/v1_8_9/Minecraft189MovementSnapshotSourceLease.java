package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Atomic original EntityPlayerSP movement source sampling boundary.
 *
 * Each mapped onGround/sneaking/sprinting getter may reenter the host;
 * a nested different original player or null source revokes the outer
 * snapshot so one player's ground cannot mix with another's sprint.
 * Reentrant same-player sampling is not another genuine movement tick.
 */
final class Minecraft189MovementSnapshotSourceLease {
    private boolean dispatching;
    private Minecraft189PlayerMovementStateAccess original;
    private boolean revoked;

    boolean enter(final Minecraft189PlayerMovementStateAccess source) {
        if (dispatching) {
            if (source != original) {
                revoked = true;
            }
            return false;
        }
        dispatching = true;
        original = source;
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
