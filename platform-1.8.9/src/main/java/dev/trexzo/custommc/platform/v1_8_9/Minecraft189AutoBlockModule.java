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
    // V340: capture the SAME original HeldItem callback generation for
    // the entire native rightClickMouse/use-item lease. A reentrant
    // inventory/local-player callback may replace the source with another
    // sword before right-click returns. Sword kind alone is not identity.
    private boolean pendingHeldOwnerRequired;
    private long pendingHeldOwnerSerial;
    // The synthetic sword block already owns a genuine original use-item
    // action. Persist the EXACT policy that authorized THAT action rather
    // than allowing a later GUI edit to extend its lifetime.
    private int ownedHoldTicks;
    private boolean ownedManualPolicy;

    @Override public String id() { return ID; }
    @Override public synchronized void onEnable() {
        lifecycleEpoch++;
        pendingUseToken = 0;
        pendingHeldOwnerRequired = false;
        pendingHeldOwnerSerial = 0L;
        enabled = true;
    }
    @Override public synchronized void onDisable() {
        // Preserve pending release: native stop must happen on next callback.
        lifecycleEpoch++;
        pendingUseToken = 0;
        pendingHeldOwnerRequired = false;
        pendingHeldOwnerSerial = 0L;
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
            clearOwnedPolicy();
            return false;
        }
        if (!enabled || guiOpen || !swordPresent || !targetPresent
                || ownedHoldTicks != holdTicks.get()
                || ownedManualPolicy != pauseManualRight.get()
                || ++age >= ownedHoldTicks) {
            ownsBlock = false;
            age = 0;
            clearOwnedPolicy();
            return true;
        }
        return false;
    }

    /** Called BEFORE any competing synthetic click, rod or potion action. */
    synchronized boolean releaseForAction(final boolean manualRightHeld) {
        if (!ownsBlock) return false;
        ownsBlock = false;
        age = 0;
        clearOwnedPolicy();
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

    /** Source-independent legacy admission retained for existing unit fixtures. */
    synchronized long beginNativeUse() {
        return beginNativeUseInternal(0L, false);
    }

    /**
     * Original-game admission: the exact HeldItem source generation must
     * survive native rightClickMouse and its potentially reentrant getters.
     */
    synchronized long beginNativeUse(final long originalHeldOwnerSerial) {
        return beginNativeUseInternal(originalHeldOwnerSerial, true);
    }

    private long beginNativeUseInternal(final long originalHeldOwnerSerial,
            final boolean requireOriginalHeldOwner) {
        if (!enabled || ownsBlock) return 0;
        final long token = ++nextUseToken;
        pendingUseToken = token;
        pendingEpoch = lifecycleEpoch;
        pendingHoldTicks = holdTicks.get();
        pendingManualPolicy = pauseManualRight.get();
        pendingHeldOwnerRequired = requireOriginalHeldOwner;
        pendingHeldOwnerSerial = originalHeldOwnerSerial;
        return token;
    }

    /**
     * V346: abandon ONLY this exact pending native sword-use lease when
     * original rightClickMouse refuses, throws or an original GuiScreen
     * veto interrupts dispatch. Never revoke a newer reentrant source
     * reservation or an already completed owned block.
     *
     * Cancellation is bookkeeping only; it NEVER calls stopUsingItem and
     * cannot release a user's physical/manual right-click.
     */
    synchronized boolean cancelPendingNativeUse(final long token) {
        if (token == 0L || token != pendingUseToken) return false;
        pendingUseToken = 0L;
        pendingEpoch = 0L;
        pendingHoldTicks = 0;
        pendingManualPolicy = false;
        pendingHeldOwnerRequired = false;
        pendingHeldOwnerSerial = 0L;
        return true;
    }

    /**
     * V360: final pre-rightClickMouse dispatch gate for the exact
     * original-game HeldItem reservation. This DOES NOT consume the lease:
     * completeNativeUse still requires genuine isUsingItem after vanilla
     * right-click, and cancellation remains token-specific in finally.
     *
     * The source World/held-item getter may synchronously reenter the
     * module or change the inventory. A new owner generation, changed
     * policy, GUI, manual-right takeover or missing original sword must
     * veto the native click BEFORE it occurs, not merely its bookkeeping.
     */
    synchronized boolean permitsNativeUseDispatch(final long token,
            final boolean swordStillHeld, final boolean guiOpen,
            final boolean manualRightHeld, final long currentHeldOwnerSerial) {
        return token != 0L && token == pendingUseToken
                && pendingEpoch == lifecycleEpoch && enabled
                && !ownsBlock && swordStillHeld && !guiOpen
                && !manualRightHeld
                && pendingHoldTicks == holdTicks.get()
                && pendingManualPolicy == pauseManualRight.get()
                && pendingHeldOwnerRequired
                && pendingHeldOwnerSerial == currentHeldOwnerSerial;
    }

    /** Legacy tests carry no original native HeldItem generation evidence. */
    synchronized boolean completeNativeUse(final long token,
            final boolean swordStillHeld, final boolean guiOpen,
            final boolean manualRightHeld) {
        return completeNativeUseInternal(token, swordStillHeld, guiOpen,
                manualRightHeld, 0L, false);
    }

    /** Complete ONLY with the original owner generation actually observed. */
    synchronized boolean completeNativeUse(final long token,
            final boolean swordStillHeld, final boolean guiOpen,
            final boolean manualRightHeld, final long currentHeldOwnerSerial) {
        return completeNativeUseInternal(token, swordStillHeld, guiOpen,
                manualRightHeld, currentHeldOwnerSerial, true);
    }

    private boolean completeNativeUseInternal(final long token,
            final boolean swordStillHeld, final boolean guiOpen,
            final boolean manualRightHeld, final long currentHeldOwnerSerial,
            final boolean hasCurrentHeldOwner) {
        final boolean valid = token != 0 && token == pendingUseToken
                && pendingEpoch == lifecycleEpoch && enabled
                && !ownsBlock && swordStillHeld && !guiOpen
                && !manualRightHeld && pendingHoldTicks == holdTicks.get()
                && pendingManualPolicy == pauseManualRight.get()
                && (!pendingHeldOwnerRequired || (hasCurrentHeldOwner
                        && pendingHeldOwnerSerial == currentHeldOwnerSerial));
        if (token != 0 && token == pendingUseToken) {
            pendingUseToken = 0;
            pendingHeldOwnerRequired = false;
            pendingHeldOwnerSerial = 0L;
        }
        if (valid) {
            ownsBlock = true;
            age = 0;
            ownedHoldTicks = pendingHoldTicks;
            ownedManualPolicy = pendingManualPolicy;
        }
        return valid;
    }

    /** Only call after native rightClickMouse returns and isUsingItem is true. */
    synchronized void started() {
        if (enabled) {
            ownsBlock = true;
            age = 0;
            ownedHoldTicks = holdTicks.get();
            ownedManualPolicy = pauseManualRight.get();
        }
    }

    synchronized void forgetForShutdown() {
        lifecycleEpoch++;
        pendingUseToken = 0;
        pendingHeldOwnerRequired = false;
        pendingHeldOwnerSerial = 0L;
        ownsBlock = false;
        age = 0;
        clearOwnedPolicy();
        enabled = false;
    }

    private void clearOwnedPolicy() {
        ownedHoldTicks = 0;
        ownedManualPolicy = false;
    }
}
