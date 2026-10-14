package dev.trexzo.custommc.platform.v1_8_9;

import java.lang.ref.WeakReference;

/**
 * A V34 activation is bound to the first genuine Minecraft/World/player
 * identity seen after module enable, independently of cursor, inventory,
 * grounded state or placement readiness.
 *
 * No source object is kept strongly or moved to another tick. A missing
 * source BEFORE first World entry is WAITING; after it has been seen, any
 * missing, replaced or garbage-collected witness is REVOKED until reset.
 */
final class Minecraft189ScaffoldActivationSource {
    enum Verdict { WAITING, SAME, REVOKED }

    private WeakReference<Object> owner = new WeakReference<Object>(null);
    private WeakReference<Object> player = new WeakReference<Object>(null);
    private WeakReference<Object> world = new WeakReference<Object>(null);
    private boolean witnessed;
    private boolean revoked;

    synchronized Verdict observe(final Object minecraft) {
        if (revoked) return Verdict.REVOKED;
        Object nativePlayer = null;
        Object nativeWorld = null;
        if (minecraft instanceof Minecraft189ScaffoldNativeContextAccess) {
            try {
                final Object[] current =
                        ((Minecraft189ScaffoldNativeContextAccess)minecraft)
                                .customMcScaffoldNativeContext();
                if (current != null && current.length == 3
                        && current[0] instanceof Minecraft189LocalPlayerSafeWalkMarker
                        && current[0] instanceof Minecraft189PlayerPositionAccess
                        && current[1] instanceof Minecraft189ScaffoldWorldReadAccess) {
                    nativePlayer = current[0];
                    nativeWorld = current[1];
                }
            } catch (RuntimeException unavailable) {
                // No source after a witnessed World is always a revocation.
            }
        }
        if (nativePlayer == null || nativeWorld == null || minecraft == null) {
            if (!witnessed) return Verdict.WAITING;
            revoked = true;
            dropReferences();
            return Verdict.REVOKED;
        }
        if (!witnessed) {
            witnessed = true;
            owner = new WeakReference<Object>(minecraft);
            player = new WeakReference<Object>(nativePlayer);
            world = new WeakReference<Object>(nativeWorld);
            return Verdict.SAME;
        }
        if (owner.get() != minecraft
                || player.get() != nativePlayer
                || world.get() != nativeWorld) {
            revoked = true;
            dropReferences();
            return Verdict.REVOKED;
        }
        return Verdict.SAME;
    }

    synchronized boolean witnessed() { return witnessed; }

    synchronized void reset() {
        revoked = false;
        witnessed = false;
        dropReferences();
    }

    private void dropReferences() {
        owner.clear();
        player.clear();
        world.clear();
        owner = new WeakReference<Object>(null);
        player = new WeakReference<Object>(null);
        world = new WeakReference<Object>(null);
    }
}
