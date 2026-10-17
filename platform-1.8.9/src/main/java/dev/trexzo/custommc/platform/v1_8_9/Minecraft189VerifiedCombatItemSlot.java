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
     * Native source ItemStack fields can change *in place* during the
     * mapped class getter while the Java wrapper remains in the selected
     * array slot. A single early count/type read is not sufficient.
     * Re-read exact source class and positive count after checking native
     * hotbar membership, then verify membership again to close a
     * reentrant callback during the second metadata read.
     */
    private static boolean stableRodSourceItem(
            final Minecraft189InventoryHotbarControl inventory,
            final Minecraft189PlayerHeldItemAccess player,
            final Minecraft189ItemStackAccess item) {
        return item != null && item.customMcStackSize() > 0
                && item.customMcIsFishingRod()
                && stillSelectedSourceStack(inventory, player, item)
                && item.customMcIsFishingRod()
                && item.customMcStackSize() > 0
                && stillSelectedSourceStack(inventory, player, item);
    }

    private static boolean stablePotionSourceItem(
            final Minecraft189InventoryHotbarControl inventory,
            final Minecraft189PlayerHeldItemAccess player,
            final Minecraft189ItemStackAccess item) {
        return item != null && item.customMcStackSize() > 0
                && item.customMcIsPotion()
                && stillSelectedSourceStack(inventory, player, item)
                && item.customMcIsPotion()
                && item.customMcStackSize() > 0
                && stillSelectedSourceStack(inventory, player, item);
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
        return stableRodSourceItem(inventory, player, item)
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
        return stablePotionSourceItem(inventory, player, item)
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
            matches = module.ownsSelectedSlot(inventory, original)
                    && stableRodSourceItem(inventory, player, selected)
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
            matches = module.ownsSelectedSlot(inventory, original)
                    && stablePotionSourceItem(inventory, player, selected)
                    && module.ownsSelectedSlot(inventory, original);
            return matches ? original : -1;
        } finally {
            if (!matches) module.restoreSlot(inventory, original);
        }
    }
}
