package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189VerifiedCombatItemSlotTest {
    @Test void onlyActualMappedRodTypeCanOwnRightClick() {
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        final Inventory inventory = new Inventory();
        final Player player = new Player(inventory);
        rod.onEnable();
        inventory.selected = 4;
        player.items[1] = new FakeItem(true, false);
        assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                rod, inventory, player));
        assertEquals(1, inventory.selected);
        rod.restoreSlot(inventory, 4);
        assertEquals(4, inventory.selected);

        player.items[1] = new FakeItem(false, true); // potion is not a rod
        assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(
                rod, inventory, player));
        assertEquals(4, inventory.selected);

        player.items[1] = new FakeItem(false, false); // sword/other/unknown
        assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(
                rod, inventory, player));
        assertEquals(4, inventory.selected);

        player.items[1] = null;
        assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(
                rod, inventory, player));
        assertEquals(4, inventory.selected);
        assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(
                rod, inventory, null));
        assertEquals(4, inventory.selected);
        rod.onDisable();
    }

    @Test void onlyActualPotionAndSafeRestoration() {
        final Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        final Inventory inventory = new Inventory();
        final Player player = new Player(inventory);
        inventory.selected = 7;
        pot.onEnable();
        player.items[2] = new FakeItem(false, true);
        assertEquals(7, Minecraft189VerifiedCombatItemSlot.selectPotion(
                pot, inventory, player));
        assertEquals(2, inventory.selected);
        inventory.selected = 6; // user changed slot during use
        pot.restoreSlot(inventory, 7);
        assertEquals(6, inventory.selected);
        player.items[2] = new FakeItem(true, false);
        assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectPotion(
                pot, inventory, player));
        assertEquals(6, inventory.selected);
        pot.onDisable();
    }

    @Test void failureToReadNewlySelectedItemStillRestoresSlot() {
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        rod.onEnable();
        final Inventory inventory = new Inventory();
        inventory.selected = 5;
        final Minecraft189PlayerHeldItemAccess broken = () -> {
            throw new IllegalStateException("item access failed");
        };
        assertThrows(IllegalStateException.class,
                () -> Minecraft189VerifiedCombatItemSlot.selectRod(
                        rod, inventory, broken));
        assertEquals(5, inventory.selected);
        rod.onDisable();
    }


    @Test void emptyMappedRodAndPotionStacksCannotSpendAUseLease() {
        final Inventory inventory = new Inventory();
        final Player player = new Player(inventory);
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        final Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        rod.onEnable();
        pot.onEnable();
        try {
            inventory.selected = 4;
            for (final int count : new int[] {0, -1}) {
                player.items[1] = new FakeItem(true, false, count);
                assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(
                        rod, inventory, player));
                assertEquals(4, inventory.selected);
                player.items[2] = new FakeItem(false, true, count);
                assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectPotion(
                        pot, inventory, player));
                assertEquals(4, inventory.selected);
            }
            player.items[1] = new FakeItem(true, false, 1);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inventory, player));
            rod.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);
            player.items[2] = new FakeItem(false, true, 1);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            pot.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);
        } finally {
            rod.onDisable();
            pot.onDisable();
        }
    }

    @Test void reentrantUserHotbarChangeRevokesRodAndPotItemIdentity() {
        final Inventory inventory = new Inventory();
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        final Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        rod.onEnable();
        pot.onEnable();
        try {
            inventory.selected = 4;
            // The native getter may run code that moves the original
            // player's hotbar slot. Returning a verified type alone is
            // insufficient: module-owned slot must still be selected.
            final Minecraft189PlayerHeldItemAccess movedRod = () -> {
                inventory.selected = 6;
                return new FakeItem(true, false);
            };
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inventory, movedRod));
            assertEquals(6, inventory.selected,
                    "the user-selected slot must never be overwritten");
            inventory.selected = 4;
            final Minecraft189PlayerHeldItemAccess movedPotion = () -> {
                inventory.selected = 7;
                return new FakeItem(false, true);
            };
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, movedPotion));
            assertEquals(7, inventory.selected);

            // No zombie lease may remain after either failed admission.
            final Player player = new Player(inventory);
            inventory.selected = 4;
            player.items[1] = new FakeItem(true, false);
            player.items[2] = new FakeItem(false, true);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inventory, player));
            rod.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            pot.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);
        } finally {
            rod.onDisable();
            pot.onDisable();
        }
    }

    @Test void disablingModuleDuringNativeItemReadCannotAuthorizeUse() {
        final Inventory inventory = new Inventory();
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        final Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        rod.onEnable();
        pot.onEnable();
        inventory.selected = 4;
        final Minecraft189PlayerHeldItemAccess disabledRod = () -> {
            rod.onDisable();
            return new FakeItem(true, false);
        };
        assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(
                rod, inventory, disabledRod));
        assertEquals(4, inventory.selected);
        final Minecraft189PlayerHeldItemAccess disabledPotion = () -> {
            pot.onDisable();
            return new FakeItem(false, true);
        };
        assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectPotion(
                pot, inventory, disabledPotion));
        assertEquals(4, inventory.selected);
    }


    @Test void originalRodRequiresSameValidItemAgainImmediatelyBeforeUse() {
        final Inventory inventory = new Inventory();
        final Player player = new Player(inventory);
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        rod.onEnable();
        try {
            inventory.selected = 4;
            player.items[1] = new FakeItem(true, false, 1);
            final int original = Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inventory, player);
            assertEquals(4, original);
            assertTrue(Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(
                    rod, inventory, player, original));

            // Source stack may change between first verification and
            // AutoBlock's stopUsingItem callback; exact mapped type and
            // count must still be valid at the final use boundary.
            player.items[1] = new FakeItem(false, true, 1);
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(
                    rod, inventory, player, original));
            player.items[1] = new FakeItem(true, false, 0);
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(
                    rod, inventory, player, original));
            player.items[1] = new FakeItem(true, false, 1);
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(
                    rod, inventory, player, 3),
                    "a foreign original-slot token cannot spend this lease");
            inventory.selected = 6;
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(
                    rod, inventory, player, original));
            rod.restoreSlot(inventory, original);
            assertEquals(6, inventory.selected,
                    "the original user's new slot must never be restored away");
            inventory.selected = 4;
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inventory, player));
            rod.onDisable();
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(
                    rod, inventory, player, 4));
            rod.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);
        } finally {
            rod.onDisable();
        }
    }

    @Test void originalPotionUseRechecksAfterNativeGetterReentrancy() {
        final Inventory inventory = new Inventory();
        final Player player = new Player(inventory);
        final Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        pot.onEnable();
        try {
            inventory.selected = 4;
            player.items[2] = new FakeItem(false, true, 1);
            final int original = Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player);
            assertEquals(4, original);
            assertTrue(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                    pot, inventory, player, original));
            final Minecraft189PlayerHeldItemAccess changedDuringGet = () -> {
                inventory.selected = 7;
                return new FakeItem(false, true, 1);
            };
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                    pot, inventory, changedDuringGet, original),
                    "a later user slot change invalidates even a valid potion");
            assertEquals(7, inventory.selected);
            pot.restoreSlot(inventory, original);
            assertEquals(7, inventory.selected);

            inventory.selected = 4;
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            player.items[2] = new FakeItem(false, true, -1);
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                    pot, inventory, player, 4));
            player.items[2] = new FakeItem(true, false, 1);
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                    pot, inventory, player, 4));
            player.items[2] = new FakeItem(false, true, 1);
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                    pot, inventory, player, 4),
                    "new source ItemStack must never reanimate old use permission");
            pot.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            assertTrue(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                    pot, inventory, player, 4),
                    "explicitly renewed source use is valid");
            pot.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);
        } finally {
            pot.onDisable();
        }
    }


    @Test void sourceRodPotionReplacedDuringInitialNativeTypeReadNeverPublishLease() {
        final Inventory inventory = new Inventory();
        final Player player = new Player(inventory);
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        final Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        rod.onEnable();
        pot.onEnable();
        try {
            inventory.selected = 4;
            final FakeItem initialRod = new FakeItem(true, false);
            player.items[1] = initialRod;
            initialRod.afterRodTypeRead = () ->
                    player.items[1] = new FakeItem(true, false);
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inventory, player),
                    "a same-type replacement during getter cannot authorize a stale rod");
            assertEquals(4, inventory.selected,
                    "initial failure must restore original hotbar slot");
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inventory, player),
                    "a later stable replacement rod must work normally");
            rod.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);

            final FakeItem initialPotion = new FakeItem(false, true);
            player.items[2] = initialPotion;
            initialPotion.afterPotionTypeRead = () ->
                    player.items[2] = new FakeItem(true, false);
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player),
                    "a source potion replaced by rod during getter must veto use");
            assertEquals(4, inventory.selected);
            player.items[2] = new FakeItem(false, true);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            pot.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);
        } finally {
            rod.onDisable();
            pot.onDisable();
        }
    }

    @Test void sourceRodPotionReplacedDuringFinalNativeTypeReadCannotSpendClick() {
        final Inventory inventory = new Inventory();
        final Player player = new Player(inventory);
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        final Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        rod.onEnable();
        pot.onEnable();
        try {
            inventory.selected = 4;
            final FakeItem rodBefore = new FakeItem(true, false);
            player.items[1] = rodBefore;
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inventory, player));
            rodBefore.afterRodTypeRead = () ->
                    player.items[1] = new FakeItem(true, false);
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(
                    rod, inventory, player, 4),
                    "matching rod type is insufficient after ItemStack replacement");
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(
                    rod, inventory, player, 4),
                    "same-type replacement cannot borrow the old action lease");
            rod.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inventory, player));
            assertTrue(Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(
                    rod, inventory, player, 4),
                    "separately selected stable replacement can now cast");
            rod.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);

            final FakeItem potionBefore = new FakeItem(false, true);
            player.items[2] = potionBefore;
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            potionBefore.afterPotionTypeRead = () ->
                    player.items[2] = new FakeItem(false, true);
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                    pot, inventory, player, 4));
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                    pot, inventory, player, 4),
                    "new same-type potion cannot adopt a replaced source lease");
            pot.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            assertTrue(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                    pot, inventory, player, 4));
            pot.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);
        } finally {
            rod.onDisable();
            pot.onDisable();
        }
    }


    @Test void inPlaceSourceRodPotionStackDepletionDuringNativeTypeGetterFailsClosed() {
        final Inventory inventory = new Inventory();
        final Player player = new Player(inventory);
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        final Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        rod.onEnable();
        pot.onEnable();
        try {
            inventory.selected = 4;
            final FakeItem rodItem = new FakeItem(true, false, 1);
            player.items[1] = rodItem;
            rodItem.afterRodTypeRead = () -> rodItem.count = 0;
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inventory, player),
                    "source getter depleted *same* original ItemStack wrapper");
            assertEquals(4, inventory.selected);
            rodItem.count = 1;
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inventory, player));
            rod.restoreSlot(inventory, 4);

            final FakeItem potionItem = new FakeItem(false, true, 1);
            player.items[2] = potionItem;
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            potionItem.afterPotionTypeRead = () -> potionItem.count = 0;
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                    pot, inventory, player, 4),
                    "in-place potion depletion after AutoBlock release vetoes click");
            assertEquals(2, inventory.selected);
            potionItem.count = 1;
            assertTrue(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                    pot, inventory, player, 4),
                    "a genuinely restored stable source potion may be checked anew");
            pot.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);
        } finally {
            rod.onDisable();
            pot.onDisable();
        }
    }

    @Test void sourceTypeChangingInPlaceDuringGetterNeverBorrowsOldClassEvidence() {
        final Inventory inventory = new Inventory();
        final Player player = new Player(inventory);
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        final Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        rod.onEnable();
        pot.onEnable();
        try {
            inventory.selected = 4;
            final FakeItem rodStack = new FakeItem(true, false, 1);
            player.items[1] = rodStack;
            rodStack.afterRodTypeRead = () -> { rodStack.rod = false; rodStack.potion = true; };
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inventory, player));
            assertEquals(4, inventory.selected);

            final FakeItem potionStack = new FakeItem(false, true, 1);
            player.items[2] = potionStack;
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            potionStack.afterPotionTypeRead = () -> {
                potionStack.potion = false;
                potionStack.rod = true;
            };
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                    pot, inventory, player, 4));
            pot.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);
        } finally {
            rod.onDisable();
            pot.onDisable();
        }
    }


    @Test void nativeSetterNoOpOrUserRedirectCannotPublishRodOrPotionLease() {
        final Inventory inventory = new Inventory();
        final Player player = new Player(inventory);
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        final Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        rod.onEnable();
        pot.onEnable();
        try {
            inventory.selected = 4;
            player.items[1] = new FakeItem(true, false);
            player.items[2] = new FakeItem(false, true);
            inventory.ignoreNextSet = true;
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inventory, player),
                    "ignored native rod setter cannot create a lease");
            assertEquals(4, inventory.selected);
            inventory.redirectNextSetTo = 7;
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player),
                    "reentrant user slot selection defeats a potion lease");
            assertEquals(7, inventory.selected,
                    "the user's redirected source currentItem survives");
            inventory.selected = 4;
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inventory, player));
            rod.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            pot.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);
        } finally {
            rod.onDisable();
            pot.onDisable();
        }
    }

    @Test void nativeSetterDisablingModuleReleasesOnlyItsOwnTemporarySlot() {
        final Inventory inventory = new Inventory();
        final Player player = new Player(inventory);
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        final Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        rod.onEnable();
        pot.onEnable();
        try {
            inventory.selected = 4;
            player.items[1] = new FakeItem(true, false);
            player.items[2] = new FakeItem(false, true);
            inventory.afterNextSet = rod::onDisable;
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inventory, player));
            assertEquals(4, inventory.selected);
            rod.onEnable();
            inventory.afterNextSet = pot::onDisable;
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            assertEquals(4, inventory.selected);
            pot.onEnable();
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inventory, player));
            rod.restoreSlot(inventory, 4);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            pot.restoreSlot(inventory, 4);
        } finally {
            rod.onDisable();
            pot.onDisable();
        }
    }


    @Test void nativeAutoDiscoveryIsOptInAndNeverInventsItemTypes() {
        final Inventory inv = new Inventory();
        final Player player = new Player(inv);
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        final Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        rod.onEnable();
        pot.onEnable();
        try {
            inv.selected = 4;
            player.items[6] = new FakeItem(true, false);
            player.items[7] = new FakeItem(false, true);
            assertFalse(rod.autoDetectSlotSetting().get());
            assertFalse(pot.autoDetectSlotSetting().get());
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(rod, inv, player));
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectPotion(pot, inv, player));
            assertEquals(4, inv.selected);
            rod.autoDetectSlotSetting().set(true);
            pot.autoDetectSlotSetting().set(true);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(rod, inv, player));
            assertEquals(6, inv.selected);
            assertTrue(Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(rod, inv, player, 4));
            rod.restoreSlot(inv, 4);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(pot, inv, player));
            assertEquals(7, inv.selected);
            assertTrue(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(pot, inv, player, 4));
            pot.restoreSlot(inv, 4);
            assertEquals(4, inv.selected);
            inv.selected = 6;
            player.items[1] = new FakeItem(true, false);
            assertEquals(6, Minecraft189VerifiedCombatItemSlot.selectRod(rod, inv, player));
            assertEquals(6, inv.selected, "verified already-held rod must be preferred");
            rod.restoreSlot(inv, 6);
        } finally { rod.onDisable(); pot.onDisable(); }
    }

    @Test void nativeAutoDiscoveryVetoesGetterUserSlotChangeAndEmptyStacks() {
        final Inventory inv = new Inventory();
        final Player player = new Player(inv);
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        final Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        rod.autoDetectSlotSetting().set(true);
        pot.autoDetectSlotSetting().set(true);
        rod.onEnable();
        pot.onEnable();
        try {
            inv.selected = 4;
            player.items[1] = new FakeItem(true, false, 0);
            player.items[2] = new FakeItem(false, true, -1);
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(rod, inv, player));
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectPotion(pot, inv, player));
            final FakeItem good = new FakeItem(true, false);
            player.items[8] = good;
            good.afterRodTypeRead = () -> inv.selected = 5;
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(rod, inv, player));
            assertEquals(5, inv.selected, "preserve real user currentItem");
            inv.selected = 4;
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(rod, inv, player));
            assertEquals(8, inv.selected);
            rod.restoreSlot(inv, 4);
            player.items[2] = new FakeItem(false, true);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(pot, inv, player));
            assertEquals(2, inv.selected);
            pot.restoreSlot(inv, 4);
        } finally { rod.onDisable(); pot.onDisable(); }
    }


    @Test void splashHealingMetadataExcludesWaterDrinksAndHarmfulPotions() {
        final Inventory inv = new Inventory();
        final Player player = new Player(inv);
        final Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        pot.onEnable();
        try {
            inv.selected = 4;
            final FakeItem item = new FakeItem(false, true);
            player.items[2] = item;
            for (final int invalid : new int[] {
                    0, 8197, 8229, 16388, 16420, 16396, 16428, -1
            }) {
                item.damage = invalid;
                assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectPotion(
                        pot, inv, player), "invalid potion data " + invalid);
                assertEquals(4, inv.selected);
            }
            for (final int valid : new int[] {16389, 16421, 16385, 16417, 16449}) {
                item.damage = valid;
                assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(
                        pot, inv, player));
                assertTrue(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                        pot, inv, player, 4));
                item.damage = 16396;
                assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                        pot, inv, player, 4), "poison/harming cannot use stale lease");
                pot.restoreSlot(inv, 4);
                assertEquals(4, inv.selected);
            }
        } finally { pot.onDisable(); }
    }

    @Test void autoDiscoverySkipsWaterAndRejectsNativeDamageGetterMutation() {
        final Inventory inv = new Inventory();
        final Player player = new Player(inv);
        final Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        pot.autoDetectSlotSetting().set(true);
        pot.onEnable();
        try {
            inv.selected = 4;
            final FakeItem water = new FakeItem(false, true);
            water.damage = 0;
            player.items[1] = water;
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inv, player));
            final FakeItem heal = new FakeItem(false, true);
            player.items[7] = heal;
            heal.afterDamageRead = () -> heal.damage = 16388;
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inv, player), "metadata getter mutated healing to poison");
            assertEquals(4, inv.selected);
            heal.damage = 16421;
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inv, player), "skip earlier water potion");
            assertEquals(7, inv.selected);
            heal.afterDamageRead = () -> heal.damage = 16396;
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                    pot, inv, player, 4), "final use must recheck native damage");
            pot.restoreSlot(inv, 4);
            assertEquals(4, inv.selected);
        } finally { pot.onDisable(); }
    }


    @Test void optInRankedPotionsPreferActualInstantHealingOverEarlierRegeneration() {
        final Inventory inventory = new Inventory();
        final Player player = new Player(inventory);
        final Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        pot.autoDetectSlotSetting().set(true);
        pot.onEnable();
        try {
            inventory.selected = 4;
            final FakeItem regeneration = new FakeItem(false, true);
            regeneration.damage = 16417;
            player.items[1] = regeneration;
            final FakeItem instantOne = new FakeItem(false, true);
            instantOne.damage = 16389;
            player.items[2] = instantOne;
            final FakeItem instantTwo = new FakeItem(false, true);
            instantTwo.damage = 16421;
            player.items[6] = instantTwo;
            // Previous V131 selection remains unchanged by default.
            assertFalse(pot.preferImmediateHealingSetting().get());
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            assertEquals(1, inventory.selected);
            pot.restoreSlot(inventory, 4);

            pot.preferImmediateHealingSetting().set(true);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            assertEquals(6, inventory.selected);
            assertTrue(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                    pot, inventory, player, 4));
            pot.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);

            // Current lower-rank regeneration is deliberately bypassed.
            inventory.selected = 1;
            assertEquals(1, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            assertEquals(6, inventory.selected);
            pot.restoreSlot(inventory, 1);
            assertEquals(1, inventory.selected);

            // Rank tie is stable and preserves already-held source slot.
            final FakeItem sameRank = new FakeItem(false, true);
            sameRank.damage = 16421;
            player.items[3] = sameRank;
            inventory.selected = 3;
            assertEquals(3, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            assertEquals(3, inventory.selected);
            pot.restoreSlot(inventory, 3);
            assertEquals(3, inventory.selected);
        } finally { pot.onDisable(); }
    }

    @Test void rankedPotionSearchDoesNotBorrowReentrantMetadataOrChangedHotbar() {
        final Inventory inventory = new Inventory();
        final Player player = new Player(inventory);
        final Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        pot.autoDetectSlotSetting().set(true);
        pot.preferImmediateHealingSetting().set(true);
        pot.onEnable();
        try {
            inventory.selected = 4;
            final FakeItem stable = new FakeItem(false, true);
            stable.damage = 16389;
            player.items[2] = stable;
            final FakeItem candidate = new FakeItem(false, true);
            candidate.damage = 16421;
            player.items[7] = candidate;
            candidate.afterDamageRead = () -> {
                // Source callback replaces a higher-priority item with water.
                player.items[7] = new FakeItem(false, true);
                ((FakeItem) player.items[7]).damage = 0;
            };
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            assertEquals(2, inventory.selected,
                    "displaced stronger wrapper must not outrank stable heal");
            pot.restoreSlot(inventory, 4);

            // Candidate class getter changes the actual user selected slot.
            final FakeItem switched = new FakeItem(false, true);
            switched.damage = 16421;
            player.items[7] = switched;
            switched.afterPotionTypeRead = () -> inventory.selected = 5;
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            assertEquals(5, inventory.selected);
            inventory.selected = 4;
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            assertEquals(7, inventory.selected);
            pot.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);
        } finally { pot.onDisable(); }
    }


    @Test void optInRodDurabilityPriorityPrefersOriginalPristineRod() {
        final Inventory inv = new Inventory();
        final Player player = new Player(inv);
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        rod.autoDetectSlotSetting().set(true);
        rod.onEnable();
        try {
            inv.selected = 4;
            final FakeItem worn = new FakeItem(true, false);
            worn.damage = 60;
            player.items[1] = worn;
            final FakeItem good = new FakeItem(true, false);
            good.damage = 0;
            player.items[6] = good;

            assertFalse(rod.preferDurableRodSetting().get(),
                    "legacy held-first/lowest slot remains default");
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inv, player));
            assertEquals(1, inv.selected);
            rod.restoreSlot(inv, 4);

            rod.preferDurableRodSetting().set(true);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inv, player));
            assertEquals(6, inv.selected,
                    "original source currentItem chooses most remaining durability");
            assertTrue(Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(
                    rod, inv, player, 4));
            rod.restoreSlot(inv, 4);
            assertEquals(4, inv.selected);

            inv.selected = 1;
            assertEquals(1, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inv, player));
            assertEquals(6, inv.selected, "worn held rod must lose to fresh rod");
            rod.restoreSlot(inv, 1);
            assertEquals(1, inv.selected);

            final FakeItem same = new FakeItem(true, false);
            same.damage = 0;
            player.items[3] = same;
            inv.selected = 3;
            assertEquals(3, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inv, player));
            assertEquals(3, inv.selected,
                    "same durability prioritizes genuine already-held rod");
            rod.restoreSlot(inv, 3);
            assertEquals(3, inv.selected);
        } finally { rod.onDisable(); }
    }

    @Test void rodDurabilityRankingRejectsUnknownBrokenAndNativeMutation() {
        final Inventory inv = new Inventory();
        final Player player = new Player(inv);
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        rod.autoDetectSlotSetting().set(true);
        rod.preferDurableRodSetting().set(true);
        rod.onEnable();
        try {
            inv.selected = 4;
            final FakeItem stable = new FakeItem(true, false);
            stable.damage = 35;
            player.items[2] = stable;
            final FakeItem broken = new FakeItem(true, false);
            broken.damage = broken.maxDamage;
            player.items[1] = broken;
            final FakeItem unknown = new FakeItem(true, false);
            unknown.maxDamage = 0;
            player.items[3] = unknown;
            final FakeItem nearlyFresh = new FakeItem(true, false);
            nearlyFresh.damage = 1;
            player.items[7] = nearlyFresh;
            nearlyFresh.afterDamageRead = () ->
                    player.items[7] = new FakeItem(true, false);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inv, player));
            assertEquals(2, inv.selected,
                    "displaced original wrapper must not outrank known stable rod");
            rod.restoreSlot(inv, 4);

            final FakeItem mutate = new FakeItem(true, false);
            mutate.damage = 1;
            player.items[7] = mutate;
            mutate.afterMaxDamageRead = () -> inv.selected = 5;
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inv, player),
                    "source durability getter changing user currentItem must reject scan");
            assertEquals(5, inv.selected);
            inv.selected = 4;
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inv, player));
            assertEquals(7, inv.selected);
            rod.restoreSlot(inv, 4);
            assertEquals(4, inv.selected);

            inv.selected = 4;
            mutate.afterDamageRead = () -> mutate.damage = mutate.maxDamage;
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inv, player));
            assertEquals(2, inv.selected,
                    "changed native durability must not outrank stable source rod");
            rod.restoreSlot(inv, 4);
        } finally { rod.onDisable(); }
    }

    @Test void nativeRodProtectionSkipsFragileHeldRodAndUsesHealthyAlternative() {
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        final Inventory inv = new Inventory();
        final Player player = new Player(inv);
        inv.selected = 4;
        final FakeItem worn = new FakeItem(true, false);
        worn.damage = 62; // only 2 uses left of original 64
        final FakeItem fresh = new FakeItem(true, false);
        inv.items[4] = worn;
        inv.items[7] = fresh;
        rod.onEnable();
        try {
            rod.autoDetectSlotSetting().set(true);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inv, player));
            assertEquals(4, inv.selected,
                    "legacy original held-first choice stays unchanged");
            rod.restoreSlot(inv, 4);
            rod.protectRodDurabilitySetting().set(true);
            rod.minimumRemainingDurabilitySetting().set(3);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inv, player));
            assertEquals(7, inv.selected,
                    "protect mode must skip nearly broken held rod");
            assertTrue(Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(
                    rod, inv, player, 4));
            fresh.damageReads = 0;
            fresh.mutateAfterDamageReadNumber = 3;
            fresh.afterNthDamageRead = () -> fresh.damage = 63;
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(
                    rod, inv, player, 4),
                    "third original source damage getter reentrantly wears the"
                    + " same ItemStack immediately before synthetic use");
            fresh.mutateAfterDamageReadNumber = 0;
            fresh.damage = 62;
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(
                    rod, inv, player, 4),
                    "source rod must be revalidated just before actual rightclick");
            rod.restoreSlot(inv, 4);
            assertEquals(4, inv.selected);
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inv, player),
                    "no usable original rod must not steal source hotbar");
            assertEquals(4, inv.selected);

            fresh.damage = 0;
            rod.autoDetectSlotSetting().set(false);
            rod.rodSlotSetting().set(5); // configured worn, not fresh
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inv, player),
                    "configured original fragile rod is also vetoed");
            assertEquals(4, inv.selected);
            rod.autoDetectSlotSetting().set(true);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inv, player));
            assertEquals(7, inv.selected);
            rod.minimumRemainingDurabilitySetting().set(5);
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(
                    rod, inv, player, 4),
                    "GUI threshold changed during native lease revokes use");
            rod.restoreSlot(inv, 4);
            assertEquals(4, inv.selected);
        } finally { rod.onDisable(); }
    }

    @Test void protectedRodRankingKeepsV135DurablePreferenceWithoutUnknownMetadata() {
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        final Inventory inv = new Inventory();
        final Player player = new Player(inv);
        inv.selected = 4;
        final FakeItem worn = new FakeItem(true, false);
        worn.damage = 60; // 4 left
        final FakeItem sturdy = new FakeItem(true, false);
        sturdy.damage = 10; // 54 left
        inv.items[2] = worn;
        inv.items[7] = sturdy;
        rod.onEnable();
        try {
            rod.autoDetectSlotSetting().set(true);
            rod.protectRodDurabilitySetting().set(true);
            rod.minimumRemainingDurabilitySetting().set(3);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inv, player));
            assertEquals(2, inv.selected, "default eligible lowest-slot wins");
            rod.restoreSlot(inv, 4);
            rod.preferDurableRodSetting().set(true);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inv, player));
            assertEquals(7, inv.selected,
                    "existing V135 strongest remaining durability wins");
            rod.restoreSlot(inv, 4);
            sturdy.afterMaxDamageRead = () -> sturdy.maxDamage = 0;
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inv, player));
            assertEquals(2, inv.selected,
                    "invalid source metadata cannot win guarded durable scan");
            rod.restoreSlot(inv, 4);
            rod.minimumRemainingDurabilitySetting().set(5);
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inv, player),
                    "every source rod beneath minimum must fail closed");
            assertEquals(4, inv.selected);
        } finally { rod.onDisable(); }
    }

    @Test void nativeRodUsePinsExactOriginalItemAndRejectsLateGuiIntentChanges() {
        final Inventory inventory = new Inventory();
        final Player player = new Player(inventory);
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        inventory.selected = 4;
        inventory.items[1] = new FakeItem(true, false);
        rod.onEnable();
        try {
            rod.autoDetectSlotSetting().set(true);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inventory, player));
            assertEquals(1, inventory.selected);
            assertTrue(Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(
                    rod, inventory, player, 4));
            // Even a second REAL rod of equal class/durability in the same
            // source slot is not the exact ItemStack that owned this cast.
            inventory.items[1] = new FakeItem(true, false);
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(
                    rod, inventory, player, 4));
            rod.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);
            // A fresh independent selection can explicitly own the new rod.
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inventory, player));
            assertTrue(Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(
                    rod, inventory, player, 4));
            rod.preferDurableRodSetting().set(true);
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(
                    rod, inventory, player, 4),
                    "live GUI durability ranking change revokes old cast");
            rod.restoreSlot(inventory, 4);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inventory, player));
            rod.preserveActiveHookSetting().set(true);
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(
                    rod, inventory, player, 4),
                    "live hook policy change revokes in-flight native cast");
            rod.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);
        } finally { rod.onDisable(); }
    }

    @Test void nativePotionUsePinsSameSourceStackAndCapturedHealingPolicy() {
        final Inventory inventory = new Inventory();
        final Player player = new Player(inventory);
        final Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        inventory.selected = 4;
        inventory.items[2] = new FakeItem(false, true);
        pot.onEnable();
        try {
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            assertTrue(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                    pot, inventory, player, 4));
            inventory.items[2] = new FakeItem(false, true);
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                    pot, inventory, player, 4),
                    "same vanilla healing type cannot replace captured ItemStack");
            pot.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            assertTrue(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                    pot, inventory, player, 4));
            pot.preferImmediateHealingSetting().set(true);
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                    pot, inventory, player, 4),
                    "changing potion rank while item selected revokes cast");
            pot.restoreSlot(inventory, 4);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            pot.aimDownForSplashSetting().set(true);
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                    pot, inventory, player, 4),
                    "changed physical splash aim cannot inherit a stale lease");
            pot.restoreSlot(inventory, 4);
            assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            pot.healthThresholdSetting().set(20D);
            assertFalse(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                    pot, inventory, player, 4),
                    "modified health threshold must re-admit the action");
            pot.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);
        } finally { pot.onDisable(); }
    }

    @Test void sourceItemGetterChangingSelectionPolicyAbortsBeforeNativeHotbarWrite() {
        final Inventory inventory = new Inventory();
        final Player player = new Player(inventory);
        inventory.selected = 4;
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        final FakeItem nativeRod = new FakeItem(true, false);
        inventory.items[1] = nativeRod;
        rod.autoDetectSlotSetting().set(true);
        rod.onEnable();
        try {
            nativeRod.afterRodTypeRead = () ->
                    rod.autoDetectSlotSetting().set(false);
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(
                    rod, inventory, player));
            assertEquals(4, inventory.selected,
                    "stale rod candidate must not select from changed GUI intent");
        } finally { rod.onDisable(); }

        final Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        final FakeItem nativePotion = new FakeItem(false, true);
        inventory.items[2] = nativePotion;
        pot.autoDetectSlotSetting().set(true);
        pot.onEnable();
        try {
            nativePotion.afterPotionTypeRead = () ->
                    pot.autoDetectSlotSetting().set(false);
            assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectPotion(
                    pot, inventory, player));
            assertEquals(4, inventory.selected,
                    "stale healing candidate must not select from changed GUI intent");
        } finally { pot.onDisable(); }
    }

    private static final class Inventory implements Minecraft189InventoryHotbarItemsAccess {
        int selected;
        boolean ignoreNextSet;
        int redirectNextSetTo = -1;
        Runnable afterNextSet;
        final Minecraft189ItemStackAccess[] items = new Minecraft189ItemStackAccess[9];
        @Override public Minecraft189ItemStackAccess[] customMcHotbarItems() {
            return items;
        }
        @Override public int customMcSelectedHotbarSlot() { return selected; }
        @Override public void customMcSetSelectedHotbarSlot(final int slot) {
            if (ignoreNextSet) {
                ignoreNextSet = false;
                return;
            }
            if (redirectNextSetTo >= 0) {
                selected = redirectNextSetTo;
                redirectNextSetTo = -1;
                return;
            }
            selected = slot;
            final Runnable callback = afterNextSet;
            afterNextSet = null;
            if (callback != null) callback.run();
        }
    }

    private static final class Player implements Minecraft189PlayerHeldItemAccess {
        final Inventory inventory;
        final Minecraft189ItemStackAccess[] items;
        Player(final Inventory inventory) {
            this.inventory = inventory;
            this.items = inventory.items;
        }
        @Override public Minecraft189ItemStackAccess customMcHeldItem() {
            return items[inventory.selected];
        }
    }

    private static final class FakeItem implements Minecraft189ItemStackAccess {
        boolean rod;
        boolean potion;
        int count;
        Runnable afterRodTypeRead;
        Runnable afterPotionTypeRead;
        Runnable afterDamageRead;
        Runnable afterMaxDamageRead;
        Runnable afterNthDamageRead;
        int damageReads;
        int mutateAfterDamageReadNumber;
        int damage;
        int maxDamage;
        FakeItem(final boolean rod, final boolean potion) {
            this(rod, potion, 1);
        }
        FakeItem(final boolean rod, final boolean potion, final int count) {
            this.rod = rod;
            this.potion = potion;
            this.count = count;
            this.damage = potion ? 16389 : 0;
            this.maxDamage = rod ? 64 : 0;
        }
        @Override public String customMcDisplayName() { return "untrusted"; }
        @Override public int customMcStackSize() { return count; }
        @Override public int customMcItemDamage() {
            final Runnable callback = afterDamageRead;
            afterDamageRead = null;
            if (callback != null) callback.run();
            damageReads++;
            if (mutateAfterDamageReadNumber == damageReads) {
                final Runnable nth = afterNthDamageRead;
                afterNthDamageRead = null;
                if (nth != null) nth.run();
            }
            return damage;
        }
        @Override public int customMcMaxDamage() {
            final Runnable callback = afterMaxDamageRead;
            afterMaxDamageRead = null;
            if (callback != null) callback.run();
            return maxDamage;
        }
        @Override public boolean customMcIsFishingRod() {
            final Runnable callback = afterRodTypeRead;
            afterRodTypeRead = null;
            if (callback != null) callback.run();
            return rod;
        }
        @Override public boolean customMcIsPotion() {
            final Runnable callback = afterPotionTypeRead;
            afterPotionTypeRead = null;
            if (callback != null) callback.run();
            return potion;
        }
    }
}
