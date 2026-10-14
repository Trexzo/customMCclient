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
    // V36 future opt-in proof only; never called by production runTick.
    private final Minecraft189ScaffoldRepeatSequence repeatSequence =
            new Minecraft189ScaffoldRepeatSequence();

    @Override public String id() { return ID; }

    @Override public synchronized void onEnable() {
        if (enabled) return; // duplicate lifecycle callback may not re-arm
        repeatSequence.invalidate();
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
        repeatSequence.invalidate();
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
    /**
     * V36 isolated API for future original-game two-block certification. This
     * is deliberately NOT installed in the runTick callback or ClickGUI.
     * Starting a sequence consumes the one-shot arm, so the live V35 path
     * cannot race it or double-click on the same module activation.
     */
    synchronized boolean beginRepeatProof(final Object originalMinecraft,
            final long tick, final int maximumAttempts, final int minimumGapTicks) {
        if (!armed()) return false;
        if (!repeatSequence.start(originalMinecraft, activation, tick,
                maximumAttempts, minimumGapTicks)) return false;
        attempted = true;
        return true;
    }

    synchronized Minecraft189ScaffoldNativePlacementAttempt.Result
            repeatProofAttempt(final Object originalMinecraft, final long tick) {
        if (!active() || !attempted) return null;
        return repeatSequence.tryNext(originalMinecraft, activation, tick);
    }

    synchronized boolean repeatProofReceipt(
            final Object originalMinecraft, final long tick) {
        return active() && attempted
                && repeatSequence.checkReceipt(originalMinecraft, activation, tick);
    }

    synchronized String repeatProofDiagnostics(final Object originalMinecraft) {
        return repeatSequence.diagnose(originalMinecraft);
    }
    synchronized boolean repeatProofStopped() { return repeatSequence.stopped(); }
    synchronized int repeatProofAttemptedCount() {
        return repeatSequence.attemptedCount();
    }

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
