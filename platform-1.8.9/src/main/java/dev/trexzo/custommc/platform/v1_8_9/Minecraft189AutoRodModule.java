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
    synchronized int selectSlot(final Minecraft189InventoryHotbarControl inventory) {
        if (!enabled || inventory == null) return -1;
        final int original = inventory.customMcSelectedHotbarSlot();
        if (original < 0 || original > 8) return -1;
        inventory.customMcSetSelectedHotbarSlot(rodSlot.get() - 1);
        return original;
    }

    /** Restore even if the Minecraft use-item method throws. */
    synchronized void restoreSlot(final Minecraft189InventoryHotbarControl inventory,
            final int original) {
        if (inventory != null && original >= 0 && original <= 8
                && inventory.customMcSelectedHotbarSlot() == rodSlot.get() - 1)
            inventory.customMcSetSelectedHotbarSlot(original);
    }
}
