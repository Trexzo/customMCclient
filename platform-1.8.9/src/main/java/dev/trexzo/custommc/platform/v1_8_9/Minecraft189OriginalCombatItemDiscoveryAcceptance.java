package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Opt-in genuine pinned Mojang 1.8.9 hotbar acceptance after vanilla /give.
 * Requires real transformed ItemStack subclasses in InventoryPlayer, never
 * creates synthetic stacks, bypasses rightClickMouse or claims a server cast.
 */
final class Minecraft189OriginalCombatItemDiscoveryAcceptance {
    private static final String PROPERTY =
            "custommc.acceptance.requireNativeRodPotionDiscovery";
    private static int phase;
    private static int stableTicks;
    private static Minecraft189PlayerInventoryAccess owner;
    private static Minecraft189InventoryHotbarItemsAccess inventory;
    private static int baseline = -1;

    private Minecraft189OriginalCombatItemDiscoveryAcceptance() {}

    static synchronized void observe(final Minecraft189HostRuntime host,
            final Minecraft189PlayerInventoryAccess source) {
        if (!Boolean.getBoolean(PROPERTY) || phase == 5 || source == null
                || !host.playerMovementState().snapshot().available())
            return;
        if (!(source instanceof Minecraft189PlayerHeldItemAccess))
            throw new IllegalStateException("original local player missing mapped held ItemStack");
        final Minecraft189InventoryHotbarAccess nativeInventory =
                source.customMcInventory();
        if (!(nativeInventory instanceof Minecraft189InventoryHotbarItemsAccess))
            throw new IllegalStateException("original InventoryPlayer missing source mainInventory");
        final Minecraft189InventoryHotbarItemsAccess mapped =
                (Minecraft189InventoryHotbarItemsAccess) nativeInventory;
        final int selected = mapped.customMcSelectedHotbarSlot();
        if (selected < 0 || selected > 8) return;
        final boolean gui = host.clickGuiRuntime()
                .coreRuntime().model().snapshot().open();
        // The genuine player is also the source-mapped held-item owner;
        // never use a fabricated player or stale snapshot.
        host.playerHeldItem((Minecraft189PlayerHeldItemAccess) source);

        if (phase == 0) {
            if (gui || owner != source || inventory != mapped
                    || baseline != selected) {
                owner = source;
                inventory = mapped;
                baseline = selected;
                stableTicks = 0;
            }
            if (++stableTicks < 12) return;
            host.featureCatalog().enableOriginalRodPotionDiscoveryAcceptance();
            assertTrue(host.featureCatalog().autoRod().active()
                    && host.featureCatalog().autoPot().active(),
                    "original module controller did not enable native Rod/Potion");
            phase = 1;
            report("CUSTOMMC_189_NATIVE_ROD_POTION_SOURCE_MODULES_ARMED=YES");
            return;
        }
        assertTrue(owner == source && inventory == mapped && baseline == selected,
                "source player or InventoryPlayer changed across native use acceptance");
        assertTrue(host.featureCatalog().autoRod().active()
                && host.featureCatalog().autoPot().active(),
                "source Rod/Potion controller disabled unexpectedly");

        if (phase == 1) {
            if (gui) return;
            final Minecraft189ItemStackAccess[] items = inventory.customMcHotbarItems();
            if (items == null || items.length < 9) return;
            boolean rod = false;
            boolean water = false;
            boolean heal = false;
            for (final Minecraft189ItemStackAccess item : items) {
                if (item == null || item.customMcStackSize() < 1) continue;
                rod |= item.customMcIsFishingRod();
                if (!item.customMcIsPotion()) continue;
                final int damage = item.customMcItemDamage();
                if (damage == 0) water = true;
                if (Minecraft189LegacySplashHealingPotion
                        .isKnownSplashHealing(damage)) heal = true;
            }
            if (!rod || !water || heal) return;
            assertTrue(host.selectAutoPotSlot() == -1,
                    "real source water ItemPotion must not acquire AutoPot");
            assertTrue(inventory.customMcSelectedHotbarSlot() == baseline,
                    "rejected water ItemPotion changed original currentItem");
            phase = 2;
            report("CUSTOMMC_OFFICIAL_189_ROD_POTION_NATIVE_NONHEALING_VETO_PASS=YES");
            return;
        }

        if (phase == 2) {
            if (gui) return;
            final Minecraft189ItemStackAccess[] items = inventory.customMcHotbarItems();
            if (items == null || items.length < 9) return;
            int rodSlot = -1;
            int freshRodSlot = -1;
            int potSlot = -1;
            int strongestSlot = -1;
            for (int i = 0; i < 9; i++) {
                final Minecraft189ItemStackAccess stack = items[i];
                if (stack == null || stack.customMcStackSize() < 1) continue;
                if (stack.customMcIsFishingRod()) {
                    if (stack.customMcItemDamage() == 60 && rodSlot < 0)
                        rodSlot = i;
                    if (stack.customMcItemDamage() == 0
                            && stack.customMcMaxDamage() > 0
                            && freshRodSlot < 0)
                        freshRodSlot = i;
                }
                if (stack.customMcIsPotion()) {
                    final int damage = stack.customMcItemDamage();
                    if (damage == 16389 && potSlot < 0) potSlot = i;
                    if (damage == 16421 && strongestSlot < 0) strongestSlot = i;
                }
            }
            // Explicit positive item evidence is required; empty creative
            // world and an unfulfilled /give must never emit the PASS marker.
            if (rodSlot < 0 || freshRodSlot < 0 || potSlot < 0
                    || strongestSlot < 0 || rodSlot == freshRodSlot
                    || rodSlot == potSlot || rodSlot == strongestSlot) return;
            assertTrue(inventory.customMcHotbarItems()[rodSlot] == items[rodSlot]
                    && inventory.customMcHotbarItems()[potSlot] == items[potSlot],
                    "original /give ItemStack membership changed during scan");
            // Set deliberately incorrect *configured* slots; only V131's
            // opt-in native scan can locate the actual source items.
            host.featureCatalog().autoRod().rodSlotSetting().set(
                    Integer.valueOf(((rodSlot + 1) % 9) + 1));
            host.featureCatalog().autoPot().potSlotSetting().set(
                    Integer.valueOf(((potSlot + 1) % 9) + 1));
            assertTrue(host.featureCatalog().autoRod().autoDetectSlotSetting().get()
                    && host.featureCatalog().autoPot().autoDetectSlotSetting().get(),
                    "source scan was not enabled through persisted settings");
            assertTrue(!host.featureCatalog().autoRod()
                    .preferDurableRodSetting().get(),
                    "V135 default durability preference must preserve V131 selection");
            verifyRod(host, rodSlot);
            host.featureCatalog().autoRod()
                    .preferDurableRodSetting().set(Boolean.TRUE);
            verifyRod(host, freshRodSlot);
            report("CUSTOMMC_OFFICIAL_189_AUTOROD_NATIVE_DURABLE_ROD_PRIORITY_PASS=YES");
            assertTrue(!host.featureCatalog().autoPot()
                    .preferImmediateHealingSetting().get(),
                    "V134 default priority must preserve V131 legacy scan ordering");
            verifyPotion(host, potSlot);
            host.featureCatalog().autoPot()
                    .preferImmediateHealingSetting().set(Boolean.TRUE);
            verifyPotion(host, strongestSlot);
            report("CUSTOMMC_OFFICIAL_189_AUTOPOT_NATIVE_STRONG_HEAL_PRIORITY_PASS=YES");
            phase = 3;
            report("CUSTOMMC_OFFICIAL_189_ROD_POTION_NATIVE_SOURCE_SELECTION_PASS=YES");
            return;
        }
        if (phase == 3) {
            if (!gui) return;
            assertTrue(host.selectAutoRodSlot() == -1
                    && host.selectAutoPotSlot() == -1,
                    "physical ClickGUI should veto both genuine source item leases");
            assertTrue(inventory.customMcSelectedHotbarSlot() == baseline,
                    "GUI veto overwrote genuine user selected currentItem");
            phase = 4;
            report("CUSTOMMC_OFFICIAL_189_ROD_POTION_NATIVE_GUI_VETO_PASS=YES");
            return;
        }
        if (phase == 4 && !gui) {
            final Minecraft189ItemStackAccess[] items = inventory.customMcHotbarItems();
            int rod = -1;
            int mostRodDurability = -1;
            int pot = -1;
            int priority = 0;
            if (items == null || items.length < 9)
                throw new IllegalStateException("original /give inventory disappeared");
            for (int i = 0; i < 9; i++) {
                if (items[i] == null || items[i].customMcStackSize() < 1) continue;
                if (items[i].customMcIsFishingRod()) {
                    final int maximum = items[i].customMcMaxDamage();
                    final int damage = items[i].customMcItemDamage();
                    if (maximum > 0 && damage >= 0 && damage < maximum
                            && maximum - damage > mostRodDurability) {
                        rod = i;
                        mostRodDurability = maximum - damage;
                    }
                }
                if (items[i].customMcIsPotion()) {
                    final int score = Minecraft189LegacySplashHealingPotion
                            .immediateHealingPriority(items[i].customMcItemDamage());
                    if (score > priority) {
                        priority = score;
                        pot = i;
                    }
                }
            }
            assertTrue(rod >= 0 && pot >= 0, "original source items vanished before GUI-close");
            verifyRod(host, rod);
            verifyPotion(host, pot);
            host.featureCatalog().disableOriginalRodPotionDiscoveryAcceptance();
            assertTrue(!host.featureCatalog().autoRod().active()
                    && !host.featureCatalog().autoPot().active(),
                    "normal native module controller disable failed");
            assertTrue(inventory.customMcSelectedHotbarSlot() == baseline,
                    "source InventoryPlayer currentItem not restored after native disable");
            phase = 5;
            report("CUSTOMMC_OFFICIAL_189_ROD_POTION_NATIVE_GUI_CLOSE_RESTORE_PASS=YES");
        }
    }

    private static void verifyRod(final Minecraft189HostRuntime host,
            final int expectedSlot) {
        final int token = host.selectAutoRodSlot();
        assertTrue(token == baseline, "original rod discovery did not capture currentItem");
        try {
            assertTrue(inventory.customMcSelectedHotbarSlot() == expectedSlot,
                    "source rod slot did not use original ItemFishingRod");
            assertTrue(host.mayExecuteAutoRodUse(token),
                    "original source rod final-use gate rejected real /give ItemStack");
            assertTrue(!host.mayExecuteAutoRodUse((baseline + 2) % 9),
                    "foreign original rod token authorized use");
        } finally {
            host.restoreAutoRodSlot(token);
        }
        assertTrue(inventory.customMcSelectedHotbarSlot() == baseline,
                "native Rod finally did not restore original currentItem");
    }

    private static void verifyPotion(final Minecraft189HostRuntime host,
            final int expectedSlot) {
        final int token = host.selectAutoPotSlot();
        assertTrue(token == baseline, "original potion discovery did not capture currentItem");
        try {
            assertTrue(inventory.customMcSelectedHotbarSlot() == expectedSlot,
                    "source potion slot did not use original ItemPotion");
            assertTrue(host.mayExecuteAutoPotUse(token),
                    "original source potion final-use gate rejected real /give ItemStack");
            assertTrue(!host.mayExecuteAutoPotUse((baseline + 2) % 9),
                    "foreign original potion token authorized use");
        } finally {
            host.restoreAutoPotSlot(token);
        }
        assertTrue(inventory.customMcSelectedHotbarSlot() == baseline,
                "native Potion finally did not restore original currentItem");
    }

    private static void assertTrue(final boolean result, final String message) {
        if (!result) throw new IllegalStateException(message);
    }

    private static void report(final String message) {
        System.out.println(message);
        System.out.flush();
    }
}
