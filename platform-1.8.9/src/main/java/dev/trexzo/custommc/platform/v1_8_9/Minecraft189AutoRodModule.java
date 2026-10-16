package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

/**
 * Explicit configured-slot AutoRod; never guesses which item is a rod.
 * A rod attempt owns exactly one synthetic use-item action instead of attack.
 */
public final class Minecraft189AutoRodModule implements Module {
    public static final String ID = "combat.autoRod";
    public static final String ROD_SLOT = ID + ".rodSlot";
    public static final String COOLDOWN_TICKS = ID + ".cooldownTicks";
    public static final String REQUIRE_HOLD = ID + ".requireAttackHeld";
    public static final String PAUSE_RIGHT = ID + ".pauseWhileRightClicking";
    private final Setting<Integer> rodSlot = new Setting<Integer>(
            ROD_SLOT, 2, x -> x != null && x >= 1 && x <= 9,
            SettingCodecs.INTEGER);
    private final Setting<Integer> cooldown = new Setting<Integer>(
            COOLDOWN_TICKS, 30, x -> x != null && x >= 1 && x <= 200,
            SettingCodecs.INTEGER);
    private final Setting<Boolean> requireHold = new Setting<Boolean>(
            REQUIRE_HOLD, Boolean.TRUE, x -> x != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseRight = new Setting<Boolean>(
            PAUSE_RIGHT, Boolean.TRUE, x -> x != null, SettingCodecs.BOOLEAN);
    private boolean enabled;
    private int remaining;
    private int activeCooldown;

    @Override public String id() { return ID; }
    @Override public synchronized void onEnable() {
        enabled = true;
        remaining = 0;
        activeCooldown = cooldown.get();
    }
    @Override public synchronized void onDisable() {
        enabled = false;
        suspend();
    }
    public Setting<Integer> rodSlotSetting() { return rodSlot; }
    public Setting<Integer> cooldownTicksSetting() { return cooldown; }
    public Setting<Boolean> requireHoldSetting() { return requireHold; }
    public Setting<Boolean> pauseRightSetting() { return pauseRight; }
    synchronized boolean active() { return enabled; }

    synchronized void suspend() { remaining = 0; activeCooldown = cooldown.get(); }

    synchronized boolean shouldUse(final boolean confirmedPlayer,
            final boolean leftHeld, final boolean rightHeld,
            final boolean guiOpen, final boolean hasInventory) {
        if (!enabled || guiOpen || !confirmedPlayer || !hasInventory
                || (requireHold.get() && !leftHeld)
                || (pauseRight.get() && rightHeld)) {
            suspend();
            return false;
        }
        if (activeCooldown != cooldown.get()) {
            remaining = 0;
            activeCooldown = cooldown.get();
        }
        if (remaining > 0) {
            remaining--;
            return false;
        }
        // Admission does not commit cooldown: item verification may still
        // reject the configured slot, or native use could throw.
        return true;
    }

    synchronized void commitUseAttempt() {
        if (enabled) {
            remaining = cooldown.get();
            activeCooldown = cooldown.get();
        }
    }

    /** Selects the user-configured slot for one synchronous native right click. */
    // Synchronous native hotbar lease. Persist the ACTUAL slot chosen for
    // this attempt: a UI setting change before finally/restore must not
    // strand the selected rod/potion or overwrite another actor's slot.
    private Minecraft189InventoryHotbarControl selectedInventory;
    private int selectedSlot = -1;
    private int previousSlot = -1;

    synchronized int selectSlot(final Minecraft189InventoryHotbarControl inventory) {
        if (!enabled || inventory == null || selectedInventory != null) return -1;
        final int original = inventory.customMcSelectedHotbarSlot();
        if (original < 0 || original > 8) return -1;
        final int target = rodSlot.get().intValue() - 1;
        // Record exact slot ownership before invoking the native setter.
        // A reentrant GUI setting change never changes this lease.
        selectedInventory = inventory;
        selectedSlot = target;
        previousSlot = original;
        try {
            inventory.customMcSetSelectedHotbarSlot(target);
        } catch (RuntimeException failure) {
            try {
                restoreSlot(inventory, original);
            } catch (RuntimeException rollback) {
                failure.addSuppressed(rollback);
            }
            throw failure;
        }
        return original;
    }


    /**
     * The real hotbar slot must still be the one this module selected for
     * this exact attempt. A reentrant user action can change currentItem
     * during the native held-item read; that cannot authorize a synthetic
     * use of an unrelated item, even if its type happens to match.
     */
    synchronized boolean ownsSelectedSlot(
            final Minecraft189InventoryHotbarControl inventory,
            final int original) {
        return enabled && inventory != null && inventory == selectedInventory
                && original == previousSlot && selectedSlot >= 0
                && inventory.customMcSelectedHotbarSlot() == selectedSlot;
    }

    synchronized void restoreSlot(final Minecraft189InventoryHotbarControl inventory,
            final int original) {
        if (inventory == null || inventory != selectedInventory
                || original != previousSlot || original < 0 || original > 8)
            return;
        try {
            // Do not overwrite a real user/another module slot change.
            // Compare the original target captured at select time, NOT the
            // mutable ClickGUI configured target slot.
            if (inventory.customMcSelectedHotbarSlot() == selectedSlot
                    && selectedSlot != original)
                inventory.customMcSetSelectedHotbarSlot(original);
        } finally {
            selectedInventory = null;
            selectedSlot = -1;
            previousSlot = -1;
        }
    }
}
