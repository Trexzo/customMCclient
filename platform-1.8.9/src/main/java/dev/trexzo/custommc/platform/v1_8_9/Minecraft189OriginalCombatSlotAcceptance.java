package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Opt-in physical X11/Mojang 1.8.9 original InventoryPlayer.currentItem
 * acceptance. Observes genuine transformed local-player inventory callback
 * and executes the real CombatSlot module's normal one-action lease.
 *
 * This fixture never calls Minecraft.clickMouse, performs an actual attack,
 * fabricates item stacks, or infers server inventory synchronization.
 */
final class Minecraft189OriginalCombatSlotAcceptance {
    private static final String PROPERTY =
            "custommc.acceptance.requireNativeCombatSlotGuiFocus";
    private static int phase;
    private static int consecutiveBaselines;
    private static Minecraft189PlayerInventoryAccess ownerPlayer;
    private static Minecraft189InventoryHotbarControl ownerInventory;
    private static int baselineSlot = -1;
    private static int selectedSlot = -1;

    private Minecraft189OriginalCombatSlotAcceptance() {}

    static synchronized void observe(
            final Minecraft189HostRuntime host,
            final Minecraft189PlayerInventoryAccess sourcePlayer) {
        if (!Boolean.getBoolean(PROPERTY) || phase == 3
                || sourcePlayer == null
                || !host.playerMovementState().snapshot().available())
            return;
        final Minecraft189InventoryHotbarAccess mapped =
                sourcePlayer.customMcInventory();
        if (!(mapped instanceof Minecraft189InventoryHotbarControl)) return;
        final Minecraft189InventoryHotbarControl inventory =
                (Minecraft189InventoryHotbarControl) mapped;
        final int nativeSlot = inventory.customMcSelectedHotbarSlot();
        if (nativeSlot < 0 || nativeSlot > 8) return;
        final boolean guiOpen = host.clickGuiRuntime()
                .coreRuntime().model().snapshot().open();

        if (phase == 0) {
            if (guiOpen || host.featureCatalog().combatSlot().active()) {
                consecutiveBaselines = 0;
                return;
            }
            if (sourcePlayer != ownerPlayer || inventory != ownerInventory
                    || nativeSlot != baselineSlot) {
                ownerPlayer = sourcePlayer;
                ownerInventory = inventory;
                baselineSlot = nativeSlot;
                consecutiveBaselines = 0;
            }
            if (++consecutiveBaselines < 12) return;
            selectedSlot = (baselineSlot + 1) % 9;
            host.featureCatalog().enableCombatSlotForOriginalGuiAcceptance(
                    selectedSlot + 1);
            requireState(host.featureCatalog().combatSlot().active(),
                    "CombatSlot was not enabled by native ModuleController");
            verifyTemporaryLease(host, inventory, selectedSlot, baselineSlot,
                    "initial genuine currentItem lease");
            phase = 1;
            report("CUSTOMMC_OFFICIAL_189_COMBATSLOT_NATIVE_SELECTION_RESTORE_PASS=YES");
            return;
        }
        // Never substitute one local player or InventoryPlayer instance for
        // the owner of the recorded original lease across a GUI transition.
        requireState(sourcePlayer == ownerPlayer
                && inventory == ownerInventory,
                "V108 source native player/inventory changed across focus");
        requireState(nativeSlot == baselineSlot,
                "V108 native user hotbar baseline changed unexpectedly");
        requireState(host.featureCatalog().combatSlot().active(),
                "V108 source CombatSlot unexpectedly disabled");
        if (phase == 1) {
            if (!guiOpen) return;
            final int blocked = host.selectCombatSlotBeforeSyntheticClick(true);
            requireState(blocked == -1,
                    "V108 ClickGUI failed to veto original CombatSlot selection");
            requireState(inventory.customMcSelectedHotbarSlot() == baselineSlot,
                    "V108 native GUI focus changed InventoryPlayer.currentItem");
            phase = 2;
            report("CUSTOMMC_OFFICIAL_189_COMBATSLOT_GUI_REAL_KEY_OPEN_VETO_PASS=YES");
            return;
        }
        if (phase == 2 && !guiOpen) {
            final int prior = host.selectCombatSlotBeforeSyntheticClick(true);
            requireState(prior == baselineSlot,
                    "V108 source CombatSlot did not acquire its original lease");
            requireState(inventory.customMcSelectedHotbarSlot() == selectedSlot,
                    "V108 source setter failed to select actual preferred slot");
            // V117 source-native wrong-token proof: original currentItem is
            // selected, but an unrelated positive prior-slot token cannot
            // consume the live host owner or let another synthetic selection
            // take over this player's currentItem. Use the real Mojang object
            // and the production HostRuntime paths, not a fabricated slot.
            final int wrongPrior = (baselineSlot + 3) % 9;
            host.restoreCombatSlotAfterSyntheticClick(wrongPrior);
            requireState(inventory.customMcSelectedHotbarSlot() == selectedSlot,
                    "V117 foreign positive token changed original Mojang currentItem");
            requireState(host.selectCombatSlotBeforeSyntheticClick(true) == -1,
                    "V117 foreign positive token released original hotbar owner");
            host.restoreCombatSlotAfterSyntheticClick(-1);
            requireState(inventory.customMcSelectedHotbarSlot() == selectedSlot,
                    "V117 nested -1 finally changed original Mojang currentItem");
            report("CUSTOMMC_OFFICIAL_189_COMBATSLOT_NATIVE_WRONG_TOKEN_VETO_PASS=YES");
            // V105 core regression using *real* original InventoryPlayer:
            // edit ClickGUI setting DURING the native slot lease, then
            // restore the exact captured target instead of mutable config.
            final int changedTarget = (baselineSlot + 2) % 9;
            host.featureCatalog().combatSlot().preferredSlotSetting().set(
                    Integer.valueOf(changedTarget + 1));
            host.restoreCombatSlotAfterSyntheticClick(prior);
            requireState(inventory.customMcSelectedHotbarSlot() == baselineSlot,
                    "V108 changed GUI preferredSlot broke original slot restore");
            report("CUSTOMMC_OFFICIAL_189_COMBATSLOT_GUI_REAL_KEY_CLOSE_CONFIG_LEASE_PASS=YES");
            host.featureCatalog().disableCombatSlotForOriginalGuiAcceptance();
            requireState(!host.featureCatalog().combatSlot().active()
                    && inventory.customMcSelectedHotbarSlot() == baselineSlot,
                    "V108 native module disable did not preserve original currentItem");
            phase = 3;
            report("CUSTOMMC_OFFICIAL_189_COMBATSLOT_NATIVE_DISABLE_ORIGINAL_PASS=YES");
        }
    }

    private static void verifyTemporaryLease(
            final Minecraft189HostRuntime host,
            final Minecraft189InventoryHotbarControl inventory,
            final int expectedSelected, final int original, final String phase) {
        final int prior = host.selectCombatSlotBeforeSyntheticClick(true);
        requireState(prior == original,
                "V108 " + phase + " missing original lease token");
        requireState(inventory.customMcSelectedHotbarSlot() == expectedSelected,
                "V108 " + phase + " no original native currentItem selection");
        host.restoreCombatSlotAfterSyntheticClick(prior);
        requireState(inventory.customMcSelectedHotbarSlot() == original,
                "V108 " + phase + " no original currentItem restoration");
    }

    private static void requireState(final boolean allowed, final String stage) {
        if (!allowed) throw new IllegalStateException(stage);
    }

    private static void report(final String marker) {
        System.out.println(marker);
        System.out.flush();
    }
}
