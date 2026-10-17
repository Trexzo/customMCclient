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
    private static int physicalCastSlot = -1;
    private static int continuousHookCallbacks;
    private static int sourceRegenerationPotionSlot = -1;
    private static int physicalImpactSlot = -1;
    private static Minecraft189ItemStackAccess physicalImpactPotion;
    private static float physicalImpactOriginalPitch = Float.NaN;
    private static boolean physicalImpactPitchOwned;
    private static int physicalImpactReadyCallbacks;
    private static int physicalImpactEffectCallbacks;

    private Minecraft189OriginalCombatItemDiscoveryAcceptance() {}

    static synchronized void observe(final Minecraft189HostRuntime host,
            final Minecraft189PlayerInventoryAccess source) {
        if (!Boolean.getBoolean(PROPERTY) || phase == 16 || source == null
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
            assertTrue(source instanceof Minecraft189PlayerFishingHookAccess,
                    "original EntityPlayer lacks source-mapped fishEntity getter");
            assertTrue(!((Minecraft189PlayerFishingHookAccess) source)
                            .customMcHasFishingHook(),
                    "original creative baseline unexpectedly has a cast hook");
            report("CUSTOMMC_OFFICIAL_189_ROD_NATIVE_FISH_ENTITY_EMPTY_BASELINE_PASS=YES");
            host.featureCatalog().enableOriginalRodPotionDiscoveryAcceptance();
            host.featureCatalog().autoRod()
                    .preserveActiveHookSetting().set(Boolean.TRUE);
            assertTrue(host.featureCatalog().autoRod().active()
                    && host.featureCatalog().autoPot().active(),
                    "original module controller did not enable native Rod/Potion");
            phase = 1;
            report("CUSTOMMC_189_NATIVE_ROD_POTION_SOURCE_MODULES_ARMED=YES");
            return;
        }
        assertTrue(owner == source && inventory == mapped,
                "original local player/InventoryPlayer changed during native fixture");
        if (phase <= 4) {
            assertTrue(baseline == selected,
                    "original currentItem changed during source selection acceptance");
            assertTrue(host.featureCatalog().autoRod().active()
                    && host.featureCatalog().autoPot().active(),
                    "source Rod/Potion controller disabled unexpectedly");
        }

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
            physicalCastSlot = rod;
            assertTrue(physicalCastSlot != baseline,
                    "X11 source rod cast test needs a physical slot transition");
            phase = 5;
            report("CUSTOMMC_OFFICIAL_189_ROD_POTION_NATIVE_GUI_CLOSE_RESTORE_PASS=YES");
            // The Python runner must use an actual physical hotbar number
            // matching this exact source-verified genuine fishing rod.
            report("CUSTOMMC_OFFICIAL_189_NATIVE_CAST_ROD_SLOT="
                    + (physicalCastSlot + 1));
            return;
        }

        // After old selection/GUI gates, the user physically selects the
        // source rod (one of keys 1..9). No fixture inventory setter or
        // synthetic right-click establishes a successful cast.
        if (phase == 5) {
            if (gui || selected != physicalCastSlot) return;
            final Minecraft189ItemStackAccess[] items =
                    inventory.customMcHotbarItems();
            if (items == null || items.length < 9
                    || items[selected] == null
                    || items[selected].customMcStackSize() <= 0
                    || !items[selected].customMcIsFishingRod()
                    || ((Minecraft189PlayerFishingHookAccess) source)
                            .customMcHasFishingHook())
                throw new IllegalStateException(
                        "physical hotbar selection lacks real no-hook ItemFishingRod");
            phase = 6;
            report("CUSTOMMC_OFFICIAL_189_NATIVE_ROD_PHYSICAL_SLOT_SELECTED_PASS=YES");
            return;
        }

        // The runner now physically right-clicks in the original Mojang
        // X11 game window. A class/name lookup or timer is insufficient:
        // this transition requires an actual non-null fishEntity object.
        if (phase == 6) {
            if (!((Minecraft189PlayerFishingHookAccess) source)
                    .customMcHasFishingHook()) {
                continuousHookCallbacks = 0;
                return;
            }
            // Require the real bobber to persist across multiple live
            // game callbacks rather than briefly observing a transient.
            if (++continuousHookCallbacks < 5) return;
            assertTrue(inventory.customMcSelectedHotbarSlot() == physicalCastSlot,
                    "vanilla cast rewrote real currentItem");
            host.featureCatalog().enableOriginalRodPotionDiscoveryAcceptance();
            assertTrue(host.featureCatalog().autoRod()
                            .preserveActiveHookSetting().get(),
                    "re-enabled source AutoRod lost persistent active-hook guard");
            assertTrue(host.selectAutoRodSlot() == -1,
                    "live original fishing bobber must veto synthetic AutoRod reeling");
            assertTrue(inventory.customMcSelectedHotbarSlot() == physicalCastSlot,
                    "hook guard changed physical selected hotbar slot");
            phase = 7;
            report("CUSTOMMC_OFFICIAL_189_REAL_PHYSICAL_ROD_CAST_ACTIVE_HOOK_GUARD_PASS=YES");
            return;
        }

        // The runner sends a second physical right-click to exercise the
        // *manual vanilla* reel branch, which the optional AutoRod guard
        // intentionally does not intercept. Do not infer server trajectory.
        if (phase == 7) {
            if (((Minecraft189PlayerFishingHookAccess) source)
                    .customMcHasFishingHook()) return;
            assertTrue(inventory.customMcSelectedHotbarSlot() == physicalCastSlot,
                    "manual vanilla reeling changed the real hotbar slot");
            final int acquired = host.selectAutoRodSlot();
            assertTrue(acquired == physicalCastSlot,
                    "source AutoRod failed to reacquire the exact original slot after reel");
            try {
                assertTrue(host.mayExecuteAutoRodUse(acquired),
                        "original source Rod final-use gate did not recover after hook vanished");
            } finally {
                host.restoreAutoRodSlot(acquired);
            }
            assertTrue(inventory.customMcSelectedHotbarSlot() == physicalCastSlot,
                    "post-reel original source hotbar restore wrote a different slot");
            host.featureCatalog().disableOriginalRodPotionDiscoveryAcceptance();
            phase = 8;
            report("CUSTOMMC_OFFICIAL_189_REAL_PHYSICAL_ROD_REEL_HOOK_CLEARED_PASS=YES");
            return;
        }

        if (phase == 8) {
            if (gui) return;
            assertTrue(source instanceof Minecraft189PlayerPotionEffectsAccess,
                    "original EntityLivingBase lacks source PotionEffect getter");
            final Minecraft189ItemStackAccess[] items =
                    inventory.customMcHotbarItems();
            if (items == null || items.length < 9) return;
            int regenSlot = -1;
            for (int i = 0; i < 9; i++) {
                final Minecraft189ItemStackAccess candidate = items[i];
                if (candidate != null && candidate.customMcStackSize() > 0
                        && candidate.customMcIsPotion()
                        && candidate.customMcItemDamage() == 16417) {
                    regenSlot = i;
                    break;
                }
            }
            if (regenSlot < 0 || regenSlot == selected) return;
            assertTrue(!hasLongNativeRegeneration(source),
                    "unmodified creative original player unexpectedly has Regeneration");
            sourceRegenerationPotionSlot = regenSlot;
            host.featureCatalog().enableOriginalRodPotionDiscoveryAcceptance();
            host.featureCatalog().autoPot().autoDetectSlotSetting().set(false);
            host.featureCatalog().autoPot().potSlotSetting().set(regenSlot + 1);
            host.featureCatalog().autoPot().avoidActiveRegenerationSetting()
                    .set(Boolean.TRUE);
            assertTrue(host.featureCatalog().autoPot().active(),
                    "normal AutoPot module enable missing from effect acceptance");
            final int saved = host.selectAutoPotSlot();
            assertTrue(saved == selected,
                    "real source splash regeneration could not acquire no-effect lease");
            try {
                assertTrue(inventory.customMcSelectedHotbarSlot() == regenSlot
                        && host.mayExecuteAutoPotUse(saved),
                        "real no-effect Regeneration splash failed final source use gate");
            } finally {
                host.restoreAutoPotSlot(saved);
            }
            assertTrue(inventory.customMcSelectedHotbarSlot() == selected,
                    "source no-effect Regeneration lease changed physical player slot");
            phase = 9;
            report("CUSTOMMC_OFFICIAL_189_REGEN_NATIVE_NO_EFFECT_POTION_ALLOWED_PASS=YES");
            return;
        }

        if (phase == 9) {
            // Physical vanilla /effect grants weaker Regeneration I (amp 0).
            // Original splash Regen II must remain eligible as an upgrade.
            if (!hasNativeRegeneration(source, 0, 40)) return;
            final int upgraded = host.selectAutoPotSlot();
            assertTrue(upgraded == physicalCastSlot,
                    "original source Regeneration II must upgrade weaker ID10 amp0");
            try {
                assertTrue(inventory.customMcSelectedHotbarSlot()
                                == sourceRegenerationPotionSlot
                        && host.mayExecuteAutoPotUse(upgraded),
                        "genuine stronger source potion was wrongly vetoed by weaker effect");
            } finally {
                host.restoreAutoPotSlot(upgraded);
            }
            assertTrue(inventory.customMcSelectedHotbarSlot() == physicalCastSlot,
                    "Regeneration II upgrade failed physical hotbar rollback");
            phase = 10;
            report("CUSTOMMC_OFFICIAL_189_REGEN_NATIVE_WEAKER_EFFECT_UPGRADE_PASS=YES");
            return;
        }

        if (phase == 10) {
            // Subsequent physical /effect upgrades to Regeneration II
            // (amplifier 1). Another Regeneration II is now redundant.
            if (!hasNativeRegeneration(source, 1, 40)) return;
            assertTrue(host.selectAutoPotSlot() == -1,
                    "original /effect Regeneration 10 must veto duplicate splash");
            assertTrue(inventory.customMcSelectedHotbarSlot() == physicalCastSlot,
                    "active Regeneration veto overwrote human physical currentItem");
            // Negative control: an immediate health potion still protects
            // the player even while Regeneration 10 is active.
            final Minecraft189ItemStackAccess[] sourceItems =
                    inventory.customMcHotbarItems();
            int instantHealthTwo = -1;
            if (sourceItems != null && sourceItems.length >= 9) {
                for (int i = 0; i < 9; i++) {
                    final Minecraft189ItemStackAccess candidate = sourceItems[i];
                    if (candidate != null && candidate.customMcStackSize() > 0
                            && candidate.customMcIsPotion()
                            && candidate.customMcItemDamage() == 16421) {
                        instantHealthTwo = i;
                        break;
                    }
                }
            }
            assertTrue(instantHealthTwo >= 0,
                    "original source Instant Health II disappeared during effect test");
            host.featureCatalog().autoPot().potSlotSetting()
                    .set(instantHealthTwo + 1);
            final int savedHealth = host.selectAutoPotSlot();
            assertTrue(savedHealth == physicalCastSlot,
                    "active Regeneration must not block genuine Instant Health II");
            try {
                assertTrue(inventory.customMcSelectedHotbarSlot() == instantHealthTwo
                                && host.mayExecuteAutoPotUse(savedHealth),
                        "native Instant Health blocked despite verified player effect");
            } finally {
                host.restoreAutoPotSlot(savedHealth);
            }
            assertTrue(inventory.customMcSelectedHotbarSlot() == physicalCastSlot,
                    "real Instant Health exception failed native hotbar restoration");
            host.featureCatalog().autoPot().potSlotSetting()
                    .set(sourceRegenerationPotionSlot + 1);
            phase = 11;
            report("CUSTOMMC_OFFICIAL_189_REGEN_NATIVE_INSTANT_HEAL_EXEMPT_PASS=YES");
            report("CUSTOMMC_OFFICIAL_189_REGEN_NATIVE_ACTIVE_EFFECT_DUPLICATE_VETO_PASS=YES");
            return;
        }

        if (phase == 11) {
            if (hasAnyNativeRegeneration(source)) return;
            final int saved = host.selectAutoPotSlot();
            assertTrue(saved == physicalCastSlot,
                    "original /effect clear did not restore legal Regeneration selection");
            try {
                assertTrue(inventory.customMcSelectedHotbarSlot()
                                == sourceRegenerationPotionSlot
                        && host.mayExecuteAutoPotUse(saved),
                        "cleared original PotionEffect denied true native use");
            } finally {
                host.restoreAutoPotSlot(saved);
            }
            assertTrue(inventory.customMcSelectedHotbarSlot() == physicalCastSlot,
                    "post-clear Regeneration use did not restore actual physical slot");
            host.featureCatalog().disableOriginalRodPotionDiscoveryAcceptance();
            phase = 12;
            report("CUSTOMMC_OFFICIAL_189_REGEN_NATIVE_EFFECT_CLEAR_REARM_PASS=YES");
            return;
        }

        // V142: runner explicitly clears potions and grants genuine source
        // Regen II *before* genuine Instant Health II, then grants actual
        // long Regeneration II effect via the vanilla integrated server.
        // The default held-first source scan would choose the redundant
        // lower-slot Regen item. The opted-in guard must instead select
        // the later source instant-heal item with an owned rollback.
        if (phase == 12) {
            if (gui || !hasNativeRegeneration(source, 1, 40)) return;
            final Minecraft189ItemStackAccess[] items =
                    inventory.customMcHotbarItems();
            if (items == null || items.length < 9) return;
            int regen = -1;
            int heal = -1;
            for (int slot = 0; slot < 9; slot++) {
                final Minecraft189ItemStackAccess item = items[slot];
                if (item == null || item.customMcStackSize() <= 0
                        || !item.customMcIsPotion()) continue;
                if (item.customMcItemDamage() == 16417 && regen < 0)
                    regen = slot;
                if (item.customMcItemDamage() == 16421 && heal < 0)
                    heal = slot;
            }
            if (regen < 0 || heal < 0) return;
            assertTrue(regen < heal && selected == physicalCastSlot,
                    "vanilla /clear and /give did not create native Regen-before-Heal"
                    + " source hotbar order with preserved physical selected rod");
            host.featureCatalog().enableOriginalRodPotionDiscoveryAcceptance();
            final Minecraft189AutoPotModule pot = host.featureCatalog().autoPot();
            pot.autoDetectSlotSetting().set(Boolean.TRUE);
            pot.preferImmediateHealingSetting().set(Boolean.FALSE);
            pot.avoidActiveRegenerationSetting().set(Boolean.FALSE);
            // Negative control: the old V131 selection really would have
            // picked the earlier useless Regen potion in this live world.
            final int oldSelection = host.selectAutoPotSlot();
            assertTrue(oldSelection == physicalCastSlot,
                    "original baseline could not acquire actual source Potion");
            try {
                assertTrue(inventory.customMcSelectedHotbarSlot() == regen
                                && host.mayExecuteAutoPotUse(oldSelection),
                        "pre-v142 unguarded source held-first did not choose Regen II");
            } finally {
                host.restoreAutoPotSlot(oldSelection);
            }
            pot.avoidActiveRegenerationSetting().set(Boolean.TRUE);
            final int useful = host.selectAutoPotSlot();
            assertTrue(useful == physicalCastSlot,
                    "V142 source scan did not continue past useless Regen");
            try {
                assertTrue(inventory.customMcSelectedHotbarSlot() == heal
                                && host.mayExecuteAutoPotUse(useful),
                        "real source Instant Health alternative not selected"
                        + " under active native Regeneration II");
            } finally {
                host.restoreAutoPotSlot(useful);
            }
            assertTrue(inventory.customMcSelectedHotbarSlot() == physicalCastSlot,
                    "alternative Potion native lease overwrote physical hotbar");
            report("CUSTOMMC_OFFICIAL_189_REGEN_NATIVE_AUTODETECT_FALLBACK_HEAL_PASS=YES");

            // V134 strongest-heal preference must compose with viability
            // filtering instead of reinstating the redundant Regen item.
            pot.preferImmediateHealingSetting().set(Boolean.TRUE);
            final int ranked = host.selectAutoPotSlot();
            assertTrue(ranked == physicalCastSlot,
                    "native instant-healing preference failed with regen guard");
            try {
                assertTrue(inventory.customMcSelectedHotbarSlot() == heal
                                && host.mayExecuteAutoPotUse(ranked),
                        "V134 native ranking selected disallowed Regen item");
            } finally {
                host.restoreAutoPotSlot(ranked);
            }
            assertTrue(inventory.customMcSelectedHotbarSlot() == physicalCastSlot,
                    "ranked useful Potion failed original hotbar restoration");

            // V143: a genuinely transformed Mojang player must expose the
            // original Entity.rotationPitch read/write field, not a fake
            // HUD value. The optional native lease aims exactly this
            // source player then restores its real pitch in finally.
            assertTrue(source instanceof Minecraft189PlayerRotationControl,
                    "original client missing native pitch control mapping");
            final Minecraft189PlayerRotationControl pitchOwner =
                    (Minecraft189PlayerRotationControl) source;
            final float humanPitch = pitchOwner.customMcRotationPitch();
            assertTrue(Float.isFinite(humanPitch)
                    && humanPitch >= -90.0F && humanPitch <= 90.0F,
                    "vanilla source pitch unknown/invalid");
            pot.aimDownForSplashSetting().set(Boolean.TRUE);
            final int aimToken = host.selectAutoPotSlot();
            assertTrue(aimToken == physicalCastSlot,
                    "real original splash could not acquire pitch-owned item lease");
            try {
                final Minecraft189AutoPotPitchLease aim =
                        host.beginAutoPotDownwardPitch(aimToken);
                assertTrue(aim != null,
                        "enabled source pitch option did not produce a lease");
                try {
                    assertTrue(pitchOwner.customMcRotationPitch() == 85.0F,
                            "original Entity.rotationPitch setter did not aim downward");
                    assertTrue(host.mayExecuteAutoPotAtPitch(aim, aimToken),
                            "same-player source pitch did not authorize the verified splash");
                    // No right-click here: this test proves rotation mapping
                    // and permission. Actual potion healing needs separate
                    // server-observed splash evidence, not an assumed result.
                } finally {
                    aim.restore();
                }
            } finally {
                host.restoreAutoPotSlot(aimToken);
            }
            assertTrue(pitchOwner.customMcRotationPitch() == humanPitch
                    && inventory.customMcSelectedHotbarSlot() == physicalCastSlot,
                    "native original pitch or item lease not restored");
            report("CUSTOMMC_OFFICIAL_189_AUTOPOT_NATIVE_DOWNWARD_PITCH_RESTORE_PASS=YES");
            host.featureCatalog().disableOriginalRodPotionDiscoveryAcceptance();
            phase = 13;
            report("CUSTOMMC_OFFICIAL_189_REGEN_NATIVE_AUTODETECT_RANK_COMPOSE_PASS=YES");
            return;
        }

        // V144: unlike earlier source selection/permission checks, the
        // vanilla integrated-world player will physically THROW this
        // actual splash item. The preceding effect is cleared using a
        // different vanilla chat command, and must remain absent until
        // an X11 physical original right click causes a true new effect.
        if (phase == 13) {
            if (gui || hasAnyNativeRegeneration(source)) return;
            final Minecraft189ItemStackAccess[] items =
                    inventory.customMcHotbarItems();
            if (items == null || items.length < 9) return;
            int sourceSplash = -1;
            for (int slot = 0; slot < 9; slot++) {
                final Minecraft189ItemStackAccess item = items[slot];
                if (item != null && item.customMcStackSize() > 0
                        && item.customMcIsPotion()
                        && item.customMcItemDamage() == 16449) {
                    sourceSplash = slot;
                    physicalImpactPotion = item;
                    break;
                }
            }
            if (sourceSplash < 0 || sourceSplash == selected) return;
            assertTrue(source instanceof Minecraft189PlayerRotationControl,
                    "real physical splash needs source-mapped Mojang rotationPitch");
            physicalImpactSlot = sourceSplash;
            phase = 14;
            report("CUSTOMMC_OFFICIAL_189_REAL_SPLASH_SOURCE_SLOT="
                    + (sourceSplash + 1));
            return;
        }

        if (phase == 14) {
            if (gui || selected != physicalImpactSlot) return;
            final Minecraft189ItemStackAccess[] now =
                    inventory.customMcHotbarItems();
            assertTrue(now != null && now.length >= 9
                    && now[physicalImpactSlot] == physicalImpactPotion
                    && physicalImpactPotion.customMcIsPotion()
                    && physicalImpactPotion.customMcStackSize() > 0
                    && physicalImpactPotion.customMcItemDamage() == 16449,
                    "physical vanilla splash is not same verified original source ItemStack");
            assertTrue(!hasAnyNativeRegeneration(source),
                    "effect must be absent BEFORE physical native splash throw");
            // Require several independent live Mojang callbacks with the
            // same physical source potion and NO Regeneration. A late
            // command effect from the previous acceptance cannot sneak
            // through a single transient empty snapshot.
            if (++physicalImpactReadyCallbacks < 5) return;
            final Minecraft189PlayerRotationControl rotation =
                    (Minecraft189PlayerRotationControl) source;
            physicalImpactOriginalPitch = rotation.customMcRotationPitch();
            assertTrue(Float.isFinite(physicalImpactOriginalPitch)
                    && physicalImpactOriginalPitch >= -90F
                    && physicalImpactOriginalPitch <= 90F,
                    "physical original source user look pitch invalid");
            rotation.customMcSetRotationPitch(85F);
            physicalImpactPitchOwned =
                    rotation.customMcRotationPitch() == 85F;
            assertTrue(physicalImpactPitchOwned,
                    "real vanilla source cannot aim physical Regeneration splash at own feet");
            phase = 15;
            report("CUSTOMMC_OFFICIAL_189_REAL_SPLASH_PHYSICAL_CAST_READY_PASS=YES");
            return;
        }

        if (phase == 15) {
            // The runner does not issue /effect in this phase. A new
            // PotionEffect ID 10 amp 0 on this SAME local original player
            // can only be reported after its genuine physical splash
            // actually reaches the player through vanilla game mechanics.
            if (!hasNativeRegeneration(source, 0, 20)) {
                physicalImpactEffectCallbacks = 0;
                return;
            }
            // A real vanilla potion effect must persist in the exact
            // original EntityLivingBase over consecutive game callbacks.
            if (++physicalImpactEffectCallbacks < 5) return;
            assertTrue(owner == source && inventory == source.customMcInventory(),
                    "physical splash effect belongs to a foreign/respawn player");
            assertTrue(inventory.customMcSelectedHotbarSlot() == physicalImpactSlot,
                    "physical splash switched the actual human hotbar");
            final Minecraft189PlayerRotationControl rotation =
                    (Minecraft189PlayerRotationControl) source;
            if (physicalImpactPitchOwned
                    && rotation.customMcRotationPitch() == 85F)
                rotation.customMcSetRotationPitch(physicalImpactOriginalPitch);
            assertTrue(rotation.customMcRotationPitch()
                            == physicalImpactOriginalPitch,
                    "true splash must restore original user pitch");
            physicalImpactPitchOwned = false;
            phase = 16;
            report("CUSTOMMC_OFFICIAL_189_REAL_SPLASH_REGEN_IMPACT_SOURCE_EFFECT_PASS=YES");
        }
    }

    private static boolean hasNativeRegeneration(
            final Minecraft189PlayerInventoryAccess source,
            final int requiredAmplifier, final int minimumDuration) {
        final Minecraft189PotionEffectAccess[] effects =
                ((Minecraft189PlayerPotionEffectsAccess) source)
                        .customMcPotionEffects();
        if (effects == null)
            throw new IllegalStateException("original potion effects missing");
        for (final Minecraft189PotionEffectAccess effect : effects) {
            if (effect == null)
                throw new IllegalStateException("null native PotionEffect");
            if (effect.customMcPotionId() == 10
                    && effect.customMcAmplifier() == requiredAmplifier
                    && effect.customMcDurationTicks() >= minimumDuration)
                return true;
        }
        return false;
    }

    private static boolean hasLongNativeRegeneration(
            final Minecraft189PlayerInventoryAccess source) {
        final Minecraft189PotionEffectAccess[] effects =
                ((Minecraft189PlayerPotionEffectsAccess) source)
                        .customMcPotionEffects();
        if (effects == null)
            throw new IllegalStateException("original player potion effects unavailable");
        for (final Minecraft189PotionEffectAccess effect : effects) {
            if (effect == null)
                throw new IllegalStateException("null original potion effect");
            if (effect.customMcPotionId() == 10
                    && effect.customMcDurationTicks() >= 40)
                return true;
        }
        return false;
    }

    private static boolean hasAnyNativeRegeneration(
            final Minecraft189PlayerInventoryAccess source) {
        final Minecraft189PotionEffectAccess[] effects =
                ((Minecraft189PlayerPotionEffectsAccess) source)
                        .customMcPotionEffects();
        if (effects == null)
            throw new IllegalStateException("original player potion effects unavailable");
        for (final Minecraft189PotionEffectAccess effect : effects) {
            if (effect == null)
                throw new IllegalStateException("null original potion effect");
            if (effect.customMcPotionId() == 10) return true;
        }
        return false;
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
