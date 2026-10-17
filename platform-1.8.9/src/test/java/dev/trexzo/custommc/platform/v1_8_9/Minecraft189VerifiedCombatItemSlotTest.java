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
            assertTrue(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                    pot, inventory, player, 4));
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
            assertTrue(Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(
                    rod, inventory, player, 4),
                    "same-type stable new ItemStack is legal next independent check");
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
            assertTrue(Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                    pot, inventory, player, 4));
            pot.restoreSlot(inventory, 4);
            assertEquals(4, inventory.selected);
        } finally {
            rod.onDisable();
            pot.onDisable();
        }
    }

    private static final class Inventory implements Minecraft189InventoryHotbarItemsAccess {
        int selected;
        final Minecraft189ItemStackAccess[] items = new Minecraft189ItemStackAccess[9];
        @Override public Minecraft189ItemStackAccess[] customMcHotbarItems() {
            return items;
        }
        @Override public int customMcSelectedHotbarSlot() { return selected; }
        @Override public void customMcSetSelectedHotbarSlot(final int slot) {
            selected = slot;
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
        final boolean rod;
        final boolean potion;
        final int count;
        Runnable afterRodTypeRead;
        Runnable afterPotionTypeRead;
        FakeItem(final boolean rod, final boolean potion) {
            this(rod, potion, 1);
        }
        FakeItem(final boolean rod, final boolean potion, final int count) {
            this.rod = rod;
            this.potion = potion;
            this.count = count;
        }
        @Override public String customMcDisplayName() { return "untrusted"; }
        @Override public int customMcStackSize() { return count; }
        @Override public int customMcItemDamage() { return 0; }
        @Override public int customMcMaxDamage() { return 0; }
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
