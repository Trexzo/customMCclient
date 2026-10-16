package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

/**
 * Ranks only verified 1.8.9 ItemSword base damage in the nine hotbar slots.
 * Does not claim enchantment, durability, axe, or server attack-damage parity.
 */
public final class Minecraft189AutoWeaponModule implements Module {
    public static final String ID = "combat.autoWeapon";
    public static final String REQUIRE_HIT = ID + ".requirePlayerHit";
    public static final String RESTORE = ID + ".restoreAfterAttack";
    private final Setting<Boolean> requireHit = new Setting<Boolean>(
            REQUIRE_HIT, Boolean.TRUE, v -> v != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> restore = new Setting<Boolean>(
            RESTORE, Boolean.TRUE, v -> v != null, SettingCodecs.BOOLEAN);
    private boolean enabled;
    private int selected = -1;
    private int previous = -1;
    private Minecraft189InventoryHotbarControl ownerInventory;

    @Override public String id() { return ID; }
    @Override public synchronized void onEnable() { enabled = true; }
    @Override public synchronized void onDisable() { enabled = false; }
    public Setting<Boolean> requirePlayerHitSetting() { return requireHit; }
    public Setting<Boolean> restoreAfterAttackSetting() { return restore; }
    synchronized boolean active() { return enabled; }

    /**
     * Selects the strongest verified sword, breaking damage ties in favor of
     * the already held slot, then the lowest slot. Returns previous slot only
     * if a real switch happened; -1 otherwise.
     */
    synchronized int select(
            final Minecraft189InventoryHotbarControl control,
            final boolean verifiedPlayerHit, final boolean guiOpen) {
        // A reentrant second combat action must not erase the first native
        // action's selected-slot lease. The first action's finally owns
        // restoration; do not guess how to merge nested hotbar changes.
        if (ownerInventory != null) return -1;
        if (!enabled || guiOpen
                || (requireHit.get() && !verifiedPlayerHit)
                || !(control instanceof Minecraft189InventoryHotbarItemsAccess))
            return -1;
        final Minecraft189InventoryHotbarItemsAccess inventory =
                (Minecraft189InventoryHotbarItemsAccess) control;
        final int prior = inventory.customMcSelectedHotbarSlot();
        if (prior < 0 || prior > 8) return -1;

        final Minecraft189ItemStackAccess[] items = inventory.customMcHotbarItems();
        if (items == null || items.length < 9) return -1;

        int bestSlot = -1;
        float bestDamage = Float.NEGATIVE_INFINITY;
        for (int slot = 0; slot < 9; slot++) {
            final Minecraft189ItemStackAccess item = items[slot];
            if (item == null || !item.customMcIsSword()
                    || item.customMcStackSize() < 1) continue;
            final float base = item.customMcSwordBaseDamage();
            if (!Float.isFinite(base) || base <= 0) continue;
            if (base > bestDamage
                    || (base == bestDamage && slot == prior)) {
                bestDamage = base;
                bestSlot = slot;
            }
        }
        if (bestSlot < 0 || bestSlot == prior) return -1;
        // Obtaining original inventory item descriptors can execute mapped
        // native getters with reentrant user/currentItem changes. Never
        // overwrite a different actual selected slot based on an old read.
        if (!enabled || inventory.customMcSelectedHotbarSlot() != prior)
            return -1;
        ownerInventory = inventory;
        selected = bestSlot;
        previous = prior;
        try {
            inventory.customMcSetSelectedHotbarSlot(bestSlot);
            // A source setter may be ignored or reentrantly redirected.
            // The selected sword is owned only if the original inventory
            // currently points at our captured best slot.
            if (inventory.customMcSelectedHotbarSlot() != bestSlot) {
                clearLease();
                return -1;
            }
            if (!enabled) {
                // The module could have been disabled reentrantly from the
                // native setter. Restore only our own exact hotbar slot.
                inventory.customMcSetSelectedHotbarSlot(prior);
                clearLease();
                return -1;
            }
            return prior;
        } catch (RuntimeException failure) {
            // Native hotbar setter can mutate its backing currentItem
            // field BEFORE throwing. Roll back only if our intended slot
            // still owns it, preserving another actor's later change.
            try {
                if (inventory.customMcSelectedHotbarSlot() == bestSlot)
                    inventory.customMcSetSelectedHotbarSlot(prior);
            } catch (RuntimeException rollback) {
                failure.addSuppressed(rollback);
            } finally {
                clearLease();
            }
            throw failure;
        }
    }

    synchronized void restore(final Minecraft189InventoryHotbarControl inventory,
            final int previousSlot) {
        if (inventory == null || inventory != ownerInventory
                || previousSlot != previous || previousSlot < 0) return;
        try {
            if (restore.get() && selected >= 0
                    && inventory.customMcSelectedHotbarSlot() == selected)
                inventory.customMcSetSelectedHotbarSlot(previousSlot);
        } finally {
            clearLease();
        }
    }

    private void clearLease() {
        ownerInventory = null;
        selected = -1;
        previous = -1;
    }
}
