package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Validates the *newly selected* mapped item, not an old pre-switch snapshot.
 * A failed/unknown type never authorizes native use-item.
 */
final class Minecraft189VerifiedCombatItemSlot {
    private Minecraft189VerifiedCombatItemSlot() {}

    /**
     * The original 1.8.9 held-item getter returns a source ItemStack from
     * EntityLivingBase.getEquipmentInSlot(0), not an immutable snapshot.
     * A getter reading its type/stack count may reenter inventory sync and
     * replace the selected mainInventory entry without moving currentItem.
     * Metadata of the displaced wrapper must not authorize a right click.
     *
     * Require the same source ItemStack be reachable both from the player
     * and (when mapped) InventoryPlayer.mainInventory on either side of
     * the final native read. InventoryHotbarControl-only test adapters still
     * need a stable pair of held-item reads. This is a synchronous
     * observation, not a global atomic inventory transaction.
     */
    private static boolean stillSelectedSourceStack(
            final Minecraft189InventoryHotbarControl inventory,
            final Minecraft189PlayerHeldItemAccess player,
            final Minecraft189ItemStackAccess observed) {
        if (inventory == null || player == null || observed == null)
            return false;
        final int slot = inventory.customMcSelectedHotbarSlot();
        if (slot < 0 || slot > 8) return false;
        if (inventory instanceof Minecraft189InventoryHotbarItemsAccess) {
            final Minecraft189ItemStackAccess[] before =
                    ((Minecraft189InventoryHotbarItemsAccess) inventory)
                            .customMcHotbarItems();
            if (before == null || before.length <= slot
                    || before[slot] != observed) return false;
        }
        if (player.customMcHeldItem() != observed
                || inventory.customMcSelectedHotbarSlot() != slot)
            return false;
        if (inventory instanceof Minecraft189InventoryHotbarItemsAccess) {
            final Minecraft189ItemStackAccess[] after =
                    ((Minecraft189InventoryHotbarItemsAccess) inventory)
                            .customMcHotbarItems();
            if (after == null || after.length <= slot
                    || after[slot] != observed) return false;
        }
        return inventory.customMcSelectedHotbarSlot() == slot;
    }

    /**
     * Final synchronous boundary immediately before native right-click.
     * Revalidate mapped item class, positive stack size and the captured
     * InventoryPlayer.currentItem lease AFTER any AutoBlock release hooks.
     * Neither matching item type alone nor a stale prior check can permit
     * a synthetic use of a different user-selected hotbar slot.
     */
    static boolean stillOwnsRodAtUse(
            final Minecraft189AutoRodModule module,
            final Minecraft189InventoryHotbarControl inventory,
            final Minecraft189PlayerHeldItemAccess player,
            final int original) {
        if (module == null || inventory == null || player == null
                || original < 0 || !module.ownsSelectedSlot(inventory, original))
            return false;
        final Minecraft189ItemStackAccess item = player.customMcHeldItem();
        return item != null && item.customMcStackSize() > 0
                && item.customMcIsFishingRod()
                && module.ownsSelectedSlot(inventory, original)
                && stillSelectedSourceStack(inventory, player, item)
                && module.ownsSelectedSlot(inventory, original);
    }

    static boolean stillOwnsPotionAtUse(
            final Minecraft189AutoPotModule module,
            final Minecraft189InventoryHotbarControl inventory,
            final Minecraft189PlayerHeldItemAccess player,
            final int original) {
        if (module == null || inventory == null || player == null
                || original < 0 || !module.ownsSelectedSlot(inventory, original))
            return false;
        final Minecraft189ItemStackAccess item = player.customMcHeldItem();
        return item != null && item.customMcStackSize() > 0
                && item.customMcIsPotion()
                && module.ownsSelectedSlot(inventory, original)
                && stillSelectedSourceStack(inventory, player, item)
                && module.ownsSelectedSlot(inventory, original);
    }

    static int selectRod(final Minecraft189AutoRodModule module,
            final Minecraft189InventoryHotbarControl inventory,
            final Minecraft189PlayerHeldItemAccess player) {
        final int original = module.selectSlot(inventory);
        if (original < 0) return -1;
        boolean matches = false;
        try {
            final Minecraft189ItemStackAccess selected = player == null
                    ? null : player.customMcHeldItem();
            matches = selected != null && selected.customMcStackSize() > 0
                    && selected.customMcIsFishingRod()
                    && module.ownsSelectedSlot(inventory, original)
                    && stillSelectedSourceStack(inventory, player, selected)
                    && module.ownsSelectedSlot(inventory, original);
            return matches ? original : -1;
        } finally {
            if (!matches) module.restoreSlot(inventory, original);
        }
    }

    static int selectPotion(final Minecraft189AutoPotModule module,
            final Minecraft189InventoryHotbarControl inventory,
            final Minecraft189PlayerHeldItemAccess player) {
        final int original = module.selectSlot(inventory);
        if (original < 0) return -1;
        boolean matches = false;
        try {
            final Minecraft189ItemStackAccess selected = player == null
                    ? null : player.customMcHeldItem();
            matches = selected != null && selected.customMcStackSize() > 0
                    && selected.customMcIsPotion()
                    && module.ownsSelectedSlot(inventory, original)
                    && stillSelectedSourceStack(inventory, player, selected)
                    && module.ownsSelectedSlot(inventory, original);
            return matches ? original : -1;
        } finally {
            if (!matches) module.restoreSlot(inventory, original);
        }
    }
}
