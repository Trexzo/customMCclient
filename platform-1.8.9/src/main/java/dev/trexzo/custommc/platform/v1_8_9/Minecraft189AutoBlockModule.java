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

    @Override public String id() { return ID; }
    @Override public synchronized void onEnable() { enabled = true; }
    @Override public synchronized void onDisable() {
        // Preserve pending release: native stop must happen on next callback.
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

    /** Only call after native rightClickMouse returns and isUsingItem is true. */
    synchronized void started() {
        if (enabled) {
            ownsBlock = true;
            age = 0;
        }
    }

    synchronized void forgetForShutdown() {
        ownsBlock = false;
        age = 0;
        enabled = false;
    }
}
