package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

/**
 * Preferred Combat Slot: opt-in fixed hotbar slot, not yet strongest-weapon
 * classification. Bounded to verified player-hit synthetic clicks.
 */
public final class Minecraft189CombatSlotModule implements Module {
    public static final String ID = "combat.combatSlot";
    public static final String PREFERRED_SLOT = ID + ".preferredSlot";
    public static final String RESTORE_AFTER_CLICK = ID + ".restoreAfterClick";
    public static final String REQUIRE_HIT = ID + ".requirePlayerHit";

    private final Setting<Integer> slot = new Setting<Integer>(
            PREFERRED_SLOT, 1,
            value -> value != null && value >= 1 && value <= 9,
            SettingCodecs.INTEGER);
    private final Setting<Boolean> restore = new Setting<Boolean>(
            RESTORE_AFTER_CLICK, Boolean.TRUE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> requireHit = new Setting<Boolean>(
            REQUIRE_HIT, Boolean.TRUE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private boolean enabled;
    // Capture the exact original native inventory and target at selection.
    // The ClickGUI preferred slot can change reentrantly before finally;
    // restoration must never consult that mutable setting.
    private Minecraft189InventoryHotbarControl ownerInventory;
    private int selectedSlot = -1;
    private int originalSlot = -1;

    @Override public String id() { return ID; }
    @Override public synchronized void onEnable() { enabled = true; }
    @Override public synchronized void onDisable() { enabled = false; }
    public Setting<Integer> preferredSlotSetting() { return slot; }
    public Setting<Boolean> restoreAfterClickSetting() { return restore; }
    public Setting<Boolean> requirePlayerHitSetting() { return requireHit; }
    synchronized boolean active() { return enabled; }

    /**
     * Returns prior 0-based slot, or -1 if no switch happened.
     * Never reads item names as weapon proof.
     */
    synchronized int select(
            final Minecraft189InventoryHotbarControl inventory,
            final boolean verifiedPlayerHit, final boolean guiOpen) {
        // A nested synthetic click may not steal another action's current
        // hotbar lease. Its original finally owns the only restoration.
        if (ownerInventory != null || !enabled || guiOpen || inventory == null
                || (requireHit.get() && !verifiedPlayerHit)) return -1;
        final int original = inventory.customMcSelectedHotbarSlot();
        if (original < 0 || original >= 9) return -1;
        final int preferred = slot.get() - 1;
        if (preferred == original) return -1;

        ownerInventory = inventory;
        selectedSlot = preferred;
        originalSlot = original;
        try {
            inventory.customMcSetSelectedHotbarSlot(preferred);
            // A native setter can return without selecting the requested
            // item, or a reentrant user action can select a different slot.
            // Do not report a successful module-owned switch in that case.
            if (inventory.customMcSelectedHotbarSlot() != preferred) {
                clearLease();
                return -1;
            }
            return original;
        } catch (RuntimeException failure) {
            // Original setters sometimes mutate currentItem before throwing.
            // Roll back only our exact selected slot, never another actor's.
            try {
                if (inventory.customMcSelectedHotbarSlot() == preferred)
                    inventory.customMcSetSelectedHotbarSlot(original);
            } catch (RuntimeException rollback) {
                failure.addSuppressed(rollback);
            } finally {
                clearLease();
            }
            throw failure;
        }
    }

    synchronized void restore(
            final Minecraft189InventoryHotbarControl inventory,
            final int original) {
        if (inventory == null || inventory != ownerInventory
                || original != originalSlot || original < 0 || original >= 9)
            return;
        try {
            // Restoration must still work after onDisable: the synthetic
            // action already acquired this exact native-player lease.
            if (restore.get().booleanValue() && selectedSlot >= 0
                    && inventory.customMcSelectedHotbarSlot() == selectedSlot)
                inventory.customMcSetSelectedHotbarSlot(original);
        } finally {
            clearLease();
        }
    }

    private void clearLease() {
        ownerInventory = null;
        selectedSlot = -1;
        originalSlot = -1;
    }
}
