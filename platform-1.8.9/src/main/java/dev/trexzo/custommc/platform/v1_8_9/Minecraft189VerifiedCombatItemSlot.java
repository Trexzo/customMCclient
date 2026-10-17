package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Validates the *newly selected* mapped item, not an old pre-switch snapshot.
 * A failed/unknown type never authorizes native use-item.
 */
final class Minecraft189VerifiedCombatItemSlot {
    private Minecraft189VerifiedCombatItemSlot() {}

    /** Optional scan of the original InventoryPlayer.mainInventory mapping. */
    private static int discover(final Minecraft189InventoryHotbarControl control,
            final boolean rod, final boolean prioritizeInstantHealing,
            final boolean prioritizeRodDurability) {
        if (!(control instanceof Minecraft189InventoryHotbarItemsAccess))
            return -1; // No source array: no guessed fallback.
        final Minecraft189InventoryHotbarItemsAccess inv =
                (Minecraft189InventoryHotbarItemsAccess) control;
        final int prior = control.customMcSelectedHotbarSlot();
        if (prior < 0 || prior > 8) return -1;
        // Existing default is held-first. Opt-in ranks exact vanilla
        // source potion metadata instead; Rod discovery stays untouched.
        if (!rod && prioritizeInstantHealing)
            return discoverHighestHealing(inv, prior);
        if (rod && prioritizeRodDurability)
            return discoverMostDurableRod(inv, prior);
        // Already-held matching source item wins; otherwise lowest slot.
        if (nativeCandidate(inv, prior, prior, rod)) return prior;
        for (int slot = 0; slot < 9; slot++) {
            if (control.customMcSelectedHotbarSlot() != prior) return -1;
            if (slot != prior && nativeCandidate(inv, prior, slot, rod))
                return slot;
        }
        return -1;
    }


    /**
     * V135 source-only fishing-rod choice. Ranking is relative to real
     * source ItemStack.getMaxDamage/getItemDamage, not display names.
     * Equal remaining durability prefers the genuine currently-held item,
     * then the lowest slot. No guess if metadata/owner changes mid-scan.
     */
    private static int discoverMostDurableRod(
            final Minecraft189InventoryHotbarItemsAccess inventory,
            final int original) {
        int bestSlot = -1;
        int mostRemaining = -1;
        for (int slot = 0; slot < 9; slot++) {
            if (inventory.customMcSelectedHotbarSlot() != original)
                return -1;
            final int remaining = verifiedRodRemainingDurability(
                    inventory, original, slot);
            if (remaining > mostRemaining
                    || (remaining >= 0 && remaining == mostRemaining
                        && slot == original)) {
                mostRemaining = remaining;
                bestSlot = slot;
            }
        }
        return inventory.customMcSelectedHotbarSlot() == original
                ? bestSlot : -1;
    }

    /** Unknown, depleted, displaced or reentrant-mutation candidates fail. */
    private static int verifiedRodRemainingDurability(
            final Minecraft189InventoryHotbarItemsAccess inventory,
            final int original, final int slot) {
        final Minecraft189ItemStackAccess[] first =
                inventory.customMcHotbarItems();
        if (first == null || first.length < 9) return -1;
        final Minecraft189ItemStackAccess rod = first[slot];
        if (rod == null || rod.customMcStackSize() <= 0
                || !rod.customMcIsFishingRod()) return -1;
        final int maximum = rod.customMcMaxDamage();
        final int damage = rod.customMcItemDamage();
        if (maximum <= 0 || damage < 0 || damage >= maximum
                || !nativeCandidate(inventory, original, slot, true))
            return -1;
        final Minecraft189ItemStackAccess[] verified =
                inventory.customMcHotbarItems();
        if (verified == null || verified.length < 9
                || verified[slot] != rod
                || inventory.customMcSelectedHotbarSlot() != original
                || rod.customMcMaxDamage() != maximum
                || rod.customMcItemDamage() != damage
                || inventory.customMcSelectedHotbarSlot() != original)
            return -1;
        final Minecraft189ItemStackAccess[] finalArray =
                inventory.customMcHotbarItems();
        return finalArray != null && finalArray.length >= 9
                && finalArray[slot] == rod
                && inventory.customMcSelectedHotbarSlot() == original
                ? maximum - damage : -1;
    }

    private static int discoverHighestHealing(
            final Minecraft189InventoryHotbarItemsAccess inventory,
            final int original) {
        int bestSlot = -1;
        int bestPriority = 0;
        for (int slot = 0; slot < 9; slot++) {
            if (inventory.customMcSelectedHotbarSlot() != original)
                return -1;
            final int priority = verifiedHealingPriority(
                    inventory, original, slot);
            if (priority > bestPriority
                    || (priority > 0 && priority == bestPriority
                        && slot == original)) {
                bestPriority = priority;
                bestSlot = slot;
            }
        }
        return inventory.customMcSelectedHotbarSlot() == original
                ? bestSlot : -1;
    }

    /**
     * ItemStack data and class getters may invoke a reentrant source
     * inventory mutation. Never rank a displaced wrapper, changing
     * metadata, or a different user-selected currentItem. The selected
     * candidate still passes normal native setter and final-use checks.
     */
    private static int verifiedHealingPriority(
            final Minecraft189InventoryHotbarItemsAccess inventory,
            final int original, final int slot) {
        final Minecraft189ItemStackAccess[] snapshot =
                inventory.customMcHotbarItems();
        if (snapshot == null || snapshot.length < 9) return 0;
        final Minecraft189ItemStackAccess candidate = snapshot[slot];
        if (candidate == null || candidate.customMcStackSize() <= 0
                || !candidate.customMcIsPotion()) return 0;
        final int damage = candidate.customMcItemDamage();
        final int priority = Minecraft189LegacySplashHealingPotion
                .immediateHealingPriority(damage);
        if (priority <= 0
                || !nativeCandidate(inventory, original, slot, false))
            return 0;
        final Minecraft189ItemStackAccess[] after =
                inventory.customMcHotbarItems();
        return after != null && after.length >= 9
                && after[slot] == candidate
                && inventory.customMcSelectedHotbarSlot() == original
                && candidate.customMcItemDamage() == damage
                && inventory.customMcSelectedHotbarSlot() == original
                && inventory.customMcHotbarItems() != null
                && inventory.customMcHotbarItems().length >= 9
                && inventory.customMcHotbarItems()[slot] == candidate
                ? priority : 0;
    }

    private static boolean nativeCandidate(
            final Minecraft189InventoryHotbarItemsAccess inv,
            final int prior, final int slot, final boolean rod) {
        final Minecraft189ItemStackAccess[] items = inv.customMcHotbarItems();
        if (items == null || items.length < 9) return false;
        final Minecraft189ItemStackAccess observed = items[slot];
        if (observed == null || observed.customMcStackSize() <= 0)
            return false;
        if (!(rod ? observed.customMcIsFishingRod() : observed.customMcIsPotion()))
            return false;
        if (!rod && !Minecraft189LegacySplashHealingPotion
                .isKnownSplashHealing(observed.customMcItemDamage()))
            return false;
        // Native class/count getters can replace selected ItemStacks and
        // invoke a user hotbar change. Recheck actual array membership.
        final Minecraft189ItemStackAccess[] after = inv.customMcHotbarItems();
        if (after == null || after.length < 9 || after[slot] != observed
                || inv.customMcSelectedHotbarSlot() != prior
                || observed.customMcStackSize() <= 0
                || !(rod ? observed.customMcIsFishingRod()
                        : observed.customMcIsPotion())
                || (!rod && !Minecraft189LegacySplashHealingPotion
                        .isKnownSplashHealing(observed.customMcItemDamage())))
            return false;
        final Minecraft189ItemStackAccess[] stable = inv.customMcHotbarItems();
        return stable != null && stable.length >= 9
                && stable[slot] == observed
                && inv.customMcSelectedHotbarSlot() == prior;
    }


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
                && Minecraft189LegacySplashHealingPotion
                        .isKnownSplashHealing(item.customMcItemDamage())
                && stillSelectedSourceStack(inventory, player, item)
                && item.customMcIsPotion()
                && item.customMcStackSize() > 0
                && Minecraft189LegacySplashHealingPotion
                        .isKnownSplashHealing(item.customMcItemDamage())
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
        final int candidate = module.autoDetectSlotSetting().get().booleanValue()
                ? discover(inventory, true, false,
                        module.preferDurableRodSetting().get().booleanValue())
                : module.rodSlotSetting().get().intValue() - 1;
        final int original = module.selectSlot(inventory, candidate);
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
        final int candidate = module.autoDetectSlotSetting().get().booleanValue()
                ? discover(inventory, false,
                        module.preferImmediateHealingSetting().get()
                                .booleanValue(), false)
                : module.potSlotSetting().get().intValue() - 1;
        final int original = module.selectSlot(inventory, candidate);
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
