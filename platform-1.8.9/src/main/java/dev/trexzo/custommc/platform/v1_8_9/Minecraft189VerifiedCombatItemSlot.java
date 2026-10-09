package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Validates the *newly selected* mapped item, not an old pre-switch snapshot.
 * A failed/unknown type never authorizes native use-item.
 */
final class Minecraft189VerifiedCombatItemSlot {
    private Minecraft189VerifiedCombatItemSlot() {}

    static int selectRod(final Minecraft189AutoRodModule module,
            final Minecraft189InventoryHotbarControl inventory,
            final Minecraft189PlayerHeldItemAccess player) {
        final int original = module.selectSlot(inventory);
        if (original < 0) return -1;
        boolean matches = false;
        try {
            final Minecraft189ItemStackAccess selected = player == null
                    ? null : player.customMcHeldItem();
            matches = selected != null && selected.customMcIsFishingRod();
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
            matches = selected != null && selected.customMcIsPotion();
            return matches ? original : -1;
        } finally {
            if (!matches) module.restoreSlot(inventory, original);
        }
    }
}
