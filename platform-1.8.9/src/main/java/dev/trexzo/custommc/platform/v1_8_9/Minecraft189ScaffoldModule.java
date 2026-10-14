package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;

/**
 * V32 first user-visible Scaffold module.
 *
 * This deliberately permits at most ONE native vanilla block-placement
 * attempt per explicit enable. No repeated bridging, tower motion, camera
 * spoofing, raw packets or automatic inventory slot changes. The original
 * same-tick World/eye/cursor/ItemBlock gates remain authoritative.
 *
 * A module disable or new enable advances its activation generation, which
 * prevents pending receipt reuse across toggles and runtime replacement.
 */
public final class Minecraft189ScaffoldModule implements Module {
    public static final String ID = "movement.scaffold";
    private boolean enabled;
    private boolean attempted;
    private boolean exhausted;
    private long activation;

    @Override public String id() { return ID; }

    @Override public synchronized void onEnable() {
        if (enabled) return; // duplicate lifecycle callback may not re-arm
        enabled = false;
        attempted = false;
        if (exhausted || activation == Long.MAX_VALUE) {
            exhausted = true;
            return;
        }
        activation++;
        enabled = true;
    }

    @Override public synchronized void onDisable() {
        if (!enabled) return; // idempotent disable, including close rollback
        enabled = false;
        attempted = false;
        if (activation == Long.MAX_VALUE) exhausted = true;
        else activation++;
    }

    synchronized boolean active() { return enabled && !exhausted; }
    synchronized boolean armed() { return active() && !attempted; }
    synchronized long activation() { return active() ? activation : 0L; }

    /**
     * Keep module ownership throughout the side-effecting controller call.
     * A null result means native source was unavailable and no action was
     * attempted; a non-null result consumes this enable, even on false return.
     */
    synchronized Minecraft189ScaffoldNativePlacementAttempt.Result attemptOnce(
            final Object originalMinecraft) {
        if (!armed()) return null;
        final Minecraft189ScaffoldNativePlacementAttempt.Result result =
                Minecraft189ScaffoldNativePlacementAttempt.attempt(
                        originalMinecraft, true);
        if (result != null) attempted = true;
        return result;
    }
}
