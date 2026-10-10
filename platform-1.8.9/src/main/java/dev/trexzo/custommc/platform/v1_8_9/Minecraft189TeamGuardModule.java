package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;

/**
 * Independent friendly-fire protection for synthetic combat actions.
 * A disabled module preserves vanilla/default behavior. When active,
 * only confirmed non-teammates are eligible; unknown evidence fails closed.
 */
public final class Minecraft189TeamGuardModule implements Module {
    public static final String ID = "combat.teamGuard";
    private boolean enabled;

    @Override public String id() { return ID; }
    @Override public synchronized void onEnable() { enabled = true; }
    @Override public synchronized void onDisable() { enabled = false; }
    public synchronized boolean active() { return enabled; }

    synchronized boolean permits(final int index,
            final Minecraft189WorldEntityCombatState.Snapshot state) {
        if (!enabled) return true;
        return index >= 0 && state != null && state.available()
                && state.alive(index) && state.teamKnown(index)
                && !state.sameTeam(index);
    }
}
