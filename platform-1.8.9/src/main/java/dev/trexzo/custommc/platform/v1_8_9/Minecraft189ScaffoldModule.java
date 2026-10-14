package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

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
    public static final String TWO_PLACEMENTS_SETTING_ID =
            "movement.scaffold.twoPlacements";
    private static final int PRODUCTION_REPEAT_COUNT = 2;
    private static final int PRODUCTION_REPEAT_GAP_TICKS = 4;
    // Explicit UI setting, always OFF on a clean installation. Only captured
    // on a fresh module enable; changing it mid-activation grants no clicks.
    private final Setting<Boolean> twoPlacements = new Setting<Boolean>(
            TWO_PLACEMENTS_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private boolean productionRepeatForActivation;
    private boolean productionRepeatStarted;
    private long productionRepeatTick;
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
        productionRepeatForActivation = false;
        productionRepeatStarted = false;
        productionRepeatTick = 0L;
        if (exhausted || activation == Long.MAX_VALUE) {
            exhausted = true;
            return;
        }
        activation++;
        productionRepeatForActivation = Boolean.TRUE.equals(twoPlacements.get());
        enabled = true;
    }

    @Override public synchronized void onDisable() {
        if (!enabled) return; // idempotent disable, including close rollback
        repeatSequence.invalidate();
        enabled = false;
        attempted = false;
        productionRepeatForActivation = false;
        productionRepeatStarted = false;
        productionRepeatTick = 0L;
        if (activation == Long.MAX_VALUE) exhausted = true;
        else activation++;
    }

    public Setting<Boolean> twoPlacementsSetting() { return twoPlacements; }
    synchronized boolean productionRepeatEnabledForActivation() {
        return active() && productionRepeatForActivation;
    }
    synchronized boolean productionRepeatStarted() { return productionRepeatStarted; }
    synchronized long productionRepeatTickCount() { return productionRepeatTick; }
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
    /**
     * V42 production opt-in path. Call ONLY from original runTick with a
     * witnessed same original Minecraft/World/player and CLEAR GUI screen.
     * The default-off setting is frozen at onEnable; this path cannot run in
     * the same activation as the normal one-shot or the CI-only repeat proof.
     * No original game references escape the module-owned synchronous tick.
     */
    synchronized void productionRepeatAtOriginalTick(
            final Object originalMinecraft, final boolean originalGuiClear) {
        if (!productionRepeatEnabledForActivation() || !originalGuiClear
                || originalMinecraft == null
                || (attempted && !productionRepeatStarted))
            return;
        if (productionRepeatTick == Long.MAX_VALUE) {
            repeatSequence.invalidate();
            productionRepeatStarted = true;
            return;
        }
        final long tick = ++productionRepeatTick;
        if (!productionRepeatStarted && armed()
                && repeatSequence.start(originalMinecraft, activation, tick,
                        PRODUCTION_REPEAT_COUNT, PRODUCTION_REPEAT_GAP_TICKS)) {
            productionRepeatStarted = true;
            attempted = true; // Permanently exclude the one-shot per enable.
        }
        if (!productionRepeatStarted || repeatSequence.stopped()) return;
        repeatSequence.checkReceipt(originalMinecraft, activation, tick);
        if (!repeatSequence.stopped()) {
            repeatSequence.tryNext(originalMinecraft, activation, tick);
        }
    }

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
        if (!armed() || productionRepeatForActivation) return null;
        final Minecraft189ScaffoldNativePlacementAttempt.Result result =
                Minecraft189ScaffoldNativePlacementAttempt.attempt(
                        originalMinecraft, true);
        if (result != null) attempted = true;
        return result;
    }
}
