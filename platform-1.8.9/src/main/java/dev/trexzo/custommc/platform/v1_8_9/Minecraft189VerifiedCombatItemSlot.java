package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Validates the *newly selected* mapped item, not an old pre-switch snapshot.
 * A failed/unknown type never authorizes native use-item.
 */
final class Minecraft189VerifiedCombatItemSlot {
    private Minecraft189VerifiedCombatItemSlot() {}

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
                    && module.ownsSelectedSlot(inventory, original);
            return matches ? original : -1;
        } finally {
            if (!matches) module.restoreSlot(inventory, original);
        }
    }
}
