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

    /**
     * Opt-in conservation scans only actually usable original rods. Legacy
     * held-first and V135 strongest-durability priorities remain separate.
     */
    private static int discoverProtectedRod(
            final Minecraft189InventoryHotbarControl inventory,
            final int minimumRemaining, final boolean preferMostDurable) {
        if (!(inventory instanceof Minecraft189InventoryHotbarItemsAccess))
            return -1;
        final Minecraft189InventoryHotbarItemsAccess hotbar =
                (Minecraft189InventoryHotbarItemsAccess) inventory;
        final int original = inventory.customMcSelectedHotbarSlot();
        if (original < 0 || original >= 9) return -1;
        if (preferMostDurable) {
            int chosen = -1;
            int best = -1;
            for (int slot = 0; slot < 9; slot++) {
                if (inventory.customMcSelectedHotbarSlot() != original)
                    return -1;
                final int remaining = verifiedRodRemainingDurability(
                        hotbar, original, slot);
                if (remaining < minimumRemaining) continue;
                if (remaining > best
                        || (remaining == best && slot == original)) {
                    best = remaining;
                    chosen = slot;
                }
            }
            return chosen >= 0 && inventory.customMcSelectedHotbarSlot()
                    == original
                    && verifiedRodRemainingDurability(
                            hotbar, original, chosen) >= minimumRemaining
                    ? chosen : -1;
        }
        // Original held-first, then lowest real hotbar index among viable rods.
        final int held = verifiedRodRemainingDurability(hotbar, original, original);
        if (held >= minimumRemaining) return original;
        for (int slot = 0; slot < 9; slot++) {
            if (inventory.customMcSelectedHotbarSlot() != original)
                return -1;
            if (slot != original
                    && verifiedRodRemainingDurability(
                            hotbar, original, slot) >= minimumRemaining
                    && verifiedRodRemainingDurability(
                            hotbar, original, slot) >= minimumRemaining)
                return slot;
        }
        return -1;
    }

    private static boolean stableProtectedRodSourceItem(
            final Minecraft189InventoryHotbarControl inventory,
            final Minecraft189PlayerHeldItemAccess player,
            final Minecraft189ItemStackAccess rod,
            final int minimumRemaining) {
        if (!stableRodSourceItem(inventory, player, rod)) return false;
        final int maximum = rod.customMcMaxDamage();
        final int damage = rod.customMcItemDamage();
        if (maximum <= 0 || damage < 0 || damage >= maximum
                || maximum - damage < minimumRemaining
                || !stillSelectedSourceStack(inventory, player, rod))
            return false;
        if (rod.customMcMaxDamage() != maximum
                || rod.customMcItemDamage() != damage
                || !stableRodSourceItem(inventory, player, rod))
            return false;
        // Even the *second* native getter or source array membership
        // check can run a reentrant callback that wears the same ItemStack.
        // Verify actual remaining uses AFTER those getters, not only from
        // the original pre-callback snapshot.
        final int finalMax = rod.customMcMaxDamage();
        final int finalDamage = rod.customMcItemDamage();
        return finalMax == maximum && finalDamage == damage
                && finalMax > 0 && finalDamage >= 0
                && finalDamage < finalMax
                && finalMax - finalDamage >= minimumRemaining
                && stillSelectedSourceStack(inventory, player, rod);
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

    /**
     * V142 prevents an unusable Regeneration splash from monopolizing the
     * Auto Detect selection when another source healing potion is valid.
     * This is an entirely read-only candidate scan; the same source setter,
     * original ItemStack checks and final use gate are still required.
     */
    private static int discoverUsefulPotion(
            final Minecraft189InventoryHotbarControl control,
            final Minecraft189PlayerInventoryAccess owner,
            final boolean preferImmediate, final float missingHealth,
            final boolean conserveStrong) {
        if (!(control instanceof Minecraft189InventoryHotbarItemsAccess)
                || owner == null || owner.customMcInventory() != control)
            return -1;
        final Minecraft189InventoryHotbarItemsAccess inv =
                (Minecraft189InventoryHotbarItemsAccess) control;
        final int original = inv.customMcSelectedHotbarSlot();
        if (original < 0 || original > 8) return -1;
        int choice = -1;
        int strongest = 0;
        // Held-first when unranked. With V134 rank option on, maximize
        // source vanilla heal priority but skip any redundant regeneration.
        for (int n = 0; n < 9; n++) {
            final int slot = n == 0 ? original : (n <= original ? n - 1 : n);
            if (inv.customMcSelectedHotbarSlot() != original) return -1;
            if (!nativeCandidate(inv, original, slot, false)
                    || !Minecraft189AutoPotRegenerationGuard
                            .maySelectAtSlot(inv, owner, slot, original))
                continue;
            if (!preferImmediate) {
                choice = slot;
                break;
            }
            final int priority = verifiedHealingPriority(inv, original, slot,
                    missingHealth, conserveStrong);
            if (priority > strongest) {
                strongest = priority;
                choice = slot;
            }
        }
        if (choice < 0
                || inv.customMcSelectedHotbarSlot() != original
                || !nativeCandidate(inv, original, choice, false)
                || !Minecraft189AutoPotRegenerationGuard
                        .maySelectAtSlot(inv, owner, choice, original))
            return -1;
        return choice;
    }

    private static int discoverHighestHealing(
            final Minecraft189InventoryHotbarItemsAccess inventory,
            final int original) {
        return discoverHighestHealing(inventory, original, Float.NaN, false);
    }

    private static int discoverHighestHealing(
            final Minecraft189InventoryHotbarItemsAccess inventory,
            final int original, final float missingHealth,
            final boolean conserveStrong) {
        int bestSlot = -1;
        int bestPriority = 0;
        for (int slot = 0; slot < 9; slot++) {
            if (inventory.customMcSelectedHotbarSlot() != original)
                return -1;
            final int priority = verifiedHealingPriority(
                    inventory, original, slot, missingHealth, conserveStrong);
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
        return verifiedHealingPriority(
                inventory, original, slot, Float.NaN, false);
    }

    private static int verifiedHealingPriority(
            final Minecraft189InventoryHotbarItemsAccess inventory,
            final int original, final int slot,
            final float missingHealth, final boolean conserveStrong) {
        final Minecraft189ItemStackAccess[] snapshot =
                inventory.customMcHotbarItems();
        if (snapshot == null || snapshot.length < 9) return 0;
        final Minecraft189ItemStackAccess candidate = snapshot[slot];
        if (candidate == null || candidate.customMcStackSize() <= 0
                || !candidate.customMcIsPotion()) return 0;
        final int damage = candidate.customMcItemDamage();
        final int priority = Minecraft189LegacySplashHealingPotion
                .healthAwarePriority(damage, missingHealth, conserveStrong);
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
        return module.ownsVerifiedItem(inventory, original, item)
                && (module.protectRodDurabilitySetting().get().booleanValue()
                    ? stableProtectedRodSourceItem(inventory, player, item,
                            module.minimumRemainingDurabilitySetting().get())
                    : stableRodSourceItem(inventory, player, item))
                && module.ownsVerifiedItem(inventory, original, item);
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
        return module.ownsVerifiedItem(inventory, original, item)
                && stablePotionSourceItem(inventory, player, item)
                && module.ownsVerifiedItem(inventory, original, item);
    }

    static int selectRod(final Minecraft189AutoRodModule module,
            final Minecraft189InventoryHotbarControl inventory,
            final Minecraft189PlayerHeldItemAccess player) {
        // Read the GUI intent once BEFORE the original inventory scan.
        // A reentrant source getter can change Auto Detect, durability
        // priority or the configured slot while ranking source items.
        final boolean protecting =
                module.protectRodDurabilitySetting().get().booleanValue();
        final int minimum =
                module.minimumRemainingDurabilitySetting().get().intValue();
        final boolean autodetect =
                module.autoDetectSlotSetting().get().booleanValue();
        final boolean durable =
                module.preferDurableRodSetting().get().booleanValue();
        final int configured = module.rodSlotSetting().get().intValue();
        final boolean preserveHook =
                module.preserveActiveHookSetting().get().booleanValue();
        final boolean requireHold =
                module.requireHoldSetting().get().booleanValue();
        final boolean pauseRight =
                module.pauseRightSetting().get().booleanValue();
        final int cooldown =
                module.cooldownTicksSetting().get().intValue();
        final int candidate = autodetect
                ? (protecting
                        ? discoverProtectedRod(inventory, minimum, durable)
                        : discover(inventory, true, false, durable))
                : configured - 1;
        if (protecting != module.protectRodDurabilitySetting().get()
                || minimum != module.minimumRemainingDurabilitySetting().get()
                || autodetect != module.autoDetectSlotSetting().get()
                || durable != module.preferDurableRodSetting().get()
                || configured != module.rodSlotSetting().get()
                || preserveHook != module.preserveActiveHookSetting().get()
                || requireHold != module.requireHoldSetting().get()
                || pauseRight != module.pauseRightSetting().get()
                || cooldown != module.cooldownTicksSetting().get())
            return -1;
        final int original = module.selectSlot(inventory, candidate);
        if (original < 0) return -1;
        boolean matches = false;
        try {
            final Minecraft189ItemStackAccess selected = player == null
                    ? null : player.customMcHeldItem();
            matches = module.ownsSelectedSlot(inventory, original)
                    && (protecting
                            ? stableProtectedRodSourceItem(
                                    inventory, player, selected, minimum)
                            : stableRodSourceItem(inventory, player, selected))
                    && module.ownsSelectedSlot(inventory, original)
                    && module.bindVerifiedItem(inventory, original, selected);
            return matches ? original : -1;
        } finally {
            if (!matches) module.restoreSlot(inventory, original);
        }
    }

    /**
     * The optional heal-conservation preference is allowed only from this
     * exact native player, which must own the actual source hotbar. A stale
     * health HUD or another EntityPlayer's health cannot rank potions.
     */
    private static float nativeMissingHealth(
            final Minecraft189PlayerHeldItemAccess player,
            final Minecraft189InventoryHotbarControl inventory) {
        if (!(player instanceof Minecraft189PlayerHealthAccess)
                || !(player instanceof Minecraft189PlayerInventoryAccess)
                || ((Minecraft189PlayerInventoryAccess) player)
                        .customMcInventory() != inventory)
            return Float.NaN;
        final Minecraft189PlayerHealthAccess health =
                (Minecraft189PlayerHealthAccess) player;
        final float maximum = health.customMcMaxHealth();
        final float current = health.customMcHealth();
        if (!Float.isFinite(maximum) || !Float.isFinite(current)
                || maximum <= 0F || current <= 0F || current > maximum)
            return Float.NaN;
        return maximum - current;
    }

    private static int discoverHealthAware(
            final Minecraft189InventoryHotbarControl control,
            final float missingHealth) {
        if (!(control instanceof Minecraft189InventoryHotbarItemsAccess))
            return -1;
        final Minecraft189InventoryHotbarItemsAccess source =
                (Minecraft189InventoryHotbarItemsAccess) control;
        final int selected = source.customMcSelectedHotbarSlot();
        if (selected < 0 || selected >= 9) return -1;
        return discoverHighestHealing(source, selected, missingHealth, true);
    }

    static int selectPotion(final Minecraft189AutoPotModule module,
            final Minecraft189InventoryHotbarControl inventory,
            final Minecraft189PlayerHeldItemAccess player) {
        final boolean autodetect = module.autoDetectSlotSetting().get()
                .booleanValue();
        final boolean avoidDuplicate = module.avoidActiveRegenerationSetting()
                .get().booleanValue();
        final boolean priority = module.preferImmediateHealingSetting().get()
                .booleanValue();
        final boolean conserveConfigured =
                module.conserveInstantHealingIISetting().get().booleanValue();
        final int configured = module.potSlotSetting().get().intValue();
        final boolean aimDown =
                module.aimDownForSplashSetting().get().booleanValue();
        final double healthThreshold =
                module.healthThresholdSetting().get().doubleValue();
        final boolean requireTarget =
                module.requireCombatTargetSetting().get().booleanValue();
        final int cooldown =
                module.cooldownTicksSetting().get().intValue();
        final boolean conserve = autodetect && priority && conserveConfigured;
        final float missingHealth = conserve
                ? nativeMissingHealth(player, inventory) : Float.NaN;
        if (conserve && (!Float.isFinite(missingHealth)
                || missingHealth <= 0F))
            return -1; // No genuine live low-health ranking witness.
        final int candidate = autodetect
                ? (avoidDuplicate
                    ? discoverUsefulPotion(inventory,
                            player instanceof Minecraft189PlayerInventoryAccess
                                ? (Minecraft189PlayerInventoryAccess) player
                                : null, priority, missingHealth, conserve)
                    : (conserve
                        ? discoverHealthAware(inventory, missingHealth)
                        : discover(inventory, false, priority, false)))
                : configured - 1;
        // Abort a stale candidate if an original ItemStack getter changed
        // any selection policy during the scan; never quietly bind a newly
        // configured policy to an old, already-picked potion.
        if (autodetect != module.autoDetectSlotSetting().get()
                || avoidDuplicate != module.avoidActiveRegenerationSetting().get()
                || priority != module.preferImmediateHealingSetting().get()
                || conserveConfigured != module.conserveInstantHealingIISetting().get()
                || configured != module.potSlotSetting().get()
                || aimDown != module.aimDownForSplashSetting().get()
                || Double.compare(healthThreshold,
                        module.healthThresholdSetting().get()) != 0
                || requireTarget != module.requireCombatTargetSetting().get()
                || cooldown != module.cooldownTicksSetting().get()
                || (conserve && nativeMissingHealth(player, inventory) != missingHealth))
            return -1;
        final int original = module.selectSlot(inventory, candidate);
        if (original < 0) return -1;
        boolean matches = false;
        try {
            final Minecraft189ItemStackAccess selected = player == null
                    ? null : player.customMcHeldItem();
            matches = module.ownsSelectedSlot(inventory, original)
                    && stablePotionSourceItem(inventory, player, selected)
                    && (!conserve || nativeMissingHealth(player, inventory)
                            == missingHealth)
                    && module.ownsSelectedSlot(inventory, original)
                    && module.bindVerifiedItem(inventory, original, selected);
            return matches ? original : -1;
        } finally {
            if (!matches) module.restoreSlot(inventory, original);
        }
    }
}
