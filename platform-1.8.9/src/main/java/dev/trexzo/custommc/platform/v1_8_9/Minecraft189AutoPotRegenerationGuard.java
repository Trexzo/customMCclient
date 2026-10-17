package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Optional conservative veto of redundant vanilla splash regeneration.
 *
 * Both the actual selected original ItemStack and the same original player's
 * active PotionEffects must be read again at native selection and at the
 * last right-click boundary. Instant Health is NEVER blocked by regeneration.
 * No item display name or cached UI potion snapshot grants authority.
 */
final class Minecraft189AutoPotRegenerationGuard {
    static final int REGENERATION_EFFECT_ID = 10;
    static final int MINIMUM_REMAINING_TICKS = 40;

    private Minecraft189AutoPotRegenerationGuard() {}

    static boolean isRegenerationSplash(final int itemDamage) {
        return itemDamage == 16385 || itemDamage == 16417
                || itemDamage == 16449;
    }

    static boolean mayUse(final Minecraft189InventoryHotbarControl inventory,
            final Minecraft189PlayerInventoryAccess sourcePlayer) {
        if (!(inventory instanceof Minecraft189InventoryHotbarItemsAccess)
                || !(sourcePlayer instanceof Minecraft189PlayerPotionEffectsAccess)
                || sourcePlayer.customMcInventory() != inventory)
            return false;

        final Minecraft189InventoryHotbarItemsAccess sourceInventory =
                (Minecraft189InventoryHotbarItemsAccess) inventory;
        final int slot = sourceInventory.customMcSelectedHotbarSlot();
        if (slot < 0 || slot >= 9) return false;
        final Minecraft189ItemStackAccess[] first =
                sourceInventory.customMcHotbarItems();
        if (first == null || first.length < 9) return false;
        final Minecraft189ItemStackAccess item = first[slot];
        if (item == null || !item.customMcIsPotion()
                || item.customMcStackSize() <= 0)
            return false;

        final int damage = item.customMcItemDamage();
        if (!Minecraft189LegacySplashHealingPotion
                .isKnownSplashHealing(damage)) return false;

        // Instant Health I/II is needed in an emergency even when
        // slow regeneration is active, so it must remain eligible.
        if (!isRegenerationSplash(damage))
            return sameLiveItem(sourceInventory, sourcePlayer, slot,
                    item, damage);

        final Minecraft189PotionEffectAccess[] effects =
                ((Minecraft189PlayerPotionEffectsAccess) sourcePlayer)
                        .customMcPotionEffects();
        if (effects == null) return false;
        for (final Minecraft189PotionEffectAccess effect : effects) {
            if (effect == null) return false;
            final int id = effect.customMcPotionId();
            final int duration = effect.customMcDurationTicks();
            // Conservative: unknown/negative original effect duration does
            // not authorize spending a regeneration splash potion.
            if (duration < 0) return false;
            if (id == REGENERATION_EFFECT_ID
                    && duration >= MINIMUM_REMAINING_TICKS)
                return false;
        }
        return sameLiveItem(sourceInventory, sourcePlayer, slot,
                item, damage);
    }

    private static boolean sameLiveItem(
            final Minecraft189InventoryHotbarItemsAccess inventory,
            final Minecraft189PlayerInventoryAccess player,
            final int slot, final Minecraft189ItemStackAccess item,
            final int damage) {
        final Minecraft189ItemStackAccess[] now =
                inventory.customMcHotbarItems();
        return now != null && now.length >= 9
                && player.customMcInventory() == inventory
                && inventory.customMcSelectedHotbarSlot() == slot
                && now[slot] == item
                && item.customMcIsPotion() && item.customMcStackSize() > 0
                && item.customMcItemDamage() == damage
                && inventory.customMcSelectedHotbarSlot() == slot
                && inventory.customMcHotbarItems() != null
                && inventory.customMcHotbarItems().length >= 9
                && inventory.customMcHotbarItems()[slot] == item;
    }
}
