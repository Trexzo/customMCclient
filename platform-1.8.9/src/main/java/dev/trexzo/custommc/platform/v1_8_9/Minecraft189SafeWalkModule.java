package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;

/**
 * SafeWalk reuses actual vanilla Entity.moveEntity edge clipping and the
 * ORIGINAL World.getCollidingBoundingBoxes. It never guesses block geometry,
 * teleports or alters movement coordinates. Disabled by default.
 */
public final class Minecraft189SafeWalkModule implements Module {
    public static final String ID = "movement.safeWalk";
    private boolean enabled;

    @Override public String id() { return ID; }
    @Override public synchronized void onEnable() { enabled = true; }
    @Override public synchronized void onDisable() { enabled = false; }

    synchronized boolean effectiveSneaking(final boolean originalSneaking,
            final Object nativeEntity) {
        // Preserve real sneak, only affect source-native local player marker.
        return originalSneaking || enabled
                && nativeEntity instanceof Minecraft189LocalPlayerSafeWalkMarker;
    }
    synchronized boolean active() { return enabled; }
}
