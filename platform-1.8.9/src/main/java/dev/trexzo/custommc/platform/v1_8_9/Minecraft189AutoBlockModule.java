package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

/**
 * Sword-only vanilla use-item block after an authorized synthetic attack.
 * A block always has one owner and a bounded expiry; manual right click
 * takes ownership away without synthesizing a release of the user's input.
 */
public final class Minecraft189AutoBlockModule implements Module {
    public static final String ID = "combat.autoBlock";
    public static final String HOLD_TICKS = ID + ".holdTicks";
    public static final String PAUSE_MANUAL_RIGHT = ID + ".pauseManualRight";

    private final Setting<Integer> holdTicks = new Setting<Integer>(
            HOLD_TICKS, 2, value -> value != null && value >= 1 && value <= 20,
            SettingCodecs.INTEGER);
    private final Setting<Boolean> pauseManualRight = new Setting<Boolean>(
            PAUSE_MANUAL_RIGHT, Boolean.TRUE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private boolean enabled;
    private boolean ownsBlock;
    private int age;
    private long lifecycleEpoch;
    private long nextUseToken;
    private long pendingUseToken;
    private long pendingEpoch;
    private int pendingHoldTicks;
    private boolean pendingManualPolicy;

    @Override public String id() { return ID; }
    @Override public synchronized void onEnable() {
        lifecycleEpoch++;
        pendingUseToken = 0;
        enabled = true;
    }
    @Override public synchronized void onDisable() {
        // Preserve pending release: native stop must happen on next callback.
        lifecycleEpoch++;
        pendingUseToken = 0;
        enabled = false;
    }

    public Setting<Integer> holdTicksSetting() { return holdTicks; }
    public Setting<Boolean> pauseManualRightSetting() { return pauseManualRight; }
    synchronized boolean active() { return enabled; }
    synchronized boolean ownsBlock() { return ownsBlock; }

    /** One invocation per mapped combat tick. False does not mean owned. */
    synchronized boolean shouldStop(final boolean guiOpen,
            final boolean manualRightHeld, final boolean swordPresent,
            final boolean targetPresent) {
        if (!ownsBlock) return false;
        if (manualRightHeld) {
            // The user has taken over right-click. Don't send a synthetic
            // release that could interrupt their genuine held input.
            ownsBlock = false;
            age = 0;
            return false;
        }
        if (!enabled || guiOpen || !swordPresent || !targetPresent
                || ++age >= holdTicks.get()) {
            ownsBlock = false;
            age = 0;
            return true;
        }
        return false;
    }

    /** Called BEFORE any competing synthetic click, rod or potion action. */
    synchronized boolean releaseForAction(final boolean manualRightHeld) {
        if (!ownsBlock) return false;
        ownsBlock = false;
        age = 0;
        return !manualRightHeld;
    }

    synchronized boolean mayStart(final boolean swordPresent,
            final boolean confirmedPlayer, final boolean guiOpen,
            final boolean manualRightHeld, final boolean slotWasSwitched,
            final boolean alreadyUsingItem) {
        return enabled && !ownsBlock && swordPresent && confirmedPlayer
                && !guiOpen && !slotWasSwitched && !alreadyUsingItem
                && (!pauseManualRight.get() || !manualRightHeld);
    }

    /** Reserves a single use-result token after the current native admission. */
    synchronized long beginNativeUse() {
        if (!enabled || ownsBlock) return 0;
        final long token = ++nextUseToken;
        pendingUseToken = token;
        pendingEpoch = lifecycleEpoch;
        pendingHoldTicks = holdTicks.get();
        pendingManualPolicy = pauseManualRight.get();
        return token;
    }

    /** Commit only an unchanged native right-click result for this activation. */
    synchronized boolean completeNativeUse(final long token,
            final boolean swordStillHeld, final boolean guiOpen,
            final boolean manualRightHeld) {
        final boolean valid = token != 0 && token == pendingUseToken
                && pendingEpoch == lifecycleEpoch && enabled
                && !ownsBlock && swordStillHeld && !guiOpen
                && !manualRightHeld && pendingHoldTicks == holdTicks.get()
                && pendingManualPolicy == pauseManualRight.get();
        if (token != 0 && token == pendingUseToken) pendingUseToken = 0;
        if (valid) {
            ownsBlock = true;
            age = 0;
        }
        return valid;
    }

    /** Only call after native rightClickMouse returns and isUsingItem is true. */
    synchronized void started() {
        if (enabled) {
            ownsBlock = true;
            age = 0;
        }
    }

    synchronized void forgetForShutdown() {
        lifecycleEpoch++;
        pendingUseToken = 0;
        ownsBlock = false;
        age = 0;
        enabled = false;
    }
}
