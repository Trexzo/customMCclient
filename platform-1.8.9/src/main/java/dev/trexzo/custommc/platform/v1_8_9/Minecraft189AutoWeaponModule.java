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
        selected = -1;
        previous = -1;
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
        inventory.customMcSetSelectedHotbarSlot(bestSlot);
        selected = bestSlot;
        previous = prior;
        return prior;
    }

    synchronized void restore(final Minecraft189InventoryHotbarControl inventory,
            final int previousSlot) {
        if (inventory != null && previousSlot >= 0 && previousSlot == previous
                && restore.get() && selected >= 0
                && inventory.customMcSelectedHotbarSlot() == selected)
            inventory.customMcSetSelectedHotbarSlot(previousSlot);
        selected = -1;
        previous = -1;
    }
}
