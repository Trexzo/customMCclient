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
    public static final String IGNORE_FRAGILE_SWORDS = ID + ".ignoreFragileSwords";
    public static final String MIN_REMAINING_DURABILITY = ID + ".minRemainingDurability";
    public static final String PREFER_DURABLE_TIES = ID + ".preferDurableTies";
    private final Setting<Boolean> requireHit = new Setting<Boolean>(
            REQUIRE_HIT, Boolean.TRUE, v -> v != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> restore = new Setting<Boolean>(
            RESTORE, Boolean.TRUE, v -> v != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> ignoreFragileSwords = new Setting<Boolean>(
            IGNORE_FRAGILE_SWORDS, Boolean.FALSE, v -> v != null,
            SettingCodecs.BOOLEAN);
    private final Setting<Integer> minRemainingDurability = new Setting<Integer>(
            MIN_REMAINING_DURABILITY, Integer.valueOf(8),
            v -> v != null && v >= 1 && v <= 128, SettingCodecs.INTEGER);
    private final Setting<Boolean> preferDurableTies = new Setting<Boolean>(
            PREFER_DURABLE_TIES, Boolean.FALSE, v -> v != null,
            SettingCodecs.BOOLEAN);
    private boolean enabled;
    private int selected = -1;
    private int previous = -1;
    private Minecraft189InventoryHotbarControl ownerInventory;
    // ItemStack wrappers may be replaced by vanilla inventory sync. Retain
    // the original mapped Item singleton when available, not the wrapper.
    private Object selectedNativeSwordIdentity;
    private float selectedSwordBaseDamage = Float.NaN;
    private boolean selectedFragilePolicy;
    private int selectedMinimumRemaining;
    private boolean selectedDurableTies;

    @Override public String id() { return ID; }
    @Override public synchronized void onEnable() { enabled = true; }
    @Override public synchronized void onDisable() { enabled = false; }
    public Setting<Boolean> requirePlayerHitSetting() { return requireHit; }
    public Setting<Boolean> restoreAfterAttackSetting() { return restore; }
    public Setting<Boolean> ignoreFragileSwordsSetting() {
        return ignoreFragileSwords;
    }
    public Setting<Integer> minRemainingDurabilitySetting() {
        return minRemainingDurability;
    }
    public Setting<Boolean> preferDurableTiesSetting() {
        return preferDurableTies;
    }

    private static int remainingDurability(final Minecraft189ItemStackAccess item) {
        if (item == null) return -1;
        final int max = item.customMcMaxDamage();
        final int used = item.customMcItemDamage();
        if (max <= 0 || used < 0 || used >= max) return -1;
        return max - used;
    }
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

        final boolean rejectFragile = ignoreFragileSwords.get().booleanValue();
        final int minRemaining = minRemainingDurability.get().intValue();
        final boolean preferDurable = preferDurableTies.get().booleanValue();
        int bestSlot = -1;
        float bestDamage = Float.NEGATIVE_INFINITY;
        int bestRemaining = -1;
        for (int slot = 0; slot < 9; slot++) {
            final Minecraft189ItemStackAccess item = items[slot];
            if (item == null || !item.customMcIsSword()
                    || item.customMcStackSize() < 1) continue;
            final float base = item.customMcSwordBaseDamage();
            if (!Float.isFinite(base) || base <= 0) continue;
            final int remaining = rejectFragile || preferDurable
                    ? remainingDurability(item) : -1;
            if ((rejectFragile || preferDurable) && remaining < 0)
                continue; // Invalid original durability is not evidence.
            if (rejectFragile && remaining < minRemaining) continue;
            if (base > bestDamage
                    || (base == bestDamage && preferDurable
                        && remaining > bestRemaining)
                    || (base == bestDamage
                        && (!preferDurable || remaining == bestRemaining)
                        && slot == prior)) {
                bestDamage = base;
                bestSlot = slot;
                bestRemaining = remaining;
            }
        }
        if (bestSlot < 0 || bestSlot == prior) return -1;
        // Obtaining original inventory item descriptors can execute mapped
        // native getters with reentrant user/currentItem changes. Never
        // overwrite a different actual selected slot based on an old read.
        if (!enabled || inventory.customMcSelectedHotbarSlot() != prior
                || rejectFragile != ignoreFragileSwords.get()
                || minRemaining != minRemainingDurability.get()
                || preferDurable != preferDurableTies.get())
            return -1;
        final Object mappedSwordIdentity =
                items[bestSlot].customMcOriginalItemIdentity();
        if (!enabled || inventory.customMcSelectedHotbarSlot() != prior)
            return -1;
        ownerInventory = inventory;
        selectedNativeSwordIdentity = mappedSwordIdentity;
        selectedSwordBaseDamage = bestDamage;
        selectedFragilePolicy = rejectFragile;
        selectedMinimumRemaining = minRemaining;
        selectedDurableTies = preferDurable;
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
            if (!enabled || !ownsVerifiedSwordAtDispatch(inventory, prior)) {
                // The native setter may reenter inventory synchronization,
                // replace the sword, or disable AutoWeapon. Never publish
                // an apparently successful source lease for that changed
                // ItemStack. Restore only if our originally selected slot
                // still owns native currentItem, preserving a real user
                // choice made by a reentrant callback.
                try {
                    if (inventory.customMcSelectedHotbarSlot() == bestSlot)
                        inventory.customMcSetSelectedHotbarSlot(prior);
                } finally {
                    clearLease();
                }
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

    /**
     * Last source-native admission immediately before Minecraft clickMouse
     * or PlayerControllerMP.attackEntity. A correct hotbar *index* does not
     * prove the ItemSword selected earlier still occupies the slot.
     *
     * Original Item singleton identity is checked when mapped; a fresh
     * ItemStack wrapper holding the same source Item remains acceptable.
     * Null identity is never treated as proof of an unchanged item, so
     * mapped sword type and finite base damage must still agree.
     */
    synchronized boolean ownsVerifiedSwordAtDispatch(
            final Minecraft189InventoryHotbarControl control,
            final int original) {
        if (!enabled || control == null || control != ownerInventory
                || selectedFragilePolicy != ignoreFragileSwords.get()
                || selectedMinimumRemaining != minRemainingDurability.get()
                || selectedDurableTies != preferDurableTies.get()
                || original < 0 || original != previous || selected < 0
                || !(control instanceof Minecraft189InventoryHotbarItemsAccess)
                || control.customMcSelectedHotbarSlot() != selected)
            return false;
        final Minecraft189ItemStackAccess[] items =
                ((Minecraft189InventoryHotbarItemsAccess) control)
                        .customMcHotbarItems();
        if (items == null || items.length <= selected)
            return false;
        final Minecraft189ItemStackAccess item = items[selected];
        if (item == null || !item.customMcIsSword()
                || item.customMcStackSize() < 1)
            return false;
        final float baseDamage = item.customMcSwordBaseDamage();
        if (!Float.isFinite(baseDamage) || baseDamage <= 0.0F
                || baseDamage != selectedSwordBaseDamage)
            return false;
        if (selectedNativeSwordIdentity != null
                && item.customMcOriginalItemIdentity()
                        != selectedNativeSwordIdentity)
            return false;
        if (selectedFragilePolicy || selectedDurableTies) {
            final int remaining = remainingDurability(item);
            if (remaining < 0 || (selectedFragilePolicy
                    && remaining < selectedMinimumRemaining)) return false;
        }
        // Reading ItemSword, stack count, base damage or original Item may
        // reenter inventory synchronization. A native getter can replace
        // InventoryPlayer.mainInventory[selected] without changing
        // currentItem. Do not authorize a click from metadata belonging
        // to a displaced ItemStack wrapper. A replacement *between*
        // independent dispatch checks is fine once revalidated as normal.
        final Minecraft189ItemStackAccess[] current =
                ((Minecraft189InventoryHotbarItemsAccess) control)
                        .customMcHotbarItems();
        if (current == null || current.length <= selected
                || current[selected] != item)
            return false;
        // Native getters may also reenter a GUI change, module disable,
        // or currentItem mutation. Check exact lease again after the
        // stable source ItemStack membership check.
        return enabled && control == ownerInventory && original == previous
                && control.customMcSelectedHotbarSlot() == selected;
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
        selectedNativeSwordIdentity = null;
        selectedSwordBaseDamage = Float.NaN;
        selectedFragilePolicy = false;
        selectedMinimumRemaining = 0;
        selectedDurableTies = false;
        selected = -1;
        previous = -1;
    }
}
