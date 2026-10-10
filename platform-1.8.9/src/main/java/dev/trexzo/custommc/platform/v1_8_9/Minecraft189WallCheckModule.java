package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;

/**
 * Optional wall check for automatic Combat actions only.
 * Exact vanilla local-player canEntityBeSeen evidence; unknowns fail closed.
 */
public final class Minecraft189WallCheckModule implements Module {
    public static final String ID = "combat.wallCheck";
    private boolean enabled;

    @Override public String id() { return ID; }
    @Override public synchronized void onEnable() { enabled = true; }
    @Override public synchronized void onDisable() { enabled = false; }
    synchronized boolean active() { return enabled; }

    synchronized boolean permits(final int index,
            final Minecraft189WorldEntityVisibilityState.Snapshot evidence) {
        return !enabled || evidence != null && evidence.visible(index);
    }
}
